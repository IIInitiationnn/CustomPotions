package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.ExitUnsavedAction;
import xyz.iiinitiationnn.custompotions.Actions.FinalConfirmAction;
import xyz.iiinitiationnn.custompotions.Actions.FinalEditAction;
import xyz.iiinitiationnn.custompotions.Actions.InvalidAction;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestConfirmInventory;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.MagicNumber;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinalConfirmMenu extends State {
    public FinalConfirmMenu(State state) {
        super(state, new ChestConfirmInventory(ChatColor.GOLD + "Confirm Changes"), new Input());
    }

    /**
     * Returns a button which brings the user to the first menu.
     */
    private ItemStack editButton() {
        ItemStack button = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);

        State nextState = this.clone();
        nextState.setAction(new FinalEditAction());

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());
        ItemStackUtil.setDisplayName(button, ChatColor.GOLD + "Edit the Potion");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Click to go back and make additional changes to your potion.");
        ItemStackUtil.addLore(button, lore);

        return button;
    }

    /**
     * Returns a button which confirms and saves the potion.
     */
    private ItemStack confirmButton() {
        ItemStack button = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

        State nextState = this.clone();
        nextState.setAction(new FinalConfirmAction());

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());
        ItemStackUtil.setDisplayName(button, ChatColor.GREEN + "Save and Confirm");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Click to save your potion.");
        ItemStackUtil.addLore(button, lore);

        return button;
    }

    /**
     * Returns a button which exits the menu without saving changes to the potion.
     */
    private ItemStack unsavedExitButton() {
        ItemStack button = new ItemStack(Material.RED_STAINED_GLASS_PANE);

        State nextState = this.clone();
        nextState.setAction(new ExitUnsavedAction());

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());
        ItemStackUtil.setDisplayName(button, ChatColor.RED + "Exit without Saving");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Click to exit without saving any changes.");
        ItemStackUtil.addLore(button, lore);

        return button;
    }

    @Override
    public Map<Integer, ItemStack> calculateButtons() {
        Map<Integer, ItemStack> buttons = new HashMap<>();
        buttons.put(MagicNumber.CONFIRM_EDIT_SLOT, this.editButton());
        buttons.put(MagicNumber.CONFIRM_CONFIRM_SLOT, this.confirmButton());
        buttons.put(MagicNumber.CONFIRM_EXIT_SLOT, this.unsavedExitButton());
        return buttons;
    }

    /**
     * Final potion to be displayed for confirmation.
     */
    @Override
    public List<ItemStack> calculateInventoryItems() {
        State nextState = this.clone();
        nextState.setAction(new InvalidAction());

        ItemStack potion = nextState.getPotionItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        PotionUtil.addLoreRecipes(potion, nextState.getPotion());

        return Collections.singletonList(potion);
    }
}
