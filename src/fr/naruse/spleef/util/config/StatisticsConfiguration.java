package fr.naruse.spleef.util.config;

import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class StatisticsConfiguration {
    private SpleefPlugin pl;
    private File statisticsFile;
    private FileConfiguration statistics;
    public StatisticsConfiguration(SpleefPlugin spleefPlugin) {
        this.pl = spleefPlugin;
        createConfig();
    }

    private void createConfig(){
        statisticsFile = new File(pl.getDataFolder(), "statistics.yml");
        statistics = new YamlConfiguration();
        try {
            if(!statisticsFile.exists()){
                statisticsFile.createNewFile();
            }
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§3[Spleef] §C There is an error with the configuration Statistics.yml. You should perform a reload.");
            e.printStackTrace();
        }
        try{
            statistics.load(statisticsFile);
        }catch(Exception e){
            e.printStackTrace();
        }
        saveConfig();
    }

    public void saveConfig(){
        try {
            statistics.save(statisticsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig(){
        return this.statistics;
    }
}
