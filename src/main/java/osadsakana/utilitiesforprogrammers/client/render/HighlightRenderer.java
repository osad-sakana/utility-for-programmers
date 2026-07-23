package osadsakana.utilitiesforprogrammers.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.FreezeClock;
import osadsakana.utilitiesforprogrammers.client.ToggleState;
import osadsakana.utilitiesforprogrammers.client.tracking.BlockChangeTracker;

/**
 * Renders a wireframe (and optional translucent filled) box around each recently
 * changed block. Boxes are colored by placement order: newest = red, fading
 * through to blue, with opacity fading to zero as a change approaches its expiry.
 *
 * <p>Geometry is submitted during {@link SubmitCustomGeometryEvent}, built in
 * camera-relative world space, matching the vanilla debug/outline renderers.
 */
public final class HighlightRenderer {

    private static final double OUTLINE_INFLATE = 0.002D;
    private static final float FILL_ALPHA_SCALE = 0.22F;
    private static final float OUTLINE_WIDTH = 1.0F;

    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        if (!ToggleState.isEnabled() || !Config.HIGHLIGHT_ENABLED.get()) {
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

        final Vec3 cam = mc.gameRenderer.mainCamera().position();
        final PoseStack pose = event.getPoseStack();
        final var submitNodeCollector = event.getSubmitNodeCollector();

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        for (BlockChangeTracker.Change change : changes) {
            final float fraction = ageFraction(now - change.timeMillis(), ttlMs);
            final float alpha = Math.max(0.0F, 1.0F - fraction);
            final AABB box = boxAround(change.pos());
            final float r = 1.0F - fraction, g = 0.0F, b = fraction;

            final int outlineColor = ARGB.colorFromFloat(alpha, r, g, b);
            submitNodeCollector.submitShapeOutline(
                    pose, Shapes.create(box), RenderTypes.lines(), outlineColor, OUTLINE_WIDTH, false);

            if (drawFill) {
                final float fillAlpha = alpha * FILL_ALPHA_SCALE;
                submitNodeCollector.submitCustomGeometry(pose, RenderTypes.debugFilledBox(),
                        (fillPose, buffer) -> BoxFill.submit(buffer, fillPose, box, r, g, b, fillAlpha));
            }
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
