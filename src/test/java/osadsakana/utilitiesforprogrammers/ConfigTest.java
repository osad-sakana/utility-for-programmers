package osadsakana.utilitiesforprogrammers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Exercises {@link Config}'s hand-rolled JSON store without a running
 * {@link net.fabricmc.loader.api.FabricLoader} instance, via the
 * package-private {@code Path}-parameterized overloads.
 */
class ConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void intValueClampsAboveMax() throws IOException {
        final Path path = writeJson("{\"highlight\":{\"radius\":999}}");
        Config.readFromDisk(path);
        assertEquals(128, Config.HIGHLIGHT_RADIUS.get());
    }

    @Test
    void intValueClampsBelowMin() throws IOException {
        final Path path = writeJson("{\"highlight\":{\"radius\":-5}}");
        Config.readFromDisk(path);
        assertEquals(1, Config.HIGHLIGHT_RADIUS.get());
    }

    @Test
    void stringValueRejectsBadHexAndFallsBackToDefault() throws IOException {
        final Path path = writeJson("{\"targetHighlight\":{\"outlineColorARGB\":\"not-hex\"}}");
        Config.readFromDisk(path);
        assertEquals("FFFFEE00", Config.TARGET_HL_COLOR.get());
        assertEquals(Config.parseColor("FFFFEE00"), Config.TARGET_HL_COLOR.getArgb());
    }

    @Test
    void stringValueAcceptsValidHexAndCachesArgb() throws IOException {
        final Path path = writeJson("{\"targetHighlight\":{\"outlineColorARGB\":\"AABBCCDD\"}}");
        Config.readFromDisk(path);
        assertEquals("AABBCCDD", Config.TARGET_HL_COLOR.get());
        assertEquals(Config.parseColor("AABBCCDD"), Config.TARGET_HL_COLOR.getArgb());
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
    void loadWritesDefaultsOnlyWhenFileIsAbsent() throws IOException {
        final Path path = tempDir.resolve("absent.json");
        assertFalse(Files.exists(path));

        Config.load(path);

        assertTrue(Files.exists(path));
    }

    private Path writeJson(String json) throws IOException {
        final Path path = tempDir.resolve("config-" + System.nanoTime() + ".json");
        Files.writeString(path, json, StandardCharsets.UTF_8);
        return path;
    }
}
