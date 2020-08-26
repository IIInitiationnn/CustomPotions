package custompotions;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class commands implements CommandExecutor {
    // Plugin instance.
    private main pluginInstance;

    commands(main pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("custompotions") && !label.equalsIgnoreCase("cp")) {
            return false;
        }

        // Display help prompt with available CP commands.
        if (args.length == 0) {
            if (!sender.hasPermission("custompotions.reload") && !sender.hasPermission("custompotions.modify") && !sender.hasPermission("custompotions.brew")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to run this command.");
                pluginInstance.permissionDenied(sender);
                return false;
            }
            sender.sendMessage(ChatColor.GOLD + "CustomPotions commands you have access to:");
            if (sender.hasPermission("custompotions.reload")) {
                sender.sendMessage(ChatColor.GOLD + "/" + label + " reload" + ChatColor.WHITE + ": reloads the config and plugin.");
            }
            if (sender.hasPermission("custompotions.modify")) {
                sender.sendMessage(ChatColor.GOLD + "/" + label + " modify" + ChatColor.WHITE + ": allows you to edit and create new potions with custom effects.");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " give" + ChatColor.WHITE + ": allows you to withdraw a custom potion.");
            }
            if (sender.hasPermission("custompotions.brew")) {
                sender.sendMessage(ChatColor.GOLD + "/" + label + " info <potion>" + ChatColor.WHITE + ": displays all information about the potion.");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " list" + ChatColor.WHITE + ": displays all custom potions.");
            }
            return false;
        }

        /***************************************************************************************************************
        *                                                     INFO                                                     *
        ***************************************************************************************************************/

        if (args[0].equalsIgnoreCase("info")) {
            // pluginInstance.getLogger().info("inv is opened: " + pluginInstance.isInvOpened);
            // TODO: for console, display all information. for player, open GUI with the potion

            if (!sender.hasPermission("custompotions.brew")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to run this command.");
                pluginInstance.permissionDenied(sender);
                return false;
            }

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " info <potion>");
                return false;
            }

            sender.sendMessage(ChatColor.GOLD + "TO BE IMPLEMENTED");
            return true;
            //return !pluginInstance.info(sender, args[1].toUpperCase());
        }

        /***************************************************************************************************************
        *                                                     LIST                                                     *
        ***************************************************************************************************************/

        // TODO: for console: ???? for player, they get a gui for info (could even just call info)
        else if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("custompotions.brew")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to run this command.");
                pluginInstance.permissionDenied(sender);
                return false;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " list");
                return false;
            }

            //pluginInstance.list(sender);
            sender.sendMessage(ChatColor.GOLD + "TO BE IMPLEMENTED");
            return true;
        }

        /***************************************************************************************************************
        *                                                    RELOAD                                                    *
        ***************************************************************************************************************/

        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("custompotions.reload")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to run this command.");
                pluginInstance.permissionDenied(sender);
                return false;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reload");
                return false;
            }
            pluginInstance.reload();
            sender.sendMessage(ChatColor.GREEN + "CustomPotions has been successfully reloaded.");
            return true;

        }

        /***************************************************************************************************************
        *                                                    MODIFY                                                    *
        ***************************************************************************************************************/

        else if (args[0].equalsIgnoreCase("modify")) {
            if (!sender.hasPermission("custompotions.modify")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to run this command.");
                pluginInstance.permissionDenied(sender);
                return false;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " modify");
                return false;
            }

            return !pluginInstance.modifyPotions(sender);

        }

        /***************************************************************************************************************
        *                                                     GIVE                                                     *
        ***************************************************************************************************************/

        else if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("custompotions.modify")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to run this command.");
                pluginInstance.permissionDenied(sender);
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "Only players can use this command.");
                return false;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " give");
                return false;
            }

            return !pluginInstance.givePotions((Player) sender, null);
        }

        // INVALID COMMAND
        else {
            sender.sendMessage(ChatColor.RED + "/" + label + " " + args[0] + " is not a valid command.");
            return false;
        }

    }

}
