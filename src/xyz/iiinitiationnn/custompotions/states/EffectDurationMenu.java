package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.EnterEffectDurationAction;
import xyz.iiinitiationnn.custompotions.inventorytypes.AnvilInventory;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EffectDurationMenu extends State {
    public EffectDurationMenu(State state) {
        super(state, new AnvilInventory(ChatColor.GOLD + "Effect Duration"), state.input);
    }

    @Override
    public List<ItemStack> calculateInventoryItems() {
        State nextState = this.clone();
        nextState.setAction(new EnterEffectDurationAction());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        if (this.isPotionLingering()) {
            lore.add(ChatColor.GOLD + "Enter the effect duration in seconds (1 to 26,843,545).");
        } else {
            lore.add(ChatColor.GOLD + "Enter the effect duration in seconds (1 to 107,374,182).");
        }
        lore.add("");
        lore.add(ChatColor.GREEN + "Click the output slot to continue.");
        lore.add(ChatColor.GOLD + "Click the left input slot to skip (e.g. if you have misclicked).");
        lore.add(ChatColor.RED + "Press ESC to exit without saving.");

        ItemStack potion = nextState.getPotionItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        ItemStackUtil.addLore(potion, lore);

        return Collections.singletonList(potion);
    }
}
