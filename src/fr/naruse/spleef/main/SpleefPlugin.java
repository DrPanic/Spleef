package fr.naruse.spleef.main;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import fr.naruse.spleef.cmd.SpleefCommands;
import fr.naruse.spleef.event.Listeners;
import fr.naruse.spleef.game.spleef.Spleefs;
import fr.naruse.spleef.game.duel.Duels;
import fr.naruse.spleef.game.wager.Wagers;
import fr.naruse.spleef.util.Logs;
import fr.naruse.spleef.util.board.Holograms;
import fr.naruse.spleef.util.config.Configurations;
import fr.naruse.spleef.util.support.OtherPluginSupport;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SpleefPlugin extends JavaPlugin {
    public static SpleefPlugin INSTANCE;
    public Spleefs spleefs;
    public Configurations configurations;
    public WorldEditPlugin worldEditPlugin;
    public OtherPluginSupport otherPluginSupport;
    public Wagers wagers;
    public Duels duels;
    public Holograms holograms;
    @Override
    public void onEnable(){
        super.onEnable();
        this.INSTANCE = this;
        this.worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        this.otherPluginSupport = new OtherPluginSupport();
        saveConfig();
        this.configurations = new Configurations(this);
        this.holograms = new Holograms(this);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Logs logs = new Logs();
                spleefs = new Spleefs(INSTANCE);
                wagers = new Wagers(INSTANCE);
                duels = new Duels(INSTANCE);
                getCommand("spleef").setExecutor(new SpleefCommands(INSTANCE));
                Bukkit.getPluginManager().registerEvents(new Listeners(INSTANCE), INSTANCE);
                Bukkit.getPluginManager().registerEvents(duels, INSTANCE);
                logs.stop();
            }
        });
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
        holograms.removeLeaderBoard();
    }
}
