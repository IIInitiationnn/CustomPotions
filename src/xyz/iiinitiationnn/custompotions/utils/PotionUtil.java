package xyz.iiinitiationnn.custompotions.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PotionUtil {

    /**
     * Given a potion effect, return its maximum amplifier.
     */
    public static int maxAmp(String effectName) {
        switch (effectName) {
            case "BAD_OMEN":
            case "HERO_OF_THE_VILLAGE":
                return 9;
            case "BLINDNESS":
            case "CONFUSION":
            case "DOLPHINS_GRACE":
            case "FIRE_RESISTANCE":
            case "GLOWING":
            case "INVISIBILITY":
            case "NIGHT_VISION":
            case "SLOW_FALLING":
            case "WATER_BREATHING":
                return 0;
            case "DAMAGE_RESISTANCE":
                return 4;
            case "HARM":
            case "HEAL":
            case "REGENERATION":
            case "POISON":
            case "WITHER":
                return 31;
            default:
                return 127;
        }
    }



}
