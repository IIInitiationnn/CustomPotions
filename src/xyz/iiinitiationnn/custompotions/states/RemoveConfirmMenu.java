package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestConfirmInventory;

import java.util.List;

public class RemoveConfirmMenu extends State {
    public RemoveConfirmMenu(State state) {
        super(state, new ChestConfirmInventory(ChatColor.GOLD + "Confirm Potion Deletion"), new Input());
        // TODO need to differentiate from normal inv, will probably be in the calculatePotions here i suppose
    }

    @Override
    protected boolean needsNextPage() {
        return false; // TODO
    }

    @Override
    public List<ItemStack> calculatePotions() {
        return null; // TODO
    }
}
