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
import java.util.Objects;

public class Potion implements Serializable, Cloneable {
    private String potionId;
    private Colour colour;
    private String name;
    private Material type;
    private List<PotionRecipe> recipes;
    private List<PotionEffectSerializable> effects;

    public Potion() {
        this("NoIdError");
    }

    public Potion(boolean ignored) {
        this(PotionUtil.generatePotionId());
    }

    public Potion(String potionId) {
        this.potionId = potionId;
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
            Potion p = (Potion) super.clone();
            p.recipes = new ArrayList<>(this.recipes);
            p.effects = new ArrayList<>(this.effects);
            return p;
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    // Getters
    public String getPotionId() {
        return this.potionId;
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

    public List<PotionEffectSerializable> getEffects() {
        return this.effects;
    }

    public ChatColor getCorrespondingChatColor() {
        return ColourUtil.getChatColor(this.colour);
    }

    public ItemStack toItemStack() {
        ItemStack potion = new ItemStack(this.type);
        PotionUtil.setColor(potion, this.colour);
        ItemStackUtil.setDisplayName(potion, this.name);
        PotionUtil.setEffects(potion, this.effects);
        return potion;
    } // TODO version of this method which also applies the state to the localized name NVM dont
    // TODO may need version of toItemStack with correct lore (potency + readable duration) for brewing

    public boolean hasEffect(PotionEffectType effectType) {
        for (PotionEffectSerializable effect : this.effects) {
            if (Objects.equals(effect.getType(), effectType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLingering() {
        return this.type == Material.LINGERING_POTION;
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
    public void removeRecipe(PotionRecipe potion) {
        this.recipes.removeIf(potion::equals);
    }
    public void setEffects(List<PotionEffectSerializable> effects) {
        this.effects = effects;
    }
    public void addEffect(PotionEffectSerializable effect) {
        this.effects.add(effect);
    }
    public void removeEffectByName(String name) {
        this.effects.removeIf(effect -> Objects.equals(effect.getType().getName(), name));
    }
    public void removeRecipesByIngredient(String name) {
        this.recipes.removeIf(recipe -> Objects.equals(recipe.getIngredient().name(), name));
    }

    /**
     * Not to be confused with cloning the object; this duplicates the potion with a new ID.
     * In the domain of this plugin, it is a "clone".
     */
    public Potion duplicate() {
        Potion cloned = this.clone();
        return cloned
            .resetId()
            .setName(this.getName() + " (Copy)")
            .resetRecipes();
    }

    /**
     * Make a new ID for the potion (useful when cloning a potion).
     */
    public Potion resetId() {
        this.potionId = PotionUtil.generatePotionId();
        return this;
    }

    /**
     * Reset the recipes for the potion (useful when cloning a potion).
     */
    public Potion resetRecipes() {
        this.recipes = new ArrayList<>();
        return this;
    }

    // Debugging
    public static void debugCustomPotions() {
        List<Potion> customPotions = PotionUtil.getCustomPotions();
        if (customPotions.size() == 0) {
            Main.logInfo("No custom potions to debug.");
            return;
        } else {
            Main.logInfo(String.format("Debugging %d custom potions:", customPotions.size()));
        }
        for (Potion customPotion : customPotions) {
            customPotion.debugCustomPotion(customPotions);
        }
    }

    public void debugCustomPotion(List<Potion> customPotions) {
        Main.logInfo("    Potion ID: " + this.potionId);
        Main.logInfo("        Name: " + this.name);
        Main.logInfo("        Type: " + this.type.name());
        Main.logInfo(String.format("        Colour: (%d, %d, %d)", this.colour.getR(), this.colour.getG(), this.colour.getB()));
        if (this.effects.size() == 0) {
            Main.logInfo("        Effects: None");
        } else {
            Main.logInfo("        Effects:");
            for (PotionEffectSerializable effect : this.effects) {
                Main.logInfo("            " + effect.getType().getName() + ":");
                Main.logInfo("                Duration (ticks): " + effect.getDuration());
                Main.logInfo("                Amplifier: " + effect.getAmplifier());
            }
        }
        if (this.recipes.size() == 0) {
            Main.logInfo("        Recipes: None");
        } else {
            Main.logInfo("        Recipes:");
            for (PotionRecipe recipe : this.recipes) {
                String ingredient = StringUtil.titleCase(recipe.getIngredient().name(), "_");
                String base = ChatColor.stripColor(PotionUtil.potionNameFromID(recipe.getBase(), customPotions));
                Main.logInfo("            " + ingredient + " + " + base);
            }
        }
    }
}
