package xyz.iiinitiationnn.custompotions.inventorytypes;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.states.State;
import xyz.iiinitiationnn.custompotions.utils.MagicNumber;

import java.util.Map;

public class ChestConfirmInventory extends ChestInventory {
    public ChestConfirmInventory(String title) {
        super(title);
    }

    @Override
    protected Inventory createInventory(State state) {
        Inventory inv = Bukkit.createInventory(null, MagicNumber.INVENTORY_SIZE - 18, this.title);

        // Add buttons
        for (Map.Entry<Integer, ItemStack> button : state.calculateButtons().entrySet()) {
            inv.setItem(button.getKey(), button.getValue());
        }

        // Add potion
        inv.setItem(MagicNumber.CONFIRM_POTION_DISPLAY_SLOT, state.calculateInventoryItems().get(0));

        return inv;
    }
}
