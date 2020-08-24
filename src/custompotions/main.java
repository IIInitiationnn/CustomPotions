package custompotions;

import org.apache.commons.lang.StringUtils;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryAnvil;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import utilities.AnvilGUI;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Logger;

public class main extends JavaPlugin implements Listener {
    private Logger log;
    public data potionData;
    public List<PotionInfo> allCustomPotions;
    public int REDSTAINEDGLASSNUMBER = Integer.MAX_VALUE;

    // TODO two pots cant have same name (includes _ vs space, and is caps insensitive), and no two with same predecessor can have same ingredient
    //  potentially playing around with lore for potion effects to show the correct potency, and time in day:hour:minute:second if applicable
    //  maybe in the distant future add presets like splash from existing or whatever
    //  handle weird gunpowder and dragons breath shenanigans??
    //  potions having limited potency like dolphins grace, do conditional effect amplifiers on those cases
    //  uniquely identify clicks to reduce spaghetti

    /*******************************************************************************************************************
    *                                                      GLOBALS                                                     *
    ********************************************************************************************************************
    * Global variables involved in creating a new potion.                                                              *
    * Type and colour are immediately embedded in the potion ItemStack.                                                *
    * Effects are added to the potion ItemStack after the relevant significant information is added.                   *
    * They are temporarily stored in the buffers effectTypeInput, effectDurationInput and effectAmplifierInput.        *
    * Ingredients and predecessors are added to a list of PotionRecipes once both are added.                           *
    *******************************************************************************************************************/

    public Inventory inv = null;
    AnvilGUI anvilInv = null;
    public String effectTypeInput = null;
    public String effectDurationInput = null; // in seconds
    public String effectAmplifierInput = null;
    public Material ingredientInput = null;
    public List<PotionRecipe> potionRecipesInput = new ArrayList<>();
    public String potionNameInput = null;
    boolean isInvOpened = false;

    // Called whenever the GUI is exited.
    public void resetGlobals() {
        this.inv = null;
        this.anvilInv = null;
        this.effectTypeInput = null;
        this.effectDurationInput = null;
        this.effectAmplifierInput = null;
        this.ingredientInput = null;
        this.potionRecipesInput = new ArrayList<>();
        this.potionNameInput = null;
        this.isInvOpened = false;
    }

    /*******************************************************************************************************************
    *                                                    ESSENTIALS                                                    *
    *******************************************************************************************************************/

    // Startup.
    @java.lang.Override
    public void onEnable() {
        this.log = this.getLogger();
        this.log.info("Initialising CustomPotions and validating potions.");
        this.potionData = new data(this);
        potionData.saveDefaultData();
        this.allCustomPotions = newPotionInfoList();
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("custompotions").setExecutor(new commands(this));
        this.getCommand("custompotions").setTabCompleter(new tabcomplete(this));
    }

    // Reload.
    public void reload() {
        this.potionData.reloadData();
        allCustomPotions = newPotionInfoList();
        // TODO close inventories
        resetGlobals();
        this.getCommand("custompotions").setExecutor(new commands(this));
        this.getCommand("custompotions").setTabCompleter(new tabcomplete(this));
    }

    // Stop.
    @java.lang.Override
    public void onDisable() {
        this.log.info("CustomPotions has been disabled.");
    }

    // Displaying permission denied to console.
    public void permissionDenied(CommandSender sender) {
        this.log.info(ChatColor.RED + "" + sender.getName() + ChatColor.DARK_RED + " was denied access to command.");
    }

    /*******************************************************************************************************************
    *                                                      HELPERS                                                     *
    *******************************************************************************************************************/

    // Capitalise string and replace delimiter characters with spaces.
    public String normaliseCapitalise(String string, String delimiter) {
        String[] all = string.split(delimiter);
        String normalised = null;
        for (String s : all) {
            if (normalised == null) {
                normalised = "";
            } else {
                normalised = normalised.concat(" ");
            }
            normalised = normalised.concat(StringUtils.capitalize(s.toLowerCase()));
        }
        return normalised;
    }

    // Given a predecessor potion's metadata, return an appropriate string matching its name
    public String elementaryName(PotionMeta meta) {
        switch (meta.getBasePotionData().getType()) {
            case WATER:
                return "Water Bottle";
            case AWKWARD:
                return "Awkward Potion";
            case MUNDANE:
                return "Mundane Potion";
            case THICK:
                return "Thick Potion";
            default:
                return ChatColor.stripColor(meta.getDisplayName());
        }
    }

    /*******************************************************************************************************************
    *                                                 COLOUR MANAGEMENT                                                *
    *******************************************************************************************************************/

    // Colour class.
    public static class Colour {
        public int r, g, b;
        public String name;

        // Constructor.
        public Colour(String name, int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
        }

        // Methods.
        public String getName() {
            return name;
        }
        public int getR() {
            return r;
        }
        public int getG() {
            return g;
        }
        public int getB() {
            return b;
        }
    }

    // Creates a list of suitable potion colours.
    private ArrayList<Colour> newColourList() {
        ArrayList<Colour> colourList = new ArrayList<Colour>();
        colourList.add(new Colour("Red", 0xFF, 0x00, 0x00));
        colourList.add(new Colour("Orange", 0xFF, 0xA5, 0x00));
        colourList.add(new Colour("Yellow", 0xFF, 0xFF, 0x00));
        colourList.add(new Colour("Olive", 0x80, 0x80, 0x00));
        colourList.add(new Colour("Lime", 0x00, 0xFF, 0x00));
        colourList.add(new Colour("Green", 0x00, 0x80, 0x00));
        colourList.add(new Colour("Teal", 0x00, 0x80, 0x80));
        colourList.add(new Colour("Aqua", 0x00, 0xFF, 0xFF));
        colourList.add(new Colour("Fuchsia", 0xFF, 0x00, 0xFF));
        colourList.add(new Colour("Maroon", 0x80, 0x00, 0x00));
        colourList.add(new Colour("Purple", 0x80, 0x00, 0x80));
        colourList.add(new Colour("Blue", 0x00, 0x00, 0xFF));
        colourList.add(new Colour("Navy", 0x00, 0x00, 0x80));
        colourList.add(new Colour("White", 0xFF, 0xFF, 0xFF));
        colourList.add(new Colour("Silver", 0xC0, 0xC0, 0xC0));
        colourList.add(new Colour("Gray", 0x80, 0x80, 0x80));
        colourList.add(new Colour("Black", 0x00, 0x00, 0x00));
        return colourList;
    }

    // Given a Colour, return the corresponding ChatColor.
    public ChatColor chatColorFromColor(Color c) {
        String name = colourNameFromColor(c) == null ? "none" : colourNameFromColor(c);

        switch (name) {
            case "Red":
                return ChatColor.RED;
            case "Orange":
                return ChatColor.GOLD;
            case "Yellow":
                return ChatColor.YELLOW;
            case "Olive":
            case "Green":
                return ChatColor.DARK_GREEN;
            case "Lime":
                return ChatColor.GREEN;
            case "Teal":
                return ChatColor.DARK_AQUA;
            case "Aqua":
                return ChatColor.AQUA;
            case "Fuchsia":
                return ChatColor.LIGHT_PURPLE;
            case "Maroon":
                return ChatColor.DARK_RED;
            case "Purple":
                return ChatColor.DARK_PURPLE;
            case "Blue":
            case "Navy":
                return ChatColor.BLUE;
            case "White":
                return ChatColor.WHITE;
            case "Silver":
                return ChatColor.GRAY;
            case "Gray":
            case "Black":
                return ChatColor.DARK_GRAY;
            default:
                return null;
        }
    }

    public String colourNameFromColor (Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        String name = null;

        for (Colour h : newColourList()) {
            if (h.getR() == r && h.getG() == g && h.getB() == b) {
                name = h.getName();
                break;
            }
        }
        return name;
    }

