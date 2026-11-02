package com.political;


import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import com.political.magic.MagicManager;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Summons, tracks and levels companions. One companion is active per player at a time.
 *
 * <p>A companion is a real entity in the world, tagged so it can be found again after a restart.
 * It is removed cleanly when dismissed, when the owner logs out and when the server stops, so
 * nothing is left wandering.</p>
 */
public final class CompanionManager {

    private CompanionManager() {}

    public static final String TAG = "political_companion";
    private static final int MAX_LEVEL = 30;
    /** Experience needed for each level, kept flat so levelling cannot spiral. */
    private static final int XP_PER_LEVEL = 120;
    /** Daily cap on companion experience, which is what stops AFK farms feeding it. */
    private static final int DAILY_XP_CAP = 900;

    public static final class Profile {
        public Set<String> owned = new LinkedHashSet<>();
        public String active = null;
        public Map<String, Integer> xp = new HashMap<>();
        public Map<String, String> nicknames = new HashMap<>();
        public int xpToday = 0;
        public long dayStamp = 0L;
    }

    private static final Map<String, Profile> DATA = new HashMap<>();
    /** owner uuid -> live companion entity uuid. */
    private static final Map<String, UUID> LIVE = new HashMap<>();

    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("companions.json");
        if (!Files.exists(file)) return;
        try (Reader r = Files.newBufferedReader(file)) {
            java.lang.reflect.Type t = new TypeToken<Map<String, Profile>>() {}.getType();
            Map<String, Profile> loaded = GSON.fromJson(r, t);
            if (loaded != null) {
                DATA.clear();
                DATA.putAll(loaded);
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to load companions.json", e);
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(DATA, w);
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to save companions.json", e);
        }
    }

    public static Profile profile(String uuid) {
        return DATA.computeIfAbsent(uuid, k -> new Profile());
    }

    // ── Ownership ───────────────────────────────────────────────────────────

    public static boolean owns(String uuid, Companion c) {
        return profile(uuid).owned.contains(c.name());
    }

    public static int levelOf(String uuid, Companion c) {
        int xp = profile(uuid).xp.getOrDefault(c.name(), 0);
        return Math.min(MAX_LEVEL, xp / XP_PER_LEVEL);
    }

    public static int xpInLevel(String uuid, Companion c) {
        return profile(uuid).xp.getOrDefault(c.name(), 0) % XP_PER_LEVEL;
    }

    public static int xpPerLevel() {
        return XP_PER_LEVEL;
    }

    public static int maxLevel() {
        return MAX_LEVEL;
    }

    public static String nameOf(String uuid, Companion c) {
        String nick = profile(uuid).nicknames.get(c.name());
        return nick == null || nick.isBlank() ? c.display : nick;
    }

