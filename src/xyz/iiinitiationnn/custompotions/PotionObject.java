package xyz.iiinitiationnn.custompotions;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import xyz.iiinitiationnn.custompotions.utils.ColourUtil;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;
import xyz.iiinitiationnn.custompotions.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PotionObject {
    private String potionID;
    private String name;
    private Material type;
    private Colour colour;
    private ItemStack potion;
    private List<PotionRecipeObject> recipes;

    // TODO whichever fields here dont have getters are not useful and should be removed
    //  most likely name, type, colour (all contained in potion itself, can use Util methods to extract)

    public PotionObject() {
    }

    // existingPotion must be modified appropriately e.g. if a type is chosen, the type of existingPotion must be correctly set in the passed-in existingPotion
    public PotionObject(ItemStack existingPotion) {
        LocalizedName localizedName = new LocalizedName(ItemStackUtil.getLocalizedName(existingPotion));
        this.potionID = localizedName.getPotionID();
        this.name = localizedName.getPotionName();
        this.type = existingPotion.getType();
        this.colour = new Colour(ColourUtil.getPotionColor(existingPotion));
        this.recipes = localizedName.getPotionRecipes();

        // realistically only the stuff from localizedName (i.e. additional stored info not carried by the potion metadata)
        // should be "reset" below
        ItemStack potionItem = existingPotion.clone();
        PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
        if (potionMeta == null) {
            Main.log.severe("There was an error retrieving the potion metadata when constructing a PotionObject from an existing potion.");
            return;
        }
        potionMeta.setDisplayName(ChatColor.RESET + this.name);
        potionMeta.setLocalizedName(null);
        potionMeta.setLore(null);
        potionItem.setItemMeta(potionMeta);
        this.potion = potionItem;
    }

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

    public String getPotionID() {
        return this.potionID;
    }

    public String getName() {
        return this.name;
    }

    public List<PotionRecipeObject> getRecipes() {
        return this.recipes;
    }

    public ItemStack getPotion() {
        return this.potion;
    }

    public static void debugCustomPotions() {
        List<PotionObject> customPotions = PotionReader.getCustomPotions();
        if (customPotions.size() == 0) {
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
        Main.log.info("    Potion ID: " + this.potionID);
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
        if (this.recipes.size() == 0) {
            Main.log.info("        Recipes: None");
        } else {
            Main.log.info("        Recipes:");
            for (PotionRecipeObject recipe : this.recipes) {
                String ingredient = StringUtil.titleCase(recipe.getIngredient().name(), "_");
                String base = ChatColor.stripColor(PotionUtil.potionNameFromID(recipe.getBase()));
                Main.log.info("            " + ingredient + " + " + base);
            }
        }
    }



}
