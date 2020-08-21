package custompotions;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class data {

    private main pluginInstance;
    private File dataFile = null;
    private FileConfiguration dataConfig = null;

    public data(main pluginInstance) {
        this.pluginInstance = pluginInstance;
        saveDefaultData();
    }

    public void reloadData() {
        if (this.dataFile == null) {
            this.dataFile = new File(this.pluginInstance.getDataFolder(), "potions.yml");
        }

        this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
        InputStream defaultStream = this.pluginInstance.getResource("potions.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getData() {
        if (this.dataConfig == null) {
            reloadData();
        }
        return this.dataConfig;
    }

    public void saveData() {
        if (this.dataConfig == null || dataFile == null) {
            return;
        }
        try {
            this.getData().save(this.dataFile);
        } catch (Exception e) {
            pluginInstance.getLogger().log(Level.SEVERE, "Failed to save data file to " + this.dataFile, e);
        }
    }

    public void saveDefaultData() {
        if (this.dataFile == null) {
            this.dataFile = new File(this.pluginInstance.getDataFolder(), "potions.yml");
        }

        if (!this.dataFile.exists()) {
            this.pluginInstance.saveResource("potions.yml", false);
        }

    }

}
