package xyz.iiinitiationnn.custompotions;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {
    public static Logger log;
    public Data potionData;
    //public List<PotionInfo> allCustomPotions;
    public List<ItemStack> allVanillaPotions = newVanillaPotionsList();
    public List<PotionRecipe> allPotionRecipes; // TODO add all vanilla ones

    // TODO
    //  predecessor -> base
    //  ingredient -> reagent
    //  potentially playing around with lore for potion effects to show the correct potency, and time in day:hour:minute:second if applicable
    //  maybe in the distant future add presets like splash from existing or whatever
    //  handle weird gunpowder and dragons breath shenanigans??
    //  two predecessors cannot have same ingredients in helperRecipe ie. when loading from data (how we gonna initialise)
    //  right click potion in select menu to clone it
    //  when doing cp info, maybe find all potions which use it as a predecessor
    //  edit (left, gold), confirm (right, green), exit (bottom right or far right, red), back to main menu (bottom left or far left, red)

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
        log = this.getLogger();
        log.info("Initialising CustomPotions and validating potions.");
        this.potionData = new Data(this);
        potionData.saveDefaultData();
        //this.allCustomPotions = newPotionInfoList();
        //reinitialiseAllPotionRecipes();
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("custompotions").setExecutor(new Commands(this));
        this.getCommand("custompotions").setTabCompleter(new TabComplete(this));
    }

    // Reload.
    public void reload() {
        this.potionData.reloadData();
        //allCustomPotions = newPotionInfoList();
        // TODO close inventories
        resetGlobals();
        this.getCommand("custompotions").setExecutor(new Commands(this));
        this.getCommand("custompotions").setTabCompleter(new TabComplete(this));
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
                log.severe("Unknown error X10.");
                return null;
            }
            lore.add(ChatColor.GOLD + StringUtil.titleCase(recipe.ingredient.name(), "_") + " + "
                    + ChatColor.stripColor(ItemStackUtil.getDisplayName(this, recipe.predecessor)));
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
/*
    // Returns if two potions are the same (display name and effects).
    public boolean potionsAreSame(ItemStack potion1, ItemStack potion2) {
        return ItemStackUtil.resetLocalizedName(ItemStackUtil.resetLore(potion1)).isSimilar(ItemStackUtil.resetLocalizedName(ItemStackUtil.resetLore(potion2)));
    }*/

    // Determines if an item is a valid ingredient.
    public boolean isValidIngredient(Material material) {
        return material.isItem() && material != Material.AIR && material != Material.POTION &&
                material != Material.SPLASH_POTION && material != Material.LINGERING_POTION &&
                material != Material.DEBUG_STICK && material != Material.KNOWLEDGE_BOOK;
    }

    /*public ItemStack getVanillaPredecessor(ItemStack result) {
        boolean isExtended = ((PotionMeta) result.getItemMeta()).getBasePotionData().isExtended();
        boolean isUpgraded = ((PotionMeta) result.getItemMeta()).getBasePotionData().isUpgraded();
        if (!isExtended && !isUpgraded) {
            ItemStack potion = new ItemStack(result.getType());
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            if (meta == null) {
                this.log.severe("Unknown error X43.");
                return null;
            }
            meta.setBasePotionData(new PotionData(PotionType.AWKWARD, false, false));
            potion.setItemMeta(meta);
            return potion;
        } else {
            ItemStack potion = new ItemStack(result.getType());
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            if (meta == null) {
                this.log.severe("Unknown error X44.");
                return null;
            }
            meta.setBasePotionData(new PotionData(((PotionMeta) result.getItemMeta()).getBasePotionData().getType(), false, false));
            potion.setItemMeta(meta);
            return potion;
        }
    }*/
