package osadsakana.utilitiesforprogrammers.client;

import osadsakana.utilitiesforprogrammers.Config;

/**
 * Runtime on/off state that the user can flip with key bindings at runtime,
 * independent of the persisted {@link Config} defaults.
 *
 * <p>The config values act as the initial/default state; key bindings toggle the
 * live flags held here. Fields are {@code volatile} because they are written from
 * the key-handling tick and read from the render thread.
 */
public final class ToggleState {

    private static volatile boolean hudVisible = true;
    private static volatile boolean highlightVisible = true;
    private static volatile boolean gridVisible = true;

    /** When frozen, HUD values and highlight ages stop advancing so they can be copied. */
    private static volatile boolean frozen = false;

    /** Whether the game window is currently pinned always-on-top. */
    private static volatile boolean alwaysOnTop = false;

    /** Re-seed the live toggles from the persisted config (called when a level loads). */
    public static void initFromConfig() {
        hudVisible = Config.HUD_ENABLED.get();
        highlightVisible = Config.HIGHLIGHT_ENABLED.get();
        gridVisible = Config.GRID_ENABLED.get();
    }

    public static boolean isHudVisible() {
        return hudVisible;
    }

    public static boolean isHighlightVisible() {
        return highlightVisible;
    }

    public static boolean isGridVisible() {
        return gridVisible;
    }

    public static boolean isFrozen() {
        return frozen;
    }

    public static boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    public static boolean toggleHud() {
        hudVisible = !hudVisible;
        return hudVisible;
    }

    public static boolean toggleHighlight() {
        highlightVisible = !highlightVisible;
        return highlightVisible;
    }

    public static boolean toggleGrid() {
        gridVisible = !gridVisible;
        return gridVisible;
    }

    public static boolean toggleFrozen() {
        frozen = !frozen;
        return frozen;
    }

    public static void setAlwaysOnTop(boolean value) {
        alwaysOnTop = value;
    }

    private ToggleState() {
    }
}
