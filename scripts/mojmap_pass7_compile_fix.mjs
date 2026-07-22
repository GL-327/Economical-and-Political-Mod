/**
 * Mojmap 26.1 compile fixes — ordered replacements.
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src", "main", "java");

const REPLACEMENTS = [
  ["import net.minecraft.world.entity.MobSpawnType;", "import net.minecraft.world.entity.EntitySpawnReason;"],
  ["net.minecraft.world.entity.MobSpawnType", "net.minecraft.world.entity.EntitySpawnReason"],
  ["MobSpawnType.", "EntitySpawnReason."],

  ["DataComponentTypes.", "DataComponents."],
  ["import net.minecraft.core.component.DataComponentTypes;", "import net.minecraft.core.component.DataComponents;"],

  ["import net.minecraft.world.item.component.EquipmentSlotGroup;", "import net.minecraft.world.entity.EquipmentSlotGroup;"],

  ["import net.minecraft.world.level.block.BlockState;", "import net.minecraft.world.level.block.state.BlockState;"],

  ["import net.minecraft.util.WorldSavePath;", "import net.minecraft.world.level.storage.LevelResource;"],
  ["net.minecraft.util.WorldSavePath.ROOT", "net.minecraft.world.level.storage.LevelResource.ROOT"],
  ["WorldSavePath.ROOT", "LevelResource.ROOT"],
  [".getSavePath(", ".getWorldPath("],

  ["import net.minecraft.util.ActionResult;", "import net.minecraft.world.InteractionResult;"],
  ["net.minecraft.util.ActionResult", "net.minecraft.world.InteractionResult"],

  ["net.minecraft.sound.SoundEvent", "net.minecraft.sounds.SoundEvent"],
  ["import net.minecraft.sound.", "import net.minecraft.sounds."],

  ["net.minecraft.core.particles.ParticleEffect", "net.minecraft.core.particles.ParticleOptions"],
  ["import net.minecraft.core.particles.ParticleEffect;", "import net.minecraft.core.particles.ParticleOptions;"],

  ["net.minecraft.registry.tag.", "net.minecraft.tags."],
  ["import net.minecraft.registry.tag.", "import net.minecraft.tags."],
  ["net.minecraft.registry.ResourceKey<", "net.minecraft.resources.ResourceKey<"],
  ["import net.minecraft.registry.RegistryOps;", "import net.minecraft.resources.RegistryOps;"],
  ["import net.minecraft.registry.DynamicRegistryManager;", "import net.minecraft.core.RegistryAccess;"],
  ["net.minecraft.registry.DynamicRegistryManager", "net.minecraft.core.RegistryAccess"],
  ["DynamicRegistryManager.", "RegistryAccess."],

  ["import net.minecraft.recipe.", "import net.minecraft.world.item.crafting."],
  ["net.minecraft.recipe.", "net.minecraft.world.item.crafting."],
  ["RecipeEntry<", "RecipeHolder<"],

  ["CraftingAbstractContainerMenu", "CraftingMenu"],

  ["import net.minecraft.inventory.CraftingResultInventory;", "import net.minecraft.world.inventory.ResultContainer;"],
  ["import net.minecraft.inventory.RecipeInputInventory;", "import net.minecraft.world.inventory.TransientCraftingContainer;"],
  ["CraftingResultInventory", "ResultContainer"],
  ["RecipeInputInventory", "TransientCraftingContainer"],
  ["import net.minecraft.inventory.CraftingInventory;", "import net.minecraft.world.inventory.CraftingContainer;"],
  ["CraftingInventory", "CraftingContainer"],

  ["import net.minecraft.world.inventory.slot.Slot;", "import net.minecraft.world.inventory.Slot;"],

  ["SimpleNamedAbstractContainerMenuFactory", "SimpleMenuProvider"],

  ["import net.minecraft.world.entity.boss.ServerBossBar;", "import net.minecraft.server.level.ServerBossEvent;"],
  ["import net.minecraft.world.entity.boss.BossBar;", "import net.minecraft.world.BossEvent;"],
  ["ServerBossBar", "ServerBossEvent"],
  ["BossBar.Color", "BossEvent.BossBarColor"],
  ["BossBar.Style", "BossEvent.BossBarOverlay"],
  ["BossBar.Overlay", "BossEvent.BossBarOverlay"],

  ["import net.minecraft.world.entity.monster.SlimeEntity;", "import net.minecraft.world.entity.monster.Slime;"],
  ["SlimeEntity", "Slime"],

  ["import net.minecraft.world.entity.LightningEntity;", "import net.minecraft.world.entity.LightningBolt;"],
  ["import net.minecraft.entity.LightningEntity;", "import net.minecraft.world.entity.LightningBolt;"],
  ["LightningEntity", "LightningBolt"],

  ["import net.minecraft.world.entity.monster.HostileEntity;", "import net.minecraft.world.entity.monster.Monster;"],
  ["HostileEntity", "Monster"],

  ["import net.minecraft.world.entity.monster.Warden;", "import net.minecraft.world.entity.monster.warden.Warden;"],
  ["import net.minecraft.world.entity.monster.Piglin;", "import net.minecraft.world.entity.monster.piglin.Piglin;"],
  ["import net.minecraft.world.entity.monster.PiglinBrute;", "import net.minecraft.world.entity.monster.piglin.PiglinBrute;"],

  ["PlayerEntity", "Player"],

  [".age %", ".tickCount %"],
  [".age ==", ".tickCount =="],
  [".age !=", ".tickCount !="],
  [".age <", ".tickCount <"],
  [".age >", ".tickCount >"],

  [".getHungerManager()", ".getFoodData()"],

  [".isIn(net.minecraft.tags.BlockTags.", ".is(net.minecraft.tags.BlockTags."],
  [".isIn(net.minecraft.tags.FluidTags.", ".is(net.minecraft.tags.FluidTags."],
  [".getType().isIn(net.minecraft.tags.EntityTypeTags.", ".getType().is(net.minecraft.tags.EntityTypeTags."],

  // sgui / player menu fields (broken ScreenHandler -> AbstractContainerMenu substring replace)
  ["AbstractContainerMenuType", "MenuType"],
  ["currentAbstractContainerMenu", "containerMenu"],
  ["playerAbstractContainerMenu", "inventoryMenu"],
  [".getCursorStack()", ".getCarried()"],
  [".setCursorStack(", ".setCarried("],
  [".sendContentUpdates()", ".broadcastChanges()"],

  // Container / inventory stacks (Mojmap)
  ["getInventory().getStack(", "getInventory().getItem("],
  ["getInventory().setStack(", "getInventory().setItem("],
];

const FILE_CLASS_FIXES = [
  ["AuctionScreenHandler.java", "public class AuctionAbstractContainerMenu ", "public class AuctionScreenHandler "],
  ["EnchantmentScreenHandlerMixin.java", "public class EnchantmentAbstractContainerMenuMixin", "public class EnchantmentScreenHandlerMixin"],
  ["GrindstoneScreenHandlerMixin.java", "public class GrindstoneAbstractContainerMenuMixin", "public class GrindstoneScreenHandlerMixin"],
  ["SmithingScreenHandlerMixin.java", "public class SmithingAbstractContainerMenuMixin", "public class SmithingScreenHandlerMixin"],
  ["MobEntityMixin.java", "public abstract class MobMixin", "public abstract class MobEntityMixin"],
  ["ServerWorldMixin.java", "public abstract class ServerLevelMixin", "public abstract class ServerWorldMixin"],
  ["WardenEntityMixin.java", "public abstract class WardenMixin", "public abstract class WardenEntityMixin"],
];

/** Yarn Inventory (container) -> Mojmap Container; avoid clobbering method names like openPlayerInventory */
const CONTAINER_TYPE_FILES = new Set([
  "RemoteChestAdminGui.java",
  "ContainerInventoryUtil.java",
  "ServerPlayerInteractionManagerMixin.java",
  "EnchantmentScreenHandlerMixin.java",
]);

