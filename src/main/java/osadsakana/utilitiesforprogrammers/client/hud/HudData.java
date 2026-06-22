package osadsakana.utilitiesforprogrammers.client.hud;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Immutable snapshot of the information shown by the HUD: integer (block)
 * coordinates, facing direction, and the targeted block's position and localized
 * display name ({@link Component} from {@code Block#getName()}, e.g. 安山岩 /
 * Andesite). A new snapshot is captured every client tick unless frozen.
 */
public record HudData(
        boolean valid,
        int x,
        int y,
        int z,
        Direction facing,
        @Nullable BlockPos targetPos,
        @Nullable Component blockName) {

    public static final HudData EMPTY =
            new HudData(false, 0, 0, 0, Direction.NORTH, null, null);

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

        final HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            final BlockPos pos = blockHit.getBlockPos();
            final BlockState state = mc.level.getBlockState(pos);
            targetPos = pos;
            blockName = state.getBlock().getName();
        }

        return new HudData(true, x, y, z, facing, targetPos, blockName);
    }
}
