package xyz.iiinitiationnn.custompotions;

import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.iiinitiationnn.custompotions.states.*;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.MagicNumber;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

import java.io.Serializable;

public class Actions {
    public static abstract class Action implements Serializable {
        /**
         * Processes the action on the given state and returns the new state.
         */
        public abstract State execute(State state);
    }

    public static class StartupAction extends Action {
        @Override
        public State execute(State state) {
            return state; // this action does nothing
        }
    }

    public static class InvalidAction extends Action {
        @Override
        public State execute(State state) {
            return null;
        }
    }

    private static class ExitSavedAction extends Action {
        @Override
        public State execute(State state) {
            HumanEntity user = state.getEvent().getWhoClicked();

            // Save data
            Main.fileData.writeData(state.getPotion());

            // Close user inventory
            user.closeInventory();
            user.sendMessage(ChatColor.GREEN + "Your changes to "
                + state.getPotionName() + ChatColor.GREEN + " have been saved.");

            return null;
        }
    }

    public static class ExitUnsavedAction extends Action {
        @Override
        public State execute(State state) {
            HumanEntity user = state.getEvent().getWhoClicked();

            user.closeInventory();
            user.sendMessage(ChatColor.RED + "Your changes have not been saved.");

            return null;
        }
    }

    public static class ExitSelectAction extends Action {
        @Override
        public State execute(State state) {
            ClickType clickType = state.getEvent().getClick();
            State nextState = state.clone();

            if (clickType == ClickType.LEFT) {
                nextState.setAction(new ExitSavedAction());
            } else if (clickType == ClickType.RIGHT) {
                nextState.setAction(new ExitUnsavedAction());
            } else {
                nextState.setAction(new InvalidAction());
            }

            return nextState.nextState();
        }
    }

    public static class MenuPreviousAction extends Action {
        @Override
        public State execute(State state) {
            State midState = state.clone().resetPageNum();
            if (state instanceof PotionTypeMenu) {
                return new MainMenu();
            } else if (state instanceof PotionColourMenu) {
                return new PotionTypeMenu(midState);
            } else if (state instanceof EffectTypeMenu) {
                return new PotionColourMenu(midState);
            } else if (state instanceof RecipeIngredientMenu) {
                return new EffectTypeMenu(midState);
            } else if (state instanceof RecipeBaseMenu) {
                return new RecipeIngredientMenu(midState);
            } else {
                Main.logSevere("There was an error navigating to the previous menu. Current: "
                        + state.getClass().getSimpleName());
                return null;
            }
        }
    }

    public static class MenuNextAction extends Action {
        @Override
        public State execute(State state) {
            State midState = state.clone().resetPageNum();
            if (state instanceof MainMenu) {
                return new PotionTypeMenu(midState);
            } else if (state instanceof PotionTypeMenu) {
                return new PotionColourMenu(midState);
            } else if (state instanceof PotionColourMenu) {
                return new EffectTypeMenu(midState);
            } else if (state instanceof EffectTypeMenu || state instanceof EffectDurationMenu
                    || state instanceof EffectAmplifierMenu) {
                return new RecipeIngredientMenu(midState);
            } else if (state instanceof RecipeIngredientMenu) {
                return new PotionNameMenu(midState);
            } else if (state instanceof PotionNameMenu) {
                return new FinalConfirmMenu(midState);
            } else {
                Main.logSevere("There was an error navigating to the next menu. Current: "
                        + state.getClass().getSimpleName());
                return null;
            }
        }
    }

    public static class PagePreviousAction extends Action {
        @Override
        public State execute(State state) {
            return state.clone().decrementPageNum();
        }
    }

    public static class PageNextAction extends Action {
        @Override
        public State execute(State state) {
            return state.clone().incrementPageNum();
        }
    }

    /**
     * Creating, cloning, or modifying a new potion.
     */
    public static class ModifyPotionAction extends Action {
        @Override
        public State execute(State state) {
            return new PotionTypeMenu(state.clone().resetPageNum());
        }
    }

    public static class RemovePotionAction extends Action {
        @Override
        public State execute(State state) {
            return new RemoveConfirmMenu(state.clone().resetPageNum());
        }
    }

    public static class SelectPotionAction extends Action {
        @Override
        public State execute(State state) {
            ClickType clickType = state.getEvent().getClick();
            State nextState = state.clone();

            if (clickType == ClickType.LEFT) {
                nextState.setAction(new ModifyPotionAction());
            } else if (clickType == ClickType.SHIFT_LEFT) {
                nextState
                    .duplicatePotion()
                    .setAction(new ModifyPotionAction());
            } else if (clickType == ClickType.RIGHT) {
                nextState.setAction(new RemovePotionAction());
            } else {
                nextState.setAction(new InvalidAction());
            }

            return nextState.nextState();
        }
    }

    public static class SelectPotionTypeAction extends Action {
        @Override
        public State execute(State state) {
            return new PotionColourMenu(state.clone().resetPageNum());
        }
    }

