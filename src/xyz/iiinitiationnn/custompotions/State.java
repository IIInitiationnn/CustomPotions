package xyz.iiinitiationnn.custompotions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

/**
 * Class representing localized_name field within Minecraft ItemStacks.
 * Useful for interacting with the InventoryGUI by transmitting information about the current state.
 */
public class State implements Cloneable, Serializable {
    // 0             1  2    3       4         5    6      7
    // custompotions.ID.name.recipes.menuClick.page.action.extraField

    // menu: mainMenu, potionType, potionColour, effectType, effectDuration, effectAmplifier, recipeIngredient, recipeBase, potionName, finalMenu

    // action: exit, pageNext, pagePrevious, pageInvalid, createPotion, selectPotion, selectType,
    //         selectColour, noEffects, addEffectType, selectEffectType, enterEffectDuration, enterEffectAmplifier,
    //         addRecipeIngredient, selectRecipeIngredient, addRecipeBase, removeRecipeBase, recipeBaseInvalid,
    //         enterName, finalInvalid, finalEdit, finalConfirm, skipL, skipR, give

    // Instance Variables
    private Potion potion;
    private Menu menu;
    private int page; // starts at 0
    private String action;
    private Input input = new Input();

    // Constructors
    /**
     * Constructs a state for startup.
     */
    public State() {
        this.potion = new Potion(true);
        this.menu = new Menu();
        this.page = 0;
        this.action = "createPotion";
    }

    // Methods
    /**
     * Clones a State, but resets the extra field parameter.
     */
    public State clone() {
        try {
            State s = (State) super.clone();
            s.menu = this.menu.clone();
            s.potion = this.potion.clone();
            s.input = this.input.clone();
            return s;
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public void setPotion(Potion potion) {
        this.potion = potion;
    }
    public void nextMenu() {
        this.menu.nextMenu();
    }
    public void skipNextMenu() {
        this.menu.skipNextMenu();
    }
    public void skipPreviousMenu() {
        this.menu.skipPreviousMenu();
    }
    public void setPage(int page) {
        this.page = page;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public Potion getPotion() {
        return this.potion;
    }
    public String getMenuName() {
        return this.menu.getName();
    }
    public int getPage() {
        return this.page;
    }
    public String getAction() {
        return this.action;
    }
    public Input getInput() {
        return this.input;
    }

    public State resetInput() {
        this.input = new Input();
        return this;
    }

    /**
     * Encode state object as a string.
     */
    public String encodeToString() {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(this);
            objectOut.close();
            return Base64.getEncoder().encodeToString(byteOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Main.log.severe("There was an error encoding the state to a string.");
            return null;
        }
        // return new String(SerializationUtils.serialize(this)); TODO doesnt work https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
    }

    /**
     * Constructs a state from a Bukkit ItemStack's localized name.
     */
    public static State decodeFromString(String localizedName) throws IOException, ClassNotFoundException {
        byte[] byteData = Base64.getDecoder().decode(localizedName);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteData);
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        State state = (State) objectIn.readObject();
        objectIn.close();
        return state;
        // return (State) SerializationUtils.deserialize(localizedName.getBytes());
    }

    /* commented out for now, not sure what it was written to be used for
    public static boolean isInvalidPageSelection(String name) {
        return name.split("\\" + DELIMITER)[ACTION].equals("pageInvalid");
    }*/


}
