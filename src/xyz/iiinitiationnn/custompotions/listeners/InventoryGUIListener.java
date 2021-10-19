package xyz.iiinitiationnn.custompotions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.iiinitiationnn.custompotions.Main;
import xyz.iiinitiationnn.custompotions.gui.GUI;
import xyz.iiinitiationnn.custompotions.states.State;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;

import java.io.IOException;

public class InventoryGUIListener implements Listener {
    // action: forceExit, exit, skipL, skipR, pageNext, pagePrevious, pageInvalid, createPotion, selectPotion, selectType,
    //         selectColour, noEffects, addEffectType, selectEffectType, enterEffectDuration, enterEffectAmplifier,
    //         addRecipeIngredient, selectRecipeIngredient, addRecipeBase, removeRecipeBase, recipeBaseInvalid,
    //         enterName, finalInvalid, finalEdit, finalConfirm, give

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        ItemStack interaction = event.getCurrentItem();

        if (inv == null || interaction == null || interaction.getItemMeta() == null || event.getCurrentItem() == null) {
            return;
        }
        State state;
        try {
            state = State.decodeFromString(ItemStackUtil.getLocalizedName(interaction));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            // Not a State class
            return;
        }
        Main.log.info("Action: " + state.getAction() + "; Menu: " + state.getClass().getSimpleName());
        // TODO prevent placing in the custom inventories, including dragging event
        //  is this covered by interaction == null? (for the placing part)

        event.setCancelled(true);
        new GUI(state, (Player) event.getWhoClicked())
            .updateEvent(event)
            .nextState()
            .open();


