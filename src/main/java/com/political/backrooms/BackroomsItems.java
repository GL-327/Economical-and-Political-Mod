package com.political.backrooms;

import com.political.PoliticalServer;
import com.political.backrooms.attribute.ArmorAttribute;
import com.political.backrooms.attribute.AttributeRuneItem;
import com.political.backrooms.item.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class BackroomsItems {

    // SECTION 1: CONSUMABLES
    public static final Item INFUSED_ECHO_SHARD = registerItem("infused_echo_shard", s -> new InfusedEchoShardItem(s.maxCount(16)));
    public static final Item ALMOND_WATER = registerItem("almond_water", s -> new AlmondWaterItem(s.maxCount(16)));
    public static final Item ENERGY_DRINK = registerItem("energy_drink", s -> new EnergyDrinkItem(s.maxCount(16)));
    public static final Item LIQUID_PAIN = registerItem("liquid_pain", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 2)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0)); }, true, false));
    public static final Item MEMORY_JUICE = registerItem("memory_juice", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 100, 1)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 0)); }, true, false));
    public static final Item WANDERERS_TEA = registerItem("wanderers_tea", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1)), false, true));
    public static final Item NULL_COLA = registerItem("null_cola", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 400, 1)), true, false));
    public static final Item FILTERED_WATER = registerItem("filtered_water", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> { user.removeStatusEffect(StatusEffects.POISON); user.removeStatusEffect(StatusEffects.WITHER); user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 0)); }, true, false));
    public static final Item SUSPICIOUS_LIQUID = registerItem("suspicious_liquid", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 600, 1)); }, true, false));
    public static final Item ADRENALINE_SHOT = registerItem("adrenaline_shot", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 2)), false, false));
    public static final Item COURAGE_ELIXIR = registerItem("courage_elixir", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 1)), true, false));
    public static final Item NIGHT_VISION_SERUM = registerItem("night_vision_serum", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3600, 0)), false, false));
    public static final Item CLARITY_POTION = registerItem("clarity_potion", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> { user.removeStatusEffect(StatusEffects.NAUSEA); user.removeStatusEffect(StatusEffects.BLINDNESS); user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 200, 2)); }, true, false));
    public static final Item ENDURANCE_BREW = registerItem("endurance_brew", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 3600, 0)), true, false));
    public static final Item HASTE_TONIC = registerItem("haste_tonic", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 1200, 2)), false, false));
    public static final Item FEATHERFALL_DRAUGHT = registerItem("featherfall_draught", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 2400, 0)), true, false));

    // SECTION 2: FOOD
    public static final Item RAW_CRAWLER_MEAT = registerItem("raw_crawler_meat", s -> new RawCrawlerMeatItem(s.maxCount(64)));
    public static final Item COOKED_CRAWLER_MEAT = registerItem("cooked_crawler_meat", s -> new CookedCrawlerMeatItem(s.maxCount(64)));
    public static final Item RAW_HOUND_MEAT = registerItem("raw_hound_meat", s -> new RawHoundMeatItem(s.maxCount(64)));
    public static final Item COOKED_HOUND_MEAT = registerItem("cooked_hound_meat", s -> new CookedHoundMeatItem(s.maxCount(64)));
    public static final Item VOID_JERKY = registerItem("void_jerky", s -> new VoidJerkyItem(s.maxCount(16)));
    public static final Item SKULK_STEW = registerItem("skulk_stew", s -> new SkulkStewItem(s.maxCount(1)));
    public static final Item MOLDY_BREAD = registerItem("moldy_bread", s -> new BackroomsConsumableItem(s.maxCount(64), UseAction.EAT, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 40, 1)); if (user.getRandom().nextFloat() < 0.4f) user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0)); }, false, false));
    public static final Item CANNED_BEANS = registerItem("canned_beans", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.EAT, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 60, 2)), false, false));
    public static final Item PROTEIN_BAR = registerItem("protein_bar", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.EAT, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 50, 2)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 0)); }, false, false));
    public static final Item EMERGENCY_RATIONS = registerItem("emergency_rations", s -> new BackroomsConsumableItem(s.maxCount(8), UseAction.EAT, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 100, 4)), false, false));
    public static final Item MYSTERY_MEAT = registerItem("mystery_meat", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.EAT, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 80, 3)); if (user.getRandom().nextBoolean()) user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 400, 0)); else user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0)); }, false, false));
    public static final Item PRESERVED_FRUIT = registerItem("preserved_fruit", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.EAT, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 40, 1)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 0)); }, false, false));
    public static final Item NUTRIENT_PASTE = registerItem("nutrient_paste", s -> new BackroomsConsumableItem(s.maxCount(8), UseAction.EAT, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 80, 4)), false, false));
    public static final Item SURVIVAL_BISCUIT = registerItem("survival_biscuit", s -> new BackroomsConsumableItem(s.maxCount(64), UseAction.EAT, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 20, 0)), false, false));
    public static final Item FACELING_STEAK = registerItem("faceling_steak", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.EAT, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 90, 5)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 400, 0)); }, false, false));

    // SECTION 3: MATERIALS
    public static final Item CRAWLER_HIDE = registerItem("crawler_hide", s -> new CrawlerHideItem(s.maxCount(64)));
    public static final Item HOUND_PELT = registerItem("hound_pelt", s -> new HoundPeltItem(s.maxCount(64)));
    public static final Item VOID_ESSENCE = registerItem("void_essence", s -> new VoidEssenceItem(s.maxCount(16)));
    public static final Item SKULK_FRAGMENT = registerItem("skulk_fragment", s -> new SkulkFragmentItem(s.maxCount(64)));
    public static final Item ECHO_RESIDUE = registerItem("echo_residue", s -> new EchoResidueItem(s.maxCount(16)));
    public static final Item WARDEN_BONE = registerItem("warden_bone", s -> new WardenBoneItem(s.maxCount(16)));
    public static final Item TATTERED_CLOTH = registerItem("tattered_cloth", s -> new TatteredClothItem(s.maxCount(64)));
    public static final Item WRETCH_CLAWS = registerItem("wretch_claws", s -> new WretchClawsItem(s.maxCount(64)));
    public static final Item WRETCH_EYE = registerItem("wretch_eye", s -> new WretchEyeItem(s.maxCount(16)));
    public static final Item HOUND_FANG = registerItem("hound_fang", s -> new HoundFangItem(s.maxCount(64)));
    public static final Item CORRUPTED_CORE = registerItem("corrupted_core", s -> new Item(s.maxCount(16)));
    public static final Item NULL_SHARD = registerItem("null_shard", s -> new Item(s.maxCount(64)));
    public static final Item TEMPORAL_FRAGMENT = registerItem("temporal_fragment", s -> new Item(s.maxCount(64)));
    public static final Item ESSENCE_OF_FEAR = registerItem("essence_of_fear", s -> new Item(s.maxCount(16)));
    public static final Item SKINSTEALER_MEMBRANE = registerItem("skinstealer_membrane", s -> new Item(s.maxCount(64)));
    public static final Item ARANEA_CHITIN = registerItem("aranea_chitin", s -> new Item(s.maxCount(64)));
    public static final Item POOLROOM_TILE = registerItem("poolroom_tile", s -> new Item(s.maxCount(64)));
    public static final Item INDUSTRIAL_SCRAP = registerItem("industrial_scrap", s -> new Item(s.maxCount(64)));
    public static final Item RUSTED_GEAR = registerItem("rusted_gear", s -> new Item(s.maxCount(64)));
    public static final Item BROKEN_CIRCUIT = registerItem("broken_circuit", s -> new Item(s.maxCount(64)));
    public static final Item FLUORESCENT_TUBE = registerItem("fluorescent_tube", s -> new Item(s.maxCount(16)));
    public static final Item MOLDY_CARPET = registerItem("moldy_carpet", s -> new Item(s.maxCount(64)));
    public static final Item YELLOW_WALLPAPER_SCRAP = registerItem("yellow_wallpaper_scrap", s -> new Item(s.maxCount(64)));
    public static final Item EXIT_SIGN_FRAGMENT = registerItem("exit_sign_fragment", s -> new Item(s.maxCount(64)));
    public static final Item VENTILATION_DUST = registerItem("ventilation_dust", s -> new Item(s.maxCount(64)));
    public static final Item CEILING_TILE = registerItem("ceiling_tile", s -> new Item(s.maxCount(64)));
    public static final Item OLD_PIPE_FITTING = registerItem("old_pipe_fitting", s -> new Item(s.maxCount(64)));
    public static final Item CORRODED_WIRE = registerItem("corroded_wire", s -> new Item(s.maxCount(64)));
    public static final Item FADED_PAINT_CHIP = registerItem("faded_paint_chip", s -> new Item(s.maxCount(64)));
    public static final Item CRACKED_LENS = registerItem("cracked_lens", s -> new Item(s.maxCount(16)));
    public static final Item UNSTABLE_ISOTOPE = registerItem("unstable_isotope", s -> new Item(s.maxCount(16)));
    public static final Item CONDENSED_FOG = registerItem("condensed_fog", s -> new Item(s.maxCount(16)));
    public static final Item LIQUID_MEMORY = registerItem("liquid_memory", s -> new Item(s.maxCount(16)));
    public static final Item CRYSTALLIZED_SILENCE = registerItem("crystallized_silence", s -> new Item(s.maxCount(16)));
    public static final Item SOLIDIFIED_DARKNESS = registerItem("solidified_darkness", s -> new Item(s.maxCount(16)));
    public static final Item HUMMING_FRAGMENT = registerItem("humming_fragment", s -> new Item(s.maxCount(64)));
    public static final Item DISTORTED_GLASS = registerItem("distorted_glass", s -> new Item(s.maxCount(64)));
    public static final Item ANOMALOUS_METAL = registerItem("anomalous_metal", s -> new Item(s.maxCount(64)));
    public static final Item PHASE_CRYSTAL = registerItem("phase_crystal", s -> new Item(s.maxCount(16)));
    public static final Item MIND_RESIDUE = registerItem("mind_residue", s -> new Item(s.maxCount(16)));
    public static final Item SOUL_WISP = registerItem("soul_wisp", s -> new Item(s.maxCount(16)));
    public static final Item NULL_THREAD = registerItem("null_thread", s -> new Item(s.maxCount(64)));
    public static final Item REALITY_ANCHOR_FRAGMENT = registerItem("reality_anchor_fragment", s -> new Item(s.maxCount(16)));
    public static final Item THRESHOLD_DUST = registerItem("threshold_dust", s -> new Item(s.maxCount(64)));
    public static final Item LIMINAL_ESSENCE = registerItem("liminal_essence", s -> new Item(s.maxCount(16)));
    public static final Item FORGOTTEN_MEMORY = registerItem("forgotten_memory", s -> new Item(s.maxCount(16)));
    public static final Item LOST_TIME = registerItem("lost_time", s -> new Item(s.maxCount(16)));
    public static final Item COMPRESSED_STATIC = registerItem("compressed_static", s -> new Item(s.maxCount(64)));
    public static final Item ECHO_PEARL = registerItem("echo_pearl", s -> new Item(s.maxCount(16)));
    public static final Item FRACTURED_REALITY = registerItem("fractured_reality", s -> new Item(s.maxCount(16)));

    // SECTION 4: THROWABLES
    public static final Item SMOKE_BOMB = registerItem("smoke_bomb", s -> new BackroomsThrowableItem(s.maxCount(16), 8.0, target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0)), "Smoke deployed"));
    public static final Item FLASH_GRENADE = registerItem("flash_grenade", s -> new BackroomsThrowableItem(s.maxCount(16), 6.0, target -> { target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0)); target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0)); }, "Flash!"));
    public static final Item NOISE_MAKER = registerItem("noise_maker", s -> new BackroomsThrowableItem(s.maxCount(16), 15.0, target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0)), "Noise activated"));
    public static final Item GLUE_BOMB = registerItem("glue_bomb", s -> new BackroomsThrowableItem(s.maxCount(16), 5.0, target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 2)), "Glue bomb detonated"));
    public static final Item FIRE_BOMB = registerItem("fire_bomb", s -> new BackroomsThrowableItem(s.maxCount(16), 5.0, target -> { target.setOnFireFor(5); target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0)); }, "Fire bomb!"));
    public static final Item FROST_BOMB = registerItem("frost_bomb", s -> new BackroomsThrowableItem(s.maxCount(16), 6.0, target -> { target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 3)); target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 1)); }, "Frost bomb!"));
    public static final Item NULL_GRENADE = registerItem("null_grenade", s -> new BackroomsThrowableItem(s.maxCount(16), 8.0, target -> { target.removeStatusEffect(StatusEffects.ABSORPTION); target.removeStatusEffect(StatusEffects.SPEED); target.removeStatusEffect(StatusEffects.STRENGTH); target.removeStatusEffect(StatusEffects.RESISTANCE); }, "Null field!"));
    public static final Item BEACON_FLARE = registerItem("beacon_flare", s -> new BackroomsThrowableItem(s.maxCount(16), 20.0, target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 400, 0)), "Beacon active"));
    public static final Item ESCAPE_ORB = registerItem("escape_orb", s -> new EscapeOrbItem(s.maxCount(8)));

    // SECTION 5: MELEE WEAPONS
    public static final Item BONE_SHIV = registerItem("bone_shiv", s -> new BoneShivItem(s.maxCount(1).maxDamage(128).attributeModifiers(BoneShivItem.createAttributes())));
    public static final Item CRAWLER_SPIKE = registerItem("crawler_spike", s -> new CrawlerSpikeItem(s.maxCount(1).maxDamage(160).attributeModifiers(CrawlerSpikeItem.createAttributes())));
    public static final Item PIPE_WRENCH = registerItem("pipe_wrench", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(180).attributeModifiers(BackroomsWeaponItem.createAttributes("pipe_wrench", 4.0, -1.2))));
    public static final Item MAKESHIFT_CLUB = registerItem("makeshift_club", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(120).attributeModifiers(BackroomsWeaponItem.createAttributes("makeshift_club", 3.0, -1.0))));
    public static final Item SHARPENED_REBAR = registerItem("sharpened_rebar", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(200).attributeModifiers(BackroomsWeaponItem.createAttributes("sharpened_rebar", 5.0, -1.4)), target -> { if (target.getRandom().nextFloat() < 0.3f) target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0)); }));
    public static final Item GLASS_SHARD_BLADE = registerItem("glass_shard_blade", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(60).attributeModifiers(BackroomsWeaponItem.createAttributes("glass_shard_blade", 4.0, -0.6))));
    public static final Item RUSTY_MACHETE = registerItem("rusty_machete", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(220).attributeModifiers(BackroomsWeaponItem.createAttributes("rusty_machete", 6.0, -1.6)), target -> { if (target.getRandom().nextFloat() < 0.25f) target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 40, 0)); }));
    public static final Item VOID_BLADE = registerItem("void_blade", s -> new VoidBladeItem(s.maxCount(1).maxDamage(256).attributeModifiers(VoidBladeItem.createAttributes())));
    public static final Item HOUND_FANG_DAGGER = registerItem("hound_fang_dagger", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(180).attributeModifiers(BackroomsWeaponItem.createAttributes("hound_fang_dagger", 7.0, -1.6)), target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0))));
    public static final Item FACELESS_MACE = registerItem("faceless_mace", s -> new FacelessMaceItem(s.maxCount(1).maxDamage(300).attributeModifiers(FacelessMaceItem.createAttributes())));
    public static final Item INDUSTRIAL_HAMMER = registerItem("industrial_hammer", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(280).attributeModifiers(BackroomsWeaponItem.createAttributes("industrial_hammer", 8.0, -1.8)), target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0))));
    public static final Item ELECTRIC_BATON = registerItem("electric_baton", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(200).attributeModifiers(BackroomsWeaponItem.createAttributes("electric_baton", 6.0, -1.4)), target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0))));
    public static final Item SERRATED_BLADE = registerItem("serrated_blade", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(240).attributeModifiers(BackroomsWeaponItem.createAttributes("serrated_blade", 7.0, -1.6)), target -> { if (target.getRandom().nextFloat() < 0.2f) target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0)); }));
    public static final Item CHAIN_WHIP = registerItem("chain_whip", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(320).attributeModifiers(BackroomsWeaponItem.createAttributes("chain_whip", 8.0, -1.8))));
    public static final Item VOID_CLEAVER = registerItem("void_cleaver", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(400).attributeModifiers(BackroomsWeaponItem.createAttributes("void_cleaver", 11.0, -3.1))));
    public static final Item SKULK_REAPER = registerItem("skulk_reaper", s -> new SkulkReaperItem(s.maxCount(1).maxDamage(384).attributeModifiers(SkulkReaperItem.createAttributes())));
    public static final Item ARANEA_FANG_DAGGER = registerItem("aranea_fang_dagger", s -> new AraneaFangDaggerItem(s.maxCount(1).maxDamage(200).attributeModifiers(AraneaFangDaggerItem.createAttributes())));
    public static final Item ECHO_SCYTHE = registerItem("echo_scythe", s -> new EchoScytheItem(s.maxCount(1).maxDamage(512).attributeModifiers(EchoScytheItem.createAttributes())));
    public static final Item NULL_BLADE = registerItem("null_blade", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(350).attributeModifiers(BackroomsWeaponItem.createAttributes("null_blade", 9.0, -1.6)), target -> target.removeStatusEffect(StatusEffects.RESISTANCE)));
    public static final Item REALITY_SLICER = registerItem("reality_slicer", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(480).attributeModifiers(BackroomsWeaponItem.createAttributes("reality_slicer", 11.0, -1.8))));
    public static final Item TEMPORAL_EDGE = registerItem("temporal_edge", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(500).attributeModifiers(BackroomsWeaponItem.createAttributes("temporal_edge", 12.0, -2.0)), target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1))));
    public static final Item NIGHTMARE_BLADE = registerItem("nightmare_blade", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(600).attributeModifiers(BackroomsWeaponItem.createAttributes("nightmare_blade", 15.0, -3.2)), target -> { target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 1)); target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1)); }));
    public static final Item THE_SILENCER = registerItem("the_silencer", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(420).attributeModifiers(BackroomsWeaponItem.createAttributes("the_silencer", 9.0, -1.2)), target -> { if (target.getRandom().nextFloat() < 0.4f) target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0)); }));
    public static final Item ENTROPY = registerItem("entropy", s -> new EntropyItem(s.maxCount(1).maxDamage(550).attributeModifiers(EntropyItem.createAttributes())));
    public static final Item MEMENTO_MORI = registerItem("memento_mori", s -> new MementoMoriItem(s.maxCount(1).maxDamage(800).attributeModifiers(MementoMoriItem.createAttributes())));
    public static final Item PARADOX = registerItem("paradox", s -> new ParadoxItem(s.maxCount(1).maxDamage(600).attributeModifiers(ParadoxItem.createAttributes())));
    public static final Item OBLIVION = registerItem("oblivion", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(700).attributeModifiers(BackroomsWeaponItem.createAttributes("oblivion", 8.0, -1.0)), target -> { double angle = target.getRandom().nextDouble() * Math.PI * 2; target.teleport(target.getX() + Math.cos(angle) * 5, target.getY(), target.getZ() + Math.sin(angle) * 5, true); }));
    public static final Item THE_LAST_WORD = registerItem("the_last_word", s -> new TheLastWordItem(s.maxCount(1).maxDamage(750).attributeModifiers(TheLastWordItem.createAttributes())));
    public static final Item DIMENSIONAL_RIFT_BLADE = registerItem("dimensional_rift_blade", s -> new DimensionalRiftBladeItem(s.maxCount(1).maxDamage(700).attributeModifiers(DimensionalRiftBladeItem.createAttributes())));
    public static final Item REALITY_CLEAVER = registerItem("reality_cleaver", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(900).attributeModifiers(BackroomsWeaponItem.createAttributes("reality_cleaver", 16.0, -3.2)), target -> { target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2)); target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 0)); }));

    // SECTION 6: RANGED WEAPONS
    public static final Item NAIL_GUN = registerItem("nail_gun", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(150).attributeModifiers(BackroomsWeaponItem.createAttributes("nail_gun", 4.0, -2.0))));
    public static final Item INDUSTRIAL_CROSSBOW = registerItem("industrial_crossbow", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(250).attributeModifiers(BackroomsWeaponItem.createAttributes("industrial_crossbow", 7.0, -2.8))));
    public static final Item ECHO_BOW = registerItem("echo_bow", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(300).attributeModifiers(BackroomsWeaponItem.createAttributes("echo_bow", 9.0, -3.0))));
    public static final Item VOID_LAUNCHER = registerItem("void_launcher", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(400).attributeModifiers(BackroomsWeaponItem.createAttributes("void_launcher", 11.0, -3.2))));
    public static final Item HARPOON_GUN = registerItem("harpoon_gun", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(200).attributeModifiers(BackroomsWeaponItem.createAttributes("harpoon_gun", 8.0, -3.4)), target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1))));
    public static final Item FLARE_GUN = registerItem("flare_gun", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(100).attributeModifiers(BackroomsWeaponItem.createAttributes("flare_gun", 5.0, -3.0)), target -> target.setOnFireFor(5)));
    public static final Item SONIC_DISRUPTOR = registerItem("sonic_disruptor", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(300).attributeModifiers(BackroomsWeaponItem.createAttributes("sonic_disruptor", 6.0, -2.5)), target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0))));
    public static final Item REALITY_FRACTURE_GUN = registerItem("reality_fracture_gun", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(500).attributeModifiers(BackroomsWeaponItem.createAttributes("reality_fracture_gun", 13.0, -3.5))));
    public static final Item NULL_RIFLE = registerItem("null_rifle", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(600).attributeModifiers(BackroomsWeaponItem.createAttributes("null_rifle", 15.0, -3.6))));
    public static final Item THE_HARBINGER = registerItem("the_harbinger", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(800).attributeModifiers(BackroomsWeaponItem.createAttributes("the_harbinger", 18.0, -3.8)), target -> { target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 1)); target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2)); }));

    // SECTION 7: TOOLS
    public static final Item VOID_PICKAXE = registerItem("void_pickaxe", s -> new VoidPickaxeItem(s.maxCount(1).maxDamage(512).attributeModifiers(VoidPickaxeItem.createAttributes())));
    public static final Item SKULK_DRILL = registerItem("skulk_drill", s -> new SkulkDrillItem(s.maxCount(1).maxDamage(384).attributeModifiers(SkulkDrillItem.createAttributes())));
    public static final Item ECHO_AXE = registerItem("echo_axe", s -> new EchoAxeItem(s.maxCount(1).maxDamage(384).attributeModifiers(EchoAxeItem.createAttributes())));
    public static final Item NULL_SHOVEL = registerItem("null_shovel", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(200).attributeModifiers(BackroomsWeaponItem.createAttributes("null_shovel", 4.5, -3.0))));
    public static final Item REALITY_PICK = registerItem("reality_pick", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(600).attributeModifiers(BackroomsWeaponItem.createAttributes("reality_pick", 6.0, -2.8))));
    public static final Item INDUSTRIAL_CUTTER = registerItem("industrial_cutter", s -> new BackroomsWeaponItem(s.maxCount(1).maxDamage(300).attributeModifiers(BackroomsWeaponItem.createAttributes("industrial_cutter", 5.0, -2.5))));
    public static final Item LEVEL_COMPASS = registerItem("level_compass", s -> new Item(s.maxCount(1)));
    public static final Item ENTITY_DETECTOR = registerItem("entity_detector", s -> new Item(s.maxCount(1)));
    public static final Item EXIT_FINDER = registerItem("exit_finder", s -> new Item(s.maxCount(1)));
    public static final Item DIMENSIONAL_ANCHOR = registerItem("dimensional_anchor", s -> new Item(s.maxCount(1)));
    public static final Item PORTABLE_LIGHT = registerItem("portable_light", s -> new Item(s.maxCount(1).maxDamage(400)));
    public static final Item ROPE_LADDER = registerItem("rope_ladder", s -> new Item(s.maxCount(16)));
    public static final Item THERMAL_SCANNER = registerItem("thermal_scanner", s -> new Item(s.maxCount(1)));
    public static final Item PORTABLE_BARRIER = registerItem("portable_barrier", s -> new Item(s.maxCount(4)));
    public static final Item REPAIR_KIT = registerItem("repair_kit", s -> new Item(s.maxCount(8)));
    public static final Item SALVAGE_TOOL = registerItem("salvage_tool", s -> new Item(s.maxCount(1).maxDamage(50)));
    public static final Item EXTRACTION_DEVICE = registerItem("extraction_device", s -> new Item(s.maxCount(4)));
    public static final Item CONTAINMENT_UNIT = registerItem("containment_unit", s -> new Item(s.maxCount(4)));
    public static final Item DIMENSIONAL_STABILIZER = registerItem("dimensional_stabilizer", s -> new Item(s.maxCount(1).maxDamage(200)));
    public static final Item GRAPPLING_HOOK = registerItem("grappling_hook", s -> new BackroomsConsumableItem(s.maxCount(1).maxDamage(80), UseAction.DRINK, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 3)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200, 0)); }, false, false));
    public static final Item CLIMBING_PICKS = registerItem("climbing_picks", s -> new BackroomsConsumableItem(s.maxCount(1).maxDamage(100), UseAction.DRINK, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 1200, 0)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 1200, 0)); }, false, false));
    public static final Item DIVING_GEAR = registerItem("diving_gear", s -> new BackroomsConsumableItem(s.maxCount(1).maxDamage(400), UseAction.DRINK, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 3600, 0)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 3600, 0)); }, false, false));
    public static final Item GAS_MASK = registerItem("gas_mask", s -> new BackroomsConsumableItem(s.maxCount(1).maxDamage(200), UseAction.DRINK, user -> { user.removeStatusEffect(StatusEffects.NAUSEA); user.removeStatusEffect(StatusEffects.BLINDNESS); user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2400, 0)); }, false, false));
    public static final Item NIGHT_VISION_GOGGLES = registerItem("night_vision_goggles", s -> new BackroomsConsumableItem(s.maxCount(1).maxDamage(500), UseAction.DRINK, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 6000, 0)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 6000, 0)); }, false, false));
    public static final Item AUDIO_DECOY = registerItem("audio_decoy", s -> new BackroomsThrowableItem(s.maxCount(8), 20.0, target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0)), "Audio decoy activated"));

    // SECTION 8: ARMOR SETS
    // Crawler Set (Tier 1) - 2pc: Resistance I; 4pc: +Haste I
    public static final Item CRAWLER_HELMET = registerItem("crawler_helmet", s -> new Item(s.maxCount(1).maxDamage(180).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("crawler_helmet", EquipmentSlot.HEAD, 2.0, 0.0))));
    public static final Item CRAWLER_CHESTPLATE = registerItem("crawler_chestplate", s -> new Item(s.maxCount(1).maxDamage(280).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("crawler_chestplate", EquipmentSlot.CHEST, 5.0, 0.0))));
    public static final Item CRAWLER_LEGGINGS = registerItem("crawler_leggings", s -> new Item(s.maxCount(1).maxDamage(260).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("crawler_leggings", EquipmentSlot.LEGS, 3.0, 0.0))));
    public static final Item CRAWLER_BOOTS = registerItem("crawler_boots", s -> new Item(s.maxCount(1).maxDamage(160).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("crawler_boots", EquipmentSlot.FEET, 1.0, 0.0))));
    // Hound Set (Tier 2) - 2pc: Speed I; 4pc: Speed II
    public static final Item HOUND_HELMET = registerItem("hound_helmet", s -> new Item(s.maxCount(1).maxDamage(260).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("hound_helmet", EquipmentSlot.HEAD, 3.0, 0.0))));
    public static final Item HOUND_CHESTPLATE = registerItem("hound_chestplate", s -> new Item(s.maxCount(1).maxDamage(380).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("hound_chestplate", EquipmentSlot.CHEST, 7.0, 0.0))));
    public static final Item HOUND_LEGGINGS = registerItem("hound_leggings", s -> new Item(s.maxCount(1).maxDamage(340).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("hound_leggings", EquipmentSlot.LEGS, 5.0, 0.0))));
    public static final Item HOUND_BOOTS = registerItem("hound_boots", s -> new Item(s.maxCount(1).maxDamage(220).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("hound_boots", EquipmentSlot.FEET, 2.0, 0.0))));
    // Faceling Set (Tier 2) - 2pc: Strength I; 4pc: Strength II
    public static final Item FACELING_HELMET = registerItem("faceling_helmet", s -> new Item(s.maxCount(1).maxDamage(280).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("faceling_helmet", EquipmentSlot.HEAD, 3.0, 0.0))));
    public static final Item FACELING_CHESTPLATE = registerItem("faceling_chestplate", s -> new Item(s.maxCount(1).maxDamage(400).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("faceling_chestplate", EquipmentSlot.CHEST, 8.0, 0.0))));
    public static final Item FACELING_LEGGINGS = registerItem("faceling_leggings", s -> new Item(s.maxCount(1).maxDamage(360).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("faceling_leggings", EquipmentSlot.LEGS, 6.0, 0.0))));
    public static final Item FACELING_BOOTS = registerItem("faceling_boots", s -> new Item(s.maxCount(1).maxDamage(240).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("faceling_boots", EquipmentSlot.FEET, 2.0, 0.0))));
    // Void Set (Tier 3) - 2pc: Night Vision; 4pc: +Absorption II
    public static final Item VOID_HELMET = registerItem("void_helmet", s -> new Item(s.maxCount(1).maxDamage(350).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("void_helmet", EquipmentSlot.HEAD, 4.0, 1.0))));
    public static final Item VOID_CHESTPLATE = registerItem("void_chestplate", s -> new Item(s.maxCount(1).maxDamage(520).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("void_chestplate", EquipmentSlot.CHEST, 10.0, 1.0))));
    public static final Item VOID_LEGGINGS = registerItem("void_leggings", s -> new Item(s.maxCount(1).maxDamage(470).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("void_leggings", EquipmentSlot.LEGS, 7.0, 1.0))));
    public static final Item VOID_BOOTS = registerItem("void_boots", s -> new Item(s.maxCount(1).maxDamage(300).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("void_boots", EquipmentSlot.FEET, 3.0, 1.0))));
    // Skulk Set (Tier 3) - 2pc: Haste I; 4pc: Haste II + Strength I
    public static final Item SKULK_HELMET = registerItem("skulk_helmet", s -> new Item(s.maxCount(1).maxDamage(420).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("skulk_helmet", EquipmentSlot.HEAD, 5.0, 1.0))));
    public static final Item SKULK_CHESTPLATE = registerItem("skulk_chestplate", s -> new Item(s.maxCount(1).maxDamage(620).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("skulk_chestplate", EquipmentSlot.CHEST, 12.0, 1.0))));
    public static final Item SKULK_LEGGINGS = registerItem("skulk_leggings", s -> new Item(s.maxCount(1).maxDamage(560).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("skulk_leggings", EquipmentSlot.LEGS, 8.0, 1.0))));
    public static final Item SKULK_BOOTS = registerItem("skulk_boots", s -> new Item(s.maxCount(1).maxDamage(360).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("skulk_boots", EquipmentSlot.FEET, 3.0, 1.0))));
    // Industrial Set (Tier 3) - 2pc: Resistance I; 4pc: +Fire Resistance
    public static final Item INDUSTRIAL_HELMET = registerItem("industrial_helmet", s -> new Item(s.maxCount(1).maxDamage(360).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("industrial_helmet", EquipmentSlot.HEAD, 3.0, 1.0))));
    public static final Item INDUSTRIAL_CHESTPLATE = registerItem("industrial_chestplate", s -> new Item(s.maxCount(1).maxDamage(540).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("industrial_chestplate", EquipmentSlot.CHEST, 8.0, 1.0))));
    public static final Item INDUSTRIAL_LEGGINGS = registerItem("industrial_leggings", s -> new Item(s.maxCount(1).maxDamage(500).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("industrial_leggings", EquipmentSlot.LEGS, 5.0, 1.0))));
    public static final Item INDUSTRIAL_BOOTS = registerItem("industrial_boots", s -> new Item(s.maxCount(1).maxDamage(320).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("industrial_boots", EquipmentSlot.FEET, 2.0, 1.0))));
    // Poolrooms Set (Tier 2) - 2pc: Water Breathing + Slow Falling; 4pc: +Dolphin's Grace
    public static final Item POOLROOMS_HELMET = registerItem("poolrooms_helmet", s -> new Item(s.maxCount(1).maxDamage(240).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("poolrooms_helmet", EquipmentSlot.HEAD, 2.0, 0.0))));
    public static final Item POOLROOMS_CHESTPLATE = registerItem("poolrooms_chestplate", s -> new Item(s.maxCount(1).maxDamage(360).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("poolrooms_chestplate", EquipmentSlot.CHEST, 6.0, 0.0))));
    public static final Item POOLROOMS_LEGGINGS = registerItem("poolrooms_leggings", s -> new Item(s.maxCount(1).maxDamage(320).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("poolrooms_leggings", EquipmentSlot.LEGS, 4.0, 0.0))));
    public static final Item POOLROOMS_BOOTS = registerItem("poolrooms_boots", s -> new Item(s.maxCount(1).maxDamage(200).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("poolrooms_boots", EquipmentSlot.FEET, 2.0, 0.0))));
    // Wretch Set (Tier 1) - 4pc: Regeneration I
    public static final Item WRETCH_HELMET = registerItem("wretch_helmet", s -> new Item(s.maxCount(1).maxDamage(200).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("wretch_helmet", EquipmentSlot.HEAD, 2.0, 0.0))));
    public static final Item WRETCH_CHESTPLATE = registerItem("wretch_chestplate", s -> new Item(s.maxCount(1).maxDamage(300).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("wretch_chestplate", EquipmentSlot.CHEST, 5.0, 0.0))));
    public static final Item WRETCH_LEGGINGS = registerItem("wretch_leggings", s -> new Item(s.maxCount(1).maxDamage(280).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("wretch_leggings", EquipmentSlot.LEGS, 4.0, 0.0))));
    public static final Item WRETCH_BOOTS = registerItem("wretch_boots", s -> new Item(s.maxCount(1).maxDamage(180).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("wretch_boots", EquipmentSlot.FEET, 2.0, 0.0))));
    // Null Set (Tier 4) - 2pc: Resistance I; 4pc: Resistance II + Absorption II
    public static final Item NULL_HELMET = registerItem("null_helmet", s -> new Item(s.maxCount(1).maxDamage(500).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("null_helmet", EquipmentSlot.HEAD, 4.0, 2.0))));
    public static final Item NULL_CHESTPLATE = registerItem("null_chestplate", s -> new Item(s.maxCount(1).maxDamage(740).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("null_chestplate", EquipmentSlot.CHEST, 11.0, 2.0))));
    public static final Item NULL_LEGGINGS = registerItem("null_leggings", s -> new Item(s.maxCount(1).maxDamage(670).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("null_leggings", EquipmentSlot.LEGS, 8.0, 2.0))));
    public static final Item NULL_BOOTS = registerItem("null_boots", s -> new Item(s.maxCount(1).maxDamage(440).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("null_boots", EquipmentSlot.FEET, 2.0, 2.0))));
    // Temporal Set (Tier 4) - 2pc: Haste I + Speed I; 4pc: Haste II + Speed II
    public static final Item TEMPORAL_HELMET = registerItem("temporal_helmet", s -> new Item(s.maxCount(1).maxDamage(550).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("temporal_helmet", EquipmentSlot.HEAD, 4.0, 2.0))));
    public static final Item TEMPORAL_CHESTPLATE = registerItem("temporal_chestplate", s -> new Item(s.maxCount(1).maxDamage(820).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("temporal_chestplate", EquipmentSlot.CHEST, 9.0, 2.0))));
    public static final Item TEMPORAL_LEGGINGS = registerItem("temporal_leggings", s -> new Item(s.maxCount(1).maxDamage(740).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("temporal_leggings", EquipmentSlot.LEGS, 7.0, 2.0))));
    public static final Item TEMPORAL_BOOTS = registerItem("temporal_boots", s -> new Item(s.maxCount(1).maxDamage(490).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("temporal_boots", EquipmentSlot.FEET, 2.0, 2.0))));
    // Ascendant Set (Tier 5) - 2pc: Strength I + Resistance I; 4pc: Strength II + Resistance II + Regeneration
    public static final Item ASCENDANT_HELMET = registerItem("ascendant_helmet", s -> new Item(s.maxCount(1).maxDamage(700).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("ascendant_helmet", EquipmentSlot.HEAD, 5.0, 3.0))));
    public static final Item ASCENDANT_CHESTPLATE = registerItem("ascendant_chestplate", s -> new Item(s.maxCount(1).maxDamage(1050).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("ascendant_chestplate", EquipmentSlot.CHEST, 13.0, 3.0))));
    public static final Item ASCENDANT_LEGGINGS = registerItem("ascendant_leggings", s -> new Item(s.maxCount(1).maxDamage(950).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("ascendant_leggings", EquipmentSlot.LEGS, 9.0, 3.0))));
    public static final Item ASCENDANT_BOOTS = registerItem("ascendant_boots", s -> new Item(s.maxCount(1).maxDamage(630).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("ascendant_boots", EquipmentSlot.FEET, 3.0, 3.0))));
    // Reality Weaver Set (Tier 5, Legendary) - 2pc: Absorption + Resistance; 4pc: +Strength II + Regeneration
    public static final Item REALITY_WEAVER_HELMET = registerItem("reality_weaver_helmet", s -> new Item(s.maxCount(1).maxDamage(850).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("reality_weaver_helmet", EquipmentSlot.HEAD, 5.0, 4.0))));
    public static final Item REALITY_WEAVER_CHESTPLATE = registerItem("reality_weaver_chestplate", s -> new Item(s.maxCount(1).maxDamage(1300).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("reality_weaver_chestplate", EquipmentSlot.CHEST, 14.0, 4.0))));
    public static final Item REALITY_WEAVER_LEGGINGS = registerItem("reality_weaver_leggings", s -> new Item(s.maxCount(1).maxDamage(1170).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("reality_weaver_leggings", EquipmentSlot.LEGS, 10.0, 4.0))));
    public static final Item REALITY_WEAVER_BOOTS = registerItem("reality_weaver_boots", s -> new Item(s.maxCount(1).maxDamage(780).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("reality_weaver_boots", EquipmentSlot.FEET, 3.0, 4.0))));

    // SECTION 9: SPECIAL ARMOR PIECES
    public static final Item CROWN_OF_THE_LOST = registerItem("crown_of_the_lost", s -> new Item(s.maxCount(1).maxDamage(600).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("crown_of_the_lost", EquipmentSlot.HEAD, 4.0, 2.0))));
    public static final Item WANDERERS_HOOD = registerItem("wanderers_hood", s -> new Item(s.maxCount(1).maxDamage(400).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("wanderers_hood", EquipmentSlot.HEAD, 2.0, 0.0))));
    public static final Item THIRD_EYE_VISOR = registerItem("third_eye_visor", s -> new Item(s.maxCount(1).maxDamage(500).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("third_eye_visor", EquipmentSlot.HEAD, 3.0, 1.0))));
    public static final Item SCREAMERS_MASK = registerItem("screamers_mask", s -> new Item(s.maxCount(1).maxDamage(350).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("screamers_mask", EquipmentSlot.HEAD, 3.0, 0.0))));
    public static final Item BREATHING_APPARATUS = registerItem("breathing_apparatus", s -> new Item(s.maxCount(1).maxDamage(800).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("breathing_apparatus", EquipmentSlot.HEAD, 3.0, 1.0))));
    public static final Item COAT_OF_MANY_FACES = registerItem("coat_of_many_faces", s -> new Item(s.maxCount(1).maxDamage(900).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("coat_of_many_faces", EquipmentSlot.CHEST, 7.0, 2.0))));
    public static final Item REACTOR_CORE_VEST = registerItem("reactor_core_vest", s -> new Item(s.maxCount(1).maxDamage(1100).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("reactor_core_vest", EquipmentSlot.CHEST, 11.0, 3.0))));
    public static final Item PHASE_SHIFT_JACKET = registerItem("phase_shift_jacket", s -> new Item(s.maxCount(1).maxDamage(700).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("phase_shift_jacket", EquipmentSlot.CHEST, 8.0, 2.0))));
    public static final Item LIVING_ARMOR = registerItem("living_armor", s -> new Item(s.maxCount(1).maxDamage(1200).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("living_armor", EquipmentSlot.CHEST, 13.0, 4.0))));
    public static final Item MIRROR_SHIELD_PLATE = registerItem("mirror_shield_plate", s -> new Item(s.maxCount(1).maxDamage(1000).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("mirror_shield_plate", EquipmentSlot.CHEST, 10.0, 3.0))));
    public static final Item BLINK_LEGGINGS = registerItem("blink_leggings", s -> new Item(s.maxCount(1).maxDamage(800).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("blink_leggings", EquipmentSlot.LEGS, 9.0, 3.0))));
    public static final Item SPRINT_ENHANCERS = registerItem("sprint_enhancers", s -> new Item(s.maxCount(1).maxDamage(600).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.LEGS).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("sprint_enhancers", EquipmentSlot.LEGS, 6.0, 1.0))));
    public static final Item GRAVITY_DEFIERS = registerItem("gravity_defiers", s -> new Item(s.maxCount(1).maxDamage(700).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("gravity_defiers", EquipmentSlot.FEET, 3.0, 2.0))));
    public static final Item HOVER_BOOTS = registerItem("hover_boots", s -> new Item(s.maxCount(1).maxDamage(500).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("hover_boots", EquipmentSlot.FEET, 2.0, 1.0))));
    public static final Item GROUND_POUND_BOOTS = registerItem("ground_pound_boots", s -> new Item(s.maxCount(1).maxDamage(600).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.FEET).build()).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttr("ground_pound_boots", EquipmentSlot.FEET, 3.0, 2.0))));

    // SECTION 10: UTILITY ITEMS
    public static final Item DETERRENT_TORCH = registerItem("deterrent_torch", s -> new DeterrentTorchItem(s.maxCount(64)));
    public static final Item UV_FLASHLIGHT = registerItem("uv_flashlight", s -> new Item(s.maxCount(1).maxDamage(200)));
    public static final Item PORTABLE_GENERATOR = registerItem("portable_generator", s -> new Item(s.maxCount(1)));
    public static final Item BATTERY_PACK = registerItem("battery_pack", s -> new Item(s.maxCount(8)));
    public static final Item FIRST_AID_KIT = registerItem("first_aid_kit", s -> new FirstAidKitItem(s.maxCount(8)));
    public static final Item ANTIDOTE = registerItem("antidote", s -> new AntidoteItem(s.maxCount(8)));
    public static final Item LOCKPICK_SET = registerItem("lockpick_set", s -> new Item(s.maxCount(1).maxDamage(20)));
    public static final Item KEYCARD = registerItem("keycard", s -> new Item(s.maxCount(1)));
    public static final Item RADIO_TRANSMITTER = registerItem("radio_transmitter", s -> new Item(s.maxCount(1)));
    public static final Item HOLOGRAPHIC_DECOY = registerItem("holographic_decoy", s -> new Item(s.maxCount(4)));
    public static final Item CLOAKING_DEVICE = registerItem("cloaking_device", s -> new CloakingDeviceItem(s.maxCount(8)));
    public static final Item MOTION_SENSOR = registerItem("motion_sensor", s -> new Item(s.maxCount(4)));
    public static final Item SOUND_TRAP = registerItem("sound_trap", s -> new Item(s.maxCount(8)));
    public static final Item TRIPWIRE_ALARM = registerItem("tripwire_alarm", s -> new Item(s.maxCount(16)));
    public static final Item PORTABLE_TURRET = registerItem("portable_turret", s -> new Item(s.maxCount(2)));
    public static final Item AMMO_BOX = registerItem("ammo_box", s -> new Item(s.maxCount(16)));
    public static final Item TOOL_BOX = registerItem("tool_box", s -> new Item(s.maxCount(4)));
    public static final Item MAP_FRAGMENT = registerItem("map_fragment", s -> new Item(s.maxCount(64)));
    public static final Item COMPASS_OF_RETURN = registerItem("compass_of_return", s -> new Item(s.maxCount(1)));
    public static final Item WAYPOINT_MARKER = registerItem("waypoint_marker", s -> new Item(s.maxCount(16)));
    public static final Item TELEPORT_BEACON = registerItem("teleport_beacon", s -> new Item(s.maxCount(4)));
    public static final Item DIMENSION_SHARD = registerItem("dimension_shard", s -> new Item(s.maxCount(16)));
    public static final Item EXIT_KEY = registerItem("exit_key", s -> new Item(s.maxCount(1)));
    public static final Item SAFE_ROOM_CREATOR = registerItem("safe_room_creator", s -> new Item(s.maxCount(1)));
    public static final Item EMERGENCY_FLARE = registerItem("emergency_flare", s -> new BackroomsThrowableItem(s.maxCount(16), 20.0, target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 600, 0)), "Emergency flare lit"));
    public static final Item SIGNAL_JAMMER = registerItem("signal_jammer", s -> new BackroomsThrowableItem(s.maxCount(8), 10.0, target -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 2)), "Signal jammed"));
    public static final Item GLOW_STICK = registerItem("glow_stick", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.DRINK, user -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 1200, 0)), false, false));
    public static final Item BANDAGES = registerItem("bandages", s -> new BackroomsConsumableItem(s.maxCount(16), UseAction.EAT, user -> { user.heal(4.0f); user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0)); }, false, false));
    public static final Item ADRENALINE_INJECTOR = registerItem("adrenaline_injector", s -> new BackroomsConsumableItem(s.maxCount(8), UseAction.DRINK, user -> { user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 2)); user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 300, 1)); }, false, false));
    public static final Item HEALING_STATION = registerItem("healing_station", s -> new FirstAidKitItem(s.maxCount(4)));

    // SECTION 11: DECORATIVE / LORE
    public static final Item FADED_PHOTOGRAPH = registerItem("faded_photograph", s -> new Item(s.maxCount(1)));
    public static final Item OLD_JOURNAL = registerItem("old_journal", s -> new Item(s.maxCount(1)));
    public static final Item VHS_TAPE = registerItem("vhs_tape", s -> new Item(s.maxCount(1)));
    public static final Item CASSETTE_TAPE = registerItem("cassette_tape", s -> new Item(s.maxCount(1)));
    public static final Item RESEARCH_NOTES = registerItem("research_notes", s -> new Item(s.maxCount(1)));
    public static final Item INCIDENT_REPORT = registerItem("incident_report", s -> new Item(s.maxCount(1)));
    public static final Item EMPLOYEE_BADGE = registerItem("employee_badge", s -> new Item(s.maxCount(1)));
    public static final Item WARNING_SIGN = registerItem("warning_sign", s -> new Item(s.maxCount(1)));
    public static final Item EXIT_SIGN = registerItem("exit_sign", s -> new Item(s.maxCount(1)));
    public static final Item EMERGENCY_POSTER = registerItem("emergency_poster", s -> new Item(s.maxCount(1)));
    public static final Item OLD_NEWSPAPER = registerItem("old_newspaper", s -> new Item(s.maxCount(1)));
    public static final Item CHILDS_DRAWING = registerItem("childs_drawing", s -> new Item(s.maxCount(1)));
    public static final Item LOVE_LETTER = registerItem("love_letter", s -> new Item(s.maxCount(1)));
    public static final Item SHOPPING_LIST = registerItem("shopping_list", s -> new Item(s.maxCount(1)));
    public static final Item POLAROID = registerItem("polaroid", s -> new Item(s.maxCount(1)));
    public static final Item BROKEN_WATCH = registerItem("broken_watch", s -> new Item(s.maxCount(1)));
    public static final Item EMPTY_WALLET = registerItem("empty_wallet", s -> new Item(s.maxCount(1)));
    public static final Item KEYS_TO_NOWHERE = registerItem("keys_to_nowhere", s -> new Item(s.maxCount(1)));
    public static final Item TOY_ROBOT = registerItem("toy_robot", s -> new Item(s.maxCount(1)));
    public static final Item MUSIC_BOX = registerItem("music_box", s -> new Item(s.maxCount(1)));
    public static final Item SNOW_GLOBE = registerItem("snow_globe", s -> new Item(s.maxCount(1)));
    public static final Item COFFEE_MUG = registerItem("coffee_mug", s -> new Item(s.maxCount(1)));
    public static final Item LORE_NAME_TAG = registerItem("lore_name_tag", s -> new Item(s.maxCount(1)));
    public static final Item GLASSES = registerItem("glasses", s -> new Item(s.maxCount(1)));
    public static final Item WEDDING_RING = registerItem("wedding_ring", s -> new Item(s.maxCount(1)));

    // SECTION 12: SPECIAL / UNIQUE
    public static final Item SKELETON_KEY = registerItem("skeleton_key", s -> new Item(s.maxCount(1)));
    public static final Item POCKET_DIMENSION = registerItem("pocket_dimension", s -> new Item(s.maxCount(1)));
    public static final Item QUANTUM_ENTANGLER = registerItem("quantum_entangler", s -> new Item(s.maxCount(1)));
    public static final Item REALITY_ANCHOR = registerItem("reality_anchor", s -> new Item(s.maxCount(1)));
    public static final Item THE_COLLECTOR = registerItem("the_collector", s -> new Item(s.maxCount(1)));
    public static final Item INFINITY_COMPASS = registerItem("infinity_compass", s -> new Item(s.maxCount(1)));
    public static final Item THE_NEGOTIATOR = registerItem("the_negotiator", s -> new Item(s.maxCount(1)));
    public static final Item DOPPELGANGER_SEED = registerItem("doppelganger_seed", s -> new Item(s.maxCount(8)));
    public static final Item NULL_CRYSTAL = registerItem("null_crystal", s -> new AntidoteItem(s.maxCount(8)));
    public static final Item ADMIN_BADGE = registerItem("admin_badge", s -> new Item(s.maxCount(1)));
    public static final Item MEMORY_WIPER = registerItem("memory_wiper", s -> new MemoryWiperItem(s.maxCount(8)));
    public static final Item SOUL_VESSEL = registerItem("soul_vessel", s -> new SoulVesselItem(s.maxCount(8)));
    public static final Item PHOENIX_FEATHER = registerItem("phoenix_feather", s -> new PhoenixFeatherItem(s.maxCount(4)));
    public static final Item THE_EXIT = registerItem("the_exit", s -> new EscapeOrbItem(s.maxCount(4)));
    public static final Item TIME_STOPPER = registerItem("time_stopper", s -> new BackroomsThrowableItem(s.maxCount(4), 8.0, target -> { target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 4)); target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 160, 2)); }, "Time stopped!"));

    // SECTION 13: ARMOR RUNES
    public static final Item RUNE_PYROMANIAC = registerItem("rune_pyromaniac", s -> new AttributeRuneItem(ArmorAttribute.PYROMANIAC, s.maxCount(1)));
    public static final Item RUNE_BERSERKERS_RAGE = registerItem("rune_berserkers_rage", s -> new AttributeRuneItem(ArmorAttribute.BERSERKERS_RAGE, s.maxCount(1)));
    public static final Item RUNE_NIGHTSTALKER = registerItem("rune_nightstalker", s -> new AttributeRuneItem(ArmorAttribute.NIGHTSTALKER, s.maxCount(1)));
    public static final Item RUNE_HEAVYWEIGHT = registerItem("rune_heavyweight", s -> new AttributeRuneItem(ArmorAttribute.HEAVYWEIGHT, s.maxCount(1)));
    public static final Item RUNE_VAMPIRIC = registerItem("rune_vampiric", s -> new AttributeRuneItem(ArmorAttribute.VAMPIRIC, s.maxCount(1)));
    public static final Item RUNE_ECHO_WALKER = registerItem("rune_echo_walker", s -> new AttributeRuneItem(ArmorAttribute.ECHO_WALKER, s.maxCount(1)));
    public static final Item RUNE_SKULK_INFUSED = registerItem("rune_skulk_infused", s -> new AttributeRuneItem(ArmorAttribute.SKULK_INFUSED, s.maxCount(1)));
    public static final Item RUNE_FEATHERWEIGHT = registerItem("rune_featherweight", s -> new AttributeRuneItem(ArmorAttribute.FEATHERWEIGHT, s.maxCount(1)));
    public static final Item RUNE_THORNED = registerItem("rune_thorned", s -> new AttributeRuneItem(ArmorAttribute.THORNED, s.maxCount(1)));
    public static final Item RUNE_GLUTTON = registerItem("rune_glutton", s -> new AttributeRuneItem(ArmorAttribute.GLUTTON, s.maxCount(1)));

    // HELPERS
    static AttributeModifiersComponent armorAttr(String id, EquipmentSlot slot, double def, double toughness) {
        AttributeModifierSlot attrSlot = switch (slot) {
            case HEAD  -> AttributeModifierSlot.HEAD;
            case CHEST -> AttributeModifierSlot.CHEST;
            case LEGS  -> AttributeModifierSlot.LEGS;
            case FEET  -> AttributeModifierSlot.FEET;
            default    -> AttributeModifierSlot.ARMOR;
        };
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder()
            .add(EntityAttributes.ARMOR,
                new EntityAttributeModifier(
                    Identifier.of(PoliticalServer.MOD_ID, id + "_armor"),
                    def,
                    EntityAttributeModifier.Operation.ADD_VALUE),
                attrSlot);
        if (toughness > 0) {
            builder.add(EntityAttributes.ARMOR_TOUGHNESS,
                new EntityAttributeModifier(
                    Identifier.of(PoliticalServer.MOD_ID, id + "_toughness"),
                    toughness,
                    EntityAttributeModifier.Operation.ADD_VALUE),
                attrSlot);
        }
        return builder.build();
    }

    private static <T extends Item> T registerItem(String name, Function<Item.Settings, T> factory) {
        // Only register items on server side to prevent client registry sync issues
        try {
            // Check if we're on a server by trying to get the current server
            if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER) {
                RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM,
                    Identifier.of(PoliticalServer.MOD_ID, name));
                return Registry.register(Registries.ITEM, key, factory.apply(new Item.Settings().registryKey(key)));
            }
        } catch (Exception e) {
            // If registration fails, create a dummy item
            PoliticalServer.LOGGER.warn("Failed to register item " + name + ": " + e.getMessage());
        }
        // Return null if not on server or registration failed
        return null;
    }

    public static void register() {
        // All items are registered via static field initialization above.
        // This method triggers class loading to ensure registration occurs.
    }
}
