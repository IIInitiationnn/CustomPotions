package custompotions;

import org.apache.commons.lang.StringUtils;
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
    public List<ItemStack> allVanillaPotions = newVanillaPotionsList();

    // TODO two pots cant have same name (case sensitive), and no two with same predecessor can have same ingredient
    //  potentially playing around with lore for potion effects to show the correct potency, and time in day:hour:minute:second if applicable
    //  maybe in the distant future add presets like splash from existing or whatever
    //  handle weird gunpowder and dragons breath shenanigans??
    //  when closing inventory, set invOpened to false, then when opening inventory, set invOpened to true
    //  maybe allow use of colour codes in potion names and do away with colours from potion colour, let it represent the name
    //  two predecessors cannot have same ingredients (need to do in helperRecipe, in predecessor menu)
    //  right click potion in select menu to clone it

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
    ItemStack potionFromMemory = null;

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
        this.isInvOpened = false; // TODO maybe set it to the player who has it opened, that way you can close it.
        this.potionFromMemory = null;
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

    // Given a predecessor potion's metadata, return an appropriate string matching its name.
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

    // Given a potion effect's in-game name, return its common name.
    public String toCommonName(String effectName) {
        switch (effectName) {
            case "CONFUSION":
                return "Nausea";
            case "DAMAGE_RESISTANCE":
                return "Resistance";
            case "FAST_DIGGING":
                return "Haste";
            case "HARM":
                return "Instant Damage";
            case "HEAL":
                return "Instant Health";
            case "INCREASE_DAMAGE":
                return "Strength";
            case "JUMP":
                return "Jump Boost";
            case "SLOW_DIGGING":
                return "Mining Fatigue";
            default:
                return normaliseCapitalise(effectName, "_");
        }
    }

    // Given a potion effect, return its maximum amplifier.
    public int maxAmp(String effectName) {
        switch (effectName) {
            case "BAD_OMEN":
            case "HERO_OF_THE_VILLAGE":
                return 9;
            case "BLINDNESS":
            case "CONFUSION":
            case "DOLPHINS_GRACE":
            case "FIRE_RESISTANCE":
            case "GLOWING":
            case "INVISIBILITY":
            case "NIGHT_VISION":
            case "SLOW_FALLING":
            case "WATER_BREATHING":
                return 0;
            case "DAMAGE_RESISTANCE":
                return 4;
            case "HARM":
            case "HEAL":
            case "REGENERATION":
            case "POISON":
            case "WITHER":
                return 31;
            default:
                return 127;
        }
    }

    // Reset localized name of an ItemStack to null.
    public ItemStack resetLocalizedName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            this.log.severe("Unknown error X26.");
            return null;
        }
        meta.setLocalizedName(null);
        item.setItemMeta(meta);
        return item;
    }

    // Set localized name of an ItemStack.
    public ItemStack setLocalizedName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            this.log.severe("Unknown error X33.");
            return null;
        }
        meta.setLocalizedName(name);
        item.setItemMeta(meta);
        return item;
    }

    // Reset lore of an ItemStack to null.
    public ItemStack resetLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            this.log.severe("Unknown error X26.");
            return null;
        }
        meta.setLore(null);
        item.setItemMeta(meta);
        return item;
    }

    // Gets display name of custom potion, or type of Vanilla potion.
    public String getDisplayName(ItemStack item) {
        if (!isPotion(item.getType())) {
            return null;
        }
        if (item.getItemMeta() == null) {
            this.log.severe("Unknown error X39.");
            return null;
        }

        // Custom potion.
        if (item.getItemMeta().hasDisplayName()) return item.getItemMeta().getDisplayName() +
                " (" + normaliseCapitalise(item.getType().name(), "_") + ")";

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        boolean isRegular = item.getType() == Material.POTION;
        boolean isSplash = item.getType() == Material.SPLASH_POTION;
        boolean isLingering = item.getType() == Material.LINGERING_POTION;

        // Vanilla potion.
        switch (meta.getBasePotionData().getType()) {
            case WATER:
                if (isRegular) return "Water Bottle";
                else if (isSplash) return "Splash Water Bottle";
                else return "Lingering Water Bottle";
            case AWKWARD:
                return "Awkward " + normaliseCapitalise(item.getType().name(), "_");
            case MUNDANE:
                return "Mundane " + normaliseCapitalise(item.getType().name(), "_");
            case THICK:
                return "Thick " + normaliseCapitalise(item.getType().name(), "_");
            default:
                StringBuilder name = new StringBuilder();
                name.append(normaliseCapitalise(item.getType().name(), "_"));
                name.append(" of ");
                name.append(normaliseCapitalise(meta.getBasePotionData().getType().name(), "_"));
                if (meta.getBasePotionData().isExtended()) name.append(" (Extended)");
                if (meta.getBasePotionData().isUpgraded()) name.append(" (Stronger)");
                return name.toString();
        }
    }

    // Set display name of ItemStack.
    public ItemStack setDisplayName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            this.log.severe("Unknown error X27.");
            return null;
        }
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    // Determine whether a material is a type of potion.
    public boolean isPotion(Material material) {
        return material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION;
    }

    // Add predecessors and ingredients to lore.
    public List<String> addRecipes(List<String> lore, List<PotionRecipe> recipes) {
        if (recipes.size() > 0) {
            lore.add("");
            lore.add(ChatColor.GOLD + "Recipes:");
        } else {
            return lore;
        }

        for (PotionRecipe recipe : recipes) {
            if (recipe.predecessor.getItemMeta() == null) {
                this.log.severe("Unknown error X10.");
                return null;
            }
            lore.add(ChatColor.GOLD + normaliseCapitalise(recipe.ingredient.name(), "_") + " + "
                    + ChatColor.stripColor(getDisplayName(recipe.predecessor)));
        }
        return lore;
    }

    // Imprint Color of src onto dest.
    public ItemStack imprintMeta(ItemStack dest, ItemStack src) {
        if (!isPotion(dest.getType()) || !isPotion(src.getType())) {
            return null;
        }
        PotionMeta destMeta = (PotionMeta) dest.getItemMeta();
        PotionMeta srcMeta = (PotionMeta) src.getItemMeta();
        if (destMeta == null || srcMeta == null) {
            this.log.severe("Unknown error X31.");
            return null;
        }
        dest.setItemMeta(srcMeta);
        return dest;
    }

    // Returns if two potions are the same (display name and effects).
    public boolean potionsAreSame(ItemStack potion1, ItemStack potion2) {
        return resetLocalizedName(resetLore(potion1)).isSimilar(resetLocalizedName(resetLore(potion2)));
    }

    // Determines if an item is a valid ingredient.
    public boolean isValidIngredient(Material material) {
        return material.isItem() && material != Material.AIR && material != Material.POTION &&
                material != Material.SPLASH_POTION && material != Material.LINGERING_POTION &&
                material != Material.DEBUG_STICK && material != Material.KNOWLEDGE_BOOK;
    }

    // Convert number to Roman numerals for use in custom lore.
    public String intToRoman(int num) {
        int[] values = {1, 4, 5, 9, 10, 40, 50, 90, 100};
        String[] roman = {"I", "IV", "V", "IX", "X", "XL", "L", "XC", "C"};
        StringBuilder sb = new StringBuilder();

        for (int i = values.length - 1; i >= 0 && num > 0; i--) {
            while (num >= values[i]) {
                num -= values[i];
                sb.append(roman[i]);
            }
        }
        return sb.toString();
    }

    // Print all useful information.
    public void printDebug(String localizedName) {
        this.log.info("localizedName: " + localizedName);
        this.log.info("type: " + effectTypeInput);
        this.log.info("dur: " + effectDurationInput);
        this.log.info("amp: " + effectAmplifierInput);
        this.log.info("name: " + potionNameInput);
        this.log.info("size of potionrecipes: " + potionRecipesInput.size());
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

    // Given a Color, return the corresponding ChatColor.
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

    // Given a Color, return the name corresponding to its RGB values.
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

    // Return Color of a potion, with error handling.
    public Color getColor(ItemStack potion) {
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null || meta.getColor() == null) {
            this.log.severe("Unknown error X32.");
            return null;
        }
        return meta.getColor();
    }

    // Return the ChatColor corresponding with a potion's Color.
    public ChatColor chatColorFromPotion(ItemStack potion) {
        return chatColorFromColor(getColor(potion));
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

    // List of all Vanilla potions.
    public List<ItemStack> newVanillaPotionsList () {
        List<ItemStack> potions = new ArrayList<>();
        List<Material> threeTypes = new ArrayList<>();
        threeTypes.add(Material.POTION);
        threeTypes.add(Material.SPLASH_POTION);
        threeTypes.add(Material.LINGERING_POTION);

        for (Material m : threeTypes) {
            for (PotionType type : PotionType.values()) {
                if (type == PotionType.UNCRAFTABLE) continue;
                ItemStack vanillaPotion = new ItemStack(m);
                PotionMeta meta = (PotionMeta) vanillaPotion.getItemMeta();
                if (meta == null) {
                    this.log.severe("Unknown error X24.");
                    return null;
                }
                meta.setBasePotionData(new PotionData(type, false, false));
                vanillaPotion.setItemMeta(meta);
                potions.add(vanillaPotion);
                if (type.isExtendable()) {
                    vanillaPotion = new ItemStack(m);
                    meta = (PotionMeta) vanillaPotion.getItemMeta();
                    if (meta == null) {
                        this.log.severe("Unknown error X35.");
                        return null;
                    }
                    meta.setBasePotionData(new PotionData(type, true, false));
                    vanillaPotion.setItemMeta(meta);
                    potions.add(vanillaPotion);
                }
                if (type.isUpgradeable()) {
                    vanillaPotion = new ItemStack(m);
                    meta = (PotionMeta) vanillaPotion.getItemMeta();
                    if (meta == null) {
                        this.log.severe("Unknown error X36.");
                        return null;
                    }
                    meta.setBasePotionData(new PotionData(type, false, true));
                    vanillaPotion.setItemMeta(meta);
                    potions.add(vanillaPotion);
                }
            }
        }
        return potions;
    }

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
        if (fileInput.getString(s + ".effects") != null && fileInput.getString(s + ".effects").equals("none")) {

        } else if (helperEffects(fileInput.getConfigurationSection(s + ".effects"), s, potionMeta,
                pc.type == Material.LINGERING_POTION) == null) {
            return null;
        }

        // ItemStack (ItemStack).
        potionItemStack.setItemMeta(potionMeta);
        pc.itemstack = potionItemStack;

        // Recipes (List<PotionRecipe>).
        pc.potionrecipes = helperRecipe(fileInput, s, potionItemStack);
        if (pc.potionrecipes == null) return null;

        this.log.info("Successfully added " + s + " to the game.");

        return pc;
    }

    // Returns list of all custom potions loaded from memory.
    public List<PotionInfo> newPotionInfoList() {
        FileConfiguration fileInput = potionData.getData();
        Set<String> names = fileInput.getKeys(false);
        List<PotionInfo> pcList = new ArrayList<>();

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
            int durationTicks = isLingering ? 80 * durationI : 20 * durationI;
            if (durationTicks < 1) {
                this.log.warning(isLingering ? s + " must have an effect duration between 1 and 26,843,545 for " + effect + ". Skipping the effect." :
                        s + " must have an effect duration between 1 and 107,374,182 for " + effect + ". Skipping the effect.");
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
            if (amplifierI < 0 || amplifierI > maxAmp(effect)) {
                this.log.warning(s + " must have an effect amplifier from 0 to " + maxAmp(effect) + " for " + effect + ". Skipping the effect.");
                continue;
            }

            PotionEffect pe = new PotionEffect(effectType, durationI, amplifierI);
            potionEffects.add(pe);
            potionMeta.addCustomEffect(pe, false);
        }

        return potionEffects;
    }

    // Helper to get (from file) the information of a recipe.
    public List<PotionRecipe> helperRecipe(FileConfiguration fileInput, String s, ItemStack currentPotion) {
        List<PotionRecipe> potionRecipes = new ArrayList<>();
        ConfigurationSection recipes = fileInput.getConfigurationSection(s + ".recipes");
        if (recipes == null) {
            this.log.warning(s + " does not have any valid recipes. Skipping the potion.");
            return null;
        }
        Set<String> indices = recipes.getKeys(false);
        if (indices.size() == 0) {
            this.log.warning(s + " does not have any valid recipes. Skipping the potion.");
            return null;
        }

        for (String index : indices) {
            ItemStack predecessor = recipes.getItemStack(index + ".predecessor");
            if (predecessor == null) {
                this.log.severe("Unknown error X38.");
                return null;
            }
            if (!isPotion(predecessor.getType())) {
                this.log.warning(s + "'s predecessor in recipe " + index + " is not a valid type of potion. Skipping the potion.");
                return null;
            }
            if (potionsAreSame(currentPotion, predecessor)) {
                this.log.warning(s + " cannot have itself as a predecessor in recipe " + index + ". Skipping the potion.");
                return null;
            }
            String match = recipes.getString(index + ".ingredient");
            if (match == null) {
                this.log.warning(s + " does not have a valid ingredient in recipe " + index + ". Skipping the potion.");
                return null;
            }
            Material ingredient = Material.matchMaterial(match);
            if (ingredient == null) {
                this.log.warning(match + " is not a valid ingredient in recipe " + index + " of " + s + ChatColor.RESET + ". Skipping the potion.");
                return null;
            }
            potionRecipes.add(new PotionRecipe(predecessor, ingredient));
        }
        return potionRecipes;
    }

    // 0             1          2        3          4          5          6
    // custompotions click-page type/not colour/not effect/not recipe/not name/not

    // Methods using localized names.
    public String newLocalized(boolean fromMemory) {
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
    public String getClick(String localized) {
        return localized.split(" ")[1].split("-")[0];
    }
    public int getPage(String localized) {
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

    // Returns general ItemStack with all information added.
    public ItemStack newItemStackGUI(ItemStack item, String localizedName, String displayName, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            this.log.severe("Unknown error X3.");
            return item;
        }

        meta.setLocalizedName(localizedName);

        meta.setDisplayName(displayName);

        if (getClick(localizedName).equals("exit")) {
            lore = new ArrayList<>();
            if (item.getType() == Material.BARRIER) {
                lore.add(ChatColor.RED + "Click to exit.");
            } else {
                addRecipes(lore, potionRecipesInput);
                lore.add("");
                if (isCompletePotion(localizedName)) {
                    lore.add(ChatColor.GOLD + "This is your current potion.");
                    lore.add(ChatColor.GREEN + "Left click to save your changes and exit.");
                    lore.add(ChatColor.RED + "Right click to exit without saving.");
                } else {
                    lore.add(ChatColor.GOLD + "This is your current potion.");
                    lore.add(ChatColor.RED + "Click to exit without saving.");
                }
            }
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    // Returns page ItemStack with all information added.
    public ItemStack newPageGUI(String type, String localizedName, String currentMenu, int currentPage, boolean needsNextPage) {
        if (type.equals("previous")) {
            boolean needsPreviousPage = currentPage > 0;
            ItemStack previous = needsPreviousPage ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta prevMeta = previous.getItemMeta();
            if (prevMeta == null) {
                this.log.severe("Unknown error X5.");
                return null;
            }

            localizedName = setClick(localizedName, needsPreviousPage ? currentMenu : "invalid_page");
            localizedName = setPage(localizedName, needsPreviousPage ? currentPage - 1 : currentPage);
            prevMeta.setLocalizedName(localizedName);
            prevMeta.setDisplayName(needsPreviousPage ? ChatColor.GOLD + "PREVIOUS PAGE" : ChatColor.RED + "NO PREVIOUS PAGE");
            if (needsPreviousPage) {
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Page " + (currentPage));
                prevMeta.setLore(lore);
            }
            previous.setItemMeta(prevMeta);
            return previous;
        } else if (type.equals("next")) {
            ItemStack next = needsNextPage ? new ItemStack(Material.LIME_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta == null) {
                this.log.severe("Unknown error X6.");
                return null;
            }

            localizedName = setClick(localizedName, needsNextPage ? currentMenu : "invalid_page");
            localizedName = setPage(localizedName, needsNextPage ? currentPage + 1 : currentPage);
            nextMeta.setLocalizedName(localizedName);
            nextMeta.setDisplayName(needsNextPage ? ChatColor.GREEN + "NEXT PAGE" : ChatColor.RED + "NO NEXT PAGE");
            if (needsNextPage) {
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Page " + (currentPage + 2));
                nextMeta.setLore(lore);
            }
            next.setItemMeta(nextMeta);
            return next;
        } else {
            this.log.severe("Unknown error X4.");
            return null;
        }
    }

    /*******************************************************************************************************************
    *                                                     COMMANDS                                                     *
    *******************************************************************************************************************/

    // Create and modify custom potions.
    // "modify"
    public boolean modifyPotions(CommandSender sender) {
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
        if (clicked == null && existingPotion == null) {
            previousClick = newLocalized(true);
        } else {
            if (clicked == null || clicked.getItemMeta() == null) {
                this.log.severe("Unknown error X8.");
                return true;
            }
            previousClick = clicked.getItemMeta().getLocalizedName();
        }

        // If returning to the main menu, potions from memory should be relabelled as complete.
        String click = getClick(previousClick);
        if (click.equalsIgnoreCase("select") && getPage(previousClick) == 0) {
            previousClick = newLocalized(true);
        }

        // menus: select, type, colour, effect type, effect duration, effect amplifier, ingredient, predecessor, name
        switch (click) {
            case "select": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion to Modify");
                int PAGESIZE = 51;
                int numPotions = 0;
                int pageNumToDisplay = getPage(previousClick);
                int totalPotions = 1 + allCustomPotions.size();
                boolean needsNextPage = (totalPotions - PAGESIZE * pageNumToDisplay) > PAGESIZE;

                // PREVIOUS PAGE, slot 35.
                inv.setItem(35, newPageGUI("previous", previousClick, "select", pageNumToDisplay, needsNextPage));

                // NEXT PAGE, slot 44.
                inv.setItem(44, newPageGUI("next", previousClick, "select", pageNumToDisplay, needsNextPage));

                // EXIT, slot 53.
                inv.setItem(53, newItemStackGUI(new ItemStack(Material.BARRIER),
                        setClick(previousClick, "exit"), ChatColor.RED + "EXIT", null));

                // New potion.
                List<Colour> colourList = newColourList();
                if (pageNumToDisplay == 0) {
                    ItemStack potionItemStack = new ItemStack(Material.POTION);
                    PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
                    if (meta == null) {
                        this.log.severe("Unknown error X7.");
                        return true;
                    }
                    Colour randomColour = colourList.get((new Random()).nextInt(17));
                    Color randomColor = Color.fromRGB(randomColour.getR(), randomColour.getG(), randomColour.getB());
                    meta.setColor(randomColor);
                    potionItemStack.setItemMeta(meta);
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Create a new custom potion from scratch.");
                    potionItemStack = newItemStackGUI(potionItemStack, setClick(newLocalized(false),
                            "new_selected"), chatColorFromColor(randomColor) + "New Potion", lore);
                    inv.addItem(potionItemStack);
                }
                numPotions++;

                // All custom potions from potions.yml.
                String currentClick = setPage(setClick(previousClick, "selected"), 0);
                for (PotionInfo pi : allCustomPotions) {
                    // Past the page we want to display.
                    if (numPotions >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }

                    // Get to the page we want to display.
                    if (numPotions < PAGESIZE * pageNumToDisplay) {
                        numPotions++;
                        continue;
                    }

                    ItemStack potionItemStack = pi.itemstack.clone();
                    PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
                    if (meta == null) {
                        this.log.severe("Unknown error X9.");
                        return true;
                    }
                    List<String> lore = addRecipes(new ArrayList<>(), pi.potionrecipes);
                    potionItemStack = newItemStackGUI(potionItemStack, currentClick, meta.getDisplayName(), lore);
                    inv.addItem(potionItemStack);
                    numPotions++;
                }
                player.openInventory(inv);
                break;
            }
            case "type": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion Type");
                String currentClick = setPage(setType(setClick(previousClick, "colour"), true), 0);

                ItemStack potionItemStack = imprintMeta(new ItemStack(Material.POTION), existingPotion);
                inv.addItem(newItemStackGUI(potionItemStack, currentClick, chatColorFromPotion(potionItemStack) + "Potion", null));

                potionItemStack = imprintMeta(new ItemStack(Material.SPLASH_POTION), existingPotion);
                inv.addItem(newItemStackGUI(potionItemStack, currentClick, chatColorFromPotion(potionItemStack) + "Splash Potion", null));

                potionItemStack = imprintMeta(new ItemStack(Material.LINGERING_POTION), existingPotion);
                inv.addItem(newItemStackGUI(potionItemStack, currentClick, chatColorFromPotion(potionItemStack) + "Lingering Potion", null));

                currentClick = setType(previousClick, hasType(previousClick));

                // PREVIOUS PAGE, slot 35.
                inv.setItem(35, newPageGUI("previous", currentClick, "type", 0, false));

                // NEXT PAGE, slot 44.
                inv.setItem(44, newPageGUI("next", currentClick, "type", 0, false));

                // PREVIOUS MENU, slot 51.
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Potion Selection");
                lore.add(ChatColor.RED + "Warning: you will lose your unsaved changes!");
                lore.add(ChatColor.RED + "Save a completed potion to validate your changes.");
                inv.setItem(51, newItemStackGUI(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE),
                        setClick(currentClick, "previous_menu_select"), ChatColor.GOLD + "PREVIOUS MENU", lore));

                // NEXT MENU, slot 52.
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Colour Selection");
                inv.setItem(52, newItemStackGUI(new ItemStack(Material.LIME_STAINED_GLASS_PANE),
                        setClick(currentClick, "colour"), ChatColor.GREEN + "NEXT MENU", lore));

                // EXIT, slot 53.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X11.");
                    return true;
                }
                inv.setItem(53, newItemStackGUI(existingPotion.clone(),
                        setClick(currentClick, "exit"), potionNameInput, null));

                player.openInventory(inv);
                break;
            }
            case "colour": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion Colour");
                String currentClick = setPage(setClick(previousClick, "effect_type"), 0);

                // PREVIOUS PAGE, slot 35.
                inv.setItem(35, newPageGUI("previous", currentClick, "colour", 0, false));

                // NEXT PAGE, slot 44.
                inv.setItem(44, newPageGUI("next", currentClick, "colour", 0, false));

                // PREVIOUS MENU, slot 51.
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Type Selection");
                inv.setItem(51, newItemStackGUI(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE),
                        setClick(currentClick, "type"), ChatColor.GOLD + "PREVIOUS MENU", lore));

                // NEXT MENU, slot 52.
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Effect Type(s) Selection");
                inv.setItem(52, newItemStackGUI(new ItemStack(Material.LIME_STAINED_GLASS_PANE),
                        setClick(currentClick, "effect_type"), ChatColor.GREEN + "NEXT MENU", lore));

                // EXIT, slot 53.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X12.");
                    return true;
                }
                inv.setItem(53, newItemStackGUI(existingPotion.clone(),
                        setClick(currentClick, "exit"), potionNameInput, null));

                currentClick = setColour(currentClick, true);

                // All potion colours.
                ArrayList<Colour> colourList = newColourList();
                for (Colour c : colourList) {
                    ItemStack potionItemStack = existingPotion.clone();
                    PotionMeta meta = (PotionMeta) existingPotion.getItemMeta();
                    if (meta == null) {
                        this.log.severe("Unknown error X13.");
                        return true;
                    }

                    Color current = Color.fromRGB(c.getR(), c.getG(), c.getB());
                    String s = c.getName();
                    meta.setColor(current);
                    potionItemStack.setItemMeta(meta);
                    inv.addItem(newItemStackGUI(potionItemStack, currentClick, chatColorFromColor(meta.getColor()) + s, null));
                }

                player.openInventory(inv);
                break;
            }
            case "effect_type": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select an Effect Type");
                String currentClick = setPage(setClick(previousClick, "effect_dur"), 0);

                // PREVIOUS PAGE, slot 35.
                inv.setItem(35, newPageGUI("previous", currentClick, "effect_type", 0, false));

                // NEXT PAGE, slot 44.
                inv.setItem(44, newPageGUI("next", currentClick, "effect_type", 0, false));

                // PREVIOUS MENU, slot 51.
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Colour Selection");
                inv.setItem(51, newItemStackGUI(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE),
                        setClick(currentClick, "colour"), ChatColor.GOLD + "PREVIOUS MENU", lore));

                // NEXT MENU, slot 52.
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Ingredient Selection");
                inv.setItem(52, newItemStackGUI(new ItemStack(Material.LIME_STAINED_GLASS_PANE),
                        setClick(currentClick, "ingredient"), ChatColor.GREEN + "NEXT MENU", lore));

                // EXIT, slot 53.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X20.");
                    return true;
                }
                inv.setItem(53, newItemStackGUI(existingPotion.clone(),
                        setClick(currentClick, "exit"), potionNameInput, null));

                // No potion effect.
                lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "This potion will have no effects.");
                inv.addItem(newItemStackGUI(existingPotion.clone(), setEffect(setClick(currentClick, "no_effects"), true),
                        chatColorFromColor(getColor(existingPotion)) + "NO EFFECTS", lore));

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
                    ItemStack potionItemStack = existingPotion.clone();
                    PotionMeta meta = (PotionMeta) existingPotion.getItemMeta();
                    if (meta == null) {
                        this.log.severe("Unknown error X14.");
                        return true;
                    }

                    boolean hasEffect = false;
                    for (PotionEffect effect: meta.getCustomEffects()) {
                        if (effect.getType() == pet) {
                            hasEffect = true;
                            break;
                        }
                    }

                    String commonName = toCommonName(petName);

                    lore = new ArrayList<>();
                    lore.add("");
                    if (hasEffect) {
                        lore.add(ChatColor.GOLD + "Left click to modify " + commonName + ".");
                        lore.add(ChatColor.RED + "Right click to remove " + commonName + ".");
                        lore.add(ChatColor.GOLD + "It has potency range I to " + intToRoman(maxAmp(petName) + 1) + ".");
                        inv.addItem(newItemStackGUI(potionItemStack, setClick(currentClick, "effect_type_mixed"),
                                chatColorFromColor(meta.getColor()) + pet.getName(), lore));
                    } else {
                        lore.add(ChatColor.GREEN + "Click to add " + commonName + ".");
                        lore.add(ChatColor.GOLD + "It has potency range I to " + intToRoman(maxAmp(petName) + 1) + ".");
                        inv.addItem(newItemStackGUI(potionItemStack, currentClick,
                                chatColorFromColor(meta.getColor()) + pet.getName(), lore));
                    }
                }

                player.openInventory(inv);
                break;
            }
            case "effect_dur": {
                String currentClick = setClick(previousClick, "effect_amp");

                AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                anvil.title(ChatColor.GOLD + "Effect Duration");

                // POTION, slot 0 and 2.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X15.");
                    return true;
                }
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "This is your current potion.");
                if (existingPotion.getType() == Material.LINGERING_POTION) {
                    lore.add(ChatColor.GOLD + "Enter the effect duration (in seconds from 1 to 26,843,545).");
                } else {
                    lore.add(ChatColor.GOLD + "Enter the effect duration (in seconds from 1 to 107,374,182).");
                }
                lore.add(ChatColor.GOLD + "You will not be able to change menu or save in this menu.");
                lore.add(ChatColor.GOLD + "If you have misclicked, just continue.");
                lore.add(ChatColor.GREEN + "Left click the output slot to continue.");
                lore.add(ChatColor.RED + "Right click the output slot to exit without saving.");
                anvil.item(newItemStackGUI(existingPotion.clone(), currentClick, potionNameInput, lore));

                anvil.text(ChatColor.RESET + "Enter here:");
                anvil.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.text("Enter here:"));
                anvil.plugin(this);

                anvilInv = anvil.open(player);
                break;
            }
            case "effect_amp": {
                String currentClick = setClick(previousClick, "effect_type");

                AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                anvil.title(ChatColor.GOLD + "Effect Amplifier");

                // POTION, slot 0 and 2.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X16.");
                    return true;
                }
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.GOLD + "Enter the effect amplifier (integer from 0 to " + maxAmp(effectTypeInput) + ").");
                lore.add(ChatColor.GOLD + "0 means potency I, eg. " + toCommonName(effectTypeInput) + " I.");
                lore.add(ChatColor.GOLD + "Similarly, 1 means potency II, eg. " + toCommonName(effectTypeInput) + " II, and so on.");
                lore.add(ChatColor.GOLD + "You will not be able to change menu or save in this menu.");
                lore.add(ChatColor.GOLD + "If you have misclicked, just continue.");
                lore.add(ChatColor.GREEN + "Left click the output slot to continue.");
                lore.add(ChatColor.RED + "Right click the output slot to exit without saving.");
                anvil.item(newItemStackGUI(existingPotion.clone(), currentClick, potionNameInput, lore));

                anvil.text(ChatColor.RESET + "Enter here:");
                anvil.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.text("Enter here:"));
                anvil.plugin(this);

                anvilInv = anvil.open(player);
                break;
            }
            case "ingredient": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select an Ingredient");

                Material[] allMaterials = Material.values();
                List<Material> allItemMaterials = new ArrayList<>();
                for (Material material : allMaterials) {
                    if (isValidIngredient(material)) allItemMaterials.add(material);
                }
                int numMaterials = 0;
                int pageNumToDisplay = getPage(previousClick);
                int totalIngredients = allItemMaterials.size();
                int PAGESIZE = 49;
                boolean needsNextPage = (totalIngredients - PAGESIZE * pageNumToDisplay) > PAGESIZE;

                // PREVIOUS PAGE, slot 35.
                inv.setItem(35, newPageGUI("previous", previousClick, "ingredient", pageNumToDisplay, needsNextPage));

                // NEXT PAGE, slot 44.
                inv.setItem(44, newPageGUI("next", previousClick, "ingredient", pageNumToDisplay, needsNextPage));

                previousClick = setPage(previousClick,0);

                // PREVIOUS MENU, slot 51.
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Effect Type(s) Selection");
                inv.setItem(51, newItemStackGUI(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE),
                        setClick(previousClick, "effect_type"), ChatColor.GOLD + "PREVIOUS MENU", lore));

                // NEXT MENU, slot 52.
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Naming");
                inv.setItem(52, newItemStackGUI(new ItemStack(Material.LIME_STAINED_GLASS_PANE),
                        setClick(previousClick, "name"), ChatColor.GREEN + "NEXT MENU", lore));

                // EXIT, slot 53.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X19.");
                    return true;
                }
                inv.setItem(53, newItemStackGUI(existingPotion.clone(),
                        setClick(previousClick, "exit"), potionNameInput, null));

                String currentClick = setPage(setClick(previousClick, "ingredient_chosen"), 0);

                // All materials.
                for (Material material : allItemMaterials) {
                    if (numMaterials >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }

                    if (numMaterials < PAGESIZE * pageNumToDisplay) {
                        numMaterials++;
                        continue;
                    }

                    // TODO don't allow two with same predecessor to have same ingredient, will have to alter the
                    //  length of the allMaterials above too

                    // Determine if ingredient has already been chosen.
                    String predecessorName = null;
                    for (PotionRecipe potionRecipe : this.potionRecipesInput) {
                        if (potionRecipe.ingredient == material) {
                            if (potionRecipe.predecessor.getItemMeta() == null) {
                                this.log.severe("Unknown error X22");
                                return true;
                            }
                            predecessorName = potionRecipe.predecessor.getItemMeta().getDisplayName();
                            break;
                        }
                    }

                    lore = new ArrayList<>();
                    if (predecessorName != null) {
                        lore.add(ChatColor.GOLD + "Left click to add or modify the predecessor(s) associated with " +
                                normaliseCapitalise(material.name(), "_") + ".");
                        lore.add(ChatColor.RED + "Right click to remove all recipes using " + normaliseCapitalise(material.name(), "_") + ".");
                        inv.addItem(newItemStackGUI(new ItemStack(material), setClick(currentClick, "ingredient_mixed"), null, lore));
                        // TODO ingredient_mixed in click
                    } else {
                        lore.add(ChatColor.GREEN + "Click to add " + normaliseCapitalise(material.name(), "_") + ".");
                        inv.addItem(newItemStackGUI(new ItemStack(material), currentClick, null, lore));
                    }
                    numMaterials++;
                }

                player.openInventory(inv);
                break;
            }
            case "predecessor": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Predecessor");

                int numPredecessors = 0;
                int pageNumToDisplay = getPage(previousClick);
                int totalPredecessors = potionFromMemory == null ? allCustomPotions.size() + allVanillaPotions.size() :
                        allCustomPotions.size() + allVanillaPotions.size() - 1;
                int PAGESIZE = 50;
                boolean needsNextPage = (totalPredecessors - PAGESIZE * pageNumToDisplay) > PAGESIZE;

                // PREVIOUS PAGE, slot 35.
                inv.setItem(35, newPageGUI("previous", previousClick, "predecessor", pageNumToDisplay, needsNextPage));

                // NEXT PAGE, slot 44.
                inv.setItem(44, newPageGUI("next", previousClick, "predecessor", pageNumToDisplay, needsNextPage));

                String currentClick = setPage(previousClick,0);

                // PREVIOUS MENU, slot 51.
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Ingredient Selection");
                lore.add(ChatColor.RED + "Warning: you will lose your choice of ingredient!");
                inv.setItem(52, newItemStackGUI(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE),
                        setClick(currentClick, "previous_menu_ingredient"), ChatColor.GOLD + "PREVIOUS MENU", lore));

                // NEXT MENU, slot 52.
                /*lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Naming");
                inv.setItem(52, newItemStackGUI(new ItemStack(Material.LIME_STAINED_GLASS_PANE),
                        setClick(currentClick, "name"), ChatColor.GREEN + "NEXT MENU", lore));*/

                // EXIT, slot 53.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X23.");
                    return true;
                }
                inv.setItem(53, newItemStackGUI(existingPotion.clone(),
                        setClick(currentClick, "exit"), potionNameInput, null));

                // All vanilla potions.
                currentClick = setClick(currentClick, "predecessor_chosen");
                for (ItemStack vanillaPotion : allVanillaPotions) {
                    if (numPredecessors >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }

                    if (numPredecessors < PAGESIZE * pageNumToDisplay) {
                        numPredecessors++;
                        continue;
                    }
                    inv.addItem(setLocalizedName(vanillaPotion, currentClick));
                    numPredecessors++;
                }

                // All custom potions from potions.yml.
                for (PotionInfo customPotion : allCustomPotions) {
                    // If the potion is from memory and it is the same as the potion about to be added, don't add it.
                    if (numPredecessors >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }

                    if (potionsAreSame(potionFromMemory, customPotion.itemstack)) continue;

                    if (numPredecessors < PAGESIZE * pageNumToDisplay) {
                        numPredecessors++;
                        continue;
                    }

                    lore = addRecipes(new ArrayList<>(), customPotion.potionrecipes);
                    inv.addItem(newItemStackGUI(customPotion.itemstack.clone(), currentClick, customPotion.name, lore));
                    numPredecessors++;
                }

                player.openInventory(inv);
                break;
            }
            case "name": {
                String currentClick = setName(setClick(previousClick, "final"), true);

                AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                anvil.title(ChatColor.GOLD + "Enter a Name");

                // TODO if potion is incomplete, take them to first empty menu (ie. not not)

                // POTION, slot 0 and 2.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X30.");
                    return true;
                }
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.GOLD + "The potion name must be unique (case sensitive).");
                lore.add(ChatColor.GOLD + "You can use Minecraft chat code colours.");
                lore.add(ChatColor.GOLD + "(Hint: Use &r to remove default italicisation!)");
                lore.add(ChatColor.GOLD + "You will have the option to review changes in the next menu.");
                lore.add(ChatColor.GREEN + "Left click the output slot to continue.");
                lore.add(ChatColor.RED + "Right click the output slot to exit without saving.");
                anvil.item(newItemStackGUI(existingPotion.clone(), currentClick, null, lore));

                anvil.text(ChatColor.RESET + "Enter here:");
                anvil.onComplete((whoTyped, whatWasTyped) -> {
                    // TODO check if potion already exists with the name and isn't the current potion
                    return AnvilGUI.Response.text("Enter here:");
                });
                anvil.plugin(this);

                anvilInv = anvil.open(player);
                break;
            }
            case "final":
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Confirm Changes");
                // TODO slot 23 modify; slot 24 exit without saving; slot 25 save and exit
                // TODO this is all very scrappy temp code. need to fix
                // TODO add all menus
                List<String> lore = addRecipes(new ArrayList<>(), potionRecipesInput);
                if (existingPotion == null) {
                    this.log.severe("Unknown error X29.");
                    return true;
                }
                inv.setItem(22, newItemStackGUI(existingPotion.clone(), setClick(previousClick, "complete"),
                        potionNameInput, lore));
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
    public boolean givePotions(Player player, ItemStack clicked) {
        String previousClick;
        if (clicked == null) {
            previousClick = "custompotions give-0 not not not not not";
        } else {
            if (clicked.getItemMeta() == null) {
                this.log.severe("Unknown error X41.");
                return true;
            }
            previousClick = clicked.getItemMeta().getLocalizedName();
        }

        inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion to Withdraw");
        int PAGESIZE = 51;
        int numPotions = 0;
        int pageNumToDisplay = getPage(previousClick);
        int totalPotions = allCustomPotions.size();
        boolean needsNextPage = (totalPotions - PAGESIZE * pageNumToDisplay) > PAGESIZE;

        // PREVIOUS PAGE, slot 35.
        inv.setItem(35, newPageGUI("previous", previousClick, "give", pageNumToDisplay, needsNextPage));

        // NEXT PAGE, slot 44.
        inv.setItem(44, newPageGUI("next", previousClick, "give", pageNumToDisplay, needsNextPage));

        // EXIT, slot 53.
        inv.setItem(53, newItemStackGUI(new ItemStack(Material.BARRIER),
                setClick(previousClick, "give_exit"), ChatColor.RED + "EXIT", null));

        // All custom potions from potions.yml.
        for (PotionInfo pi : allCustomPotions) {
            // Past the page we want to display.
            if (numPotions >= PAGESIZE * (pageNumToDisplay + 1)) {
                break;
            }

            // Get to the page we want to display.
            if (numPotions < PAGESIZE * pageNumToDisplay) {
                numPotions++;
                continue;
            }

            ItemStack potionItemStack = pi.itemstack.clone();
            PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
            if (meta == null) {
                this.log.severe("Unknown error X9.");
                return true;
            }
            List<String> lore = addRecipes(new ArrayList<>(), pi.potionrecipes);
            potionItemStack = newItemStackGUI(potionItemStack, setPage(previousClick, pageNumToDisplay), meta.getDisplayName(), lore);
            inv.addItem(potionItemStack);
            numPotions++;
        }
        player.openInventory(inv);
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
                if (isPotion(insertIngredient.getType())) {
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
                if (isPotion(insertIngredient.getType())) {
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
                if (isPotion(insertIngredient.getType())) {
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
                if (isPotion(insertIngredient.getType())) {
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
        if (clicked == null || clicked.getItemMeta() == null) return;
        if (inventory == inv) {
            // Inventory is the custom 54-slot.
        } else if (anvilInv == null || inventory != anvilInv.getInventory()) {
            // Inventory is not the 54-slot and not anvil.
            return;
        }

        String localizedName = clicked.getItemMeta().getLocalizedName();

        event.setCancelled(true);

        if (getClick(localizedName).equals("give")) {
            if (isPotion(clicked.getType())) {
                event.getWhoClicked().sendMessage(ChatColor.GREEN + "You have withdrawn " + clicked.getItemMeta().getDisplayName() + ChatColor.GREEN + ".");
                event.getWhoClicked().getInventory().addItem(clicked);
            }
            givePotions((Player) event.getWhoClicked(), clicked);
            return;
        }

        if (getClick(localizedName).equals("give_exit")) {
            event.getWhoClicked().closeInventory();
        }

        if (!isPotionClick(localizedName)) {
            this.log.warning("Unknown error X1.");
            return;
        }

        boolean isPotion = isPotion(clicked.getType());
        boolean isLingeringPotion = clicked.getType() == Material.LINGERING_POTION;

        if (inventory == inv) { // 54-slot inventory.
            // Invalid attempt to change page (red glass).
            if (getClick(localizedName).equalsIgnoreCase("invalid_page")) {
                return;
            }

            // Leave via slot 53 exit.
            if (getClick(localizedName).equals("exit")) {
                if (clicked.getType() == Material.BARRIER || event.getClick() == ClickType.RIGHT) {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                    resetGlobals();
                } else if (event.getClick() == ClickType.LEFT) {
                    String newName = potionNameInput;
                    PotionMeta newMeta = ((PotionMeta) clicked.getItemMeta());
                    Color newColor = newMeta.getColor();
                    if (newColor == null) {
                        this.log.severe("Unknown error X37.");
                        return;
                    }
                    boolean isLingering = clicked.getType() == Material.LINGERING_POTION;

                    // Remove old information.
                    if (potionFromMemory.getItemMeta() == null) {
                        this.log.severe("Unknown error X40.");
                        return;
                    }
                    potionData.getData().set(potionFromMemory.getItemMeta().getDisplayName(), null);

                    // TODO Fix all potions which use this potion as a predecessor.

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
                    int i = 0;
                    for (PotionRecipe newPotionRecipe : potionRecipesInput) {
                        String s = newName + ".recipes." + i;
                        potionData.getData().set(s + ".ingredient", newPotionRecipe.ingredient.name());
                        potionData.getData().set(s + ".predecessor", newPotionRecipe.predecessor);
                        i++;
                    }

                    potionData.saveData();
                    allCustomPotions.add(newPotionInfo(potionData.getData(), newName)); // TODO potentially add to other structures if they are added.
                    resetGlobals();

                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Your changes to " + newName + ChatColor.GREEN + " have been saved.");
                    return;
                }
                return;
            }

            switch (getClick(localizedName)) {
                // Select -> Type via selecting a potion. Set potionNameInput to this name, and whether or not it is from memory.
                case "selected": {
                    potionFromMemory = resetLore(resetLocalizedName(clicked));
                    for (PotionInfo customPotion : allCustomPotions) {
                        if (customPotion.name.equals(clicked.getItemMeta().getDisplayName())) {
                            this.potionRecipesInput.addAll(customPotion.potionrecipes);
                            break;
                        }
                    }
                    // fall through
                } case "new_selected": {
                    this.potionNameInput = clicked.getItemMeta().getDisplayName();
                    PotionMeta meta = (PotionMeta) clicked.getItemMeta();
                    if (meta == null) {
                        this.log.severe("Unknown error X21.");
                        return;
                    }
                    meta.setLocalizedName(setClick(localizedName, "type"));
                    clicked.setItemMeta(meta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

                // Type -> Select via previous menu.
                case "previous_menu_select": {
                    playerModifyPotions((Player) event.getWhoClicked(), null,
                            setLocalizedName(clicked, setClick(localizedName, "select")));
                    return;
                }

                // Effect Type -> Effect Duration via selecting an effect. Set effectTypeInput to this effect.
                case "effect_dur": {
                    effectTypeInput = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

                // Effect Type -> Effect Type / Effect Duration via selecting an effect.
                case "effect_type_mixed": {
                    if (event.getClick() == ClickType.LEFT) { // Modify.
                        effectTypeInput = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                        clicked = setLocalizedName(clicked, setClick(localizedName, "effect_dur"));
                        playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    } else if (event.getClick() == ClickType.RIGHT) { // Remove
                        PotionMeta removeMeta = ((PotionMeta) clicked.getItemMeta());
                        PotionEffectType type = PotionEffectType.getByName(ChatColor.stripColor(removeMeta.getDisplayName()));
                        if (type == null) {
                            this.log.severe("Unknown error X34.");
                            return;
                        }
                        removeMeta.removeCustomEffect(type);
                        removeMeta.setLocalizedName(setClick(localizedName, "effect_type"));
                        clicked.setItemMeta(removeMeta);
                        playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    }
                    return;
                }

                // Effect Type -> Ingredient via selecting no effects.
                case "no_effects": {
                    PotionMeta removeMeta = ((PotionMeta) clicked.getItemMeta());
                    removeMeta.clearCustomEffects();
                    removeMeta.setLocalizedName(setClick(localizedName, "ingredient"));
                    clicked.setItemMeta(removeMeta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

                // Ingredient -> Predecessor via selecting an ingredient.
                case "ingredient_chosen": {
                    ingredientInput = clicked.getType();
                    clicked = setLocalizedName(clicked, setClick(localizedName, "predecessor"));
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Predecessor -> Ingredient via previous menu.
                case "previous_menu_ingredient": {
                    ingredientInput = null;
                    String newLocalized = setRecipe(localizedName, potionRecipesInput.size() > 0);
                    clicked = setLocalizedName(clicked, setClick(newLocalized, "ingredient"));
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Predecessor -> Ingredient via selecting a predecessor.
                case "predecessor_chosen": {
                    clicked = setLocalizedName(clicked, setClick(setRecipe(localizedName, true), "ingredient"));
                    ItemStack chosenPredecessor = resetLore(resetLocalizedName(clicked.clone()));
                    potionRecipesInput.add(new PotionRecipe(chosenPredecessor, ingredientInput));
                    ingredientInput = null;
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Saving completed potion to potions.yml.
                case "complete": {
                    String newName = potionNameInput;
                    PotionMeta newMeta = ((PotionMeta) clicked.getItemMeta());
                    Color newColor = newMeta.getColor();
                    if (newColor == null) {
                        this.log.severe("Unknown error X37.");
                        return;
                    }
                    boolean isLingering = clicked.getType() == Material.LINGERING_POTION;

                    // Remove old information.
                    if (potionFromMemory != null && potionFromMemory.getItemMeta() != null) {
                        potionData.getData().set(potionFromMemory.getItemMeta().getDisplayName(), null);
                    }

                    // TODO Fix all potions which use this potion as a predecessor.

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
                    int i = 0;
                    for (PotionRecipe newPotionRecipe : potionRecipesInput) {
                        String s = newName + ".recipes." + i;
                        potionData.getData().set(s + ".ingredient", newPotionRecipe.ingredient.name());
                        potionData.getData().set(s + ".predecessor", newPotionRecipe.predecessor);
                        i++;
                    }

                    potionData.saveData();
                    allCustomPotions.add(newPotionInfo(potionData.getData(), newName)); // TODO potentially add to other structures if they are added.
                    resetGlobals();

                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Your changes to " + newName + ChatColor.GREEN + " have been saved.");
                    return;
                }
            }
        } else { // Anvil inventory.
            // Not the output slot. anvilInv remains unchanged.
            if (event.getSlot() != AnvilGUI.Slot.OUTPUT) {
                return;
            }

            // Right clicking the output slot (exit), or not left clicking (nothing happens).
            switch (getClick(localizedName)) {
                case "effect_amp":
                case "effect_type":
                case "final": {
                    if (event.getClick() == ClickType.RIGHT) {
                        anvilInv.closeInventory();
                        event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                        resetGlobals();
                        return;
                    } else if (event.getClick() != ClickType.LEFT) {
                        return;
                    }
                }
            }

            // Left clicking the output slot (continue).
            switch (getClick(localizedName)) {
                // Effect Duration -> Effect Amp via continue.
                case "effect_amp": {
                    effectDurationInput = clicked.getItemMeta().getDisplayName();
                    int dur;
                    try {
                        dur = Integer.parseInt(effectDurationInput);
                    } catch (Exception e) { // Not an integer.
                        effectDurationInput = null;
                        return;
                    }

                    printDebug(localizedName);

                    int max = isLingeringPotion ? 26843545 : 107374182;
                    if (dur < 1 || dur > max) { // Not in range.
                        effectDurationInput = null;
                        return;
                    }

                    // effectDurationInput is valid.
                    if (maxAmp(effectTypeInput) == 0) { // No need for inputting an amp.
                        PotionMeta newEffectMeta = ((PotionMeta) clicked.getItemMeta());
                        PotionEffectType newType = PotionEffectType.getByName(effectTypeInput);
                        if (newType == null) {
                            this.log.severe("Unknown error X17.");
                            return;
                        }
                        int newDur = isLingeringPotion ? Integer.parseInt(effectDurationInput) * 80 : Integer.parseInt(effectDurationInput) * 20;
                        newEffectMeta.addCustomEffect(new PotionEffect(newType, newDur, 0), true);
                        newEffectMeta.setLocalizedName(setClick(setEffect(localizedName, true), "effect_type"));
                        clicked.setItemMeta(newEffectMeta);
                        effectTypeInput = null;
                        effectDurationInput = null;
                    }
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

                // Effect Amp -> Effect Type via continue.
                case "effect_type": {
                    effectAmplifierInput = clicked.getItemMeta().getDisplayName();
                    int amp;
                    try {
                        amp = Integer.parseInt(effectAmplifierInput);
                    } catch (Exception e) { // Not an integer.
                        effectAmplifierInput = null;
                        return;
                    }

                    printDebug(localizedName);

                    if (amp < 0 || amp > maxAmp(effectTypeInput)) { // Not in range.
                        effectAmplifierInput = null;
                    } else {
                        PotionMeta newEffectMeta = ((PotionMeta) clicked.getItemMeta());
                        PotionEffectType newType = PotionEffectType.getByName(effectTypeInput);
                        if (newType == null) {
                            this.log.severe("Unknown error X18.");
                            return;
                        }
                        int newDur = isLingeringPotion ? Integer.parseInt(effectDurationInput) * 80 : Integer.parseInt(effectDurationInput) * 20;
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

                // Naming -> Final via continue.
                case "final": {
                    potionNameInput = ChatColor.translateAlternateColorCodes('&', clicked.getItemMeta().getDisplayName());

                    printDebug(localizedName);

                    PotionMeta namedMeta = ((PotionMeta) clicked.getItemMeta());
                    namedMeta.setDisplayName(potionNameInput);
                    clicked.setItemMeta(namedMeta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

            }
        }

        printDebug(localizedName);

        if (isPotion) { // Pass the potion that was just clicked.
            playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
        } else { // Pass the potion which will be in the bottom right hand corner.
            playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
        }
    }

}