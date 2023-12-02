package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.SelectPotionTypeAction;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestInventory;
import xyz.iiinitiationnn.custompotions.utils.ColourUtil;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PotionTypeMenu extends State {
    public PotionTypeMenu(State state) {
        super(state, new ChestInventory(ChatColor.GOLD + "Select a Potion Type"), new Input());
    }

    @Override
    protected List<String> previousMenuLore() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Potion Selection");
        lore.add(ChatColor.RED + "Warning: you will lose your unsaved changes!");
        lore.add(ChatColor.RED + "Save a completed potion to validate your changes.");
        return lore;
    }

    @Override
    protected List<String> nextMenuLore() {
        return Collections.singletonList(ChatColor.GREEN + "Colour Selection");
    }

    /**
     * Fetches potions for the potion type selection menu including:
     * potions, splash potions, and lingering potions.
     */
    @Override
    public List<ItemStack> calculateInventoryItems() {
        State nextStateBase = this.clone();
        nextStateBase.setAction(new SelectPotionTypeAction());
        ChatColor chatColor = this.getPotionChatColor();

        List<ItemStack> allPotions = new ArrayList<>();

        // Potion
        State nextStateP = nextStateBase.clone();
        nextStateP.setPotionType(Material.POTION);
        ItemStack potion = nextStateP.getPotionItemStack();

        ItemStackUtil.setLocalizedName(potion, nextStateP.encodeToString());
        ItemStackUtil.setDisplayName(potion, chatColor + "Potion");
        allPotions.add(potion);

        // Splash Potion
        State nextStateS = nextStateBase.clone();
        nextStateS.setPotionType(Material.SPLASH_POTION);
        ItemStack splashPotion = nextStateS.getPotionItemStack();

        ItemStackUtil.setLocalizedName(splashPotion, nextStateS.encodeToString());
        ItemStackUtil.setDisplayName(splashPotion, chatColor + "Splash Potion");
        allPotions.add(splashPotion);

        // Lingering Potion
        State nextStateL = nextStateBase.clone();
        nextStateL.setPotionType(Material.LINGERING_POTION);
        ItemStack lingeringPotion = nextStateL.getPotionItemStack();

        ItemStackUtil.setLocalizedName(lingeringPotion, nextStateL.encodeToString());
        ItemStackUtil.setDisplayName(lingeringPotion, chatColor + "Lingering Potion");
        allPotions.add(lingeringPotion);

        return allPotions;
    }
}
