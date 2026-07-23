package osadsakana.utilitiesforprogrammers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.phys.AABB;

/**
 * Emits the 6 quads (24 vertices) of a filled, camera-relative box, matching the
 * vertex order vanilla's own cuboid gizmo uses for {@code RenderTypes.debugFilledBox()}.
 */
final class BoxFill {

    static void submit(VertexConsumer buffer, PoseStack.Pose pose, AABB box,
                        float r, float g, float b, float a) {
        final double x0 = box.minX, y0 = box.minY, z0 = box.minZ;
        final double x1 = box.maxX, y1 = box.maxY, z1 = box.maxZ;
        quad(buffer, pose, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, r, g, b, a);
        quad(buffer, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a);
        quad(buffer, pose, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, r, g, b, a);
        quad(buffer, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a);
        quad(buffer, pose, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, b, a);
        quad(buffer, pose, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                              double x1, double y1, double z1, double x2, double y2, double z2,
                              double x3, double y3, double z3, double x4, double y4, double z4,
                              float r, float g, float b, float a) {
        buffer.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a);
        buffer.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a);
        buffer.addVertex(pose, (float) x3, (float) y3, (float) z3).setColor(r, g, b, a);
        buffer.addVertex(pose, (float) x4, (float) y4, (float) z4).setColor(r, g, b, a);
    }

    private BoxFill() {
    }
}
