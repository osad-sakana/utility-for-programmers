package osadsakana.utilitiesforprogrammers.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;

/**
 * Window-level helpers backed by GLFW.
 *
 * <ul>
 *   <li>{@link #disablePauseOnLostFocus} keeps the game rendering when focus moves
 *       to another window (e.g. a coding environment) instead of pausing.</li>
 *   <li>{@link #toggleAlwaysOnTop} pins the Minecraft window above other desktop
 *       windows via the {@code GLFW_FLOATING} attribute.</li>
 * </ul>
 *
 * All methods must be called from the main (render) thread.
 */
public final class WindowController {

    public static void disablePauseOnLostFocus(Minecraft mc) {
        if (mc.options != null) {
            mc.options.pauseOnLostFocus = false;
        }
    }

    public static boolean toggleAlwaysOnTop(Minecraft mc) {
        final boolean next = !ToggleState.isAlwaysOnTop();
        applyAlwaysOnTop(mc, next);
        return next;
    }

    public static void applyAlwaysOnTop(Minecraft mc, boolean onTop) {
        final long handle = mc.getWindow().handle();
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_FLOATING, onTop ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        ToggleState.setAlwaysOnTop(onTop);
    }

    private WindowController() {
    }
}
