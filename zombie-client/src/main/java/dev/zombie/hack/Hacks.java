package dev.zombie.hack;

import dev.zombie.util.Util;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/** All modules live here as small nested classes. */
public final class Hacks {
    private Hacks() {}

    // ---------------- COMBAT ----------------

    public static class KillAura extends Hack {
        public KillAura() { super("KillAura", Category.COMBAT, GLFW.GLFW_KEY_R); }

        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.currentScreen != null) return;
            PlayerEntity target = Util.nearestPlayer(mc, 4.2);
            if (target == null || p.getAttackCooldownProgress(0.5f) < 0.95f) return;
            float[] rot = Util.rotationsTo(p.getEyePos(), target.getBoundingBox().getCenter());
            p.setYaw(rot[0]);
            p.setPitch(rot[1]);
            mc.interactionManager.attackEntity(p, target);
            p.swingHand(Hand.MAIN_HAND);
        }
    }

    public static class TriggerBot extends Hack {
        public TriggerBot() { super("TriggerBot", Category.COMBAT, -1); }

        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.currentScreen != null || p.getAttackCooldownProgress(0.5f) < 0.95f) return;
            if (mc.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
                mc.interactionManager.attackEntity(p, target);
                p.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    public static class AimAssist extends Hack {
        public AimAssist() { super("AimAssist", Category.COMBAT, -1); }

        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.currentScreen != null) return;
            PlayerEntity target = Util.nearestPlayer(mc, 6.0);
            if (target == null) return;
            float[] rot = Util.rotationsTo(p.getEyePos(), target.getBoundingBox().getCenter());
            float dYaw = MathHelper.wrapDegrees(rot[0] - p.getYaw());
            float dPitch = rot[1] - p.getPitch();
            if (Math.abs(dYaw) > 70) return; // only assist when roughly facing them
            p.setYaw(p.getYaw() + MathHelper.clamp(dYaw, -4f, 4f));
            p.setPitch(MathHelper.clamp(p.getPitch() + MathHelper.clamp(dPitch, -3f, 3f), -90f, 90f));
        }
    }

    /** Raises the client-side interaction range. The server still enforces its own limit. */
    public static class Reach extends Hack {
        public Reach() { super("Reach", Category.COMBAT, -1); }

        @Override public void onTick() { set(6.0); }
        @Override protected void onDisable() { set(3.0); }

        private void set(double value) {
            if (mc.player == null) return;
            EntityAttributeInstance attr = mc.player.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
            if (attr != null) attr.setBaseValue(value);
        }
    }

    /** Flag only. The work is done in EntityMixin. */
    public static class Hitboxes extends Hack {
        public Hitboxes() { super("Hitboxes", Category.COMBAT, -1); }
    }

    public static class Criticals extends Hack {
        public Criticals() {
            super("Criticals", Category.COMBAT, -1);
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                if (isEnabled() && player == mc.player && player.isOnGround()
                        && !player.isTouchingWater() && entity instanceof LivingEntity) {
                    double x = player.getX(), y = player.getY(), z = player.getZ();
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false, false));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, false));
                }
                return ActionResult.PASS;
            });
        }
    }

    /** Flag only. Knockback packets are dropped in ClientPlayNetworkHandlerMixin. */
    public static class Velocity extends Hack {
        public Velocity() { super("Velocity", Category.COMBAT, -1); }
    }

    public static class AutoTotem extends Hack {
        public AutoTotem() { super("AutoTotem", Category.COMBAT, -1); }

        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.currentScreen != null || p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
            for (int i = 0; i < 36; i++) {
                if (p.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                    int slot = i < 9 ? 36 + i : i; // hotbar slots sit at 36-44 in the player screen handler
                    mc.interactionManager.clickSlot(p.playerScreenHandler.syncId, slot, 40, SlotActionType.SWAP, p);
                    return;
                }
            }
        }
    }

    // ---------------- MOVEMENT ----------------

    public static class Fly extends Hack {
        public Fly() { super("Fly", Category.MOVEMENT, GLFW.GLFW_KEY_G); }

        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            double speed = 0.8;
            Vec3d d = Util.moveInput(mc, speed);
            double vy = (mc.options.jumpKey.isPressed() ? speed : 0) - (mc.options.sneakKey.isPressed() ? speed : 0);
            p.setVelocity(d.x, vy, d.z);
        }
    }

    public static class Speed extends Hack {
        public Speed() { super("Speed", Category.MOVEMENT, GLFW.GLFW_KEY_X); }

        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            Vec3d d = Util.moveInput(mc, 0.45);
            if (d == Vec3d.ZERO) return;
            p.setVelocity(d.x, p.getVelocity().y, d.z);
        }
    }

    public static class NoFall extends Hack {
        public NoFall() { super("NoFall", Category.MOVEMENT, -1); }

        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (p.fallDistance > 2 && !p.isGliding()) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, p.horizontalCollision));
            }
        }
    }

    public static class Sprint extends Hack {
        public Sprint() { super("Sprint", Category.MOVEMENT, -1); }

        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.horizontalCollision
                    && p.getHungerManager().getFoodLevel() > 6) {
                p.setSprinting(true);
            }
        }
    }

    // ---------------- RENDER ----------------

    public static class Fullbright extends Hack {
        public Fullbright() { super("Fullbright", Category.RENDER, GLFW.GLFW_KEY_B); }

        @Override public void onTick() {
            StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur == null || cur.getDuration() < 240) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
            }
        }

        @Override protected void onDisable() {
            if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    /** Flag only. Glowing outline is forced in EntityMixin. */
    public static class ESP extends Hack {
        public ESP() { super("ESP", Category.RENDER, GLFW.GLFW_KEY_H); }
    }
}
