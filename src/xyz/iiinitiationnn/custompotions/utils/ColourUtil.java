package xyz.iiinitiationnn.custompotions.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import xyz.iiinitiationnn.custompotions.Colour;
import xyz.iiinitiationnn.custompotions.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ColourUtil {
    /**
     * Selects a random default potion Colour.
     */
    public static Colour randomDefaultColour() {
        List<Colour> defaultPotionColourList = defaultPotionColourList();
        return defaultPotionColourList.get(new Random().nextInt(defaultPotionColourList.size()));
    }

    /**
     * Returns a List of all default potion Colours which can be selected from in the potionColour menu.
     */
    public static List<Colour> defaultPotionColourList() {
        return new ArrayList<>(potionColourNames().values());
    }

    /**
     * Returns a bijective map of all default potion Colours which can be selected from in the potionColour menu.
     * Key: (String) Colour name.
     * Value: (Colour) Colour.
     */
    public static BiMap<String, Colour> potionColourNames() {
        BiMap<String, Colour> colourMap = HashBiMap.create();
        colourMap.put("Red", new Colour(0xFF, 0x00, 0x00));
        colourMap.put("Orange", new Colour(0xFF, 0xA5, 0x00));
        colourMap.put("Yellow", new Colour(0xFF, 0xFF, 0x00));
        colourMap.put("Olive", new Colour(0x80, 0x80, 0x00));
        colourMap.put("Lime", new Colour(0x00, 0xFF, 0x00));
        colourMap.put("Green", new Colour(0x00, 0x80, 0x00));
        colourMap.put("Teal", new Colour(0x00, 0x80, 0x80));
        colourMap.put("Aqua", new Colour(0x00, 0xFF, 0xFF));
        colourMap.put("Fuchsia", new Colour(0xFF, 0x00, 0xFF));
        colourMap.put("Maroon", new Colour(0x80, 0x00, 0x00));
        colourMap.put("Purple", new Colour(0x80, 0x00, 0x80));
        colourMap.put("Blue", new Colour(0x00, 0x00, 0xFF));
        colourMap.put("Navy", new Colour(0x00, 0x00, 0x80));
        colourMap.put("White", new Colour(0xFF, 0xFF, 0xFF));
        colourMap.put("Silver", new Colour(0xC0, 0xC0, 0xC0));
        colourMap.put("Gray", new Colour(0x80, 0x80, 0x80));
        colourMap.put("Black", new Colour(0x00, 0x00, 0x00));
        return colourMap;
    }

    /**
     * Returns a bijective map of all default potion Colours which can be selected from in the potionColour menu.
     * Key: (Colour) Colour.
     * Value: (String) Colour name.
     */
    public static BiMap<Colour, String> potionColours() {
        return potionColourNames().inverse();
    }


    /**
     * All default potion colours as a HashMap which can be selected from in the potionColour menu.
     */
    public static HashMap<Colour, ChatColor> colourChatColorMap() {
        HashMap<Colour, ChatColor> colourMap = new HashMap<>();
        colourMap.put(new Colour(0xFF, 0x00, 0x00), ChatColor.RED);
        colourMap.put(new Colour(0xFF, 0xA5, 0x00), ChatColor.GOLD);
        colourMap.put(new Colour(0xFF, 0xFF, 0x00), ChatColor.YELLOW);
        colourMap.put(new Colour(0x80, 0x80, 0x00), ChatColor.DARK_GREEN);
        colourMap.put(new Colour(0x00, 0xFF, 0x00), ChatColor.GREEN);
        colourMap.put(new Colour(0x00, 0x80, 0x00), ChatColor.DARK_GREEN);
        colourMap.put(new Colour(0x00, 0x80, 0x80), ChatColor.DARK_AQUA);
        colourMap.put(new Colour(0x00, 0xFF, 0xFF), ChatColor.AQUA);
        colourMap.put(new Colour(0xFF, 0x00, 0xFF), ChatColor.LIGHT_PURPLE);
        colourMap.put(new Colour(0x80, 0x00, 0x00), ChatColor.DARK_RED);
        colourMap.put(new Colour(0x80, 0x00, 0x80), ChatColor.DARK_PURPLE);
        colourMap.put(new Colour(0x00, 0x00, 0xFF), ChatColor.BLUE);
        colourMap.put(new Colour(0x00, 0x00, 0x80), ChatColor.BLUE);
        colourMap.put(new Colour(0xFF, 0xFF, 0xFF), ChatColor.WHITE);
        colourMap.put(new Colour(0xC0, 0xC0, 0xC0), ChatColor.GRAY);
        colourMap.put(new Colour(0x80, 0x80, 0x80), ChatColor.DARK_GRAY);
        colourMap.put(new Colour(0x00, 0x00, 0x00), ChatColor.DARK_GRAY);
        return colourMap;
    }

    /**
     * Given the ItemStack of a potion, return the ChatColor closest to the potion's colour.
     */
    public static ChatColor getChatColor(ItemStack potion) {
        return getChatColor(getPotionColor(potion));
    }

    /**
     * Given a Bukkit Color, return the ChatColor closest to it.
     */
    public static ChatColor getChatColor(Color color) {
        return getChatColor(new Colour(color));
    }

    /**
     * Given a Colour, return the ChatColor closest to it.
     */
    public static ChatColor getChatColor(Colour colour) {
        List<Colour> defaultPotionColourList = new ArrayList<>(potionColourNames().values());
        Colour closest = colour.closestMatchFromList(defaultPotionColourList);
        return colourChatColorMap().get(closest);
    }

    /**
     * Obtain the Bukkit Color of a potion.
     */
    public static Color getPotionColor(ItemStack potion) {
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null || meta.getColor() == null) {
            Main.log.severe("There was an error retrieving the item metadata of a potion.");
            return null;
        }
        return meta.getColor();
    }


}
