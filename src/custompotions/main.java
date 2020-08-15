package custompotions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.logging.Logger;

public class main extends JavaPlugin implements Listener {
    private Logger log;

    // STARTUP
    @java.lang.Override
    public void onEnable() {
        this.log = this.getLogger();
        this.log.info("Initialising CustomPotions and validating potions.");
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    // STOP
    @java.lang.Override
    public void onDisable() {
        this.log.info("CustomPotions has been disabled.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void insertBrewingItem(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getInventory().getType() != InventoryType.BREWING) {
            return;
        }


        boolean cancel = false;
        this.log.info(event.getAction().name());
        switch (event.getAction()) {
            case MOVE_TO_OTHER_INVENTORY: {
                ItemStack insertIngredient = event.getCurrentItem();
                if (insertIngredient == null) {
                    return;
                }
                if (insertIngredient.getType() == Material.POTION || insertIngredient.getType() == Material.SPLASH_POTION || insertIngredient.getType() == Material.LINGERING_POTION) {
                    return;
                }
                if (event.getClickedInventory().getType() == InventoryType.BREWING) {
                    return;
                }
                BrewerInventory brewingStand = (BrewerInventory) event.getInventory();
                ItemStack existingIngredient = brewingStand.getIngredient();
                // TODO for each ingredient in the hashmap of custom recipes, if item matches any, add item to the brewing stand

                if (existingIngredient == null) {
                    // Ingredient slot is empty, insert item stack directly.
                    brewingStand.setIngredient(insertIngredient);
                    event.getClickedInventory().clear(event.getSlot());
                    cancel = true;
                } else if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    // Ingredient slot matches, insert item stack until ingredient slot full, or source empty.
                    while (brewingStand.getIngredient().getAmount() < brewingStand.getIngredient().getMaxStackSize() && insertIngredient.getAmount() > 0) {
                        existingIngredient.setAmount(existingIngredient.getAmount() + 1);
                        brewingStand.setIngredient(existingIngredient);
                        insertIngredient.setAmount(insertIngredient.getAmount() - 1);

                        // Prevents moving remaining ingredients to the other side (main to hotbar, or hotbar to main).
                        cancel = true;
                    }
                }
                break;
            }
            case PICKUP_ALL:
                if (!event.getClick().equals(ClickType.LEFT)) {
                    break;
                }
            case PLACE_ALL: {
                ItemStack insertIngredient = event.getCursor();
                if (insertIngredient == null) {
                    return;
                }
                if (insertIngredient.getType() == Material.POTION || insertIngredient.getType() == Material.SPLASH_POTION || insertIngredient.getType() == Material.LINGERING_POTION) {
                    return;
                }
                if (event.getClickedInventory().getType() != InventoryType.BREWING) {
                    return;
                }
                if (event.getSlotType() != InventoryType.SlotType.FUEL) {
                    return;
                }

                BrewerInventory brewingStand = (BrewerInventory) event.getInventory();
                ItemStack existingIngredient = brewingStand.getIngredient();
                // TODO for each ingredient in the hashmap of custom recipes, if item matches any, add item to the brewing stand

                if (existingIngredient == null) {
                    // Ingredient slot is empty, insert item stack directly.
                    brewingStand.setIngredient(insertIngredient);

                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            event.setCursor(new ItemStack(Material.AIR));
                        }
                    }, 1L);
                } else if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    // Ingredient slot matches, insert item stack until ingredient slot full, or source empty.
                    int resultantStackSize = insertIngredient.getAmount() + existingIngredient.getAmount();
                    if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                        // Combined stack can be inserted in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(resultantStackSize);