/*
    public void reinitialiseAllPotionRecipes() {
        allPotionRecipes = new ArrayList<>();
        for (ItemStack vanillaPotion : allVanillaPotions) {
            if (vanillaPotion.getItemMeta() == null) {
                this.log.severe("Unknown error X42.");
                return;
            }
            if (((PotionMeta) vanillaPotion.getItemMeta()).getBasePotionData().getType() == PotionType.WATER) continue;
            boolean isExtended = ((PotionMeta) vanillaPotion.getItemMeta()).getBasePotionData().isExtended();
            boolean isUpgraded = ((PotionMeta) vanillaPotion.getItemMeta()).getBasePotionData().isUpgraded();

            if (!isExtended && !isUpgraded) {
                switch (((PotionMeta) vanillaPotion.getItemMeta()).getBasePotionData().getType()) {
                    case NIGHT_VISION:
                        allPotionRecipes.add(new PotionRecipe(vanillaPotion, getVanillaPredecessor(vanillaPotion), Material.GOLDEN_CARROT, -1));
                        break;

                }
            } else if (isExtended) {

            } else {

            }


            if (vanillaPotion.getType() == Material.SPLASH_POTION) {

            } else if (vanillaPotion.getType() == Material.LINGERING_POTION) {

            }


        }

        this.log.info("There are " + allPotionRecipes.size() + " vanilla recipes");

        for (PotionInfo info : allCustomPotions) {
            allPotionRecipes.addAll(info.potionrecipes); // if it's removed from / modified in info.potionrecipes, does it get removed from allPotionRecipes too?
        }
    }
*/
    // TODO control whether certain things can be placed in the brewing stand
    /*public boolean isInsertableIngredient(Material ingredient) {
        for (PotionRecipe recipe : allPotionRecipes) {
            if ()
        }
    }*/

    // Convert number to Roman numerals for use in custom lore.


    // Print all useful information.
    public void printDebug(String localizedName) {
        this.log.info("localizedName: " + localizedName);
        this.log.info("type: " + effectTypeInput);
        this.log.info("dur: " + effectDurationInput);
        this.log.info("amp: " + effectAmplifierInput);
        this.log.info("name: " + potionNameInput);
        this.log.info("size of potionRecipesInput: " + potionRecipesInput.size());
        this.log.info("size of allPotionRecipes: " + allPotionRecipes.size());
        //this.log.info("size of allCustomPotions: " + allCustomPotions.size());
    }

    /*******************************************************************************************************************
    *                                                 POTION MANAGEMENT                                                *
    *******************************************************************************************************************/

    // List of all Vanilla potions.
    public List<ItemStack> newVanillaPotionsList () {

    }

/*
    // Match the PotionInfo from the ItemStack.
    public PotionInfo matchPotionInfo(ItemStack itemstack, List<PotionInfo> list) {
        for (PotionInfo info : list) {
            if (info.itemstack.isSimilar(itemstack)) {
                return info;
            }
        }
        return null;
    }
*/


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

        ItemStack newItem = item.clone();
        newItem.setItemMeta(meta);
        return newItem;
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
    //public boolean modifyPotions(CommandSender sender) {
        /*if (isInvOpened) {
            sender.sendMessage(ChatColor.RED + "Potion management is currently being undertaken by another user.");
            return true;
        }*/
        //resetGlobals();
        //return playerModifyPotions((Player) sender, null, null);
    //}
