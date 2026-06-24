package osadsakana.utilitiesforprogrammers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import osadsakana.utilitiesforprogrammers.client.ToggleState;

/**
 * Freezes player movement while the external-operation (freeze) mode is active.
 *
 * <p>{@code KeyboardInput#tick} recomputes both {@code keyPresses} (sent to the
 * server) and {@code moveVector} (used for local travel) from the held keys. By
 * zeroing both at the tail of tick, the character does not walk, jump, sneak or
 * sprint even while the window is focused, and no desync occurs because the empty
 * {@code keyPresses} is what gets sent. Camera rotation is already suppressed
 * because the freeze mode releases the mouse.
 *
 * <p>The fields live on the {@code ClientInput} superclass, which {@code @Shadow}
 * cannot reach, so {@link ClientInputAccessor}'s generated setters are used.
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void ufp$freezeMovement(CallbackInfo ci) {
        if (ToggleState.isFrozen()) {
            final ClientInputAccessor input = (ClientInputAccessor) this;
            input.ufp$setKeyPresses(Input.EMPTY);
            input.ufp$setMoveVector(Vec2.ZERO);
        }
    }
}
