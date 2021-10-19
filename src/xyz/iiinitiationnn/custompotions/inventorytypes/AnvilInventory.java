package xyz.iiinitiationnn.custompotions.inventorytypes;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import xyz.iiinitiationnn.custompotions.states.State;

public class AnvilInventory extends InventoryType<AnvilGUI.Builder> {
    public AnvilInventory(String title) {
        super(title);
    }

    @Override
    protected AnvilGUI.Builder createInventory(State state) {
        return new AnvilGUI.Builder(); // TODO
    }

    @Override
    public void openInventory(State state, Player player) {
        this.createInventory(state).open(player);
    }
}
