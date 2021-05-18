package xyz.iiinitiationnn.custompotions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.UUID;

public class PotionRecipeObject implements Serializable {
    private Material ingredient;
    private String base;
    private String result;

    public PotionRecipeObject(Material ingredient, String base, String result) {
        this.ingredient = ingredient;
        this.base = base;
        this.result = result;
    }

    public Material getIngredient() {
        return this.ingredient;
    }

    public String getBase() {
        return this.base;
    }

    public String getResult() {
        return this.result;
    }

    public void setIngredient(Material ingredient) {
        this.ingredient = ingredient;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean conflictsWith(PotionRecipeObject potionRecipe) {
        return this.ingredient == potionRecipe.ingredient && this.base.equals(potionRecipe.base)
                && !this.result.equals(potionRecipe.result);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                append(this.ingredient).
                append(this.base).
                append(this.result).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PotionRecipeObject))
            return false;
        if (obj == this)
            return true;

        PotionRecipeObject r = (PotionRecipeObject) obj;
        return new EqualsBuilder().
                append(this.ingredient, r.ingredient).
                append(this.base, r.base).
                append(this.result, r.result).
                isEquals();
    }

}