/*
    // Modifying potions with GUI for players.
    public boolean playerModifyPotions(Player player, ItemStack existingPotion, ItemStack clicked) {
        // previousClick is the info contained in the item that was just clicked.
        String previousClick;
        if (clicked == null && existingPotion == null) {
            previousClick = newLocalized(true);
        } else {
            if (clicked == null || clicked.getItemMeta() == null) {
                log.severe("Unknown error X8.");
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
                List<Colour> colourList = ColourUtil.newColourList();
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
                            "new_selected"), ColourUtil.chatColorFromColor(randomColor) + "New Potion", lore);
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
                inv.addItem(newItemStackGUI(potionItemStack, currentClick, ColourUtil.chatColorFromPotion(potionItemStack) + "Potion", null));

                potionItemStack = imprintMeta(new ItemStack(Material.SPLASH_POTION), existingPotion);
                inv.addItem(newItemStackGUI(potionItemStack, currentClick, ColourUtil.chatColorFromPotion(potionItemStack) + "Splash Potion", null));

                potionItemStack = imprintMeta(new ItemStack(Material.LINGERING_POTION), existingPotion);
                inv.addItem(newItemStackGUI(potionItemStack, currentClick, ColourUtil.chatColorFromPotion(potionItemStack) + "Lingering Potion", null));

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
                ArrayList<Colour> colourList = ColourUtil.newColourList();
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
                    inv.addItem(newItemStackGUI(potionItemStack, currentClick, ColourUtil.chatColorFromColor(meta.getColor()) + s, null));
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
                        ColourUtil.chatColorFromColor(ColourUtil.getColor(existingPotion)) + "NO EFFECTS", lore));

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

                    String commonName = StringUtil.toCommonName(petName);

                    lore = new ArrayList<>();
                    lore.add("");
                    if (hasEffect) {
                        lore.add(ChatColor.GOLD + "Left click to modify " + commonName + ".");
                        lore.add(ChatColor.RED + "Right click to remove " + commonName + ".");
                        lore.add(ChatColor.GOLD + "It has potency range I to " + intToRoman(PotionUtil.maxAmp(petName) + 1) + ".");
                        inv.addItem(newItemStackGUI(potionItemStack, setClick(currentClick, "effect_type_mixed"),
                                ColourUtil.chatColorFromColor(meta.getColor()) + pet.getName(), lore));
                    } else {
                        lore.add(ChatColor.GREEN + "Click to add " + commonName + ".");
                        lore.add(ChatColor.GOLD + "It has potency range I to " + intToRoman(PotionUtil.maxAmp(petName) + 1) + ".");
                        inv.addItem(newItemStackGUI(potionItemStack, currentClick,
                                ColourUtil.chatColorFromColor(meta.getColor()) + pet.getName(), lore));
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
                lore.add(ChatColor.GOLD + "Enter the effect amplifier (integer from 0 to " + PotionUtil.maxAmp(effectTypeInput) + ").");
                lore.add(ChatColor.GOLD + "0 means potency I, eg. " + StringUtil.toCommonName(effectTypeInput) + " I.");
                lore.add(ChatColor.GOLD + "Similarly, 1 means potency II, eg. " + StringUtil.toCommonName(effectTypeInput) + " II, and so on.");
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
                                StringUtil.normaliseCapitalise(material.name(), "_") + ".");
                        lore.add(ChatColor.RED + "Right click to remove all recipes using " + StringUtil.normaliseCapitalise(material.name(), "_") + ".");
                        inv.addItem(newItemStackGUI(new ItemStack(material), setClick(currentClick, "ingredient_mixed"), null, lore));
                    } else {
                        lore.add(ChatColor.GREEN + "Click to add a recipe with " + StringUtil.normaliseCapitalise(material.name(), "_") + ".");
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
/*
                // EXIT, slot 53.
                if (existingPotion == null) {
                    this.log.severe("Unknown error X23.");
                    return true;
                }
                inv.setItem(53, newItemStackGUI(existingPotion.clone(),
                        setClick(currentClick, "exit"), potionNameInput, null));

                // All vanilla potions.
                for (ItemStack vanillaPotion : allVanillaPotions) {
                    currentClick = setClick(currentClick, "predecessor_chosen");
                    // If the potion being modified has this recipe already.
                    for (PotionRecipe recipe : potionRecipesInput) {
                        if (recipe.ingredient == ingredientInput && potionsAreSame(recipe.predecessor, vanillaPotion)) {
                            currentClick = setClick(currentClick, "predecessor_remove");
                            break;
                        }
                    }

                    // If some other potion has this recipe already.
                    for (PotionRecipe recipe : allPotionRecipes) {
                        if (recipe.ingredient == ingredientInput && potionsAreSame(recipe.predecessor, vanillaPotion) &&
                                !getClick(currentClick).equals("predecessor_remove")) {
                            currentClick = setClick(currentClick, "predecessor_invalid");
                            break;
                        }
                    }

                    if (numPredecessors >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }

                    if (numPredecessors < PAGESIZE * pageNumToDisplay) {
                        numPredecessors++;
                        continue;
                    }

                    lore = new ArrayList<>();
                    lore.add("");
                    if (getClick(currentClick).equals("predecessor_invalid")) {
                        lore.add(ChatColor.DARK_RED + "You cannot select this recipe.");
                        lore.add(ChatColor.DARK_RED + "This recipe is already in use.");
                    } else if (getClick(currentClick).equals("predecessor_remove")) {
                        lore.add(ChatColor.RED + "Click to remove this recipe.");
                    } else {
                        lore.add(ChatColor.GREEN + "Click to add this recipe.");
                    }

                    inv.addItem(newItemStackGUI(vanillaPotion.clone(), currentClick, null, lore));
                    numPredecessors++;
                }

                // All custom potions from potions.yml.
                for (PotionInfo customPotion : allCustomPotions) {
                    if (numPredecessors >= PAGESIZE * (pageNumToDisplay + 1)) {
                        break;
                    }

                    currentClick = setClick(currentClick, "predecessor_chosen");
                    // If the potion being modified has this recipe already.
                    for (PotionRecipe recipe : potionRecipesInput) {
                        if (recipe.ingredient == ingredientInput && potionsAreSame(recipe.predecessor, customPotion.itemstack)) {
                            currentClick = setClick(currentClick, "predecessor_remove");
                            break;
                        }
                    }

                    // If some other potion has this recipe already.
                    boolean skip = false;
                    for (PotionRecipe recipe : allPotionRecipes) {
                        if (recipe.ingredient == ingredientInput && potionsAreSame(recipe.predecessor, customPotion.itemstack) &&
                                !getClick(currentClick).equals("predecessor_remove")) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) continue;

                    // If the potion is from memory and it is the same as the potion about to be added, don't add it.
                    if (potionFromMemory != null && potionsAreSame(potionFromMemory, customPotion.itemstack)) continue;

                    if (numPredecessors < PAGESIZE * pageNumToDisplay) {
                        numPredecessors++;
                        continue;
                    }

                    lore = new ArrayList<>();
                    lore.add("");
                    if (getClick(currentClick).equals("predecessor_remove")) {
                        lore.add(ChatColor.RED + "Click to remove this recipe.");
                    } else {
                        lore.add(ChatColor.GREEN + "Click to add this recipe.");
                    }

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
                lore.add(ChatColor.GOLD + "(Hint: Use &f to remove default italicisation!)");
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

    //    return false;
    //}
/*
    // Give potions to players.
    public boolean givePotions(Player player, ItemStack clicked) {
        String previousClick;
        if (clicked == null) {
            previousClick = "custompotions give-0 not not not not not";
        } else {
            if (clicked.getItemMeta() == null) {
                this.log.severe("Unknown error X40.");
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
    }*/

    /*******************************************************************************************************************
    *                                                      EVENTS                                                      *
    *******************************************************************************************************************/

    // Inserting an item into a brewing stand.
    @EventHandler(priority = EventPriority.HIGH)
    public void insertBrewingItem(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getInventory().getType() != InventoryType.BREWING) return;

        // TODO check if it is valid to be placed???

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

                if (existingIngredient == null) {
                    // Ingredient slot is empty, insert item stack directly.
                    brewingStand.setIngredient(insertIngredient);

                    getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                            event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR)), 0L);
                } else if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    // Ingredient slot matches, insert item stack until ingredient slot full, or source empty.
                    int resultantStackSize = insertIngredient.getAmount() + existingIngredient.getAmount();
                    if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                        // Combined stack can be inserted in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(resultantStackSize);

                        // Insert ingredient and set cursor to nothing.
                        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                            brewingStand.setIngredient(toInsert);
                            event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                        }, 0L);
                    } else {
                        // Combined stack is too large to be placed in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(existingIngredient.getMaxStackSize());

                        // Insert ingredient and set cursor to resultant stack size.
                        ItemStack toCursor = insertIngredient.clone();
                        toCursor.setAmount(resultantStackSize - existingIngredient.getMaxStackSize());
                        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                            brewingStand.setIngredient(toInsert);
                            event.getWhoClicked().setItemOnCursor(toCursor);
                        }, 0L);
                    }
                } else {
                    // Ingredient slot does not match, swap cursor with existing ingredient.
                    getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                        brewingStand.setIngredient(insertIngredient);
                        event.getWhoClicked().setItemOnCursor(existingIngredient);
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

                        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                            brewingStand.setIngredient(toInsert);
                            event.getWhoClicked().setItemOnCursor(toCursor);
                        }, 0L);
                    } else if (event.getClick().equals(ClickType.RIGHT)) {
                        int resultantStackSize = existingIngredient.getAmount() + 1;
                        if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                            ItemStack toInsert = insertIngredient.clone();
                            toInsert.setAmount(resultantStackSize);

                            // Insert ingredient and set cursor to one less.
                            ItemStack ingredientMinusOne = insertIngredient.clone();
                            ingredientMinusOne.setAmount(insertIngredient.getAmount() - 1);

                            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                                brewingStand.setIngredient(toInsert);
                                event.getWhoClicked().setItemOnCursor(ingredientMinusOne);
                            }, 0L);
                        }
                    }
                } else {
                    // Swap cursor with existing ingredient.
                    getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                        brewingStand.setIngredient(insertIngredient);
                        event.getWhoClicked().setItemOnCursor(existingIngredient);
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

                    getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                            event.getWhoClicked().setItemOnCursor(ingredientMinusOne), 0L);
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

                        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                            brewingStand.setIngredient(toInsert);
                            event.getWhoClicked().setItemOnCursor(ingredientMinusOne);
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

        getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                ((Player)event.getWhoClicked()).updateInventory(), 1L);
    }

    @EventHandler
    public void brewPotionClick(InventoryClickEvent event) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (event.getClickedInventory() == null) return;
            if (event.getInventory().getType() != InventoryType.BREWING) return;
            if (((BrewerInventory) event.getInventory()).getHolder() == null) return;
            if (((BrewerInventory) event.getInventory()).getIngredient() == null) return;

            PotionRecipe recipe = PotionRecipe.getRecipe((BrewerInventory) event.getInventory(), allPotionRecipes, this);
            if (recipe == null) return;
            if (((BrewerInventory) event.getInventory()).getHolder().getBrewingTime() == 0) {
                recipe.startBrewing((BrewerInventory) event.getInventory(), this, allPotionRecipes);
            } else {
                // TODO ?????
            }
        }, 1L);
    }
