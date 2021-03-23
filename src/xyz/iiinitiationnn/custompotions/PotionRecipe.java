package xyz.iiinitiationnn.custompotions;

import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.List;

public class PotionRecipe {
    public ItemStack result;
    public ItemStack predecessor;
    public Material ingredient;
    public int index;

    public PotionRecipe(ItemStack result, ItemStack predecessor, Material ingredient, int index) {
        this.result = result;
        this.predecessor = predecessor;
        this.ingredient = ingredient;
        this.index = index;
    }

    public Material getIngredient() {
        return ingredient;
    }

    @Nullable
    public static PotionRecipe getRecipe(BrewerInventory inventory, List<PotionRecipe> recipes, Main pluginInstance) {
        boolean allAir = true;
        for (int i = 0; i < 3 && allAir; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) continue;
            allAir = false;
        }
        if (allAir) return null;

        Material ingredient = inventory.getIngredient().getType();

        for (PotionRecipe recipe : recipes) {
            for (int i = 0; i < 3; i++) {
                if (ingredient == recipe.getIngredient() && recipe.predecessor.isSimilar(inventory.getItem(i))) return recipe;
            }
        }
        pluginInstance.getLogger().info("no recipe found");
        return null;
    }

    public void startBrewing(BrewerInventory inventory, Main pluginInstance, List<PotionRecipe> recipes) {
        new BrewClock(this, inventory, pluginInstance, recipes);
    }

    private class BrewClock extends BukkitRunnable {
        private Main pluginInstance;
        private BrewerInventory inventory;
        private ItemStack ingredient;
        private PotionRecipe recipe;
        private List<PotionRecipe> recipes;
        private BrewingStand brewingStand;
        private int time = 400;

        public BrewClock(PotionRecipe recipe, BrewerInventory inventory, Main pluginInstance, List<PotionRecipe> recipes) {
            this.recipe = recipe;
            this.recipes = recipes;
            this.inventory = inventory;
            this.ingredient = inventory.getIngredient();
            this.brewingStand = inventory.getHolder();
            this.pluginInstance = pluginInstance;
            runTaskTimer(pluginInstance, 0L, 1L);
        }
        // TODO work out why it brek
        @Override
        public void run() {
            if (time == 0) {
                ItemStack ingredientMinusOne = ingredient.clone();
                ingredientMinusOne.setAmount(ingredient.getAmount() - 1);
                inventory.setIngredient(ingredientMinusOne);
                for (int i = 0; i < 3; i ++) {
                    if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) continue;
                    for (PotionRecipe r : recipes) {
                        if (r.ingredient != ingredient.getType()) continue;
                        if (inventory.getItem(i).isSimilar(r.predecessor)) {
                            inventory.setItem(i, r.result);
                            break;
                        }
                    }
                }
                cancel();
                return;
            }

            if (inventory.getIngredient() == null) {
                pluginInstance.getLogger().info("ingredient is null");
                cancel();
                return;
            }

            if (inventory.getIngredient().getType() == ingredient.getType() && time == 400) {
                if (brewingStand.getFuelLevel() == 0) {
                    if (inventory.getFuel() == null) {
                        cancel();
                        return;
                    } else {
                        ItemStack fuelMinusOne = inventory.getFuel();
                        fuelMinusOne.setAmount(fuelMinusOne.getAmount() - 1);
                        inventory.setFuel(fuelMinusOne);
                        brewingStand.setFuelLevel(20);
                    }
                } else {
                    brewingStand.setFuelLevel(inventory.getHolder().getFuelLevel() - 1);
                }
                pluginInstance.getLogger().info("starting brew");
                brewingStand.setBrewingTime(400);
            }

            boolean allAir = true;
            for (int i = 0; i < 3 && allAir; i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) continue;
                allAir = false;
            }
            if (allAir) {
                cancel();
                return;
            }
            time--;
            brewingStand.setBrewingTime(time);
            brewingStand.update(); // is the disappearing / appearing potion stuff due to this?
        }
    }


}