function fixContainerTypes(content, filename) {
  if (!CONTAINER_TYPE_FILES.has(filename)) return content;
  let s = content;
  s = s.replace(/import net\.minecraft\.inventory\.Inventory;/g, "import net.minecraft.world.Container;");
  s = s.replace(/\bInventory\b/g, (m, off) => {
    const ctx = s.slice(Math.max(0, off - 12), off + 12);
    if (/openPlayer|drawPlayer|bindInv|PlayerInv/.test(ctx)) return m;
    return "Container";
  });
  return s;
}

/** Auction GUIs: player inv + container */
function fixAuctionInventoryTypes(content, filename) {
  if (filename !== "AuctionGui.java" && filename !== "AuctionScreenHandler.java") return content;
  let s = content;
  s = s.replace(/import net\.minecraft\.entity\.player\.PlayerInventory;/g, "import net.minecraft.world.entity.player.Inventory;");
  s = s.replace(/import net\.minecraft\.inventory\.Inventory;/g, "import net.minecraft.world.Container;");
  s = s.replace(/import net\.minecraft\.inventory\.SimpleInventory;/g, "import net.minecraft.world.SimpleContainer;");
  s = s.replace(/\bPlayerInventory\b/g, "Inventory");
  s = s.replace(/\bSimpleInventory\b/g, "SimpleContainer");
  s = s.replace(/\bInventory auctionInventory\b/g, "Container auctionInventory");
  s = s.replace(/\(Inventory auctionInventory\)/g, "(Container auctionInventory)");
  s = s.replace(/, Inventory auctionInventory\)/g, ", Container auctionInventory)");
  return s;
}

function walk(dir, out = []) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, out);
    else if (e.name.endsWith(".java")) out.push(p);
  }
  return out;
}

let n = 0;
for (const file of walk(ROOT)) {
  let s = fs.readFileSync(file, "utf8");
  const orig = s;
  const base = path.basename(file);

  for (const [fn, a, b] of FILE_CLASS_FIXES) {
    if (base === fn) s = s.split(a).join(b);
  }

  for (const [a, b] of REPLACEMENTS) s = s.split(a).join(b);

  s = fixContainerTypes(s, base);
  s = fixAuctionInventoryTypes(s, base);

  if (s !== orig) {
    fs.writeFileSync(file, s);
    n++;
  }
}
console.error("pass7 updated", n, "files");
