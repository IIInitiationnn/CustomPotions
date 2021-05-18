package xyz.iiinitiationnn.custompotions;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.iiinitiationnn.custompotions.utils.ColourUtil;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;
import xyz.iiinitiationnn.custompotions.utils.StringUtil;

import java.util.*;

public class InventoryGUI {
    // Constants
    private static final int INVENTORY_SIZE = 54;
    private static final int PAGE_SIZE_A = 51; // mainMenu
    private static final int PAGE_SIZE_B = 49; // recipeIngredient
    private static final int PAGE_SIZE_C = 50; // recipeBase

    private static final int PREVIOUS_PAGE_SLOT = 35;
    private static final int NEXT_PAGE_SLOT = 44;
    private static final int PREVIOUS_MENU_SLOT = 51;
    private static final int NEXT_MENU_SLOT = 52;
    private static final int EXIT_SLOT = 53;
    private static final int FINAL_POTION_SLOT = 22;
    private static final int FINAL_EDIT_SLOT = 30;
    private static final int FINAL_CONFIRM_SLOT = 31;
    private static final int FINAL_EXIT_SLOT = 32;

    // Instance Variables
    private Inventory inv = null;
    private AnvilGUI.Builder anvilInv = null;

    // mainMenu, potionType, potionColour, effectType, effectDuration, effectAmplifier, recipeIngredient, recipeBase, potionName, finalMenu
    // inventory gui will utilise Input to abstract away the processing
    // input can maybe be more lenient with duration eg. if someone says 3 hours 10 seconds you can parse for it
    // input will use clicked localized name as currentState, use action to determine nextState then pass to this

    // don't handle actions like writing to data file here, since this is a constructor used to construct a gui based on previous action
    // should delegate that to listener which will delegate to another class ActionProcess or something like that

    // Constructors
    public InventoryGUI(LocalizedName state, PotionObject existingPotion, Material recipeIngredient) {
        switch (state.getMenu()) {
            case "mainMenu":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion to Modify");
                setPreviousPageSlot(state);
                setNextPageSlot(state);
                setExitSlot(state);
                addMainMenuPotions(state);
                break;
            case "potionType":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion Type");
                setPreviousPageSlot(state);
                setNextPageSlot(state);
                setPreviousMenuSlot(state);
                setNextMenuSlot(state);

                setExitSlot(state, existingPotion);
                addTypePotions(state, existingPotion.getPotion());
                break;
            case "potionColour":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion Colour");
                setPreviousPageSlot(state);
                setNextPageSlot(state);
                setPreviousMenuSlot(state);
                setNextMenuSlot(state);

                setExitSlot(state, existingPotion);
                addColourPotions(state, existingPotion.getPotion());
                break;
            case "effectType":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select an Effect Type");
                setPreviousPageSlot(state);
                setNextPageSlot(state);
                setPreviousMenuSlot(state);
                setNextMenuSlot(state);

                setExitSlot(state, existingPotion);
                addEffectTypePotions(state, existingPotion.getPotion());
                break;
            case "effectDuration":
                this.anvilInv = new AnvilGUI.Builder();
                this.anvilInv.title(ChatColor.GOLD + "Effect Duration");
                addEffectDurationPotion(state, existingPotion.getPotion());
                break;
            case "effectAmplifier":
                this.anvilInv = new AnvilGUI.Builder();
                this.anvilInv.title(ChatColor.GOLD + "Effect Amplifier");
                addEffectAmplifierPotion(state, existingPotion.getPotion());
                break;
            case "recipeIngredient":
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select an Ingredient for a Recipe");
                setPreviousPageSlot(state);
                setNextPageSlot(state);
                setPreviousMenuSlot(state);
                setNextMenuSlot(state);

                setExitSlot(state, existingPotion);
                addIngredients(state, existingPotion);
                break;
            case "recipeBase":
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Base for the Recipe");
                setPreviousPageSlot(state);
                setNextPageSlot(state);
                setPreviousMenuSlot(state, PREVIOUS_MENU_SLOT + 1);

                setExitSlot(state, existingPotion);
                addBases(state, existingPotion, recipeIngredient);
                break;
            case "potionName":
                this.anvilInv = new AnvilGUI.Builder();
                this.anvilInv.title(ChatColor.GOLD + "Enter a Name");
                addNamePotion(state, existingPotion.getPotion());
                break;
            case "finalMenu":
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Confirm Changes");
                setFinalPotionSlot(state, existingPotion);
                setFinalEditSlot(state);
                setFinalConfirmSlot(state);
                setFinalExitSlot(state);
                break;
        }
    }