    // Using values from potion.yml, determine if a colour is valid.
    public boolean isValidColour(String name, int r, int g, int b) {
        for (Colour h : newColourList()) {
            if (h.getR() == r && h.getG() == g && h.getB() == b && h.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /*******************************************************************************************************************
    *                                                 POTION MANAGEMENT                                                *
    *******************************************************************************************************************/

    // PotionRecipe class.
    public static class PotionRecipe {
        ItemStack predecessor;
        Material ingredient;

        public PotionRecipe(ItemStack predecessor, Material ingredient) {
            this.predecessor = predecessor;
            this.ingredient = ingredient;
        }
    }

    // PotionInfo class.
    public static class PotionInfo {
        public String name;
        public Material type;
        public List<PotionRecipe> potionrecipes;
        public Colour colour;
        public ItemStack itemstack;
    }

    // Returns a data-type containing all information regarding a custom potion.
    public PotionInfo newPotionInfo(FileConfiguration fileInput, String s) {
        PotionInfo pc = new PotionInfo();

        // Name (String).
        pc.name = s;

        // Type (Material).
        pc.type = helperType(fileInput, s);
        if (pc.type == null) return null;

        // ItemStack (ItemStack).
        ItemStack potionItemStack = new ItemStack(pc.type);
        PotionMeta potionMeta = (PotionMeta) potionItemStack.getItemMeta();

        potionMeta.setDisplayName(ChatColor.RESET + s);

        // Colour (Colour) - handles potionMeta.
        pc.colour = helperColour(fileInput, s, potionMeta);
        if (pc.colour == null) return null;

        // Effects (List<PotionEffect>).
        if (fileInput.getString(s + ".effects") != null && fileInput.getString(s + ".effects").equalsIgnoreCase("none")) {

        } else if (helperEffects(fileInput.getConfigurationSection(s + ".effects"), s, potionMeta,
                pc.type == Material.LINGERING_POTION) == null) {
            return null;
        }

        // Predecessor (ItemStack).
        pc.potionrecipes = helperPredecessor(fileInput, s);
        if (pc.potionrecipes == null) return null;

        // ItemStack (ItemStack).
        potionItemStack.setItemMeta(potionMeta);
        pc.itemstack = potionItemStack;
        this.log.info("Successfully added " + s + " to the game.");

        return pc;
    }

    // Returns list of all custom potions loaded from memory.
    public List<PotionInfo> newPotionInfoList() {
        FileConfiguration fileInput = potionData.getData();
        Set<String> names = fileInput.getKeys(false);
        List<PotionInfo> pcList = new ArrayList<PotionInfo>();

        for (String s : names) {
            PotionInfo add = newPotionInfo(fileInput, s);
            if (add == null) continue;
            pcList.add(add);
        }
        return pcList;
    }

    // Helper to get (from file) and set the type of a potion.
    public Material helperType(FileConfiguration fileInput, String s) {
        String potionType = fileInput.getString(s + ".type");
        if (potionType == null) {
            this.log.warning(s + " does not have a valid type. Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion.");
            return null;
        }
        Material match = Material.matchMaterial(potionType);
        if (match == null) {
            this.log.warning(potionType + " is not a valid type for " + s + ". Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion.");
            return null;
        }
        return match;
    }

    // Helper to get (from file) and set the colour of a potion, as well as its corresponding metadata information.
    public Colour helperColour(FileConfiguration fileInput, String s, PotionMeta potionMeta) {
        String colourName = fileInput.getString(s + ".colour.name");
        String redS = fileInput.getString(s + ".colour.red");
        String greenS = fileInput.getString(s + ".colour.green");
        String blueS = fileInput.getString(s + ".colour.blue");
        int redI, greenI, blueI;
        if (colourName == null || redS == null || greenS == null || blueS == null) {
            this.log.warning(s + " does not have a valid colour. Skipping the potion.");
            return null;
        } else {
            try {
                redI = Integer.parseInt(redS);
                greenI = Integer.parseInt(greenS);
                blueI = Integer.parseInt(blueS);
                if (!isValidColour(colourName, redI, greenI, blueI)) {
                    this.log.warning(s + " does not have a valid colour with name and/or RGB values matching database values. Skipping the potion.");
                    return null;
                }
                potionMeta.setColor(Color.fromRGB(redI, greenI, blueI));
                return new Colour(colourName, redI, greenI, blueI);
            } catch (Exception e) {
                this.log.warning(s + " does not have a valid colour. Skipping the potion.");
                return null;
            }
        }
    }

    // Helper to get (from file) and set the effects of a potion, as well as its corresponding metadata information.
    public List<PotionEffect> helperEffects(ConfigurationSection fx, String s, PotionMeta potionMeta, boolean isLingering) {
        List<PotionEffect> potionEffects = new ArrayList<>();

        if (fx == null) {
            this.log.info(s + " does not have any valid effects. Continuing with the potion.");
            return potionEffects;
        }

        Set<String> effects = fx.getKeys(false);
        for (String effect : effects) {
            if (effect.equalsIgnoreCase("none")) {
                return potionEffects;
            }
            PotionEffectType effectType = PotionEffectType.getByName(effect);
            if (effectType == null) {
                this.log.warning(effect + " is not a valid effect for " + s + ". Skipping the effect.");
                continue;
            }

            String durationS = fx.getString(effect + ".duration");
            if (durationS == null) {
                this.log.warning(effect + " does not have a valid effect duration for " + s + ". Skipping the effect.");
                continue;
            }
            int durationI;
            try {
                durationI = Integer.parseInt(durationS);
            } catch (NumberFormatException e) {
                this.log.warning(effect + " does not have a valid effect duration for " + s + ". Skipping the effect.");
                continue;
            }

            String amplifierS = fx.getString(effect + ".amplifier");
            if (amplifierS == null) {
                this.log.warning(effect + " does not have a valid effect amplifier for " + s + ". Skipping the effect.");
                continue;
            }
            int amplifierI;
            try {
                amplifierI = Integer.parseInt(amplifierS);
            } catch (NumberFormatException e) {
                this.log.warning(effect + " does not have a valid effect amplifier for " + s + ". Skipping the effect.");
                continue;
            }

            durationI = (isLingering) ? 80 * durationI : 20 * durationI;

            PotionEffect pe = new PotionEffect(effectType, durationI, amplifierI);
            potionEffects.add(pe);
            potionMeta.addCustomEffect(pe, false);
        }

        return potionEffects;
    }

    // Helper to get (from file) the information of an ingredient.
    public Material helperIngredient(FileConfiguration fileInput, String s, String predecessor) {
        String ingredient = fileInput.getString(s + ".predecessors." + predecessor + ".ingredient");
        if (ingredient == null) {
            this.log.warning(s + " does not have a valid ingredient for predecessor " + predecessor + ". Skipping.");
            return null;
        }
        Material match = Material.matchMaterial(ingredient);
        if (match == null) {
            this.log.warning(ingredient + " is not a valid ingredient for predecessor " + predecessor + " of " + s + ". Skipping the potion.");
            return null;
        }
        return match;
    }

    // Helper to get (from file) the information of a predecessor.
    public List<PotionRecipe> helperPredecessor(FileConfiguration fileInput, String s) {
        List<PotionRecipe> potionRecipes = new ArrayList<>();
        ConfigurationSection pCS = fileInput.getConfigurationSection(s + ".predecessors");
        if (pCS == null) {
            this.log.warning(s + " does not have any valid predecessors. Skipping the potion.");
            return null;
        }
        Set<String> ps = pCS.getKeys(false);
        for (String preName : ps) {
            boolean potionExists = false;
            for (String name : fileInput.getKeys(false)) {
                if (name.equals(preName)) potionExists = true;
            }
            if (!potionExists) {
                this.log.warning(s + " has a predecessor which does not exist. Skipping the potion.");
                return null;
            }
            // TODO need to check if the effects are the same, + add the 4 types of special water potions
            //  may be a little difficult since it's hard to know whether or not that potion is valid or not. how can we solve this?
            // TODO checking if two predecessors have same ingredient
            String preType = pCS.getString(preName + ".type");
            if (preType == null) {
                this.log.warning(s + " does not have a valid type for predecessor " + preName + ". Skipping the potion.");
                return null;
            }
            Material match = Material.matchMaterial(preType);
            if (match == null) {
                this.log.warning(preType + " is not a valid type for predecessor " + preName + " of potion " + s + ". Skipping the potion.");
                return null;
            } else if (match != Material.POTION && match != Material.SPLASH_POTION && match != Material.LINGERING_POTION) {
                this.log.warning(preType + " is not a valid type for predecessor " + preName + " of potion " + s + "." +
                        " Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion.");
                return null;
            }
            ItemStack predecessor = new ItemStack(match);

            PotionMeta meta = (PotionMeta) predecessor.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + preName);
            if (pCS.getString(preName + ".effects") != null && pCS.getString(preName + ".effects").equalsIgnoreCase("none")) {

            } else if (helperEffects(pCS.getConfigurationSection(preName + ".effects"), s + "'s predecessor " + preName, meta,
                    match == Material.LINGERING_POTION) == null) {
                return null;
            }
            predecessor.setItemMeta(meta);

            /*if (match == Material.POTION || match == Material.LINGERING_POTION || match == Material.SPLASH_POTION) {
                PotionMeta meta = (PotionMeta) predecessor.getItemMeta();
                meta.setDisplayName(ChatColor.RESET + preName);
                if (helperEffects(pCS.getConfigurationSection(preName + ".effects"), s + "'s predecessor " + preName, meta) == null)
                    return null;
                predecessor.setItemMeta(meta);
            } else {
                ItemMeta meta = predecessor.getItemMeta();
                meta.setDisplayName(ChatColor.RESET + preName);
                predecessor.setItemMeta(meta);
            }*/

            Material ingredient = helperIngredient(fileInput, s, preName);
            if (ingredient == null) return null;

            potionRecipes.add(new PotionRecipe(predecessor, ingredient));
        }
        return potionRecipes;
    }

    // 0             1          2        3          4          5          6
    // custompotions click-page type/not colour/not effect/not recipe/not name/not

    // Methods using localized names.
    public String  newLocalized(boolean fromMemory) {
        if (!fromMemory) {
            return "custompotions select-0 not not not not not";
        } else {
            return "custompotions select-0 type colour effect recipe name";
        }
    }
    public boolean isPotionClick(String localized) {
        return localized.split(" ")[0].equalsIgnoreCase("custompotions");
    }
    public boolean isCompletePotion(String localized) {
        for (String s : localized.split(" ")) {
            if (s.equalsIgnoreCase("not")) {
                return false;
            }
        }
        return true;
    }
    public String  getClick(String localized) {
        return localized.split(" ")[1].split("-")[0];
    }
    public int     getPage(String localized) {
        return Integer.parseInt(localized.split(" ")[1].split("-")[1]);
    }
    public boolean hasType(String localized) {
        return localized.split(" ")[2].equalsIgnoreCase("type");
    }
    public boolean hasColour(String localized) {
        return localized.split(" ")[3].equalsIgnoreCase("colour");
    }
    public boolean hasEffect(String localized) {
        return localized.split(" ")[4].equalsIgnoreCase("effect");
    }
    public boolean hasRecipe(String localized) {
        return localized.split(" ")[5].equalsIgnoreCase("recipe");
    }
    public boolean hasName(String localized) {
        return localized.split(" ")[6].equalsIgnoreCase("name");
    }
    public String setClick(String localized, String click) {
        String[] all = localized.split(" ");
        String newLocalized = "custompotions " + click + "-" + all[1].split("-")[1];
        for (int i = 2; i < 7; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        return newLocalized;
    }
    public String setPage(String localized, int page) {
        String[] all = localized.split(" ");
        String newLocalized = "custompotions " + all[1].split("-")[0] + "-" + page;
        for (int i = 2; i < 7; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        return newLocalized;
    }
    public String setType(String localized, boolean type) {
        String[] all = localized.split(" ");
        String newLocalized = "custompotions";
        for (int i = 1; i < 2; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        newLocalized = type ? newLocalized.concat(" type") : newLocalized.concat(" not");
        for (int i = 3; i < 7; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        return newLocalized;
    }
    public String setColour(String localized, boolean colour) {
        String[] all = localized.split(" ");
        String newLocalized = "custompotions";
        for (int i = 1; i < 3; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        newLocalized = colour ? newLocalized.concat(" colour") : newLocalized.concat(" not");
        for (int i = 4; i < 7; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        return newLocalized;
    }
    public String setEffect(String localized, boolean effect) {
        String[] all = localized.split(" ");
        String newLocalized = "custompotions";
        for (int i = 1; i < 4; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        newLocalized = effect ? newLocalized.concat(" effect") : newLocalized.concat(" not");
        for (int i = 5; i < 7; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        return newLocalized;
    }
    public String setRecipe(String localized, boolean recipe) {
        String[] all = localized.split(" ");
        String newLocalized = "custompotions";
        for (int i = 1; i < 5; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        newLocalized = recipe ? newLocalized.concat(" recipe") : newLocalized.concat(" not");
        for (int i = 6; i < 7; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        return newLocalized;
    }
    public String setName(String localized, boolean name) {
        String[] all = localized.split(" ");
        String newLocalized = "custompotions";
        for (int i = 1; i < 6; i++) {
            newLocalized = newLocalized.concat(" " + all[i]);
        }
        newLocalized = name ? newLocalized.concat(" name") : newLocalized.concat(" not");
        return newLocalized;
    }

    // Returns ItemStack with all information added.
    public ItemStack newItemStackGUI(ItemStack item, String localized) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (getClick(localized).equalsIgnoreCase("exit")) {
            if (item.getType() == Material.BARRIER) {
                meta.setDisplayName(ChatColor.RED + "EXIT");
                lore.add(ChatColor.RED + "Click to exit.");
            } else if (isCompletePotion(localized)) {
                meta.setDisplayName(chatColorFromColor(((PotionMeta) meta).getColor()) + potionNameInput);
                lore.add("");
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.GREEN + "Left click to save your changes and exit.");
                lore.add(ChatColor.RED + "Right click to exit without saving.");
            } else {
                meta.setDisplayName(chatColorFromColor(((PotionMeta) meta).getColor()) + potionNameInput);
                lore.add("");
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.RED + "Click to exit without saving.");
            }
            meta.setLore(lore);
        }
        meta.setLocalizedName(localized);
        if (item.getType() == Material.RED_STAINED_GLASS_PANE) {
            meta.setDisplayName(ChatColor.RED + "NO PAGE TO GO TO");
            meta.setLocalizedName(setPage(localized, REDSTAINEDGLASSNUMBER));
        }
        item.setItemMeta(meta);
        return item;
    }

    /*******************************************************************************************************************
    *                                                     COMMANDS                                                     *
    *******************************************************************************************************************/

    // Create and modify custom potions.
    // "modify"
    public boolean modifyPotions(CommandSender sender) {
        this.isInvOpened = true;
        /*if (isInvOpened) {
            sender.sendMessage(ChatColor.RED + "Potion management is currently being undertaken by another user.");
            return true;
        }*/
        if (sender instanceof Player) {
            return playerModifyPotions((Player) sender, null, null);
        } else {
            return consoleModifyPotions(sender);
        }
    }

    // Modifying potions with GUI for players.
    public boolean playerModifyPotions(Player player, ItemStack existingPotion, ItemStack clicked) {
        // previousClick is the info contained in the item that was just clicked.
        String previousClick;
        List<String> lore;
        if (clicked == null && existingPotion == null) {
            previousClick = newLocalized(true);
        } else {
            previousClick = clicked.getItemMeta().getLocalizedName();
        }

        // If returning to the main menu, potions from memory should be relabelled as complete.
        String click = getClick(previousClick);
        if (click.equalsIgnoreCase("select") && getPage(previousClick) == 0) {
            previousClick = newLocalized(true);
        }

        // currentClick is what all the new items will display.
        String currentClick = previousClick;

        // menus: select, type, colour, effect type, effect duration, effect amplifier, ingredient, predecessor, name
        switch (click) {
            case "select": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion to Modify");
                int numPotions = 0;
                int pageNumToDisplay = getPage(previousClick);
                int totalPotions = 1 + allCustomPotions.size();
                int PAGESIZE = 51;
                boolean needsNextPage = (totalPotions - PAGESIZE * pageNumToDisplay) > PAGESIZE;

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = pageNumToDisplay != 0 ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
                currentClick = setClick(previousClick,"select");
                currentClick = setPage(currentClick, pageNumToDisplay != 0 ? pageNumToDisplay - 1 : REDSTAINEDGLASSNUMBER);
                ItemMeta prevMeta = previous.getItemMeta();
                prevMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS PAGE");
                if (pageNumToDisplay != 0) {
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Page " + (pageNumToDisplay - 1));
                    prevMeta.setLore(lore);
                }
                previous.setItemMeta(prevMeta);
                previous = newItemStackGUI(previous, currentClick);
                inv.setItem(35, previous);

                // NEXT PAGE, slot 44.
                ItemStack next = needsNextPage ? new ItemStack(Material.LIME_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta nextMeta = next.getItemMeta();
                currentClick = setClick(previousClick,"select");
                currentClick = setPage(currentClick, needsNextPage ? pageNumToDisplay + 1 : REDSTAINEDGLASSNUMBER);
                nextMeta.setDisplayName(ChatColor.GREEN + "NEXT PAGE");
                if (needsNextPage) {
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GREEN + "Page " + (pageNumToDisplay + 1));
                    nextMeta.setLore(lore);
                }
                next.setItemMeta(nextMeta);
                next = newItemStackGUI(next, currentClick);
                inv.setItem(44, next);

                // EXIT, slot 53.
                ItemStack barrier = new ItemStack(Material.BARRIER);
                barrier = newItemStackGUI(barrier, setClick(previousClick, "exit"));
                inv.setItem(53, barrier);

                // New potion.
                currentClick = setClick(previousClick, "type");
                currentClick = setPage(currentClick, 0); // for all the potions
                List<Colour> colourList = newColourList();
                if (pageNumToDisplay == 0) {
                    ItemStack potionItemStack = new ItemStack(Material.POTION);
                    String newClick = newLocalized(false);
                    newClick = setClick(newClick, "type");
                    PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
                    Colour randomColour = colourList.get((new Random()).nextInt(17));
                    Color randomColor = Color.fromRGB(randomColour.getR(), randomColour.getG(), randomColour.getB());
                    meta.setColor(randomColor);
                    meta.setDisplayName(chatColorFromColor(randomColor) + "New Potion");
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Create a new custom potion from scratch.");

                    meta.setLore(lore);
                    potionItemStack.setItemMeta(meta);
                    potionItemStack = newItemStackGUI(potionItemStack, newClick);
                    inv.addItem(potionItemStack);
                }
                numPotions++;

                // All custom potions from potions.yml
                for (PotionInfo pi : allCustomPotions) {
                    if (numPotions < PAGESIZE * pageNumToDisplay) {
                        numPotions++;
                        continue;
                    }
                    ItemStack potionItemStack = pi.itemstack.clone();
                    PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.DARK_PURPLE + "Recipes:");
                    for (PotionRecipe pr : pi.potionrecipes) {
                        lore.add(ChatColor.GOLD + normaliseCapitalise(pr.ingredient.name(), "_") + " + "
                                + pr.predecessor.getItemMeta().getDisplayName() + " (" +
                                normaliseCapitalise(pr.predecessor.getType().name(), "_") + ")");
                    }
                    meta.setLore(lore);
                    meta.setDisplayName(chatColorFromColor(meta.getColor()) + meta.getDisplayName());
                    potionItemStack.setItemMeta(meta);
                    potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                    inv.addItem(potionItemStack);
                    numPotions++;
                    if (numPotions >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }
                }

                player.openInventory(inv);
                break;
            }
            case "type": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion Type");
                currentClick = setClick(currentClick, "colour");
                currentClick = setPage(currentClick, 0);
                currentClick = setType(currentClick, true);

                ItemStack potionItemStack = new ItemStack(Material.POTION);
                PotionMeta meta = (PotionMeta) existingPotion.getItemMeta();
                meta.setDisplayName(chatColorFromColor(meta.getColor()) + "Potion");
                meta.setLore(null);
                potionItemStack.setItemMeta(meta);
                potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                inv.addItem(potionItemStack);

                potionItemStack = new ItemStack(Material.SPLASH_POTION);
                meta = (PotionMeta) existingPotion.getItemMeta();
                meta.setDisplayName(chatColorFromColor(meta.getColor()) + "Splash Potion");
                meta.setLore(null);
                potionItemStack.setItemMeta(meta);
                potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                inv.addItem(potionItemStack);

                potionItemStack = new ItemStack(Material.LINGERING_POTION);
                meta = (PotionMeta) existingPotion.getItemMeta();
                meta.setDisplayName(chatColorFromColor(meta.getColor()) + "Lingering Potion");
                meta.setLore(null);
                potionItemStack.setItemMeta(meta);
                potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                inv.addItem(potionItemStack);

                currentClick = setType(currentClick, hasType(previousClick));

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, currentClick);
                inv.setItem(35, previous);

                // NEXT PAGE, slot 44.
                ItemStack next = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, currentClick);
                inv.setItem(44, next);

                // PREVIOUS MENU, slot 51.
                currentClick = setClick(currentClick, "select");
                ItemStack left = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                leftMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Potion Selection");
                lore.add(ChatColor.RED + "Warning: you will lose your unsaved changes!");
                lore.add(ChatColor.RED + "Save a completed potion to validate your changes.");
                leftMeta.setLore(lore);
                left.setItemMeta(leftMeta);
                left = newItemStackGUI(left, currentClick);
                inv.setItem(51, left);

                // NEXT MENU, slot 52.
                currentClick = setClick(currentClick, "colour");
                ItemStack right = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta rightMeta = right.getItemMeta();
                rightMeta.setDisplayName(ChatColor.GREEN + "NEXT MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Colour Selection");
                rightMeta.setLore(lore);
                right.setItemMeta(rightMeta);
                right = newItemStackGUI(right, currentClick);
                inv.setItem(52, right);

                // EXIT, slot 53.
                currentClick = setClick(currentClick, "exit");
                ItemStack currentPotion = existingPotion.clone();
                currentPotion = newItemStackGUI(currentPotion, currentClick);
                inv.setItem(53, currentPotion);

                player.openInventory(inv);
                break;
            }
            case "colour": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion Colour");
                currentClick = setClick(currentClick, "effect_type");
                currentClick = setPage(currentClick, 0);

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, setClick(currentClick, "colour"));
                inv.setItem(35, previous);

                // NEXT PAGE, slot 44.
                ItemStack next = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, setClick(currentClick, "colour"));
                inv.setItem(44, next);

                // PREVIOUS MENU, slot 51.
                ItemStack left = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                leftMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Type Selection");
                leftMeta.setLore(lore);
                left.setItemMeta(leftMeta);
                left = newItemStackGUI(left, setClick(currentClick, "type"));
                inv.setItem(51, left);

                // NEXT MENU, slot 52.
                ItemStack right = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta rightMeta = right.getItemMeta();
                rightMeta.setDisplayName(ChatColor.GREEN + "NEXT MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Effect Type(s) Selection");
                rightMeta.setLore(lore);
                right.setItemMeta(rightMeta);
                right = newItemStackGUI(right, setClick(currentClick, "effect_type"));
                inv.setItem(52, right);

                // EXIT, slot 53.
                ItemStack currentPotion = existingPotion.clone();
                currentPotion = newItemStackGUI(currentPotion, setClick(currentClick, "exit"));
                inv.setItem(53, currentPotion);

                currentClick = setColour(currentClick, true);

                // All potion colours.
                ArrayList<Colour> colourList = newColourList();
                for (Colour c : colourList) {
                    ItemStack potionItemStack = new ItemStack(existingPotion.getType());
                    PotionMeta meta = (PotionMeta) existingPotion.getItemMeta();
                    Color current = Color.fromRGB(c.getR(), c.getG(), c.getB());
                    String s = c.getName();
                    meta.setColor(current);
                    meta.setDisplayName(chatColorFromColor(meta.getColor()) + s);
                    meta.setLore(null);
                    potionItemStack.setItemMeta(meta);
                    potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                    inv.addItem(potionItemStack);
                }

                player.openInventory(inv);
                break;
            }
            case "effect_type": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select an Effect Type");
                currentClick = setClick(currentClick, "effect_dur");
                currentClick = setPage(currentClick, 0);

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, setClick(currentClick, "effect_type"));
                inv.setItem(35, previous);

                // NEXT PAGE, slot 44.
                ItemStack next = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, setClick(currentClick, "effect_type"));
                inv.setItem(44, next);

                // PREVIOUS MENU, slot 51.
                ItemStack left = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                leftMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Colour Selection");
                leftMeta.setLore(lore);
                left.setItemMeta(leftMeta);
                left = newItemStackGUI(left, setClick(currentClick, "colour"));
                inv.setItem(51, left);

                // NEXT MENU, slot 52.
                ItemStack right = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta rightMeta = right.getItemMeta();
                rightMeta.setDisplayName(ChatColor.GREEN + "NEXT MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Ingredient Selection");
                rightMeta.setLore(lore);
                right.setItemMeta(rightMeta);
                right = newItemStackGUI(right, setClick(currentClick, "ingredient"));
                inv.setItem(52, right);

                // EXIT, slot 53.
                ItemStack currentPotion = existingPotion.clone();
                currentPotion = newItemStackGUI(currentPotion, setClick(currentClick, "exit"));
                inv.setItem(53, currentPotion);

                // No potion effect.
                ItemStack potionItemStack = new ItemStack(existingPotion.getType());
                PotionMeta meta = (PotionMeta) existingPotion.getItemMeta();
                meta.setDisplayName(chatColorFromColor(meta.getColor()) + "NO EFFECTS");
                lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "This potion will have no effects.");
                meta.setLore(lore);
                potionItemStack.setItemMeta(meta);
                potionItemStack = newItemStackGUI(potionItemStack, setEffect(setClick(currentClick, "no_effects"), true));
                inv.addItem(potionItemStack);

