package xyz.iiinitiationnn.custompotions;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.iiinitiationnn.custompotions.listeners.BrewingStandListener;
import xyz.iiinitiationnn.custompotions.listeners.InventoryGUIListener;

import java.util.logging.Logger;

public class Main extends JavaPlugin {
    public static Logger log;
    public static FileData fileData;

    // TODO
    //  potentially playing around with lore for potion effects to show the correct potency, and time in day:hour:minute:second if applicable
    //  maybe in the distant future add presets like splash from existing or whatever
    //  handle weird gunpowder and dragons breath shenanigans??
    //  two predecessors cannot have same ingredients in helperRecipe ie. when loading from data (how we gonna initialise)
    //  right click potion in select menu to clone it
    //  when doing cp info, maybe find all potions which use it as a predecessor
    //  dont allow ESC to exit from nonanvil menus
    //  notify server when new version is available
    //  parse effect duration eg. seconds, minutes, ticks
    //  whenever "There was an error" put : instead of . and then print the trace
    //  instant damage: skip duration????
    //  when changing lingering <-> normal / splash, adjust the durations of all effects accordingly instead of letting it x4 or /4
    // TODO new "colours.yml" file where they can customise colours

    /**
     * Startup & initialisation.
     */
    @Override
    public void onEnable() {
        log = this.getLogger();
        log.info("Initialising CustomPotions and validating potions.");

        fileData = new FileData(this);
        this.saveDefaultConfig();

        PluginManager pm = getServer().getPluginManager();
        BrewingStandListener brewingStandListener = new BrewingStandListener();
        InventoryGUIListener inventoryGUIListener = new InventoryGUIListener();
        pm.registerEvents(brewingStandListener, this);
        pm.registerEvents(inventoryGUIListener, this);

        this.getCommand("custompotions").setExecutor(new Commands(this));
        this.getCommand("custompotions").setTabCompleter(new TabComplete(this));
    }

    /**
     * Reloading the plugin.
     */
    public void reload() {
        fileData.reloadData();
        this.getCommand("custompotions").setExecutor(new Commands(this));
        this.getCommand("custompotions").setTabCompleter(new TabComplete(this));
    }

    /**
     * Terminating the server or plugin.
     */
    @Override
    public void onDisable() {
        log.info("CustomPotions has been disabled.");
    }

    public static void logInfo(String string) {
        log.info(string);
    }

    public static void logWarning(String string) {
        log.warning(string);
    }

    public static void logSevere(String string) {
        log.severe(string);
    }


    /*
    public ItemStack getVanillaPredecessor(ItemStack result) {
        boolean isExtended = ((PotionMeta) result.getItemMeta()).getBasePotionData().isExtended();
        boolean isUpgraded = ((PotionMeta) result.getItemMeta()).getBasePotionData().isUpgraded();
        if (!isExtended && !isUpgraded) {
            ItemStack potion = new ItemStack(result.getType());
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            if (meta == null) {
                log.severe("There was an error retrieving the item metadata when getting the base potion of an"
                        + "extended or upgraded Vanilla potion.");
                return null;
            }
            meta.setBasePotionData(new PotionData(PotionType.AWKWARD, false, false));
            potion.setItemMeta(meta);
            return potion;
        } else {
            ItemStack potion = new ItemStack(result.getType());
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            if (meta == null) {
                log.severe("There was an error retrieving the item metadata when getting the base potion of an"
                        + "extended or upgraded Vanilla potion.";
                return null;
            }
            meta.setBasePotionData(new PotionData(((PotionMeta) result.getItemMeta()).getBasePotionData().getType(), false, false));
            potion.setItemMeta(meta);
            return potion;
        }
    }*/

