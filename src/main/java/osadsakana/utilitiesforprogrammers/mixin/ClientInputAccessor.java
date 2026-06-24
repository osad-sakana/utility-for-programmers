package osadsakana.utilitiesforprogrammers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

/**
 * Accessor for {@link ClientInput}'s movement fields, which are declared on this
 * base class (not on {@code KeyboardInput}). Mixin's {@code @Shadow} cannot reach
 * inherited fields, so generated setters are used instead to zero movement while
 * the external-operation (freeze) mode is active.
 */
@Mixin(ClientInput.class)
public interface ClientInputAccessor {

    @Accessor("keyPresses")
    void ufp$setKeyPresses(Input keyPresses);

    @Accessor("moveVector")
    void ufp$setMoveVector(Vec2 moveVector);
}
