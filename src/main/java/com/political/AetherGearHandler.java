package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Drives the Aether Arsenal in play: worn auras, the full set answer, weapon signatures on a
 * strike and tool signatures on a break.
 *
 * <p>Cooldowns are per player and per source, so holding down an attack cannot spam a signature,
 * and the whole thing is visual plus a light touch of effect rather than a damage rewrite.</p>
 */
public final class AetherGearHandler {

    private AetherGearHandler() {}

    private static final long WEAPON_CD_MS = 3_000L;
    private static final long TOOL_CD_MS = 6_000L;
    private static final long SET_CD_MS = 45_000L;

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private static boolean ready(String key, long ms) {
        long now = System.currentTimeMillis();
        Long last = COOLDOWNS.get(key);
        if (last != null && now - last < ms) return false;
        COOLDOWNS.put(key, now);
        return true;
    }

    // ── Worn armour ─────────────────────────────────────────────────────────

    /** The attunement worn on all four armour slots, or null if the set is mixed. */
    public static AetherAttunement fullSetOf(ServerPlayer player) {
        AetherAttunement found = null;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            AetherAttunement a = AetherGear.attunementOf(player.getItemBySlot(slot));
            if (a == null) return null;
            if (found == null) found = a;
            else if (found != a) return null;
        }
        return found;
    }

    /** Ticked per player. Draws the worn aura and keeps the set bonus honest. */
    public static void tickPlayer(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // The aura is drawn from the chestplate, so one piece is enough to show your colours.
        AetherAttunement chest = AetherGear.attunementOf(player.getItemBySlot(EquipmentSlot.CHEST));
        if (chest != null && player.tickCount % 4 == 0) {
            chest.aura(level, player.position(), player.tickCount);
        }

        AetherAttunement set = fullSetOf(player);
        if (set == null) return;

        // A quiet, small benefit for committing to one attunement.
        if (player.tickCount % 40 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false, false));
        }

        // The set answers when you are badly hurt, once in a while.
        if (player.getHealth() <= player.getMaxHealth() * 0.35f
                && ready(player.getUUID() + ":aetherset", SET_CD_MS)) {
            set.signature(level, player.position());
            player.heal(6.0f);
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1, true, false, false));
            player.sendSystemMessage(Component.literal("✦ Your " + set.display + " set answers.")
                    .withStyle(set.colour, ChatFormatting.BOLD), true);
        }
    }

    // ── Weapons ─────────────────────────────────────────────────────────────

    /** Called when a player lands a hit. Fires the held weapon's signature. */
    public static void onHit(ServerPlayer player, LivingEntity target) {
        ItemStack weapon = player.getMainHandItem();
        AetherAttunement a = AetherGear.attunementOf(weapon);
        if (a == null) return;
        AetherGear.Piece p = AetherGear.pieceOf(weapon);
        if (p == null || p.slot != AetherGear.Piece.Slot.WEAPON) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ready(player.getUUID() + ":aetherhit", WEAPON_CD_MS)) return;

        // The lance fires at reach, the others at the target itself.
        Vec3 at = p == AetherGear.Piece.GREATAXE
                ? target.position()
                : target.position().add(0, target.getBbHeight() * 0.4, 0);
        a.signature(level, at);

        // A small, attunement flavoured touch so the signature is not purely decorative.
        switch (a) {
            case EMBER, SOLARIS -> target.setRemainingFireTicks(60);
            case GLACIUS -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, true, false, true));
            case UMBRA, VOIDREND -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false, true));
            case TEMPEST -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, true, false, true));
            case VERDANT -> player.heal(1.5f);
            case ABYSSAL -> target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 0, true, false, true));
            case ASTRAL -> target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0, true, false, true));
            case SANGUINE -> {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, true, false, true));
                player.heal(1.0f);
            }
        }
    }

    // ── Tools ───────────────────────────────────────────────────────────────

    /** Called when a player breaks a block. Fires the held tool's signature. */
    public static void onBreak(ServerPlayer player, Vec3 at) {
        ItemStack tool = player.getMainHandItem();
        AetherAttunement a = AetherGear.attunementOf(tool);
        if (a == null) return;
        AetherGear.Piece p = AetherGear.pieceOf(tool);
        if (p == null || p.slot != AetherGear.Piece.Slot.TOOL) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!ready(player.getUUID() + ":aetherbreak", TOOL_CD_MS)) return;

        a.signature(level, at);

        switch (p) {
            case PICKAXE -> player.addEffect(new MobEffectInstance(MobEffects.HASTE, 120, 1, true, false, false));
            case MATTOCK -> player.addEffect(new MobEffectInstance(MobEffects.HASTE, 120, 0, true, false, false));
            case REAPER -> player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0, true, false, false));
            default -> { }
        }
    }

    public static void clear(java.util.UUID uuid) {
        COOLDOWNS.keySet().removeIf(k -> k.startsWith(uuid.toString()));
    }
}
