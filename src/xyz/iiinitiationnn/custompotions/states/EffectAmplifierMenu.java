package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.EnterEffectAmplifierAction;
import xyz.iiinitiationnn.custompotions.inventorytypes.AnvilInventory;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;
import xyz.iiinitiationnn.custompotions.utils.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EffectAmplifierMenu extends State {
    public EffectAmplifierMenu(State state) {
        super(state, new AnvilInventory(ChatColor.GOLD + "Effect Amplifier"), state.input);
    }

    @Override
    public List<ItemStack> calculateInventoryItems() {
        State nextState = this.clone();
        nextState.setAction(new EnterEffectAmplifierAction());
        String effectTypeName = this.getInputEffectType();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GOLD + "Enter the effect amplifier (integer from 1 to " + (PotionUtil.maxAmp(effectTypeName) + 1) + ").");
        lore.add(ChatColor.GOLD + "1 means potency I, e.g. " + StringUtil.toCommonName(effectTypeName) + " I.");
        lore.add(ChatColor.GOLD + "Similarly, 2 means potency II, e.g. " + StringUtil.toCommonName(effectTypeName) + " II, and so on.");
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
