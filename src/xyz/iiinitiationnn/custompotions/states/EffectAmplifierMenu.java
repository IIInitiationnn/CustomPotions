package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.inventorytypes.AnvilInventory;

import java.util.List;
import java.util.Map;

public class EffectAmplifierMenu extends State {
    public EffectAmplifierMenu(State state) {
        super(state, new AnvilInventory(ChatColor.GOLD + "Effect Amplifier"), state.input);
    }

    @Override
    protected boolean needsNextPage() {
        return false; // TODO
    }

    @Override
    public Map<Integer, ItemStack> calculateButtons() {
        return super.calculateButtons(); // TODO
    }

    @Override
    public List<ItemStack> calculatePotions() {
        return null; // TODO
    }
}
