package osadsakana.utilitiesforprogrammers.client.hud;

import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Immutable snapshot of the information shown by the HUD.
 *
 * <p>Coordinates are stored as integers (block coordinates). The targeted block
 * keeps both its localized display name ({@link Component} from
 * {@code Block#getName()}, e.g. 安山岩 / Andesite) and its registry id
 * (e.g. {@code minecraft:andesite}). A new snapshot is captured every client tick
 * unless frozen.
 */
public record HudData(
        boolean valid,
        int x,
        int y,
        int z,
        Direction facing,
        @Nullable BlockPos targetPos,
        @Nullable Component blockName,
        @Nullable String blockId,
        @Nullable String blockProperties) {

    public static final HudData EMPTY =
            new HudData(false, 0, 0, 0, Direction.NORTH, null, null, null, null);

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

        final int x = Mth.floor(player.getX());
        final int y = Mth.floor(player.getY());
        final int z = Mth.floor(player.getZ());
        final Direction facing = player.getDirection();

        BlockPos targetPos = null;
        Component blockName = null;
        String blockId = null;
        String blockProperties = null;

        final HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            final BlockPos pos = blockHit.getBlockPos();
            final BlockState state = mc.level.getBlockState(pos);
            targetPos = pos;
            blockName = state.getBlock().getName();
            final ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            blockId = id.toString();
            blockProperties = formatProperties(state);
        }

        return new HudData(true, x, y, z, facing, targetPos, blockName, blockId, blockProperties);
    }

    /** Properties as {@code key=value, ...}, or an empty string when there are none. */
    private static String formatProperties(BlockState state) {
        final Map<Property<?>, Comparable<?>> values = state.getValues();
        if (values.isEmpty()) {
            return "";
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
