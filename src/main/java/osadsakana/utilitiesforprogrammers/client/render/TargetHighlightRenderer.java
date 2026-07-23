package osadsakana.utilitiesforprogrammers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Draws a stronger highlight on the block the player is currently looking at
 * (the same block shown in the HUD inspector): a bright outline following the
 * block's actual shape plus an optional translucent fill, making it stand out
 * more than the faint vanilla selection outline.
 */
public final class TargetHighlightRenderer {

    private static final float OUTLINE_WIDTH = 1.0F;

    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        if (!ToggleState.isEnabled() || !Config.TARGET_HL_ENABLED.get()) {
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
        final Vec3 cam = mc.gameRenderer.mainCamera().position();
        final PoseStack pose = event.getPoseStack();
        final var submitNodeCollector = event.getSubmitNodeCollector();

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        // Pass 1: outline following the block shape.
        submitNodeCollector.submitShapeOutline(
                pose, shape, RenderTypes.lines(), outlineColor, OUTLINE_WIDTH, false);

        // Pass 2: optional translucent fill over the shape's bounds.
        if (Config.TARGET_HL_FILL.get()) {
            final int alpha = Config.TARGET_HL_FILL_ALPHA.get();
            final float r = ((outlineColor >> 16) & 0xFF) / 255.0F;
            final float g = ((outlineColor >> 8) & 0xFF) / 255.0F;
            final float b = (outlineColor & 0xFF) / 255.0F;
            final float a = alpha / 255.0F;

            final AABB bounds = shape.bounds();
            final AABB worldBox = new AABB(
                    pos.getX() + bounds.minX, pos.getY() + bounds.minY, pos.getZ() + bounds.minZ,
                    pos.getX() + bounds.maxX, pos.getY() + bounds.maxY, pos.getZ() + bounds.maxZ);
            submitNodeCollector.submitCustomGeometry(pose, RenderTypes.debugFilledBox(),
                    (fillPose, buffer) -> BoxFill.submit(buffer, fillPose, worldBox, r, g, b, a));
        }

        pose.popPose();
    }

    private TargetHighlightRenderer() {
    }
}
