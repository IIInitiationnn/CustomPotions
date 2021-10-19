package xyz.iiinitiationnn.custompotions;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.iiinitiationnn.custompotions.gui.GUI;
import xyz.iiinitiationnn.custompotions.states.GiveMenu;
import xyz.iiinitiationnn.custompotions.states.MainMenu;

public class Commands implements CommandExecutor {
    private final Main pluginInstance;

    public Commands(Main pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        // Not a CP command
        if (!label.equalsIgnoreCase("custompotions") && !label.equalsIgnoreCase("cp")) {
            return false;
        }

        // Display help prompt with available commands
        // TODO hover and clickable
        if (args.length == 0) {
            // Insufficient permissions
            if (Permissions.hasNone(sender)) {
                return Permissions.sendDeniedMessage(sender);
            }

            sender.sendMessage(ChatColor.GOLD + "CustomPotions commands you have access to:");
            String prefix = ChatColor.GOLD + "/" + label;
            if (Permissions.hasReload(sender))
                sender.sendMessage(prefix + " reload" + ChatColor.WHITE + ": reloads the config and plugin.");
            if (Permissions.hasModify(sender))
                sender.sendMessage(prefix + " modify" + ChatColor.WHITE + ": allows you to edit and create new potions with custom effects.");
                sender.sendMessage(prefix + " give" + ChatColor.WHITE + ": allows you to withdraw a custom potion.");
            if (Permissions.hasBrew(sender))
                sender.sendMessage(prefix + " info" + ChatColor.WHITE + ": displays information about all custom potions.");
            return true;
        }

        // Info for all potions
        if (args[0].equalsIgnoreCase("info")) {
            // TODO: for console, display all information. for player, open GUI with the potion, clicking it brings up a brewing stand GUI
            //  which rotates through all the recipes (1 per 2 seconds?) maybe clicking on potions in this menu will show how they can be brewed
            //  and clicking on the item will take you back

            // Insufficient permissions
            if (!Permissions.hasBrew(sender)) {
                return Permissions.sendDeniedMessage(sender);
            }

            // Incorrect usage
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " info");
                return false;
            }

            sender.sendMessage(ChatColor.GOLD + "TO BE IMPLEMENTED");
            return true;
        }

        // Reloads plugin
        else if (args[0].equalsIgnoreCase("reload")) {
            // Insufficient permissions
            if (!Permissions.hasReload(sender)) {
                return Permissions.sendDeniedMessage(sender);
            }

            // Incorrect usage
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reload");
                return false;
            }

            pluginInstance.reload();
            sender.sendMessage(ChatColor.GREEN + "CustomPotions has been successfully reloaded.");
            return true;

        }

        // Modify or create new potions
        else if (args[0].equalsIgnoreCase("modify")) {
            // Insufficient permissions
            if (!Permissions.hasModify(sender)) {
                return Permissions.sendDeniedMessage(sender);
            }

            // Not a player
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "Only players can use this command.");
                return false;
            }

            // Incorrect usage
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " modify");
                return false;
            }

            new GUI(new MainMenu(), (Player) sender)
                .nextState()
                .open();
            return true;
        }

        // Give potions to player
        else if (args[0].equalsIgnoreCase("give")) {
            // Insufficient permissions
            if (!Permissions.hasModify(sender)) {
                return Permissions.sendDeniedMessage(sender);
            }

            // Not a player
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "Only players can use this command.");
                return false;
            }

            // Incorrect usage
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " give");
                return false;
            }

            new GUI(new GiveMenu(), (Player) sender)
                .nextState()
                .open();
            return true;
        }

        // Debug
        else if (args[0].equalsIgnoreCase("debug")) {
            Potion.debugCustomPotions();
            return true;
        }

        // Invalid command
        else {
            sender.sendMessage(ChatColor.RED + "/" + label + " " + args[0] + " is not a valid command.");
            return false;
        }
    }
}
