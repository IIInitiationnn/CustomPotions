package custompotions;

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
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.logging.Logger;

import static utilities.ColorUtil.chatColorFromColor;

public class main extends JavaPlugin implements Listener {
    private Logger log;
    public data potionData;
    public List<PotionInfo> allCustomPotions;
    public Inventory inv;
    AnvilGUI.Builder anvil;
    AnvilGUI anvilInv = null;

    // TODO have we fixed page thing where if you change to page 3 and then
    // TODO remove custom colours (dont add potions with funky colours). can get rid of colorutil too. we need a chatColorFromColor using color which will use rgb matching!!!

    // STARTUP
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

    // RELOAD
    public void reload() {
        this.potionData.reloadData();
        allCustomPotions = newPotionInfoList();
        this.getCommand("custompotions").setExecutor(new commands(this));
        this.getCommand("custompotions").setTabCompleter(new tabcomplete(this));
    }

    // STOP
    @java.lang.Override
    public void onDisable() {
        this.log.info("CustomPotions has been disabled.");
    }

    // DISPLAY PERMISSION DENIED TO CONSOLE
    public void permissionDenied(CommandSender sender) {
        this.log.info(ChatColor.RED + "" + sender.getName() + ChatColor.DARK_RED + " was denied access to command.");
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
    public ChatColor chatColorFromColour(Colour colour) {
        switch (colour.getName()) {
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

    /*******************************************************************************************************************
    *                                                 POTION MANAGEMENT                                                *
    *******************************************************************************************************************/

    // PotionInfo class.
    public static class PotionInfo {
        public String name;
        public Material type;
        public Material ingredient;
        public ItemStack predecessor;
        public Colour colour;
        public ItemStack itemstack;
    }

    // Returns list of a data-type containing information about all custom potions.
    // TODO maybe make this call a thing which makes a single PotionInfo so we can call when adding new one
    public List<PotionInfo> newPotionInfoList() {
        FileConfiguration fileInput = potionData.getData();
        Set<String> names = fileInput.getKeys(false);
        List<PotionInfo> pcList = new ArrayList<PotionInfo>();
        for (String s : names) {
            PotionInfo pc = new PotionInfo();

            // Name (String).
            pc.name = s;

            // Type (Material).
            pc.type = helperType(fileInput, s);
            if (pc.type == null) continue;

            // ItemStack (ItemStack).
            ItemStack potionItemStack = new ItemStack(pc.type);
            PotionMeta potionMeta = (PotionMeta) potionItemStack.getItemMeta();
            if (potionMeta == null) {
                this.log.severe("Unknown error. Skipping the potion.");
                continue;
            }
            potionMeta.setDisplayName(ChatColor.RESET + s);

            // Colour (Colour) - handles potionMeta.
            pc.colour = helperColour(fileInput, s, potionMeta);
            if (pc.colour == null) continue;

            // Effects (List<PotionEffect>).
            if (helperEffects(fileInput.getConfigurationSection(s + ".effects"), s, potionMeta) == null) continue;

            // Ingredient (Material).
            pc.ingredient = helperIngredient(fileInput, s);
            if (pc.ingredient == null) continue;

            // Predecessor (ItemStack).
            pc.predecessor = helperPredecessor(fileInput, s);
            if (pc.predecessor == null) continue;

            // ItemStack (ItemStack).
            potionItemStack.setItemMeta(potionMeta);
            pc.itemstack = potionItemStack;
            pcList.add(pc);
            this.log.info("Successfully added " + s + " to the game.");
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
                if (redI < 0 || redI > 255 || greenI < 0 || greenI > 255 || blueI < 0 || blueI > 255) {
                    this.log.warning(s + " has an invalid RGB value(s). Must be an integer from 0 to 255 inclusive. Skipping the potion.");
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
    public List<PotionEffect> helperEffects(ConfigurationSection fx, String s, PotionMeta potionMeta) {
        List<PotionEffect> potionEffects = new ArrayList<>();
        if (fx == null) {
            this.log.info(s + " does not have any valid effects. Continuing with the potion.");
            return potionEffects;
        }

        Set<String> effects = fx.getKeys(false);
        for (String effect : effects) {
            if (effect.equalsIgnoreCase("none")) {
                // this.log.info(s + " does not have any valid effects. Continuing with the potion.");
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

            PotionEffect pe = new PotionEffect(effectType, durationI, amplifierI);
            potionEffects.add(pe);
            potionMeta.addCustomEffect(pe, false);
        }

        return potionEffects;
    }

    // Helper to get (from file) and set the ingredient of a potion.
    public Material helperIngredient(FileConfiguration fileInput, String s) {
        String ingredient = fileInput.getString(s + ".ingredient");
        if (ingredient == null) {
            this.log.warning(s + " does not have an ingredient. Skipping.");
            return null;
        }
        Material match = Material.matchMaterial(ingredient);
        if (match == null) {
            this.log.warning(ingredient + " is not a valid ingredient for " + s + ". Skipping the potion.");
            return null;
        }
        return match;
    }

    // Helper to get (from file) the information of a predecessor.
    public ItemStack helperPredecessor(FileConfiguration fileInput, String s) {
        String preType = fileInput.getString(s + ".predecessor.type");
        if (preType == null) {
            this.log.warning(s + " does not have a valid predecessor type. Skipping the potion.");
            return null;
        }
        Material match = Material.matchMaterial(preType);
        if (match == null) {
            this.log.warning(preType + " is not a valid predecessor type for " + s + ". Skipping the potion.");
            return null;
        }
        ItemStack item = new ItemStack(match);
        String name = fileInput.getString(s + ".predecessor.name");
        if (name == null) {
            this.log.warning(s + " does not have a valid predecessor name. Skipping the potion.");
            return null;
        }

        if (match == Material.POTION || match == Material.LINGERING_POTION || match == Material.SPLASH_POTION) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + name);
            if (helperEffects(fileInput.getConfigurationSection(s + ".predecessor.effects"), s + "'s predecessor", meta) == null) // TODO why is this bugging out
                return null;
            item.setItemMeta(meta);
        } else {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + name);
            item.setItemMeta(meta);
        }

        return item;
    }

    // When something in potion creation GUI is clicked, this class will be used.
    private static class PotionClick implements Cloneable {
        private String click; // exit, select, type, colour, effect_type, effect_dur, effect_amp, ingredient, predecessor, name
        private int page; // 0 if a not a page indicator, otherwise will be the page to go to
        private boolean type;
        private boolean colour;
        private boolean effect_type;
        private boolean effect_dur;
        private boolean effect_amp;
        private boolean ingredient;
        private boolean predecessor;
        private boolean name;

        // Initializer constructor for when command is issued.
        public PotionClick(boolean fromMemory) {
            this.click = "select";
            this.page = 0;
            this.type = this.colour = this.effect_type = this.effect_dur = this.effect_amp = this.ingredient = this.predecessor = this.name = fromMemory;
        }

        // Constructor using localized name.
        // 0             1          2        3          4               5              6              7              8               9
        // custompotions click-page type/not colour/not effect_type/not effect_dur/not effect_amp/not ingredient/not predecessor/not name/not
        public PotionClick(String s) {
            String[] all = s.split(" ");
            this.click = all[1].split("-")[0];
            this.page = Integer.parseInt(all[1].split("-")[1]);
            this.type = all[2].equalsIgnoreCase("type");
            this.colour = all[3].equalsIgnoreCase("colour");
            this.effect_type = all[4].equalsIgnoreCase("effect_type");
            this.effect_dur = all[5].equalsIgnoreCase("effect_dur");
            this.effect_amp = all[6].equalsIgnoreCase("effect_amp");
            this.ingredient = all[7].equalsIgnoreCase("ingredient");
            this.predecessor = all[8].equalsIgnoreCase("predecessor");
            this.name = all[9].equalsIgnoreCase("name");
        }

        // Simple methods.
        public PotionClick clone() throws CloneNotSupportedException {
            return (PotionClick) super.clone();
        }
        public String getClick() {
            return click;
        }
        public int getPage() {
            return page;
        }
        public boolean hasType() {
            return type;
        }
        public boolean hasColour() {
            return colour;
        }
        public boolean hasEffectType() {
            return effect_type;
        }
        public boolean hasEffectDur() {
            return effect_dur;
        }
        public boolean hasEffectAmp() {
            return effect_amp;
        }
        public boolean hasIngredient() {
            return ingredient;
        }
        public boolean hasPredecessor() {
            return predecessor;
        }
        public boolean hasName() {
            return name;
        }
        public boolean isComplete() {
            return type && colour && effect_type && effect_dur && effect_amp && ingredient && predecessor && name;
        }
        public void setClick(String click) {
            this.click = click;
        }
        public void setPage(int page) {
            this.page = page;
        }
        public void setType(boolean type) {
            this.type = type;
        }
        public void setColour(boolean colour) {
            this.colour = colour;
        }
        public void setEffectType(boolean effect_type) {
            this.effect_type = effect_type;
        }
        public void setEffectDur(boolean effect_dur) {
            this.effect_dur = effect_dur;
        }
        public void setEffectAmp(boolean effect_amp) {
            this.effect_amp = effect_amp;
        }
        public void setIngredient(boolean ingredient) {
            this.ingredient = ingredient;
        }
        public void setPredecessor(boolean predecessor) {
            this.predecessor = predecessor;
        }
        public void setName(boolean name) {
            this.name = name;
        }

        // Provided with a PotionClick, create a localized name.
        public String localizedNameFromPotionClick() {
            String s = ("custompotions " + click + "-" + String.valueOf(page));
            s = type ? s.concat(" type") : s.concat(" not");
            s = colour ? s.concat(" colour") : s.concat(" not");
            s = effect_type ? s.concat(" effect_type") : s.concat(" not");
            s = effect_dur ? s.concat(" effect_dur") : s.concat(" not");
            s = effect_amp ? s.concat(" effect_amp") : s.concat(" not");
            s = ingredient ? s.concat(" ingredient") : s.concat(" not");
            s = predecessor ? s.concat(" predecessor") : s.concat(" not");
            s = name ? s.concat(" name") : s.concat(" not");
            return s;
        }
    }

    // Returns true if an item involving CustomPotions GUI is clicked.
    public boolean isPotionClick(String s) {
        return s.split(" ")[0].equalsIgnoreCase("custompotions");
    }

    // Returns ItemStack with all information added.
    public ItemStack newItemStackGUI(ItemStack item, PotionClick pc) throws CloneNotSupportedException {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (pc.getClick().equalsIgnoreCase("exit")) {
            meta.setDisplayName(ChatColor.RED + "EXIT");
            if (item.getType() == Material.BARRIER) {
                lore.add(ChatColor.RED + "Click to exit without saving.");
            } else if (pc.isComplete()) {
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.GREEN + "Left click to save and exit.");
                lore.add(ChatColor.RED + "Right click to exit without saving.");
            } else {
                lore.add(ChatColor.GOLD + "This is your current potion.");
                lore.add(ChatColor.RED + "Click to exit without saving.");
            }
            meta.setLore(lore);
            meta.setLocalizedName(pc.localizedNameFromPotionClick());
        }
        switch (item.getType()) {
            case RED_STAINED_GLASS_PANE:
                meta.setDisplayName(ChatColor.RED + "NO PAGE TO GO TO");
            default:
                break;
        }
        meta.setLocalizedName(pc.localizedNameFromPotionClick());
        item.setItemMeta(meta);
        // TODO maybe add lore in the future when add lore for page numbers, predecessor, ingredient all from a glance
        return item;
    }

    /*******************************************************************************************************************
     *                                                     COMMANDS                                                     *
     *******************************************************************************************************************/

    // Create and modify custom potions.
    // "modify"
    public boolean modifyPotions(CommandSender sender) throws CloneNotSupportedException {
        if (sender instanceof Player) {
            return playerModifyPotions((Player) sender, null, null);
        } else {
            return consoleModifyPotions(sender);
        }
    }

    // Modifying potions with GUI for players.
    public boolean playerModifyPotions(Player player, ItemStack existingPotion, ItemStack clicked) throws CloneNotSupportedException {
        // previousClick is the item that was just clicked.
        PotionClick previousClick;
        List<String> lore;
        if (clicked == null && existingPotion == null) {
            previousClick = new PotionClick(true);
        } else {
            previousClick = new PotionClick(clicked.getItemMeta().getLocalizedName());
        }
        String click = previousClick.getClick();
        if (click.equalsIgnoreCase("select") && previousClick.getPage() == 0) {
            previousClick = new PotionClick(true);
        }

        // currentClick is what all the new items will display.
        PotionClick currentClick = previousClick.clone();

        // menus: select, type colour, effect type, effect duration, effect amplifier, ingredient, predecessor, name
        switch (click) {
            case "select": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion to Modify");
                currentClick.setClick("type");
                currentClick.setPage(0);
                int numPotions = 0;
                int pageNumToDisplay = previousClick.getPage();
                int totalPotions = 1 + allCustomPotions.size();
                int PAGESIZE = 51;
                boolean needsNextPage = (totalPotions - PAGESIZE * pageNumToDisplay) > PAGESIZE;

                // EXIT, slot 53.
                currentClick.setClick("exit");
                ItemStack barrier = new ItemStack(Material.BARRIER);
                barrier = newItemStackGUI(barrier, currentClick);
                inv.setItem(53, barrier);

                // NEXT PAGE, slot 44.
                currentClick.setClick("select");
                ItemStack next = needsNextPage ? new ItemStack(Material.LIME_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
                currentClick.setPage(needsNextPage ? pageNumToDisplay + 1 : 0);
                ItemMeta nextMeta = next.getItemMeta();
                nextMeta.setDisplayName(ChatColor.GREEN + "NEXT PAGE");
                nextMeta.setLocalizedName(currentClick.localizedNameFromPotionClick());
                if (needsNextPage) {
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GREEN + "Page " + (pageNumToDisplay + 1));
                    nextMeta.setLore(lore);
                }
                next.setItemMeta(nextMeta);
                next = newItemStackGUI(next, currentClick);
                inv.setItem(44, next);

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = pageNumToDisplay != 0 ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);
                currentClick.setPage(pageNumToDisplay != 0 ? pageNumToDisplay - 1 : 0);
                ItemMeta prevMeta = previous.getItemMeta();
                prevMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS PAGE");
                prevMeta.setLocalizedName(currentClick.localizedNameFromPotionClick());
                if (pageNumToDisplay != 0) {
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Page " + (pageNumToDisplay - 1));
                    prevMeta.setLore(lore);
                }
                previous.setItemMeta(prevMeta);
                previous = newItemStackGUI(previous, currentClick);
                inv.setItem(35, previous);

                // New potion.
                currentClick.setClick("type");
                currentClick.setPage(0);
                List<Colour> colourList = newColourList();
                if (pageNumToDisplay == 0) {
                    ItemStack potionItemStack = new ItemStack(Material.POTION);
                    PotionClick newClick = new PotionClick(false);
                    newClick.setClick("type");
                    PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
                    Colour h = colourList.get((new Random()).nextInt(17));
                    Color k = Color.fromRGB(h.getR(), h.getG(), h.getB());
                    meta.setColor(k);
                    meta.setDisplayName(chatColorFromColour(h) + "Create New Potion");
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Create a custom potion from scratch.");

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
                    lore.add(ChatColor.GOLD + "ALL INFORMATION GOES HERE.");
                    meta.setLore(lore);
                    meta.setDisplayName(chatColorFromColour(pi.colour) != null ? chatColorFromColour(pi.colour) + meta.getDisplayName() : chatColorFromColor(meta.getColor()) + meta.getDisplayName());
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
                PotionClick specialCLick = currentClick.clone();
                specialCLick.setPage(0); // UNIVERSAL
                currentClick.setClick("colour");
                currentClick.setPage(0);
                currentClick.setType(true);

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

                // EXIT, slot 53.
                specialCLick.setClick("exit");
                ItemStack currentPotion = existingPotion.clone();
                currentPotion = newItemStackGUI(currentPotion, specialCLick);
                inv.setItem(53, currentPotion);

                // NEXT MENU, slot 52.
                specialCLick.setClick("colour");
                ItemStack right = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta rightMeta = right.getItemMeta();
                rightMeta.setDisplayName(ChatColor.GREEN + "NEXT MENU");
                rightMeta.setLocalizedName(specialCLick.localizedNameFromPotionClick());
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Colour Selection");
                rightMeta.setLore(lore);
                right.setItemMeta(rightMeta);
                right = newItemStackGUI(right, specialCLick);
                inv.setItem(52, right);

                // PREVIOUS MENU, slot 51.
                specialCLick.setClick("select");
                ItemStack left = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                leftMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS MENU");
                leftMeta.setLocalizedName(specialCLick.localizedNameFromPotionClick());
                lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Potion Selection");
                lore.add(ChatColor.RED + "Warning: you will lose your unsaved changes!");
                lore.add(ChatColor.RED + "Save a completed potion to validate your changes.");
                leftMeta.setLore(lore);
                left.setItemMeta(leftMeta);
                left = newItemStackGUI(left, specialCLick);
                inv.setItem(51, left);

                // NEXT PAGE, slot 44.
                specialCLick.setClick("type");
                ItemStack next = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, specialCLick);
                inv.setItem(44, next);

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, specialCLick);
                inv.setItem(35, previous);

                player.openInventory(inv);
                break;
            }
            case "colour": {
                // TODO for colour, effecttype and type need to get rid of special click
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion Colour");
                PotionClick specialCLick = currentClick.clone();
                specialCLick.setPage(0); // UNIVERSAL
                currentClick.setClick("effect_type");
                currentClick.setPage(0);
                currentClick.setColour(true);

                // EXIT, slot 53.
                specialCLick.setClick("exit");
                ItemStack currentPotion = existingPotion.clone();
                currentPotion = newItemStackGUI(currentPotion, specialCLick);
                inv.setItem(53, currentPotion);

                // NEXT MENU, slot 52.
                specialCLick.setClick("effect_type");
                ItemStack right = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta rightMeta = right.getItemMeta();
                rightMeta.setDisplayName(ChatColor.GREEN + "NEXT MENU");
                rightMeta.setLocalizedName(specialCLick.localizedNameFromPotionClick());
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Effect Type(s) Selection");
                rightMeta.setLore(lore);
                right.setItemMeta(rightMeta);
                right = newItemStackGUI(right, specialCLick);
                inv.setItem(52, right);

                // PREVIOUS MENU, slot 51.
                specialCLick.setClick("type");
                ItemStack left = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                leftMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS MENU");
                leftMeta.setLocalizedName(specialCLick.localizedNameFromPotionClick());
                lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Type Selection");
                leftMeta.setLore(lore);
                left.setItemMeta(leftMeta);
                left = newItemStackGUI(left, specialCLick);
                inv.setItem(51, left);

                // NEXT PAGE, slot 44.
                specialCLick.setClick("colour");
                ItemStack next = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, specialCLick);
                inv.setItem(44, next);

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, specialCLick);
                inv.setItem(35, previous);

                // All potion colours.
                ArrayList<Colour> colourList = newColourList();
                for (Colour c : colourList) {
                    ItemStack potionItemStack = new ItemStack(existingPotion.getType());
                    PotionMeta meta = (PotionMeta) existingPotion.getItemMeta();
                    Color current = Color.fromRGB(c.getR(), c.getG(), c.getB());
                    String s = c.getName();
                    meta.setColor(current);
                    meta.setDisplayName(chatColorFromColour(c) + s);
                    meta.setLore(null);
                    potionItemStack.setItemMeta(meta);
                    potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                    inv.addItem(potionItemStack);
                }

                player.openInventory(inv);
                break;
            }
            case "effect_type": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select Effect Type(s)");
                PotionClick specialCLick = currentClick.clone();
                specialCLick.setPage(0); // UNIVERSAL
                currentClick.setClick("effect_dur");
                currentClick.setPage(0);
                currentClick.setEffectType(true);

