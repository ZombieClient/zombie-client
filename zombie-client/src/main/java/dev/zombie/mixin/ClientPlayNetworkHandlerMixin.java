package dev.zombie.mixin;

import dev.zombie.ZombieClient;
import dev.zombie.hack.Hacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void zombie$noKnockback(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (ZombieClient.hacks() == null) return;
        Hacks.Velocity v = ZombieClient.hacks().get(Hacks.Velocity.class);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (v != null && v.isEnabled() && mc.player != null && packet.getEntityId() == mc.player.getId()) {
            ci.cancel();
        }
    }
}
