package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * The Quartermaster's Warrant, the set the requisition board pays out in.
 *
 * <p>Every other set in the mod can be crafted. This one cannot be crafted, bought, rolled out of a
 * crate or traded, and the only way to hold a piece is to finish a Great Work or an Impossibility.
 * Four pieces exist and each one is a separate several hour job, which is the point: it should be
 * obvious what somebody wearing the full set did to get it.</p>
 *
 * <h2>One ability, in four parts</h2>
 *
 * <p>Each piece grants a quarter of <b>Levy</b> rather than its own unrelated perk. Alone the helmet
 * is a convenience, the chest is a small mitigation, the legs are a little speed and the boots are
 * safe footing. Worn together they stop being four small things and become the capstone: charge
 * built from everything you hand in can be spent to requisition a commission outright, filling
 * whatever is still owed on it.</p>
 */
public final class WarrantArmour {

    private WarrantArmour() {}

    public static final String ID_PREFIX = "warrant";

    /** The four pieces. */
    public enum Piece {
        HELM("Warrant Helm", "helm", Items.NETHERITE_HELMET, EquipmentSlot.HEAD,
                45, 40, "The Ledger's Eye",
                "Ore and valuables inside stone light up as you pass."),
        PLATE("Warrant Plate", "plate", Items.NETHERITE_CHESTPLATE, EquipmentSlot.CHEST,
                85, 90, "The Ledger's Weight",
                "A share of what hits you is banked as charge instead."),
        GREAVES("Warrant Greaves", "greaves", Items.NETHERITE_LEGGINGS, EquipmentSlot.LEGS,
                65, 70, "The Ledger's Pace",
                "You move quicker the more charge you are carrying."),
        TREAD("Warrant Tread", "tread", Items.NETHERITE_BOOTS, EquipmentSlot.FEET,
                50, 55, "The Ledger's Footing",
                "Falling stops mattering and rough ground stops slowing you.");

        public final String display;
        public final String key;
        public final Item base;
        public final EquipmentSlot slot;
        public final int defense;
        public final int health;
        public final String partName;
        public final String partEffect;

        Piece(String display, String key, Item base, EquipmentSlot slot,
              int defense, int health, String partName, String partEffect) {
            this.display = display;
            this.key = key;
            this.base = base;
            this.slot = slot;
            this.defense = defense;
            this.health = health;
            this.partName = partName;
            this.partEffect = partEffect;
        }

        public String itemId() {
            return ID_PREFIX + "_" + key;
        }
    }

    /** Charge held per player, spent by the full set. */
    private static final Map<String, Integer> CHARGE = new HashMap<>();
    public static final int MAX_CHARGE = 1000;
    /** What a full set spends to requisition a commission outright. */
    public static final int REQUISITION_COST = 750;

    // ── Items ─────────────────────────────────────────────────────────────

    public static ItemStack create(Piece p) {
        ItemStack s = new ItemStack(p.base);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(p.display).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Quartermaster's Warrant"));
        lore.add(Component.literal("§8Paid out by the board. Not craftable."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7❈ Defense: §a+" + p.defense));
        lore.add(Component.literal("§7❤ Health: §c+" + p.health));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§6" + p.partName));
        lore.add(Component.literal("§7" + p.partEffect));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Levy, one quarter of four"));
        lore.add(Component.literal("§8Wear all four to spend charge on a"));
        lore.add(Component.literal("§8commission and fill what is owed."));

        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, p.itemId());
        return s;
    }

    public static Piece pieceOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        for (Piece p : Piece.values()) {
            if (p.itemId().equals(id)) return p;
        }
        return null;
    }

    /** Which pieces a player is actually wearing. */
    public static List<Piece> wornBy(ServerPlayer player) {
        List<Piece> out = new ArrayList<>();
        for (Piece p : Piece.values()) {
            if (pieceOf(player.getItemBySlot(p.slot)) == p) out.add(p);
        }
        return out;
    }

    public static boolean fullSet(ServerPlayer player) {
        return wornBy(player).size() == Piece.values().length;
    }

    /** The piece a player is missing, used to decide what a reward should hand over. */
    public static Piece nextMissing(ServerPlayer player, java.util.Set<String> owned) {
        for (Piece p : Piece.values()) {
            if (!owned.contains(p.key)) return p;
        }
        return null;
    }

    // ── Charge ────────────────────────────────────────────────────────────

    public static int chargeOf(ServerPlayer player) {
        return CHARGE.getOrDefault(player.getUUID().toString(), 0);
    }

    public static void addCharge(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        if (wornBy(player).isEmpty()) return;
        String uuid = player.getUUID().toString();
        CHARGE.put(uuid, Math.min(MAX_CHARGE, CHARGE.getOrDefault(uuid, 0) + amount));
    }

    public static boolean spendCharge(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        int have = CHARGE.getOrDefault(uuid, 0);
        if (have < amount) return false;
        CHARGE.put(uuid, have - amount);
        return true;
    }

    // ── The parts, applied ────────────────────────────────────────────────

    /** Ticked once a second per player. Each worn piece contributes its quarter. */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        List<Piece> worn = wornBy(player);
        if (worn.isEmpty()) return;

        if (worn.contains(Piece.GREAVES)) {
            // Pace scales with charge, so the set rewards holding a reserve.
            int charge = chargeOf(player);
            int amp = charge > MAX_CHARGE / 2 ? 1 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, amp, true, false, false));
        }
        if (worn.contains(Piece.TREAD)) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, false));
        }
        if (worn.contains(Piece.HELM) && player.tickCount % 60 == 0) {
            markValuables(player);
        }
    }

    /** Weight banks part of incoming damage as charge instead of simply reducing it. */
    public static float onHurt(ServerPlayer player, float damage) {
        List<Piece> worn = wornBy(player);
        if (!worn.contains(Piece.PLATE)) return damage;
        addCharge(player, Math.max(1, (int) (damage * 2)));
        return damage * 0.85f;
    }

    private static void markValuables(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        net.minecraft.core.BlockPos base = player.blockPosition();
        int r = 10;
        for (int x = -r; x <= r; x += 2) {
            for (int y = -r; y <= r; y += 2) {
                for (int z = -r; z <= r; z += 2) {
                    net.minecraft.core.BlockPos p = base.offset(x, y, z);
                    if (!isValuable(level.getBlockState(p))) continue;
                    net.minecraft.world.phys.Vec3 c = net.minecraft.world.phys.Vec3.atCenterOf(p);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.WAX_ON,
                            c.x, c.y, c.z, 2, 0.2, 0.2, 0.2, 0.0);
                }
            }
        }
    }

    private static boolean isValuable(net.minecraft.world.level.block.state.BlockState s) {
        return s.is(Blocks.DIAMOND_ORE) || s.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                || s.is(Blocks.EMERALD_ORE) || s.is(Blocks.DEEPSLATE_EMERALD_ORE)
                || s.is(Blocks.ANCIENT_DEBRIS) || s.is(Blocks.GOLD_ORE) || s.is(Blocks.DEEPSLATE_GOLD_ORE);
    }

    public static void clear(UUID uuid) {
        CHARGE.remove(uuid.toString());
    }

    /** Every piece, for the catalogue screens. */
    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Piece p : Piece.values()) out.add(create(p));
        return out;
    }

    public static String keyOf(Piece p) {
        return p.key.toLowerCase(Locale.ROOT);
    }
}
