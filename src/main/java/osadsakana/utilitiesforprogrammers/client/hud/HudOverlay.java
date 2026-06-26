package osadsakana.utilitiesforprogrammers.client.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.GuiLayer;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Top-left HUD overlay: absolute (integer) coordinates, facing direction and a
 * target-block inspector (localized block name + registry id + properties).
 *
 * <p>All text is localized through translation keys (see {@code lang/*.json}) and
 * the block's own {@code getName()}, so the HUD follows the game language.
 */
public final class HudOverlay implements GuiLayer {

    // Translation keys.
    private static final String K_COORDS = "hud.utilitiesforprogrammers.coords";
    private static final String K_FACING = "hud.utilitiesforprogrammers.facing";
    private static final String K_BLOCK = "hud.utilitiesforprogrammers.block";
    private static final String K_NO_BLOCK = "hud.utilitiesforprogrammers.no_block";
    private static final String K_FROZEN = "hud.utilitiesforprogrammers.frozen";

    private static final int MARGIN = 4;
    private static final int PADDING = 3;
    private static final int LINE_GAP = 1;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int NAME_COLOR = 0xFFFFE070;
    private static final int DIM_COLOR = 0xFFB0B0B0;
    private static final int FROZEN_COLOR = 0xFFFF8080;
    private static final int BG_COLOR = 0x90000000;

    private record HudLine(String text, int color) {
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!ToggleState.isEnabled() || !Config.HUD_ENABLED.get()) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.getDebugOverlay().showDebugScreen()) {
            return;
        }

        final HudData data = HudData.latest();
        if (!data.valid()) {
            return;
        }

        drawPanel(graphics, mc.font, buildLines(data));
    }

    private static List<HudLine> buildLines(HudData data) {
        final List<HudLine> lines = new ArrayList<>();

        lines.add(new HudLine(tr(K_COORDS, data.x(), data.y(), data.z()), TEXT_COLOR));
        lines.add(new HudLine(tr(K_FACING, directionComponent(data.facing())), TEXT_COLOR));

        final BlockPos target = data.targetPos();
        if (target != null) {
            lines.add(new HudLine(
                    tr(K_BLOCK, data.blockName(), target.getX(), target.getY(), target.getZ()),
                    NAME_COLOR));
        } else {
            lines.add(new HudLine(tr(K_NO_BLOCK), DIM_COLOR));
        }

        if (ToggleState.isFrozen()) {
            lines.add(new HudLine(tr(K_FROZEN), FROZEN_COLOR));
        }
        return lines;
    }

    private static Component directionComponent(Direction direction) {
        return Component.translatable("hud.utilitiesforprogrammers.dir." + direction.getName());
    }

    /** Resolve a translation key (with optional args) to a string in the current language. */
    private static String tr(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    private static void drawPanel(GuiGraphics graphics, Font font, List<HudLine> lines) {
        final int lineHeight = font.lineHeight + LINE_GAP;

        int maxWidth = 0;
        for (HudLine line : lines) {
            maxWidth = Math.max(maxWidth, font.width(line.text()));
        }

        final int panelWidth = maxWidth + PADDING * 2;
        final int panelHeight = lines.size() * lineHeight - LINE_GAP + PADDING * 2;

        graphics.fill(MARGIN, MARGIN, MARGIN + panelWidth, MARGIN + panelHeight, BG_COLOR);

        int y = MARGIN + PADDING;
        final int x = MARGIN + PADDING;
        for (HudLine line : lines) {
            graphics.drawString(font, line.text(), x, y, line.color());
            y += lineHeight;
        }
    }
}
