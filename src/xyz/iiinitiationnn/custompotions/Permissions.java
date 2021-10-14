package xyz.iiinitiationnn.custompotions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Permissions {

    public static boolean hasReload(CommandSender sender) {
        return sender.hasPermission("custompotions.reload");
    }

    public static boolean hasModify(CommandSender sender) {
        return sender.hasPermission("custompotions.modify");
    }

    public static boolean hasBrew(CommandSender sender) {
        return sender.hasPermission("custompotions.brew");
    }

    public static boolean hasNone(CommandSender sender) {
        return !hasReload(sender) && !hasModify(sender) && !hasBrew(sender);
    }

    public static boolean sendDeniedMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to run this command.");
        Main.log.info(ChatColor.RED + "" + sender.getName() + ChatColor.DARK_RED + " was denied access to command.");
        return false;
    }
}
