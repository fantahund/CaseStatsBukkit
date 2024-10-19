package de.fanta.casestatsbukkit.commands;

import de.fanta.casestatsbukkit.CaseStatsBukkit;
import de.fanta.casestatsbukkit.data.CaseStat;
import de.fanta.casestatsbukkit.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CaseDeleteCommand extends SubCommand {
   private final CaseStatsBukkit plugin;

   public CaseDeleteCommand(CaseStatsBukkit plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
      if (!(sender instanceof Player p)) {
         ChatUtil.sendErrorMessage(sender, "Du musst ein Spieler sein!");
         return true;
      } else if (!args.hasNext()) {
         ChatUtil.sendErrorMessage(p, "Du musst ein Case angeben.");
         return true;
      } else {
         CaseStat finalCaseStats = null;

         try {
            List<CaseStat> caseStats = this.plugin.getDatabase().getCaseStatList();
            HashMap<String, CaseStat> caseStatsMap = new HashMap<>();
            caseStats.forEach((caseStat) -> {
               caseStatsMap.put(caseStat.id(), caseStat);
            });
            String caseIDString = args.getNext();
            finalCaseStats = caseStatsMap.get(caseIDString);
            if (finalCaseStats == null) {
               ChatUtil.sendErrorMessage(p, "Case " + caseIDString + " nicht gefunden.");
               return true;
            }

            if (!args.hasNext()) {
               ChatUtil.sendErrorMessage(p, "Schreibe zum Löschen \"DELETE\" ans ende vom Befehl!");
               return true;
            }

            if (!args.getNext().equals("DELETE")) {
               ChatUtil.sendErrorMessage(p, "Schreibe zum Löschen \"DELETE\" ans ende vom Befehl!");
            }
         } catch (SQLException var12) {
            throw new RuntimeException(var12);
         }

         try {
            this.plugin.getDatabase().deleteCase(finalCaseStats);
            ChatUtil.sendErrorMessage(p, "Case " + finalCaseStats.id() + " wurde gelöscht!");
            plugin.getLogger().info(p.getName() + " delete Case with id " + finalCaseStats.id() + ".");
         } catch (SQLException var11) {
            this.plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen vom Case", var11);
            ChatUtil.sendErrorMessage(p, "Die Casee konnte nicht gelöscht werden!");
         }

         return true;
      }
   }

   public String getRequiredPermission() {
      return "casestats.editcase";
   }

   public Collection<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
      try {
         List<CaseStat> caseStats = this.plugin.getDatabase().getCaseStatList();
         Collection<String> tabs = new ArrayList<>();
         caseStats.forEach((caseStat) -> {
            tabs.add(caseStat.id());
         });
         return tabs;
      } catch (SQLException var7) {
         plugin.getLogger().log(Level.SEVERE, "Fehler beim abrufen der Cases", var7);
         return Collections.emptyList();
      }
   }
}
