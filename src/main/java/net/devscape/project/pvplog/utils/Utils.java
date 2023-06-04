package net.devscape.project.pvplog.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static final Pattern p2 = Pattern.compile("&#([A-Fa-f0-9]){6}");

    public static String format(String message) {
        message = message.replace(">>", "").replace("<<", "");
        Matcher matcher = p2.matcher(message);
        while (matcher.find()) {
            ChatColor hexColor = ChatColor.of(matcher.group().substring(1));
            String before = message.substring(0, matcher.start());
            String after = message.substring(matcher.end());
            message = before + hexColor + after;
            matcher = p2.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String deformat(String str) {
        return ChatColor.stripColor(format(str));
    }

    public static void msgPlayer(Player player, String... str) {
        for (String msg : str) {
            player.sendMessage(format(msg));
        }
    }

    public static void msgPlayer(CommandSender player, String... str) {
        for (String msg : str) {
            player.sendMessage(format(msg));
        }
    }

    public static List<String> color(List<String> lore){
        return lore.stream().map(Utils::format).collect(Collectors.toList());
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(format(message)));
    }
}