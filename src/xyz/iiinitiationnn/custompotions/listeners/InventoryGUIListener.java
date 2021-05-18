package xyz.iiinitiationnn.custompotions.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import xyz.iiinitiationnn.custompotions.*;
import org.bukkit.event.Listener;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;

import java.util.List;

public class InventoryGUIListener implements Listener {
    int EXISTING_POTION_SLOT = 53;
    // action: exit, skipL, skipR, pageNext, pagePrevious, pageInvalid, createPotion, selectPotion, selectType,
    //         selectColour, addEffectType, selectEffectType, enterEffectDuration, enterEffectAmplifier,
    //         addRecipeIngredient, selectRecipeIngredient, addRecipeBase, removeRecipeBase, recipeBaseInvalid,
    //         enterName, finalInvalid, finalEdit, finalConfirm, give

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        ItemStack interaction = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (inv == null || interaction == null || interaction.getItemMeta() == null) return;
        String localizedName = ItemStackUtil.getLocalizedName(interaction);
        if (!LocalizedName.isCustomPotionsClick(localizedName)) return;
        // TODO prevent placing in the custom inventories, including dragging event
        //  is this covered by interaction == null? (for the placing part)

        event.setCancelled(true);

        LocalizedName state = new LocalizedName(localizedName);
        LocalizedName nextState = state.clone();
        InventoryGUI next;
        switch (state.getAction()) {
            case "exit":
                // TODO
                break;
            case "skipL":
                nextState.previousMenu();
                next = new InventoryGUI(nextState, new PotionObject(inv.getItem(EXISTING_POTION_SLOT)));
                next.openInv(player);
                break;
            case "skipR":
                nextState.nextMenu();
                next = new InventoryGUI(nextState, new PotionObject(inv.getItem(EXISTING_POTION_SLOT)));
                next.openInv(player);
                break;
            case "pageNext":
                nextState.setPage(state.getPage() + 1);
                next = new InventoryGUI(nextState, new PotionObject(inv.getItem(EXISTING_POTION_SLOT)));
                next.openInv(player);
                break;
            case "pagePrevious":
                nextState.setPage(state.getPage() - 1);
                next = new InventoryGUI(nextState, new PotionObject(inv.getItem(EXISTING_POTION_SLOT)));
                next.openInv(player);
                break;
            case "pageInvalid":
                break;
            case "createPotion":
                nextState.nextMenu();
                next = new InventoryGUI(nextState, new PotionObject(event.getCurrentItem()));
                next.openInv(player);
                break;
            case "selectPotion":
                switch (event.getClick()) {
                    case LEFT: // modify
                        nextState.nextMenu();
                        new PotionObject(event.getCurrentItem()).debugCustomPotion();
                        next = new InventoryGUI(nextState, new PotionObject(event.getCurrentItem()));
                        next.openInv(player);
                    case RIGHT: // remove
                    case SHIFT_LEFT: // clone
                }
                break;
            case "selectType":
            case "selectColour":
                nextState.nextMenu();
                next = new InventoryGUI(nextState, new PotionObject(event.getCurrentItem()));
                next.openInv(player);
                break;
            case "addEffectType":
                nextState.nextMenu();
                nextState.setExtraField(event.getCurrentItem().getType().name());
                next = new InventoryGUI(nextState, new PotionObject(event.getCurrentItem()));
                next.openInv(player);
                break;
        }

    }
}
    /*
    // Interacting with plugin's GUI.
    @EventHandler
    public void clickGUI(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        ItemStack clicked = event.getCurrentItem();

        String localizedName = clicked.getItemMeta().getLocalizedName();

        event.setCancelled(true);

        if (getClick(localizedName).equals("give")) {
            if (isPotion(clicked.getType())) {
                if (event.getClick() != ClickType.SHIFT_LEFT) {
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "You have withdrawn 1x " + clicked.getItemMeta().getDisplayName() + ChatColor.GREEN + ".");
                    event.getWhoClicked().getInventory().addItem(ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked)));
                } else {
                    int maxStackSize = clicked.getMaxStackSize();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "You have withdrawn " + maxStackSize + "x " + clicked.getItemMeta().getDisplayName() + ChatColor.GREEN + ".");
                    ItemStack clickedMax = clicked.clone();
                    clickedMax.setAmount(maxStackSize);
                    event.getWhoClicked().getInventory().addItem(ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clickedMax)));
                }
            }
            givePotions((Player) event.getWhoClicked(), clicked);
            return;
        }

        if (getClick(localizedName).equals("give_exit")) {
            event.getWhoClicked().closeInventory();
        }

        boolean isPotion = isPotion(clicked.getType());
        boolean isLingeringPotion = clicked.getType() == Material.LINGERING_POTION;

        if (inventory == inv && inventory != null) { // 54-slot inventory.
            // Invalid attempt to change page (red glass).
            if (getClick(localizedName).equalsIgnoreCase("invalid_page")) {
                return;
            }

            // Leave via slot 53 exit.
            if (getClick(localizedName).equals("exit")) {
                if (clicked.getType() == Material.BARRIER || !isCompletePotion(localizedName) || event.getClick() == ClickType.RIGHT) {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                    resetGlobals();
                } else if (event.getClick() == ClickType.LEFT) {
                    String newName = potionNameInput;
                    PotionMeta newMeta = ((PotionMeta) clicked.getItemMeta());
                    Color newColor = newMeta.getColor();
                    if (newColor == null) {
                        this.log.severe("Unknown error X37.");
                        return;
                    }
                    boolean isLingering = clicked.getType() == Material.LINGERING_POTION;

                    // Remove old information from data file.
                    if (potionFromMemory != null && potionFromMemory.getItemMeta() != null) {
                        allCustomPotions.remove(matchPotionInfo(potionFromMemory, allCustomPotions));
                        reinitialiseAllPotionRecipes();
                        potionData.getData().set(potionFromMemory.getItemMeta().getDisplayName(), null);
                    }

                    // Type + Colour.
                    potionData.getData().set(newName + ".type", clicked.getType().toString().toUpperCase());
                    potionData.getData().set(newName + ".colour.name", ColourUtil.colourNameFromColor(newColor));
                    potionData.getData().set(newName + ".colour.red", newColor.getRed());
                    potionData.getData().set(newName + ".colour.green", newColor.getGreen());
                    potionData.getData().set(newName + ".colour.blue", newColor.getBlue());

                    // Effects.
                    if (newMeta.hasCustomEffects()) {
                        List<PotionEffect> newPotionEffects = newMeta.getCustomEffects();
                        for (PotionEffect newPotionEffect : newPotionEffects) {
                            String s = newName + ".effects." + newPotionEffect.getType().getName();
                            potionData.getData().set(s + ".duration",
                                    isLingering ? newPotionEffect.getDuration() / 80 : newPotionEffect.getDuration() / 20);
                            potionData.getData().set(s + ".amplifier", newPotionEffect.getAmplifier());
                        }
                    } else {
                        potionData.getData().set(newName + ".effects", "none");
                    }

                    // Fix all potions which use this potion as a predecessor.
                    if (potionFromMemory != null) {
                        for (PotionInfo customPotion : allCustomPotions) {
                            for (PotionRecipe recipe : customPotion.potionrecipes) {
                                if (potionsAreSame(potionFromMemory, recipe.predecessor)) {
                                    recipe.predecessor = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked));
                                    potionData.getData().set(customPotion.name + ".recipes." + recipe.index + ".predecessor",
                                            ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked)));
                                }
                            }
                        }
                    }

                    // Predecessors.
                    int i = 0;
                    for (PotionRecipe newPotionRecipe : potionRecipesInput) {
                        String s = newName + ".recipes." + i;
                        potionData.getData().set(s + ".ingredient", newPotionRecipe.ingredient.name());
                        potionData.getData().set(s + ".predecessor", newPotionRecipe.predecessor);
                        i++;
                    }

                    potionData.saveData();
                    allCustomPotions.add(newPotionInfo(potionData.getData(), newName));

                    //printDebug(localizedName);
                    reinitialiseAllPotionRecipes();
                    resetGlobals();

                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Your changes to " + newName + ChatColor.GREEN + " have been saved.");
                    return;
                }
                return;
            }

            switch (getClick(localizedName)) {
                // Select -> Type via selecting a potion. Set potionNameInput to this name, and whether or not it is from memory.
                case "selected": {
                    potionFromMemory = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked));
                    for (PotionInfo customPotion : allCustomPotions) {
                        if (customPotion.itemstack.isSimilar(ItemStackUtil.resetLocalizedName(ItemStackUtil.resetLore(clicked)))) {
                            this.potionRecipesInput.addAll(customPotion.potionrecipes);
                            break;
                        }
                    }
                    // fall through
                } case "new_selected": {
                    this.potionNameInput = clicked.getItemMeta().getDisplayName();
                    PotionMeta meta = (PotionMeta) clicked.getItemMeta();
                    if (meta == null) {
                        this.log.severe("Unknown error X21.");
                        return;
                    }
                    meta.setLocalizedName(setClick(localizedName, "type"));
                    clicked.setItemMeta(meta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

                // Type -> Select via previous menu.
                case "previous_menu_select": {
                    resetGlobals();
                    playerModifyPotions((Player) event.getWhoClicked(), null,
                            ItemStackUtil.setLocalizedName(clicked, setClick(localizedName, "select")));
                    return;
                }

                // Effect Type -> Effect Duration via selecting an effect. Set effectTypeInput to this effect.
                case "effect_dur": {
                    effectTypeInput = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

                // Effect Type -> Effect Type / Effect Duration via selecting an effect.
                case "effect_type_mixed": {
                    if (event.getClick() == ClickType.LEFT) { // Modify.
                        effectTypeInput = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                        clicked = ItemStackUtil.setLocalizedName(clicked, setClick(localizedName, "effect_dur"));
                        playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    } else if (event.getClick() == ClickType.RIGHT) { // Remove
                        PotionMeta removeMeta = ((PotionMeta) clicked.getItemMeta());
                        PotionEffectType type = PotionEffectType.getByName(ChatColor.stripColor(removeMeta.getDisplayName()));
                        if (type == null) {
                            this.log.severe("Unknown error X34.");
                            return;
                        }
                        removeMeta.removeCustomEffect(type);
                        removeMeta.setLocalizedName(setClick(localizedName, "effect_type"));
                        clicked.setItemMeta(removeMeta);
                        playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    }
                    return;
                }

                // Effect Type -> Ingredient via selecting no effects.
                case "no_effects": {
                    PotionMeta removeMeta = ((PotionMeta) clicked.getItemMeta());
                    removeMeta.clearCustomEffects();
                    removeMeta.setLocalizedName(setClick(localizedName, "ingredient"));
                    clicked.setItemMeta(removeMeta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

                // Ingredient -> Ingredient / Predecessor via selecting an ingredient.
                case "ingredient_mixed": {
                    if (event.getClick() == ClickType.LEFT) { // Modify.
                        ingredientInput = clicked.getType();
                        clicked = ItemStackUtil.setLocalizedName(clicked, setClick(localizedName, "predecessor"));
                        playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    } else if (event.getClick() == ClickType.RIGHT) { // Remove.
                        Material clickedType = clicked.getType();
                        potionRecipesInput.removeIf(recipe -> recipe.ingredient == clickedType);
                        String newLocalized = setRecipe(localizedName, potionRecipesInput.size() > 0);
                        clicked = ItemStackUtil.setLocalizedName(clicked, setClick(newLocalized, "ingredient"));
                        playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    }
                    return;
                }

                // Ingredient -> Predecessor via selecting an ingredient.
                case "ingredient_chosen": {
                    ingredientInput = clicked.getType();
                    clicked = ItemStackUtil.setLocalizedName(clicked, setClick(localizedName, "predecessor"));
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Predecessor -> Ingredient via previous menu.
                case "previous_menu_ingredient": {
                    ingredientInput = null;
                    String newLocalized = setRecipe(localizedName, potionRecipesInput.size() > 0);
                    clicked = ItemStackUtil.setLocalizedName(clicked, setClick(newLocalized, "ingredient"));
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Predecessor -> Predecessor via trying to select an invalid recipe.
                case "predecessor_invalid": {
                    return;
                }

                // Predecessor -> Ingredient via selecting a predecessor.
                case "predecessor_chosen": {
                    clicked = ItemStackUtil.setLocalizedName(clicked, setClick(setRecipe(localizedName, true), "ingredient"));
                    ItemStack chosenPredecessor = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked.clone()));
                    potionRecipesInput.add(new PotionRecipe(null, ItemStackUtil.resetLocalizedName(ItemStackUtil.resetLore(chosenPredecessor)), ingredientInput, -1));
                    ingredientInput = null;
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Predecessor -> Predecessor via removing a predecessor.
                case "predecessor_remove": {
                    ItemStack chosenPredecessor = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked.clone()));
                    potionRecipesInput.removeIf(recipe -> recipe.ingredient == ingredientInput &&
                            potionsAreSame(recipe.predecessor, chosenPredecessor));
                    allPotionRecipes.removeIf(recipe ->
                            recipe.ingredient == ingredientInput && potionsAreSame(recipe.predecessor, chosenPredecessor));
                    String newLocalized = setRecipe(localizedName, potionRecipesInput.size() > 0);
                    clicked = ItemStackUtil.setLocalizedName(clicked, setClick(newLocalized, "ingredient"));
                    playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
                    return;
                }

                // Saving completed potion to potions.yml.
                case "complete": {
                    String newName = potionNameInput;
                    PotionMeta newMeta = ((PotionMeta) clicked.getItemMeta());
                    Color newColor = newMeta.getColor();
                    if (newColor == null) {
                        this.log.severe("Unknown error X37.");
                        return;
                    }
                    boolean isLingering = clicked.getType() == Material.LINGERING_POTION;

                    // Remove old information.
                    if (potionFromMemory != null && potionFromMemory.getItemMeta() != null) {
                        allCustomPotions.remove(matchPotionInfo(potionFromMemory, allCustomPotions));
                        reinitialiseAllPotionRecipes();
                        potionData.getData().set(potionFromMemory.getItemMeta().getDisplayName(), null);
                        // TODO TODO TODO why isnt it being removed sometimes
                    }

                    // Type + Colour.
                    potionData.getData().set(newName + ".type", clicked.getType().name().toUpperCase());
                    potionData.getData().set(newName + ".colour.name", ColourUtil.colourNameFromColor(newColor));
                    potionData.getData().set(newName + ".colour.red", newColor.getRed());
                    potionData.getData().set(newName + ".colour.green", newColor.getGreen());
                    potionData.getData().set(newName + ".colour.blue", newColor.getBlue());

                    // Effects.
                    if (newMeta.hasCustomEffects()) {
                        List<PotionEffect> newPotionEffects = newMeta.getCustomEffects();
                        for (PotionEffect newPotionEffect : newPotionEffects) {
                            String s = newName + ".effects." + newPotionEffect.getType().getName();
                            potionData.getData().set(s + ".duration",
                                    isLingering ? newPotionEffect.getDuration() / 80 : newPotionEffect.getDuration() / 20);
                            potionData.getData().set(s + ".amplifier", newPotionEffect.getAmplifier());
                        }
                    } else {
                        potionData.getData().set(newName + ".effects", "none");
                    }

                    // Fix all potions which use this potion as a predecessor.
                    if (potionFromMemory != null) {
                        for (PotionInfo customPotion : allCustomPotions) {
                            for (PotionRecipe recipe : customPotion.potionrecipes) {
                                if (potionsAreSame(potionFromMemory, recipe.predecessor)) {
                                    recipe.predecessor = ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked));
                                    potionData.getData().set(customPotion.name + ".recipes." + recipe.index + ".predecessor",
                                            ItemStackUtil.resetLore(ItemStackUtil.resetLocalizedName(clicked)));
                                }
                            }
                        }
                    }

                    // Predecessors.
                    int i = 0;
                    for (PotionRecipe newPotionRecipe : potionRecipesInput) {
                        String s = newName + ".recipes." + i;
                        potionData.getData().set(s + ".ingredient", newPotionRecipe.ingredient.name());
                        potionData.getData().set(s + ".predecessor", newPotionRecipe.predecessor);
                        i++;
                    }

                    potionData.saveData();
                    allCustomPotions.add(newPotionInfo(potionData.getData(), newName));

                    //printDebug(localizedName);
                    reinitialiseAllPotionRecipes();
                    resetGlobals();

                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Your changes to " + newName + ChatColor.GREEN + " have been saved.");
                    return;
                }
            }
        } else { // Anvil inventory.
            // Not the output slot. anvilInv remains unchanged.
            if (event.getSlot() != AnvilGUI.Slot.OUTPUT) {
                return;
            }

            // Right clicking the output slot (exit), or not left clicking (nothing happens).
            switch (getClick(localizedName)) {
                case "effect_amp":
                case "effect_type":
                case "final": {
                    if (event.getClick() == ClickType.RIGHT) {
                        anvilInv.closeInventory();
                        event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                        resetGlobals();
                        return;
                    } else if (event.getClick() != ClickType.LEFT) {
                        return;
                    }
                }
            }

            // Left clicking the output slot (continue).
            switch (getClick(localizedName)) {
                // Effect Duration -> Effect Amp via continue.
                case "effect_amp": {
                    effectDurationInput = clicked.getItemMeta().getDisplayName();
                    int dur;
                    try {
                        dur = Integer.parseInt(effectDurationInput);
                    } catch (Exception e) { // Not an integer.
                        effectDurationInput = null;
                        return;
                    }

                    //printDebug(localizedName);

                    int max = isLingeringPotion ? 26843545 : 107374182;
                    if (dur < 1 || dur > max) { // Not in range.
                        effectDurationInput = null;
                        return;
                    }

                    // effectDurationInput is valid.
                    if (PotionUtil.maxAmp(effectTypeInput) == 0) { // No need for inputting an amp.
                        PotionMeta newEffectMeta = ((PotionMeta) clicked.getItemMeta());
                        PotionEffectType newType = PotionEffectType.getByName(effectTypeInput);
                        if (newType == null) {
                            this.log.severe("Unknown error X17.");
                            return;
                        }
                        int newDur = isLingeringPotion ? Integer.parseInt(effectDurationInput) * 80 : Integer.parseInt(effectDurationInput) * 20;
                        newEffectMeta.addCustomEffect(new PotionEffect(newType, newDur, 0), true);
                        newEffectMeta.setLocalizedName(setClick(setEffect(localizedName, true), "effect_type"));
                        clicked.setItemMeta(newEffectMeta);
                        effectTypeInput = null;
                        effectDurationInput = null;
                    }
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

                // Effect Amp -> Effect Type via continue.
                case "effect_type": {
                    effectAmplifierInput = clicked.getItemMeta().getDisplayName();
                    int amp;
                    try {
                        amp = Integer.parseInt(effectAmplifierInput);
                    } catch (Exception e) { // Not an integer.
                        effectAmplifierInput = null;
                        return;
                    }

                    //printDebug(localizedName);

                    if (amp < 0 || amp > PotionUtil.maxAmp(effectTypeInput)) { // Not in range.
                        effectAmplifierInput = null;
                    } else {
                        PotionMeta newEffectMeta = ((PotionMeta) clicked.getItemMeta());
                        PotionEffectType newType = PotionEffectType.getByName(effectTypeInput);
                        if (newType == null) {
                            this.log.severe("Unknown error X18.");
                            return;
                        }
                        int newDur = isLingeringPotion ? Integer.parseInt(effectDurationInput) * 80 : Integer.parseInt(effectDurationInput) * 20;
                        int newAmp = Integer.parseInt(effectAmplifierInput);
                        newEffectMeta.addCustomEffect(new PotionEffect(newType, newDur, newAmp), true);
                        newEffectMeta.setLocalizedName(setEffect(localizedName, true));
                        clicked.setItemMeta(newEffectMeta);
                        effectTypeInput = null;
                        effectDurationInput = null;
                        effectAmplifierInput = null;
                        playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    }
                    return;
                }

                // Naming -> Final via continue.
                case "final": {
                    potionNameInput = ChatColor.translateAlternateColorCodes('&', clicked.getItemMeta().getDisplayName());

                    // TODO cant have same name

                    //printDebug(localizedName);

                    PotionMeta namedMeta = ((PotionMeta) clicked.getItemMeta());
                    namedMeta.setDisplayName(potionNameInput);
                    clicked.setItemMeta(namedMeta);
                    playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
                    return;
                }

            }
        }

        //printDebug(localizedName);

        if (inventory == null) {
            this.log.severe("Unknown error X41.");
            return;
        }

        if (isPotion) { // Pass the potion that was just clicked.
            playerModifyPotions((Player) event.getWhoClicked(), clicked, clicked);
        } else { // Pass the potion which will be in the bottom right hand corner.
            playerModifyPotions((Player) event.getWhoClicked(), inventory.getContents()[53], clicked);
        }
    }*/