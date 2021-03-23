package xyz.iiinitiationnn.custompotions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PotionRecipeObject {
    private Material reagent;
    private String base;
    private String result;

    public PotionRecipeObject(Material reagent, String base, String result) {
        this.reagent = reagent;
        this.base = base;
        this.result = result;
    }

    public void setReagent(Material reagent) {
        this.reagent = reagent;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
