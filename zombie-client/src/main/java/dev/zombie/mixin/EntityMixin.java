package dev.zombie.mixin;

import dev.zombie.ZombieClient;
import dev.zombie.hack.Hacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void zombie$hitboxes(CallbackInfoReturnable<Float> cir) {
        if (ZombieClient.hacks() == null) return;
        Hacks.Hitboxes h = ZombieClient.hacks().get(Hacks.Hitboxes.class);
        Entity self = (Entity) (Object) this;
        if (h != null && h.isEnabled() && self instanceof PlayerEntity
                && self != MinecraftClient.getInstance().player) {
            cir.setReturnValue(0.4f);
        }
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void zombie$esp(CallbackInfoReturnable<Boolean> cir) {
        if (ZombieClient.hacks() == null) return;
        Hacks.ESP esp = ZombieClient.hacks().get(Hacks.ESP.class);
        Entity self = (Entity) (Object) this;
        if (esp != null && esp.isEnabled() && self instanceof PlayerEntity
                && self != MinecraftClient.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
