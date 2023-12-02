package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.AddRecipeBaseAction;
import xyz.iiinitiationnn.custompotions.Actions.InvalidAction;
import xyz.iiinitiationnn.custompotions.Actions.MenuPreviousAction;
import xyz.iiinitiationnn.custompotions.Actions.RemoveRecipeBaseAction;
import xyz.iiinitiationnn.custompotions.Potion;
import xyz.iiinitiationnn.custompotions.PotionRecipe;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestInventory;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.MagicNumber;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecipeBaseMenu extends State {
    private final static int pageSize = 50;

    public RecipeBaseMenu(State state) {
        super(state, new ChestInventory(ChatColor.GOLD + "Select a Base for the Recipe"), state.input);
    }

    @Override
    protected boolean needsNextPage() {
        return false; // TODO
    }

    @Override
    protected List<String> previousMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Recipe Ingredient(s) Selection");
        lore.add(ChatColor.RED + "Warning: you will lose your choice of ingredient!");
        return lore;
    }

    @Override
    protected ItemStack previousMenuButton(List<String> lore) {
        ItemStack button = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);

        State nextState = this.clone()
            .setAction(new MenuPreviousAction())
            .resetInput();

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());
        ItemStackUtil.setDisplayName(button, ChatColor.GOLD + "PREVIOUS MENU");
        ItemStackUtil.addLore(button, lore);

        return button;
    }

    @Override
    public Map<Integer, ItemStack> calculateButtons() {
        Map<Integer, ItemStack> buttons = new HashMap<>();
        buttons.put(MagicNumber.PREVIOUS_PAGE_SLOT, this.previousPageButton());
        buttons.put(MagicNumber.NEXT_PAGE_SLOT, this.nextPageButton());
        buttons.put(MagicNumber.NEXT_MENU_SLOT, this.previousMenuButton(this.previousMenuLore()));
        buttons.put(MagicNumber.EXIT_SLOT, this.exitButton());
        return buttons;
    }

    /**
     * Fetches the potions for the recipe base potion selection menu.
     */
    @Override
    public List<ItemStack> calculateInventoryItems() {
        // TODO move bases currently involved in recipes to the top
        List<PotionRecipe> allRecipes = PotionUtil.getAllRecipes();
        Material recipeIngredient = Material.matchMaterial(this.getInputRecipeIngredient());

        Map<String, ItemStack> potions = new HashMap<>();
        // TODO when you SELECT an ingredient and left click, a bunch of vanilla potions are missing IF another
        //  potion uses it as a recipe

        // TODO abstract away checks into a Util so it can be used upon save to verify that another person hasnt
        //  used those recipes and made something in the current potion invalid

        // TODO order is wrong because of map

        int i = 0;

        for (ItemStack vanillaPotion : PotionUtil.getVanillaPotions()) {
            if (isWithinBounds(i, pageSize)) {
                potions.put(PotionUtil.getIdFromVanillaPotion(vanillaPotion), vanillaPotion);
            }
            i++;
        }

        for (Potion customPotion : PotionUtil.getCustomPotions()) {
            if (isWithinBounds(i, pageSize)) {
                // Potion cannot use itself in a recipe
                if (!Objects.equals(customPotion.getPotionId(), this.getPotionId())) {
                    potions.put(customPotion.getPotionId(), customPotion.toItemStack());
                }
            }
            i++;
        }

        for (Map.Entry<String, ItemStack> entry : potions.entrySet()) {
            State nextState = this.clone();
            nextState.setAction(new AddRecipeBaseAction());

            Potion currentPotion = nextState.getPotion();

            PotionRecipe resultantRecipe = new PotionRecipe(recipeIngredient, entry.getKey(), currentPotion);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to add this recipe.");

            boolean addRecipe = true;

            // Another potion uses this recipe
            if (allRecipes.stream().anyMatch(recipe -> recipe.conflictsWith(resultantRecipe))) {
                nextState.setAction(new InvalidAction());
                addRecipe = false;
                lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.DARK_RED + "You cannot select this as a base potion for a new recipe.");
                lore.add(ChatColor.DARK_RED + "This recipe is already being used to brew another potion.");
            }

            // Current potion has this recipe
            for (PotionRecipe recipe : currentPotion.getRecipes()) {
                if (recipe.equals(resultantRecipe)) {
                    nextState.setAction(new RemoveRecipeBaseAction());
                    currentPotion.removeRecipe(recipe);
                    addRecipe = false;
                    lore = new ArrayList<>();
                    lore.add("");
                    lore.add(ChatColor.RED + "Click to remove this recipe.");
                    break;
                }
            }

            if (addRecipe) currentPotion.addRecipe(resultantRecipe);

            ItemStack potion = entry.getValue();
            ItemStackUtil.addLore(potion, lore);
            ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        }

        return new ArrayList<>(potions.values());
    }
}
