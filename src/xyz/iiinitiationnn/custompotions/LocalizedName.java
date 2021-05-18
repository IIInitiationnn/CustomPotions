package xyz.iiinitiationnn.custompotions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static xyz.iiinitiationnn.custompotions.InventoryGUI.getMenuAfter;
import static xyz.iiinitiationnn.custompotions.InventoryGUI.getMenuBefore;

/**
 * Class representing localized_name field within Minecraft ItemStacks.
 * Useful for interacting with the InventoryGUI by transmitting information about the current state.
 */
public class LocalizedName implements Cloneable, Serializable {
    // 0             1  2    3       4         5    6      7
    // custompotions.ID.name.recipes.menuClick.page.action.extraField

    // action: exit, pageNext, pagePrevious, pageInvalid, createPotion, selectPotion, selectType,
    //         selectColour, addEffectType, selectEffectType, enterEffectDuration, enterEffectAmplifier,
    //         addRecipeIngredient, selectRecipeIngredient, addRecipeBase, removeRecipeBase, recipeBaseInvalid,
    //         enterName, finalInvalid, finalEdit, finalConfirm, skipL, skipR, give

    // Constants
    private static final String DELIMITER = ".";
    private static final int PREFIX = 0;
    private static final int ID = 1;
    private static final int NAME = 2;
    private static final int RECIPES = 3;
    private static final int MENU = 4;
    private static final int PAGE = 5;
    private static final int ACTION = 6;
    private static final int EXTRAFIELD = 7;

    private static final String RECIPE_SEPARATOR = ";";
    private static final String COMPONENT_SEPARATOR = ",";

    // Instance Variables
    private final String prefix;
    private String potionID;
    private String potionName;
    private List<PotionRecipeObject> potionRecipes;
    private String menu;
    private int page;
    private String action;
    private String extraField = null;

    // Constructors
    /**
     * Constructs a state for startup.
     */
    public LocalizedName() {
        this.prefix = "custompotions";
        this.potionID = PotionUtil.generatePotionID();
        this.potionName = "New Potion";
        this.potionRecipes = new ArrayList<>();
        this.menu = "mainMenu";
        this.page = 0;
        this.action = "createPotion";
    }
    /**
     * Constructs a state from supplied information representing what clicking an item will do.
     */
    public LocalizedName(String potionID, String potionName, List<PotionRecipeObject> potionRecipes, String menu, int page, String action) {
        this.prefix = "custompotions";
        this.potionID = potionID;
        this.potionName = potionName;
        this.potionRecipes = potionRecipes;
        this.menu = menu;
        this.page = page;
        this.action = action;
    }

    /**
     * Constructs a state from a Bukkit ItemStack's localized name.
     */
    public LocalizedName(String potionName) {
        String[] fields = potionName.split("\\" + DELIMITER);
        this.prefix = fields[PREFIX];
        this.potionID = fields[ID];
        this.potionName = fields[NAME];
        this.potionRecipes = toPotionRecipeObjectList(this.potionID, fields[RECIPES]);
        this.menu = fields[MENU];
        this.page = Integer.parseInt(fields[PAGE]);
        this.action = fields[ACTION];
    }

    // Methods
    /**
     * Clones a LocalizedName, but resets the extra field parameter.
     */
    public LocalizedName clone() {
        try {
            LocalizedName c = (LocalizedName)super.clone();
            c.setExtraField(null);
            return c;
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public void setPotionID(String potionID) {
        this.potionID = potionID;
    }

    public void setPotionName(String potionName) {
        this.potionName = potionName;
    }

    public void addPotionRecipe(PotionRecipeObject potionRecipe) {
        this.potionRecipes.add(potionRecipe);
    }

    public void setPotionRecipes(List<PotionRecipeObject> potionRecipes) {
        this.potionRecipes = potionRecipes;
    }

    private void setPotionRecipes(String potionRecipes) {
        this.potionRecipes = toPotionRecipeObjectList(this.potionID, potionRecipes);
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setExtraField(String extraField) {
        this.extraField = extraField;
    }

    public String getPotionID() {
        return this.potionID;
    }

    public String getPotionName() {
        return this.potionName;
    }

    public List<PotionRecipeObject> getPotionRecipes() {
        return this.potionRecipes;
    }

    public String getMenu() {
        return this.menu;
    }

    public int getPage() {
        return this.page;
    }

    public String getAction() {
        return this.action;
    }

    public String getExtraField() {
        return this.extraField;
    }

    public String toLocalizedString() {
        return prefix + DELIMITER + potionID + DELIMITER + potionName + ChatColor.RESET + DELIMITER
                + toPotionRecipeString(potionRecipes) + DELIMITER + menu + DELIMITER + page + DELIMITER + action
                + (extraField != null ? DELIMITER + extraField : "");
    }

    private static List<PotionRecipeObject> toPotionRecipeObjectList(String potionID, String s) {
        List<PotionRecipeObject> potionRecipeObjects = new ArrayList<>();
        if (s.equals("")) {
            return potionRecipeObjects;
        }
        String[] recipes = s.split("\\" + RECIPE_SEPARATOR);
        for (String recipe : recipes) {
            String[] components = recipe.split("\\" + COMPONENT_SEPARATOR);
            String ingredient = components[0];
            String base = components[1];
            potionRecipeObjects.add(new PotionRecipeObject(Material.matchMaterial(ingredient), base, potionID));
        }
        return potionRecipeObjects;
    }

    private static String toPotionRecipeString(List<PotionRecipeObject> recipes) {
        String s = "";
        for (PotionRecipeObject recipe : recipes) {
            s += recipe.getIngredient().name() + COMPONENT_SEPARATOR + recipe.getBase() + RECIPE_SEPARATOR;
        }
        if (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * Encode state object as a string.
     */
    public static String toString(LocalizedName state) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
        objectOut.writeObject(state);
        objectOut.close();
        return Base64.getEncoder().encodeToString(byteOut.toByteArray());
    }

    /**
     * Decode a string to obtain the state.
     */
    private static Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] byteData = Base64.getDecoder().decode(s);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteData);
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        LocalizedName state = (LocalizedName) objectIn.readObject();
        objectIn.close();
        return state;
    }

    public void nextMenu() {
        this.menu = getMenuAfter(this.menu);
    }

    public void previousMenu() {
        this.menu = getMenuBefore(this.menu);
    }

    public static boolean isCustomPotionsClick(String name) {
        return name.split("\\" + DELIMITER)[PREFIX].equals("custompotions");
    }

    /* commented out for now, not sure what it was written to be used for
    public static boolean isInvalidPageSelection(String name) {
        return name.split("\\" + DELIMITER)[ACTION].equals("pageInvalid");
    }*/


}
