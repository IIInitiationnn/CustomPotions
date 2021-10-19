package xyz.iiinitiationnn.custompotions.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.iiinitiationnn.custompotions.states.State;

public class GUI {
    private State state;
    private Player player;

    public GUI(State currentState, Player player) {
        this.state = currentState;
        this.player = player;
    }

    public GUI updateEvent(InventoryClickEvent event) {
        this.state.setEvent(event);
        return this;
    }

    public GUI nextState() {
        this.state = state.nextState();
        return this;
    }

    public void open() {
        if (this.state == null) return; // no inventory to open
        this.state.openInventory(this.player);
    }
}
