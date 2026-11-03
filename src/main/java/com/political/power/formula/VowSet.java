package com.political.power.formula;

import com.political.power.PowerSettings;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The vows one player holds (C10). At most the configured number are active. They are checked on a timer and
 * when equipment changes; a vow whose condition stops holding is broken: its bonus goes, and the player must
 * wait the configured time before taking any vow again. Death clears all of them.
 */
public final class VowSet {

    /** What happened when the player tried to take a vow. */
    public enum AcceptResult { ACCEPTED, ALREADY_ACTIVE, LIMIT_REACHED, ON_COOLDOWN, CONDITION_NOT_MET }

    private final EnumSet<Vow> active = EnumSet.noneOf(Vow.class);
    private long reacceptAtTick;

    public VowSet() {
    }

    /** For loading: the vows that were active when the player last played. */
    public VowSet(Set<Vow> loaded) {
        active.addAll(loaded);
    }

    public Set<Vow> active() {
        return Set.copyOf(active);
    }

    public boolean isActive(Vow vow) {
        return active.contains(vow);
    }

    /** Ticks left before a new vow may be taken, 0 when allowed. */
    public long reacceptRemaining(long nowTick) {
        return Math.max(0L, reacceptAtTick - nowTick);
    }

    public AcceptResult accept(Vow vow, Vow.Loadout loadout, long nowTick) {
        if (active.contains(vow)) {
            return AcceptResult.ALREADY_ACTIVE;
        }
        if (active.size() >= PowerSettings.VOW_MAX_ACTIVE.get()) {
            return AcceptResult.LIMIT_REACHED;
        }
        if (nowTick < reacceptAtTick) {
            return AcceptResult.ON_COOLDOWN;
        }
        if (!vow.satisfiedBy(loadout)) {
            return AcceptResult.CONDITION_NOT_MET;
        }
        active.add(vow);
        return AcceptResult.ACCEPTED;
    }

    /**
     * Checks every active vow against the loadout. Vows whose condition no longer holds are removed and
     * returned, and the re-accept cooldown starts. Returns an empty list when nothing broke.
     */
    public List<Vow> check(Vow.Loadout loadout, long nowTick) {
        List<Vow> broken = new ArrayList<>();
        for (Vow vow : EnumSet.copyOf(active)) {
            if (!vow.satisfiedBy(loadout)) {
                active.remove(vow);
                broken.add(vow);
            }
        }
        if (!broken.isEmpty()) {
            reacceptAtTick = nowTick + PowerSettings.VOW_REACCEPT_COOLDOWN_TICKS.get();
        }
        return broken;
    }

    /** Death clears every vow. It does not start the re-accept cooldown. */
    public void clearOnDeath() {
        active.clear();
    }

    /** Maximum resource gained from active vows. */
    public double resourceBonus() {
        return active.contains(Vow.NO_ARMOR) ? PowerSettings.VOW_NO_ARMOR_BONUS.get() : 0.0;
    }

    /** Share of every cooldown removed by active vows. */
    public double cooldownReduction() {
        return active.contains(Vow.NO_OFFHAND) ? PowerSettings.VOW_NO_OFFHAND_COOLDOWN_REDUCTION.get() : 0.0;
    }

    /** Extra technique damage from active vows, as a fraction. */
    public double damageBonus() {
        return active.contains(Vow.NO_SPRINT) ? PowerSettings.VOW_NO_SPRINT_DAMAGE_BONUS.get() : 0.0;
    }
}
