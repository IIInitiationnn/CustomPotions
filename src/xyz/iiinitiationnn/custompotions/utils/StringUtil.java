package xyz.iiinitiationnn.custompotions.utils;

import org.apache.commons.lang.WordUtils;

public class StringUtil {

    /**
     * Capitalise string using title case and replace delimiter characters with spaces.
     */
    public static String titleCase(String s, String delimiter) {
        return WordUtils.capitalizeFully(s.replace(delimiter, " "));
    }

    /**
     * Given a potion effect's in-game name, return its common name.
     */
    public static String toCommonName(String effectName) {
        switch (effectName) {
            case "CONFUSION":
                return "Nausea";
            case "DAMAGE_RESISTANCE":
                return "Resistance";
            case "FAST_DIGGING":
                return "Haste";
            case "HARM":
                return "Instant Damage";
            case "HEAL":
                return "Instant Health";
            case "INCREASE_DAMAGE":
                return "Strength";
            case "JUMP":
                return "Jump Boost";
            case "SLOW":
                return "Slowness";
            case "SLOW_DIGGING":
                return "Mining Fatigue";
            default:
                return titleCase(effectName, "_");
        }
    }

    /**
     * Convert integer to Roman numeral equivalent.
     */
    public static String numToRomanNum(int num) {
        int[] values = {1, 4, 5, 9, 10, 40, 50, 90, 100};
        String[] roman = {"I", "IV", "V", "IX", "X", "XL", "L", "XC", "C"};
        StringBuilder sb = new StringBuilder();

        for (int i = values.length - 1; i >= 0 && num > 0; i--) {
            while (num >= values[i]) {
                num -= values[i];
                sb.append(roman[i]);
            }
        }
        return sb.toString();
    }

}
