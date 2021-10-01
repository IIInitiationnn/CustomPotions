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
import xyz.iiinitiationnn.custompotions.utils.*;

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

    // TODO currently the PotionObject (Potion now) in the state represents what the potion will look like when some choice is made
    //  but the state represents what is currently happening, and the action decides what will happen in the next state
    //  we can keep it as it is, which will be a little unintuitive but cleaner, or we can put the processing in InventoryGuiListener
    //  verdict: i think im going to put the processing in InventoryGUIListener so it makes more sense that the state represents now
    //      and the future is calculated by the action. maybe java generics (generic object) for the selected option though so you dont have to
    //      read off the clicked item, you can just use the state (much cleaner code)

    // Constructors
    public InventoryGUI(State state) {
        boolean needsNextPage;
        switch (state.getMenu()) {
            case "mainMenu":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion to Modify");
                setEmpty(PAGE_SIZE_A);

                needsNextPage = addMainMenuPotions(state);

                setPreviousPageSlot(state);
                setNextPageSlot(state, needsNextPage);
                setBarrierExitSlot(state);
                break;
            case "potionType":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion Type");
                setEmpty(PAGE_SIZE_B);

                needsNextPage = addTypePotions(state);

                setPreviousPageSlot(state);
                setNextPageSlot(state, needsNextPage);
                setPreviousMenuSlot(state);
                setNextMenuSlot(state);
                setPotionExitSlot(state);
                break;
            case "potionColour":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select a Potion Colour");
                setEmpty(PAGE_SIZE_B);

                needsNextPage = addColourPotions(state);

                setPreviousPageSlot(state);
                setNextPageSlot(state, needsNextPage);
                setPreviousMenuSlot(state);
                setNextMenuSlot(state);
                setPotionExitSlot(state);
                break;
            case "effectType":
                this.inv = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "Select an Effect Type");
                setEmpty(PAGE_SIZE_B);

                needsNextPage = addEffectTypePotions(state);

                setPreviousPageSlot(state);
                setNextPageSlot(state, needsNextPage);
                setPreviousMenuSlot(state);
                setNextMenuSlot(state);
                setPotionExitSlot(state);
                break;
            case "effectDuration":
                this.anvilInv = new AnvilGUI.Builder();
                this.anvilInv.title(ChatColor.GOLD + "Effect Duration");
                addEffectDurationPotion(state);
                break;
            case "effectAmplifier":
                this.anvilInv = new AnvilGUI.Builder();
                this.anvilInv.title(ChatColor.GOLD + "Effect Amplifier");
                addEffectAmplifierPotion(state);
                break;
            case "recipeIngredient":
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select an Ingredient for a Recipe");
                setEmpty(PAGE_SIZE_B);

                needsNextPage = addIngredients(state);

                setPreviousPageSlot(state);
                setNextPageSlot(state, needsNextPage);
                setPreviousMenuSlot(state);
                setNextMenuSlot(state);
                setPotionExitSlot(state);
                break;
            case "recipeBase":
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Base for the Recipe");
                setEmpty(PAGE_SIZE_C);

                needsNextPage = addBases(state);

                setPreviousPageSlot(state);
                setNextPageSlot(state, needsNextPage);
                setPreviousMenuSlot(state, PREVIOUS_MENU_SLOT + 1);
                setPotionExitSlot(state);
                break;
            case "potionName":
                this.anvilInv = new AnvilGUI.Builder();
                this.anvilInv.title(ChatColor.GOLD + "Enter a Name");
                addNamePotion(state);
                break;
            case "finalMenu":
                inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Confirm Changes");
                setFinalPotionSlot(state);
                setFinalEditSlot(state);
                setFinalConfirmSlot(state);
                setFinalExitSlot(state);
                break;
        }
    }

    // Methods

    /**
     * Puts placeholder empty items in certain slots to prevent buttons from being occupied by,
     * and later overriding, items in the GUI.
     * @param pageSize
     */
    private void setEmpty(int pageSize) {
        if (pageSize == PAGE_SIZE_A) {
            for (int slot : Arrays.asList(PREVIOUS_PAGE_SLOT, NEXT_PAGE_SLOT, EXIT_SLOT)) {
                this.inv.setItem(slot, new ItemStack(Material.BARRIER));
            }
        } else if (pageSize == PAGE_SIZE_B) {
            for (int slot :
                    Arrays.asList(PREVIOUS_PAGE_SLOT, NEXT_PAGE_SLOT, PREVIOUS_MENU_SLOT, NEXT_MENU_SLOT, EXIT_SLOT)) {
                this.inv.setItem(slot, new ItemStack(Material.BARRIER));
            }
        } else if (pageSize == PAGE_SIZE_C) {
            for (int slot : Arrays.asList(PREVIOUS_PAGE_SLOT, NEXT_PAGE_SLOT, NEXT_MENU_SLOT, EXIT_SLOT)) {
                this.inv.setItem(slot, new ItemStack(Material.BARRIER));
            }
        }
    }

    /**
     * Sets the previous page slot to a selector which brings the user to the previous page of the same menu.
     */
    private void setPreviousPageSlot(State state) {
        int page = state.getPage();
        boolean needsPreviousPage = page > 0;

        ItemStack selector;
        State previousPageState = state.clone();

        if (needsPreviousPage) {
            selector = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
            previousPageState.setAction("pagePrevious");
            ItemStackUtil.setDisplayName(selector, ChatColor.GOLD + "PREVIOUS PAGE");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Page " + page);
            ItemStackUtil.addLore(selector, lore);
        } else {
            selector = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            previousPageState.setAction("pageInvalid");
            ItemStackUtil.setDisplayName(selector, ChatColor.RED + "NO PREVIOUS PAGE");
        }

        ItemStackUtil.setLocalizedName(selector, previousPageState.encodeToString());

        this.inv.setItem(PREVIOUS_PAGE_SLOT, selector);
    }

    /**
     * Sets the next page slot to a selector which brings the user to the next page of the same menu.
     */
    private void setNextPageSlot(State state, boolean needsNextPage) {
        int page = state.getPage();

        ItemStack selector;
        State nextPageState = state.clone();

        if (needsNextPage) {
            selector = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            nextPageState.setAction("pageNext");
            ItemStackUtil.setDisplayName(selector, ChatColor.GREEN + "NEXT PAGE");
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "Page " + (page + 2));
            ItemStackUtil.addLore(selector, lore);
        } else {
            selector = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            nextPageState.setAction("pageInvalid");
            ItemStackUtil.setDisplayName(selector, ChatColor.RED + "NO NEXT PAGE");
        }

        ItemStackUtil.setLocalizedName(selector, nextPageState.encodeToString());

        this.inv.setItem(NEXT_PAGE_SLOT, selector);
    }

    /**
     * Sets the previous menu slot to a selector which brings the user to the previous menu.
     */
    private void setPreviousMenuSlot(State state) {
        setPreviousMenuSlot(state, PREVIOUS_MENU_SLOT);
    }

    /**
     * Sets the previous menu slot to a selector which brings the user to the previous menu.
     */
    private void setPreviousMenuSlot(State state, int slot) {
        ItemStack selector = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);

        State previousMenuState = state.clone();
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

        ItemStackUtil.setLocalizedName(selector, previousMenuState.encodeToString());
        ItemStackUtil.setDisplayName(selector, ChatColor.GOLD + "PREVIOUS MENU");
        ItemStackUtil.addLore(selector, lore);

        this.inv.setItem(slot, selector);
    }

    /**
     * Sets the next menu slot to a selector which brings the user to the next menu.
     */
    private void setNextMenuSlot(State state) {
        ItemStack selector = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

        State nextMenuState = state.clone();
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

        ItemStackUtil.setLocalizedName(selector, nextMenuState.encodeToString());
        ItemStackUtil.setDisplayName(selector, ChatColor.GREEN + "NEXT MENU");
        ItemStackUtil.addLore(selector, lore);

        this.inv.setItem(NEXT_MENU_SLOT, selector);
    }

    /**
     * Given the current page index (starting at 0), the size of each page, and the total number of items,
     * determine if there is another page following this one.
     * @param currentPage
     * @param pageSize
     * @param totalItems
     * @return
     */
    private static boolean determineIfNextPage(int currentPage, int pageSize, int totalItems) {
        return (totalItems - pageSize * currentPage) > pageSize;
    }

    /**
     * Sets exit slot for the main menu using a barrier block.
     */
    private void setBarrierExitSlot(State state) {
        ItemStack selector = new ItemStack(Material.BARRIER);

        State exitState = state.clone();
        exitState.setAction("exit");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Click to exit.");

        ItemStackUtil.setLocalizedName(selector, exitState.encodeToString());
        ItemStackUtil.setDisplayName(selector, ChatColor.RED + "EXIT");
        ItemStackUtil.addLore(selector,lore);

        this.inv.setItem(EXIT_SLOT, selector);
    }

    /**
     * Sets exit slot for any menu using the creator's existing potion.
     */
    private void setPotionExitSlot(State state) {
        Potion existingPotion = state.getPotion();
        ItemStack selector = existingPotion.toItemStack().clone();

        State exitState = state.clone();
        exitState.setAction("exit");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GREEN + "Left click to save your changes and exit.");
        lore.add(ChatColor.RED + "Right click to exit without saving.");

        ItemStackUtil.setLocalizedName(selector, exitState.encodeToString());
        PotionUtil.addLoreRecipes(selector, existingPotion);
        ItemStackUtil.addLore(selector,lore);

        this.inv.setItem(EXIT_SLOT, selector);
    }

    /**
     * Adds potions for the main menu including, if applicable:
     * a (random) new potion, and all existing potions.
     */
    private boolean addMainMenuPotions(State state) {
        int page = state.getPage();
        State nextStateBase = state.clone();
        nextStateBase.setAction("selectPotion");

        List<ItemStack> allPotions = new ArrayList<>();
        ItemStack newPotion = getNewPotion();
        allPotions.add(newPotion);

        List<Potion> customPotions = PotionReader.getCustomPotions();
        for (Potion customPotion : customPotions) {
            State nextState = nextStateBase.clone();
            nextState.setPotion(customPotion);
            ItemStack customPotionItem = customPotion.toItemStack();

            ItemStackUtil.setLocalizedName(customPotionItem, nextState.encodeToString());
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
        return determineIfNextPage(page, PAGE_SIZE_A, allPotions.size());
    }

    /**
     * Returns a potion indicating a new potion is to be created.
     */
    private ItemStack getNewPotion() {
        State nextState = new State();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Create a new custom potion from scratch.");

        ItemStack potion = nextState.getPotion().toItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        ItemStackUtil.addLore(potion, lore);

        return potion;
    }

    /**
     * Adds potions for the potion type selection menu including:
     * potions, splash potions, and lingering potions.
     */
    private boolean addTypePotions(State state) {
        State nextStateBase = state.clone();
        nextStateBase.setAction("selectType");
        ChatColor c = ColourUtil.getChatColor(nextStateBase.getPotion().toItemStack());

        // Potion
        State nextState1 = nextStateBase.clone();
        nextState1.getPotion().setType(Material.POTION);
        ItemStack potion = nextState1.getPotion().toItemStack();

        ItemStackUtil.setLocalizedName(potion, nextState1.encodeToString());
        ItemStackUtil.setDisplayName(potion, c + "Potion");
        this.inv.addItem(potion);

        // Splash Potion
        State nextState2 = nextStateBase.clone();
        nextState2.getPotion().setType(Material.SPLASH_POTION);
        ItemStack splashPotion = nextState2.getPotion().toItemStack();

        ItemStackUtil.setLocalizedName(splashPotion, nextState2.encodeToString());
        ItemStackUtil.setDisplayName(splashPotion, c + "Splash Potion");
        this.inv.addItem(splashPotion);

        // Lingering Potion
        State nextState3 = nextStateBase.clone();
        nextState3.getPotion().setType(Material.LINGERING_POTION);
        ItemStack lingeringPotion = nextState3.getPotion().toItemStack();

        ItemStackUtil.setLocalizedName(lingeringPotion, nextState3.encodeToString());
        ItemStackUtil.setDisplayName(lingeringPotion, c + "Lingering Potion");
        this.inv.addItem(lingeringPotion);

        return false;
    }

    /**
     * Adds potions for the potion colour selection menu.
     */
    private boolean addColourPotions(State state) {
        State nextStateBase = state.clone();
        nextStateBase.setAction("selectColour");
        List<Colour> colours = ColourUtil.defaultPotionColourList();

        for (Colour colour : colours) {
            State nextState = nextStateBase.clone();
            nextState.getPotion().setColour(colour);
            ItemStack potion = nextState.getPotion().toItemStack();

            ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());

            ChatColor c = ColourUtil.getChatColor(colour);
            ItemStackUtil.setDisplayName(potion, c + ColourUtil.defaultPotionColourMapReverse().get(colour));
            this.inv.addItem(potion);
        }
        return false;
    }

    /**
     * Adds potions for the potion effect type selection menu.
     */
    private boolean addEffectTypePotions(State state) {
        ChatColor c = ColourUtil.getChatColor(state.getPotion().toItemStack());

        // No Effects
        State plainPotionState = state.clone();
        plainPotionState.setAction("noEffects");
        plainPotionState.getPotion().setEffects(new ArrayList<>());
        ItemStack plainPotion = plainPotionState.getPotion().toItemStack();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This potion will have no effects.");
        ItemStackUtil.setLocalizedName(plainPotion, plainPotionState.encodeToString());
        ItemStackUtil.setDisplayName(plainPotion, c + "NO EFFECTS");
        ItemStackUtil.addLore(plainPotion, lore);
        this.inv.addItem(plainPotion);
        // TODO no handling on this yet; currently goes to duration menu

        // Effects
        List<PotionEffectType> effectTypeList = Arrays.asList(PotionEffectType.values());
        effectTypeList.sort(Comparator.comparing(PotionEffectType :: getName)); // sort list by effect name
        for (PotionEffectType effectType : effectTypeList) {
            String commonName = StringUtil.toCommonName(effectType.getName());

            lore = new ArrayList<>();
            lore.add("");
            ItemStack potion;
            if (state.getPotion().hasEffect(effectType)) {
                State nextState = state.clone();
                nextState.setAction("selectEffectType");
                nextState.getInput().setEffectType(effectType.getName());
                Main.log.info(effectType.getName()); // hhh
                potion = nextState.getPotion().toItemStack();

                lore.add(ChatColor.GOLD + "Left click to modify " + commonName + ".");
                lore.add(ChatColor.RED + "Right click to remove " + commonName + ".");
                ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
            } else {
                State nextState = state.clone();
                nextState.setAction("addEffectType");
                nextState.getInput().setEffectType(effectType.getName());
                Main.log.info(effectType.getName()); // hhh
                potion = nextState.getPotion().toItemStack();

                lore.add(ChatColor.GREEN + "Click to add " + commonName + ".");
                ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
            }

            int maxAmp = PotionUtil.maxAmp(effectType.getName()) + 1;
            if (maxAmp == 1) {
                lore.add(ChatColor.GOLD + "It has potency 1.");
            } else {
                lore.add(ChatColor.GOLD + "Its potency ranges from 1 to " + maxAmp + ".");
            }
            ItemStackUtil.setDisplayName(potion, c + effectType.getName());
            ItemStackUtil.addLore(potion, lore);
            this.inv.addItem(potion);
        }
        return false;
    }

    /**
     * Adds a potion for the effect duration menu.
     */
    private void addEffectDurationPotion(State state) {
        State nextState = state.clone();
        nextState.setAction("enterEffectDuration");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        if (state.getPotion().isLingering()) {
            lore.add(ChatColor.GOLD + "Enter the effect duration in seconds (1 to 26,843,545).");
        } else {
            lore.add(ChatColor.GOLD + "Enter the effect duration in seconds (1 to 107,374,182).");
        }
        lore.add("");
        lore.add(ChatColor.GREEN + "Click the output slot to continue.");
        lore.add(ChatColor.GOLD + "Click the left input slot to skip (if you have misclicked).");
        lore.add(ChatColor.RED + "Press ESC to exit without saving.");

        ItemStack potion = nextState.getPotion().toItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        ItemStackUtil.addLore(potion, lore);

        this.anvilInv
            .plugin(Main.getPlugin(Main.class))
            .text(ChatColor.RESET + "Enter here:")
            .itemLeft(potion)
            .onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.close());
    }

    /**
     * Adds a potion for the effect amplifier menu.
     */
    private void addEffectAmplifierPotion(State state) {
        State nextState = state.clone();
        nextState.setAction("enterEffectAmplifier");
        String effectTypeName = state.getInput().getEffectType();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GOLD + "Enter the effect amplifier (integer from 1 to " + (PotionUtil.maxAmp(effectTypeName) + 1) + ").");
        lore.add(ChatColor.GOLD + "1 means potency I, eg. " + StringUtil.toCommonName(effectTypeName) + " I.");
        lore.add(ChatColor.GOLD + "Similarly, 2 means potency II, eg. " + StringUtil.toCommonName(effectTypeName) + " II, and so on.");
        lore.add("");
        lore.add(ChatColor.GREEN + "Click the output slot to continue.");
        lore.add(ChatColor.GOLD + "Click the left input slot to skip (if you have misclicked).");
        lore.add(ChatColor.RED + "Press ESC to exit without saving.");

        ItemStack potion = nextState.getPotion().toItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        ItemStackUtil.addLore(potion, lore);

        this.anvilInv
            .plugin(Main.getPlugin(Main.class))
            .text(ChatColor.RESET + "Enter here:")
            .itemLeft(potion)
            .onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.close());
    }

    /**
     * Adds materials for the recipe ingredient selection menu.
     */
    private boolean addIngredients(State state) {
        // TODO recipes are cooked?????
        int page = state.getPage();

        List<Material> allValidMaterials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (ItemStackUtil.isValidIngredient(material)) {
                allValidMaterials.add(material);
            }
        }

        Set<Material> chosenIngredients = new HashSet<>();
        for (PotionRecipe recipe : state.getPotion().getRecipes()) {
            chosenIngredients.add(recipe.getIngredient());
        }

        for (Material material : allValidMaterials.subList(page * PAGE_SIZE_B,
            Math.min((page + 1) * PAGE_SIZE_B, allValidMaterials.size()))) {
            ItemStack item = new ItemStack(material);
            List<String> lore = new ArrayList<>();
            String materialName = StringUtil.titleCase(material.name(), "_");
            State nextState = state.clone();
            nextState.getInput().setMaterial(materialName);
            if (chosenIngredients.contains(material)) {
                nextState.setAction("selectRecipeIngredient");
                lore.add(ChatColor.GOLD + "Left click to add or modify the recipes using " + materialName + ".");
                lore.add(ChatColor.RED + "Right click to remove all recipes using " + materialName + ".");
            } else {
                nextState.setAction("addRecipeIngredient");
                nextState.getInput().setMaterial(materialName);
                lore.add(ChatColor.GREEN + "Click to add a recipe using " + materialName + ".");
            }
            ItemStackUtil.setLocalizedName(item, nextState.encodeToString());
            ItemStackUtil.addLore(item, lore);
            this.inv.addItem(item);
        }
        return determineIfNextPage(page, PAGE_SIZE_B, allValidMaterials.size());
    }

    /**
     * Adds potions for the recipe base potion selection menu.
     */
    private boolean addBases(State state) {
        int page = state.getPage();
        Material recipeIngredient = Material.matchMaterial(state.getInput().getMaterial());

        List<ItemStack> potions = new ArrayList<>();
        for (ItemStack vanillaPotion : PotionUtil.getVanillaPotions()) {
            State nextState = state.clone();
            nextState.setAction("addRecipeBase");

            Potion potion = nextState.getPotion();

            PotionRecipe resultantRecipe = new PotionRecipe(recipeIngredient,
                    PotionUtil.getIDFromVanillaPotion(vanillaPotion), potion.getPotionID());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to add this recipe.");

            // Another potion uses this recipe
            for (PotionRecipe recipe : PotionUtil.getAllRecipes()) {
                if (recipe.conflictsWith(resultantRecipe)) {
                    nextState.setAction("recipeBaseInvalid");
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.DARK_RED + "You cannot select this as a base potion for a new recipe.");
                    lore.add(ChatColor.DARK_RED + "This recipe is already being used to brew another potion.");
                    break;
                }
            }

            boolean addRecipe = true;

            // Current potion has this recipe
            for (PotionRecipe recipe : potion.getRecipes()) {
                if (recipe.equals(resultantRecipe)) {
                    nextState.setAction("removeRecipeBase");
                    potion.removeRecipe(recipe);
                    addRecipe = false;
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.RED + "Click to remove this recipe.");
                    break;
                }
            }

            if (addRecipe) potion.addRecipe(resultantRecipe);

            ItemStackUtil.addLore(vanillaPotion, lore);
            ItemStackUtil.setLocalizedName(vanillaPotion, nextState.encodeToString());
            potions.add(vanillaPotion);
        }

        for (Potion customPotion : PotionReader.getCustomPotions()) {
            Potion potion = state.getPotion();

            // Potion cannot use itself in a recipe
            if (customPotion.getPotionID().equals(potion.getPotionID())) {
                continue;
            }

            PotionRecipe resultantRecipe = new PotionRecipe(recipeIngredient,
                    customPotion.getPotionID(), potion.getPotionID());
            State nextState = state.clone();
            nextState.setAction("addRecipeBase");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to add this recipe.");

            // Another potion uses this recipe
            for (PotionRecipe recipe : PotionUtil.getAllRecipes()) {
                if (recipe.conflictsWith(resultantRecipe)) {
                    nextState.setAction("recipeBaseInvalid");
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.DARK_RED + "You cannot select this as a base potion for a new recipe.");
                    lore.add(ChatColor.DARK_RED + "This recipe is already being used to brew another potion.");
                    break;
                }
            }

            // Current potion has this recipe
            for (PotionRecipe recipe : potion.getRecipes()) {
                if (recipe.equals(resultantRecipe)) {
                    nextState.setAction("removeRecipeBase");
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.RED + "Click to remove this recipe.");
                    break;
                }
            }

            ItemStack potionItem = customPotion.toItemStack();

            ItemStackUtil.addLore(potionItem, lore);
            ItemStackUtil.setLocalizedName(potionItem, nextState.encodeToString());
            potions.add(potionItem);
        }

        for (ItemStack potion : potions.subList(page * PAGE_SIZE_C, Math.min((page + 1) * PAGE_SIZE_C, potions.size()))) {
            this.inv.addItem(potion);
        }

        return determineIfNextPage(page, PAGE_SIZE_C, potions.size());
    }

    /**
     * Adds a potion for the name menu.
     */
    private void addNamePotion(State state) {
        State nextState = state.clone();
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

        ItemStack potion = nextState.getPotion().toItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        ItemStackUtil.addLore(potion, lore);

        this.anvilInv.text(ChatColor.RESET + "Enter here:");
        this.anvilInv.onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.text("Enter here:")); // TODO send to process handler skip the listener and also onLeftInputClick and onRightInputClick
        this.anvilInv.plugin(Main.getPlugin(Main.class));
    }

    /**
     * Sets final potion slot to the potion for confirmation.
     */
    private void setFinalPotionSlot(State state) {
        State nextState = state.clone();
        nextState.setAction("finalInvalid");

        ItemStack potion = nextState.getPotion().toItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        PotionUtil.addLoreRecipes(potion, nextState.getPotion());

        this.inv.setItem(FINAL_POTION_SLOT, potion);
    }

    /**
     * Sets edit slot to a selector which brings the user to the first menu.
     */
    private void setFinalEditSlot(State state) {
        ItemStack edit = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);

        State nextState = state.clone();
        nextState.setAction("finalEdit");

        ItemStackUtil.setLocalizedName(edit, nextState.encodeToString());
        ItemStackUtil.setDisplayName(edit, ChatColor.GOLD + "Edit the Potion");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Click to go back and make additional changes to your potion.");
        ItemStackUtil.addLore(edit, lore);

        this.inv.setItem(FINAL_EDIT_SLOT, edit);
    }

    /**
     * Sets confirm slot to a selector which confirms and saves the potion.
     */
    private void setFinalConfirmSlot(State state) {
        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

        State nextState = state.clone();
        nextState.setAction("finalConfirm");

        ItemStackUtil.setLocalizedName(confirm, nextState.encodeToString());
        ItemStackUtil.setDisplayName(confirm, ChatColor.GREEN + "Save and Confirm");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Click to save your potion.");
        ItemStackUtil.addLore(confirm, lore);

        this.inv.setItem(FINAL_CONFIRM_SLOT, confirm);
    }

    /**
     * Sets exit slot to a selector which exits the menu without saving changes to the potion.
     */
    private void setFinalExitSlot(State state) {
        ItemStack exit = new ItemStack(Material.RED_STAINED_GLASS_PANE);

        State nextState = state.clone();
        nextState.setAction("exit");

        ItemStackUtil.setLocalizedName(exit, nextState.encodeToString());
        ItemStackUtil.setDisplayName(exit, ChatColor.RED + "Exit without Saving");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Click to exit without saving any changes.");
        ItemStackUtil.addLore(exit, lore);

        this.inv.setItem(FINAL_EXIT_SLOT, exit);
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
