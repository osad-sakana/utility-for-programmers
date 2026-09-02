package osadsakana.utilitiesforprogrammers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Exercises {@link Config}'s hand-rolled JSON store without a running
 * {@link net.fabricmc.loader.api.FabricLoader} instance, via the
 * package-private {@code Path}-parameterized overloads.
 *
 * <p>{@link Config}'s {@code Value}s are {@code static final} singletons shared
 * across all test methods in this class, so every test resets them to defaults in
 * {@link #resetToDefaults()} rather than assuming a clean slate.
 */
class ConfigTest {

    /**
     * Must exceed any filesystem's mtime granularity (some are 1-2s) so the bumped
     * timestamp in {@link #reloadIfChangedReloadsWhenMtimeChanges} is unambiguously
     * "later" everywhere, not just on high-resolution filesystems.
     */
    private static final long MTIME_BUMP_MILLIS = 5000L;

    /** Fixed, far-from-"now" mtime used to make the stale-cursor regression unambiguous. */
    private static final long SENTINEL_MTIME_MILLIS = 1_000_000_000_000L;

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetToDefaults() throws IOException {
        // An empty object leaves every registered Value with no matching element,
        // so readFrom() falls each one back to its default.
        Config.readFromDisk(writeJson("{}"));
    }

    @Test
    void intValueClampsAboveMax() throws IOException {
        final Path path = writeJson("{\"highlight\":{\"radius\":999}}");
        assertTrue(Config.readFromDisk(path));
        assertEquals(128, Config.HIGHLIGHT_RADIUS.get());
    }

    @Test
    void intValueClampsBelowMin() throws IOException {
        final Path path = writeJson("{\"highlight\":{\"radius\":-5}}");
        Config.readFromDisk(path);
        assertEquals(1, Config.HIGHLIGHT_RADIUS.get());
    }

    @Test
    void doubleValueClampsToRange() throws IOException {
        final Path path = writeJson("{\"highlight\":{\"displaySeconds\":999.0}}");
        Config.readFromDisk(path);
        assertEquals(120.0, Config.HIGHLIGHT_SECONDS.get());
    }

    @Test
    void booleanValueRoundTrips() throws IOException {
        final Path path = writeJson("{\"hud\":{\"enabled\":false}}");
        Config.readFromDisk(path);
        assertFalse(Config.HUD_ENABLED.get());
    }

    @Test
    void colorValueRejectsBadHexAndFallsBackToDefault() throws IOException {
        final Path path = writeJson("{\"targetHighlight\":{\"outlineColorARGB\":\"not-hex\"}}");
        Config.readFromDisk(path);
        assertEquals("FFFFEE00", Config.TARGET_HL_COLOR.get());
        assertEquals(Config.parseColor("FFFFEE00"), Config.TARGET_HL_COLOR.getArgb());
    }

    @Test
    void colorValueAcceptsValidHexAndCachesArgb() throws IOException {
        final Path path = writeJson("{\"targetHighlight\":{\"outlineColorARGB\":\"AABBCCDD\"}}");
        Config.readFromDisk(path);
        assertEquals("AABBCCDD", Config.TARGET_HL_COLOR.get());
        assertEquals(Config.parseColor("AABBCCDD"), Config.TARGET_HL_COLOR.getArgb());
    }

    @Test
    void parseColorHandlesFullRangeAndInvalidInput() {
        assertEquals(-1, Config.parseColor("FFFFFFFF"));
        assertEquals(0, Config.parseColor("not-hex"));
        assertTrue(Config.isHexColor("FFFFFFFF"));
        assertTrue(Config.isHexColor("F"));
        assertFalse(Config.isHexColor("123456789")); // 9 digits: too long
        assertFalse(Config.isHexColor(null));
    }

    @Test
    void readFromDiskTreatsNonObjectJsonAsFailure() throws IOException {
        final Path path = writeJson("[1,2,3]");
        assertFalse(Config.readFromDisk(path));
    }

    @Test
    void malformedJsonKeepsPreviousValuesAndDoesNotRewriteFile() throws IOException {
        // Establish a known non-default value first.
        final Path path = writeJson("{\"highlight\":{\"radius\":50}}");
        Config.readFromDisk(path);
        assertEquals(50, Config.HIGHLIGHT_RADIUS.get());

        // Corrupt the file (trailing comma is invalid JSON) and attempt to load it.
        final byte[] malformed = "{\"highlight\":{\"radius\":50,}}".getBytes(StandardCharsets.UTF_8);
        Files.write(path, malformed);
        Config.load(path);

        // The in-memory value must survive a parse failure...
        assertEquals(50, Config.HIGHLIGHT_RADIUS.get());
        // ...and the malformed file must be left exactly as the user wrote it,
        // never silently clobbered with regenerated defaults.
        assertArrayEquals(malformed, Files.readAllBytes(path));
    }

    @Test
    void loadWritesDefaultsWhenFileIsAbsent() throws IOException {
        final Path path = tempDir.resolve("absent.json");
        assertFalse(Files.exists(path));

        Config.load(path);

        assertTrue(Files.exists(path));
        final JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        assertEquals(32, root.getAsJsonObject("highlight").get("radius").getAsInt());
        assertEquals("FFFFEE00", root.getAsJsonObject("targetHighlight").get("outlineColorARGB").getAsString());
        assertFalse(Files.exists(path.resolveSibling(path.getFileName() + ".tmp")));
    }

    @Test
    void loadMergesNewlyRegisteredKeysIntoAnExistingFileOnSuccessfulParse() throws IOException {
        // Simulate an older config file that only has one key set.
        final Path path = writeJson("{\"highlight\":{\"radius\":77}}");

        Config.load(path);

        final JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        // The explicitly-set value survives the merge...
        assertEquals(77, root.getAsJsonObject("highlight").get("radius").getAsInt());
        // ...and every other registered key is now present too, with its default.
        assertTrue(root.has("hud"));
        assertTrue(root.has("grid"));
    }

    @Test
    void reloadIfChangedNoOpWhenFileAbsent() {
        final Path path = tempDir.resolve("nope.json");
        assertDoesNotThrow(() -> Config.reloadIfChanged(path));
    }

    @Test
    void reloadIfChangedSkipsWhenMtimeUnchanged() throws IOException {
        final Path path = writeJson("{\"highlight\":{\"radius\":10}}");
        // Unconditional first read establishes the baseline mtime for this file.
        Config.readFromDisk(path);
        assertEquals(10, Config.HIGHLIGHT_RADIUS.get());

        // Mutate in-memory only, bypassing the file, so a real reload would be observable.
        Config.HIGHLIGHT_RADIUS.value = 999;

        Config.reloadIfChanged(path); // same file, mtime unchanged -> must be a no-op
        assertEquals(999, Config.HIGHLIGHT_RADIUS.get());
    }

    @Test
    void reloadIfChangedReloadsWhenMtimeChanges() throws IOException {
        final Path path = writeJson("{\"highlight\":{\"radius\":10}}");
        Config.readFromDisk(path);
        assertEquals(10, Config.HIGHLIGHT_RADIUS.get());

        Files.writeString(path, "{\"highlight\":{\"radius\":20}}", StandardCharsets.UTF_8);
        Files.setLastModifiedTime(path,
                FileTime.fromMillis(Files.getLastModifiedTime(path).toMillis() + MTIME_BUMP_MILLIS));

        Config.reloadIfChanged(path);
        assertEquals(20, Config.HIGHLIGHT_RADIUS.get());
    }

    /**
     * Regression test for a HIGH bug that was fixed and then, in review, found not
     * to be covered by any test: a failed parse must still advance
     * {@code lastLoadedModifiedMillis}, or {@code reloadIfChanged} re-parses (and
     * re-logs) an unchanged-but-broken file on every single poll forever.
     */
    @Test
    void malformedFileFailureAdvancesTrackedMtimeSoUnchangedPollsAreSkipped() throws IOException {
        final Path path = writeJson("{\"highlight\":{\"radius\":50,}}"); // malformed: trailing comma
        // Pin the mtime to a fixed sentinel so a *stale* cursor (the regression)
        // can never coincidentally equal it -- two temp files created microseconds
        // apart otherwise routinely share the same millisecond.
        Files.setLastModifiedTime(path, FileTime.fromMillis(SENTINEL_MTIME_MILLIS));

        Config.load(path); // parse fails; must still advance the tracked mtime (the fix under test)
        final long mtimeAfterFailure = Files.getLastModifiedTime(path).toMillis();
        // Assert the invariant this test is named after, directly.
        assertEquals(mtimeAfterFailure, Config.lastLoadedModifiedMillis);

        // Replace the content with valid, distinguishable JSON but restore the
        // exact same mtime. A *correct* reloadIfChanged sees "unchanged" (matches
        // what was recorded above) and skips it. A regression that failed to
        // advance the tracked mtime on the parse failure would instead see this as
        // still "changed" relative to its stale cursor and re-read it.
        Files.writeString(path, "{\"highlight\":{\"radius\":999}}", StandardCharsets.UTF_8);
        Files.setLastModifiedTime(path, FileTime.fromMillis(mtimeAfterFailure));

        Config.reloadIfChanged(path);

        // Fixed behavior: skipped, so the value is untouched (still the default
        // from @BeforeEach) rather than having picked up 999.
        assertEquals(32, Config.HIGHLIGHT_RADIUS.get());
    }

    @Test
    void writeBackPreservesKeysThisVersionDoesNotRecognize() throws IOException {
        final Path path = writeJson(
                "{\"highlight\":{\"radius\":50},\"_comment\":\"keep me\",\"futureSection\":{\"newOption\":true}}");

        Config.load(path);

        final JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        assertEquals(50, root.getAsJsonObject("highlight").get("radius").getAsInt());
        assertEquals("keep me", root.get("_comment").getAsString());
        assertTrue(root.getAsJsonObject("futureSection").get("newOption").getAsBoolean());
    }

    @Test
    void writeBackPreservesAFutureKeyAddedInsideAnExistingSection() throws IOException {
        // The scenario the merge fix actually targets: a newer mod version adds a
        // key to a category ("highlight") this version has already registered
        // other keys for, so the section is reused (Value#section), not recreated.
        final Path path = writeJson("{\"highlight\":{\"radius\":50,\"futureKnob\":7}}");

        Config.load(path);

        final JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        final JsonObject highlight = root.getAsJsonObject("highlight");
        assertEquals(50, highlight.get("radius").getAsInt());
        assertEquals(7, highlight.get("futureKnob").getAsInt());
    }

    private Path writeJson(String json) throws IOException {
        final Path path = tempDir.resolve("config-" + System.nanoTime() + ".json");
        Files.writeString(path, json, StandardCharsets.UTF_8);
        return path;
    }
}