    public static class SelectPotionColourAction extends Action {
        @Override
        public State execute(State state) {
            return new EffectTypeMenu(state.clone().resetPageNum());
        }
    }

    public static class NoEffectsAction extends Action {
        @Override
        public State execute(State state) {
            return new RecipeIngredientMenu(state.clone().resetPageNum());
        }
    }

    public static class AddEffectTypeAction extends Action {
        @Override
        public State execute(State state) {
            // TODO move inputting to inventory creation logic in menus
            // TODO use potionutils to determine if it has a duration, if not, set input.effectDuration to something and go to amplifier
            // TODO when creating new effect, remove the old effect from the potion
            return new EffectDurationMenu(state.clone().resetPageNum());
        }
    }

    public static class RemoveEffectAction extends Action {
        @Override
        public State execute(State state) {
            // TODO could probably use input instead of ItemStackUtil.getDisplayName(state.getEvent().getCurrentItem()
            State nextState = state
                    .clone()
                    .removePotionEffectByName(ItemStackUtil.getDisplayName(state.getEvent().getCurrentItem())); // TODO also move into inventory creation logic
            return new EffectTypeMenu(nextState);
        }
    }

    public static class SelectEffectTypeAction extends Action {
        @Override
        public State execute(State state) {
            ClickType clickType = state.getEvent().getClick();
            State nextState = state.clone();

            if (clickType == ClickType.LEFT) {
                // modify selected effect type
                nextState.setAction(new AddEffectTypeAction());
            } else if (clickType == ClickType.RIGHT) {
                // remove the effect
                nextState.setAction(new RemoveEffectAction());
            } else {
                nextState.setAction(new InvalidAction());
            }

            return nextState.nextState();
        }
    }

    public static class EnterEffectDurationAction extends Action {
        @Override
        public State execute(State state) {
            int slot = state.getEvent().getSlot();
            State nextState = state.clone();

            if (slot == MagicNumber.ANVIL_LEFT_INPUT_SLOT) {
                // skip to next menu
                nextState.setAction(new MenuNextAction());
                return nextState.nextState();
            } else if (slot == MagicNumber.ANVIL_OUTPUT_SLOT) {
                // add an effect duration for the new effect type
                try {
                    String input = ItemStackUtil.getDisplayName(state.getEvent().getCurrentItem());
                    int duration = Integer.parseInt(input);

                    if (PotionUtil.isValidDuration(state.isPotionLingering(), duration)) {
                        int durationTicks = PotionUtil.secondsToTicks(state.isPotionLingering(), duration);
                        nextState.setInputEffectDuration(durationTicks);

                        // Effect only has one possible amplifier (I), add new effect to the potion
                        if (PotionUtil.hasSingleAmplifier(state.getInput().getEffectType())) {
                            PotionEffectType type = PotionEffectType.getByName(state.getInputEffectType());
                            nextState
                                .addPotionEffect(new PotionEffectSerializable(type, durationTicks, 0))
                                .resetInput()
                                .resetPageNum();
                            return new EffectTypeMenu(nextState);
                        } else {
                            return new EffectAmplifierMenu(nextState);
                        }
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ignored) {
                    nextState.setAction(new InvalidAction());
                    return nextState.nextState();
                }
            } else {
                nextState.setAction(new InvalidAction());
                return nextState.nextState();
            }
        }
    }

    public static class EnterEffectAmplifierAction extends Action {
        @Override
        public State execute(State state) {
            int slot = state.getEvent().getSlot();
            State nextState = state.clone();

            if (slot == MagicNumber.ANVIL_LEFT_INPUT_SLOT) {
                // skip to next menu
                nextState.setAction(new MenuNextAction());
                return nextState.nextState();
            } else if (slot == MagicNumber.ANVIL_OUTPUT_SLOT) {
                // add an effect amplifier for the new effect type
                try {
                    String input = ItemStackUtil.getDisplayName(state.getEvent().getCurrentItem());
                    int amplifier = Integer.parseInt(input);

                    if (PotionUtil.isValidAmp(state.getInputEffectType(), amplifier)) {
                        // add the new effect to the potion
                        PotionEffectType type = PotionEffectType.getByName(state.getInputEffectType());
                        int duration = state.getInputEffectDuration();
                        nextState
                            .addPotionEffect(new PotionEffectSerializable(type, duration, amplifier - 1))
                            .resetInput()
                            .resetPageNum();
                        return new EffectTypeMenu(nextState);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ignored) {
                    nextState.setAction(new InvalidAction());
                    return nextState.nextState();
                }
            } else {
                nextState.setAction(new InvalidAction());
                return nextState.nextState();
            }
        }
    }

    public static class AddRecipeIngredientAction extends Action {
        @Override
        public State execute(State state) {
            // TODO move inputting to inventory creation logic in menus; also applies to selectrecipeingredientaction
            return new RecipeBaseMenu(state.clone().resetPageNum());
        }
    }

