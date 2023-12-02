package xyz.iiinitiationnn.custompotions.inventorytypes;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.iiinitiationnn.custompotions.Main;
import xyz.iiinitiationnn.custompotions.states.State;

public class AnvilInventory extends InventoryType<AnvilGUI.Builder> {
    public AnvilInventory(String title) {
        super(title);
    }

    @Override
    protected AnvilGUI.Builder createInventory(State state) {
        return new AnvilGUI.Builder()
            .plugin(Main.getPlugin(Main.class))
            .title(this.title)
            .text(ChatColor.RESET + "Enter here:")
            .itemLeft(state.calculateInventoryItems().get(0))
            .onComplete((whoTyped, whatWasTyped) -> AnvilGUI.Response.close())
            .preventClose();
    }

    @Override
    public void openInventory(State state, Player player) {
        this.createInventory(state).open(player);
    }
}
