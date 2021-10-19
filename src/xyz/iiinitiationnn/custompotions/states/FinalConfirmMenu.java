package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestConfirmInventory;

import java.util.List;

public class FinalConfirmMenu extends State {
    public FinalConfirmMenu(State state) {
        super(state, new ChestConfirmInventory(ChatColor.GOLD + "Confirm Changes"), new Input());
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
