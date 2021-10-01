package xyz.iiinitiationnn.custompotions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Material;

import java.io.Serializable;

public class PotionRecipe implements Serializable {
    private Material ingredient;
    private String base; // potion ID
    private String result; // potion ID

    public PotionRecipe(Material ingredient, String base, String result) {
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

    public boolean conflictsWith(PotionRecipe potionRecipe) {
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
        if (!(obj instanceof PotionRecipe))
            return false;
        if (obj == this)
            return true;

        PotionRecipe r = (PotionRecipe) obj;
        return new EqualsBuilder().
            append(this.ingredient, r.ingredient).
            append(this.base, r.base).
            append(this.result, r.result).
            isEquals();
    }

}
