/**
 * Minecraft 26.1 named API — second pass (Mojmap / package moves).
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const REPLACEMENTS = [
  ["getPlayerManager()", "getPlayerList()"],

  ["MenuType.GENERIC_9X1", "MenuType.GENERIC_9x1"],
  ["MenuType.GENERIC_9X2", "MenuType.GENERIC_9x2"],
  ["MenuType.GENERIC_9X3", "MenuType.GENERIC_9x3"],
  ["MenuType.GENERIC_9X4", "MenuType.GENERIC_9x4"],
  ["MenuType.GENERIC_9X5", "MenuType.GENERIC_9x5"],
  ["MenuType.GENERIC_9X6", "MenuType.GENERIC_9x6"],

  [".formatted(", ".withStyle("],

  ["import net.minecraft.world.entity.monster.Mob;", "import net.minecraft.world.entity.Mob;"],
  ["net.minecraft.world.entity.monster.Mob", "net.minecraft.world.entity.Mob"],

  ["import net.minecraft.world.Heightmap;", "import net.minecraft.world.level.levelgen.Heightmap;"],
  ["import net.minecraft.world.level.Heightmap;", "import net.minecraft.world.level.levelgen.Heightmap;"],

  ["import net.minecraft.world.item.ItemConvertible;", "import net.minecraft.world.level.ItemLike;"],
  ["net.minecraft.world.item.ItemConvertible", "net.minecraft.world.level.ItemLike"],

  ["import net.minecraft.world.entity.boss.wither.Wither;", "import net.minecraft.world.entity.boss.wither.WitherBoss;"],

  ["import net.minecraft.world.item.component.EquippableComponent;", "import net.minecraft.world.item.equipment.Equippable;"],
  ["net.minecraft.world.item.component.EquippableComponent", "net.minecraft.world.item.equipment.Equippable"],

  ["import net.minecraft.world.inventory.AnvilAbstractContainerMenu;", "import net.minecraft.world.inventory.AnvilMenu;"],
  ["AnvilAbstractContainerMenu", "AnvilMenu"],

  ["import net.minecraft.world.inventory.EnchantmentAbstractContainerMenu;", "import net.minecraft.world.inventory.EnchantmentMenu;"],
  ["EnchantmentAbstractContainerMenu", "EnchantmentMenu"],

  ["import net.minecraft.world.inventory.GrindstoneAbstractContainerMenu;", "import net.minecraft.world.inventory.GrindstoneMenu;"],
  ["GrindstoneAbstractContainerMenu", "GrindstoneMenu"],

  ["import net.minecraft.world.inventory.SmithingAbstractContainerMenu;", "import net.minecraft.world.inventory.SmithingMenu;"],
  ["SmithingAbstractContainerMenu", "SmithingMenu"],

  ["import net.minecraft.world.item.ItemUsageContext;", "import net.minecraft.world.item.context.UseOnContext;"],
  ["ItemUsageContext", "UseOnContext"],

  ["import net.minecraft.world.entity.npc.TradeOffer;", "import net.minecraft.world.item.trading.MerchantOffer;"],
  ["TradeOffer", "MerchantOffer"],

  ["import net.minecraft.commands.RecipeCommand;", "import net.minecraft.server.commands.RecipeCommand;"],

  ["import net.minecraft.world.level.block.entity.MobSpawnerBlockEntity;", "import net.minecraft.world.level.block.entity.SpawnerBlockEntity;"],
  ["MobSpawnerBlockEntity", "SpawnerBlockEntity"],

  ["import net.minecraft.world.item.component.ItemEnchantmentsComponent;", "import net.minecraft.world.item.enchantment.ItemEnchantments;"],
  ["net.minecraft.world.item.component.ItemEnchantmentsComponent", "net.minecraft.world.item.enchantment.ItemEnchantments"],

  ["import net.minecraft.world.item.component.PotionContentsComponent;", "import net.minecraft.world.item.alchemy.PotionContents;"],
  ["net.minecraft.world.item.component.PotionContentsComponent", "net.minecraft.world.item.alchemy.PotionContents"],

  ["import net.minecraft.world.entity.ai.attributes.EntityAttribute;", "import net.minecraft.world.entity.ai.attributes.Attribute;"],
  ["Holder<EntityAttribute>", "Holder<Attribute>"],
  ["Registry<EntityAttribute>", "Registry<Attribute>"],

  ["import net.minecraft.world.entity.SpawnGroup;", "import net.minecraft.world.entity.MobCategory;"],
  ["SpawnGroup", "MobCategory"],

  ["import net.minecraft.world.SpawnHelper;", "import net.minecraft.world.level.NaturalSpawner;"],
  ["SpawnHelper", "NaturalSpawner"],

  ["net.minecraft.registry.entry.Holder", "net.minecraft.core.Holder"],

  ["import net.minecraft.world.GameMode;", "import net.minecraft.world.level.GameType;"],
  ["net.minecraft.world.GameMode", "net.minecraft.world.level.GameType"],

  ["import net.minecraft.world.entity.animal.IronGolem;", "import net.minecraft.world.entity.animal.golem.IronGolem;"],

  ["import net.minecraft.world.biome.Biome;", "import net.minecraft.world.level.biome.Biome;"],
  ["import net.minecraft.world.biome.source.BiomeSource;", "import net.minecraft.world.level.biome.BiomeSource;"],

  ["import net.minecraft.world.inventory.ClickType;", "import net.minecraft.world.inventory.ClickAction;"],
  ["net.minecraft.world.inventory.ClickType", "net.minecraft.world.inventory.ClickAction"],

  ["import net.minecraft.server.network.ServerPlayerInteractionManager;", "import net.minecraft.server.level.ServerPlayerGameMode;"],
  ["ServerPlayerInteractionManager", "ServerPlayerGameMode"],

  ["import net.minecraft.world.item.crafting.RecipeEntry;\n", ""],
  ["import net.minecraft.world.item.crafting.RecipeEntry;", ""],

  ["import net.minecraft.world.level.storage.loot.parameters.LootWorldContext;", "import net.minecraft.world.level.storage.loot.LootContext;"],
  ["LootWorldContext", "LootContext"],

  ["import net.minecraft.world.entity.EntityCollisionHandler;", "import net.minecraft.world.phys.shapes.EntityCollisionContext;"],
  ["EntityCollisionHandler", "EntityCollisionContext"],

  ["import net.minecraft.world.item.crafting.book.CraftingRecipeCategory;\n", ""],
  ["import net.minecraft.world.item.crafting.book.CraftingRecipeCategory;", ""],
];

function walk(dir, out = []) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, out);
    else if (e.name.endsWith(".java") && !e.name.endsWith(".backup")) out.push(p);
  }
  return out;
}

let n = 0;
for (const file of walk(ROOT)) {
  let s = fs.readFileSync(file, "utf8");
  const orig = s;
  for (const [a, b] of REPLACEMENTS) s = s.split(a).join(b);
  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("pass13_26_1 updated", n, "files");
