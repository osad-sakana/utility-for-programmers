package osadsakana.utilitiesforprogrammers;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
 */
public final class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("utilitiesforprogrammers-client.json");
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

    /** Loads the config from disk (creating it with defaults if absent). Call once at startup. */
    public static void load() {
        readFromDisk();
        writeToDisk();
    }

    /** Re-reads the file from disk if its modification time changed since the last load. */
    public static void reloadIfChanged() {
        try {
            if (!Files.exists(PATH)) {
                return;
            }
            final long modified = Files.getLastModifiedTime(PATH).toMillis();
            if (modified != lastLoadedModifiedMillis) {
                readFromDisk();
            }
        } catch (IOException e) {
            // Leave in-memory values as-is; the file may be mid-write.
        }
    }

    private static void readFromDisk() {
        if (!Files.exists(PATH)) {
            lastLoadedModifiedMillis = -1L;
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            final JsonElement root = JsonParser.parseReader(reader);
            if (root != null && root.isJsonObject()) {
                final JsonObject rootObject = root.getAsJsonObject();
                for (Value<?> value : ALL) {
                    value.readFrom(rootObject);
                }
            }
            lastLoadedModifiedMillis = Files.getLastModifiedTime(PATH).toMillis();
        } catch (IOException | RuntimeException e) {
            // Keep current (default or previously loaded) values on a malformed file.
        }
    }

    private static void writeToDisk() {
        final JsonObject root = new JsonObject();
        for (Value<?> value : ALL) {
            value.writeTo(root);
        }
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            lastLoadedModifiedMillis = Files.getLastModifiedTime(PATH).toMillis();
        } catch (IOException e) {
            // Non-fatal: the mod continues to run with in-memory defaults.
        }
    }

    /** Accepts 1-8 hex digits (RGB or ARGB). */
    public static boolean isHexColor(String value) {
        return value != null && value.matches("(?i)[0-9a-f]{1,8}");
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

        StringValue(String category, String key, String defaultValue, Predicate<String> validator) {
            super(category, key, defaultValue);
            this.validator = validator;
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
                        return;
                    }
                }
            }
            value = defaultValue;
        }

        @Override
        void writeTo(JsonObject root) {
            section(root).addProperty(key, value);
        }
    }
}
