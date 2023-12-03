package de.fanta.casestatsbukkit.utils;

import de.iani.cubesideutils.bukkit.ChatUtilBukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class ChatUtil {

    public static final ChatColor GREEN = ChatColor.of("#52ff9d");
    public static final ChatColor YELLOW = ChatColor.of("#E4E737");
    public static final ChatColor ORANGE = ChatColor.of("#ffac4d");
    public static final ChatColor RED = ChatColor.of("#ff6b6b");
    public static final ChatColor PINK = ChatColor.of("#FF04F7");
    public static final ChatColor BLUE = ChatColor.of("#87f7ea");

    private ChatUtil() {
        // prevent instances
    }

    public static void sendMessage(CommandSender sender, String colors, Object... messageParts) {
        ChatUtilBukkit.sendMessage(sender, GREEN + "[" + BLUE + "CaseStats" + GREEN + "]", colors, messageParts);
    }

    public static void sendNormalMessage(CommandSender sender, Object... messageParts) {
        sendMessage(sender, GREEN.toString(), messageParts);
    }

    public static void sendWarningMessage(CommandSender sender, Object... messageParts) {
        sendMessage(sender, ORANGE.toString(), messageParts);
    }

    public static void sendErrorMessage(CommandSender sender, Object... messageParts) {
        sendMessage(sender, RED.toString(), messageParts);
    }

    public static void sendDebugMessage(CommandSender sender, Object... messageParts) {
        if (sender.hasPermission("fanta.debug")) {
            if (sender != null) {
                sendMessage(sender, PINK.toString(), messageParts);
            }
        }
    }

    public static void sendBrodcastMessage(Object... messageParts) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMessage(player, GREEN.toString(), messageParts);
        }
    }

    public static void sendTitleToAll(String headline, String text, ChatColor textColor, int in, int time, int out, boolean pling) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendTitleToPlayer(p, headline, text, textColor, in, time, out, pling);
        }
    }

    public static void sendTitleToAll(String headline, String text, ChatColor textColor, boolean pling) {
        sendTitleToAll(headline, text, textColor, 10, 60, 10, pling);
    }

    public static void sendTitleToAll(String headline, String text, ChatColor textColor) {
        sendTitleToAll(headline, text, textColor, 10, 60, 10, true);
    }

    public static void sendTitleToPlayer(Player player, @Nullable String headline, @Nullable String text, ChatColor textColor, int in, int time, int out, boolean pling) {
        String tempheadline = headline;
        String temptext = text;

        if (headline == null || headline.equals("")) {
            tempheadline = " ";
        }

        if (temptext == null || temptext.equals("")) {
            temptext = " ";
        }

        player.sendTitle(ChatUtil.BLUE + tempheadline, textColor + temptext, in, time, out);
        if (pling) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F);
        }
    }
}
