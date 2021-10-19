package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.AnvilInventory;

import java.util.List;
import java.util.Map;

public class PotionNameMenu extends State {
    public PotionNameMenu(State state) {
        super(state, new AnvilInventory(ChatColor.GOLD + "Enter a Name"), new Input());
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
