package de.fanta.casestatsbukkit.commands;

import de.fanta.casestatsbukkit.CaseStatsBukkit;
import de.fanta.casestatsbukkit.CaseStatsGlobalDataHelper;
import de.fanta.casestatsbukkit.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.logging.Level;

public class CaseStatsLoginCommand extends SubCommand {
    private final CaseStatsBukkit plugin;

    public CaseStatsLoginCommand(CaseStatsBukkit plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        if (!(sender instanceof Player p)) {
            ChatUtil.sendErrorMessage(sender, "Du musst ein Spieler sein!");
            return true;
        }

        if (!isListeningToCaseStatsChannel(p)) {
            ChatUtil.sendErrorMessage(p, "Dir fehlt der CaseStats-Mod um dich einzuloggen.");
            return true;
        }
        try {
            plugin.getGlobalDataHelper().sendLoginRequest(p.getUniqueId());
            plugin.getLoginRequestList().add(p.getUniqueId());
            ChatUtil.sendNormalMessage(p, "Login anfrage wurde gesendet.");
        } catch (IOException e) {
            ChatUtil.sendNormalMessage(p, "Fehler beim Senden der Anfrage.");
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Senden der Anfrage.", e);
        }

        return true;
    }

    public boolean isListeningToCaseStatsChannel(Player player) {
        return player.getListeningPluginChannels().contains(CaseStatsGlobalDataHelper.LOGIN_CLIENT_CHANNEL);
    }
}