/*
    // Interacting with plugin's GUI.
    @EventHandler
    public void clickGUI(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();


        // TODO prevent placing in the custom inventories, including dragging event

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
                if (event.getClick() != ClickType.SHIFT_LEFT) {
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "You have withdrawn 1x " + clicked.getItemMeta().getDisplayName() + ChatColor.GREEN + ".");
                    event.getWhoClicked().getInventory().addItem(ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked)));
                } else {
                    int maxStackSize = clicked.getMaxStackSize();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "You have withdrawn " + maxStackSize + "x " + clicked.getItemMeta().getDisplayName() + ChatColor.GREEN + ".");
                    ItemStack clickedMax = clicked.clone();
                    clickedMax.setAmount(maxStackSize);
                    event.getWhoClicked().getInventory().addItem(ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clickedMax)));
                }
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

        if (inventory == inv && inventory != null) { // 54-slot inventory.
            // Invalid attempt to change page (red glass).
            if (getClick(localizedName).equalsIgnoreCase("invalid_page")) {
                return;
            }

            // Leave via slot 53 exit.
            if (getClick(localizedName).equals("exit")) {
                if (clicked.getType() == Material.BARRIER || !isCompletePotion(localizedName) || event.getClick() == ClickType.RIGHT) {
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

                    // Remove old information from data file.
                    if (potionFromMemory != null && potionFromMemory.getItemMeta() != null) {
                        allCustomPotions.remove(matchPotionInfo(potionFromMemory, allCustomPotions));
                        reinitialiseAllPotionRecipes();
                        potionData.getData().set(potionFromMemory.getItemMeta().getDisplayName(), null);
                    }

                    // Type + Colour.
                    potionData.getData().set(newName + ".type", clicked.getType().toString().toUpperCase());
                    potionData.getData().set(newName + ".colour.name", ColourUtil.colourNameFromColor(newColor));
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

                    // Fix all potions which use this potion as a predecessor.
                    if (potionFromMemory != null) {
                        for (PotionInfo customPotion : allCustomPotions) {
                            for (PotionRecipe recipe : customPotion.potionrecipes) {
                                if (potionsAreSame(potionFromMemory, recipe.predecessor)) {
                                    recipe.predecessor = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked));
                                    potionData.getData().set(customPotion.name + ".recipes." + recipe.index + ".predecessor",
                                            ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked)));
                                }
                            }
                        }
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
                    allCustomPotions.add(newPotionInfo(potionData.getData(), newName));

                    //printDebug(localizedName);
                    reinitialiseAllPotionRecipes();
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
                    potionFromMemory = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked));
                    for (PotionInfo customPotion : allCustomPotions) {
                        if (customPotion.itemstack.isSimilar(ItemStackUtil.resetLocalizedName(ItemStackUtil.resetLore(clicked)))) {
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
                    resetGlobals();
                    playerModifyPotions((Player) event.getWhoClicked(), null,
                            ItemStackUtil.setLocalizedName(clicked, setClick(localizedName, "select")));
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
                        clicked = ItemStackUtil.setLocalizedName(clicked, setClick(localizedName, "effect_dur"));
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

                // Ingredient -> Ingredient / Predecessor via selecting an ingredient.
                case "ingredient_mixed": {
                    if (event.getClick() == ClickType.LEFT) { // Modify.
                        ingredientInput = clicked.getType();
                        clicked = ItemStackUtil.setLocalizedName(clicked, setClick(localizedName, "predecessor"));
                        playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    } else if (event.getClick() == ClickType.RIGHT) { // Remove.
                        Material clickedType = clicked.getType();
                        potionRecipesInput.removeIf(recipe -> recipe.ingredient == clickedType);
                        String newLocalized = setRecipe(localizedName, potionRecipesInput.size() > 0);
                        clicked = ItemStackUtil.setLocalizedName(clicked, setClick(newLocalized, "ingredient"));
                        playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    }
                    return;
                }

                // Ingredient -> Predecessor via selecting an ingredient.
                case "ingredient_chosen": {
                    ingredientInput = clicked.getType();
                    clicked = ItemStackUtil.setLocalizedName(clicked, setClick(localizedName, "predecessor"));
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Predecessor -> Ingredient via previous menu.
                case "previous_menu_ingredient": {
                    ingredientInput = null;
                    String newLocalized = setRecipe(localizedName, potionRecipesInput.size() > 0);
                    clicked = ItemStackUtil.setLocalizedName(clicked, setClick(newLocalized, "ingredient"));
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Predecessor -> Predecessor via trying to select an invalid recipe.
                case "predecessor_invalid": {
                    return;
                }

                // Predecessor -> Ingredient via selecting a predecessor.
                case "predecessor_chosen": {
                    clicked = ItemStackUtil.setLocalizedName(clicked, setClick(setRecipe(localizedName, true), "ingredient"));
                    ItemStack chosenPredecessor = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked.clone()));
                    potionRecipesInput.add(new PotionRecipe(null, ItemStackUtil.resetLocalizedName(ItemStackUtil.resetLore(chosenPredecessor)), ingredientInput, -1));
                    ingredientInput = null;
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Predecessor -> Predecessor via removing a predecessor.
                case "predecessor_remove": {
                    ItemStack chosenPredecessor = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked.clone()));
                    potionRecipesInput.removeIf(recipe -> recipe.ingredient == ingredientInput &&
                            potionsAreSame(recipe.predecessor, chosenPredecessor));
                    allPotionRecipes.removeIf(recipe ->
                            recipe.ingredient == ingredientInput && potionsAreSame(recipe.predecessor, chosenPredecessor));
                    String newLocalized = setRecipe(localizedName, potionRecipesInput.size() > 0);
                    clicked = ItemStackUtil.setLocalizedName(clicked, setClick(newLocalized, "ingredient"));
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
                        allCustomPotions.remove(matchPotionInfo(potionFromMemory, allCustomPotions));
                        reinitialiseAllPotionRecipes();
                        potionData.getData().set(potionFromMemory.getItemMeta().getDisplayName(), null);
                        // TODO TODO TODO why isnt it being removed sometimes
                    }

                    // Type + Colour.
                    potionData.getData().set(newName + ".type", clicked.getType().name().toUpperCase());
                    potionData.getData().set(newName + ".colour.name", ColourUtil.colourNameFromColor(newColor));
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

                    // Fix all potions which use this potion as a predecessor.
                    if (potionFromMemory != null) {
                        for (PotionInfo customPotion : allCustomPotions) {
                            for (PotionRecipe recipe : customPotion.potionrecipes) {
                                if (potionsAreSame(potionFromMemory, recipe.predecessor)) {
                                    recipe.predecessor = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked));
                                    potionData.getData().set(customPotion.name + ".recipes." + recipe.index + ".predecessor",
                                            ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked)));
                                }
                            }
                        }
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
                    allCustomPotions.add(newPotionInfo(potionData.getData(), newName));

                    //printDebug(localizedName);
                    reinitialiseAllPotionRecipes();
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

                    //printDebug(localizedName);

                    int max = isLingeringPotion ? 26843545 : 107374182;
                    if (dur < 1 || dur > max) { // Not in range.
                        effectDurationInput = null;
                        return;
                    }

                    // effectDurationInput is valid.
                    if (PotionUtil.maxAmp(effectTypeInput) == 0) { // No need for inputting an amp.
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

                    //printDebug(localizedName);

                    if (amp < 0 || amp > PotionUtil.maxAmp(effectTypeInput)) { // Not in range.
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

                    // TODO cant have same name

                    //printDebug(localizedName);

                    PotionMeta namedMeta = ((PotionMeta) clicked.getItemMeta());
                    namedMeta.setDisplayName(potionNameInput);
                    clicked.setItemMeta(namedMeta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

            }
        }

        //printDebug(localizedName);

        if (inventory == null) {
            this.log.severe("Unknown error X41.");
            return;
        }

        if (isPotion) { // Pass the potion that was just clicked.
            playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
        } else { // Pass the potion which will be in the bottom right hand corner.
            playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
        }
    }
*/
}