package xyz.iiinitiationnn.custompotions;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.iiinitiationnn.custompotions.utils.ColourUtil;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;
import xyz.iiinitiationnn.custompotions.utils.StringUtil;

import java.util.*;

import static xyz.iiinitiationnn.custompotions.PotionReader.getCustomPotions;

public class InventoryGUI {
    // Constants
    private static final String INVALID_PAGE = "invalidPageSelection";
    private static final int INVENTORY_SIZE = 54;
    private static final int PAGE_SIZE_A = 51; // mainMenu
    private static final int PAGE_SIZE_B = 49; // recipeReagent, recipeBase
    private static final int PREVIOUS_PAGE_SLOT = 35;
    private static final int NEXT_PAGE_SLOT = 44;
    private static final int PREVIOUS_MENU_SLOT = 51;
    private static final int NEXT_MENU_SLOT = 52;
    private static final int EXIT_SLOT = 53;

    // Instance Variables
    private Inventory inv;
    private AnvilGUI.Builder anvilInv;

    // mainMenu, potionType, potionColour, effectType, effectDuration, effectAmplifier, recipeReagent, recipeBase, potionName, finalMenu
    // inventory gui will utilise Input to abstract away the processing
    // input can maybe be more lenient with duration eg. if someone says 3 hours 10 seconds you can parse for it
    // input will use clicked localized name as currentState, use action to determine nextState then pass to this

    // Constructor
    public InventoryGUI(LocalizedName newState, ItemStack existingPotion, String effectTypeName) {
        // New menu state information
        String menuClick = newState.getMenuClick();
        int page = newState.getPage();

        // Inventory GUI to be displayed

        /**
         * inventoryMenus = [mainMenu, potionType, ...]
         * anvilMenus = [effectDuration, effectAmplifier, potionName]
         * if menuClick in anvilMenus then set the anvil gui else do everything below
         */

        // dont handle actions like writing to data file here, since this is a constructor used to construct a gui based on previous action
        // should delegate that to listener which will delegate to another class ActionProcess or something like that

        switch (menuClick) {
            case "mainMenu":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion to Modify");
                setPreviousPageSlot(newState);
                setNextPageSlot(newState);
                setExitSlot(newState);
                addMainMenuPotions(newState);
                break;
            case "potionType":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion Type");
                setPreviousPageSlot(newState);
                setNextPageSlot(newState);
                setPreviousMenuSlot(newState);
                setNextMenuSlot(newState);

                setExitSlot(newState, existingPotion);
                addTypePotions(newState, existingPotion);
                break;
            case "potionColour":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion Colour");
                setPreviousPageSlot(newState);
                setNextPageSlot(newState);
                setPreviousMenuSlot(newState);
                setNextMenuSlot(newState);

                setExitSlot(newState, existingPotion);
                addColourPotions(newState, existingPotion);
                break;
            case "effectType":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select an Effect Type");
                setPreviousPageSlot(newState);
                setNextPageSlot(newState);
                setPreviousMenuSlot(newState);
                setNextMenuSlot(newState);

                setExitSlot(newState, existingPotion);
                addEffectTypePotions(newState, existingPotion);
                break;
            case "effectDuration":
                this.anvilInv = new AnvilGUI.Builder();
                this.anvilInv.title(ChatColor.GOLD + "Effect Duration");
                addEffectDurationPotion(newState, existingPotion);
                break;
            case "effectAmplifier":
                this.anvilInv = new AnvilGUI.Builder();
                this.anvilInv.title(ChatColor.GOLD + "Effect Amplifier");
                addEffectAmplifierPotion(newState, existingPotion, effectTypeName);
                break;
            case "recipeReagent":
                break;

        }
    }

    // Methods
    private void setPreviousPageSlot(LocalizedName newState) {
        String menuClick = newState.getMenuClick();
        int page = newState.getPage();
        boolean needsPreviousPage = page > 0;
        ItemStack selectPreviousPage = needsPreviousPage ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE)
                : new ItemStack(Material.RED_STAINED_GLASS_PANE);

