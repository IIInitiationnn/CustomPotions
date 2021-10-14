package xyz.iiinitiationnn.custompotions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Menu implements Cloneable, Serializable {
    private static final List<String> allMenus = Arrays.asList("mainMenu", "potionType", "potionColour",
        "effectType", "effectDuration", "effectAmplifier", "recipeIngredient",
        "recipeBase", "potionName", "finalMenu");
    private String name;

    public Menu() {
        this.name = "mainMenu";
    }

    // TODO ideally remove this...good design should not need
    public String getName() {
        return this.name;
    }

    private static String nextMenuInOrder(String currentMenu) {
        int index = allMenus.indexOf(currentMenu);
        int newIndex = (index + 1) % allMenus.size();
        return allMenus.get(newIndex);
    }

    private static String previousMenuInOrder(String currentMenu) {
        int index = allMenus.indexOf(currentMenu);
        int newIndex = (index - 1) % allMenus.size();
        return allMenus.get(newIndex);
    }

    public void nextMenu() {
        if (this.name.equals("effectAmplifier")) {
            // after creating a new effect, return to the Effects menu screen
            this.name = "effectType";
        } else if (this.name.equals("recipeBase")) {
            this.name = "recipeIngredient";
        } else {
            this.name = nextMenuInOrder(this.name);
        }
    }

    public void skipNextMenu() {
        if (Arrays.asList("effectType", "effectDuration", "effectAmplifier").contains(this.name)) {
            this.name = "recipeIngredient";
        } else if (this.name.equals("recipeIngredient")) {
            this.name = "potionName";
        } else {
            this.name = nextMenuInOrder(this.name);
        }
    }

    public void skipPreviousMenu() {
        if (this.name.equals("recipeIngredient")) {
            this.name = "effectType";
        } else if (this.name.equals("potionName")) {
            this.name = "recipeIngredient";
        } else {
            this.name = previousMenuInOrder(this.name);
        }
    }

    @Override
    public Menu clone() {
        try {
            return (Menu) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
