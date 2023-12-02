package xyz.iiinitiationnn.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Actions.ExitUnsavedAction;
import xyz.iiinitiationnn.custompotions.Actions.ModifyPotionAction;
import xyz.iiinitiationnn.custompotions.Actions.SelectPotionAction;
import xyz.iiinitiationnn.custompotions.Actions.StartupAction;
import xyz.iiinitiationnn.custompotions.Input;
import xyz.iiinitiationnn.custompotions.Potion;
import xyz.iiinitiationnn.custompotions.inventorytypes.ChestInventory;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.MagicNumber;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMenu extends State {
    private final static int pageSize = 51;

    public MainMenu() {
        super(null, new Potion(true), new StartupAction(),
            new ChestInventory(ChatColor.GOLD + "Select a Potion to Modify"), new Input());
    }

    @Override
    protected boolean needsNextPage() {
        return this.determineIfNextPage(pageSize, PotionUtil.getCustomPotions().size() + 1);
    }

    /**
     * Returns a button which exits the user from their menu, using a barrier block.
     */
    protected ItemStack exitButton() {
        ItemStack button = new ItemStack(Material.BARRIER);

        State nextState = this.clone();
        nextState.setAction(new ExitUnsavedAction());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Click to exit.");

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());
        ItemStackUtil.setDisplayName(button, ChatColor.RED + "EXIT");
        ItemStackUtil.addLore(button,lore);

        return button;
    }

    /**
     * Returns a potion indicating a new potion is to be created.
     */
    private ItemStack getNewPotion() {
        State nextState = this.clone();
        nextState.setAction(new ModifyPotionAction());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Create a new custom potion from scratch.");

        ItemStack potion = nextState.getPotionItemStack();
        ItemStackUtil.setLocalizedName(potion, nextState.encodeToString());
        ItemStackUtil.addLore(potion, lore);

        return potion;
    }

    @Override
    public Map<Integer, ItemStack> calculateButtons() {
        Map<Integer, ItemStack> buttons = new HashMap<>();
        buttons.put(MagicNumber.PREVIOUS_PAGE_SLOT, this.previousPageButton());
        buttons.put(MagicNumber.NEXT_PAGE_SLOT, this.nextPageButton());
        buttons.put(MagicNumber.EXIT_SLOT, this.exitButton());
        return buttons;
    }

    /**
     * Fetches the potions for the main menu including, if applicable:
     * a (random) new potion, and all existing potions.
     */
    @Override
    public List<ItemStack> calculateInventoryItems() {
        State nextStateTemplate = this.clone();
        nextStateTemplate.setAction(new SelectPotionAction());

        List<ItemStack> allPotions = new ArrayList<>();

        int i = 0;
        if (this.pageNum == 0) {
            allPotions.add(getNewPotion());
            i++;
        }

        List<Potion> customPotions = PotionUtil.getCustomPotions();

        for (Potion customPotion : customPotions) {
            if (this.isWithinBounds(i, pageSize)) {
                State nextState = nextStateTemplate.clone();
                nextState.setPotion(customPotion);
                ItemStack customPotionItem = customPotion.toItemStack();

                ItemStackUtil.setLocalizedName(customPotionItem, nextState.encodeToString());
                PotionUtil.addLoreRecipes(customPotionItem, customPotion);

                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GREEN + "Shift click to clone " + customPotion.getName() + ChatColor.GREEN + ".");
                lore.add(ChatColor.GOLD + "Left click to modify " + customPotion.getName() + ChatColor.GOLD + ".");
                lore.add(ChatColor.RED + "Right click to remove " + customPotion.getName() + ChatColor.RED + ".");
                ItemStackUtil.addLore(customPotionItem, lore);

                allPotions.add(customPotionItem);
            }
            i++;
        }
        return allPotions;
    }
}