                // All potion effect types.
                PotionEffectType[] petList = PotionEffectType.values();
                List<String> petNames = new ArrayList<>();
                for (PotionEffectType pet : petList) {
                    petNames.add(pet.getName());
                }
                Collections.sort(petNames);

                for (String petName : petNames) {
                    PotionEffectType pet = petList[0];
                    for (PotionEffectType a : petList) {
                        if (a.getName().equalsIgnoreCase(petName)) {
                            pet = a;
                            break;
                        }
                    }
                    potionItemStack = new ItemStack(existingPotion.getType());
                    meta = (PotionMeta) existingPotion.getItemMeta();
                    meta.setDisplayName(chatColorFromColor(meta.getColor()) + pet.getName());

                    boolean hasEffect = false;
                    for (PotionEffect effect: meta.getCustomEffects()) {
                        if (effect.getType() == pet) {
                            hasEffect = true;
                            break;
                        }
                    }

                    String commonName = pet.getName();
                    switch (pet.getName()) {
                        case "CONFUSION":
                            commonName = normaliseCapitalise(commonName.concat(" " + "(NAUSEA)"), "_");
                            break;
                        case "FAST_DIGGING":
                            commonName = normaliseCapitalise(commonName.concat(" " + "(HASTE)"), "_");
                            break;
                        case "INCREASE_DAMAGE":
                            commonName = normaliseCapitalise(commonName.concat(" " + "(STRENGTH)"), "_");
                            break;
                        case "SLOW_DIGGING":
                            commonName = normaliseCapitalise(commonName.concat(" " + "(MINING_FATIGUE)"), "_");
                            break;
                        default:
                            commonName = normaliseCapitalise(commonName, "_");
                    }

                    lore = new ArrayList<>();
                    lore.add("");
                    if (hasEffect) {
                        lore.add(ChatColor.GOLD + "Left click to modify " + commonName + ".");
                        lore.add(ChatColor.RED + "Right click to remove " + commonName + ".");
                        meta.setLore(lore);
                        potionItemStack.setItemMeta(meta);
                        potionItemStack = newItemStackGUI(potionItemStack, setClick(currentClick, "mixed"));
                    } else {
                        lore.add(ChatColor.GREEN + "Click to add " + commonName + ".");
                        meta.setLore(lore);
                        potionItemStack.setItemMeta(meta);
                        potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                    }

                    inv.addItem(potionItemStack);
                }

