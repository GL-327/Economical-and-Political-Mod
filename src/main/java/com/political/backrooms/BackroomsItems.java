package com.political.backrooms;

import com.political.PoliticalServer;
import com.political.backrooms.attribute.ArmorAttribute;
import com.political.backrooms.attribute.AttributeRuneItem;
import com.political.backrooms.item.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class BackroomsItems {

    // ---- Existing items ----
    public static final Item INFUSED_ECHO_SHARD = registerItem("infused_echo_shard",
        s -> new InfusedEchoShardItem(s.maxCount(16)));

    public static final Item ALMOND_WATER = registerItem("almond_water",
        s -> new AlmondWaterItem(s.maxCount(16)));

    public static final Item ENERGY_DRINK = registerItem("energy_drink",
        s -> new EnergyDrinkItem(s.maxCount(16)));

    public static final Item DETERRENT_TORCH = registerItem("deterrent_torch",
        s -> new DeterrentTorchItem(s.maxCount(64)));

    // ---- Material items ----
    public static final Item CRAWLER_HIDE = registerItem("crawler_hide",
        s -> new CrawlerHideItem(s.maxCount(64)));

    public static final Item HOUND_PELT = registerItem("hound_pelt",
        s -> new HoundPeltItem(s.maxCount(64)));

    public static final Item VOID_ESSENCE = registerItem("void_essence",
        s -> new VoidEssenceItem(s.maxCount(16)));

    public static final Item SKULK_FRAGMENT = registerItem("skulk_fragment",
        s -> new SkulkFragmentItem(s.maxCount(64)));

    public static final Item ECHO_RESIDUE = registerItem("echo_residue",
        s -> new EchoResidueItem(s.maxCount(16)));

    public static final Item WARDEN_BONE = registerItem("warden_bone",
        s -> new WardenBoneItem(s.maxCount(16)));

    public static final Item TATTERED_CLOTH = registerItem("tattered_cloth",
        s -> new TatteredClothItem(s.maxCount(64)));

    public static final Item WRETCH_CLAWS = registerItem("wretch_claws",
        s -> new WretchClawsItem(s.maxCount(64)));

    public static final Item WRETCH_EYE = registerItem("wretch_eye",
        s -> new WretchEyeItem(s.maxCount(16)));

    public static final Item HOUND_FANG = registerItem("hound_fang",
        s -> new HoundFangItem(s.maxCount(64)));

    // ---- Food items ----
    public static final Item RAW_CRAWLER_MEAT = registerItem("raw_crawler_meat",
        s -> new RawCrawlerMeatItem(s.maxCount(64)));

    public static final Item COOKED_CRAWLER_MEAT = registerItem("cooked_crawler_meat",
        s -> new CookedCrawlerMeatItem(s.maxCount(64)));

    public static final Item RAW_HOUND_MEAT = registerItem("raw_hound_meat",
        s -> new RawHoundMeatItem(s.maxCount(64)));

    public static final Item COOKED_HOUND_MEAT = registerItem("cooked_hound_meat",
        s -> new CookedHoundMeatItem(s.maxCount(64)));

    public static final Item VOID_JERKY = registerItem("void_jerky",
        s -> new VoidJerkyItem(s.maxCount(16)));

    public static final Item SKULK_STEW = registerItem("skulk_stew",
        s -> new SkulkStewItem(s.maxCount(1)));

    // ---- Weapon items ----
    public static final Item BONE_SHIV = registerItem("bone_shiv",
        s -> new BoneShivItem(s.maxCount(1).maxDamage(128)
            .attributeModifiers(BoneShivItem.createAttributes())));

    public static final Item CRAWLER_SPIKE = registerItem("crawler_spike",
        s -> new CrawlerSpikeItem(s.maxCount(1).maxDamage(160)
            .attributeModifiers(CrawlerSpikeItem.createAttributes())));

    public static final Item VOID_BLADE = registerItem("void_blade",
        s -> new VoidBladeItem(s.maxCount(1).maxDamage(256)
            .attributeModifiers(VoidBladeItem.createAttributes())));

    public static final Item FACELESS_MACE = registerItem("faceless_mace",
        s -> new FacelessMaceItem(s.maxCount(1).maxDamage(300)
            .attributeModifiers(FacelessMaceItem.createAttributes())));

    public static final Item SKULK_REAPER = registerItem("skulk_reaper",
        s -> new SkulkReaperItem(s.maxCount(1).maxDamage(384)
            .attributeModifiers(SkulkReaperItem.createAttributes())));

    public static final Item ARANEA_FANG_DAGGER = registerItem("aranea_fang_dagger",
        s -> new AraneaFangDaggerItem(s.maxCount(1).maxDamage(200)
            .attributeModifiers(AraneaFangDaggerItem.createAttributes())));

    public static final Item ECHO_SCYTHE = registerItem("echo_scythe",
        s -> new EchoScytheItem(s.maxCount(1).maxDamage(512)
            .attributeModifiers(EchoScytheItem.createAttributes())));

    // ---- Tool items ----
    public static final Item VOID_PICKAXE = registerItem("void_pickaxe",
        s -> new VoidPickaxeItem(s.maxCount(1).maxDamage(512)
            .attributeModifiers(VoidPickaxeItem.createAttributes())));

    public static final Item SKULK_DRILL = registerItem("skulk_drill",
        s -> new SkulkDrillItem(s.maxCount(1).maxDamage(384)
            .attributeModifiers(SkulkDrillItem.createAttributes())));

    public static final Item ECHO_AXE = registerItem("echo_axe",
        s -> new EchoAxeItem(s.maxCount(1).maxDamage(384)
            .attributeModifiers(EchoAxeItem.createAttributes())));

    // ---- Armor Runes (one per ArmorAttribute) ----
    public static final Item RUNE_PYROMANIAC = registerItem("rune_pyromaniac",
        s -> new AttributeRuneItem(ArmorAttribute.PYROMANIAC, s.maxCount(1)));

    public static final Item RUNE_BERSERKERS_RAGE = registerItem("rune_berserkers_rage",
        s -> new AttributeRuneItem(ArmorAttribute.BERSERKERS_RAGE, s.maxCount(1)));

    public static final Item RUNE_NIGHTSTALKER = registerItem("rune_nightstalker",
        s -> new AttributeRuneItem(ArmorAttribute.NIGHTSTALKER, s.maxCount(1)));

    public static final Item RUNE_HEAVYWEIGHT = registerItem("rune_heavyweight",
        s -> new AttributeRuneItem(ArmorAttribute.HEAVYWEIGHT, s.maxCount(1)));

    public static final Item RUNE_VAMPIRIC = registerItem("rune_vampiric",
        s -> new AttributeRuneItem(ArmorAttribute.VAMPIRIC, s.maxCount(1)));

    public static final Item RUNE_ECHO_WALKER = registerItem("rune_echo_walker",
        s -> new AttributeRuneItem(ArmorAttribute.ECHO_WALKER, s.maxCount(1)));

    public static final Item RUNE_SKULK_INFUSED = registerItem("rune_skulk_infused",
        s -> new AttributeRuneItem(ArmorAttribute.SKULK_INFUSED, s.maxCount(1)));

    public static final Item RUNE_FEATHERWEIGHT = registerItem("rune_featherweight",
        s -> new AttributeRuneItem(ArmorAttribute.FEATHERWEIGHT, s.maxCount(1)));

    public static final Item RUNE_THORNED = registerItem("rune_thorned",
        s -> new AttributeRuneItem(ArmorAttribute.THORNED, s.maxCount(1)));

    public static final Item RUNE_GLUTTON = registerItem("rune_glutton",
        s -> new AttributeRuneItem(ArmorAttribute.GLUTTON, s.maxCount(1)));

    // ---- Registration helper ----

    private static <T extends Item> T registerItem(String name, Function<Item.Settings, T> factory) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM,
            Identifier.of(PoliticalServer.MOD_ID, name));
        return Registry.register(Registries.ITEM, key, factory.apply(new Item.Settings().registryKey(key)));
    }

    public static void register() {
        // All items are registered via static field initialization above.
        // This method triggers class loading to ensure registration occurs.
    }
}
