package xyz.iiinitiationnn.custompotions;

import java.io.Serializable;

public class Input implements Serializable, Cloneable {
    private String effectType;
    private int effectDuration; // ticks
    private String ingredient;

    public Input clone() {
        try {
            return (Input) super.clone();
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public String getEffectType() {
        return this.effectType;
    }

    public int getEffectDuration() {
        return this.effectDuration;
    }

    public String getIngredient() {
        return this.ingredient;
    }

    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }

    public void setEffectDuration(int effectDuration) {
        this.effectDuration = effectDuration;
    }


    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }


}
