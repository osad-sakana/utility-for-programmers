package osadsakana.utilitiesforprogrammers.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.GuiLayer;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Draws a colored border around the screen edges that indicates whether the
 * Minecraft window currently has focus: green while focused (active), red while
 * unfocused (inactive). Useful when working in another window with the game kept
 * rendering / always-on-top, to see at a glance where keyboard input will go.
 *
 * <p>Colors, thickness and which states show a border are configurable.
 * Note: like other HUD layers, it is not drawn while a full screen/menu is open.
 */
public final class FocusBorderOverlay implements GuiLayer {

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!ToggleState.isEnabled() || !Config.FOCUS_BORDER_ENABLED.get()) {
            return;
        }

        final boolean active = Minecraft.getInstance().isWindowActive();
        if (active && !Config.FOCUS_BORDER_WHEN_FOCUSED.get()) {
            return;
        }
        if (!active && !Config.FOCUS_BORDER_WHEN_UNFOCUSED.get()) {
            return;
        }

        final int color = Config.parseColor(active
                ? Config.FOCUS_BORDER_COLOR_FOCUSED.get()
                : Config.FOCUS_BORDER_COLOR_UNFOCUSED.get());
        final int thickness = Config.FOCUS_BORDER_THICKNESS.get();
        final int width = graphics.guiWidth();
        final int height = graphics.guiHeight();

        // Top, bottom, then the left/right segments between them.
        graphics.fill(0, 0, width, thickness, color);
        graphics.fill(0, height - thickness, width, height, color);
        graphics.fill(0, thickness, thickness, height - thickness, color);
        graphics.fill(width - thickness, thickness, width, height - thickness, color);
    }
}
