package osadsakana.utilitiesforprogrammers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import osadsakana.utilitiesforprogrammers.client.tracking.BlockChangeTracker;

/**
 * Detects block changes the client receives from the server, so highlighting
 * works without any server-side mod.
 *
 * <p>Both handlers re-dispatch onto the main thread via {@code ensureRunningOnSameThread}
 * (throwing on the network thread), so the {@code TAIL} injection only runs on the
 * main thread after the change has been applied to the client level.
 *
 * <p>Only single-block and section (multi-block) updates are hooked — not full
 * chunk loads — so loading terrain does not get highlighted, only genuine changes.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    private void ufp$onBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        BlockChangeTracker.record(packet.getPos());
    }

    @Inject(method = "handleChunkBlocksUpdate", at = @At("TAIL"))
    private void ufp$onSectionBlocksUpdate(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
        packet.runUpdates((pos, state) -> BlockChangeTracker.record(pos));
    }
}
