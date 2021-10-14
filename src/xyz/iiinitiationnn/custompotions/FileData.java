package xyz.iiinitiationnn.custompotions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.ChatColor;
import xyz.iiinitiationnn.custompotions.utils.PotionUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileData {
    private Main pluginInstance;
    private String filePath;
    private List<Potion> potionCache;

    public FileData(Main pluginInstance) {
        this.pluginInstance = pluginInstance;
        this.filePath = this.pluginInstance.getDataFolder().getAbsolutePath() + "/potions.json";
        reloadData();
    }

    public void reloadData() {
        // Save default file if it does not exist
        File file = new File(this.filePath);
        if (!file.exists()) {
            try {
                Writer writer = new FileWriter(this.filePath);
                writer.write("[]");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Refresh cache
        readData();
    }

    private void readData() {
        List<Potion> potions = new ArrayList<>();
        File file = new File(this.filePath);

        try {
            Reader reader = new FileReader(file);
            List<Potion> all = Arrays.asList(new Gson().fromJson(reader, Potion[].class));

            List<String> vanillaPotionIds = PotionUtil.getVanillaPotionIds();
            for (Potion potion : all) {
                String name = potion.getName();

                // Potion ID
                String id = potion.getPotionId();
                if (Objects.equals(id, "NoIdError")) {
                    Main.logWarning("A potion is missing its ID. Skipping the potion for now...");
                    continue;
                } else if (vanillaPotionIds.contains(id)) {
                    Main.logWarning(id + " is the potion ID of a Vanilla potion. Skipping the potion for now...");
                    continue;
                }

                // Type
                if (!PotionUtil.isPotion(potion.getType())) {
                    Main.logWarning(name + ChatColor.RESET + " does not have a valid type. " +
                        "Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion for now...");
                    continue;
                }

                // Recipes
                List<PotionRecipe> validRecipes = new ArrayList<>();
                for (PotionRecipe potionRecipe: potion.getRecipes()) {
                    String base = potionRecipe.getBase();
                    if (all.stream().noneMatch(p -> Objects.equals(p.getPotionId(), base)) && !vanillaPotionIds.contains(base)) {
                        Main.logWarning(base + " is not a valid base potion for a recipe of " + name
                            + ChatColor.RESET + ". Skipping the recipe for now...");
                        continue;
                    }

                    if (Objects.equals(id, base)) {
                        Main.logWarning(name + ChatColor.RESET + " cannot use itself as a base in a recipe. "
                            + "Skipping the recipe for now...");
                        continue;
                    }

                    // TODO check that the recipe does not already exist for another potion. if it does, skip the recipe

                    validRecipes.add(potionRecipe);
                }
                potion.setRecipes(validRecipes);

                Main.logInfo("Successfully added " + name + ChatColor.RESET + " to the game.");

                // All valid
                potions.add(potion);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            Main.logSevere("A potion had an invalid number input. Please ensure all numbers are positive 32-bit integers. Skipping ALL potions for now...");
        }
        this.potionCache = potions;
    }

    public List<Potion> getCustomPotions() {
        return new ArrayList<>(this.potionCache);
    }

    public void writeData(Potion newPotion) {
        reloadData();
        try {
            List<Potion> potions = getCustomPotions();
            potions.removeIf(potion -> Objects.equals(potion.getPotionId(), newPotion.getPotionId()));
            potions.add(newPotion);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Writer writer = new FileWriter(this.filePath, false);
            gson.toJson(potions, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadData();
    }

}
