package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.iiinitiationnn.custompotions.Actions.AddEffectTypeAction;
import xyz.iiinitiationnn.custompotions.Actions.NoEffectsAction;
import xyz.iiinitiationnn.custompotions.Actions.SelectEffectTypeAction;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestInventory;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;
import xyz.iiinitiationnn.custompotions.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EffectTypeMenu extends State {
    private final static int pageSize = 49;

    public EffectTypeMenu(State state) {
        super(state, new ChestInventory(ChatColor.GOLD + "Select an Effect Type"), new Input());
    }

    @Override
    protected List<String> previousMenuLore() {
        return Collections.singletonList(ChatColor.GOLD + "Colour Selection");
    }

    @Override
    protected List<String> nextMenuLore() {
        return Collections.singletonList(ChatColor.GREEN + "Recipe Ingredient(s) Selection");
    }

    /**
     * Fetches potions for the potion effect type selection menu.
     */
    @Override
    public List<ItemStack> calculateInventoryItems() {
        ChatColor chatColor = this.getPotionChatColor();

        List<ItemStack> allPotions = new ArrayList<>();

        // No Effects
        State nextStatePlain = this.clone();
        nextStatePlain.setAction(new NoEffectsAction());
        nextStatePlain.setPotionEffects(new ArrayList<>());
        ItemStack plainPotion = nextStatePlain.getPotionItemStack();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This potion will have no effects.");
        ItemStackUtil.setLocalizedName(plainPotion, nextStatePlain.encodeToString());
        ItemStackUtil.setDisplayName(plainPotion, chatColor + "NO EFFECTS");
        ItemStackUtil.addLore(plainPotion, lore);
        allPotions.add(plainPotion);

        // Effects
        List<PotionEffectType> effectTypeList = Arrays.asList(PotionEffectType.values());
        effectTypeList.sort(Comparator.comparing(e -> StringUtil.toCommonName(e.getName()))); // sort list by common name
        for (PotionEffectType effectType : effectTypeList) {
            String commonName = StringUtil.toCommonName(effectType.getName());

            State nextState = this.clone();
            nextState.setInputEffectType(effectType.getName());

            lore = new ArrayList<>();
            lore.add("");

            if (this.hasPotionEffect(effectType)) {
                nextState.setAction(new SelectEffectTypeAction());
                lore.add(ChatColor.GOLD + "Left click to modify " + commonName + ".");
                lore.add(ChatColor.RED + "Right click to remove " + commonName + ".");
            } else {
                nextState.setAction(new AddEffectTypeAction());
                lore.add(ChatColor.GREEN + "Click to add " + commonName + ".");
            }

            int maxAmp = PotionUtil.maxAmp(effectType.getName()) + 1;
            if (maxAmp == 1) {
                lore.add(ChatColor.GOLD + "It has potency 1.");
            } else {
                lore.add(ChatColor.GOLD + "Its potency ranges from 1 to " + maxAmp + ".");
            }

            ItemStack potion = nextState.getPotionItemStack();
            ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
            ItemStackUtil.setDisplayName(potion, chatColor + commonName);
            ItemStackUtil.addLore(potion, lore);

            allPotions.add(potion);
        }
        return allPotions;
    }
}
