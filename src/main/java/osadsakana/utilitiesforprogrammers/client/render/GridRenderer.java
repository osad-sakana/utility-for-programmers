package osadsakana.utilitiesforprogrammers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Projects a relative-coordinate grid onto the ground around the player.
 *
 * <p>A faint 1-block grid gives a sense of scale; two brighter axes through the
 * player show the relative directions based on facing — the "forward/back" axis
 * (blue) and the "right/left" axis (red) — to connect absolute coordinates with
 * instructions like "3 steps right, 5 steps forward".
 */
public final class GridRenderer {

    private static final float GRID_R = 0.55F, GRID_G = 0.55F, GRID_B = 0.55F, GRID_A = 0.28F;
    private static final float FWD_R = 0.30F, FWD_G = 0.65F, FWD_B = 1.00F, AXIS_A = 0.85F;
    private static final float RIGHT_R = 1.00F, RIGHT_G = 0.30F, RIGHT_B = 0.30F;
    private static final double Y_OFFSET = 0.01D;

    public static void onRenderLevelStage(RenderLevelStageEvent.AfterEntities event) {
        if (!Config.GRID_ENABLED.get() || !ToggleState.isGridVisible()) {
            return;
        }
        final PoseStack pose = event.getPoseStack();
        if (pose == null) {
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

        final Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        final PoseStack.Pose last = pose.last();
        final VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        // Faint 1-block grid.
        for (int x = minX; x <= maxX; x++) {
            line(lines, last, x, y, minZ, x, y, maxZ, GRID_R, GRID_G, GRID_B, GRID_A);
        }
        for (int z = minZ; z <= maxZ; z++) {
            line(lines, last, minX, y, z, maxX, y, z, GRID_R, GRID_G, GRID_B, GRID_A);
        }

        // Relative axes through the player's column.
        final double px = centerX + 0.5D;
        final double pz = centerZ + 0.5D;
        final boolean facingAlongZ = player.getDirection().getAxis() == Direction.Axis.Z;

        // Forward/back axis runs along the facing axis; right/left is perpendicular.
        if (facingAlongZ) {
            line(lines, last, px, y, minZ, px, y, maxZ, FWD_R, FWD_G, FWD_B, AXIS_A);
            line(lines, last, minX, y, pz, maxX, y, pz, RIGHT_R, RIGHT_G, RIGHT_B, AXIS_A);
        } else {
            line(lines, last, minX, y, pz, maxX, y, pz, FWD_R, FWD_G, FWD_B, AXIS_A);
            line(lines, last, px, y, minZ, px, y, maxZ, RIGHT_R, RIGHT_G, RIGHT_B, AXIS_A);
        }

        buffers.endBatch(RenderType.lines());
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
                .setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float) x2, (float) y2, (float) z2)
                .setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
    }

    private GridRenderer() {
    }
}
