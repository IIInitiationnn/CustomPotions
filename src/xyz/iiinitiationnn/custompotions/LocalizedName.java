package xyz.iiinitiationnn.custompotions;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import static xyz.iiinitiationnn.custompotions.InventoryGUI.getMenuAfter;
import static xyz.iiinitiationnn.custompotions.InventoryGUI.getMenuBefore;

/**
 * Class representing localized_name field within Minecraft ItemStacks.
 * Useful for interacting with the InventoryGUI by transmitting information about the current state.
 */
public class LocalizedName {
    // 0             1         2
    // custompotions.menuClick.page

    // action: startup, pageNext, pagePrevious, pageInvalid, createPotion, selectPotion, selectType,
    //         selectColour, addEffectType, selectEffectType, enterEffectDuration, enterEffectAmplifier, skipL, skipR,

    // Constants
    private static final String DELIMITER = ".";
    private static final int PREFIX = 0;
    private static final int MENU_CLICK = 1;
    private static final int PAGE = 2;
    private static final int ACTION = 3;

    // Instance Variables
    private String prefix;
    private String menuClick;
    private int page;
    private String action;

    // Constructors
    /**
     * Constructs a state for startup.
     */
    public LocalizedName() {
        this.prefix = "custompotions";
        this.menuClick = "mainMenu";
        this.page = 0;
        this.action = "startup";
    }
    /**
     * Constructs a state from supplied menu, page and action representing what clicking an item will do.
     */
    public LocalizedName(String menuClick, int page, String action) {
        this.prefix = "custompotions";
        this.menuClick = menuClick;
        this.page = page;
        this.action = action;
    }

    /**
     * Constructs a state from a Bukkit ItemStack's localized name.
     */
    public LocalizedName(String name) {
        String[] fields = name.split("\\" + DELIMITER);
        this.prefix = fields[PREFIX];
        this.menuClick = fields[MENU_CLICK];
        this.page = Integer.parseInt(fields[PAGE]);
        this.action = fields[ACTION];
    }



    // Methods
    public LocalizedName clone() {
        try {
            LocalizedName localizedName = (LocalizedName)super.clone();
            return localizedName;
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public void setMenuClick(String menuClick) {
        this.menuClick = menuClick;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMenuClick() {
        return this.menuClick;
    }

    public int getPage() {
        return this.page;
    }

    public String getAction() {
        return this.action;
    }

    public String getName() {
        return this.prefix + DELIMITER + this.menuClick + DELIMITER + this.page + DELIMITER + this.action;
    }

    public void nextMenu() {
        this.menuClick = getMenuAfter(this.menuClick);
    }

    public void previousMenu() {
        this.menuClick = getMenuBefore(this.menuClick);

    }

    public boolean isCustomPotionsClick(String name) {
        return name.split("\\" + DELIMITER)[PREFIX].equals("custompotions");
    }

    public boolean isInvalidPageSelection(String name) {
        return name.split("\\" + DELIMITER)[ACTION].equals("invalidPageChange");
    }


}
