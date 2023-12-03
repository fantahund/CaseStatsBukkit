package de.fanta.casestatsbukkit.commands;

import de.fanta.casestatsbukkit.CaseStatsBukkit;
import de.fanta.casestatsbukkit.CaseStatsGlobalDataHelper;
import de.fanta.casestatsbukkit.data.CaseStat;
import de.fanta.casestatsbukkit.guis.CaseGui;
import de.fanta.casestatsbukkit.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CaseEditCommand extends SubCommand {
    private final CaseStatsBukkit plugin;

    public CaseEditCommand(CaseStatsBukkit plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        if (!(sender instanceof Player p)) {
            ChatUtil.sendErrorMessage(sender, "Du musst ein Spieler sein!");
            return true;
        }

        try {
            List<CaseStat> caseStats = plugin.getDatabase().getCaseStatList();
            if (caseStats.isEmpty()) {
                ChatUtil.sendErrorMessage(p, "Keine Cases in der Liste!");
                return true;
            }
            new CaseGui(caseStats, p).open();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error read CaseStatList", e);
            ChatUtil.sendErrorMessage(p, "Die Cases konnten nicht aus der Datenbank gelesen werden.");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return "casestats.editcase";
    }
}
