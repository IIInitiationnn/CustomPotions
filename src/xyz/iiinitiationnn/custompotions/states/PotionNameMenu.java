package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.EnterNameAction;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.AnvilInventory;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PotionNameMenu extends State {
    public PotionNameMenu(State state) {
        super(state, new AnvilInventory(ChatColor.GOLD + "Enter a Name"), new Input());
    }

    @Override
    public List<ItemStack> calculateInventoryItems() {
        State nextState = this.clone();
        nextState.setAction(new EnterNameAction());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GOLD + "The potion name can include Minecraft chat code colours.");
        lore.add(ChatColor.GOLD + "Prefix the name with the code to change the colour / style.");
        lore.add(ChatColor.GOLD + "(e.g. try \"&6Nectar\" for a potion named Nectar coloured gold!)");
        lore.add(ChatColor.GOLD + "You will have the option to review changes in the next menu.");
        lore.add("");
        lore.add(ChatColor.GREEN + "Click the output slot to continue.");
        lore.add(ChatColor.GOLD + "Click the left input slot to skip (e.g. if you have misclicked).");
        lore.add(ChatColor.RED + "Press ESC to exit without saving.");

        ItemStack potion = nextState.getPotionItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        ItemStackUtil.addLore(potion, lore);

        return Collections.singletonList(potion);
    }
}
