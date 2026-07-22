package com.political;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Backrooms consumables / crafting items. All items are vanilla ItemStacks dressed with
 * DataComponents + a custom_item_id so they feed the stats/recipe engines.
 *
 *  - almond_water        : Almond Water, cleanse + regeneration consumable
 *  - reality_shard       : Reality Shard, escape-beacon crafting ingredient
 *  - fluorescent_fragment: Fluorescent Fragment, Backrooms loot fuel
 *  - escape_beacon       : Escape Beacon, right-click to escape when inside the Backrooms
 */
public final class BackroomsItems {

    private BackroomsItems() {}

    // ── Item factories ────────────────────────────────────────────────────────

    public static ItemStack createAlmondWater(int count) {
        ItemStack s = new ItemStack(Items.HONEY_BOTTLE, count);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Almond Water").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        SlayerItems.setCustomItemId(s, "almond_water");
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7A bottle of impossibly pure water."));
        lore.add(Component.literal("§7Drink to §acleanse§7 all ailments"));
        lore.add(Component.literal("§7and gain §dRegeneration§7."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Backrooms Ration"));
        s.set(DataComponents.LORE, new ItemLore(lore));
        return s;
    }

    public static ItemStack createRealityShard(int count) {
        ItemStack s = new ItemStack(Items.ECHO_SHARD, count);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Reality Shard").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        SlayerItems.setCustomItemId(s, "reality_shard");
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7A fragment of stable reality."));
        lore.add(Component.literal("§7Used to craft an §bEscape Beacon§7."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Backrooms Material"));
        s.set(DataComponents.LORE, new ItemLore(lore));
        return s;
    }

    public static ItemStack createFluorescentFragment(int count) {
        ItemStack s = new ItemStack(Items.GLOWSTONE_DUST, count);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("Fluorescent Fragment").withStyle(ChatFormatting.YELLOW));
        SlayerItems.setCustomItemId(s, "fluorescent_fragment");
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Scraped from a humming light panel."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Backrooms Loot"));
        s.set(DataComponents.LORE, new ItemLore(lore));
        return s;
    }

    public static ItemStack createEscapeBeacon(int count) {
        ItemStack s = new ItemStack(Items.END_CRYSTAL, count);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ Escape Beacon").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        SlayerItems.setCustomItemId(s, "escape_beacon");
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Right-click inside the Backrooms"));
        lore.add(Component.literal("§7to §atear open an exit§7 back home."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Consumed on use"));
        s.set(DataComponents.LORE, new ItemLore(lore));
        return s;
    }

    // ── Recipes ────────────────────────────────────────────────────────────────

    /**
     * Registers the Backrooms crafting recipes.
     *
     * <p>Delegates to {@link V4RecipeBootstrap}, which persists them to
     * {@code custom_recipes.json} so they appear in {@code /recipe} and {@code /setrecipe}.
     *
     * <p>This used to push directly into the in-memory {@code SlayerRecipes.RECIPES} list.
     * That list is {@code clear()}ed by {@code SlayerRecipes.registerRecipes()}, which the
     * server runs after this call, so those recipes were destroyed every boot and never
     * reached the JSON config.
     */
    public static void registerRecipes() {
        V4RecipeBootstrap.ensureRecipes();
    }

    // ── Use handling ───────────────────────────────────────────────────────────

    /** Register the right-click handler for Backrooms consumables (call once at mod init). */
    public static void registerEvents() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);
            String id = SlayerItems.getCustomItemId(held);
            if (id == null) return InteractionResult.PASS;

            if ("almond_water".equals(id)) {
                return drinkAlmondWater(sp, hand, held);
            }
            if ("escape_beacon".equals(id)) {
                return useEscapeBeacon(sp, hand, held);
            }
            return InteractionResult.PASS;
        });
    }

    private static InteractionResult drinkAlmondWater(ServerPlayer sp, InteractionHand hand, ItemStack held) {
        sp.removeAllEffects();
        sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, true, true));
        sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, true));

        if (!sp.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (sp.level() instanceof ServerLevel level) {
            level.playSound(null, sp.blockPosition(),
                    net.minecraft.sounds.SoundEvents.HONEY_BLOCK_SLIDE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.4f);
        }
        sp.sendSystemMessage(Component.literal("§b✦ You sip the Almond Water. Your mind clears."), true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult useEscapeBeacon(ServerPlayer sp, InteractionHand hand, ItemStack held) {
        if (!BackroomsManager.isBackrooms(sp.level())) {
            sp.sendSystemMessage(Component.literal("§7The Escape Beacon only works inside the Backrooms.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }
        if (!sp.getAbilities().instabuild) {
            held.shrink(1);
        }
        BackroomsManager.escape(sp);
        return InteractionResult.SUCCESS;
    }
}