    /*
    public void reinitialiseAllPotionRecipes() {
        allPotionRecipes = new ArrayList<>();
        for (ItemStack vanillaPotion : allVanillaPotions) {
            if (vanillaPotion.getItemMeta() == null) {
                this.log.severe("Unknown error X42.");
                return;
            }
            if (((PotionMeta) vanillaPotion.getItemMeta()).getBasePotionData().getType() == PotionType.WATER) continue;
            boolean isExtended = ((PotionMeta) vanillaPotion.getItemMeta()).getBasePotionData().isExtended();
            boolean isUpgraded = ((PotionMeta) vanillaPotion.getItemMeta()).getBasePotionData().isUpgraded();

            if (!isExtended && !isUpgraded) {
                switch (((PotionMeta) vanillaPotion.getItemMeta()).getBasePotionData().getType()) {
                    case NIGHT_VISION:
                        allPotionRecipes.add(new PotionRecipe(vanillaPotion, getVanillaPredecessor(vanillaPotion), Material.GOLDEN_CARROT, -1));
                        break;

                }
            } else if (isExtended) {

            } else {

            }


            if (vanillaPotion.getType() == Material.SPLASH_POTION) {

            } else if (vanillaPotion.getType() == Material.LINGERING_POTION) {

            }


        }

        this.log.info("There are " + allPotionRecipes.size() + " vanilla recipes");

        for (PotionInfo info : allCustomPotions) {
            allPotionRecipes.addAll(info.potionrecipes); // if it's removed from / modified in info.potionrecipes, does it get removed from allPotionRecipes too?
        }
    }*/

    // Give potions to players.
    /*
    public boolean givePotions(Player player, ItemStack clicked) {
        String previousClick;
        if (clicked == null) {
            previousClick = "custompotions give-0 not not not not not";
        } else {
            if (clicked.getItemMeta() == null) {
                this.log.severe("Unknown error X40.");
                return true;
            }
            previousClick = clicked.getItemMeta().getLocalizedName();
        }

        inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select a Potion to Withdraw");
        int PAGESIZE = 51;
        int numPotions = 0;
        int pageNumToDisplay = getPage(previousClick);
        int totalPotions = allCustomPotions.size();
        boolean needsNextPage = (totalPotions - PAGESIZE * pageNumToDisplay) > PAGESIZE;

        // PREVIOUS PAGE, slot 35.
        inv.setItem(35, newPageGUI("previous", previousClick, "give", pageNumToDisplay, needsNextPage));

        // NEXT PAGE, slot 44.
        inv.setItem(44, newPageGUI("next", previousClick, "give", pageNumToDisplay, needsNextPage));

        // EXIT, slot 53.
        inv.setItem(53, newItemStackGUI(new ItemStack(Material.BARRIER),
                setClick(previousClick, "give_exit"), ChatColor.RED + "EXIT", null));

        // All custom potions from potions.yml.
        for (PotionInfo pi : allCustomPotions) {
            // Past the page we want to display.
            if (numPotions >= PAGESIZE * (pageNumToDisplay + 1)) {
                break;
            }

            // Get to the page we want to display.
            if (numPotions < PAGESIZE * pageNumToDisplay) {
                numPotions++;
                continue;
            }

            ItemStack potionItemStack = pi.itemstack.clone();
            PotionMeta meta = (PotionMeta) potionItemStack.getItemMeta();
            if (meta == null) {
                this.log.severe("Unknown error X9.");
                return true;
            }
            List<String> lore = addRecipes(new ArrayList<>(), pi.potionrecipes);
            potionItemStack = newItemStackGUI(potionItemStack, setPage(previousClick, pageNumToDisplay), meta.getDisplayName(), lore);
            inv.addItem(potionItemStack);
            numPotions++;
        }
        player.openInventory(inv);
        return false;
    }*/

    /*
    @EventHandler
    public void brewPotionClick(InventoryClickEvent event) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (event.getClickedInventory() == null) return;
            if (event.getInventory().getType() != InventoryType.BREWING) return;
            if (((BrewerInventory) event.getInventory()).getHolder() == null) return;
            if (((BrewerInventory) event.getInventory()).getIngredient() == null) return;

            PotionRecipe recipe = PotionRecipe.getRecipe((BrewerInventory) event.getInventory(), allPotionRecipes, this);
            if (recipe == null) return;
            if (((BrewerInventory) event.getInventory()).getHolder().getBrewingTime() == 0) {
                recipe.startBrewing((BrewerInventory) event.getInventory(), this, allPotionRecipes);
            } else {
                // TODO ?????
            }
        }, 1L);
    }
    */



}