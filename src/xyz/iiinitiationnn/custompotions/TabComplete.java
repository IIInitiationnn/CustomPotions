package xyz.iiinitiationnn.custompotions;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter {
    List<String> arguments = new ArrayList<>();
    List<String> emptyList = new ArrayList<>();

    private final Main pluginInstance;

    public TabComplete(Main pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (arguments.isEmpty()) {
            if (Permissions.hasBrew(sender)) {
                arguments.add("info");
            }
            if (Permissions.hasReload(sender)) {
                arguments.add("reload");
            }
            if (Permissions.hasModify(sender)) {
                arguments.add("modify");
                arguments.add("give");
            }

        }

        // TODO
        if (args.length == 1) {
            List<String> arg0 = new ArrayList<String>();
            for (String a: arguments) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    arg0.add(a);
                }
            }
            return arg0;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("info") && sender.hasPermission("custompotions.brew")) {
                List<String> arg1 = new ArrayList<String>();
                Material[] materials = Material.values();
                for (Material a : materials) {
                    if (!a.isItem()) {
                        continue;
                    }
                    if (a.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                        arg1.add(a.name().toLowerCase());
                    }
                }
                return arg1;
            } else {
                return emptyList;
            }
        } else {
            return emptyList;
        }
    }
}