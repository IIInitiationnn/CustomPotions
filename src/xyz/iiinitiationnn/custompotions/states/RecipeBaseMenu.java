package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestInventory;
import xyz.iiinitiationnn.custompotions.utils.MagicNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBaseMenu extends State {
    private final static int pageSize = 50;

    public RecipeBaseMenu(State state) {
        super(state, new ChestInventory(ChatColor.GOLD + "Select a Base for the Recipe"), state.input);
    }

    @Override
    protected boolean needsNextPage() {
        return false; // TODO
    }

    @Override
    protected List<String> previousMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Recipe Ingredient(s) Selection");
        lore.add(ChatColor.RED + "Warning: you will lose your choice of ingredient!");
        return lore;
    }

    @Override
    public Map<Integer, ItemStack> calculateButtons() {
        Map<Integer, ItemStack> buttons = new HashMap<>();
        buttons.put(MagicNumber.PREVIOUS_PAGE_SLOT, this.previousPageButton());
        buttons.put(MagicNumber.NEXT_PAGE_SLOT, this.nextPageButton());
        buttons.put(MagicNumber.NEXT_MENU_SLOT, this.previousMenuButton(this.previousMenuLore()));
        buttons.put(MagicNumber.EXIT_SLOT, this.exitButton());
        return buttons;
    }

    @Override
    public List<ItemStack> calculatePotions() {
        return null; // TODO
    }
}
