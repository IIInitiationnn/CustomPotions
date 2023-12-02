package xyz.iiinitiationnn.custompotions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Main;
import xyz.iiinitiationnn.custompotions.gui.GUI;
import xyz.iiinitiationnn.custompotions.states.State;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;

import java.io.IOException;

public class InventoryGUIListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        ItemStack interaction = event.getCurrentItem();

        if (inv == null || interaction == null || interaction.getItemMeta() == null || event.getCurrentItem() == null) {
            return;
        }
        State state;
        try {
            state = State.decodeFromString(ItemStackUtil.getLocalizedName(interaction));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            // Not a State class
            return;
        }
        Main.log.info("Action: " + state.getAction() + "; Menu: " + state.getClass().getSimpleName());
        // TODO prevent placing in the custom inventories, including dragging event
        //  is this covered by interaction == null? (for the placing part)

        event.setCancelled(true);
        new GUI(state, (Player) event.getWhoClicked())
            .updateEvent(event)
            .nextState()
            .open();
    }
}