package xyz.iiinitiationnn.custompotions.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import xyz.iiinitiationnn.custompotions.Colour;
import xyz.iiinitiationnn.custompotions.Main;

import java.util.List;

public class ItemStackUtil {
    /**
     * Returns whether an item is a potion.
     */
    public static boolean isPotion(ItemStack item) {
        Material material = item.getType();
        return material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION;
    }

    /**
     * Utility to reset the localized name of an ItemStack.
     */
    public static void resetLocalizedName(ItemStack item) {
        setLocalizedName(item, null);
    }

    /**
     * Utility to set the localized name of an ItemStack.
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
     * Utility to set the display name of an ItemStack.
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
     * Utility to set the color of a potion ItemStack.
     */
    public static void setColor(ItemStack item, Colour colour) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when setting the colour of a potion.");
            return;
        }
        meta.setColor(colour.toBukkitColor());
        item.setItemMeta(meta);
    }

    /**
     * Utility to set the lore of an ItemStack.
     */
    public static void setLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when setting the lore.");
            return;
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Utility to reset the lore of an ItemStack.
     */
    public static void resetLore(ItemStack item) {
        setLore(item, null);
    }

    /**
     * Returns whether potion has a certain effect type.
     */
    public static boolean hasEffect(ItemStack potion, PotionEffectType effectType) {
        if (!isPotion((potion)))
            return false;
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when determining if potion has a certain effect.");
            return false;
        }

        for (PotionEffect effect: meta.getCustomEffects()) {
            if (effect.getType() == effectType) {
                return true;
            }
        }
        return false;
    }

    public static void setBasePotionData(ItemStack potion, PotionType type, boolean extended, boolean upgraded) {
        if (!isPotion((potion)))
            return;

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when setting base potion data.");
            return;
        }
        meta.setBasePotionData(new PotionData(type, extended, upgraded));
    }

    /**
     * Returns the potion ID of a vanilla potion.
     * Useful for reading to, writing to, and processing recipes involving Vanilla potions.
     */
    public static String getVanillaPotionID(ItemStack vanillaPotion) {
        if (!isPotion((vanillaPotion)))
            return "";

        PotionMeta meta = (PotionMeta) vanillaPotion.getItemMeta();
        if (meta == null) {
            Main.log.severe("There was an error retrieving the item metadata when obtaining the potion ID of a vanilla potion.");
            return "";
        }

        PotionData data =  meta.getBasePotionData();
        String ID = data.getType().name();
        if (data.isExtended())
            ID += "_EXTENDED";
        if (data.isUpgraded())
            ID += "_UPGRADED";

        return ID;
    }

    // Gets display name of custom potion, or type of Vanilla potion.
    public static String getDisplayName(Main main, ItemStack item) {
        if (!isPotion(item))
            return "";

        if (item.getItemMeta() == null) {
            Main.log.severe("Unknown error X39.");
            return null;
        }

        // Custom potion.
        if (item.getItemMeta().hasDisplayName()) return item.getItemMeta().getDisplayName() +
                " (" + StringUtil.titleCase(item.getType().name(), "_") + ")";

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        boolean isRegular = item.getType() == Material.POTION;
        boolean isSplash = item.getType() == Material.SPLASH_POTION;
        boolean isLingering = item.getType() == Material.LINGERING_POTION;

        // Vanilla potion.
        switch (meta.getBasePotionData().getType()) {
            case WATER:
                if (isRegular) return "Water Bottle";
                else if (isSplash) return "Splash Water Bottle";
                else if (isLingering) return "Lingering Water Bottle";
                else return null;
            case AWKWARD:
                return "Awkward " + StringUtil.titleCase(item.getType().name(), "_");
            case MUNDANE:
                return "Mundane " + StringUtil.titleCase(item.getType().name(), "_");
            case THICK:
                return "Thick " + StringUtil.titleCase(item.getType().name(), "_");
            default:
                StringBuilder name = new StringBuilder();
                name.append(StringUtil.titleCase(item.getType().name(), "_"));
                name.append(" of ");
                name.append(StringUtil.titleCase(meta.getBasePotionData().getType().name(), "_"));
                if (meta.getBasePotionData().isExtended()) name.append(" (Extended)");
                if (meta.getBasePotionData().isUpgraded()) name.append(" (II)");
                return name.toString();
        }
    }




}
