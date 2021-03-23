package xyz.iiinitiationnn.custompotions;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import xyz.iiinitiationnn.custompotions.utils.ItemStackUtil;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

import java.util.*;


public class PotionReader {
    //private List<PotionObject> customPotions = new ArrayList<>();
    //private List<ItemStack> vanillaPotions = new ArrayList<>();


    private static Map<String, Object> readData() {
        FileConfiguration fileInput = (new Data(Main.getPlugin(Main.class))).getData();
        return fileInput.getValues(false);
    }

    public static List<ItemStack> getAllPotionItems(boolean vanillaFirst) {
        List<ItemStack> allPotions = new ArrayList<>();
        if (vanillaFirst)
            allPotions.addAll(getVanillaPotions());

        for (PotionObject potion : getCustomPotions()) {
            allPotions.add(potion.getPotion());
        }

        if (!vanillaFirst)
            allPotions.addAll(getVanillaPotions());
        return allPotions;
    }

    public static List<PotionObject> getCustomPotions() {
        List<PotionObject> customPotions = new ArrayList<>();
        Map<String, Object> potions = readData();
        for (Map.Entry<String, Object> potion : potions.entrySet()) {
            PotionObject newPotion = new PotionObject();

            // Potion ID
            newPotion.setPotionID(potion.getKey());

            // Name
            Map<String, Object> attributes = ((MemorySection) potion.getValue()).getValues(false);
            String name = (String) attributes.get("name");
            if (name == null) {
                Main.log.warning(potion.getKey() + " does not have a valid name. Skipping the potion for now...");
                continue;
            }
            newPotion.setName(name);

            // Type
            String typeS = (String) attributes.get("type");
            if (typeS == null) {
                Main.log.warning(name + " does not have a valid type. "
                        + "Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion for now...");
                continue;
            }
            Material typeM = Material.matchMaterial(typeS);
            if (typeM != Material.POTION && typeM != Material.SPLASH_POTION && typeM != Material.LINGERING_POTION) {
                Main.log.warning(typeS + " is not a valid type for " + name
                        + ". Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion for now...");
                continue;
            }
            newPotion.setType(typeM);

            // Colour
            Map<String, Object> colours = ((MemorySection) attributes.get("colour")).getValues(false);
            /*String redS = (String) colours.get("red");
            String greenS = (String) colours.get("green");
            String blueS = (String) colours.get("blue");
            if (redS == null || greenS == null || blueS == null) {
                Main.log.warning(name + " does not have a valid colour. Skipping the potion for now...");
                continue;
            }
            int redI, greenI, blueI;
            try {
                redI = Integer.parseInt(redS);
                greenI = Integer.parseInt(greenS);
                blueI = Integer.parseInt(blueS);
                if (!Colour.hasValidRGB(redI, greenI, blueI)) {
                    Main.log.warning(name + " does not have a valid colour with RGB values 0-255. "
                            + "Skipping the potion for now...");
                    continue;
                }
            } catch (Exception e) {
                Main.log.warning(name + " does not have a valid colour with RGB values 0-255. "
                        + "Skipping the potion for now...");
                continue;
            }*/
            int redI = (int) colours.get("red");
            int greenI = (int) colours.get("green");
            int blueI = (int) colours.get("blue");
            newPotion.setColour(new Colour(redI, greenI, blueI));

            // Effects
            List<PotionEffect> potionEffects = new ArrayList<>();
            boolean isLingering = typeM == Material.LINGERING_POTION;
            for (Map.Entry<String, Object> effect
                    : (((MemorySection) attributes.get("effects")).getValues(false)).entrySet()) {

                // Effect Type
                String effectTypeS = effect.getKey();
                if (effectTypeS.equalsIgnoreCase("none")) break;
                PotionEffectType effectTypeT = PotionEffectType.getByName(effectTypeS);
                if (effectTypeT == null) {
                    Main.log.warning(effectTypeS + " is not a valid effect for " + name
                            + ". Skipping the effect for now...");
                    continue;
                }

                // Effect Duration
                Map<String, Object> effectInfo = ((MemorySection) effect.getValue()).getValues(false);
                /*String effectDurationS = (String) effectInfo.get("duration");
                if (effectDurationS == null) {
                    Main.log.warning(effectTypeS + " does not have a valid effect duration for " + name
                            + ". Skipping the effect for now...");
                    continue;
                }
                int effectDurationI;
                try {
                    effectDurationI = Integer.parseInt(effectDurationS);
                } catch (NumberFormatException e) {
                    Main.log.warning(effectTypeS + " does not have a valid effect duration for " + name
                            + ". Skipping the effect for now...");
                    continue;
                }*/
                int effectDurationI = (int) effectInfo.get("duration"); // seconds
                int durationTicks = isLingering ? 80 * effectDurationI : 20 * effectDurationI;
                if (effectDurationI < 1 || (isLingering && effectDurationI > 26843545)
                        || (!isLingering && effectDurationI > 107374182)) {
                    if (isLingering) {
                        Main.log.warning(name + " must have an effect duration between 1 and 26,843,545 for "
                                + effectTypeS + ". Skipping the effect for now...");
                    } else {
                        Main.log.warning(name + " must have an effect duration between 1 and 107,374,182 for "
                                + effectTypeS + ". Skipping the effect for now...");
                    }
                    continue;
                }

                // Effect Amplifier
                /*String effectAmplifierS = (String) effectInfo.get("amplifier");
                if (effectAmplifierS == null) {
                    Main.log.warning(effectTypeS + " does not have a valid effect amplifier for " + name
                            + ". Skipping the effect for now...");
                    continue;
                }
                int effectAmplifierI;
                try {
                    effectAmplifierI = Integer.parseInt(effectAmplifierS);
                } catch (NumberFormatException e) {
                    Main.log.warning(effectTypeS + " does not have a valid effect amplifier for " + name
                            + ". Skipping the effect for now...");
                    continue;
                }*/
                int effectAmplifierI = (int) effectInfo.get("amplifier");
                if (effectAmplifierI < 0 || effectAmplifierI > PotionUtil.maxAmp(effectTypeS)) {
                    Main.log.warning(name + " must have an effect amplifier from 0 to "
                            + PotionUtil.maxAmp(effectTypeS) + " for " + effectTypeS
                            + ". Skipping the effect for now...");
                    continue;
                }

                PotionEffect potionEffect = new PotionEffect(effectTypeT, durationTicks, effectAmplifierI);
                potionEffects.add(potionEffect);
            }

            // Potion Item
            ItemStack potionItem = new ItemStack(typeM);
            PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
            if (potionMeta == null) {
                Main.log.severe("There was an error retrieving the potion metadata when reading from potions.yml.");
                return null;
            }
            potionMeta.setDisplayName(ChatColor.RESET + name);
            potionMeta.setColor(Color.fromRGB(redI, greenI, blueI));
            for (PotionEffect potionEffect : potionEffects) potionMeta.addCustomEffect(potionEffect, true);
            potionItem.setItemMeta(potionMeta);
            newPotion.setPotion(potionItem);

            // Recipes
            // TODO get all vanilla potions
            List <String> vanillaPotionNames = new ArrayList<>(); // TODO here <--
            List <PotionRecipeObject> potionRecipes = new ArrayList<>();
            for (Map.Entry<String, Object> recipe
                    : (((MemorySection) attributes.get("recipes")).getValues(false)).entrySet()) {
                Map<String, Object> recipeInfo = ((MemorySection) recipe.getValue()).getValues(false);

                String reagentName = (String) recipeInfo.get("reagent");
                Material reagent = Material.matchMaterial(reagentName);
                String base = (String) recipeInfo.get("base");

                if (reagent == null) {
                    Main.log.warning(reagentName + " is not a valid reagent for a recipe of " + name
                            + ". Skipping the recipe for now...");
                    continue;
                }

                if (!potions.containsKey(base) && !vanillaPotionNames.contains(base)) {
                    Main.log.warning(base + " is not a valid base potion for a recipe of " + name
                            + ". Skipping the recipe for now...");
                    continue;
                }

                if (base.equals(name)) {
                    Main.log.warning(name + " cannot use itself as a base in a recipe. "
                            + "Skipping the recipe for now...");
                    continue;
                }

                potionRecipes.add(new PotionRecipeObject(reagent, base, name));
            }
            newPotion.setRecipes(potionRecipes);

            customPotions.add(newPotion);
            Main.log.info("Successfully added " + name + ChatColor.RESET + " to the game.");

        }
        return customPotions;
    }

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

                ItemStack vanillaPotion = new ItemStack(potionType);
                ItemStackUtil.setBasePotionData(vanillaPotion, type, false, false);
                potions.add(vanillaPotion);

                if (type.isExtendable()) {
                    vanillaPotion = new ItemStack(potionType);
                    ItemStackUtil.setBasePotionData(vanillaPotion, type, true, false);
                    potions.add(vanillaPotion);
                }

                if (type.isUpgradeable()) {
                    vanillaPotion = new ItemStack(potionType);
                    ItemStackUtil.setBasePotionData(vanillaPotion, type, false, true);
                    potions.add(vanillaPotion);
                }
            }
        }
        return potions;
    }

}
