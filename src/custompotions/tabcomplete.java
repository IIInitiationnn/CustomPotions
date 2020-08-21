package custompotions;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class tabcomplete implements TabCompleter {
    List<String> arguments = new ArrayList<String>();
    List<String> emptyList = new ArrayList<String>();

    private main pluginInstance;

    tabcomplete(main pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (arguments.isEmpty()) {
            if (sender.hasPermission("custompotions.reload")) {
                arguments.add("reload");
            }
            if (sender.hasPermission("custompotions.modify")) {
                arguments.add("modify");
            }
            if (sender.hasPermission("custompotions.brew")) {
                arguments.add("info");
                arguments.add("list");
            }
        }

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