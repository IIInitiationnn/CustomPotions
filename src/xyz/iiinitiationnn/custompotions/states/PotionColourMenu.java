package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.SelectPotionColourAction;
import xyz.iiinitiationnn.custompotions.Colour;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestInventory;
import xyz.iiinitiationnn.custompotions.utils.ColourUtil;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PotionColourMenu extends State {
    private final static int pageSize = 49;

    public PotionColourMenu(State state) {
        super(state, new ChestInventory(ChatColor.GOLD + "Select a Potion Colour"), new Input());
    }

    @Override
    protected boolean needsNextPage() {
        return false; // TODO
    }

    @Override
    protected List<String> previousMenuLore() {
        return Collections.singletonList(ChatColor.GOLD + "Potion Type Selection");
    }

    @Override
    protected List<String> nextMenuLore() {
        return Collections.singletonList(ChatColor.GREEN + "Effect Type(s) Selection");
    }

    /**
     * Fetches potions for the potion colour selection menu.
     */
    @Override
    public List<ItemStack> calculatePotions() {
        State nextStateBase = this.clone();
        nextStateBase.setAction(new SelectPotionColourAction());
        List<Colour> colours = ColourUtil.defaultPotionColourList();

        List<ItemStack> allPotions = new ArrayList<>();

        for (Colour colour : colours) {
            State nextState = nextStateBase.clone();
            nextState.setPotionColour(colour);
            ItemStack potion = nextState.getPotionItemStack();

            ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());

            ChatColor c = ColourUtil.getChatColor(colour);
            ItemStackUtil.setDisplayName(potion, c + ColourUtil.defaultPotionColourMapReverse().get(colour));
            allPotions.add(potion);
        }

        return allPotions;
    }
}
