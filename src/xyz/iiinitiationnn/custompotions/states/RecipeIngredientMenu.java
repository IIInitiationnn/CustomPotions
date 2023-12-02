package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.AddRecipeIngredientAction;
import xyz.iiinitiationnn.custompotions.Actions.SelectRecipeIngredientAction;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.PotionRecipe;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestInventory;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeIngredientMenu extends State {
    private final static int pageSize = 49;

    public RecipeIngredientMenu(State state) {
        super(state, new ChestInventory(ChatColor.GOLD + "Select an Ingredient for a Recipe"), new Input());
    }

    private List<Material> validMaterials() {
        return Arrays.stream(Material.values())
            .filter(ItemStackUtil :: isValidIngredient)
            .collect(Collectors.toList());
    }

    private int numValidMaterials() {
        return validMaterials().size();
    }

    @Override
    protected boolean needsNextPage() {
        return this.determineIfNextPage(pageSize, numValidMaterials());
    }

    @Override
    protected List<String> previousMenuLore() {
        return Collections.singletonList(ChatColor.GOLD + "Effect Type(s) Selection");
    }

    @Override
    protected List<String> nextMenuLore() {
        return Collections.singletonList(ChatColor.GREEN + "Potion Naming");
    }

    /**
     * Fetches the materials for the recipe ingredient selection menu.
     */
    // TODO:
    //  - move ingredients currently involved in recipes OR in vanilla recipes eg gun, red, glow powder to the top
    //  - maybe sort alphabetically after that? easier to find stuff esp if you add big page jumpers like +5
    //  - maybe the page jump size can be set in config? not sure what to put in config lol
    @Override
    public List<ItemStack> calculateInventoryItems() {
        List<Material> valid = this.validMaterials();

        Set<Material> chosenIngredients = new HashSet<>();
        for (PotionRecipe recipe : this.getPotionRecipes()) {
            chosenIngredients.add(recipe.getIngredient());
        }

        List<ItemStack> toDisplay = new ArrayList<>();

        int i = 0;
        for (Material material : valid) {
            if (this.isWithinBounds(i, pageSize)) {
                State nextState = this.clone();
                ItemStack item = new ItemStack(material);
                String materialName = StringUtil.titleCase(material.name(), "_");
                List<String> lore = new ArrayList<>();

                nextState.setInputIngredient(materialName);
                if (chosenIngredients.contains(material)) {
                    nextState.setAction(new SelectRecipeIngredientAction());
                    lore.add(ChatColor.GOLD + "Left click to add or modify the recipes using " + materialName + ".");
                    lore.add(ChatColor.RED + "Right click to remove all recipes using " + materialName + ".");
                } else {
                    nextState.setAction(new AddRecipeIngredientAction());
                    nextState.getInput().setIngredient(materialName);
                    lore.add(ChatColor.GREEN + "Click to add a recipe using " + materialName + ".");
                }

                ItemStackUtil.setLocalizedName(item, nextState.encodeToString());
                ItemStackUtil.addLore(item, lore);
                toDisplay.add(item);
            }
            i++;
        }
        return toDisplay;
    }
}
