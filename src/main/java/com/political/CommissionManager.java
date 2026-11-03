package com.political;


import net.minecraft.util.Prediction;
import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * The requisition board.
 *
 * <p>A player holds a small number of postings at once, each in its own slot with its own two hour
 * reroll timer, and finishing one takes the goods out of their inventory rather than merely checking
 * that they own them. That consumption is the entire design. A board that only inspects is a
 * checklist, a board that takes what it asked for is a decision about whether the reward is worth
 * the material.</p>
 *
 * <p>Slots are earned rather than bought. Standing accrues from every completion, weighted by how
 * hard the posting was, and each milestone opens another slot up to a cap, so the board widens for
 * people who actually work it and cannot be widened any other way.</p>
 */
public final class CommissionManager {

    private CommissionManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Random RNG = new Random();
    private static Path savePath;

    public static final int MAX_SLOTS = 5;
    /** Standing needed for each slot beyond the first. */
    public static final int[] SLOT_MILESTONES = {0, 25, 90, 260, 700};
    private static final long REROLL_MS = 2 * 60 * 60 * 1000L;

    /** One player's board. */
    public static final class Board {
        /** Commission index per slot, or -1 for an empty slot. */
        public List<Integer> slots = new ArrayList<>();
        /** When each slot may next be rerolled for free. */
        public List<Long> nextReroll = new ArrayList<>();
        public int standing;
        public int completed;
        /** Warrant pieces already paid out, by piece key. */
        public Set<String> warrantPieces = new HashSet<>();

        int slotCount() {
            int n = 1;
            for (int i = 1; i < SLOT_MILESTONES.length && i < MAX_SLOTS; i++) {
                if (standing >= SLOT_MILESTONES[i]) n = i + 1;
            }
            return Math.min(MAX_SLOTS, n);
        }
    }

    private static final Map<String, Board> BOARDS = new HashMap<>();

