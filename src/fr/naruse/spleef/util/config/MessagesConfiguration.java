package fr.naruse.spleef.util.config;

import fr.naruse.spleef.main.Main;
import fr.naruse.spleef.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class MessagesConfiguration {
    private Main pl;
    private File messagesFile;
    private FileConfiguration messages;
    public MessagesConfiguration(Main main) {
        this.pl = main;
        createConfig();
    }

    private void createConfig(){
        messagesFile = new File(pl.getDataFolder(), "messages.yml");
        messages = new YamlConfiguration();
        try{
            if(!messagesFile.exists()){
                messagesFile.createNewFile();
            }
            Reader defConfigStream;
            defConfigStream = new InputStreamReader(pl.getResource(langFileName()), "UTF8");
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            messages.setDefaults(defConfig);
        } catch (UnsupportedEncodingException e) {
            Bukkit.getConsoleSender().sendMessage("§3[Spleef] §cThere is an error with the configuration Messages.yml. You should perform a reload.");
            e.printStackTrace();
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§3[Spleef] §cThere is an error with the configuration Messages.yml. You should perform a reload.");
            e.printStackTrace();
        }
        try{
            messages.load(messagesFile);
        }catch(Exception e){
            e.printStackTrace();
        }
        saveConfig();
        Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
            @Override
            public void run() {
                setDefault();
            }
        },20);
    }

    public void saveConfig(){
        try {
            messages.save(messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String langFileName(){
        if(pl.getConfig().getString("lang").equalsIgnoreCase("spanish")){
            return "languages/spanish.yml";
        }
        return "languages/messages.yml";
    }

    public FileConfiguration getConfig(){
        return this.messages;
    }

    private void setDefault() {
        for(Message msg : Message.values()){
            if(messages.getString(msg.getPath()) == null){
                messages.set(msg.getPath(), msg.getEnglishMessage());
            }else{
                messages.set(msg.getPath(), messages.getString(msg.getPath()));
            }
        }
        for(Message.SignColorTag sct : Message.SignColorTag.values()){
            if(messages.getString(sct.getPath()) == null){
                messages.set(sct.getPath(), sct.getColorTag());
            }else{
                messages.set(sct.getPath(), messages.getString(sct.getPath()));
            }
        }
        saveConfig();
    }

    public void clearConfiguration(){
        messagesFile.delete();
        createConfig();
    }
}