    public static boolean grant(ServerPlayer player, Companion c) {
        Profile p = profile(player.getUUID().toString());
        if (!p.owned.add(c.name())) return false;
        save();
        player.sendSystemMessage(Component.literal("✦ A companion joins you: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD)));
        if (player.level() instanceof ServerLevel level) {
            VfxPlus.summoningCircle(level, player.position(), Vfx.ARCANE);
        }
        return true;
    }

    // ── Summoning ───────────────────────────────────────────────────────────

    public static Companion activeOf(String uuid) {
        return Companion.byId(profile(uuid).active);
    }

    public static Entity liveEntity(MinecraftServer server, String ownerUuid) {
        UUID id = LIVE.get(ownerUuid);
        if (id == null || server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            Entity e = level.getEntity(id);
            if (e != null && e.isAlive()) return e;
        }
        return null;
    }

    /** Summons the given companion, replacing whatever was out. */
    public static boolean summon(ServerPlayer player, Companion c) {
        String uuid = player.getUUID().toString();
        if (!owns(uuid, c)) {
            player.sendSystemMessage(Component.literal("§cYou do not have that companion."));
            return false;
        }
        if (!(player.level() instanceof ServerLevel level)) return false;

        dismiss(player);

        Entity spawned = c.base.spawn(level, player.blockPosition(), EntitySpawnReason.EVENT);
        if (!(spawned instanceof Mob mob)) {
            if (spawned != null) spawned.discard();
            player.sendSystemMessage(Component.literal("§cThat companion could not be summoned here."));
            return false;
        }

        int level0 = levelOf(uuid, c);
        applyStats(mob, c, level0, uuid);
        mob.addTag(TAG);
        mob.addTag(TAG + "_" + uuid);
        mob.setPersistenceRequired();
        mob.setCustomName(Component.literal(nameOf(uuid, c) + " §7[" + level0 + "]")
                .withStyle(c.colour));
        mob.setCustomNameVisible(true);

        LIVE.put(uuid, mob.getUUID());
        profile(uuid).active = c.name();
        save();

        VfxPlus.summoningCircle(level, mob.position(), Vfx.ARCANE);
        player.sendSystemMessage(Component.literal("§7" + nameOf(uuid, c) + " is at your side."));
        return true;
    }

    private static void applyStats(Mob mob, Companion c, int level, String ownerUuid) {
        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(c.healthAt(level));
            mob.setHealth((float) c.healthAt(level));
        }
        var dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(c.damageAt(level));
        // Companions never wander off to fight the world on their own.
        mob.setPersistenceRequired();
    }

    /** Removes the live companion, if any. */
    public static void dismiss(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        Entity e = liveEntity(server, uuid);
        if (e != null) e.discard();
        LIVE.remove(uuid);
    }

    /** Called when a player leaves so nothing is left behind. */
    public static void onLogout(ServerPlayer player) {
        try {
            dismiss(player);
        } catch (Exception ignored) {
            LIVE.remove(player.getUUID().toString());
        }
    }

    /** Clears every live companion, used on shutdown. */
    public static void shutdown(MinecraftServer server) {
        if (server == null) return;
        for (UUID id : new ArrayList<>(LIVE.values())) {
            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(id);
                if (e != null) e.discard();
            }
        }
        LIVE.clear();
        save();
    }

    // ── Ticking ─────────────────────────────────────────────────────────────

