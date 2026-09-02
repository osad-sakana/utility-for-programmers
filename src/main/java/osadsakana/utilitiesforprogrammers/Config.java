package osadsakana.utilitiesforprogrammers;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Client configuration for UtilitiesForProgrammers.
 *
 * <p>Fabric has no built-in equivalent of NeoForge's {@code ModConfigSpec}, so this
 * is a small hand-rolled JSON store persisted to
 * {@code config/utilitiesforprogrammers-client.json}. All settings are read at
 * render time via each value's {@code get()}. {@link #reloadIfChanged()} is polled
 * periodically (see {@code ClientEvents.onClientTickPost}) so editing the file (or a
 * future in-game config screen) takes effect without restarting, matching the
 * previous NeoForge behavior.
 *
 * <p>Note on ranges: {@code radius}/{@code fillAlpha}/etc. are silently clamped to
 * the ranges documented in {@code README.md} rather than rejected, so a hand-edited
 * out-of-range value never breaks loading.
 */
public final class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger("UtilitiesForProgrammers/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "utilitiesforprogrammers-client.json";
    private static final List<Value<?>> ALL = new ArrayList<>();

    private static long lastLoadedModifiedMillis = -1L;

    // ----- HUD -----------------------------------------------------------------
    public static final BooleanValue HUD_ENABLED =
            bool("hud", "enabled", true);

    // ----- Block-update highlight ---------------------------------------------
    public static final BooleanValue HIGHLIGHT_ENABLED =
            bool("highlight", "enabled", true);
    /** How long (seconds) a highlight stays visible before it has fully faded out. */
    public static final DoubleValue HIGHLIGHT_SECONDS =
            doubleVal("highlight", "displaySeconds", 8.0D, 0.5D, 120.0D);
    /** Only block updates within this many blocks of the player are highlighted. */
    public static final IntValue HIGHLIGHT_RADIUS =
            intVal("highlight", "radius", 32, 1, 128);
    /** Draw filled translucent boxes in addition to the wireframe outline. */
    public static final BooleanValue HIGHLIGHT_FILL =
            bool("highlight", "drawFilledBox", true);

    // ----- Relative-coordinate grid -------------------------------------------
    public static final BooleanValue GRID_ENABLED =
            bool("grid", "enabled", true);
    /** Half-size of the grid (number of blocks drawn in each direction). */
    public static final IntValue GRID_RADIUS =
            intVal("grid", "radius", 8, 1, 32);

    // ----- Looking-at (target) block highlight --------------------------------
    public static final BooleanValue TARGET_HL_ENABLED =
            bool("targetHighlight", "enabled", true);
    /** Outline color of the looking-at block, ARGB hex (e.g. {@code FFFFEE00}). */
    public static final StringValue TARGET_HL_COLOR =
            str("targetHighlight", "outlineColorARGB", "FFFFEE00", Config::isHexColor);
    public static final BooleanValue TARGET_HL_FILL =
            bool("targetHighlight", "drawFill", true);
    /** Alpha (0-255) of the translucent fill for the looking-at block. */
    public static final IntValue TARGET_HL_FILL_ALPHA =
            intVal("targetHighlight", "fillAlpha", 48, 0, 255);

    // ----- Window focus border -------------------------------------------------
    public static final BooleanValue FOCUS_BORDER_ENABLED =
            bool("focusBorder", "enabled", true);
    public static final BooleanValue FOCUS_BORDER_WHEN_FOCUSED =
            bool("focusBorder", "showWhenFocused", true);
    public static final BooleanValue FOCUS_BORDER_WHEN_UNFOCUSED =
            bool("focusBorder", "showWhenUnfocused", true);
    public static final IntValue FOCUS_BORDER_THICKNESS =
            intVal("focusBorder", "thickness", 4, 1, 32);
    /** Border color while focused, as ARGB hex (e.g. {@code CC55FF55}). */
    public static final StringValue FOCUS_BORDER_COLOR_FOCUSED =
            str("focusBorder", "focusedColorARGB", "CC55FF55", Config::isHexColor);
    /** Border color while unfocused, as ARGB hex (e.g. {@code CCFF5555}). */
    public static final StringValue FOCUS_BORDER_COLOR_UNFOCUSED =
            str("focusBorder", "unfocusedColorARGB", "CCFF5555", Config::isHexColor);

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    /**
     * Loads the config from disk, creating it with defaults only if the file is
     * absent. Call once at startup.
     */
    public static void load() {
        load(configPath());
    }

    /** Re-reads the file from disk if its modification time changed since the last load. */
    public static void reloadIfChanged() {
        reloadIfChanged(configPath());
    }

    /**
     * {@link #load()}, parameterized on the file path so it can be exercised in
     * unit tests without a running {@link FabricLoader} instance.
     *
     * <p>An existing file is never overwritten here, even if it fails to parse:
     * clobbering a hand-edited file with defaults on a typo would destroy the
     * user's settings with no way back.
     */
    static void load(Path path) {
        if (Files.exists(path)) {
            readFromDisk(path);
        } else {
            writeToDisk(path);
        }
    }

    /** {@link #reloadIfChanged()}, parameterized on the file path for unit tests. */
    static void reloadIfChanged(Path path) {
        try {
            if (!Files.exists(path)) {
                return;
            }
            final long modified = Files.getLastModifiedTime(path).toMillis();
            if (modified != lastLoadedModifiedMillis) {
                readFromDisk(path);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to check {} for changes; keeping current values", path, e);
        }
    }

    static void readFromDisk(Path path) {
        if (!Files.exists(path)) {
            lastLoadedModifiedMillis = -1L;
            return;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            final JsonElement root = JsonParser.parseReader(reader);
            if (root != null && root.isJsonObject()) {
                final JsonObject rootObject = root.getAsJsonObject();
                for (Value<?> value : ALL) {
                    value.readFrom(rootObject);
                }
            }
            lastLoadedModifiedMillis = Files.getLastModifiedTime(path).toMillis();
        } catch (IOException | RuntimeException e) {
            // Keep current (default or previously loaded) values on a malformed file;
            // deliberately does NOT write, so the user's file is left intact to fix.
            LOGGER.error("Failed to read {}; keeping current values and leaving the file untouched", path, e);
        }
    }

    static void writeToDisk(Path path) {
        final JsonObject root = new JsonObject();
        for (Value<?> value : ALL) {
            value.writeTo(root);
        }
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            final Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            // Atomic swap so a crash mid-write can never leave a truncated/corrupt file.
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            lastLoadedModifiedMillis = Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            // Non-fatal: the mod continues to run with in-memory defaults.
            LOGGER.error("Failed to write {}; continuing with in-memory values", path, e);
        }
    }

    private static final Pattern HEX_COLOR = Pattern.compile("(?i)[0-9a-f]{1,8}");

    /** Accepts 1-8 hex digits (RGB or ARGB). */
    public static boolean isHexColor(String value) {
        return value != null && HEX_COLOR.matcher(value).matches();
    }

    /** Parse an ARGB hex string to an int color, or transparent black if invalid. */
    public static int parseColor(String hex) {
        try {
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static BooleanValue bool(String category, String key, boolean defaultValue) {
        final BooleanValue value = new BooleanValue(category, key, defaultValue);
        ALL.add(value);
        return value;
    }

    private static IntValue intVal(String category, String key, int defaultValue, int min, int max) {
        final IntValue value = new IntValue(category, key, defaultValue, min, max);
        ALL.add(value);
        return value;
    }

    private static DoubleValue doubleVal(String category, String key, double defaultValue, double min, double max) {
        final DoubleValue value = new DoubleValue(category, key, defaultValue, min, max);
        ALL.add(value);
        return value;
    }

    private static StringValue str(String category, String key, String defaultValue, Predicate<String> validator) {
        final StringValue value = new StringValue(category, key, defaultValue, validator);
        ALL.add(value);
        return value;
    }

    private Config() {
    }

    // `value` is mutated in place (rather than replaced via a new Config instance)
    // because these are `static final` singletons read every frame by name
    // (e.g. Config.HUD_ENABLED.get()); it is `volatile` and only ever written from
    // the client thread (tick-driven load/reload), so this is safe despite the
    // project's general immutability preference.
    private abstract static class Value<T> {
        final String category;
        final String key;
        final T defaultValue;
        volatile T value;

        Value(String category, String key, T defaultValue) {
            this.category = category;
            this.key = key;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        abstract void readFrom(JsonObject root);

        abstract void writeTo(JsonObject root);

        JsonObject section(JsonObject root) {
            final JsonElement existing = root.get(category);
            if (existing != null && existing.isJsonObject()) {
                return existing.getAsJsonObject();
            }
            final JsonObject section = new JsonObject();
            root.add(category, section);
            return section;
        }
    }

    public static final class BooleanValue extends Value<Boolean> {
        BooleanValue(String category, String key, boolean defaultValue) {
            super(category, key, defaultValue);
        }

        @Override
        void readFrom(JsonObject root) {
            final JsonElement section = root.get(category);
            if (section != null && section.isJsonObject()) {
                final JsonElement element = section.getAsJsonObject().get(key);
                if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                    value = element.getAsBoolean();
                    return;
                }
            }
            value = defaultValue;
        }

        @Override
        void writeTo(JsonObject root) {
            section(root).addProperty(key, value);
        }
    }

    public static final class IntValue extends Value<Integer> {
        final int min;
        final int max;

        IntValue(String category, String key, int defaultValue, int min, int max) {
            super(category, key, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        void readFrom(JsonObject root) {
            final JsonElement section = root.get(category);
            if (section != null && section.isJsonObject()) {
                final JsonElement element = section.getAsJsonObject().get(key);
                if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    final int candidate = element.getAsInt();
                    value = clamp(candidate);
                    return;
                }
            }
            value = defaultValue;
        }

        int clamp(int candidate) {
            return Math.max(min, Math.min(max, candidate));
        }

        @Override
        void writeTo(JsonObject root) {
            section(root).addProperty(key, value);
        }
    }

    public static final class DoubleValue extends Value<Double> {
        final double min;
        final double max;

        DoubleValue(String category, String key, double defaultValue, double min, double max) {
            super(category, key, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        void readFrom(JsonObject root) {
            final JsonElement section = root.get(category);
            if (section != null && section.isJsonObject()) {
                final JsonElement element = section.getAsJsonObject().get(key);
                if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    final double candidate = element.getAsDouble();
                    value = Math.max(min, Math.min(max, candidate));
                    return;
                }
            }
            value = defaultValue;
        }

        @Override
        void writeTo(JsonObject root) {
            section(root).addProperty(key, value);
        }
    }

    public static final class StringValue extends Value<String> {
        final Predicate<String> validator;
        /** Cached {@link Config#parseColor(String)} of {@link #value}, refreshed alongside it. */
        private volatile int argb;

        StringValue(String category, String key, String defaultValue, Predicate<String> validator) {
            super(category, key, defaultValue);
            this.validator = validator;
            this.argb = parseColor(defaultValue);
        }

        /** The current value pre-parsed as an ARGB int; avoids re-parsing hex every frame. */
        public int getArgb() {
            return argb;
        }

        @Override
        void readFrom(JsonObject root) {
            final JsonElement section = root.get(category);
            if (section != null && section.isJsonObject()) {
                final JsonElement element = section.getAsJsonObject().get(key);
                if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    final String candidate = element.getAsString();
                    if (validator.test(candidate)) {
                        value = candidate;
                        argb = parseColor(candidate);
                        return;
                    }
                }
            }
            value = defaultValue;
            argb = parseColor(defaultValue);
        }

        @Override
        void writeTo(JsonObject root) {
            section(root).addProperty(key, value);
        }
    }
}
