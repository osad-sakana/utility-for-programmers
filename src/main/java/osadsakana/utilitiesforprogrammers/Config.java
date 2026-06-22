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

        SPEC = builder.build();
    }

    private Config() {
    }
}
