package xyz.iiinitiationnn.custompotions.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import xyz.iiinitiationnn.custompotions.Colour;
import xyz.iiinitiationnn.custompotions.Main;
import xyz.iiinitiationnn.custompotions.Potion;
import xyz.iiinitiationnn.custompotions.PotionEffectSerializable;
import xyz.iiinitiationnn.custompotions.PotionRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PotionUtil {
    private static final String VANILLA_ID_DELIMITER = "-";

    /**
     * Returns whether an item is a potion.
     */
    public static boolean isPotion(ItemStack item) {
        Material type = item.getType();
        return type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION;
    }

    /**
     * Returns whether an item is a potion.
     */
    public static boolean isPotion(Material type) {
        return type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION;
    }

    /**
     * Returns whether an item is a potion.
     */
    public static boolean isPotion(String type) {
        return type.equalsIgnoreCase("POTION") || type.equalsIgnoreCase("SPLASH_POTION")
                || type.equalsIgnoreCase("LINGERING_POTION");
    }

    public static boolean isValidDuration(boolean isLingering, int duration) {
        return (isLingering && 1 <= duration && duration <= MagicNumber.LINGERING_MAX_DURATION) ||
            (!isLingering && 1 <= duration && duration <= MagicNumber.REGULAR_MAX_DURATION);
    }

    public static int secondsToTicks(boolean isLingering, int duration) {
        return isLingering ? duration * MagicNumber.LINGERING_TICK_MULTIPLIER : duration * MagicNumber.REGULAR_TICK_MULTIPLIER;
    }

    public static boolean isInstant(String effectName) {
        return Objects.equals(effectName, "HARM") || Objects.equals(effectName, "HEAL");
    }

    /**
     * Return a given potion effect's maximum amplifier.
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

    /**
     * Determines if a given potion effect has only a single amplifier (I).
     */
    public static boolean hasSingleAmplifier(String effectName) {
        return maxAmp(effectName) == 0;
    }

    public static boolean isValidAmp(String effectName, int amplifier) {
        int realAmplifier = amplifier - 1;
        return (0 <= realAmplifier && realAmplifier <= maxAmp(effectName));
    }

    /**
     * Returns a list of all valid custom potions in potions.json
     */
    public static List<Potion> getCustomPotions() {
        return Main.fileData.getCustomPotions();
    }

    /**
     * Returns all Vanilla potions.
     */
    public static List<ItemStack> getVanillaPotions() {
        List<ItemStack> potions = new ArrayList<>();
        List<Material> threeTypes = new ArrayList<>();
        threeTypes.add(Material.POTION);
        threeTypes.add(Material.SPLASH_POTION);
        threeTypes.add(Material.LINGERING_POTION);

        for (Material potionType : threeTypes) {
            for (PotionType type : PotionType.values()) {
                if (type == PotionType.UNCRAFTABLE)
                    continue;

                potions.add(constructVanillaPotion(potionType, type, false, false));

                if (type.isExtendable())
                    potions.add(constructVanillaPotion(potionType, type, true, false));

                if (type.isUpgradeable())
                    potions.add(constructVanillaPotion(potionType, type, false, true));
            }
        }
        return potions;
    }

    /**
     * Returns all Vanilla and custom potions.
     */
    public static List<ItemStack> getAllPotions(boolean vanillaFirst) {
        List<ItemStack> allPotions = new ArrayList<>();
        if (vanillaFirst) allPotions.addAll(PotionUtil.getVanillaPotions());
        for (Potion potion : getCustomPotions()) allPotions.add(potion.toItemStack());
        if (!vanillaFirst) allPotions.addAll(PotionUtil.getVanillaPotions());
        return allPotions;
    }

    /**
     * Returns a list of all recipes for custom potions.
     */
    public static List<PotionRecipe> getCustomRecipes() {
        List<PotionRecipe> recipes = new ArrayList<>();
        for (Potion potion : getCustomPotions()) {
            recipes.addAll(potion.getRecipes());
        }
        return recipes;
    }

    /**
     * TODO Returns a list of all recipes for Vanilla potions.
     */
    public static List<PotionRecipe> getVanillaRecipes() {
        return new ArrayList<>();
    }

    /**
     * Returns a list of all recipes for Vanilla and custom potions.
     */
    public static List<PotionRecipe> getAllRecipes() {
        List<PotionRecipe> recipes = new ArrayList<>();
        recipes.addAll(getCustomRecipes());
        recipes.addAll(getVanillaRecipes());
        return recipes;
    }

    public static ItemStack constructVanillaPotion(Material potionType, PotionType type, boolean extended, boolean upgraded) {
        ItemStack vanillaPotion = new ItemStack(potionType);
        setBasePotionData(vanillaPotion, type, extended, upgraded);
        return vanillaPotion;
    }

    /**
     * Returns a new unique potion ID for a custom potion.
     */
    public static String generatePotionId() {
        String id = UUID.randomUUID().toString();
        List<String> existingIds = getAllPotionIds();
        while (existingIds.contains(id)) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    /**
     * Returns the potion ID of all potions.
     */
    public static List<String> getAllPotionIds() {
        List<String> ids = new ArrayList<>();
        ids.addAll(getVanillaPotionIds());
        ids.addAll(getCustomPotionIds());
        return ids;
    }

    /**
     * Returns the potion ID of all custom potions.
     */
    public static List<String> getCustomPotionIds() {
        List<String> ids = new ArrayList<>();
        for (Potion customPotion : getCustomPotions())
            ids.add(customPotion.getPotionId());
        return ids;
    }

    /**
     * Returns the potion ID of all vanilla potions.
     */
    public static List<String> getVanillaPotionIds() {
        List<String> ids = new ArrayList<>();
        for (ItemStack vanillaPotion : getVanillaPotions())
            ids.add(getIdFromVanillaPotion(vanillaPotion));
        return ids;
    }

    /**
     * Returns the potion ID of a vanilla potion.
     * Useful for writing to and processing recipes involving Vanilla potions.
     */
    public static String getIdFromVanillaPotion(ItemStack vanillaPotion) {
        if (!isPotion((vanillaPotion)))
            return "";

        PotionMeta meta = (PotionMeta) vanillaPotion.getItemMeta();
        if (meta == null) {
            Main.logSevere("There was an error retrieving the item metadata when obtaining the potion ID of a vanilla potion.");
            return "";
        }

        PotionData data =  meta.getBasePotionData();
        String id = vanillaPotion.getType().name();
        id += VANILLA_ID_DELIMITER + data.getType().name();
        if (data.isExtended()) {
            id += VANILLA_ID_DELIMITER + "EXTENDED";
        } else if (data.isUpgraded()) {
            id += VANILLA_ID_DELIMITER + "UPGRADED";
        } else {
            id += VANILLA_ID_DELIMITER + "STANDARD";
        }

        return id;
    }

    /**
     * Returns a vanilla potion ItemStack given its potion ID.
     * Useful for reading from and processing recipes involving Vanilla potions.
     */
    private static ItemStack getVanillaPotionFromId(String potionID) {
        if (!isValidVanillaId(potionID)) {
            Main.logSevere("The potion ID was invalid when constructing a vanilla potion from its ID.");
            return null;
        }

        Material potionType = Material.matchMaterial(getPotionType(potionID));
        if (potionType == null || !isPotion(potionType)) {
            Main.logSevere("The potion type was invalid when constructing a vanilla potion from its ID.");
            return null;
        }

        PotionType potionEffectType = PotionType.valueOf(getPotionEffectType(potionID));
        if (!Arrays.asList(PotionType.values()).contains(potionEffectType)) {
            Main.logSevere("The potion effect type was invalid when constructing a vanilla potion from its ID.");
            return null;
        }

        switch (getPotionState(potionID)) {
            case "EXTENDED":
                return constructVanillaPotion(potionType, potionEffectType, true, false);
            case "UPGRADED":
                return constructVanillaPotion(potionType, potionEffectType, false, true);
            case "STANDARD":
            default:
                return constructVanillaPotion(potionType, potionEffectType, false, false);
        }
    }

    /**
     * Format: {POTION TYPE}-{EFFECT TYPE}-{STANDARD | EXTENDED | UPGRADED}
     */
    private static boolean isValidVanillaId(String potionID) {
        return potionID.split(VANILLA_ID_DELIMITER).length == 3;
    }

    private static String getPotionType(String potionID) {
        return potionID.split(VANILLA_ID_DELIMITER)[0];
    }

    private static String getPotionEffectType(String potionID) {
        return potionID.split(VANILLA_ID_DELIMITER)[1];
    }

    private static String getPotionState(String potionId) {
        return potionId.split(VANILLA_ID_DELIMITER)[2];
    }

    /**
     * Returns a custom potion ItemStack given its potion ID.
     */
    private static ItemStack customPotionFromId(String potionID) {
        for (Potion customPotion : getCustomPotions()) {
            if (customPotion.getPotionId().equals(potionID)) {
                return customPotion.toItemStack();
            }
        }
        return null;
    }

    /**
     * Returns a potion ItemStack given its potion ID.
     */
    public static ItemStack potionFromId(String potionId) {
        ItemStack potion = customPotionFromId(potionId);
        if (potion == null)
            potion = getVanillaPotionFromId(potionId);
        return potion;
    }

    /**
     * Returns a potion's display name given its potion ID.
     */
    public static String potionNameFromID(String potionId, List<Potion> customPotions) {
        // Custom Potion
        for (Potion customPotion : customPotions) {
            if (customPotion.getPotionId().equals(potionId)) {
                return customPotion.getName();
            }
        }

        // Found nothing, must be Vanilla
        String potionType = getPotionType(potionId);
        String potionEffectType = getPotionEffectType(potionId);
        String potionState = getPotionState(potionId);
        switch (potionEffectType) {
            case "WATER":
                switch (potionType) {
                    case "POTION":
                        return "Water Bottle";
                    case "SPLASH_POTION":
                        return "Splash Water Bottle";
                    case "LINGERING_POTION":
                        return "Lingering Water Bottle";
                    default:
                        return "";
                }
            case "AWKWARD":
            case "MUNDANE":
            case "THICK":
                return StringUtil.titleCase(potionEffectType + " " + potionType, "_");
            default:
                String name = StringUtil.titleCase(potionType, "_");
                name += " of ";
                name += StringUtil.titleCase(potionEffectType, "_");
                if (potionState.equals("EXTENDED")) {
                    name += " (Extended)";
                } else if (potionState.equals("UPGRADED")) {
                    name += " (II)";
                }
                return name;
        }
    }

    /**
     * Utility to set the color of a potion ItemStack.
     */
    public static void setColor(ItemStack potion, Colour colour) {
        if (!isPotion(potion))
            return;

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null) {
            Main.logSevere("There was an error retrieving the item metadata when setting the colour of a potion.");
            return;
        }
        meta.setColor(colour.toBukkitColor());
        potion.setItemMeta(meta);
    }

    public static void setBasePotionData(ItemStack potion, PotionType type, boolean extended, boolean upgraded) {
        if (!isPotion((potion)))
            return;

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null) {
            Main.logSevere("There was an error retrieving the item metadata when setting base potion data.");
            return;
        }
        meta.setBasePotionData(new PotionData(type, extended, upgraded));
        potion.setItemMeta(meta);
    }

    /**
     * Adds the recipes of a potion to its lore.
     */
    public static void addLoreRecipes(ItemStack potion, Potion potionObject) {
        if (!isPotion(potion) || potionObject.getRecipes().size() == 0)
            return;

        List<Potion> customPotions = getCustomPotions();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "Recipes:");
        for (PotionRecipe recipe : potionObject.getRecipes()) {
            String ingredient = StringUtil.titleCase(recipe.getIngredient().name(), "_");
            String base = ChatColor.stripColor(potionNameFromID(recipe.getBase(), customPotions));
            lore.add(ChatColor.GOLD + ingredient + " + " + base);
        }
        ItemStackUtil.addLore(potion, lore);
    }

    public static List<PotionEffectSerializable> getEffects(ItemStack potion) {
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null) {
            Main.logSevere("There was an error retrieving the item metadata when getting potion effects.");
            return new ArrayList<>();
        }
        List <PotionEffectSerializable> effects = new ArrayList<>();
        for (PotionEffect effect : meta.getCustomEffects()) {
            effects.add(new PotionEffectSerializable(effect));
        }
        return effects;
    }

    public static void setEffects(ItemStack potion, List<PotionEffectSerializable> effects) {
        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
        if (potionMeta == null) {
            Main.logSevere("There was an error retrieving the potion metadata when setting potion effects.");
            return;
        }
        for (PotionEffectSerializable effect : effects) potionMeta.addCustomEffect(effect.toPotionEffect(), true);
        potion.setItemMeta(potionMeta);
    }
}
