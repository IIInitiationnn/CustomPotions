package xyz.iiinitiationnn.custompotions.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Main;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

public class BrewingStandListener implements Listener {

    private void updateInventory(InventoryClickEvent event) {
        ((Player) event.getWhoClicked()).updateInventory();
    }

    private void clearCursor(InventoryClickEvent event) {
        event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
    }

    private void setCursor(InventoryClickEvent event, ItemStack item) {
        event.getWhoClicked().setItemOnCursor(item);
    }

    private void moveToOtherInventory(InventoryClickEvent event) {
        BrewerInventory brewerInventory = (BrewerInventory) event.getInventory();
        ItemStack ingredient = brewerInventory.getIngredient();
        ItemStack slotItem = event.getCurrentItem();

        int maxStackSize = slotItem.getMaxStackSize();
        int ingredientStackSize = ingredient.getAmount();
        int itemStackSize = slotItem.getAmount();
        int newIngredientStackSize = Math.min(maxStackSize, ingredientStackSize + itemStackSize);
        int newItemStackSize = Math.max(0, itemStackSize - (maxStackSize - ingredientStackSize));

        ingredient.setAmount(newIngredientStackSize);
        brewerInventory.setIngredient(ingredient);
        slotItem.setAmount(newItemStackSize);
        updateInventory(event);
    }

    private void placeAll(InventoryClickEvent event) {
        BrewerInventory brewerInventory = (BrewerInventory) event.getInventory();
        ItemStack ingredient = brewerInventory.getIngredient();
        ItemStack cursorItem = event.getCursor();

        int maxStackSize = cursorItem.getMaxStackSize();
        int ingredientStackSize = ingredient.getAmount();
        int itemStackSize = cursorItem.getAmount();
        int newIngredientStackSize = Math.min(maxStackSize, ingredientStackSize + itemStackSize);
        int newItemStackSize = Math.max(0, itemStackSize - (maxStackSize - ingredientStackSize));

        ingredient.setAmount(newIngredientStackSize);
        brewerInventory.setIngredient(ingredient);
        cursorItem.setAmount(newItemStackSize);
        setCursor(event, cursorItem);
    }

