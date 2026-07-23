package osadsakana.utilitiesforprogrammers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Projects a relative-coordinate grid onto the ground around the player.
 *
 * <p>A faint 1-block grid gives a sense of scale; two arrows anchored at the
 * world origin (0, 0, 0) show the absolute axes — +X (red) and +Z (blue) — so
 * the directions stay fixed and match the coordinates shown on the HUD.
 */
public final class GridRenderer {

    private static final float GRID_R = 0.55F, GRID_G = 0.55F, GRID_B = 0.55F, GRID_A = 0.28F;
    private static final float X_R = 1.00F, X_G = 0.30F, X_B = 0.30F; // +X axis: red
    private static final float Z_R = 0.30F, Z_G = 0.65F, Z_B = 1.00F; // +Z axis: blue
    private static final float AXIS_A = 0.85F;
    private static final double ARROW_HEAD = 0.6D; // arrowhead barb length, in blocks
    private static final double Y_OFFSET = 0.01D;
    private static final float LINE_WIDTH = 1.0F;

    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        if (!ToggleState.isEnabled() || !Config.GRID_ENABLED.get()) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        final LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        final int radius = Config.GRID_RADIUS.get();
        final int centerX = Mth.floor(player.getX());
        final int centerZ = Mth.floor(player.getZ());
        final double y = Mth.floor(player.getY()) + Y_OFFSET;

        final int minX = centerX - radius;
        final int maxX = centerX + radius + 1;
        final int minZ = centerZ - radius;
        final int maxZ = centerZ + radius + 1;
        final double axisLen = radius + 1;

        final Vec3 cam = mc.gameRenderer.mainCamera().position();
        final PoseStack pose = event.getPoseStack();

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        event.getSubmitNodeCollector().submitCustomGeometry(pose, RenderTypes.lines(), (last, lines) -> {
            // Faint 1-block grid.
            for (int x = minX; x <= maxX; x++) {
                line(lines, last, x, y, minZ, x, y, maxZ, GRID_R, GRID_G, GRID_B, GRID_A);
            }
            for (int z = minZ; z <= maxZ; z++) {
                line(lines, last, minX, y, z, maxX, y, z, GRID_R, GRID_G, GRID_B, GRID_A);
            }

            // Absolute coordinate axes anchored at the world origin (0, 0, 0), drawn
            // as arrows so +X (red) and +Z (blue) stay fixed regardless of facing.
            // +X arrow (red).
            line(lines, last, 0.0D, y, 0.0D, axisLen, y, 0.0D, X_R, X_G, X_B, AXIS_A);
            line(lines, last, axisLen, y, 0.0D, axisLen - ARROW_HEAD, y, ARROW_HEAD, X_R, X_G, X_B, AXIS_A);
            line(lines, last, axisLen, y, 0.0D, axisLen - ARROW_HEAD, y, -ARROW_HEAD, X_R, X_G, X_B, AXIS_A);
            // +Z arrow (blue).
            line(lines, last, 0.0D, y, 0.0D, 0.0D, y, axisLen, Z_R, Z_G, Z_B, AXIS_A);
            line(lines, last, 0.0D, y, axisLen, ARROW_HEAD, y, axisLen - ARROW_HEAD, Z_R, Z_G, Z_B, AXIS_A);
            line(lines, last, 0.0D, y, axisLen, -ARROW_HEAD, y, axisLen - ARROW_HEAD, Z_R, Z_G, Z_B, AXIS_A);
        });
        pose.popPose();
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             float r, float g, float b, float a) {
        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        final float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1.0E-5F) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        consumer.addVertex(pose, (float) x1, (float) y1, (float) z1)
                .setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(LINE_WIDTH);
        consumer.addVertex(pose, (float) x2, (float) y2, (float) z2)
                .setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(LINE_WIDTH);
    }

    private GridRenderer() {
    }
}