        // TODO move this into Action subclasses
        /*switch (state.getAction()) {
            case "forceExit":
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                return;
            case "exit":
                if (event.getClick() == ClickType.LEFT) {
                    // save and exit
                    Main.fileData.writeData(nextState.getPotion());
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Your changes to "
                        + state.getPotion().getName() + ChatColor.GREEN + " have been saved.");
                } else if (event.getClick() == ClickType.RIGHT) {
                    // exit without saving
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Your changes have not been saved.");
                }
                return;
            case "skipL":
                nextState.skipPreviousMenu();
                break;
            case "skipR":
                nextState.skipNextMenu();
                break;
            case "pageNext":
                nextState.setPageNum(state.getPageNum() + 1);
                break;
            case "pagePrevious":
                nextState.setPageNum(state.getPageNum() - 1);
                break;
            case "pageInvalid":
                return;
            case "createPotion":
            case "selectType":
            case "selectColour":
                nextState.nextMenu();
                nextState.setPageNum(0);
                break;
            case "selectPotion":
                if (event.getClick() == ClickType.LEFT) {
                    // modify
                    nextState.nextMenu();
                    *//*nextState.getPotion().debugCustomPotion(); // hhh*//*
                } else if (event.getClick() == ClickType.RIGHT) {
                    // remove TODO are you sure prompt
                } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                    // clone
                    nextState.nextMenu();
                    nextState.setPotion(nextState.getPotion().duplicate());
                    *//*state.getPotion().debugCustomPotion(); // hhh
                    nextState.getPotion().debugCustomPotion(); // hhh*//*
                } else {
                    return;
                }
                nextState.setPageNum(0);
                break;
            case "noEffects":
                nextState.skipNextMenu();
                nextState.setPageNum(0);
                nextState.getPotion().setEffects(new ArrayList<>());
                break;
            case "addEffectType":
                nextState.nextMenu();
                nextState.setPageNum(0);
                nextState.getInput().setEffectType(ItemStackUtil.getDisplayName(event.getCurrentItem()));
                break;
            case "selectEffectType":
                if (event.getClick() == ClickType.LEFT) {
                    // modify
                    nextState.nextMenu();
                    nextState.getInput().setEffectType(ItemStackUtil.getDisplayName(event.getCurrentItem()));
                    nextState.setPageNum(0);
                } else if (event.getClick() == ClickType.RIGHT) {
                    // remove
                    nextState.getPotion().removeEffectByName(ItemStackUtil.getDisplayName(event.getCurrentItem()));
                } else {
                    return;
                }
                break;
            case "enterEffectDuration":
                if (event.getSlot() == MagicNumber.anvilLeftInputSlot) {
                    // Skip
                    nextState.skipNextMenu();
                } else if (event.getSlot() == MagicNumber.anvilRightInputSlot) {
                    // Invalid
                     return;
                } else if (event.getSlot() == MagicNumber.anvilOutputSlot) {
                    // Continue
                    try {
                        int duration = Integer.parseInt(ItemStackUtil.getDisplayName(event.getCurrentItem()));
                        if (PotionUtil.isValidDuration(state.getPotion().isLingering(), duration)) {
                            nextState.nextMenu();
                            int durationTicks = PotionUtil.secondsToTicks(state.getPotion().isLingering(), duration);
                            nextState.getInput().setEffectDuration(durationTicks);

                            // Effect only has one possible amplifier (I), add new effect to the potion
                            if (PotionUtil.maxAmp(state.getInput().getEffectType()) == 0) {
                                nextState.nextMenu();
                                PotionEffectType type = PotionEffectType.getByName(state.getInput().getEffectType());
                                nextState.getPotion().addEffect(new PotionEffectSerializable(type, durationTicks, 0));
                                nextState.resetInput();
                            }

                        } else {
                            throw new Exception();
                        }
                    } catch (Exception ignored) {
                    }
                }
                nextState.setPageNum(0);
                break;
            case "enterEffectAmplifier":
                if (event.getSlot() == MagicNumber.anvilLeftInputSlot) {
                    // Skip
                    nextState.skipNextMenu();
                } else if (event.getSlot() == MagicNumber.anvilRightInputSlot) {
                    // Invalid
                     return;
                } else if (event.getSlot() == MagicNumber.anvilOutputSlot) {
                    // Continue
                    try {
                        int amplifier = Integer.parseInt(ItemStackUtil.getDisplayName(event.getCurrentItem()));
                        if (PotionUtil.isValidAmp(state.getInput().getEffectType(), amplifier)) {
                            nextState.nextMenu();

                            // Add the new effect to the potion
                            PotionEffectType type = PotionEffectType.getByName(state.getInput().getEffectType());
                            int duration = state.getInput().getEffectDuration();
                            nextState.getPotion().addEffect(new PotionEffectSerializable(type, duration, amplifier - 1));
                            nextState.resetInput();
                        } else {
                            throw new Exception();
                        }
                    } catch (Exception ignored) {
                    }
                }
                nextState.setPageNum(0);
                break;
            case "addRecipeIngredient":
                nextState.nextMenu();
                nextState.setPageNum(0);
                nextState.getInput().setIngredient(event.getCurrentItem().getType().name());
                break;
            case "selectRecipeIngredient":
                if (event.getClick() == ClickType.LEFT) {
                    // modify
                    nextState.nextMenu();
                    nextState.setPageNum(0);
                    nextState.getInput().setIngredient(event.getCurrentItem().getType().name());
                } else if (event.getClick() == ClickType.RIGHT) {
                    // remove TODO removeByIngredientName or watever
                } else {
                    return;
                }
                break;
            case "addRecipeBase":
                nextState.nextMenu();
                nextState.setPageNum(0);
                break;
            case "removeRecipeBase":
                break;
            case "recipeBaseInvalid":
                return;
            case "enterName":
                if (event.getSlot() == MagicNumber.anvilLeftInputSlot) {
                    // Skip
                    nextState.skipNextMenu();
                } else if (event.getSlot() == MagicNumber.anvilRightInputSlot) {
                    // Invalid
                     return;
                } else if (event.getSlot() == MagicNumber.anvilOutputSlot) {
                    // Continue
                    nextState.nextMenu();
                    String name = ChatColor.stripColor(ItemStackUtil.getDisplayName(event.getCurrentItem()));
                    if (!name.contains("&")) name = ChatColor.WHITE + name;
                    name = ChatColor.translateAlternateColorCodes('&', name);
                    nextState.getPotion().setName(name);
                }
                nextState.setPageNum(0);
                break;
            case "finalInvalid":
                return;
            case "finalEdit":
                nextState.nextMenu();
                nextState.nextMenu();
                break;
            case "finalConfirm":
                Main.fileData.writeData(nextState.getPotion());
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(ChatColor.GREEN + "Your changes to "
                    + state.getPotion().getName() + ChatColor.GREEN + " have been saved.");
                return;
            case "give":
                break;
        }
        (new InventoryGUI(nextState)).openInv(player);*/

    }
}