    private void placeOne(InventoryClickEvent event) {
        BrewerInventory brewerInventory = (BrewerInventory) event.getInventory();
        ItemStack ingredient = brewerInventory.getIngredient();
        ItemStack cursorItem = event.getCursor();

        int maxStackSize = cursorItem.getMaxStackSize();
        int ingredientStackSize = ingredient.getAmount();
        int itemStackSize = cursorItem.getAmount();
        int newIngredientStackSize = Math.min(maxStackSize, ingredientStackSize + 1);
        int newItemStackSize = Math.max(0, itemStackSize - (newIngredientStackSize - ingredientStackSize));

        ingredient.setAmount(newIngredientStackSize);
        brewerInventory.setIngredient(ingredient);
        cursorItem.setAmount(newItemStackSize);
        setCursor(event, cursorItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBrewingStandInteract(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = event.getInventory();
        if (clickedInventory == null || inventory.getType() != InventoryType.BREWING) return;
        BrewerInventory brewerInventory = (BrewerInventory) inventory;

        InventoryAction inventoryAction = event.getAction();
        ItemStack slotItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        Main.log.info(inventoryAction.toString());
        if (slotItem != null) Main.log.info("Slot item: " + slotItem.toString());
        if (cursorItem != null) Main.log.info("Cursor item: " + cursorItem.toString());

        // Player inventory was clicked
        if (clickedInventory.getType() == InventoryType.PLAYER) {
            // Shift click from player inventory
            if (inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (slotItem == null || PotionUtil.isPotion(slotItem)) return;
                ItemStack ingredient = brewerInventory.getIngredient();

                // Inserting blaze powder
                if (slotItem.getType() == Material.BLAZE_POWDER) {
                    // Insert into fuel slot
                    if (ingredient == null || ingredient.getType() != Material.BLAZE_POWDER ||
                        ingredient.getAmount() == ingredient.getMaxStackSize()) {
                        return;
                    }
                    // Insert into ingredient slot
                    else {
                        moveToOtherInventory(event);
                        event.setCancelled(true);
                    }
                }

                // Empty ingredient slot, insert item
                if (ingredient == null) {
                    brewerInventory.setIngredient(slotItem);
                    clickedInventory.clear(event.getSlot());
                    updateInventory(event);
                    event.setCancelled(true); // prevents duplication of ingredients in hotbar / main
                }
                // Ingredient slot matches: insert item stack until ingredient slot full, or source empty
                else if (ingredient.getType() == slotItem.getType()) {
                    int ingredientStackSize = ingredient.getAmount();
                    int newIngredientStackSize = Math.min(slotItem.getMaxStackSize(), ingredientStackSize + slotItem.getAmount());
                    moveToOtherInventory(event);

                    // Prevents moving remaining ingredients between hotbar <-> main
                    if (ingredientStackSize != newIngredientStackSize) event.setCancelled(true);
                }
            }
        }
        // Brewing inventory was clicked
        else {
            // Not the ingredient slot of the brewing stand
            if (event.getSlotType() != InventoryType.SlotType.FUEL) return;

            // Placing stack in ingredient slot
            // Pickup all & left click -> X in ingredient slot, clicking on ingredient slot with X in cursor
            if (inventoryAction == InventoryAction.PLACE_ALL
                || inventoryAction == InventoryAction.PICKUP_ALL && event.isLeftClick()) {
                if (cursorItem == null || PotionUtil.isPotion(cursorItem)) return;
                ItemStack ingredient = brewerInventory.getIngredient();

                // Empty ingredient slot, insert item
                if (ingredient == null) {
                    brewerInventory.setIngredient(cursorItem);
                    clearCursor(event);
                    event.setCancelled(true);
                }
                // Ingredient slot matches: insert item stack until ingredient slot full, or source empty
                else if (ingredient.getType() == cursorItem.getType()) {
                    placeAll(event);
                    event.setCancelled(true);
                }
            }
            // Placing one of stack in ingredient slot
            // Pickup all & right click -> X in ingredient slot, clicking on ingredient slot with X in cursor
            else if (inventoryAction == InventoryAction.PLACE_ONE
                || event.isRightClick() && inventoryAction == InventoryAction.PICKUP_ALL) {
                if (cursorItem == null || PotionUtil.isPotion(cursorItem)) return;
                ItemStack ingredient = brewerInventory.getIngredient();

                // Empty ingredient slot, insert an item
                if (ingredient == null) {
                    ItemStack single = cursorItem.clone();
                    single.setAmount(1);
                    brewerInventory.setIngredient(single);
                    cursorItem.setAmount(cursorItem.getAmount() - 1);
                    setCursor(event, cursorItem);
                    event.setCancelled(true);
                }
                // Ingredient slot matches: insert single item
                else if (ingredient.getType() == cursorItem.getType()) {
                    placeOne(event);
                    event.setCancelled(true);
                }
            }
            // All cases where an item would be inserted into the ingredient slot but Minecraft does not register
            // it as a valid item, therefore returning a "NOTHING" action type.
            else if (inventoryAction == InventoryAction.NOTHING) {
                if (cursorItem == null || PotionUtil.isPotion(cursorItem)) return;
                ItemStack ingredient = brewerInventory.getIngredient();
                if (ingredient == null) return;

                // Ingredient slot matches
                if (ingredient.getType() == cursorItem.getType()) {
                    if (event.isLeftClick()) {
                        placeAll(event);
                        event.setCancelled(true);
                    } else if (event.isRightClick()) {
                        placeOne(event);
                        event.setCancelled(true);
                    }
                }
                // Swap cursor with existing ingredient.
                else {
                    brewerInventory.setIngredient(cursorItem);
                    setCursor(event, ingredient);
                }
            }
        }
    }
}
