package osadsakana.utilitiesforprogrammers;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client configuration for UtilitiesForProgrammers.
 *
 * <p>Values are persisted by NeoForge to
 * {@code config/utilitiesforprogrammers-client.toml}. All settings are read at
 * render time, so editing the file (or using the in-game config screen) takes
 * effect without restarting.
 */
public final class Config {

    public static final ModConfigSpec SPEC;

    // ----- HUD -----------------------------------------------------------------
    public static final ModConfigSpec.BooleanValue HUD_ENABLED;

    // ----- Block-update highlight ---------------------------------------------
    public static final ModConfigSpec.BooleanValue HIGHLIGHT_ENABLED;
    /** How long (seconds) a highlight stays visible before it has fully faded out. */
    public static final ModConfigSpec.DoubleValue HIGHLIGHT_SECONDS;
    /** Only block updates within this many blocks of the player are highlighted. */
    public static final ModConfigSpec.IntValue HIGHLIGHT_RADIUS;
    /** Draw filled translucent boxes in addition to the wireframe outline. */
    public static final ModConfigSpec.BooleanValue HIGHLIGHT_FILL;

    // ----- Relative-coordinate grid -------------------------------------------
    public static final ModConfigSpec.BooleanValue GRID_ENABLED;
    /** Half-size of the grid (number of blocks drawn in each direction). */
    public static final ModConfigSpec.IntValue GRID_RADIUS;

    // ----- Looking-at (target) block highlight --------------------------------
    public static final ModConfigSpec.BooleanValue TARGET_HL_ENABLED;
    /** Outline color of the looking-at block, ARGB hex (e.g. {@code FFFFEE00}). */
    public static final ModConfigSpec.ConfigValue<String> TARGET_HL_COLOR;
    public static final ModConfigSpec.BooleanValue TARGET_HL_FILL;
    /** Alpha (0-255) of the translucent fill for the looking-at block. */
    public static final ModConfigSpec.IntValue TARGET_HL_FILL_ALPHA;

    // ----- Window focus border -------------------------------------------------
    public static final ModConfigSpec.BooleanValue FOCUS_BORDER_ENABLED;
    public static final ModConfigSpec.BooleanValue FOCUS_BORDER_WHEN_FOCUSED;
    public static final ModConfigSpec.BooleanValue FOCUS_BORDER_WHEN_UNFOCUSED;
    public static final ModConfigSpec.IntValue FOCUS_BORDER_THICKNESS;
    /** Border color while focused, as ARGB hex (e.g. {@code CC55FF55}). */
    public static final ModConfigSpec.ConfigValue<String> FOCUS_BORDER_COLOR_FOCUSED;
    /** Border color while unfocused, as ARGB hex (e.g. {@code CCFF5555}). */
    public static final ModConfigSpec.ConfigValue<String> FOCUS_BORDER_COLOR_UNFOCUSED;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("HUD: absolute coordinates, facing direction and target-block inspector")
                .push("hud");
        HUD_ENABLED = builder
                .comment("Master switch for the top-left HUD overlay.")
                .define("enabled", true);
        builder.pop();

        builder.comment("Client-side highlighting of block updates, colored by placement order")
                .push("highlight");
        HIGHLIGHT_ENABLED = builder
                .comment("Master switch for block-update highlighting.")
                .define("enabled", true);
        HIGHLIGHT_SECONDS = builder
                .comment("Seconds a highlight remains before fully fading out.")
                .defineInRange("displaySeconds", 8.0D, 0.5D, 120.0D);
        HIGHLIGHT_RADIUS = builder
                .comment("Detection radius in blocks around the player.")
                .defineInRange("radius", 32, 1, 128);
        HIGHLIGHT_FILL = builder
                .comment("Also draw a translucent filled box (in addition to the outline).")
                .define("drawFilledBox", true);
        builder.pop();

        builder.comment("Relative-coordinate ground grid centered on the player")
                .push("grid");
        GRID_ENABLED = builder
                .comment("Master switch for the relative-coordinate grid.")
                .define("enabled", true);
        GRID_RADIUS = builder
                .comment("How many blocks the grid extends in each direction from the player.")
                .defineInRange("radius", 8, 1, 32);
        builder.pop();

        builder.comment("Stronger highlight of the block the player is looking at")
                .push("targetHighlight");
        TARGET_HL_ENABLED = builder
                .comment("Master switch for highlighting the looking-at block.")
                .define("enabled", true);
        TARGET_HL_COLOR = builder
                .comment("Outline color of the looking-at block, ARGB hex (default yellow).")
                .define("outlineColorARGB", "FFFFEE00", Config::isHexColor);
        TARGET_HL_FILL = builder
                .comment("Also draw a translucent fill on the looking-at block.")
                .define("drawFill", true);
        TARGET_HL_FILL_ALPHA = builder
                .comment("Alpha (0-255) of the translucent fill.")
                .defineInRange("fillAlpha", 48, 0, 255);
        builder.pop();

        builder.comment("Screen-edge border that indicates the window focus (active) state")
                .push("focusBorder");
        FOCUS_BORDER_ENABLED = builder
                .comment("Master switch for the screen-edge focus border.")
                .define("enabled", true);
        FOCUS_BORDER_WHEN_FOCUSED = builder
                .comment("Show the border while the window is focused (active).")
                .define("showWhenFocused", true);
        FOCUS_BORDER_WHEN_UNFOCUSED = builder
                .comment("Show the border while the window is unfocused (inactive).")
                .define("showWhenUnfocused", true);
        FOCUS_BORDER_THICKNESS = builder
                .comment("Border thickness in GUI pixels.")
                .defineInRange("thickness", 4, 1, 32);
        FOCUS_BORDER_COLOR_FOCUSED = builder
                .comment("Border color when focused, ARGB hex (default green).")
                .define("focusedColorARGB", "CC55FF55", Config::isHexColor);
        FOCUS_BORDER_COLOR_UNFOCUSED = builder
                .comment("Border color when unfocused, ARGB hex (default red).")
                .define("unfocusedColorARGB", "CCFF5555", Config::isHexColor);
        builder.pop();

        SPEC = builder.build();
    }

    /** Accepts 1-8 hex digits (RGB or ARGB). */
    public static boolean isHexColor(Object value) {
        return value instanceof String s && s.matches("(?i)[0-9a-f]{1,8}");
    }

    /** Parse an ARGB hex string to an int color, or transparent black if invalid. */
    public static int parseColor(String hex) {
        try {
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Config() {
    }
}
