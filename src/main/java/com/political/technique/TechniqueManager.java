package com.political.technique;

import com.political.core.text.Text;
import com.political.power.game.PowerService;
import com.political.power.model.PowerCatalog;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * The old entry points of the technique system, kept so the rest of the mod did not have to change in one go
 * (strangler step, M2.7). Everything now lives in {@code com.political.power}: the state is {@code power.json},
 * casting runs through the shared validation pipeline, and the effects of the 32 older abilities are in
 * {@code LegacyEffects}. Nothing in this class holds state.
 *
 * <p>These methods only know the eight older techniques ({@link Technique}); the screens that show everything
 * use {@link PowerService} directly. New code should call {@link PowerService}.</p>
 */
public final class TechniqueManager {

    private TechniqueManager() {
    }

    /** Opens the power data for the world. */
    public static void initialize(Path worldRoot) {
        PowerService.initialize(worldRoot);
    }

    /** Writes the power data if it changed. */
    public static void save() {
        PowerService.saveNow();
    }

    /** Resource regeneration and vow checks are tick services now. */
    public static void tick(ServerPlayer player) {
    }

    // ── reading ─────────────────────────────────────────────────────────────────────────────

    /** The equipped technique when it is one of the eight older ones, otherwise null. */
    public static Technique getTechnique(String uuid) {
        return Technique.byName(PowerService.state(uuid).equipped);
    }

    public static boolean hasTechnique(String uuid) {
        return PowerService.state(uuid).equipped != null;
    }

    /** The older techniques the player holds. */
    public static Set<Technique> learned(String uuid) {
        Set<Technique> out = new HashSet<>();
        for (PowerCatalog.TechniqueDef def : PowerService.heldTechniques(uuid)) {
            Technique t = Technique.byName(def.id());
            if (t != null) {
                out.add(t);
            }
        }
        return out;
    }

    public static Technique.MasteryRank getRank(String uuid) {
        return PowerService.equippedRank(uuid);
    }

    public static int usesOf(String uuid, Technique technique) {
        return PowerService.state(uuid).uses.getOrDefault(technique.name(), 0);
    }

    public static float getEnergy(String uuid) {
        return (float) PowerService.resource(uuid);
    }

    public static int getMaxEnergy(String uuid) {
        return (int) PowerService.maxResource(uuid);
    }

    // ── changing ────────────────────────────────────────────────────────────────────────────

    /** Right click with a Technique Manual: learns the technique and uses the manual up. */
    public static boolean tryUseFromItem(ServerPlayer player, ItemStack stack) {
        Technique fromManual = TechniqueItems.fromStack(stack);
        if (fromManual == null) {
            return false;
        }
        if (PowerService.learnFromManual(player, fromManual.name())) {
            stack.shrink(1);
        }
        return true;
    }

    public static void learnFromHeldManual(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        Technique fromManual = TechniqueItems.fromStack(held);
        if (fromManual == null) {
            player.sendSystemMessage(Text.tr("political.power.manual.hold_one"), false);
            return;
        }
        if (PowerService.learnFromManual(player, fromManual.name())) {
            held.shrink(1);
        }
    }

    /** Admin grant: learn and equip. */
    public static boolean grant(ServerPlayer player, Technique technique) {
        PowerService.learnFromManual(player, technique.name());
        return PowerService.equip(player, technique.name());
    }

    public static boolean equip(ServerPlayer player, String rawName) {
        Technique technique = Technique.byDisplay(rawName);
        if (technique == null || !PowerService.equip(player, technique.name())) {
            player.sendSystemMessage(Text.tr("political.power.equip.failed"), false);
            return false;
        }
        player.sendSystemMessage(Text.tr("political.power.equip.done", Text.plain(technique.displayName)), false);
        return true;
    }

    public static void renounce(ServerPlayer player) {
        PowerService.unequip(player.getUUID().toString());
    }

    public static void selectAbility(String uuid, String abilityId) {
        PowerService.selectAbility(uuid, abilityId);
    }

    public static Technique.Ability getSelectedAbility(String uuid) {
        com.political.power.model.AbilitySpec selected = PowerService.selected(uuid);
        Technique technique = getTechnique(uuid);
        return selected == null || technique == null ? null : technique.abilityById(selected.id());
    }

    public static boolean isAbilityUnlocked(String uuid, Technique.Ability ability) {
        return PowerService.equippedRank(uuid).ordinal() >= ability.rankIndex();
    }

    // ── casting ─────────────────────────────────────────────────────────────────────────────

    /** The cast command: the same cast as the sneak click. */
    public static void castSelected(ServerPlayer player) {
        if (PowerService.selected(player.getUUID().toString()) == null) {
            player.sendSystemMessage(Text.tr("political.power.fail.no_technique_selected"), true);
            return;
        }
        PowerService.cast(player);
    }

    public static void showRanks(ServerPlayer player) {
        PowerService.showTechniques(player);
    }
}