                player.openInventory(inv);
                break;
            }
            case "effect_dur": {
                currentClick = setClick(currentClick, "effect_amp");

                AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                anvil.title(ChatColor.GOLD + "Effect Duration");

                // POTION, slot 0 and 2.
                ItemStack currentPotion = existingPotion.clone();
                ItemMeta meta = currentPotion.getItemMeta();
                lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.GOLD + "Enter the effect duration (in seconds from 1 to 26,843,545).");
                lore.add(ChatColor.GOLD + "You will not be able to change menu or save in this menu.");
                lore.add(ChatColor.GOLD + "If you have misclicked, just continue.");
                lore.add(ChatColor.GREEN + "Left click the output slot to continue.");
                lore.add(ChatColor.RED + "Right click the output slot to exit without saving.");
                meta.setLore(lore);
                meta.setLocalizedName(currentClick);
                currentPotion.setItemMeta(meta);
                anvil.item(currentPotion);

                anvil.text(ChatColor.RESET + "Enter here:");
                anvil.onComplete((whoTyped, whatWasTyped) -> {
                    return AnvilGUI.Response.text("Enter here:");
                });
                anvil.plugin(this);

                anvilInv = anvil.open(player);
                break;
            }
            case "effect_amp": {
                currentClick = setClick(currentClick, "effect_type");

                AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                anvil.title(ChatColor.GOLD + "Effect Amplifier");

                // POTION, slot 0 and 2.
                ItemStack currentPotion = existingPotion.clone();
                ItemMeta meta = currentPotion.getItemMeta();
                lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.GOLD + "Enter the effect amplifier (integer from 0 to 127).");
                lore.add(ChatColor.GOLD + "0 means tier I, eg. Regeneration I.");
                lore.add(ChatColor.GOLD + "Similarly, 1 means tier II, eg. Regeneration II, and so on");
                lore.add(ChatColor.GOLD + "You will not be able to change menu or save in this menu.");
                lore.add(ChatColor.GOLD + "If you have misclicked, just continue.");
                lore.add(ChatColor.GREEN + "Left click the output slot to continue.");
                lore.add(ChatColor.RED + "Right click the output slot to exit without saving.");
                meta.setLore(lore);
                meta.setLocalizedName(currentClick);
                currentPotion.setItemMeta(meta);
                anvil.item(currentPotion);

                anvil.text(ChatColor.RESET + "Enter here:");
                anvil.onComplete((whoTyped, whatWasTyped) -> {
                    return AnvilGUI.Response.text("Enter here:");
                });
                anvil.plugin(this);

                anvilInv = anvil.open(player);
                break;
            }
            case "ingredient": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select an Ingredient");

                Material[] allMaterials = Material.values();
                List<Material> allItemMaterials = new ArrayList<>();
                for (Material material : allMaterials) {
                    if (!material.isItem() || material == Material.AIR || material == Material.POTION ||
                            material == Material.SPLASH_POTION || material == Material.LINGERING_POTION ||
                            material == Material.DEBUG_STICK || material == Material.KNOWLEDGE_BOOK) {
                    } else {
                        allItemMaterials.add(material);
                    }
                }
                int numMaterials = 0;
                int pageNumToDisplay = getPage(previousClick);
                int totalIngredients = allItemMaterials.size();
                int PAGESIZE = 49;
                boolean needsNextPage = (totalIngredients - PAGESIZE * pageNumToDisplay) > PAGESIZE;

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = pageNumToDisplay != 0 ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
                currentClick = setClick(previousClick,"ingredient");
                currentClick = setPage(currentClick, pageNumToDisplay != 0 ? pageNumToDisplay - 1 : REDSTAINEDGLASSNUMBER);
                ItemMeta prevMeta = previous.getItemMeta();
                prevMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS PAGE");
                if (pageNumToDisplay != 0) {
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Page " + (pageNumToDisplay - 1));
                    prevMeta.setLore(lore);
                }
                previous.setItemMeta(prevMeta);
                previous = newItemStackGUI(previous, currentClick);
                inv.setItem(35, previous);

                // NEXT PAGE, slot 44.
                ItemStack next = needsNextPage ? new ItemStack(Material.LIME_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta nextMeta = next.getItemMeta();
                currentClick = setClick(previousClick,"ingredient");
                currentClick = setPage(currentClick, needsNextPage ? pageNumToDisplay + 1 : REDSTAINEDGLASSNUMBER);
                nextMeta.setDisplayName(ChatColor.GREEN + "NEXT PAGE");
                if (needsNextPage) {
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GREEN + "Page " + (pageNumToDisplay + 1));
                    nextMeta.setLore(lore);
                }
                next.setItemMeta(nextMeta);
                next = newItemStackGUI(next, currentClick);
                inv.setItem(44, next);

                currentClick = setPage(previousClick,0);

                // PREVIOUS MENU, slot 51.
                ItemStack left = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                leftMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Effect Type(s) Selection");
                leftMeta.setLore(lore);
                left.setItemMeta(leftMeta);
                left = newItemStackGUI(left, setClick(currentClick, "effect_type"));
                inv.setItem(51, left);

                // NEXT MENU, slot 52.
                ItemStack right = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta rightMeta = right.getItemMeta();
                rightMeta.setDisplayName(ChatColor.GREEN + "NEXT MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Naming");
                rightMeta.setLore(lore);
                right.setItemMeta(rightMeta);
                right = newItemStackGUI(right, setClick(currentClick, "name"));
                inv.setItem(52, right);

                // EXIT, slot 53.
                ItemStack currentPotion = existingPotion.clone();
                currentPotion = newItemStackGUI(currentPotion, setClick(currentClick, "exit"));
                inv.setItem(53, currentPotion);

                currentClick = setClick(previousClick, "ingredient_chosen");
                currentClick = setPage(currentClick, 0);

                // All materials.
                for (Material material : allItemMaterials) {
                    if (numMaterials < PAGESIZE * pageNumToDisplay) {
                        numMaterials++;
                        continue;
                    }

                    ItemStack ingredientItemStack = new ItemStack(material);
                    ItemMeta meta = ingredientItemStack.getItemMeta();

                    // TODO handle whether or not it has the ingredient already
                    // TODO don't allow two with same predecessor to have same ingredient, will have to alter the
                    //      length of the allMaterials above too

                    /*boolean hasIngredient = false;
                    for (PotionEffect effect: meta.getCustomEffects()) {
                        if (effect.getType() == pet) {
                            hasEffect = true;
                            break;
                        }
                    }*/

                    lore = new ArrayList<>();
                    if (false) {
                        lore.add("");
                        lore.add(ChatColor.RED + "Click to remove " + material.name() + " and its corresponding predecessor."); // TODO add predecessor name here
                        currentClick = setClick(currentClick, "ingredient");
                    } else {
                        lore.add("");
                        lore.add(ChatColor.GREEN + "Click to add " + material.name() + ".");
                    }
                    meta.setLore(lore);

                    ingredientItemStack.setItemMeta(meta);
                    ingredientItemStack = newItemStackGUI(ingredientItemStack, currentClick);
                    inv.addItem(ingredientItemStack);

                    numMaterials++;
                    if (numMaterials >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }
                }

                player.openInventory(inv);
                break;
            }
            case "predecessor": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Predecessor");

                int numPredecessors = 0;
                int pageNumToDisplay = getPage(previousClick);
                int totalPredecessors = allCustomPotions.size() + 4;
                int PAGESIZE = 49;
                boolean needsNextPage = (totalPredecessors - PAGESIZE * pageNumToDisplay) > PAGESIZE;

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = pageNumToDisplay != 0 ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
                currentClick = setClick(previousClick,"predecessor");
                currentClick = setPage(currentClick, pageNumToDisplay != 0 ? pageNumToDisplay - 1 : REDSTAINEDGLASSNUMBER);
                ItemMeta prevMeta = previous.getItemMeta();
                prevMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS PAGE");
                if (pageNumToDisplay != 0) {
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Page " + (pageNumToDisplay - 1));
                    prevMeta.setLore(lore);
                }
                previous.setItemMeta(prevMeta);
                previous = newItemStackGUI(previous, currentClick);
                inv.setItem(35, previous);

                // NEXT PAGE, slot 44.
                ItemStack next = needsNextPage ? new ItemStack(Material.LIME_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta nextMeta = next.getItemMeta();
                currentClick = setClick(previousClick,"predecessor");
                currentClick = setPage(currentClick, needsNextPage ? pageNumToDisplay + 1 : REDSTAINEDGLASSNUMBER);
                nextMeta.setDisplayName(ChatColor.GREEN + "NEXT PAGE");
                if (needsNextPage) {
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GREEN + "Page " + (pageNumToDisplay + 1));
                    nextMeta.setLore(lore);
                }
                next.setItemMeta(nextMeta);
                next = newItemStackGUI(next, currentClick);
                inv.setItem(44, next);

                currentClick = setPage(previousClick,0);

                // PREVIOUS MENU, slot 51.
                ItemStack left = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                leftMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Ingredient Selection");
                lore.add(ChatColor.RED + "Warning: you will lose your choice of ingredient!");
                leftMeta.setLore(lore);
                left.setItemMeta(leftMeta);
                left = newItemStackGUI(left, setClick(currentClick, "ingredient"));
                inv.setItem(51, left);

                // NEXT MENU, slot 52.
                ItemStack right = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta rightMeta = right.getItemMeta();
                rightMeta.setDisplayName(ChatColor.GREEN + "NEXT MENU");
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Naming");
                rightMeta.setLore(lore);
                right.setItemMeta(rightMeta);
                right = newItemStackGUI(right, setClick(currentClick, "name"));
                inv.setItem(52, right);

                // EXIT, slot 53.
                ItemStack currentPotion = existingPotion.clone();
                currentPotion = newItemStackGUI(currentPotion, setClick(currentClick, "exit"));
                inv.setItem(53, currentPotion);

                // 4 elementary potions.
                currentClick = setClick(previousClick, "ingredient");
                currentClick = setPage(currentClick, 0); // for all the potions
                List<Colour> colourList = newColourList();
                if (pageNumToDisplay == 0) {
                    List<PotionType> elementaries = new ArrayList<>();
                    elementaries.add(PotionType.WATER);
                    elementaries.add(PotionType.AWKWARD);
                    elementaries.add(PotionType.MUNDANE);
                    elementaries.add(PotionType.THICK);
                    for (PotionType elementary : elementaries) {
                        ItemStack potionItemStack = new ItemStack(Material.POTION);
                        PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
                        PotionData data = new PotionData(elementary);
                        meta.setBasePotionData(data);
                        meta.setLore(null);
                        potionItemStack.setItemMeta(meta);
                        potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                        inv.addItem(potionItemStack);
                    }
                }
                numPredecessors += 4;

                // All custom potions from potions.yml.
                for (PotionInfo pi : allCustomPotions) {
                    // TODO need to remove the potion itself if from potions.yml or at least handle what happens when you make live edits and it screws with itself
                    if (numPredecessors < PAGESIZE * pageNumToDisplay) {
                        numPredecessors++;
                        continue;
                    }
                    ItemStack potionItemStack = pi.itemstack.clone();
                    PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.DARK_PURPLE + "Recipes:");
                    for (PotionRecipe pr : pi.potionrecipes) {
                        lore.add(ChatColor.GOLD + normaliseCapitalise(pr.ingredient.name(), "_") + " + "
                                + elementaryName((PotionMeta) pr.predecessor.getItemMeta()) + " (" +
                                normaliseCapitalise(pr.predecessor.getType().name(), "_") + ")");
                    }
                    meta.setLore(lore);
                    meta.setDisplayName(chatColorFromColor(meta.getColor()) + meta.getDisplayName());
                    potionItemStack.setItemMeta(meta);
                    potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                    inv.addItem(potionItemStack);
                    numPredecessors++;
                    if (numPredecessors >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }
                }

                player.openInventory(inv);
                break;
            }
            case "name": {
                currentClick = setClick(previousClick, "final");
                currentClick = setName(currentClick, true);

                AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                anvil.title(ChatColor.GOLD + "Enter a Name");

                // TODO if potion is incomplete, take them to first empty menu (ie. not not)

                // POTION, slot 0 and 2.
                ItemStack currentPotion = existingPotion.clone();
                ItemMeta meta = currentPotion.getItemMeta();
                lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.GOLD + "The potion name must be unique.");
                lore.add(ChatColor.GOLD + "You will have the option to review changes in the next menu.");
                lore.add(ChatColor.GREEN + "Left click the output slot to continue.");
                lore.add(ChatColor.RED + "Right click the output slot to exit without saving.");
                meta.setLore(lore);
                meta.setLocalizedName(currentClick);
                currentPotion.setItemMeta(meta);
                anvil.item(currentPotion);

                anvil.text(ChatColor.RESET + "Enter here:");
                anvil.onComplete((whoTyped, whatWasTyped) -> {
                    // TODO check if potion already exists with the name
                    return AnvilGUI.Response.text("Enter here:");
                });
                anvil.plugin(this);

                anvilInv = anvil.open(player);
                break;
            }
            case "final":
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Confirm Changes?");
                // TODO slot 23 modify; slot 24 exit without saving; slot 25 save and exit
                // TODO this is all very scrappy temp code. need to fix
                ItemStack currentPotion = existingPotion.clone();
                currentClick = setClick(previousClick, "complete");
                PotionMeta meta = (PotionMeta) currentPotion.getItemMeta();
                lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.DARK_PURPLE + "Recipes:");
                for (PotionRecipe pr : potionRecipesInput) {
                    lore.add(ChatColor.GOLD + normaliseCapitalise(pr.ingredient.name(), "_") + " + "
                            + elementaryName((PotionMeta) pr.predecessor.getItemMeta()) + " (" +
                            normaliseCapitalise(pr.predecessor.getType().name(), "_") + ")");
                }
                meta.setLore(lore);
                meta.setDisplayName(chatColorFromColor(meta.getColor()) + meta.getDisplayName());
                currentPotion.setItemMeta(meta);
                currentPotion = newItemStackGUI(currentPotion, currentClick);

                inv.setItem(22, currentPotion);
                player.openInventory(inv);
                break;
        }


        // Test to generate 18 potions (a couple times).
        /*for (int counter = 0; counter < 6; counter++) {
            ItemStack potion = new ItemStack((Material.LINGERING_POTION));
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            if (meta == null) {
                this.log.severe("Unknown error. Skipping the potion.");
                return true;
            }

            ArrayList<Colour> colourList = newColourList();

            Colour h = colourList.get((new Random()).nextInt(17));
            Color k = Color.fromRGB(h.getR(), h.getG(), h.getB());
            meta.setColor(k);
            String j = "Jimbering" + counter;
            meta.setDisplayName(ChatColor.RESET + j);
            potion.setItemMeta(meta);

            potionData.getData().set(j + ".type", potion.getType().toString().toUpperCase());
            potionData.getData().set(j + ".colour.name", h.getName());
            potionData.getData().set(j + ".colour.red", h.getR());
            potionData.getData().set(j + ".colour.green", h.getG());
            potionData.getData().set(j + ".colour.blue", h.getB());
            potionData.getData().set(j + ".effects", "none");
            potionData.getData().set(j + ".predecessors.Awkward Potion.type", "POTION");
            potionData.getData().set(j + ".predecessors.Awkward Potion.effects", "none");
            potionData.getData().set(j + ".predecessors.Awkward Potion.ingredient", "SOUL_SAND");

            potion = new ItemStack(Material.POTION);
            meta = (PotionMeta) potion.getItemMeta();
            if (meta == null) {
                this.log.severe("Unknown error. Skipping the potion.");
                return true;
            }

            meta.addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 0), true);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 1), true);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 2), true);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 60, 3), true);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 4), true);

            int i = 0;
            for (Colour c : colourList) {
                Color current = Color.fromRGB(c.getR(), c.getG(), c.getB());
                String s = c.getName() + counter;
                meta.setColor(current);
                meta.setDisplayName(ChatColor.RESET + s);
                potion.setItemMeta(meta);

                potionData.getData().set(s + ".type", potion.getType().toString().toUpperCase());
                potionData.getData().set(s + ".colour.name", c.getName());
                potionData.getData().set(s + ".colour.red", c.getR());
                potionData.getData().set(s + ".colour.green", c.getG());
                potionData.getData().set(s + ".colour.blue", c.getB());

                for (PotionEffect effect : ((PotionMeta) potion.getItemMeta()).getCustomEffects()) {
                    potionData.getData().set(s + ".effects." + effect.getType().getName() + ".duration", effect.getDuration());
                    potionData.getData().set(s + ".effects." + effect.getType().getName() + ".amplifier", effect.getAmplifier());
                }

                potionData.getData().set(s + ".predecessors.Jimbering" + counter + ".type", "LINGERING_POTION");
                potionData.getData().set(s + ".predecessors.Jimbering" + counter + ".effects", "none");
                potionData.getData().set(s + ".predecessors.Jimbering" + counter + ".ingredient", "BUCKET");
                i++;
                if (i >= 53) break;
            }
            potionData.saveData();
        }*/

        return false;
    }

    // Modifying potions on command line for console.
    public boolean consoleModifyPotions (CommandSender sender) {
        return false;
    }

    // Give potions to players.
    public boolean givePotions(CommandSender sender, String potionName, String quantity) {
        int num;
        ItemStack potion = null;
        try {
            num = Integer.parseInt(quantity);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + quantity + " is not a valid integer quantity.");
            return true;
        }
        for (PotionInfo customPotion : allCustomPotions) {
            if (customPotion.name.equalsIgnoreCase(potionName)) {
                potion = customPotion.itemstack;
            }
        }
        if (potion == null) {
            sender.sendMessage(ChatColor.RED + potionName + " was not found in the list of custom potions.");
            return true;
        }
        while (num > 0) {
            ((Player) sender).getInventory().addItem(potion);
            num--;
        }
        return false;
    }

    /*******************************************************************************************************************
    *                                                      EVENTS                                                      *
    *******************************************************************************************************************/

    // Inserting an item into a brewing stand.
    @EventHandler(priority = EventPriority.LOW)
    public void insertBrewingItem(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getInventory().getType() != InventoryType.BREWING) {
            return;
        }

        boolean cancel = false;
        switch (event.getAction()) {
            case MOVE_TO_OTHER_INVENTORY: {
                ItemStack insertIngredient = event.getCurrentItem();
                if (insertIngredient == null) {
                    return;
                }
                if (insertIngredient.getType() == Material.POTION || insertIngredient.getType() == Material.SPLASH_POTION || insertIngredient.getType() == Material.LINGERING_POTION) {
                    return;
                }
                if (event.getClickedInventory().getType() == InventoryType.BREWING) {
                    return;
                }
                BrewerInventory brewingStand = (BrewerInventory) event.getInventory();
                ItemStack existingIngredient = brewingStand.getIngredient();
                // TODO for each ingredient in the hashmap of custom recipes, if item matches any, add item to the brewing stand

                if (existingIngredient == null) {
                    // Ingredient slot is empty, insert item stack directly.
                    brewingStand.setIngredient(insertIngredient);
                    event.getClickedInventory().clear(event.getSlot());
                    cancel = true;
                } else if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    // Ingredient slot matches, insert item stack until ingredient slot full, or source empty.
                    while (brewingStand.getIngredient().getAmount() < brewingStand.getIngredient().getMaxStackSize() && insertIngredient.getAmount() > 0) {
                        existingIngredient.setAmount(existingIngredient.getAmount() + 1);
                        brewingStand.setIngredient(existingIngredient);
                        insertIngredient.setAmount(insertIngredient.getAmount() - 1);

                        // Prevents moving remaining ingredients to the other side (main to hotbar, or hotbar to main).
                        cancel = true;
                    }
                }
                break;
            }
            case PICKUP_ALL:
                if (!event.getClick().equals(ClickType.LEFT)) {
                    break;
                }
            case PLACE_ALL: {
                ItemStack insertIngredient = event.getCursor();
                if (insertIngredient == null) {
                    return;
                }
                if (insertIngredient.getType() == Material.POTION || insertIngredient.getType() == Material.SPLASH_POTION || insertIngredient.getType() == Material.LINGERING_POTION) {
                    return;
                }
                if (event.getClickedInventory().getType() != InventoryType.BREWING) {
                    return;
                }
                if (event.getSlotType() != InventoryType.SlotType.FUEL) {
                    return;
                }

                BrewerInventory brewingStand = (BrewerInventory) event.getInventory();
                ItemStack existingIngredient = brewingStand.getIngredient();
                // TODO for each ingredient in the hashmap of custom recipes, if item matches any, add item to the brewing stand

                if (existingIngredient == null) {
                    // Ingredient slot is empty, insert item stack directly.
                    brewingStand.setIngredient(insertIngredient);

                    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        @Override
                        public void run() {
                            event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                        }
                    }, 0L);
                } else if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    // Ingredient slot matches, insert item stack until ingredient slot full, or source empty.
                    int resultantStackSize = insertIngredient.getAmount() + existingIngredient.getAmount();
                    if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                        // Combined stack can be inserted in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(resultantStackSize);

                        // Insert ingredient and set cursor to nothing.
                        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                            }
                        }, 0L);
                    } else {
                        // Combined stack is too large to be placed in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(existingIngredient.getMaxStackSize());

                        // Insert ingredient and set cursor to resultant stack size.
                        ItemStack toCursor = insertIngredient.clone();
                        toCursor.setAmount(resultantStackSize - existingIngredient.getMaxStackSize());
                        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.getWhoClicked().setItemOnCursor(toCursor);
                            }
                        }, 0L);
                    }
                } else {
                    // Ingredient slot does not match, swap cursor with existing ingredient.
                    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        @Override
                        public void run() {
                            brewingStand.setIngredient(insertIngredient);
                            event.getWhoClicked().setItemOnCursor(existingIngredient);
                        }
                    }, 0L);
                }
                cancel = true;
                break;
            }
            case NOTHING: {
                // All cases where an item would be inserted into the ingredient slot but Minecraft does not register
                // it as a valid item, therefore returning a "NOTHING" action type.
                ItemStack insertIngredient = event.getCursor();
                if (insertIngredient == null) {
                    return;
                }
                if (insertIngredient.getType() == Material.POTION || insertIngredient.getType() == Material.SPLASH_POTION || insertIngredient.getType() == Material.LINGERING_POTION) {
                    return;
                }
                if (event.getClickedInventory().getType() != InventoryType.BREWING) {
                    return;
                }
                if (event.getSlotType() != InventoryType.SlotType.FUEL) {
                    return;
                }

                BrewerInventory brewingStand = (BrewerInventory) event.getInventory();
                ItemStack existingIngredient = brewingStand.getIngredient();
                // TODO for each ingredient in the hashmap of custom recipes, if item matches any, add item to the brewing stand
                if (existingIngredient == null) {
                    return;
                }

                if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    if (event.getClick().equals(ClickType.LEFT)) {
                        int resultantStackSize = insertIngredient.getAmount() + existingIngredient.getAmount();

                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(existingIngredient.getMaxStackSize());

                        // Insert ingredient and set cursor to resultant stack size.
                        ItemStack toCursor = insertIngredient.clone();
                        toCursor.setAmount(resultantStackSize - existingIngredient.getMaxStackSize());

                        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.getWhoClicked().setItemOnCursor(toCursor);
                            }
                        }, 0L);
                    } else if (event.getClick().equals(ClickType.RIGHT)) {
                        int resultantStackSize = existingIngredient.getAmount() + 1;
                        if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                            ItemStack toInsert = insertIngredient.clone();
                            toInsert.setAmount(resultantStackSize);

                            // Insert ingredient and set cursor to one less.
                            ItemStack ingredientMinusOne = insertIngredient.clone();
                            ingredientMinusOne.setAmount(insertIngredient.getAmount() - 1);

                            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                @Override
                                public void run() {
                                    brewingStand.setIngredient(toInsert);
                                    event.getWhoClicked().setItemOnCursor(ingredientMinusOne);
                                }
                            }, 0L);
                        }
                    }
                } else {
                    // Swap cursor with existing ingredient.
                    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        @Override
                        public void run() {
                            brewingStand.setIngredient(insertIngredient);
                            event.getWhoClicked().setItemOnCursor(existingIngredient);
                        }
                    }, 0L);
                }
                cancel = true;
            }
        }

        switch (event.getAction()) {
            case PICKUP_ALL:
                if (!event.getClick().equals(ClickType.RIGHT)) {
                    break;
                }
            case PLACE_ONE:
                ItemStack insertIngredient = event.getCursor();
                if (insertIngredient == null) {
                    return;
                }
                if (insertIngredient.getType() == Material.POTION || insertIngredient.getType() == Material.SPLASH_POTION || insertIngredient.getType() == Material.LINGERING_POTION) {
                    return;
                }
                if (event.getClickedInventory().getType() != InventoryType.BREWING) {
                    return;
                }
                if (event.getSlotType() != InventoryType.SlotType.FUEL) {
                    return;
                }

                BrewerInventory brewingStand = (BrewerInventory) event.getInventory();
                ItemStack existingIngredient = brewingStand.getIngredient();
                // TODO for each ingredient in the hashmap of custom recipes, if item matches any, add item to the brewing stand

                if (existingIngredient == null) {
                    // Ingredient slot is empty, insert single item.
                    ItemStack oneIngredient = insertIngredient.clone();
                    oneIngredient.setAmount(1);
                    brewingStand.setIngredient(oneIngredient);

                    ItemStack ingredientMinusOne = insertIngredient.clone();
                    ingredientMinusOne.setAmount(insertIngredient.getAmount() - 1);

                    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        @Override
                        public void run() {
                            event.getWhoClicked().setItemOnCursor(ingredientMinusOne);
                        }
                    }, 0L);
                } else if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    // Ingredient slot matches, insert single item.
                    int resultantStackSize = existingIngredient.getAmount() + 1;
                    if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                        // Combined stack can be inserted in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(resultantStackSize);

                        // Insert ingredient and set cursor to one less.
                        ItemStack ingredientMinusOne = insertIngredient.clone();
                        ingredientMinusOne.setAmount(insertIngredient.getAmount() - 1);

                        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.getWhoClicked().setItemOnCursor(ingredientMinusOne);
                            }
                        }, 0L);
                    }
                }
                cancel = true;
                break;
        }

        // Allows moving ingredients to other side when brewing stand is full of ingredients.
        if (cancel) {
            event.setCancelled(true);
        }

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                ((Player)event.getWhoClicked()).updateInventory();
            }
        }, 2L);
    }

    @EventHandler
    public void clickGUI(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();
        // TODO prevent placing in the custom inventories, including dragging

        // Fails if:
        // - clicked is an invalid item
        // - inventory is not the custom 54-slot and not the custom anvil
        // Succeeds if:
        // - clicked is valid AND inventory is the custom 54-slot
        // - clicked is valid AND inventory is not the custom 54-slot BUT is the custom anvil
        if (clicked == null || clicked.getItemMeta() == null || inventory == null) {
            return;
        } else if (inventory == inv) {
            // continue
        } else if (anvilInv == null || inventory != anvilInv.getInventory()) {
            return;
        }

        String localizedName = clicked.getItemMeta().getLocalizedName();

        event.setCancelled(true);
        if (!isPotionClick(localizedName)) {
            this.log.warning("Unknown error X1.");
            return;
        }
        this.isInvOpened = false;

        boolean isPotion = clicked.getType() == Material.POTION || clicked.getType() == Material.SPLASH_POTION
                || clicked.getType() == Material.LINGERING_POTION;
        boolean isSelectPage = getClick(localizedName).equals("select");
        boolean isLingeringPotion = clicked.getType() == Material.LINGERING_POTION;

        if (inventory == inv || inventory.toString().equals(inv.toString())) {
            // 54-slot inventory.

            // TODO optimise these clicks..... each one should end in its own return.

            // Invalid attempt to change page (red glass).
            if (clicked.getType() == Material.RED_STAINED_GLASS_PANE && getPage(localizedName) == REDSTAINEDGLASSNUMBER) {
                this.isInvOpened = true;
                return;
            }

            // Leave via slot 53 exit.
            if (getClick(localizedName).equals("exit")) {
                if (clicked.getType() == Material.BARRIER || event.getClick() == ClickType.RIGHT) {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                    resetGlobals();
                } else if (event.getClick() == ClickType.LEFT) {
                    // TODO save the existing data, use isCompletePotion
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                    resetGlobals();
                }
                return;
            }

            // On select menu, existing potion name is set to the clicked name.
            if (getClick(localizedName).equals("type") && isPotion) this.potionNameInput = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            // Must be on effect_type menu, set the global to the type.
            if (getClick(localizedName).equals("effect_dur")) this.effectTypeInput = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            // On effect_type menu, either modify or remove effect.
            if (getClick(localizedName).equals("mixed")) {
                this.isInvOpened = true;
                if (event.getClick() == ClickType.LEFT) { // Modify.
                    this.effectTypeInput = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                    ItemMeta modifyMeta = clicked.getItemMeta();
                    modifyMeta.setLocalizedName(setClick(localizedName, "effect_dur"));
                    clicked.setItemMeta(modifyMeta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                } else if (event.getClick() == ClickType.RIGHT) { // Remove
                    PotionMeta removeMeta = ((PotionMeta) clicked.getItemMeta());
                    removeMeta.removeCustomEffect(PotionEffectType.getByName(ChatColor.stripColor(removeMeta.getDisplayName())));

                    /*// If the potion has no effects as a result of the removal, go to ingredient menu.
                    if (!removeMeta.hasCustomEffects()) {
                        removeMeta.setLocalizedName(setClick(localizedName, "ingredient"));
                    } else {
                        removeMeta.setLocalizedName(setClick(localizedName, "effect_type"));
                    }*/

                    removeMeta.setLocalizedName(setClick(localizedName, "effect_type"));
                    clicked.setItemMeta(removeMeta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                }
                return;
            }

            // No Effects selected in effect_type menu.
            if (getClick(localizedName).equals("no_effects")) {
                PotionMeta removeMeta = ((PotionMeta) clicked.getItemMeta());
                removeMeta.clearCustomEffects();
                removeMeta.setLocalizedName(setClick(localizedName, "ingredient"));
                clicked.setItemMeta(removeMeta);
                playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                return;
            }

            // On ingredients menu, having selected a material as an ingredient.
            if (getClick(localizedName).equals("ingredient_chosen")) {
                this.ingredientInput = clicked.getType();
                ItemMeta chosenMeta = clicked.getItemMeta();
                chosenMeta.setLocalizedName(setClick(localizedName, "predecessor"));
                clicked.setItemMeta(chosenMeta);
                playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                return;
            }

            // On predecessor menu, having opted to return to the ingredient menu.
            if (getClick(localizedName).equals("previous_menu_ingredient")) {
                this.ingredientInput = null;
                ItemMeta newRecipeMeta = clicked.getItemMeta();
                newRecipeMeta.setLocalizedName(setClick(localizedName, "ingredient"));
                clicked.setItemMeta(newRecipeMeta);
                playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                return;
            }

            // On predecessor menu, having selected a potion as a predecessor.
            if (getClick(localizedName).equals("ingredient") && isPotion) {
                ItemMeta newRecipeMeta = clicked.getItemMeta();
                newRecipeMeta.setLocalizedName(setRecipe(localizedName, true));
                clicked.setItemMeta(newRecipeMeta);
                ItemStack chosenPredecessor = clicked.clone();
                newRecipeMeta.setLocalizedName(null);
                newRecipeMeta.setDisplayName(ChatColor.stripColor(newRecipeMeta.getDisplayName()));
                newRecipeMeta.setLore(null);
                chosenPredecessor.setItemMeta(newRecipeMeta);
                this.potionRecipesInput.add(new PotionRecipe(chosenPredecessor, ingredientInput));
                this.ingredientInput = null;
                playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                return;
            }

            // Adding newly created / modified potion to potions.yml.
            // TODO remove old name potion ooo this would be how you can remove a potion itself from predecessor
            if (getClick(localizedName).equals("complete")) {
                String newName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                PotionMeta newMeta = ((PotionMeta) clicked.getItemMeta());
                Color newColor = newMeta.getColor();
                boolean isLingering = clicked.getType() == Material.LINGERING_POTION;

                // Type + Colour.
                potionData.getData().set(newName + ".type", clicked.getType().toString().toUpperCase());
                potionData.getData().set(newName + ".colour.name", colourNameFromColor(newColor));
                potionData.getData().set(newName + ".colour.red", newColor.getRed());
                potionData.getData().set(newName + ".colour.green", newColor.getGreen());
                potionData.getData().set(newName + ".colour.blue", newColor.getBlue());

                // Effects.
                if (newMeta.hasCustomEffects()) {
                    List<PotionEffect> newPotionEffects = newMeta.getCustomEffects();
                    for (PotionEffect newPotionEffect : newPotionEffects) {
                        String s = newName + ".effects." + newPotionEffect.getType().getName();
                        potionData.getData().set(s + ".duration",
                                isLingering ? newPotionEffect.getDuration() / 80 : newPotionEffect.getDuration() / 20);
                        potionData.getData().set(s + ".amplifier", newPotionEffect.getAmplifier());
                    }
                } else {
                    potionData.getData().set(newName + ".effects", "none");
                }

                // Predecessors.
                for (PotionRecipe newPotionRecipe : this.potionRecipesInput) {
                    PotionMeta preMeta = ((PotionMeta) newPotionRecipe.predecessor.getItemMeta());
                    boolean isPreLingering = newPotionRecipe.predecessor.getType() == Material.LINGERING_POTION;
                    String s = newName + ".predecessors." + elementaryName(preMeta);
                    potionData.getData().set(s + ".type", newPotionRecipe.predecessor.getType().name().toUpperCase());
                    if (preMeta.hasCustomEffects()) {
                        List<PotionEffect> prePotionEffects = preMeta.getCustomEffects();
                        for (PotionEffect prePotionEffect : prePotionEffects) {
                            String t = s + ".effects." + prePotionEffect.getType().getName();
                            potionData.getData().set(t + ".duration",
                                    isPreLingering ? prePotionEffect.getDuration() / 80 : prePotionEffect.getDuration() / 20);
                            potionData.getData().set(t + ".amplifier", prePotionEffect.getAmplifier());
                        }
                    } else {
                        potionData.getData().set(s + ".effects", "none");
                    }
                    potionData.getData().set(s + ".ingredient", newPotionRecipe.ingredient.name());
                }

                potionData.saveData();
                // TODO add to allCustomPotions
                resetGlobals();

                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(ChatColor.GREEN + "Your changes to " + newName + " have been saved.");
                return;
            }

            // continue to end

        } else {
            // Anvil inventory.

            // Not the output slot. anvilInv remains unchanged.
            if (event.getSlot() != AnvilGUI.Slot.OUTPUT) {
                this.isInvOpened = true;
                return;
            }

            // Clicking the output slot.
            if (getClick(localizedName).equals("effect_amp") || getClick(localizedName).equals("effect_type")) {
                if (event.getClick() == ClickType.RIGHT) { // Right click (exit).
                    anvilInv.closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                    resetGlobals();
                    return;
                } else if (event.getClick() == ClickType.LEFT) { // Left click.
                    if (getClick(localizedName).equals("effect_amp")) { // Currently using effect_dur menu.
                        effectDurationInput = clicked.getItemMeta().getDisplayName();
                        int dur;
                        try {
                            dur = Integer.parseInt(effectDurationInput);
                        } catch (Exception e) { // Not an integer.
                            this.isInvOpened = true;
                            effectDurationInput = null;
                            return;
                        }

                        this.log.info(localizedName);
                        this.log.info("type: " + (effectTypeInput));
                        this.log.info("dur: " + (effectDurationInput));
                        this.log.info("amp: " + (effectAmplifierInput));
                        this.log.info("name: " + (potionNameInput));

                        this.isInvOpened = true;
                        if (dur < 1 || dur > 26843545) { // Not in range.
                            effectDurationInput = null;
                        } else { // effectDurationInput is valid.
                            playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                        }
                        return;
                    } else { // Currently on effect_amp menu.
                        effectAmplifierInput = clicked.getItemMeta().getDisplayName();
                        int amp;
                        try {
                            amp = Integer.parseInt(effectAmplifierInput);
                        } catch (Exception e) { // Not an integer.
                            this.isInvOpened = true;
                            effectAmplifierInput = null;
                            return;
                        }

                        this.log.info(localizedName);
                        this.log.info("type: " + (effectTypeInput));
                        this.log.info("dur: " + (effectDurationInput));
                        this.log.info("amp: " + (effectAmplifierInput));
                        this.log.info("name: " + (potionNameInput));

                        this.isInvOpened = true;
                        if (amp < 0 || amp > 127) { // Not in range.
                            effectAmplifierInput = null;
                        } else {
                            PotionMeta newEffectMeta = ((PotionMeta) clicked.getItemMeta());
                            PotionEffectType newType = PotionEffectType.getByName(effectTypeInput);
                            int newDur = isLingeringPotion ? Integer.parseInt(effectDurationInput) * 80: Integer.parseInt(effectDurationInput) * 20;
                            int newAmp = Integer.parseInt(effectAmplifierInput);
                            newEffectMeta.addCustomEffect(new PotionEffect(newType, newDur, newAmp), true);
                            newEffectMeta.setLocalizedName(setEffect(localizedName, true));
                            clicked.setItemMeta(newEffectMeta);
                            effectTypeInput = null;
                            effectDurationInput = null;
                            effectAmplifierInput = null;
                            playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                        }
                        return;
                    }
                } else { // Any other click (invalid here).
                    this.isInvOpened = true;
                    return;
                }
            } else if (getClick(localizedName).equals("final")) {
                if (event.getClick() == ClickType.RIGHT) { // Right click (exit).
                    anvilInv.closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                    resetGlobals();
                    return;
                } else if (event.getClick() == ClickType.LEFT) { // Left click.
                    potionNameInput = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

                    this.log.info(localizedName);
                    this.log.info("type: " + (effectTypeInput));
                    this.log.info("dur: " + (effectDurationInput));
                    this.log.info("amp: " + (effectAmplifierInput));
                    this.log.info("name: " + (potionNameInput));

                    this.isInvOpened = true;
                    PotionMeta namedMeta = ((PotionMeta) clicked.getItemMeta());
                    namedMeta.setLocalizedName(setName(localizedName, true));
                    namedMeta.setDisplayName(potionNameInput);
                    clicked.setItemMeta(namedMeta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                } else { // Any other click (invalid here).
                    this.isInvOpened = true;
                    return;
                }
            } else {
                this.log.warning("Unknown error X2.");
            }
        }

        this.log.info(localizedName);
        this.log.info("type: " + (effectTypeInput));
        this.log.info("dur: " + (effectDurationInput));
        this.log.info("amp: " + (effectAmplifierInput));
        this.log.info("name: " + (potionNameInput));

        // Continued to end (GUI was not exited).
        this.isInvOpened = true;

        if (isPotion) { // Pass the potion that was just clicked.
            playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
        } else if (isSelectPage) { // Pass null so the localized names are reinitialised.
            playerModifyPotions((Player) event.getWhoClicked(), null, clicked);
        } else { // Pass the potion which will be in the bottom right hand corner.
            playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
        }
    }

}