        LocalizedName previousPageState = new LocalizedName(menuClick, page,
                needsPreviousPage ? "pagePrevious" : "pageInvalid");

        ItemStackUtil.setLocalizedName(selectPreviousPage, previousPageState.getName());
        ItemStackUtil.setDisplayName(selectPreviousPage, needsPreviousPage
                ? ChatColor.GOLD + "PREVIOUS PAGE" : ChatColor.RED + "NO PREVIOUS PAGE");

        if (needsPreviousPage) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Page " + page);
            ItemStackUtil.setLore(selectPreviousPage, lore);
        }

        this.inv.setItem(PREVIOUS_PAGE_SLOT, selectPreviousPage);
    }

    private void setNextPageSlot(LocalizedName newState) {
        String menuClick = newState.getMenuClick();
        int page = newState.getPage();
        boolean needsNextPage = page > 0;
        ItemStack selectNextPage = needsNextPage ? new ItemStack(Material.LIME_STAINED_GLASS_PANE)
                : new ItemStack(Material.RED_STAINED_GLASS_PANE);

        LocalizedName nextPageState = new LocalizedName(menuClick, page,
                needsNextPage ? "pageNext" : "pageInvalid");

        ItemStackUtil.setLocalizedName(selectNextPage, nextPageState.getName());
        ItemStackUtil.setDisplayName(selectNextPage, needsNextPage
                ? ChatColor.GREEN + "NEXT PAGE" : ChatColor.RED + "NO NEXT PAGE");

        if (needsNextPage) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Page " + (page + 2));
            ItemStackUtil.setLore(selectNextPage, lore);
        }
        this.inv.setItem(NEXT_PAGE_SLOT, selectNextPage);
    }

    /**
     * Sets the previous menu slot to a selector which brings the user to the previous menu.
     */
    private void setPreviousMenuSlot(LocalizedName newState) {
        String menuClick = newState.getMenuClick();
        ItemStack selectPreviousMenu = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);

        LocalizedName previousMenuState = new LocalizedName(menuClick, newState.getPage(), "skipL");

        List<String> lore = new ArrayList<>();
        switch (menuClick) {
            case "potionType":
                lore.add(ChatColor.GOLD + "Potion Selection");
                lore.add(ChatColor.RED + "Warning: you will lose your unsaved changes!");
                lore.add(ChatColor.RED + "Save a completed potion to validate your changes.");
                break;
            case "potionColour":
                lore.add(ChatColor.GOLD + "Potion Type Selection");
                break;
            case "effectType":
                lore.add(ChatColor.GOLD + "Colour Selection");
                break;
            case "recipeReagent":
                lore.add(ChatColor.GOLD + "Effect Type(s) Selection");
                break;
            case "recipeBase":
                lore.add(ChatColor.GOLD + "Recipe Reagent(s) Selection");
                lore.add(ChatColor.RED + "Warning: you will lose your choice of reagent!");
        }

        ItemStackUtil.setLocalizedName(selectPreviousMenu, previousMenuState.getName());
        ItemStackUtil.setDisplayName(selectPreviousMenu, ChatColor.GOLD + "PREVIOUS MENU");
        ItemStackUtil.setLore(selectPreviousMenu, lore);

        this.inv.setItem(PREVIOUS_MENU_SLOT, selectPreviousMenu);
    }

    /**
     * Sets the next menu slot to a selector which brings the user to the next menu.
     */
    private void setNextMenuSlot(LocalizedName newState) {
        String menuClick = newState.getMenuClick();
        ItemStack selectNextMenu = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

        LocalizedName nextMenuState = new LocalizedName(menuClick, newState.getPage(), "skipR");

        List<String> lore = new ArrayList<>();
        switch (menuClick) {
            case "potionType":
                lore.add(ChatColor.GREEN + "Colour Selection");
                break;
            case "potionColour":
                lore.add(ChatColor.GREEN + "Effect Type(s) Selection");
                break;
            case "effectType":
                lore.add(ChatColor.GREEN + "Recipe Reagent(s) Selection");
                break;
            case "recipeReagent":
                lore.add(ChatColor.GREEN + "Potion Naming");
                break;
            case "recipeBase":
                lore.add(ChatColor.GREEN + "Potion Naming");
                lore.add(ChatColor.RED + "Warning: you will lose your choice of reagent!");
        }

        ItemStackUtil.setLocalizedName(selectNextMenu, nextMenuState.getName());
        ItemStackUtil.setDisplayName(selectNextMenu, ChatColor.GREEN + "NEXT MENU");
        ItemStackUtil.setLore(selectNextMenu, lore);

        this.inv.setItem(NEXT_MENU_SLOT, selectNextMenu);
    }

    /**
     * Sets exit slot for the main menu using a barrier block.
     */
    private void setExitSlot(LocalizedName newState) {
        ItemStack selectExit = new ItemStack(Material.BARRIER);

        LocalizedName exitState = newState.clone();
        exitState.setAction("exit");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Click to exit.");

        ItemStackUtil.setLocalizedName(selectExit, exitState.getName());
        ItemStackUtil.setDisplayName(selectExit, ChatColor.RED + "EXIT");
        ItemStackUtil.setLore(selectExit,lore);

        this.inv.setItem(EXIT_SLOT, selectExit);
    }

    /**
     * Sets exit slot for any menu using the creator's existing potion.
     */
    private void setExitSlot(LocalizedName newState, ItemStack existingPotion) {
        ItemStack selectExit = existingPotion.clone();

        LocalizedName exitState = newState.clone();
        exitState.setAction("exit");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GREEN + "Left click to save your changes and exit.");
        lore.add(ChatColor.RED + "Right click to exit without saving.");

        ItemStackUtil.setLocalizedName(selectExit, exitState.getName());
        // we assume that the existingPotion has the correct display name already
        ItemStackUtil.setLore(selectExit,lore);

        this.inv.setItem(EXIT_SLOT, selectExit);
    }

    /**
     * Adds potions for the main menu including, if applicable:
     * a (random) new potion, and all existing potions.
     */
    private void addMainMenuPotions(LocalizedName newState) {
        String menuClick = newState.getMenuClick();
        int page = newState.getPage();

        List<ItemStack> allPotions = new ArrayList<>();
        ItemStack newPotion = getNewPotion();
        List<PotionObject> customPotions = getCustomPotions();
        if (newPotion == null || customPotions == null) {
            Main.log.severe("There was an error setting up the potions when creating an inventory GUI.");
            return;
        }

        allPotions.add(newPotion);
        LocalizedName nextState = new LocalizedName(menuClick, page, "selectPotion");
        for (PotionObject customPotion : customPotions) allPotions.add(customPotion.getPotion());
        nextState.nextMenu();
        for (ItemStack potion : allPotions.subList(page * PAGE_SIZE_A, (page + 1) * PAGE_SIZE_A)) {
            ItemStackUtil.setLocalizedName(potion, nextState.getName());
            //TODO potion.addRecipesLore();
            this.inv.addItem(potion);
        }
    }

    /**
     * Adds potions for the potion type selection menu including:
     * potions, splash potions, and lingering potions.
     */
    private void addTypePotions(LocalizedName newState, ItemStack existingPotion) {
        LocalizedName nextState = new LocalizedName(newState.getMenuClick(), newState.getPage(), "selectType");
        ChatColor c = ColourUtil.getChatColor(existingPotion);

        // Potion
        ItemStack potion = existingPotion.clone();
        potion.setType(Material.POTION);
        ItemStackUtil.setLocalizedName(potion, nextState.getName());
        ItemStackUtil.setDisplayName(potion, c + "Potion");
        this.inv.addItem(potion);

        // Splash Potion
        ItemStack splashPotion = existingPotion.clone();
        splashPotion.setType(Material.SPLASH_POTION);
        ItemStackUtil.setLocalizedName(splashPotion, nextState.getName());
        ItemStackUtil.setDisplayName(splashPotion, c + "Splash Potion");
        this.inv.addItem(splashPotion);

        // Potion
        ItemStack lingeringPotion = existingPotion.clone();
        lingeringPotion.setType(Material.LINGERING_POTION);
        ItemStackUtil.setLocalizedName(lingeringPotion, nextState.getName());
        ItemStackUtil.setDisplayName(lingeringPotion, c + "Lingering Potion");
        this.inv.addItem(lingeringPotion);
    }

    /**
     * Adds potions for the potion colour selection menu.
     */
    private void addColourPotions(LocalizedName newState, ItemStack existingPotion) {
        LocalizedName nextState = new LocalizedName(newState.getMenuClick(), newState.getPage(), "selectColour");
        List<Colour> colours = ColourUtil.defaultPotionColourList();

        for (Colour colour : colours) {
            ItemStack potion = existingPotion.clone();
            ItemStackUtil.setLocalizedName(potion, nextState.getName());
            ItemStackUtil.setColor(potion, colour);

            ChatColor c = ColourUtil.getChatColor(colour);
            ItemStackUtil.setDisplayName(potion, c + ColourUtil.defaultPotionColourMapReverse().get(colour));
            this.inv.addItem(potion);
        }
    }

    /**
     * Adds potions for the potion effect type selection menu.
     */
    private void addEffectTypePotions(LocalizedName newState, ItemStack existingPotion) {
        LocalizedName nextStateA = new LocalizedName(newState.getMenuClick(), newState.getPage(), "addEffectType");
        LocalizedName nextStateS = new LocalizedName(newState.getMenuClick(), newState.getPage(), "selectEffectType");
        ChatColor c = ColourUtil.getChatColor(existingPotion);

        // No Effects
        ItemStack plainPotion = existingPotion.clone();
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This potion will have no effects.");
        ItemStackUtil.setLocalizedName(plainPotion, nextStateA.getName());
        ItemStackUtil.setDisplayName(plainPotion, c + "NO EFFECTS");
        ItemStackUtil.setLore(plainPotion, lore);
        this.inv.addItem(plainPotion);

        // Effects
        List<PotionEffectType> effectTypeList = Arrays.asList(PotionEffectType.values());
        effectTypeList.sort(Comparator.comparing(PotionEffectType :: getName)); // sort list by effect name
        for (PotionEffectType effectType : effectTypeList) {
            ItemStack potion = existingPotion.clone();
            String commonName = StringUtil.toCommonName(effectType.getName());

            lore = new ArrayList<>();
            lore.add("");
            if (ItemStackUtil.hasEffect(potion, effectType)) {
                lore.add(ChatColor.GOLD + "Left click to modify " + commonName + ".");
                lore.add(ChatColor.RED + "Right click to remove " + commonName + ".");
                ItemStackUtil.setLocalizedName(potion, nextStateS.getName());
            } else {
                lore.add(ChatColor.GOLD + "Click to add " + commonName + ".");
                ItemStackUtil.setLocalizedName(potion, nextStateA.getName());
            }
            lore.add(ChatColor.GOLD + "It has potency range 1 to " + PotionUtil.maxAmp(effectType.getName()) + 1 + ".");
            ItemStackUtil.setDisplayName(potion, c + effectType.getName());
            ItemStackUtil.setLore(potion, lore);
            this.inv.addItem(potion);
        }
    }

    /**
     * Adds a potion for the effect duration menu.
     */
    private void addEffectDurationPotion(LocalizedName newState, ItemStack existingPotion) {
        LocalizedName nextState = new LocalizedName(newState.getMenuClick(), newState.getPage(), "enterEffectDuration");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        if (existingPotion.getType() == Material.LINGERING_POTION) {
            lore.add(ChatColor.GOLD + "Enter the effect duration in seconds (1 to 26,843,545).");
        } else {
            lore.add(ChatColor.GOLD + "Enter the effect duration in seconds (1 to 107,374,182).");
        }
        lore.add(ChatColor.GREEN + "Click the output slot to continue.");
        lore.add(ChatColor.GOLD + "Click the left input slot to skip (if you have misclicked).");
        lore.add(ChatColor.RED + "Press ESC to exit without saving.");

        ItemStack potion = existingPotion.clone();
        ItemStackUtil.setLocalizedName(potion, nextState.getName());
        ItemStackUtil.setLore(potion, lore);

        this.anvilInv.text(ChatColor.RESET + "Enter here:");
        this.anvilInv.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.text("Enter here:")); // TODO send to process handler skip the listener and also onLeftInputClick and onRightInputClick
        this.anvilInv.plugin(Main.getPlugin(Main.class));
    }

    /**
     * Adds a potion for the effect amplifier menu.
     */
    private void addEffectAmplifierPotion(LocalizedName newState, ItemStack existingPotion, String effectTypeName) {
        LocalizedName nextState = new LocalizedName(newState.getMenuClick(), newState.getPage(), "enterEffectAmplifier");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GOLD + "Enter the effect amplifier (integer from 1 to " + PotionUtil.maxAmp(effectTypeName) + 1 + ").");
        lore.add(ChatColor.GOLD + "1 means potency I, eg. " + StringUtil.toCommonName(effectTypeName) + " I.");
        lore.add(ChatColor.GOLD + "Similarly, 2 means potency II, eg. " + StringUtil.toCommonName(effectTypeName) + " II, and so on.");
        lore.add(ChatColor.GREEN + "Click the output slot to continue.");
        lore.add(ChatColor.GOLD + "Click the left input slot to skip (if you have misclicked).");
        lore.add(ChatColor.RED + "Press ESC to exit without saving.");

        ItemStack potion = existingPotion.clone();
        ItemStackUtil.setLocalizedName(potion, nextState.getName());
        ItemStackUtil.setLore(potion, lore);

        this.anvilInv.text(ChatColor.RESET + "Enter here:");
        this.anvilInv.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.text("Enter here:")); // TODO send to process handler skip the listener and also onLeftInputClick and onRightInputClick
        this.anvilInv.plugin(Main.getPlugin(Main.class));
    }





    /**
     * Returns a potion indicating a new potion is to be created.
     */
    private ItemStack getNewPotion() {
        // New potion item
        ItemStack potion = new ItemStack(Material.POTION);
        LocalizedName nextState = new LocalizedName("mainMenu", 0, "createPotion");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Create a new custom potion from scratch.");

        Colour randomColour = new Colour();
        ItemStackUtil.setColor(potion, randomColour);
        ItemStackUtil.setDisplayName(potion, ColourUtil.getChatColor(randomColour) + "New Potion");
        ItemStackUtil.setLocalizedName(potion, nextState.getName());
        ItemStackUtil.setLore(potion, lore);

        return potion;
    }

    public static List<String> getAllMenus() {
        List<String> menus = new ArrayList<>();
        menus.add("mainMenu");
        menus.add("potionType");
        menus.add("potionColour");
        menus.add("effectType");
        menus.add("effectDuration");
        menus.add("effectAmplifier");
        menus.add("recipeReagent");
        menus.add("recipeBase");
        menus.add("potionName");
        menus.add("finalMenu");
        return menus;
    }

    public static String getMenuAfter(String currentMenu) {
        List<String> allMenus = getAllMenus();
        int index = allMenus.indexOf(currentMenu);
        int newIndex = (index + 1) % allMenus.size();
        return allMenus.get(newIndex);
    }

    public static String getMenuBefore(String currentMenu) {
        List<String> allMenus = getAllMenus();
        int index = allMenus.indexOf(currentMenu);
        int newIndex = (index - 1) % allMenus.size();
        return allMenus.get(newIndex);
    }

    public Inventory getInv() {
        return this.inv;
    }

}
