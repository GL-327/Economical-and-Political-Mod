package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.political.core.command.CommandRegistrar;
import com.political.core.command.CommandTreeDump;
import com.political.settlement.SettlementCommands;
import com.political.settlement.WarCommands;
import com.political.stats.PlayerStatsManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;

/**
 * The single entry point that registers every command of the mod (M1.8). It replaces the two
 * CommandRegistrationCallback blocks that used to sit in PoliticalServer and keeps their order: the
 * modules first, then the NPC commands, then the large CommandRegistry.
 *
 * <p>Each module is registered through a {@link CommandRegistrar}, which reports a root literal that is
 * registered twice. In a development run it is a startup error (override with
 * {@code -Dpoliticalserver.commandsStrict=false}); in a release it is logged as an error.</p>
 */
final class ModCommands {

    private ModCommands() {
    }

    static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        String override = System.getProperty(CommandRegistrar.STRICT_PROPERTY);
        boolean strict = override != null ? Boolean.parseBoolean(override) : FabricLoader.getInstance().isDevelopmentEnvironment();
        CommandRegistrar<CommandSourceStack> registrar = new CommandRegistrar<>(dispatcher, strict)
                // ChangelogCommands registers "title" as an alias of "titles". It merges into vanilla's /title, which
                // keeps vanilla's operator requirement, so the alias only works for operators. Known defect, kept as
                // it was (HANDOFF.md, KNOWN ISSUES); the owner decides whether to drop the alias or rename it.
                .allowShadowing("title");

        // /stats and /statbar (Skyblock-style player stats)
        registrar.module("PlayerStatsManager", PlayerStatsManager::registerCommands);
        registrar.module("SettlementCommands", SettlementCommands::register);
        // /war declare|vote|peace|council|shadow
        registrar.module("WarCommands", WarCommands::register);
        // ── V5 ──
        registrar.module("ExpeditionCommands", ExpeditionCommands::register);
        registrar.module("GuildCommands", GuildCommands::register);
        registrar.module("TradeCommands", TradeCommands::register);
        registrar.module("LivingWorldCommands", LivingWorldCommands::register);
        registrar.module("ChangelogCommands", ChangelogCommands::register);
        registrar.module("V6Commands", V6Commands::register);
        registrar.module("ModernCommands", ModernCommands::register);
        registrar.module("VfxShowcase", VfxShowcase::register);
        registrar.module("ReforgeGui", ReforgeGui::register);
        registrar.module("RelicGui", RelicGui::register);
        registrar.module("CompanionGui", CompanionGui::register);
        registrar.module("ContractGui", ContractGui::register);
        registrar.module("NpcSpawnCommands", NpcSpawnCommands::register);
        registrar.module("PowerCommands", com.political.power.game.PowerCommands::register);
        // Everything else
        registrar.module("CommandRegistry", CommandRegistry::registerAll);

        CommandTreeDump.writeIfRequested(dispatcher.getRoot(), name -> registrar.owners().containsKey(name));
        registrar.finish();
    }
}
