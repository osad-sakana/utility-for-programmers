package osadsakana.utilitiesforprogrammers.client.hud;

import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Immutable snapshot of the information shown by the HUD.
 *
 * <p>A new snapshot is captured every client tick (see
 * {@code ClientEvents#onClientTickPost}). While the display is frozen the
 * snapshot is simply not refreshed, so the HUD holds its last values for copying.
 */
public record HudData(
        boolean valid,
        double x,
        double y,
        double z,
        String facing,
        @Nullable BlockPos targetPos,
        @Nullable String targetId,
        @Nullable String targetProperties) {

    public static final HudData EMPTY =
            new HudData(false, 0, 0, 0, "-", null, null, null);

    private static volatile HudData latest = EMPTY;

    public static HudData latest() {
        return latest;
    }

    public static void set(HudData data) {
        latest = data;
    }

    /** Build a snapshot from the current player/world state. */
    public static HudData capture(Minecraft mc) {
        final Player player = mc.player;
        if (player == null || mc.level == null) {
            return EMPTY;
        }

        final String facing = cardinal(player.getDirection());

        BlockPos targetPos = null;
        String targetId = null;
        String targetProperties = null;

        final HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            final BlockPos pos = blockHit.getBlockPos();
            final BlockState state = mc.level.getBlockState(pos);
            targetPos = pos;
            final ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            targetId = id.toString();
            targetProperties = formatProperties(state);
        }

        return new HudData(true, player.getX(), player.getY(), player.getZ(),
                facing, targetPos, targetId, targetProperties);
    }

    private static String cardinal(Direction direction) {
        return switch (direction) {
            case NORTH -> "North (-Z)";
            case SOUTH -> "South (+Z)";
            case EAST -> "East (+X)";
            case WEST -> "West (-X)";
            default -> direction.getName();
        };
    }

    private static String formatProperties(BlockState state) {
        final Map<Property<?>, Comparable<?>> values = state.getValues();
        if (values.isEmpty()) {
            return "(none)";
        }
        return values.entrySet().stream()
                .map(entry -> entry.getKey().getName() + "=" + propertyValue(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String propertyValue(Property property, Comparable value) {
        return property.getName(value);
    }
}
