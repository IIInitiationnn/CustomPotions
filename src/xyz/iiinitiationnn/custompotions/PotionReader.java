package xyz.iiinitiationnn.custompotions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;
import xyz.iiinitiationnn.custompotions.utils.MagicNumber;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

import java.util.*;


public class PotionReader {
    private static Map<String, Object> readData() {
        FileConfiguration fileInput = (new Data(Main.getPlugin(Main.class))).getData();
        return fileInput.getValues(false);
    }

    /**
     * Returns a list of all custom potions in potions.yml
     */
    public static List<Potion> getCustomPotions() {
        // TODO may be more efficient to use a hashmap; or add a new function which fetches them as a map
        //  since a lot of the things that call this method iterate over it to find the right potion
        List<Potion> customPotions = new ArrayList<>();
        Map<String, Object> potions = readData();
        for (Map.Entry<String, Object> potion : potions.entrySet()) {

            // Potion ID
            String potionID = potion.getKey();
            if (PotionUtil.getVanillaPotionIDs().contains(potionID)) {
                Main.log.warning(potion.getKey() + " is the potion ID of a Vanilla potion. "
                        + "Skipping the potion for now...");
                continue;
            }
            Potion newPotion = new Potion(potionID);

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
                Main.log.warning(name + ChatColor.RESET + " does not have a valid type. "
                        + "Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion for now...");
                continue;
            }
            Material typeM = Material.matchMaterial(typeS);
            if (typeM != Material.POTION && typeM != Material.SPLASH_POTION && typeM != Material.LINGERING_POTION) {
                Main.log.warning(typeS + " is not a valid type for " + name + ChatColor.RESET
                        + ". Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion for now...");
                continue;
            }
            newPotion.setType(typeM);

            // Colour
            Map<String, Object> colours = ((MemorySection) attributes.get("colour")).getValues(false);
            int redI = (int) colours.get("red");
            int greenI = (int) colours.get("green");
            int blueI = (int) colours.get("blue");
            newPotion.setColour(new Colour(redI, greenI, blueI));

            // Effects
            List<PotionEffectSerializable> potionEffects = new ArrayList<>();
            boolean isLingering = typeM == Material.LINGERING_POTION;
            for (Map.Entry<String, Object> effect
                    : (((MemorySection) attributes.get("effects")).getValues(false)).entrySet()) {

                // Effect Type
                String effectTypeS = effect.getKey();
                if (effectTypeS.equalsIgnoreCase("none")) break;
                PotionEffectType effectTypeT = PotionEffectType.getByName(effectTypeS);
                if (effectTypeT == null) {
                    Main.log.warning(effectTypeS + " is not a valid effect for " + name + ChatColor.RESET
                            + ". Skipping the effect for now...");
                    continue;
                }

                // Effect Duration
                Map<String, Object> effectInfo = ((MemorySection) effect.getValue()).getValues(false);
                int effectDurationI = (int) effectInfo.get("duration"); // seconds
                int durationTicks = isLingering ? 80 * effectDurationI : 20 * effectDurationI;
                if (effectDurationI < 1 || (isLingering && effectDurationI > MagicNumber.lingeringPotionMaxDuration)
                        || (!isLingering && effectDurationI > MagicNumber.regularPotionMaxDuration)) {
                    if (isLingering) {
                        Main.log.warning(name + ChatColor.RESET
                                + " must have an effect duration between 1 and 26,843,545 for "
                                + effectTypeS + ". Skipping the effect for now...");
                    } else {
                        Main.log.warning(name + ChatColor.RESET
                                + " must have an effect duration between 1 and 107,374,182 for "
                                + effectTypeS + ". Skipping the effect for now...");
                    }
                    continue;
                }

                // Effect Amplifier
                int effectAmplifierI = (int) effectInfo.get("amplifier");
                if (effectAmplifierI < 0 || effectAmplifierI > PotionUtil.maxAmp(effectTypeS)) {
                    Main.log.warning(name + ChatColor.RESET + " must have an effect amplifier from 0 to "
                            + PotionUtil.maxAmp(effectTypeS) + " for " + effectTypeS
                            + ". Skipping the effect for now...");
                    continue;
                }

                PotionEffectSerializable potionEffect = new PotionEffectSerializable(effectTypeT, durationTicks, effectAmplifierI);
                potionEffects.add(potionEffect);
            }
            newPotion.setEffects(potionEffects);

            // Recipes
            List <String> vanillaPotionIDs = PotionUtil.getVanillaPotionIDs();
            List <PotionRecipe> potionRecipes = new ArrayList<>();
            for (Map.Entry<String, Object> recipe
                    : (((MemorySection) attributes.get("recipes")).getValues(false)).entrySet()) {
                Map<String, Object> recipeInfo = ((MemorySection) recipe.getValue()).getValues(false);

                String ingredientName = (String) recipeInfo.get("ingredient");
                Material ingredient = Material.matchMaterial(ingredientName);
                String base = (String) recipeInfo.get("base");

                if (ingredient == null) {
                    Main.log.warning(ingredientName + " is not a valid ingredient for a recipe of " + name
                            + ChatColor.RESET + ". Skipping the recipe for now...");
                    continue;
                }

                if (!potions.containsKey(base) && !vanillaPotionIDs.contains(base)) {
                    Main.log.warning(base + " is not a valid base potion for a recipe of " + name
                            + ChatColor.RESET + ". Skipping the recipe for now...");
                    continue;
                }

                if (base.equals(potionID)) {
                    Main.log.warning(name + ChatColor.RESET + " cannot use itself as a base in a recipe. "
                            + "Skipping the recipe for now...");
                    continue;
                }

                // TODO check that the recipe does not already exist for another potion. if it does, skip the recipe

                potionRecipes.add(new PotionRecipe(ingredient, base, name));
            }
            newPotion.setRecipes(potionRecipes);

            customPotions.add(newPotion);
            Main.log.info("Successfully added " + name + ChatColor.RESET + " to the game.");

        }
        return customPotions;
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

}
