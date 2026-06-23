package osadsakana.utilitiesforprogrammers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import osadsakana.utilitiesforprogrammers.Config;

/**
 * Draws a stronger highlight on the block the player is currently looking at
 * (the same block shown in the HUD inspector): a bright outline following the
 * block's actual shape plus an optional translucent fill, making it stand out
 * more than the faint vanilla selection outline.
 */
public final class TargetHighlightRenderer {

    public static void onRenderLevelStage(RenderLevelStageEvent.AfterEntities event) {
        if (!Config.TARGET_HL_ENABLED.get()) {
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
        final HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult blockHit)) {
            return;
        }

        final BlockPos pos = blockHit.getBlockPos();
        final BlockState state = mc.level.getBlockState(pos);
        VoxelShape shape = state.getShape(mc.level, pos);
        if (shape.isEmpty()) {
            shape = Shapes.block();
        }

        final int outlineColor = Config.parseColor(Config.TARGET_HL_COLOR.get());
        final Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        // Pass 1: outline following the block shape.
        final VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        ShapeRenderer.renderShape(pose, lines, shape, pos.getX(), pos.getY(), pos.getZ(), outlineColor);
        buffers.endBatch(RenderType.lines());

        // Pass 2: optional translucent fill over the shape's bounds.
        if (Config.TARGET_HL_FILL.get()) {
            final int alpha = Config.TARGET_HL_FILL_ALPHA.get();
            final float r = ((outlineColor >> 16) & 0xFF) / 255.0F;
            final float g = ((outlineColor >> 8) & 0xFF) / 255.0F;
            final float b = (outlineColor & 0xFF) / 255.0F;
            final float a = alpha / 255.0F;

            final AABB bounds = shape.bounds();
            final VertexConsumer filled = buffers.getBuffer(RenderType.debugFilledBox());
            ShapeRenderer.addChainedFilledBoxVertices(pose, filled,
                    pos.getX() + bounds.minX, pos.getY() + bounds.minY, pos.getZ() + bounds.minZ,
                    pos.getX() + bounds.maxX, pos.getY() + bounds.maxY, pos.getZ() + bounds.maxZ,
                    r, g, b, a);
            buffers.endBatch(RenderType.debugFilledBox());
        }

        pose.popPose();
    }

    private TargetHighlightRenderer() {
    }
}
