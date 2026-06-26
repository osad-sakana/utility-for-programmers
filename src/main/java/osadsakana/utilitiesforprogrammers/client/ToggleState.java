package osadsakana.utilitiesforprogrammers.client;

/**
 * Runtime on/off state toggled by key bindings, independent of the persisted
 * {@link osadsakana.utilitiesforprogrammers.Config} defaults.
 *
 * <p>{@code enabled} is the master switch for every visual feature (HUD,
 * block-update highlight, grid, target highlight, focus border); per-feature
 * config flags still gate them individually on top of it. {@code frozen} is the
 * external-operation mode. Fields are {@code volatile} because they are written
 * from the key-handling tick and read from the render thread.
 */
public final class ToggleState {

    /** Master switch for all of the mod's visual features. */
    private static volatile boolean enabled = true;

    /** When frozen, HUD values and highlight ages stop advancing so they can be copied. */
    private static volatile boolean frozen = false;

    /** Whether the game window is currently pinned always-on-top. */
    private static volatile boolean alwaysOnTop = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean toggleEnabled() {
        enabled = !enabled;
        return enabled;
    }

    public static boolean isFrozen() {
        return frozen;
    }

    public static boolean toggleFrozen() {
        frozen = !frozen;
        return frozen;
    }

    public static boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    public static void setAlwaysOnTop(boolean value) {
        alwaysOnTop = value;
    }

    private ToggleState() {
    }
}
