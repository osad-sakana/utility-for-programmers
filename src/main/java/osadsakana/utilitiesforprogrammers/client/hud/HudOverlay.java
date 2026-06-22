package osadsakana.utilitiesforprogrammers.client.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.gui.GuiLayer;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Top-left HUD overlay: absolute coordinates, facing direction and a
 * target-block inspector (block id + block-state properties).
 *
 * <p>Registered as a NeoForge {@link GuiLayer}; it renders the most recent
 * {@link HudData} snapshot, which is held still while the display is frozen.
 */
public final class HudOverlay implements GuiLayer {

    private static final int MARGIN = 4;
    private static final int PADDING = 3;
    private static final int LINE_GAP = 1;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int LABEL_COLOR = 0xFFA0E0FF;
    private static final int FROZEN_COLOR = 0xFF8080;
    private static final int BG_COLOR = 0x90000000;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!Config.HUD_ENABLED.get() || !ToggleState.isHudVisible()) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        // Avoid overlapping the vanilla F3 debug screen.
        if (mc.getDebugOverlay().showDebugScreen()) {
            return;
        }

        final HudData data = HudData.latest();
        if (!data.valid()) {
            return;
        }

        final List<String> lines = buildLines(data);
        drawPanel(graphics, mc.font, lines);
    }

    private static List<String> buildLines(HudData data) {
        final List<String> lines = new ArrayList<>();
        lines.add("XYZ: %.2f / %.2f / %.2f".formatted(data.x(), data.y(), data.z()));
        lines.add("Facing: " + data.facing());

        final BlockPos target = data.targetPos();
        if (target != null) {
            lines.add("Target: %d / %d / %d".formatted(target.getX(), target.getY(), target.getZ()));
            lines.add("Block: " + data.targetId());
            lines.add("Props: " + data.targetProperties());
        } else {
            lines.add("Target: (looking at nothing)");
        }

        if (ToggleState.isFrozen()) {
            lines.add("[FROZEN]");
        }
        return lines;
    }

    private static void drawPanel(GuiGraphics graphics, Font font, List<String> lines) {
        final int lineHeight = font.lineHeight + LINE_GAP;

        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, font.width(line));
        }

        final int panelWidth = maxWidth + PADDING * 2;
        final int panelHeight = lines.size() * lineHeight - LINE_GAP + PADDING * 2;

        graphics.fill(MARGIN, MARGIN, MARGIN + panelWidth, MARGIN + panelHeight, BG_COLOR);

        int y = MARGIN + PADDING;
        final int x = MARGIN + PADDING;
        for (String line : lines) {
            final int color = colorFor(line);
            graphics.drawString(font, line, x, y, color);
            y += lineHeight;
        }
    }

    private static int colorFor(String line) {
        if (line.startsWith("[FROZEN]")) {
            return FROZEN_COLOR;
        }
        if (line.startsWith("Block:") || line.startsWith("Props:")) {
            return LABEL_COLOR;
        }
        return TEXT_COLOR;
    }
}