    /**
     * Remove all recipes using a certain ingredient.
     */
    public static class RemoveRecipesIngredientAction extends Action {
        @Override
        public State execute(State state) {
            // TODO move inputting to inventory creation logic in menus
            State nextState = state
                    .clone()
                    .removePotionRecipesByIngredient(state.getInputRecipeIngredient()); // TODO also move into inventory creation logic
            // actually not sure if possible to move this part into it
            return new RecipeIngredientMenu(nextState);
        }
    }

    public static class SelectRecipeIngredientAction extends Action {
        @Override
        public State execute(State state) {
            ClickType clickType = state.getEvent().getClick();
            State nextState = state.clone();

            if (clickType == ClickType.LEFT) {
                // modify recipes using the selected ingredient
                nextState.setAction(new AddRecipeIngredientAction());
            } else if (clickType == ClickType.RIGHT) {
                // remove all recipes using the selected ingredient
                nextState.setAction(new RemoveEffectAction());
            } else {
                nextState.setAction(new InvalidAction());
            }

            return nextState.nextState();
        }
    }

    public static class AddRecipeBaseAction extends Action {
        @Override
        public State execute(State state) {
            // TODO move inputting to inventory creation logic in menus
            // return new RecipeIngredientMenu(state.clone().resetInput().resetPageNum());
            return new RecipeBaseMenu(state.clone()); // decided to keep them on the same menu
        }
    }

    public static class RemoveRecipeBaseAction extends Action {
        @Override
        public State execute(State state) {
            return new RecipeBaseMenu(state.clone());
        }
    }

    public static class EnterNameAction extends Action {
        @Override
        public State execute(State state) {
            int slot = state.getEvent().getSlot();
            State nextState = state.clone();

            if (slot == MagicNumber.ANVIL_LEFT_INPUT_SLOT) {
                // skip to next menu
                nextState.setAction(new MenuNextAction());
                return nextState.nextState();
            } else if (slot == MagicNumber.ANVIL_OUTPUT_SLOT) {
                // set the name
                String name = ChatColor.stripColor(ItemStackUtil.getDisplayName(state.getEvent().getCurrentItem()));
                if (!name.contains("&")) name = ChatColor.WHITE + name;
                name = ChatColor.translateAlternateColorCodes('&', name);
                return new FinalConfirmMenu(nextState.setPotionName(name).resetPageNum());
            } else {
                nextState.setAction(new InvalidAction());
                return nextState.nextState();
            }
        }
    }

    public static class FinalEditAction extends Action {
        @Override
        public State execute(State state) {
            return new PotionTypeMenu(state.clone().resetPageNum());
        }
    }

    public static class FinalConfirmAction extends Action {
        @Override
        public State execute(State state) {
            return state
                .clone()
                .setAction(new ExitSavedAction())
                .nextState();
        }
    }

    public static class GivePotionAction extends Action {
        @Override
        public State execute(State state) {
            HumanEntity user = state.getEvent().getWhoClicked();

            if (user.getInventory().firstEmpty() == -1) {
                user.sendMessage(ChatColor.RED + "Your inventory is full.");
            } else {
                user.getInventory().addItem(state.getPotionItemStack()); // TODO localizedName for brewing, whatever system we use e.g. maybe potion ID
                user.sendMessage(ChatColor.GREEN + "You have been given 1x "
                    + state.getPotionName() + ChatColor.GREEN + ".");
            }

            return state
                .clone()
                .setAction(new InvalidAction())
                .nextState();
        }
    }

    public static class GivePotionStackAction extends Action {
        @Override
        public State execute(State state) {
            HumanEntity user = state.getEvent().getWhoClicked();

            if (user.getInventory().firstEmpty() == -1) {
                user.sendMessage(ChatColor.RED + "Your inventory is full.");
            } else {
                ItemStack potionStack = state.getPotionItemStack();
                potionStack.setAmount(potionStack.getMaxStackSize());
                user.getInventory().addItem(potionStack); // TODO localizedName for brewing, whatever system we use e.g. maybe potion ID
                user.sendMessage(ChatColor.GREEN + "You have been given " + potionStack.getMaxStackSize() + "x "
                    + state.getPotionName() + ChatColor.GREEN + ".");
            }

            return state
                .clone()
                .setAction(new InvalidAction())
                .nextState();
        }
    }

    public static class SelectGivePotionAction extends Action {
        @Override
        public State execute(State state) {
            ClickType clickType = state.getEvent().getClick();
            State nextState = state.clone();

            if (clickType == ClickType.LEFT) {
                nextState.setAction(new GivePotionAction());
            } else if (clickType == ClickType.SHIFT_LEFT) {
                nextState.setAction(new GivePotionStackAction());
            } else {
                nextState.setAction(new InvalidAction());
            }

            return nextState.nextState();
        }
    }






}