    // ── Persistence ───────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("commissions.json");
        BOARDS.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath)) {
            java.lang.reflect.Type t = new TypeToken<Map<String, Board>>() {}.getType();
            Map<String, Board> data = GSON.fromJson(r, t);
            if (data != null) BOARDS.putAll(data);
        } catch (Exception e) {
            System.out.println("[Commissions] Could not read boards: " + e.getMessage());
        }
    }

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer w = Files.newBufferedWriter(savePath)) {
                GSON.toJson(BOARDS, w);
            }
        } catch (Exception e) {
            System.out.println("[Commissions] Could not write boards: " + e.getMessage());
        }
    }

    // ── Board state ───────────────────────────────────────────────────────

    public static Board boardOf(ServerPlayer player) {
        Board b = BOARDS.computeIfAbsent(player.getUUID().toString(), k -> new Board());
        int want = b.slotCount();
        // Grow the lists to match earned slots, filling anything new with a fresh posting.
        while (b.slots.size() < want) {
            b.slots.add(randomIndex());
            b.nextReroll.add(0L);
        }
        while (b.nextReroll.size() < b.slots.size()) b.nextReroll.add(0L);
        // A slot that somehow ended up empty gets refilled rather than shown blank.
        for (int i = 0; i < b.slots.size(); i++) {
            if (b.slots.get(i) == null || b.slots.get(i) < 0) b.slots.set(i, randomIndex());
        }
        return b;
    }

    public static int slotCount(ServerPlayer player) {
        return boardOf(player).slotCount();
    }

    public static int standing(ServerPlayer player) {
        return boardOf(player).standing;
    }

    public static int completed(ServerPlayer player) {
        return boardOf(player).completed;
    }

    /** Standing still needed for the next slot, or zero at the cap. */
    public static int toNextSlot(ServerPlayer player) {
        Board b = boardOf(player);
        int slots = b.slotCount();
        if (slots >= MAX_SLOTS) return 0;
        return Math.max(0, SLOT_MILESTONES[slots] - b.standing);
    }

    public static Commission commissionAt(ServerPlayer player, int slot) {
        Board b = boardOf(player);
        if (slot < 0 || slot >= b.slots.size()) return null;
        return Commission.byIndex(b.slots.get(slot));
    }

    public static long rerollReadyAt(ServerPlayer player, int slot) {
        Board b = boardOf(player);
        if (slot < 0 || slot >= b.nextReroll.size()) return 0;
        return b.nextReroll.get(slot);
    }

    public static long rerollMinutesLeft(ServerPlayer player, int slot) {
        long ready = rerollReadyAt(player, slot);
        long left = ready - System.currentTimeMillis();
        return left <= 0 ? 0 : Math.max(1, left / 60000L);
    }

    private static int randomIndex() {
        int total = Commission.total();
        return total <= 0 ? 0 : RNG.nextInt(total);
    }

    // ── Rerolling ─────────────────────────────────────────────────────────

    public static boolean reroll(ServerPlayer player, int slot) {
        Board b = boardOf(player);
        if (slot < 0 || slot >= b.slots.size()) return false;
        long ready = b.nextReroll.get(slot);
        if (System.currentTimeMillis() < ready) {
            player.sendSystemMessage(Component.literal("§cThat slot is not ready. About §f"
                    + rerollMinutesLeft(player, slot) + "§c minutes left."));
            return false;
        }
        b.slots.set(slot, randomIndex());
        b.nextReroll.set(slot, System.currentTimeMillis() + REROLL_MS);
        Commission c = Commission.byIndex(b.slots.get(slot));
        player.sendSystemMessage(Component.literal("§eNew posting: " + (c == null ? "?" : c.colouredTitle())));
        save();
        return true;
    }

    // ── Handing in ────────────────────────────────────────────────────────

    /** How many of the required good the player is holding. */
    public static int held(ServerPlayer player, Commission c) {
        int n = 0;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty() || !st.is(c.good.item())) continue;
            // A custom item that happens to sit on the same base must not count as raw goods.
            if (SlayerItems.getCustomItemId(st) != null) continue;
            n += st.getCount();
        }
        return n;
    }

    /**
     * Takes the goods and pays out. Returns false when the player is short, having taken nothing.
     */
    public static boolean complete(ServerPlayer player, int slot) {
        Board b = boardOf(player);
        Commission c = commissionAt(player, slot);
        if (c == null) return false;

        int have = held(player, c);
        if (have < c.count) {
            player.sendSystemMessage(Component.literal("§cYou have §f" + have + "§c of §f"
                    + c.count + " " + c.good.display() + "§c."));
            return false;
        }

        // Take the goods first. Everything after this point is payout, so a failure partway
        // through can never leave the player charged twice.
        removeGoods(player, c);

        // Capture the slot count before standing moves. Reading it afterwards compares the new
        // value against itself, which is what made the milestone announcement unreachable.
        int slotsBefore = b.slotCount();

        b.completed++;
        b.standing += c.standingReward();

        long coins = c.coinReward();
        CoinManager.giveCoins(player, coins);
        player.giveExperiencePoints(c.xpReward());
        WarrantArmour.addCharge(player, (int) Math.min(400, 20 + c.difficulty / 500));

        Crate key = c.keyReward();
        if (key != null) CrateManager.giveKeys(player, key, 1);

        String piece = null;
        if (c.awardsWarrantPiece()) {
            WarrantArmour.Piece p = WarrantArmour.nextMissing(player, b.warrantPieces);
            if (p != null) {
                b.warrantPieces.add(p.key);
                ItemStack give = WarrantArmour.create(p);
                if (!player.getInventory().add(give)) player.drop(give, false, Prediction.SERVER_ONLY);
                piece = p.display;
            }
        }

        announce(player, c, coins, key, piece);

        // The slot refills immediately, but its reroll timer still runs, so finishing a posting
        // is always better than waiting for a new one.
        b.slots.set(slot, randomIndex());
        save();
        int after = b.slotCount();
        if (after > slotsBefore) {
            player.sendSystemMessage(Component.literal("§6§lThe board trusts you with more."));
            player.sendSystemMessage(Component.literal("§7You now hold §f" + after
                    + "§7 postings at once."));
        }
        return true;
    }

    private static void removeGoods(ServerPlayer player, Commission c) {
        int left = c.count;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (left <= 0) break;
            if (st.isEmpty() || !st.is(c.good.item())) continue;
            if (SlayerItems.getCustomItemId(st) != null) continue;
            int take = Math.min(left, st.getCount());
            st.shrink(take);
            left -= take;
        }
    }

    private static void announce(ServerPlayer player, Commission c, long coins,
                                 Crate key, String piece) {
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  " + c.band.colour + "§l"
                + c.band.label.toUpperCase(java.util.Locale.ROOT) + " COMPLETE"));
        player.sendSystemMessage(Component.literal("  §7" + c.title()));
        player.sendSystemMessage(Component.literal("  §7Paid §6" + GuiKit.num(coins)
                + " coins §7and §a" + c.xpReward() + " experience"));
        if (key != null) {
            player.sendSystemMessage(Component.literal("  §7Plus a " + key.colour + key.display + " §7key"));
        }
        if (piece != null) {
            player.sendSystemMessage(Component.literal("  §6§l" + piece + " §r§7enters your keeping."));
        }
        player.sendSystemMessage(Component.literal(""));

        if (player.level() instanceof ServerLevel level) {
            boolean big = c.band.ordinal() >= Commission.Band.CHARTER.ordinal();
            level.playSound(null, player.blockPosition(),
                    big ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 0.8f, big ? 1.0f : 1.3f);
            if (big) Vfx.bloom(level, player.position().add(0, 1, 0));
        }

        if (c.band == Commission.Band.IMPOSSIBILITY && PoliticalServer.server != null) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("  §6§lIMPOSSIBILITY §r§7"
                        + player.getName().getString() + " delivered §f" + c.count + " "
                        + c.good.display()));
            }
        }
    }

    // ── The full set capstone ─────────────────────────────────────────────

    /**
     * Spends Levy charge to fill whatever a posting still needs. Only a full warrant set can do
     * this, which is what makes finishing the set matter rather than being four separate perks.
     */
    public static boolean requisition(ServerPlayer player, int slot) {
        if (!WarrantArmour.fullSet(player)) {
            player.sendSystemMessage(Component.literal(
                    "§cLevy needs all four pieces of the warrant."));
            return false;
        }
        Commission c = commissionAt(player, slot);
        if (c == null) return false;
        if (held(player, c) >= c.count) {
            player.sendSystemMessage(Component.literal("§eYou already have those. Just hand them in."));
            return false;
        }
        if (!WarrantArmour.spendCharge(player, WarrantArmour.REQUISITION_COST)) {
            player.sendSystemMessage(Component.literal("§cNot enough charge. You hold §f"
                    + WarrantArmour.chargeOf(player) + "§c of §f"
                    + WarrantArmour.REQUISITION_COST + "§c."));
            return false;
        }

        // Hand over exactly the shortfall, then complete through the normal path so the goods are
        // consumed and the payout follows the same rules as any other completion.
        // A full inventory would send the topped up goods to the floor, where the hand in cannot
        // see them, so the completion would fail with the charge already gone. Refuse up front
        // rather than taking payment for something that cannot work.
        int short_ = c.count - held(player, c);
        if (!hasRoomFor(player, c, short_)) {
            WarrantArmour.addCharge(player, WarrantArmour.REQUISITION_COST);
            player.sendSystemMessage(Component.literal(
                    "§cNo room for " + short_ + " " + c.good.display() + ". Charge untouched."));
            return false;
        }

        int left = short_;
        while (left > 0) {
            int stack = Math.min(left, c.good.item().getDefaultMaxStackSize());
            ItemStack give = new ItemStack(c.good.item(), stack);
            if (!player.getInventory().add(give)) player.drop(give, false, Prediction.SERVER_ONLY);
            left -= stack;
        }
        player.sendSystemMessage(Component.literal("§6Levy answers. The shortfall is made up."));

        boolean done = complete(player, slot);
        if (!done) {
            // Should not happen now, but charge is expensive enough that a silent loss is worse
            // than an unnecessary refund.
            WarrantArmour.addCharge(player, WarrantArmour.REQUISITION_COST);
            player.sendSystemMessage(Component.literal("§cThe hand in did not go through. Charge returned."));
        }
        return done;
    }

    /** Whether the inventory can actually hold this many, counting partial stacks and gaps. */
    private static boolean hasRoomFor(ServerPlayer player, Commission c, int amount) {
        if (amount <= 0) return true;
        int max = c.good.item().getDefaultMaxStackSize();
        int room = 0;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty()) {
                room += max;
            } else if (st.is(c.good.item()) && SlayerItems.getCustomItemId(st) == null) {
                room += Math.max(0, max - st.getCount());
            }
            if (room >= amount) return true;
        }
        return room >= amount;
    }

    public static void clear(UUID uuid) {
        // Boards are owned progress and survive a disconnect.
    }
}