                // EXIT, slot 53.
                specialCLick.setClick("exit");
                ItemStack currentPotion = existingPotion.clone();
                currentPotion = newItemStackGUI(currentPotion, specialCLick);
                inv.setItem(53, currentPotion);

                // NEXT MENU, slot 52.
                specialCLick.setClick("ingredient");
                ItemStack right = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta rightMeta = right.getItemMeta();
                rightMeta.setDisplayName(ChatColor.GREEN + "NEXT MENU");
                rightMeta.setLocalizedName(specialCLick.localizedNameFromPotionClick());
                lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Ingredient Selection");
                rightMeta.setLore(lore);
                right.setItemMeta(rightMeta);
                right = newItemStackGUI(right, specialCLick);
                inv.setItem(52, right);

                // PREVIOUS MENU, slot 51.
                specialCLick.setClick("colour");
                ItemStack left = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                leftMeta.setDisplayName(ChatColor.GOLD + "PREVIOUS MENU");
                leftMeta.setLocalizedName(specialCLick.localizedNameFromPotionClick());
                lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Colour Selection");
                leftMeta.setLore(lore);
                left.setItemMeta(leftMeta);
                left = newItemStackGUI(left, specialCLick);
                inv.setItem(51, left);

                // NEXT PAGE, slot 44.
                specialCLick.setClick("effect_type");
                ItemStack next = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, specialCLick);
                inv.setItem(44, next);

                // PREVIOUS PAGE, slot 35.
                ItemStack previous = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, specialCLick);
                inv.setItem(35, previous);

                // No potion effect.
                ItemStack potionItemStack = new ItemStack(existingPotion.getType());
                PotionMeta meta = (PotionMeta) existingPotion.getItemMeta();
                meta.setDisplayName(ChatColor.DARK_RED + "No Effects");
                meta.setLore(null);
                potionItemStack.setItemMeta(meta);
                potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                inv.addItem(potionItemStack);

                // All potion effect types.
                PotionEffectType[] petList = PotionEffectType.values();
                List<String> petNames = new ArrayList<>();
                for (PotionEffectType pet : petList) {
                    petNames.add(pet.getName());
                }
                Collections.sort(petNames);

                // TODO also need to set any existing effects to "remove" - left click to modify, right click to remove
                //      if (((PotionMeta) existingPotion.getItemMeta()).getCustomEffects(). contains it idk
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
                    lore = new ArrayList<>();
                    lore.add(ChatColor.GREEN + "Add " + pet.getName());
                    leftMeta.setLore(lore);
                    meta.setLore(lore);
                    potionItemStack.setItemMeta(meta);
                    potionItemStack = newItemStackGUI(potionItemStack, currentClick);
                    inv.addItem(potionItemStack);
                }

                player.openInventory(inv);
                break;
            }
            case "effect_dur": {
                anvil = new AnvilGUI.Builder();
                anvil.title(ChatColor.GOLD + "Effect Duration");
                // TODO use lore to add instructions
                //      clicking on left slot takes you back?? clicking on right continues you
                //      for effect_amp, clicking on left slot takes you back to the effect menu too
                anvil.text(ChatColor.RESET + "Duration in seconds");
                anvil.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.close());
                anvil.plugin(this);
                currentClick.setClick("effect_amp");
                currentClick.setEffectDur(true);

                ItemStack barrier = new ItemStack(Material.BARRIER);
                barrier = newItemStackGUI(barrier, currentClick);
                anvil.item(barrier);

                anvilInv = anvil.open(player);
                break;
            }
            case "effect_amp": {
                anvil = new AnvilGUI.Builder();
                // TODO the next menu for effect_type will be ingredient
                anvil.title(ChatColor.GOLD + "Effect Amplifier");
                anvil.text(ChatColor.RESET + "Amplifier from 0 to 127");
                anvil.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.close());
                anvil.plugin(this);
                currentClick.setClick("ingredient");
                currentClick.setEffectAmp(true);

                ItemStack barrier = new ItemStack(Material.BARRIER);
                barrier = newItemStackGUI(barrier, currentClick);
                anvil.item(barrier);

                anvilInv = anvil.open(player);
                break;
            }
            case "ingredient": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select an Ingredient");
                currentClick.setClick("predecessor");
                currentClick.setIngredient(true);
                ItemStack barrier = new ItemStack(Material.BARRIER);
                barrier = newItemStackGUI(barrier, currentClick);
                inv.setItem(53, barrier);

                ItemStack next = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, currentClick);
                inv.setItem(44, next);

                ItemStack previous = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, currentClick);
                inv.setItem(35, previous);

                player.openInventory(inv);
                break;
            }
            case "predecessor": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Predecessor");
                // TODO multiple predecessors
                currentClick.setClick("name");
                currentClick.setPredecessor(true);
                ItemStack barrier = new ItemStack(Material.BARRIER);
                barrier = newItemStackGUI(barrier, currentClick);
                inv.setItem(53, barrier);

                ItemStack next = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, currentClick);
                inv.setItem(44, next);

                ItemStack previous = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, currentClick);
                inv.setItem(35, previous);

                player.openInventory(inv);
                break;
            }
            case "name": {
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Enter a Name");
                currentClick.setClick("exit");
                currentClick.setName(true);
                //TODO anvil left side will be Revise Changes (yellow), middle Save and Exit (Green), right exit (red)
                // TODO left side will be "type", middle and right will both be exit.
                ItemStack barrier = new ItemStack(Material.BARRIER);
                barrier = newItemStackGUI(barrier, currentClick);
                inv.setItem(53, barrier);

                ItemStack next = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                next = newItemStackGUI(next, currentClick);
                inv.setItem(44, next);

                ItemStack previous = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
                previous = newItemStackGUI(previous, currentClick);
                inv.setItem(35, previous);

                player.openInventory(inv);
                break;
            }
        }




        /*// Test to generate 18 potions.
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
        String j = "Jimbering";
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r" + j));
        potion.setItemMeta(meta);
        inv.addItem(potion);

        potionData.getData().set(j + ".type", potion.getType().toString().toUpperCase());
        potionData.getData().set(j + ".colour.name", h.getName());
        potionData.getData().set(j + ".colour.red", h.getR());
        potionData.getData().set(j + ".colour.green", h.getG());
        potionData.getData().set(j + ".colour.blue", h.getB());
        potionData.getData().set(j + ".colour.blue", h.getB());
        potionData.getData().set(j + ".effects", "none");
        potionData.getData().set(j + ".ingredient", "SOUL_SAND");
        potionData.getData().set(j + ".predecessor.name", "Awkward Potion");
        potionData.getData().set(j + ".predecessor.type", "POTION");
        potionData.getData().set(j + ".predecessor.effects", "none");


        potion = new ItemStack(Material.POTION);
        meta = (PotionMeta) potion.getItemMeta();
        if (meta == null) {
            this.log.severe("Unknown error. Skipping the potion.");
            return true;
        }

        meta.addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 125), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 2505, 126), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 420, 127), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 420, 128), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 420, 129), true);

        int i = 0;
        for (Colour c : colourList) {
            Color current = Color.fromRGB(c.getR(), c.getG(), c.getB());
            String s = c.getName();
            meta.setColor(current);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r" + s));
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

            potionData.getData().set(s + ".ingredient", "BUCKET");
            potionData.getData().set(s + ".predecessor.name", "Jimbering");
            potionData.getData().set(s + ".predecessor.type", "LINGERING_POTION");
            potionData.getData().set(s + ".predecessor.effects", "none");

            i++;
            if (i >= 53) break;
        }
        potionData.saveData();*/


        return false;
    }

    // Modifying potions on command line for console.
    public boolean consoleModifyPotions (CommandSender sender) {


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

                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            event.setCursor(new ItemStack(Material.AIR));
                        }
                    }, 1L);
                } else if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    // Ingredient slot matches, insert item stack until ingredient slot full, or source empty.
                    int resultantStackSize = insertIngredient.getAmount() + existingIngredient.getAmount();
                    if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                        // Combined stack can be inserted in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(resultantStackSize);

                        // Insert ingredient and set cursor to nothing.
                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.setCursor(new ItemStack(Material.AIR));
                            }
                        }, 1L);
                    } else {
                        // Combined stack is too large to be placed in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(existingIngredient.getMaxStackSize());

                        // Insert ingredient and set cursor to resultant stack size.
                        ItemStack toCursor = insertIngredient.clone();
                        toCursor.setAmount(resultantStackSize - existingIngredient.getMaxStackSize());
                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.setCursor(toCursor);
                            }
                        }, 1L);
                    }
                } else {
                    // Ingredient slot does not match, swap cursor with existing ingredient.
                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            brewingStand.setIngredient(insertIngredient);
                            event.setCursor(existingIngredient);
                        }
                    }, 1L);
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

                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.setCursor(toCursor);
                            }
                        }, 1L);
                    } else if (event.getClick().equals(ClickType.RIGHT)) {
                        int resultantStackSize = existingIngredient.getAmount() + 1;
                        if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                            ItemStack toInsert = insertIngredient.clone();
                            toInsert.setAmount(resultantStackSize);

                            // Insert ingredient and set cursor to one less.
                            ItemStack ingredientMinusOne = insertIngredient.clone();
                            ingredientMinusOne.setAmount(insertIngredient.getAmount() - 1);

                            BukkitScheduler scheduler = getServer().getScheduler();
                            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                                @SuppressWarnings("deprecation")
                                @Override
                                public void run() {
                                    brewingStand.setIngredient(toInsert);
                                    event.setCursor(ingredientMinusOne);
                                }
                            }, 1L);
                        }
                    }
                } else {
                    // Swap cursor with existing ingredient.
                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            brewingStand.setIngredient(insertIngredient);
                            event.setCursor(existingIngredient);
                        }
                    }, 1L);
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

                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            event.setCursor(ingredientMinusOne);
                        }
                    }, 1L);
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

                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.setCursor(ingredientMinusOne);
                            }
                        }, 1L);
                    }
                }
                cancel = true;
                break;
        }

        // Allows moving ingredients to other side when brewing stand is full of ingredients.
        if (cancel) {
            event.setCancelled(true);
        }

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                ((Player)event.getWhoClicked()).updateInventory();
            }
        }, 2L);
        /*if (event.getClick().isLeftClick() && event.getClick().isShiftClick()) {
            ItemStack inBrewingStand = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType() == Material.AIR) {
                return;
            } else if (inBrewingStand == null) {
                event.getClickedInventory().setItem(event.getSlot(), cursor);
            } else if (cursor.isSimilar(inBrewingStand)) {
                ItemStack newBrewingStand = inBrewingStand.clone();
                newBrewingStand.setAmount(Math.max(inBrewingStand.getAmount() + cursor.getAmount(), inBrewingStand.getMaxStackSize()));
                event.getClickedInventory().setItem(event.getSlot(), newBrewingStand);

                ItemStack newCursor = cursor.clone();
                newCursor.setAmount();
                event.setCursor(newCursor);

            }
        }*/

    }

    @EventHandler
    public void clickGUI(InventoryClickEvent event) throws CloneNotSupportedException {
        Inventory inventory = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();

        // Fails if:
        // - clicked is an invalid item
        // - inventory is not the custom 54-slot and not the custom anvil
        // Succeeds if:
        // - clicked is valid AND inventory is the custom 54-slot
        // - clicked is valid AND inventory is not the custom 54-slot BUT is the custom anvil AND is on the right slot
        if (clicked == null || clicked.getItemMeta() == null || inventory == null) {
            return;
        } else if (inventory == inv) {
            // continue
        } else if (anvilInv == null || inventory != anvilInv.getInventory()) {
            return;
        }


        event.setCancelled(true);
        if (!isPotionClick(clicked.getItemMeta().getLocalizedName())) return;
        if (inventory != inv && anvilInv != null && event.getSlot() != AnvilGUI.Slot.OUTPUT) return;

        this.log.info(clicked.getItemMeta().getLocalizedName());

        if (clicked.getType() == Material.RED_STAINED_GLASS_PANE) {
            return;
        }

        if ((new PotionClick(clicked.getItemMeta().getLocalizedName())).getClick().equals("exit")) {
            ((Player) event.getWhoClicked()).closeInventory();
            return;
        } else if (anvilInv != null) {
            anvilInv.closeInventory();
        }

        boolean isPotion = clicked.getType() == Material.POTION || clicked.getType() == Material.SPLASH_POTION
                || clicked.getType() == Material.LINGERING_POTION;
        boolean isSelectPage = new PotionClick(clicked.getItemMeta().getLocalizedName()).getClick().equalsIgnoreCase("select");

        if (isPotion) {
            playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
        } else if (isSelectPage) {
            playerModifyPotions((Player) event.getWhoClicked(), null, clicked);
        } else {
            playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
        }

    }
}