                        // Insert ingredient and set cursor to nothing.
                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.setCursor(new ItemStack(Material.AIR));
                            }
                        }, 1L);
                    } else {
                        // Combined stack is too large to be placed in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(existingIngredient.getMaxStackSize());

                        // Insert ingredient and set cursor to resultant stack size.
                        ItemStack toCursor = insertIngredient.clone();
                        toCursor.setAmount(resultantStackSize - existingIngredient.getMaxStackSize());
                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.setCursor(toCursor);
                            }
                        }, 1L);
                    }
                } else {
                    // Ingredient slot does not match, swap cursor with existing ingredient.
                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            brewingStand.setIngredient(insertIngredient);
                            event.setCursor(existingIngredient);
                        }
                    }, 1L);
                }
                cancel = true;
                break;
            }
            case NOTHING: {
                // All cases where an item would be inserted into the ingredient slot but Minecraft does not register
                // it as a valid item, therefore returning a "NOTHING" action type.
                ItemStack insertIngredient = event.getCursor();
                if (insertIngredient == null) {
                    return;
                }
                if (insertIngredient.getType() == Material.POTION || insertIngredient.getType() == Material.SPLASH_POTION || insertIngredient.getType() == Material.LINGERING_POTION) {
                    return;
                }
                if (event.getClickedInventory().getType() != InventoryType.BREWING) {
                    return;
                }
                if (event.getSlotType() != InventoryType.SlotType.FUEL) {
                    return;
                }

                BrewerInventory brewingStand = (BrewerInventory) event.getInventory();
                ItemStack existingIngredient = brewingStand.getIngredient();
                // TODO for each ingredient in the hashmap of custom recipes, if item matches any, add item to the brewing stand
                if (existingIngredient == null) {
                    return;
                }

                if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    if (event.getClick().equals(ClickType.LEFT)) {
                        int resultantStackSize = insertIngredient.getAmount() + existingIngredient.getAmount();

                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(existingIngredient.getMaxStackSize());

                        // Insert ingredient and set cursor to resultant stack size.
                        ItemStack toCursor = insertIngredient.clone();
                        toCursor.setAmount(resultantStackSize - existingIngredient.getMaxStackSize());

                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.setCursor(toCursor);
                            }
                        }, 1L);
                    } else if (event.getClick().equals(ClickType.RIGHT)) {
                        int resultantStackSize = existingIngredient.getAmount() + 1;
                        if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                            ItemStack toInsert = insertIngredient.clone();
                            toInsert.setAmount(resultantStackSize);

                            // Insert ingredient and set cursor to one less.
                            ItemStack ingredientMinusOne = insertIngredient.clone();
                            ingredientMinusOne.setAmount(insertIngredient.getAmount() - 1);

                            BukkitScheduler scheduler = getServer().getScheduler();
                            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                                @SuppressWarnings("deprecation")
                                @Override
                                public void run() {
                                    brewingStand.setIngredient(toInsert);
                                    event.setCursor(ingredientMinusOne);
                                }
                            }, 1L);
                        }
                    }
                } else {
                    // Swap cursor with existing ingredient.
                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            brewingStand.setIngredient(insertIngredient);
                            event.setCursor(existingIngredient);
                        }
                    }, 1L);
                }
                cancel = true;
            }
        }

        switch (event.getAction()) {
            case PICKUP_ALL:
                if (!event.getClick().equals(ClickType.RIGHT)) {
                    break;
                }
            case PLACE_ONE:
                ItemStack insertIngredient = event.getCursor();
                if (insertIngredient == null) {
                    return;
                }
                if (insertIngredient.getType() == Material.POTION || insertIngredient.getType() == Material.SPLASH_POTION || insertIngredient.getType() == Material.LINGERING_POTION) {
                    return;
                }
                if (event.getClickedInventory().getType() != InventoryType.BREWING) {
                    return;
                }
                if (event.getSlotType() != InventoryType.SlotType.FUEL) {
                    return;
                }

                BrewerInventory brewingStand = (BrewerInventory) event.getInventory();
                ItemStack existingIngredient = brewingStand.getIngredient();
                // TODO for each ingredient in the hashmap of custom recipes, if item matches any, add item to the brewing stand

                if (existingIngredient == null) {
                    // Ingredient slot is empty, insert single item.
                    ItemStack oneIngredient = insertIngredient.clone();
                    oneIngredient.setAmount(1);
                    brewingStand.setIngredient(oneIngredient);

                    ItemStack ingredientMinusOne = insertIngredient.clone();
                    ingredientMinusOne.setAmount(insertIngredient.getAmount() - 1);

                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            event.setCursor(ingredientMinusOne);
                        }
                    }, 1L);
                } else if (existingIngredient.getType().equals(insertIngredient.getType())) {
                    // Ingredient slot matches, insert single item.
                    int resultantStackSize = existingIngredient.getAmount() + 1;
                    if (resultantStackSize <= existingIngredient.getMaxStackSize()) {
                        // Combined stack can be inserted in the ingredients slot.
                        ItemStack toInsert = insertIngredient.clone();
                        toInsert.setAmount(resultantStackSize);

                        // Insert ingredient and set cursor to one less.
                        ItemStack ingredientMinusOne = insertIngredient.clone();
                        ingredientMinusOne.setAmount(insertIngredient.getAmount() - 1);

                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                brewingStand.setIngredient(toInsert);
                                event.setCursor(ingredientMinusOne);
                            }
                        }, 1L);
                    }
                }
                cancel = true;
                break;
        }

        // Allows moving ingredients to other side when brewing stand is full of ingredients.
        if (cancel) {
            event.setCancelled(true);
        }

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                ((Player)event.getWhoClicked()).updateInventory();
            }
        }, 2L);
        /*if (event.getClick().isLeftClick() && event.getClick().isShiftClick()) {
            ItemStack inBrewingStand = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType() == Material.AIR) {
                return;
            } else if (inBrewingStand == null) {
                event.getClickedInventory().setItem(event.getSlot(), cursor);
            } else if (cursor.isSimilar(inBrewingStand)) {
                ItemStack newBrewingStand = inBrewingStand.clone();
                newBrewingStand.setAmount(Math.max(inBrewingStand.getAmount() + cursor.getAmount(), inBrewingStand.getMaxStackSize()));
                event.getClickedInventory().setItem(event.getSlot(), newBrewingStand);

                ItemStack newCursor = cursor.clone();
                newCursor.setAmount();
                event.setCursor(newCursor);

            }
        }*/

    }

}