    public InventoryGUI(LocalizedName state, PotionObject existingPotion) {
        this(state, existingPotion, null);
    }

    public InventoryGUI(LocalizedName state) {
        this(state, null, null);
    }

    public InventoryGUI() {
        this(new LocalizedName(), null, null);
    }


    // Methods
    /**
     * Sets the previous page slot to a selector which brings the user to the previous page of the same menu.
     */
    private void setPreviousPageSlot(LocalizedName state) {
        int page = state.getPage();
        boolean needsPreviousPage = page > 0;
        ItemStack selector = needsPreviousPage ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE)
                : new ItemStack(Material.RED_STAINED_GLASS_PANE);

        LocalizedName previousPageState = state.clone();
        previousPageState.setAction(needsPreviousPage ? "pagePrevious" : "pageInvalid");

        ItemStackUtil.setLocalizedName(selector, previousPageState.toLocalizedString());
        ItemStackUtil.setDisplayName(selector, needsPreviousPage
                ? ChatColor.GOLD + "PREVIOUS PAGE" : ChatColor.RED + "NO PREVIOUS PAGE");

        if (needsPreviousPage) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Page " + page);
            ItemStackUtil.addLore(selector, lore);
        }

        this.inv.setItem(PREVIOUS_PAGE_SLOT, selector);
    }

    /**
     * Sets the next page slot to a selector which brings the user to the next page of the same menu.
     */
    private void setNextPageSlot(LocalizedName state) {
        int page = state.getPage();
        boolean needsNextPage = page > 0;
        ItemStack selector = needsNextPage ? new ItemStack(Material.LIME_STAINED_GLASS_PANE)
                : new ItemStack(Material.RED_STAINED_GLASS_PANE);

        LocalizedName nextPageState = state.clone();
        nextPageState.setAction(needsNextPage ? "pageNext" : "pageInvalid");

        ItemStackUtil.setLocalizedName(selector, nextPageState.toLocalizedString());
        ItemStackUtil.setDisplayName(selector, needsNextPage
                ? ChatColor.GREEN + "NEXT PAGE" : ChatColor.RED + "NO NEXT PAGE");

        if (needsNextPage) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Page " + (page + 2));
            ItemStackUtil.addLore(selector, lore);
        }
        this.inv.setItem(NEXT_PAGE_SLOT, selector);
    }

    /**
     * Sets the previous menu slot to a selector which brings the user to the previous menu.
     */
    private void setPreviousMenuSlot(LocalizedName state) {
        setPreviousMenuSlot(state, PREVIOUS_MENU_SLOT);
    }

    /**
     * Sets the previous menu slot to a selector which brings the user to the previous menu.
     */
    private void setPreviousMenuSlot(LocalizedName state, int slot) {
        ItemStack selector = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);

        LocalizedName previousMenuState = state.clone();
        previousMenuState.setAction("skipL");

        List<String> lore = new ArrayList<>();
        switch (state.getMenu()) {
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
            case "recipeIngredient":
                lore.add(ChatColor.GOLD + "Effect Type(s) Selection");
                break;
            case "recipeBase":
                lore.add(ChatColor.GOLD + "Recipe Ingredient(s) Selection");
                lore.add(ChatColor.RED + "Warning: you will lose your choice of ingredient!");
        }

        ItemStackUtil.setLocalizedName(selector, previousMenuState.toLocalizedString());
        ItemStackUtil.setDisplayName(selector, ChatColor.GOLD + "PREVIOUS MENU");
        ItemStackUtil.addLore(selector, lore);

        this.inv.setItem(slot, selector);
    }

    /**
     * Sets the next menu slot to a selector which brings the user to the next menu.
     */
    private void setNextMenuSlot(LocalizedName state) {
        ItemStack selector = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

        LocalizedName nextMenuState = state.clone();
        nextMenuState.setAction("skipR");

        List<String> lore = new ArrayList<>();
        switch (state.getMenu()) {
            case "potionType":
                lore.add(ChatColor.GREEN + "Colour Selection");
                break;
            case "potionColour":
                lore.add(ChatColor.GREEN + "Effect Type(s) Selection");
                break;
            case "effectType":
                lore.add(ChatColor.GREEN + "Recipe Ingredient(s) Selection");
                break;
            case "recipeIngredient":
                lore.add(ChatColor.GREEN + "Potion Naming");
                break;
        }

        ItemStackUtil.setLocalizedName(selector, nextMenuState.toLocalizedString());
        ItemStackUtil.setDisplayName(selector, ChatColor.GREEN + "NEXT MENU");
        ItemStackUtil.addLore(selector, lore);

        this.inv.setItem(NEXT_MENU_SLOT, selector);
    }

    /**
     * Sets exit slot for the main menu using a barrier block.
     */
    private void setExitSlot(LocalizedName state) {
        ItemStack selector = new ItemStack(Material.BARRIER);

        LocalizedName exitState = state.clone();
        exitState.setAction("exit");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Click to exit.");

        ItemStackUtil.setLocalizedName(selector, exitState.toLocalizedString());
        ItemStackUtil.setDisplayName(selector, ChatColor.RED + "EXIT");
        ItemStackUtil.addLore(selector,lore);

        this.inv.setItem(EXIT_SLOT, selector);
    }

    /**
     * Sets exit slot for any menu using the creator's existing potion.
     */
    private void setExitSlot(LocalizedName state, PotionObject existingPotion) {
        ItemStack selector = existingPotion.getPotion().clone();

        LocalizedName exitState = state.clone();
        exitState.setAction("exit");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GREEN + "Left click to save your changes and exit.");
        lore.add(ChatColor.RED + "Right click to exit without saving.");

        ItemStackUtil.setLocalizedName(selector, exitState.toLocalizedString());
        PotionUtil.addLoreRecipes(selector, existingPotion);
        // we assume that the existingPotion has the correct display name already
        ItemStackUtil.addLore(selector,lore);

        this.inv.setItem(EXIT_SLOT, selector);
    }

    /**
     * Adds potions for the main menu including, if applicable:
     * a (random) new potion, and all existing potions.
     */
    private void addMainMenuPotions(LocalizedName state) {
        int page = state.getPage();
        LocalizedName nextState = state.clone();
        nextState.setAction("selectPotion");

        List<ItemStack> allPotions = new ArrayList<>();
        ItemStack newPotion = getNewPotion();
        List<PotionObject> customPotions = PotionReader.getCustomPotions();

        allPotions.add(newPotion);
        for (PotionObject customPotion : customPotions) {
            nextState.setPotionID(customPotion.getPotionID());
            nextState.setPotionRecipes(customPotion.getRecipes());
            nextState.setPotionName(customPotion.getName());

            ItemStack customPotionItem = customPotion.getPotion();
            ItemStackUtil.setLocalizedName(customPotionItem, nextState.toLocalizedString());
            PotionUtil.addLoreRecipes(customPotionItem, customPotion);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GREEN + "Shift click to clone " + customPotion.getName() + ChatColor.GREEN + ".");
            lore.add(ChatColor.GOLD + "Left click to modify " + customPotion.getName() + ChatColor.GOLD + ".");
            lore.add(ChatColor.RED + "Right click to remove " + customPotion.getName() + ChatColor.RED + ".");
            ItemStackUtil.addLore(customPotionItem, lore);

            allPotions.add(customPotionItem);
        }
        for (ItemStack potion : allPotions.subList(page * PAGE_SIZE_A,
            Math.min((page + 1) * PAGE_SIZE_A, allPotions.size()))) {
            this.inv.addItem(potion);
        }
    }

    /**
     * Returns a potion indicating a new potion is to be created.
     */
    private ItemStack getNewPotion() {
        // New potion item
        ItemStack potion = new ItemStack(Material.POTION);
        LocalizedName nextState = new LocalizedName();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Create a new custom potion from scratch.");

        Colour randomColour = new Colour();
        PotionUtil.setColor(potion, randomColour);
        ItemStackUtil.setDisplayName(potion, ColourUtil.getChatColor(randomColour) + "New Potion");
        ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
        ItemStackUtil.addLore(potion, lore);

        return potion;
    }

    /**
     * Adds potions for the potion type selection menu including:
     * potions, splash potions, and lingering potions.
     */
    private void addTypePotions(LocalizedName state, ItemStack existingPotion) {
        LocalizedName nextState = state.clone();
        nextState.setAction("selectType");
        ChatColor c = ColourUtil.getChatColor(existingPotion);

        // Potion
        ItemStack potion = existingPotion.clone();
        potion.setType(Material.POTION);
        ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
        ItemStackUtil.setDisplayName(potion, c + "Potion");
        this.inv.addItem(potion);

        // Splash Potion
        ItemStack splashPotion = existingPotion.clone();
        splashPotion.setType(Material.SPLASH_POTION);
        ItemStackUtil.setLocalizedName(splashPotion, nextState.toLocalizedString());
        ItemStackUtil.setDisplayName(splashPotion, c + "Splash Potion");
        this.inv.addItem(splashPotion);

        // Potion
        ItemStack lingeringPotion = existingPotion.clone();
        lingeringPotion.setType(Material.LINGERING_POTION);
        ItemStackUtil.setLocalizedName(lingeringPotion, nextState.toLocalizedString());
        ItemStackUtil.setDisplayName(lingeringPotion, c + "Lingering Potion");
        this.inv.addItem(lingeringPotion);
    }

    /**
     * Adds potions for the potion colour selection menu.
     */
    private void addColourPotions(LocalizedName state, ItemStack existingPotion) {
        LocalizedName nextState = state.clone();
        nextState.setAction("selectColour");
        List<Colour> colours = ColourUtil.defaultPotionColourList();

        for (Colour colour : colours) {
            ItemStack potion = existingPotion.clone();
            ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
            PotionUtil.setColor(potion, colour);

            ChatColor c = ColourUtil.getChatColor(colour);
            ItemStackUtil.setDisplayName(potion, c + ColourUtil.defaultPotionColourMapReverse().get(colour));
            this.inv.addItem(potion);
        }
    }

    /**
     * Adds potions for the potion effect type selection menu.
     */
    private void addEffectTypePotions(LocalizedName state, ItemStack existingPotion) {
        LocalizedName nextStateA = state.clone();
        nextStateA.setAction("addEffectType");
        LocalizedName nextStateS = state.clone();
        nextStateS.setAction("selectEffectType");
        ChatColor c = ColourUtil.getChatColor(existingPotion);

        // No Effects
        ItemStack plainPotion = existingPotion.clone();
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This potion will have no effects.");
        ItemStackUtil.setLocalizedName(plainPotion, nextStateA.toLocalizedString());
        ItemStackUtil.setDisplayName(plainPotion, c + "NO EFFECTS");
        ItemStackUtil.addLore(plainPotion, lore);
        this.inv.addItem(plainPotion);

        // Effects
        List<PotionEffectType> effectTypeList = Arrays.asList(PotionEffectType.values());
        effectTypeList.sort(Comparator.comparing(PotionEffectType :: getName)); // sort list by effect name
        for (PotionEffectType effectType : effectTypeList) {
            ItemStack potion = existingPotion.clone();
            String commonName = StringUtil.toCommonName(effectType.getName());

            lore = new ArrayList<>();
            lore.add("");
            if (PotionUtil.hasEffect(potion, effectType)) {
                lore.add(ChatColor.GOLD + "Left click to modify " + commonName + ".");
                lore.add(ChatColor.RED + "Right click to remove " + commonName + ".");
                ItemStackUtil.setLocalizedName(potion, nextStateS.toLocalizedString());
            } else {
                lore.add(ChatColor.GREEN + "Click to add " + commonName + ".");
                ItemStackUtil.setLocalizedName(potion, nextStateA.toLocalizedString());
            }
            lore.add(ChatColor.GOLD + "It has potency range 1 to " + (PotionUtil.maxAmp(effectType.getName()) + 1) + ".");
            ItemStackUtil.setDisplayName(potion, c + effectType.getName());
            ItemStackUtil.addLore(potion, lore);
            this.inv.addItem(potion);
        }
    }

    /**
     * Adds a potion for the effect duration menu.
     */
    private void addEffectDurationPotion(LocalizedName state, ItemStack existingPotion) {
        LocalizedName nextState = state.clone();
        nextState.setAction("enterEffectDuration");
        nextState.setExtraField(state.getExtraField());

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
        ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
        ItemStackUtil.addLore(potion, lore);

        this.anvilInv.text(ChatColor.RESET + "Enter here:");
        this.anvilInv.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.text("Enter here:")); // TODO send to process handler skip the listener and also onLeftInputClick and onRightInputClick
        this.anvilInv.plugin(Main.getPlugin(Main.class));
    }

    /**
     * Adds a potion for the effect amplifier menu.
     */
    private void addEffectAmplifierPotion(LocalizedName state, ItemStack existingPotion) {
        LocalizedName nextState = state.clone();
        nextState.setAction("enterEffectAmplifier");
        String effectTypeName = state.getExtraField();

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
        ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
        ItemStackUtil.addLore(potion, lore);

        this.anvilInv.text(ChatColor.RESET + "Enter here:");
        this.anvilInv.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.text("Enter here:")); // TODO send to process handler skip the listener and also onLeftInputClick and onRightInputClick
        this.anvilInv.plugin(Main.getPlugin(Main.class));
    }

    /**
     * Adds materials for the recipe ingredient selection menu.
     */
    private void addIngredients(LocalizedName state, PotionObject existingPotion) {
        int page = state.getPage();
        LocalizedName nextStateA = state.clone();
        nextStateA.setAction("addRecipeIngredient");
        LocalizedName nextStateS = state.clone();
        nextStateS.setAction("selectRecipeIngredient");

        List<Material> allValidMaterials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (ItemStackUtil.isValidIngredient(material)) {
                allValidMaterials.add(material);
            }
        }

        Set<Material> chosenIngredients = new HashSet<>();
        for (PotionRecipeObject recipe : existingPotion.getRecipes()) {
            chosenIngredients.add(recipe.getIngredient());
        }

        for (Material material : allValidMaterials.subList(page * PAGE_SIZE_B,
            Math.min((page + 1) * PAGE_SIZE_B, allValidMaterials.size()))) {
            ItemStack item = new ItemStack(material);
            List<String> lore = new ArrayList<>();
            String materialName = StringUtil.titleCase(material.name(), "_");
            if (chosenIngredients.contains(material)) {
                lore.add(ChatColor.GOLD + "Left click to add or modify the recipes using " + materialName + ".");
                lore.add(ChatColor.RED + "Right click to remove all recipes using " + materialName + ".");
                ItemStackUtil.setLocalizedName(item, nextStateS.toLocalizedString());
            } else {
                lore.add(ChatColor.GREEN + "Click to add a recipe using " + materialName + ".");
                ItemStackUtil.setLocalizedName(item, nextStateA.toLocalizedString());
            }
            ItemStackUtil.addLore(item, lore);
            this.inv.addItem(item);
        }
    }

    /**
     * Adds potions for the recipe base potion selection menu.
     */
    private void addBases(LocalizedName state, PotionObject existingPotion, Material recipeIngredient) {
        int page = state.getPage();
        LocalizedName nextStateA = state.clone();
        nextStateA.setAction("addRecipeBase");
        LocalizedName nextStateR = state.clone();
        nextStateR.setAction("removeRecipeBase");
        LocalizedName nextStateI = state.clone();
        nextStateI.setAction("recipeBaseInvalid");

        List<ItemStack> potions = new ArrayList<>();
        for (ItemStack vanillaPotion : PotionUtil.getVanillaPotions()) {
            PotionRecipeObject resultantRecipe = new PotionRecipeObject(recipeIngredient,
                    PotionUtil.getIDFromVanillaPotion(vanillaPotion), existingPotion.getPotionID());
            LocalizedName nextState = nextStateA.clone();
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to add this recipe.");

            // Another potion uses this recipe
            for (PotionRecipeObject recipe : PotionUtil.getAllRecipes()) {
                if (recipe.conflictsWith(resultantRecipe)) {
                    nextState = nextStateI.clone();
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.DARK_RED + "You cannot select this as a base potion for a new recipe.");
                    lore.add(ChatColor.DARK_RED + "This recipe is already being used to brew another potion.");
                    break;
                }
            }

            // Current potion has this recipe
            for (PotionRecipeObject recipe : existingPotion.getRecipes()) {
                if (recipe.equals(resultantRecipe)) {
                    nextState = nextStateR.clone();
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.RED + "Click to remove this recipe.");
                    break;
                }
            }

            nextState.setPotionID(PotionUtil.getIDFromVanillaPotion(vanillaPotion));
            ItemStack potion = vanillaPotion.clone();

            ItemStackUtil.addLore(potion, lore);
            ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
            potions.add(potion);
        }

        for (PotionObject customPotion : PotionReader.getCustomPotions()) {
            PotionRecipeObject resultantRecipe = new PotionRecipeObject(recipeIngredient,
                    customPotion.getPotionID(), existingPotion.getPotionID());
            LocalizedName nextState = nextStateA.clone();
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to add this recipe.");

            // Another potion uses this recipe
            for (PotionRecipeObject recipe : PotionUtil.getAllRecipes()) {
                if (recipe.conflictsWith(resultantRecipe)) {
                    nextState = nextStateI.clone();
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.DARK_RED + "You cannot select this as a base potion for a new recipe.");
                    lore.add(ChatColor.DARK_RED + "This recipe is already being used to brew another potion.");
                    break;
                }
            }

            // Current potion has this recipe
            for (PotionRecipeObject recipe : existingPotion.getRecipes()) {
                if (recipe.equals(resultantRecipe)) {
                    nextState = nextStateR.clone();
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.RED + "Click to remove this recipe.");
                    break;
                }
            }

            nextState.setPotionID(customPotion.getPotionID());
            ItemStack potion = customPotion.getPotion().clone();

            ItemStackUtil.addLore(potion, lore);
            ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
            potions.add(potion);
        }

        for (ItemStack potion : potions.subList(page * PAGE_SIZE_C, Math.min((page + 1) * PAGE_SIZE_C, potions.size()))) {
            this.inv.addItem(potion);
        }
    }

    /**
     * Adds a potion for the name menu.
     */
    private void addNamePotion(LocalizedName state, ItemStack existingPotion) {
        LocalizedName nextState = state.clone();
        nextState.setAction("enterName");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GOLD + "The potion name can include Minecraft chat code colours.");
        lore.add(ChatColor.GOLD + "Prefix the name with the code to change the colour / style.");
        lore.add(ChatColor.GOLD + "You will have the option to review changes in the next menu.");
        lore.add(ChatColor.GREEN + "Click the output slot to continue.");
        lore.add(ChatColor.GOLD + "Click the left input slot to skip (if you have misclicked).");
        lore.add(ChatColor.RED + "Press ESC to exit without saving.");

        ItemStack potion = existingPotion.clone();
        ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
        ItemStackUtil.addLore(potion, lore);

        this.anvilInv.text(ChatColor.RESET + "Enter here:");
        this.anvilInv.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.text("Enter here:")); // TODO send to process handler skip the listener and also onLeftInputClick and onRightInputClick
        this.anvilInv.plugin(Main.getPlugin(Main.class));
    }

    /**
     * Sets final potion slot to the potion for confirmation.
     */
    private void setFinalPotionSlot(LocalizedName state, PotionObject existingPotion) {
        LocalizedName nextState = state.clone();
        nextState.setAction("finalInvalid");

        ItemStack potion = existingPotion.getPotion().clone();
        ItemStackUtil.setLocalizedName(potion, nextState.toLocalizedString());
        PotionUtil.addLoreRecipes(potion, existingPotion);

        this.inv.setItem(FINAL_POTION_SLOT, potion);
    }

    /**
     * Sets edit slot to a selector which brings the user to the first menu.
     */
    private void setFinalEditSlot(LocalizedName state) {
        ItemStack edit = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);

        LocalizedName nextState = state.clone();
        nextState.setAction("finalEdit");

        ItemStackUtil.setLocalizedName(edit, nextState.toLocalizedString());
        ItemStackUtil.setDisplayName(edit, ChatColor.GOLD + "Edit the Potion");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Click to go back and make additional changes to your potion.");
        ItemStackUtil.addLore(edit, lore);

        this.inv.setItem(FINAL_EDIT_SLOT, edit);
    }

    /**
     * Sets confirm slot to a selector which confirms and saves the potion.
     */
    private void setFinalConfirmSlot(LocalizedName state) {
        ItemStack edit = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

        LocalizedName nextState = state.clone();
        nextState.setAction("finalConfirm");

        ItemStackUtil.setLocalizedName(edit, nextState.toLocalizedString());
        ItemStackUtil.setDisplayName(edit, ChatColor.GREEN + "Save and Confirm");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Click to save your potion.");
        ItemStackUtil.addLore(edit, lore);

        this.inv.setItem(FINAL_CONFIRM_SLOT, edit);
    }

    /**
     * Sets exit slot to a selector which exits the menu without saving changes to the potion.
     */
    private void setFinalExitSlot(LocalizedName state) {
        ItemStack edit = new ItemStack(Material.RED_STAINED_GLASS_PANE);

        LocalizedName nextState = state.clone();
        nextState.setAction("exit");

        ItemStackUtil.setLocalizedName(edit, nextState.toLocalizedString());
        ItemStackUtil.setDisplayName(edit, ChatColor.RED + "Exit without Saving");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Click to exit without saving any changes.");
        ItemStackUtil.addLore(edit, lore);

        this.inv.setItem(FINAL_EXIT_SLOT, edit);
    }

    public static List<String> getAllMenus() {
        List<String> menus = new ArrayList<>();
        menus.add("mainMenu");
        menus.add("potionType");
        menus.add("potionColour");
        menus.add("effectType");
        menus.add("effectDuration");
        menus.add("effectAmplifier");
        menus.add("recipeIngredient");
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

    public void openInv(CommandSender player) {
        openInv((Player) player);
    }

    public void openInv(Player player) {
        if (this.inv != null) {
            player.openInventory(inv);
        } else if (this.anvilInv != null) {
            this.anvilInv.open(player);
        } else {
            Main.log.severe("There was an error opening a custom inventory.");
        }
    }

}
