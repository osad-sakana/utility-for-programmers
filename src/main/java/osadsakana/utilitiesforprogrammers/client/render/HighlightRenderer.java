package osadsakana.utilitiesforprogrammers.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.FreezeClock;
import osadsakana.utilitiesforprogrammers.client.ToggleState;
import osadsakana.utilitiesforprogrammers.client.tracking.BlockChangeTracker;

/**
 * Renders a wireframe (and optional translucent filled) box around each recently
 * changed block. Boxes are colored by placement order: newest = red, fading
 * through to blue, with opacity fading to zero as a change approaches its expiry.
 *
 * <p>Drawn during {@link RenderLevelStageEvent.AfterEntities}, the only level
 * stage in 1.21.10 that supplies a usable {@link PoseStack}; geometry is built in
 * camera-relative world space, matching the vanilla debug/outline renderers.
 */
public final class HighlightRenderer {

    private static final double OUTLINE_INFLATE = 0.002D;
    private static final float FILL_ALPHA_SCALE = 0.22F;

    public static void onRenderLevelStage(RenderLevelStageEvent.AfterEntities event) {
        if (!ToggleState.isEnabled() || !Config.HIGHLIGHT_ENABLED.get()) {
            return;
        }
        final PoseStack pose = event.getPoseStack();
        if (pose == null) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        final List<BlockChangeTracker.Change> changes = BlockChangeTracker.snapshot();
        if (changes.isEmpty()) {
            return;
        }

        final long now = FreezeClock.now();
        final double ttlMs = Config.HIGHLIGHT_SECONDS.get() * 1000.0D;
        final boolean drawFill = Config.HIGHLIGHT_FILL.get();

        final Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        final PoseStack.Pose last = pose.last();

        // The shared BufferSource only builds one RenderType at a time, so the
        // wireframe and fill passes must be fully separate (build -> endBatch).

        // Pass 1: wireframe outlines.
        final VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        for (BlockChangeTracker.Change change : changes) {
            final float fraction = ageFraction(now - change.timeMillis(), ttlMs);
            final AABB box = boxAround(change.pos());
            ShapeRenderer.renderLineBox(last, lines, box,
                    1.0F - fraction, 0.0F, fraction, Math.max(0.0F, 1.0F - fraction));
        }
        buffers.endBatch(RenderType.lines());

        // Pass 2: optional translucent filled boxes.
        if (drawFill) {
            final VertexConsumer filled = buffers.getBuffer(RenderType.debugFilledBox());
            for (BlockChangeTracker.Change change : changes) {
                final float fraction = ageFraction(now - change.timeMillis(), ttlMs);
                final AABB box = boxAround(change.pos());
                ShapeRenderer.addChainedFilledBoxVertices(pose, filled,
                        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ,
                        1.0F - fraction, 0.0F, fraction,
                        Math.max(0.0F, 1.0F - fraction) * FILL_ALPHA_SCALE);
            }
            buffers.endBatch(RenderType.debugFilledBox());
        }

        pose.popPose();
    }

    private static AABB boxAround(BlockPos pos) {
        return new AABB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D)
                .inflate(OUTLINE_INFLATE);
    }

    private static float ageFraction(long ageMs, double ttlMs) {
        if (ttlMs <= 0.0D) {
            return 1.0F;
        }
        final double raw = ageMs / ttlMs;
        if (raw <= 0.0D) {
            return 0.0F;
        }
        return raw >= 1.0D ? 1.0F : (float) raw;
    }

    private HighlightRenderer() {
    }
}
