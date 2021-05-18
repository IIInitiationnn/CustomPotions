package xyz.iiinitiationnn.custompotions.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import xyz.iiinitiationnn.custompotions.Main;

import java.util.List;

public class ItemStackUtil {
    /**
     * Returns whether a material is a valid ingredient for potion recipes.
     */
    public static boolean isValidIngredient(Material m) {
        return m.isItem() && m != Material.AIR && m != Material.POTION && m != Material.SPLASH_POTION
                && m != Material.LINGERING_POTION && m != Material.DEBUG_STICK && m != Material.KNOWLEDGE_BOOK;
    }

    /**
     * Resets the localized name of an ItemStack.
     */
    public static void resetLocalizedName(ItemStack item) {
        setLocalizedName(item, null);
    }

    /**
     * Sets the localized name of an ItemStack.
     */
    public static void setLocalizedName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when setting the localized name.");
            return;
        }
        meta.setLocalizedName(name);
        item.setItemMeta(meta);
    }

    /**
     * Returns the localized name of an ItemStack.
     */
    public static String getLocalizedName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when setting the localized name.");
            return "";
        }
        return meta.getLocalizedName();
    }

    /**
     * Sets the display name of an ItemStack.
     */
    public static void setDisplayName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when setting the display name.");
            return;
        }
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    /**
     * Sets the lore of an ItemStack.
     */
    private static void setLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when setting the lore.");
            return;
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Adds additional lore to an ItemStack.
     */
    public static void addLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when adding lore.");
            return;
        }
        List<String> fullLore = lore;
        if (meta.hasLore()) {
            fullLore = meta.getLore();
            fullLore.addAll(lore);
        }
        meta.setLore(fullLore);
        item.setItemMeta(meta);
    }

    /**
     * Resets the lore of an ItemStack.
     */
    public static void resetLore(ItemStack item) {
        setLore(item, null);
    }

}
