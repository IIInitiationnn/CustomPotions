package xyz.iiinitiationnn.custompotions.inventorytypes;

import org.bukkit.inventory.Inventory;
import xyz.iiinitiationnn.custompotions.states.State;

public class ChestConfirmInventory extends ChestInventory {
    public ChestConfirmInventory(String title) {
        super(title);
    }

    @Override
    protected Inventory createInventory(State state) {
        return super.createInventory(state); // TODO
    }
}
