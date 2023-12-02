package xyz.iiinitiationnn.custompotions.inventorytypes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.states.State;
import xyz.iiinitiationnn.custompotions.utils.MagicNumber;

import java.util.Map;

public class ChestInventory extends InventoryType<Inventory> {

    public ChestInventory(String title) {
        super(title);
    }

    @Override
    protected Inventory createInventory(State state) {
        Inventory inv = Bukkit.createInventory(null, MagicNumber.INVENTORY_SIZE, this.title);

        // Add buttons
        for (Map.Entry<Integer, ItemStack> button : state.calculateButtons().entrySet()) {
            inv.setItem(button.getKey(), button.getValue());
        }

        // Add potions
        for (ItemStack potion: state.calculateInventoryItems()) {
            inv.addItem(potion);
        }

        return inv;
    }

    @Override
    public void openInventory(State state, Player player) {
        player.openInventory(this.createInventory(state));
    }
}
