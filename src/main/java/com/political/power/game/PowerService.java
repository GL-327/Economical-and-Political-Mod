package com.political.power.game;

import com.mojang.serialization.JsonOps;
import com.political.SchoolTree;
import com.political.core.Clock;
import com.political.core.persist.StoreRegistry;
import com.political.core.text.Text;
import com.political.magic.MagicSchool;
import com.political.power.PowerSettings;
import com.political.power.cast.CastDecision;
import com.political.power.cast.CastPipeline;
import com.political.power.cast.EffectPlan;
import com.political.power.formula.BarrierRules;
import com.political.power.formula.Progression;
import com.political.power.formula.ResourcePool;
import com.political.power.formula.SchoolPassives;
import com.political.power.formula.Tiers;
import com.political.power.formula.UniqueHolders;
import com.political.power.formula.Vow;
import com.political.power.formula.VowSet;
import com.political.power.model.AbilityKind;
import com.political.power.model.AbilitySpec;
import com.political.power.model.PlayerPower;
import com.political.power.model.PowerCatalog;
import com.political.power.model.PowerData;
import com.political.power.store.PowerStore;
import com.political.stats.PlayerStatsManager;
import com.political.technique.Technique;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one entry point for everything the power system does to a player (M2). It owns the saved state
 * ({@link PowerStore}), the runtime state ({@link PowerRuntime}), the unlock rules, the cast flow with commit
 * and rollback, and the reactions to death, logout and dimension changes.
 *
 * <p>Rules live in the pure packages ({@code power.cast}, {@code power.formula}); this class reads the game and
 * applies the answers. Everything is called on the server thread.</p>
 */
