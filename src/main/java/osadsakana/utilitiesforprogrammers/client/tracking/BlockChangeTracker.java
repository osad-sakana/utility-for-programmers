package osadsakana.utilitiesforprogrammers.client.tracking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import osadsakana.utilitiesforprogrammers.Config;
import osadsakana.utilitiesforprogrammers.client.FreezeClock;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Stores recent client-observed block changes for the highlight renderer.
 *
 * <p>Changes are recorded by {@code ClientPacketListenerMixin} when the client
 * receives single-block or section-block update packets — this works even when
 * the server has no mod installed. Each position keeps the timestamp of its most
 * recent change (so re-changing a block makes it "newest" again). Entries expire
 * after the configured display duration, measured on the freeze-aware
 * {@link FreezeClock}.
 */
public final class BlockChangeTracker {

    /** A single recorded change: a block position and when it last changed. */
    public record Change(BlockPos pos, long timeMillis) {
    }

    /** Hard safety cap so a flood of updates (e.g. WorldEdit) cannot exhaust memory. */
    private static final int MAX_ENTRIES = 20_000;

    private static final Map<BlockPos, Change> CHANGES = new ConcurrentHashMap<>();

    /** Record a block change at {@code pos} if highlighting is active and it is in range. */
    public static void record(BlockPos pos) {
        if (!ToggleState.isEnabled() || !Config.HIGHLIGHT_ENABLED.get() || ToggleState.isFrozen()) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        final int radius = Config.HIGHLIGHT_RADIUS.get();
        final double dx = pos.getX() + 0.5D - mc.player.getX();
        final double dy = pos.getY() + 0.5D - mc.player.getY();
        final double dz = pos.getZ() + 0.5D - mc.player.getZ();
        if (dx * dx + dy * dy + dz * dz > (double) radius * radius) {
            return;
        }
        if (CHANGES.size() >= MAX_ENTRIES && !CHANGES.containsKey(pos)) {
            return;
        }
        final BlockPos key = pos.immutable();
        CHANGES.put(key, new Change(key, FreezeClock.now()));
    }

    /** Live (non-expired) changes; also evicts expired entries as a side effect. */
    public static List<Change> snapshot() {
        final long ttlMs = (long) (Config.HIGHLIGHT_SECONDS.get() * 1000.0D);
        final long now = FreezeClock.now();
        final List<Change> live = new ArrayList<>(Math.min(CHANGES.size(), 512));
        for (Iterator<Change> it = CHANGES.values().iterator(); it.hasNext(); ) {
            final Change change = it.next();
            if (now - change.timeMillis() > ttlMs) {
                it.remove();
            } else {
                live.add(change);
            }
        }
        return live;
    }

    public static void clear() {
        CHANGES.clear();
    }

    private BlockChangeTracker() {
    }
}
