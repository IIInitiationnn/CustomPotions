package xyz.iiinitiationnn.custompotions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.List;

import static xyz.iiinitiationnn.custompotions.PotionReader.getCustomPotions;

public class PotionObject {
    private String potionID;
    private String name;
    private Material type;
    private Colour colour;
    private ItemStack potion;
    private List<PotionRecipeObject> recipes;


    public void setPotionID(String potionId) {
        this.potionID = potionId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Material type) {
        this.type = type;
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    public void setPotion(ItemStack potion) {
        this.potion = potion;
    }

    public void setRecipes(List<PotionRecipeObject> recipes) {
        this.recipes = recipes;
    }

    public ItemStack getPotion() {
        return this.potion;
    }

    public static void debugCustomPotions() {
        List<PotionObject> customPotions = getCustomPotions();
        if (customPotions == null || customPotions.size() == 0) {
            Main.log.info("No custom potions to debug.");
            return;
        } else {
            Main.log.info(String.format("Debugging %d custom potions:", customPotions.size()));
        }
        for (PotionObject customPotion : customPotions) {
            customPotion.debugCustomPotion();
        }
    }

    public void debugCustomPotion() {
        Main.log.info("    Potion ID: " + this.potionID.toString());
        Main.log.info("        Name: " + this.name);
        Main.log.info("        Type: " + this.type.name());
        Main.log.info(String.format("        Colour: (%d, %d, %d)", this.colour.getR(), this.colour.getG(), this.colour.getB()));
        List<PotionEffect> potionEffects = ((PotionMeta) (this.potion.getItemMeta())).getCustomEffects();
        if (potionEffects.size() == 0) {
            Main.log.info("        Effects: None");
        } else {
            Main.log.info("        Effects:");
            for (PotionEffect potionEffect : potionEffects) {
                Main.log.info("            " + potionEffect.getType().getName() + ":");
                Main.log.info("                Duration (ticks): " + potionEffect.getDuration());
                Main.log.info("                Amplifier: " + potionEffect.getAmplifier());
            }
        }
        // TODO print recipes
    }



}