public final class PowerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerService.class);

    private static PowerStore store;
    private static boolean registered;
    private static final PowerData FALLBACK = new PowerData();
    private static final Map<UUID, PowerRuntime> RUNTIME = new ConcurrentHashMap<>();

    private PowerService() {
    }

    // ── lifecycle ───────────────────────────────────────────────────────────────────────────

    /** Opens power.json for this world, importing the old technique and magic files the first time. */
    public static void initialize(Path worldRoot) {
        store = PowerStore.open(worldRoot);
        if (!registered) {
            store.register(StoreRegistry.global());
            registered = true;
        }
        LOGGER.info("Power state loaded: {} players", store.data().players.size());
    }

    private static PowerData data() {
        return store == null ? FALLBACK : store.data();
    }

    private static void dirty() {
        if (store != null) {
            store.markDirty();
        }
    }

    public static PlayerPower state(String uuid) {
        return data().player(uuid);
    }

    static PowerRuntime runtime(UUID uuid) {
        return RUNTIME.computeIfAbsent(uuid, k -> new PowerRuntime());
    }

    /** Domain manager and runtime are cleared; for a world reload. */
    public static void shutdown() {
        RUNTIME.clear();
        DomainManager.clear();
    }

    // ── school, tier, level ─────────────────────────────────────────────────────────────────

    public static MagicSchool school(String uuid) {
        String name = state(uuid).school;
        if (name == null) {
            return null;
        }
        try {
            return MagicSchool.valueOf(name);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Player {} has an unknown school {} in the power data; treating it as none", uuid, name);
            return null;
        }
    }

    private static int highestNode(PlayerPower p, MagicSchool school) {
        return school == null ? 0 : SchoolPassives.highestSpineNode(school, p.unlockedNodes);
    }

    /** The tier (0 to 4) of the player's chosen school. */
    public static int tier(String uuid) {
        PlayerPower p = state(uuid);
        return Tiers.tier(highestNode(p, school(uuid)));
    }

    public static int level(String uuid) {
        return Math.max(Progression.levelFor(state(uuid).shardsEarnedTotal), state(uuid).level);
    }

    // ── the resource ────────────────────────────────────────────────────────────────────────

    public static double maxResource(String uuid) {
        PlayerPower p = state(uuid);
        MagicSchool school = school(uuid);
        double bonus = PlayerStatsManager.getBonusMana(uuid)
                + (school == null ? 0 : SchoolPassives.resourceBonus(school, p.unlockedNodes))
                + vowsOf(uuid).resourceBonus();
        return ResourcePool.max(tier(uuid), bonus);
    }

    public static double resource(String uuid) {
        PlayerPower p = state(uuid);
        if (p.resource < 0) {
            p.resource = maxResource(uuid);
        }
        return p.resource;
    }

    public static void setResource(String uuid, double value) {
        PlayerPower p = state(uuid);
        p.resource = Math.max(0.0, Math.min(value, maxResource(uuid)));
        dirty();
    }

    // ── vows ────────────────────────────────────────────────────────────────────────────────

    static VowSet vowsOf(String uuid) {
        return runtime(UUID.fromString(uuid)).vows;
    }

    private static Vow.Loadout loadout(ServerPlayer player) {
        boolean armorEmpty = player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                && player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
                && player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()
                && player.getItemBySlot(EquipmentSlot.FEET).isEmpty();
        return new Vow.Loadout(armorEmpty, player.getOffhandItem().isEmpty(), player.isSprinting());
    }

    /** Tries to take a vow. Returns the result so the screen can say why not. */
    public static VowSet.AcceptResult acceptVow(ServerPlayer player, Vow vow) {
        String uuid = player.getUUID().toString();
        VowSet.AcceptResult result = vowsOf(uuid).accept(vow, loadout(player), Clock.ticks());
        if (result == VowSet.AcceptResult.ACCEPTED) {
            syncVows(uuid);
        }
        return result;
    }

    /** Checks the player's vows now (timer, equipment change). Broken vows are announced and their bonus is gone. */
    public static void checkVows(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PowerRuntime rt = runtime(player.getUUID());
        List<Vow> broken = rt.vows.check(loadout(player), Clock.ticks());
        if (broken.isEmpty()) {
            return;
        }
        for (Vow vow : broken) {
            player.sendSystemMessage(Text.tr("political.power.vow.broken", Text.trNamed("political.power.vow", vow.name())), false);
        }
        PlayerPower p = state(uuid);
        p.resource = ResourcePool.clampToMax(p.resource, maxResource(uuid));
        syncVows(uuid);
    }

    private static void syncVows(String uuid) {
        PlayerPower p = state(uuid);
        PowerRuntime rt = runtime(UUID.fromString(uuid));
        p.vows.clear();
        for (Vow v : rt.vows.active()) {
            p.vows.add(v.name());
        }
        p.vowWaitTicks = rt.vows.reacceptRemaining(Clock.ticks());
        dirty();
    }

    // ── techniques and abilities ────────────────────────────────────────────────────────────

    private static boolean granted(PlayerPower p, MagicSchool school, PowerCatalog.TechniqueDef def) {
        if (school == null) {
            return false;
        }
        int node = highestNode(p, school);
        if (PowerCatalog.DOMAIN.equals(def.id())) {
            return Tiers.capstone(node);
        }
        return def.schoolId() != null && def.schoolId().equals(school.name()) && node >= def.unlockNode();
    }

    /** Whether the player holds the technique: learned, granted by their school's tree, or given by an admin. */
    public static boolean holds(String uuid, String techniqueId) {
        Optional<PowerCatalog.TechniqueDef> def = PowerCatalog.technique(techniqueId);
        if (def.isEmpty()) {
            return false;
        }
        PlayerPower p = state(uuid);
        boolean has = p.learned.contains(techniqueId) || granted(p, school(uuid), def.get());
        if (!has) {
            return false;
        }
        return !UniqueHolders.isUnique(techniqueId) || uuid.equals(data().uniqueHolders.get(techniqueId));
    }

    /** The techniques the player holds, in catalog order. */
    public static List<PowerCatalog.TechniqueDef> heldTechniques(String uuid) {
        List<PowerCatalog.TechniqueDef> out = new ArrayList<>();
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            if (holds(uuid, def.id())) {
                out.add(def);
            }
        }
        return out;
    }

    /** Mastery rank of the technique the player has equipped (Novice if none). */
    public static Technique.MasteryRank equippedRank(String uuid) {
        String equipped = state(uuid).equipped;
        return equipped == null ? Technique.MasteryRank.NOVICE : rank(uuid, equipped);
    }

    /** Mastery rank (Novice to Master) of an older technique, from how often it was cast. */
    public static Technique.MasteryRank rank(String uuid, String techniqueId) {
        return Technique.MasteryRank.forUses(state(uuid).uses.getOrDefault(techniqueId, 0));
    }

    public static boolean unlocked(String uuid, AbilitySpec spec) {
        if (!holds(uuid, spec.techniqueId())) {
            return false;
        }
        if (spec.kind() == AbilityKind.LEGACY) {
            return rank(uuid, spec.techniqueId()).ordinal() >= PowerCatalog.rankIndexOf(spec);
        }
        return true;
    }

    /** The selected ability of the equipped technique, or null. */
    public static AbilitySpec selected(String uuid) {
        PlayerPower p = state(uuid);
        if (p.equipped == null) {
            return null;
        }
        List<AbilitySpec> abilities = PowerCatalog.abilities(p.equipped);
        if (abilities.isEmpty()) {
            return null;
        }
        for (AbilitySpec spec : abilities) {
            if (spec.id().equals(p.selectedAbility)) {
                return spec;
            }
        }
        return abilities.get(0);
    }

    public enum LearnResult { OK, UNKNOWN, ALREADY, TAKEN }

    /** Learn from a manual or receive from an admin. Equips it when the player has nothing equipped. */
    public static LearnResult learn(ServerPlayer player, String techniqueId) {
        if (!PowerCatalog.isTechnique(techniqueId)) {
            return LearnResult.UNKNOWN;
        }
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        if (p.learned.contains(techniqueId)) {
            return LearnResult.ALREADY;
        }
        if (UniqueHolders.claim(data().uniqueHolders, techniqueId, uuid) == UniqueHolders.Claim.TAKEN) {
            return LearnResult.TAKEN;
        }
        p.learned.add(techniqueId);
        p.uses.putIfAbsent(techniqueId, 0);
        if (p.equipped == null) {
            equipQuietly(p, techniqueId);
        }
        dirty();
        return LearnResult.OK;
    }

    /** Takes a technique away from a player, as an admin; frees a unique technique. */
    public static boolean revoke(String uuid, String techniqueId) {
        PlayerPower p = state(uuid);
        boolean had = p.learned.remove(techniqueId);
        UniqueHolders.release(data().uniqueHolders, techniqueId, uuid);
        if (techniqueId.equals(p.equipped) && !holds(uuid, techniqueId)) {
            p.equipped = null;
            p.selectedAbility = null;
        }
        dirty();
        return had;
    }

    private static void equipQuietly(PlayerPower p, String techniqueId) {
        p.equipped = techniqueId;
        p.selectedAbility = PowerCatalog.firstAbility(techniqueId).map(AbilitySpec::id).orElse(null);
    }

    public static boolean equip(ServerPlayer player, String techniqueId) {
        String uuid = player.getUUID().toString();
        if (!holds(uuid, techniqueId)) {
            return false;
        }
        equipQuietly(state(uuid), techniqueId);
        dirty();
        return true;
    }

    public static void unequip(String uuid) {
        PlayerPower p = state(uuid);
        p.equipped = null;
        p.selectedAbility = null;
        dirty();
    }

    public static boolean selectAbility(String uuid, String abilityId) {
        PlayerPower p = state(uuid);
        if (p.equipped == null) {
            return false;
        }
        for (AbilitySpec spec : PowerCatalog.abilities(p.equipped)) {
            if (spec.id().equals(abilityId) && unlocked(uuid, spec)) {
                p.selectedAbility = abilityId;
                dirty();
                return true;
            }
        }
        return false;
    }

    /** Learning a technique from a manual (or an admin grant) with the messages a player expects. */
    public static boolean learnFromManual(ServerPlayer player, String techniqueId) {
        LearnResult result = learn(player, techniqueId);
        switch (result) {
            case OK -> player.sendSystemMessage(Text.tr("political.power.learned", techniqueName(techniqueId)), false);
            case ALREADY -> player.sendSystemMessage(Text.tr("political.power.already_known", techniqueName(techniqueId)), false);
            case TAKEN -> player.sendSystemMessage(Text.tr("political.power.technique.taken", techniqueName(techniqueId)), false);
            case UNKNOWN -> player.sendSystemMessage(Text.tr("political.power.equip.failed"), false);
        }
        return result == LearnResult.OK;
    }

    /** The display name of a technique: the old ones keep their names and colors, the new ones come from the language file. */
    public static Component techniqueName(String techniqueId) {
        Technique older = Technique.byName(techniqueId);
        if (older != null) {
            return Text.plain(older.displayName).withStyle(older.color);
        }
        return Text.trNamed("political.power.technique", techniqueId);
    }

    /** Chat summary of the player's techniques, for /technique ranks. */
    public static void showTechniques(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        List<PowerCatalog.TechniqueDef> held = heldTechniques(uuid);
        player.sendSystemMessage(Text.tr("political.power.techniques.title"), false);
        if (held.isEmpty()) {
            player.sendSystemMessage(Text.tr("political.power.techniques.none"), false);
            return;
        }
        for (PowerCatalog.TechniqueDef def : held) {
            MutableComponent line = Text.plain("  ").append(techniqueName(def.id()));
            if (def.legacy()) {
                int uses = p.uses.getOrDefault(def.id(), 0);
                line.append(Text.plain(" — " + rank(uuid, def.id()).display + " (" + uses + " uses)"));
            }
            if (def.id().equals(p.equipped)) {
                line.append(Text.plain(" ")).append(Text.tr("political.power.techniques.equipped"));
            }
            player.sendSystemMessage(line, false);
        }
    }

    /**
     * Admin reset: the player starts over (no school, nodes, techniques, shards, vows or resource). The old state is
     * first written to the quarantine as text under {@code <uuid>.reset.<time>}, so an admin's mistake can be undone
     * by hand from power.json; unique techniques the player held are freed. Throws, changing nothing, if the old
     * state cannot be written down.
     */
    public static void reset(String uuid) {
        PowerData d = data();
        PlayerPower old = d.players.get(uuid);
        if (old != null) {
            String archived = PlayerPower.CODEC.encodeStart(JsonOps.INSTANCE, old)
                    .getOrThrow(message -> new IllegalStateException("cannot archive the power state of " + uuid + ": " + message))
                    .toString();
            d.quarantine.put(uuid + ".reset." + Clock.wallMillis(), archived);
            d.players.remove(uuid);
        }
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            UniqueHolders.release(d.uniqueHolders, def.id(), uuid);
        }
        UUID id = UUID.fromString(uuid);
        PowerRuntime rt = RUNTIME.get(id);
        if (rt != null) {
            rt.vows = new VowSet();
            rt.clearCombatState();
        }
        DomainManager.endEarly(id, Clock.ticks());
        dirty();
        LOGGER.info("Power state of {} was reset; the old state is in the quarantine of power.json", uuid);
    }

    /** How many archived states the quarantine holds (resets and old data that did not fit). */
    public static int quarantineSize() {
        return data().quarantine.size();
    }

    /** Unique technique id to the UUID of its holder, for the admin screen. */
    public static Map<String, String> uniqueHolders() {
        return Map.copyOf(data().uniqueHolders);
    }

    /** Writes the power data now if it changed (shutdown). */
    public static void saveNow() {
        if (store != null) {
            store.saveIfDirty();
        }
    }

    // ── schools and the tree ────────────────────────────────────────────────────────────────

    public enum SchoolResult { OK, DORMANT, ALREADY_CHOSEN }

    /** The first choice is free; changing school afterwards is an admin action ({@link #setSchool}). */
    public static SchoolResult chooseSchool(ServerPlayer player, MagicSchool school) {
        if (!PowerCatalog.selectableSchools().contains(school.name())) {
            return SchoolResult.DORMANT;
        }
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        if (p.school != null) {
            return SchoolResult.ALREADY_CHOSEN;
        }
        setSchool(player, school);
        return SchoolResult.OK;
    }

    /** Sets the school without the first-choice rule. Used by admins and by the first choice. */
    public static void setSchool(ServerPlayer player, MagicSchool school) {
        PlayerPower p = state(player.getUUID().toString());
        p.school = school == null ? null : school.name();
        syncGrants(player);
        dirty();
    }

    public enum NodeResult { OK, UNKNOWN, NO_SCHOOL, WRONG_SCHOOL, ALREADY, PARENT_LOCKED, LEVEL_TOO_LOW, NOT_ENOUGH_SHARDS }

    public static NodeResult unlockNode(ServerPlayer player, String nodeId) {
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        SchoolTree.SchoolNode node = SchoolTree.getNode(nodeId);
        if (node == null) {
            return NodeResult.UNKNOWN;
        }
        if (p.school == null) {
            return NodeResult.NO_SCHOOL;
        }
        if (!node.school().name().equals(p.school)) {
            return NodeResult.WRONG_SCHOOL;
        }
        if (p.unlockedNodes.contains(nodeId)) {
            return NodeResult.ALREADY;
        }
        if (node.parentId() != null && !p.unlockedNodes.contains(node.parentId())) {
            return NodeResult.PARENT_LOCKED;
        }
        if (level(uuid) < node.levelRequired()) {
            return NodeResult.LEVEL_TOO_LOW;
        }
        if (p.shards < node.shardCost()) {
            return NodeResult.NOT_ENOUGH_SHARDS;
        }
        p.shards -= node.shardCost();
        p.unlockedNodes.add(nodeId);
        p.level = Math.max(p.level, level(uuid));
        syncGrants(player);
        dirty();
        return NodeResult.OK;
    }

    /** Arcane Shards from expeditions. The amount and the source are unchanged from before M2. */
    public static void addShards(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }
        PlayerPower p = state(player.getUUID().toString());
        p.shards += amount;
        p.shardsEarnedTotal += amount;
        p.level = Math.max(p.level, Progression.levelFor(p.shardsEarnedTotal));
        dirty();
        player.sendSystemMessage(Text.tr("political.power.shards.gained", amount), false);
    }

    /**
     * Claims the unique techniques the tree now grants and tells the player about techniques they gained or could
     * not get because someone else holds them.
     */
    static void syncGrants(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        MagicSchool school = school(uuid);
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            if (!granted(p, school, def) || !UniqueHolders.isUnique(def.id())) {
                continue;
            }
            if (UniqueHolders.claim(data().uniqueHolders, def.id(), uuid) == UniqueHolders.Claim.TAKEN) {
                player.sendSystemMessage(Text.tr("political.power.technique.taken", techniqueName(def.id())), false);
            }
        }
        if (p.equipped == null) {
            for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
                if (holds(uuid, def.id())) {
                    equipQuietly(p, def.id());
                    break;
                }
            }
        }
    }

    // ── casting ─────────────────────────────────────────────────────────────────────────────

    /** Sneak plus the swap-hands key (and `/technique cast`) arrive here. */
    public static void cast(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        PowerRuntime rt = runtime(player.getUUID());
        long now = Clock.ticks();
        resource(uuid); // first sight: fill the pool

        GameFacts facts = new GameFacts(player, rt, now);
        CastDecision decision = CastPipeline.validate(facts);
        if (decision instanceof CastDecision.Rejected rejected) {
            showFailure(player, rejected);
            return;
        }
        EffectPlan plan = ((CastDecision.Approved) decision).plan();
        AbilitySpec ability = plan.ability();

        // Commit: cost, cooldown and bookkeeping, remembering the old values so a failure can put them back.
        double oldResource = p.resource;
        long oldSpend = rt.lastSpendTick;
        long oldLastCast = rt.lastCastTick;
        Long oldReady = rt.readyAt.get(ability.id());
        int oldUses = p.uses.getOrDefault(ability.techniqueId(), 0);
        int oldRecent = rt.recentCasts.size();

        p.resource = Math.max(0.0, p.resource - plan.cost());
        if (plan.cost() > 0) {
            rt.lastSpendTick = now;
        }
        if (ability.kind() != AbilityKind.DOMAIN) {
            rt.readyAt.put(ability.id(), now + plan.cooldownTicks());
        }
        rt.lastCastTick = now;
        rt.recentCasts.addLast(now);
        p.uses.merge(ability.techniqueId(), 1, Integer::sum);

        try {
            PowerEffects.execute(player, plan, facts, now);
        } catch (Exception e) {
            p.resource = oldResource;
            rt.lastSpendTick = oldSpend;
            rt.lastCastTick = oldLastCast;
            if (oldReady == null) {
                rt.readyAt.remove(ability.id());
            } else {
                rt.readyAt.put(ability.id(), oldReady);
            }
            p.uses.put(ability.techniqueId(), oldUses);
            while (rt.recentCasts.size() > oldRecent) {
                rt.recentCasts.pollLast();
            }
            if (ability.kind() == AbilityKind.DOMAIN) {
                DomainManager.cancel(player.getUUID());
            }
            LOGGER.error("Cast of {} ({}) failed for player {}; cost and cooldown were put back",
                    ability.id(), ability.techniqueId(), player.getUUID(), e);
            player.sendSystemMessage(Text.tr("political.power.fail.execution_failed"), true);
            return;
        }
        dirty();
        PowerEffects.announce(player, plan);
    }

    private static void showFailure(ServerPlayer player, CastDecision.Rejected rejected) {
        Component message = switch (rejected.reason()) {
            case ON_COOLDOWN -> Text.tr(rejected.reason().messageKey(), String.format("%.1f", rejected.detail() / 20.0));
            case INSUFFICIENT_RESOURCE -> Text.tr(rejected.reason().messageKey(), rejected.detail());
            default -> Text.tr(rejected.reason().messageKey());
        };
        player.sendSystemMessage(message, true);
    }

    // ── barrier and damage ──────────────────────────────────────────────────────────────────

    /** Raises the Barrier for the given number of ticks from now. */
    static void raiseBarrier(ServerPlayer player, long ticks) {
        runtime(player.getUUID()).barrierUntilTick = Clock.ticks() + ticks;
    }

    /** Whether the player's Barrier absorbs this damage right now. */
    public static boolean absorbsDamage(ServerPlayer player, DamageSource source) {
        PowerRuntime rt = runtime(player.getUUID());
        if (Clock.ticks() >= rt.barrierUntilTick) {
            return false;
        }
        BarrierRules.DamageClass kind;
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            kind = BarrierRules.DamageClass.VOID;
        } else if (source.is(DamageTypes.GENERIC_KILL)) {
            kind = BarrierRules.DamageClass.UNBLOCKABLE;
        } else if (source.is(DamageTypeTags.IS_FALL)) {
            kind = BarrierRules.DamageClass.FALL;
        } else {
            kind = BarrierRules.DamageClass.NORMAL;
        }
        return BarrierRules.blocks(kind);
    }

    // ── events ──────────────────────────────────────────────────────────────────────────────

    public static void onJoin(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        PowerRuntime rt = runtime(player.getUUID());
        long now = Clock.ticks();
        Set<Vow> loaded = new LinkedHashSet<>();
        for (String name : p.vows) {
            try {
                loaded.add(Vow.valueOf(name));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Player {} has an unknown vow {} in the power data; ignoring it", uuid, name);
            }
        }
        rt.vows = new VowSet(loaded);
        rt.vows.restoreWait(now, p.vowWaitTicks);
        rt.lastRegenTick = now;
        rt.lastVowCheckTick = now;
        resource(uuid);
        p.level = Math.max(p.level, Progression.levelFor(p.shardsEarnedTotal));
        syncGrants(player);
    }

    public static void onLogout(ServerPlayer player) {
        syncVows(player.getUUID().toString());
        DomainManager.endEarly(player.getUUID(), Clock.ticks());
        RUNTIME.remove(player.getUUID());
    }

    /** Death halves the resource, clears vows, cooldowns and the Barrier, and ends the player's Domain. */
    public static void onDeath(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        PowerRuntime rt = runtime(player.getUUID());
        rt.vows.clearOnDeath();
        rt.clearCombatState();
        syncVows(uuid);
        DomainManager.endEarly(player.getUUID(), Clock.ticks());
        p.resource = ResourcePool.afterDeath(resource(uuid));
        dirty();
    }

    public static void onDimensionChange(ServerPlayer player) {
        DomainManager.endEarly(player.getUUID(), Clock.ticks());
    }

    /** Once per second per player: regeneration and the vow check. */
    public static void tickPlayer(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerPower p = state(uuid);
        PowerRuntime rt = runtime(player.getUUID());
        long now = Clock.ticks();
        double max = maxResource(uuid);
        double current = resource(uuid);
        long elapsed = rt.lastRegenTick == 0 ? 20 : Math.max(0, now - rt.lastRegenTick);
        rt.lastRegenTick = now;
        long sinceSpend = rt.lastSpendTick == PowerRuntime.NEVER ? Long.MAX_VALUE / 4 : now - rt.lastSpendTick;
        double next = ResourcePool.regen(current, max, sinceSpend, elapsed, DomainManager.pausesRegen(player.getUUID(), now));
        if (next != p.resource) {
            p.resource = next;
            dirty();
        }
        if (now - rt.lastVowCheckTick >= PowerSettings.VOW_CHECK_INTERVAL_TICKS.get()) {
            rt.lastVowCheckTick = now;
            checkVows(player);
        }
    }

    public static void tickDomains(MinecraftServer server) {
        DomainManager.tick(server, Clock.ticks());
    }

    /** Equipment changed: vows that depend on armor or the off hand are checked at once. */
    public static void onEquipmentChange(ServerPlayer player) {
        PowerRuntime rt = RUNTIME.get(player.getUUID());
        if (rt != null && !rt.vows.active().isEmpty()) {
            checkVows(player);
        }
    }

    // ── helpers for the screens ─────────────────────────────────────────────────────────────

    public static long cooldownRemaining(String uuid, AbilitySpec spec) {
        long now = Clock.ticks();
        if (spec.kind() == AbilityKind.DOMAIN) {
            return DomainManager.cooldownRemaining(UUID.fromString(uuid), now);
        }
        return runtime(UUID.fromString(uuid)).cooldownRemaining(spec.id(), now);
    }

    /** Share of every cooldown removed by vows and the school's tree. */
    static double cooldownReduction(String uuid) {
        MagicSchool school = school(uuid);
        double tree = school == null ? 0.0 : SchoolPassives.cooldownReduction(school, state(uuid).unlockedNodes);
        return tree + vowsOf(uuid).cooldownReduction();
    }

    /** Damage multiplier for techniques of the given technique: the school's tree bonus and the no-sprint vow. */
    static double techniqueDamageMultiplier(String uuid, String techniqueId) {
        double multiplier = 1.0 + vowsOf(uuid).damageBonus();
        MagicSchool school = school(uuid);
        Optional<PowerCatalog.TechniqueDef> def = PowerCatalog.technique(techniqueId);
        if (school != null && def.isPresent() && school.name().equals(def.get().schoolId())) {
            multiplier += SchoolPassives.damageBonus(school, state(uuid).unlockedNodes);
        }
        return multiplier;
    }

    /** Ticks since the player's last cast (for Black Flash). */
    public static long ticksSinceLastCast(ServerPlayer player) {
        PowerRuntime rt = RUNTIME.get(player.getUUID());
        return rt == null ? Long.MAX_VALUE : rt.ticksSinceLastCast(Clock.ticks());
    }
}