    private static int tickCounter = 0;

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter < 20) return;
        tickCounter = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();
            Companion c = activeOf(uuid);
            if (c == null) continue;
            Entity e = liveEntity(server, uuid);
            if (e == null) continue;

            // Keep the companion near its owner rather than letting it get lost.
            if (e.distanceToSqr(player) > 32 * 32) {
                e.teleportTo(player.getX(), player.getY(), player.getZ());
            }

            if (e instanceof Mob mob && player.level() instanceof ServerLevel level) {
                signature(player, mob, c, level);
            }
        }
    }

    /** The companion's signature ability, on a slow cadence so it reads as help. */
    private static void signature(ServerPlayer owner, Mob mob, Companion c, ServerLevel level) {
        if (owner.tickCount % 100 != 0) return;
        int lvl = levelOf(owner.getUUID().toString(), c);

        switch (c.role) {
            case SUPPORT -> {
                switch (c) {
                    case MENDING_ALLAY -> {
                        if (owner.getHealth() < owner.getMaxHealth()) {
                            owner.heal(1.0f + lvl * 0.05f);
                            Vfx.bloom(level, owner.position().add(0, 1, 0));
                        }
                    }
                    case LANTERN_WISP -> owner.addEffect(
                            new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false));
                    case ARCANE_FAMILIAR -> MagicManager.setMana(owner.getUUID().toString(),
                            MagicManager.getMana(owner.getUUID().toString()) + 5 + lvl);
                    case CHOIR_PARROT -> owner.addEffect(
                            new MobEffectInstance(MobEffects.STRENGTH, 120, 0, true, false, false));
                    default -> { }
                }
            }
            case TANK -> {
                // Pulls a nearby hostile onto itself so the owner gets a breather.
                for (LivingEntity hostile : level.getEntitiesOfClass(LivingEntity.class,
                        owner.getBoundingBox().inflate(8), ent -> ent instanceof Monster && ent.isAlive())) {
                    mob.setTarget(hostile);
                    if (hostile instanceof Mob hm) hm.setTarget(mob);
                    break;
                }
            }
            case DAMAGE -> {
                for (LivingEntity hostile : level.getEntitiesOfClass(LivingEntity.class,
                        owner.getBoundingBox().inflate(10), ent -> ent instanceof Monster && ent.isAlive())) {
                    mob.setTarget(hostile);
                    break;
                }
            }
            case FORAGER -> {
                if (c == Companion.PROSPECT_FOX && owner.getRandom().nextInt(4) == 0) {
                    CoinManager.giveCoinsQuiet(owner, 5L + lvl);
                }
            }
            case SCOUT -> {
                if (c == Companion.CURSE_MOTH) {
                    owner.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false, false));
                }
            }
        }
    }

    // ── Experience ──────────────────────────────────────────────────────────

    /** Awards companion experience, respecting the daily cap. */
    public static void awardXp(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        Profile p = DATA.get(uuid);
        if (p == null || p.active == null) return;
        Companion c = Companion.byId(p.active);
        if (c == null) return;

        long today = System.currentTimeMillis() / 86_400_000L;
        if (p.dayStamp != today) {
            p.dayStamp = today;
            p.xpToday = 0;
        }
        if (p.xpToday >= DAILY_XP_CAP) return;

        int give = Math.min(amount, DAILY_XP_CAP - p.xpToday);
        p.xpToday += give;

        int before = levelOf(uuid, c);
        p.xp.merge(c.name(), give, Integer::sum);
        int after = levelOf(uuid, c);

        if (after > before) {
            player.sendSystemMessage(Component.literal("§7" + nameOf(uuid, c) + " reached level §f" + after));
            if (player.level() instanceof ServerLevel level) {
                Vfx.bloom(level, player.position().add(0, 1, 0));
            }
            // Refresh the live companion so its stats match the new level.
            Entity e = liveEntity(((ServerLevel) player.level()).getServer(), uuid);
            if (e instanceof Mob mob) {
                applyStats(mob, c, after, uuid);
                mob.setCustomName(Component.literal(nameOf(uuid, c) + " §7[" + after + "]")
                        .withStyle(c.colour));
            }
        }
        save();
    }

    /** Hooked to mob kills. */
    public static void onKill(ServerPlayer player) {
        awardXp(player, 6);
    }

    // ── Items ───────────────────────────────────────────────────────────────

    public static ItemStack createEgg(Companion c) {
        ItemStack s = new ItemStack(c.icon);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ " + c.display).withStyle(c.colour, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8" + c.rarity.label + " " + c.role.label + " Companion"));
        lore.add(Component.literal("§7" + c.flavor));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Health: §f" + (int) c.healthAt(0)
                + "  §7Damage: §f" + String.format(java.util.Locale.ROOT, "%.1f", c.damageAt(0))));
        lore.add(Component.literal("§8Right click to take it in."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, c.id());
        return s;
    }

    /** Right clicking a companion item claims it. */
    public static boolean tryClaim(ServerPlayer player, ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null || !id.startsWith("companion_")) return false;
        Companion c = Companion.byId(id);
        if (c == null) return false;
        if (grant(player, c)) {
            stack.shrink(1);
        } else {
            player.sendSystemMessage(Component.literal("§7That companion already walks with you."));
        }
        return true;
    }

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        for (Companion c : Companion.values()) {
            // Only the commoner species are craftable, the rest are found.
            if (c.rarity != Companion.Rarity.COMMON && c.rarity != Companion.Rarity.UNCOMMON) continue;
            ItemStack[] grid = new ItemStack[9];
            for (int i = 0; i < 9; i++) grid[i] = ItemStack.EMPTY;
            grid[4] = new ItemStack(c.icon);
            grid[1] = new ItemStack(Items.GOLDEN_APPLE, 4);
            grid[3] = new ItemStack(Items.WHEAT, 32);
            grid[5] = new ItemStack(Items.BONE, 32);
            grid[7] = new ItemStack(Items.LEAD, 2);
            boolean[] locked = new boolean[9];
            for (int i = 0; i < 9; i++) locked[i] = !grid[i].isEmpty();
            out.add(new SlayerRecipes.Recipe("VX Companion: " + c.display,
                    createEgg(c), grid, locked, null, 0));
        }
        if (!out.isEmpty()) RecipeConfigManager.upsertCustomRecipesSilent(out);
    }
}
