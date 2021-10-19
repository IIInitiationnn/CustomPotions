package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestInventory;

import java.util.Collections;
import java.util.List;

public class EffectTypeMenu extends State {
    private final static int pageSize = 49;

    public EffectTypeMenu(State state) {
        super(state, new ChestInventory(ChatColor.GOLD + "Select an Effect Type"), new Input());
    }

    @Override
    protected boolean needsNextPage() {
        return false; // TODO
    }

    @Override
    protected List<String> previousMenuLore() {
        return Collections.singletonList(ChatColor.GOLD + "Colour Selection");
    }

    @Override
    protected List<String> nextMenuLore() {
        return Collections.singletonList(ChatColor.GREEN + "Recipe Ingredient(s) Selection");
    }

    @Override
    public List<ItemStack> calculatePotions() {
        return null; // TODO
    }
}
