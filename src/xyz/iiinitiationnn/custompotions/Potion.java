package xyz.iiinitiationnn.custompotions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.iiinitiationnn.custompotions.utils.ColourUtil;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;
import xyz.iiinitiationnn.custompotions.utils.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Potion implements Serializable, Cloneable {
    private String potionID;
    private Colour colour;
    private String name;
    private Material type;
    private List<PotionRecipe> recipes;
    private List<PotionEffectSerializable> effects;

    public Potion() {
        this(PotionUtil.generatePotionID());
    }

    public Potion(String potionID) {
        this.potionID = potionID;
        this.colour = new Colour();
        this.name = ColourUtil.getChatColor(this.colour) + "New Potion";
        this.type = Material.POTION;
        this.recipes = new ArrayList<>();
        this.effects = new ArrayList<>();
    }

    /**
     * Clones a Potion.
     */
    public Potion clone() {
        try {
            return (Potion) super.clone();
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    // existingPotion must be modified appropriately e.g. if a type is chosen, the type of existingPotion must be correctly set in the passed-in existingPotion
    /*public PotionObject(ItemStack existingPotion) {
        State state = new State(ItemStackUtil.getLocalizedName(existingPotion));
        this.potionID = state.getPotionID();
        this.name = state.getPotionName();
        this.type = existingPotion.getType();
        this.colour = new Colour(ColourUtil.getPotionColor(existingPotion));
        this.recipes = state.getPotionRecipes();
        this.effects = PotionUtil.getEffects(existingPotion);
    }*/

    // Setters
    public void setPotionID(String potionId) {
        this.potionID = potionId;
    }
    public void setColour(Colour colour) {
        this.colour = colour;
    }
    public Potion setName(String name) {
        this.name = name;
        return this;
    }
    public void setType(Material type) {
        this.type = type;
    }
    public void setRecipes(List<PotionRecipe> recipes) {
        this.recipes = recipes;
    }
    public void addRecipe(PotionRecipe recipe) {
        this.recipes.add(recipe);
    }
    public void removeRecipe(PotionRecipe toBeRemoved) {
        this.recipes.removeIf(toBeRemoved::equals);
    }
    public void setEffects(List<PotionEffectSerializable> effects) {
        this.effects = effects;
    }
    public void addEffect(PotionEffectSerializable effect) {
        this.effects.add(effect);
    }

    /**
     * Not to be confused with cloning the object; this duplicates the potion with a new ID.
     * In the domain of this plugin, it is a "clone".
     * @return
     */
    public Potion duplicate() {
        Potion cloned = this.clone();
        return cloned
            .resetID()
            .setName(this.getName() + " (Copy)");
    }

    /**
     * Make a new ID for the potion (useful when cloning a potion).
     */
    public Potion resetID() {
        this.potionID = PotionUtil.generatePotionID();
        return this;
    }

    // Getters
    public String getPotionID() {
        return this.potionID;
    }
    public String getName() {
        return this.name;
    }
    public Material getType() {
        return this.type;
    }
    public List<PotionRecipe> getRecipes() {
        return this.recipes;
    }
    public ItemStack toItemStack() {
        ItemStack potion = new ItemStack(this.type);
        PotionUtil.setColor(potion, this.colour);
        ItemStackUtil.setDisplayName(potion, this.name);
        PotionUtil.setEffects(potion, this.effects);
        debugCustomPotion(); // hhh
        return potion;
    } // TODO version of this method which also applies the state to the localized name NVM dont
    // TODO may need version of toItemStack with correct lore (potency + readable duration) for brewing

    public boolean hasEffect(PotionEffectType effectType) {
        for (PotionEffectSerializable effect : this.effects) {
            if (effect.getType() == effectType) { // TODO verify that this equality works
                return true;
            }
        }
        return false;
    }

    public boolean isLingering() {
        return this.type == Material.LINGERING_POTION;
    }

    // Debugging
    public static void debugCustomPotions() {
        List<Potion> customPotions = PotionReader.getCustomPotions();
        if (customPotions.size() == 0) {
            Main.log.info("No custom potions to debug.");
            return;
        } else {
            Main.log.info(String.format("Debugging %d custom potions:", customPotions.size()));
        }
        for (Potion customPotion : customPotions) {
            customPotion.debugCustomPotion();
        }
    }

    public void debugCustomPotion() {
        Main.log.info("    Potion ID: " + this.potionID);
        Main.log.info("        Name: " + this.name);
        Main.log.info("        Type: " + this.type.name());
        Main.log.info(String.format("        Colour: (%d, %d, %d)", this.colour.getR(), this.colour.getG(), this.colour.getB()));
        if (this.effects.size() == 0) {
            Main.log.info("        Effects: None");
        } else {
            Main.log.info("        Effects:");
            for (PotionEffectSerializable effect : this.effects) {
                Main.log.info("            " + effect.getType().getName() + ":");
                Main.log.info("                Duration (ticks): " + effect.getDuration());
                Main.log.info("                Amplifier: " + effect.getAmplifier());
            }
        }
        if (this.recipes.size() == 0) {
            Main.log.info("        Recipes: None");
        } else {
            Main.log.info("        Recipes:");
            for (PotionRecipe recipe : this.recipes) {
                String ingredient = StringUtil.titleCase(recipe.getIngredient().name(), "_");
                String base = ChatColor.stripColor(PotionUtil.potionNameFromID(recipe.getBase()));
                Main.log.info("            " + ingredient + " + " + base);
            }
        }
    }



}
