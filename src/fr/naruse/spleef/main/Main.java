package fr.naruse.spleef.main;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import fr.naruse.spleef.cmd.SpleefCommands;
import fr.naruse.spleef.event.Listeners;
import fr.naruse.spleef.game.spleef.Spleefs;
import fr.naruse.spleef.game.duel.Duels;
import fr.naruse.spleef.game.wager.Wagers;
import fr.naruse.spleef.util.Logs;
import fr.naruse.spleef.util.config.Configurations;
import fr.naruse.spleef.util.support.OtherPluginSupport;
import fr.naruse.spleef.util.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main instance;
    public Spleefs spleefs;
    public Configurations configurations;
    public WorldEditPlugin worldEditPlugin;
    public OtherPluginSupport otherPluginSupport;
    public Wagers wagers;
    public Duels duels;
    @Override
    public void onEnable(){
        super.onEnable();
        Logs logs = new Logs();
        this.instance = this;
        if(new Updater(this).update()){
            logs.stop();
            return;
        }
        this.worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        this.otherPluginSupport = new OtherPluginSupport();
        saveConfig();
        this.configurations = new Configurations(this);
        this.spleefs = new Spleefs(this);
        this.wagers = new Wagers(this);
        this.duels = new Duels(this);
        getCommand("spleef").setExecutor(new SpleefCommands(this));
        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
        Bukkit.getPluginManager().registerEvents(duels, this);
        logs.stop();
    }

    @Override
    public void onDisable(){
        super.onDisable();
        if(spleefs != null){
            spleefs.onDisable();
        }
        if(wagers != null){
            wagers.disable();
        }
    }
}
