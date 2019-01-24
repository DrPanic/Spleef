package fr.naruse.spleef.game.spleef;

import fr.naruse.spleef.game.SpleefGameMode;
import fr.naruse.spleef.game.wager.Wager;
import fr.naruse.spleef.main.Main;
import fr.naruse.spleef.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class SpleegSpleef extends Spleef implements Listener {
    public SpleegSpleef(Main pl, String name, Location spleefLoc, Location spleefSpawn, Location spleefLobby, int min, int max, boolean isOpen) {
        super(pl, SpleefGameMode.SPLEGG, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen);
        Bukkit.getPluginManager().registerEvents(this, getMain());
    }

    @Override
    public void runScheduler() {
        this.runNormalScheduler();
    }

    @Override
    public void removePlayer(Player p) {
        if(getPlayerInGame().contains(p)){
            getPlayerInGame().remove(p);
            p.teleport(getSpleefSpawn());
            p.getInventory().clear();
            updateSigns();
            updateScoreboards();
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    @Override
    public boolean addPlayer(Player p) {
        if(!getPlayerInGame().contains(p)){
            if(getGame().GAME){
                p.sendMessage(getNAME()+"§c "+Message.IN_GAME.getMessage());
                return false;
            }
            if(!p.isOp()){
                if(getPlayerInGame().size() >= getMax()){
                    p.sendMessage(getNAME()+"§c "+Message.FULL_GAME.getMessage());
                    return false;
                }
            }
            runNormalJoin(p);
            getPlayerInGame().add(p);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            ItemStack item = new ItemStack(Material.MAGMA_CREAM);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§c"+Message.LEAVE_THIS_GAME.getMessage());
            item.setItemMeta(meta);
            p.getInventory().setItem(8, item);
            sendMessage(getNAME()+" §6"+p.getName()+"§a "+Message.JOINED_THE_GAME.getMessage());
            updateSigns();
            updateScoreboards();
            p.setScoreboard(getScoreboardSign().getScoreboard());
            p.getInventory().setHeldItemSlot(1);
            if(getMain().wagers.getWagerOfPlayer().containsKey(p)){
                Wager wager =  getMain().wagers.getWagerOfPlayer().get(p);
                if(!getPlayerInGame().contains(wager.getPlayer1())){
                    getMain().spleefs.addPlayer(wager.getPlayer1(), this);
                }
                if(!getPlayerInGame().contains(wager.getPlayer2())){
                    getMain().spleefs.addPlayer(wager.getPlayer2(), this);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateSigns(Sign sign) {
        if(!isOpen()){
            sign.setLine(0, "§c§l[§5"+getName()+"§c§l]");
            sign.setLine(1, "");
            sign.setLine(2, Message.SignColorTag.CLOSE_LINE2.getColorTag()+Message.SPLEEF_CLOSED.getMessage());
            sign.setLine(3, "");
            sign.update();
        }else{
            if(getGame().WAIT){
                sign.setLine(0, "§c§l[§5"+getName()+"§c§l]");
                sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_2.getColorTag()+getPlayerInGame().size()+"/"+getMax());
                if(getPlayerInGame().size() >= getMin()){
                    sign.setLine(2, Message.SignColorTag.OPEN_WAIT_LINE3_0.getColorTag()+Message.READY.getMessage());
                }else{
                    sign.setLine(2, Message.SignColorTag.OPEN_WAIT_LINE3_1.getColorTag()+(getMin()-getPlayerInGame().size())+" "+Message.MISSING.getMessage());
                }
                sign.setLine(3, Message.SignColorTag.OPEN_GAME_LINE4_OTHER.getColorTag()+" "+getGameMode().getName()+" Mode");
                sign.update();
            }else if(getGame().GAME){
                sign.setLine(0, "§c§l[§5"+getName()+"§c§l]");
                sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_2.getColorTag()+getPlayerInGame().size()+"/"+getMax());
                sign.setLine(2, Message.SignColorTag.OPEN_GAME_LINE4_NORMAL.getColorTag()+Message.IN_GAME.getMessage());
                sign.setLine(3, Message.SignColorTag.OPEN_GAME_LINE4_OTHER.getColorTag()+" "+getGameMode().getName()+" Mode");
                sign.update();
            }
        }
    }

    @Override
    public void start() {
        sendMessage(getNAME()+" §a"+Message.GAME_START.getMessage());
        getGame().WAIT = false;
        getGame().GAME = true;
        for(Player p : getPlayerInGame()){
            p.teleport(getSpleefLoc());
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*5, 1, false, false));
            if(new Random().nextBoolean()){
                if(new Random().nextBoolean()){
                    p.setVelocity(new Vector(-0.5F, 0.5F, 0.5F));
                }else{
                    p.setVelocity(new Vector(-0.5F, 0.5F, -0.5F));
                }
            }else{
                if(new Random().nextBoolean()){
                    p.setVelocity(new Vector(-0.5F, 0.5F, -0.5F));
                }else{
                    p.setVelocity(new Vector(0.5F, 0.5F, -0.5F));
                }
            }
        }
        getScoreboardSign().getObjective().setDisplayName(getNAME());
        Bukkit.getScheduler().scheduleSyncDelayedTask(getMain(), new Runnable() {
            @Override
            public void run() {
                for(Player p : getPlayerInGame()){
                    p.getInventory().addItem(new ItemStack(Material.EGG, 16*8));
                }
            }
        },20*5);
    }

    @Override
    public void restart(boolean notOnDisable) {
        this.runNormalRestart(notOnDisable);
    }

    @Override
    public void updateScoreboards() {
        this.runNormalUpdateScoreboards();
    }

    @EventHandler
    public void hitEvent(ProjectileHitEvent e){
        if(!(e.getEntity().getShooter() instanceof Player)){
           return;
        }
        Projectile projectile = e.getEntity();
        if(!(projectile instanceof Egg)){
           return;
        }
        if(e.getHitBlock() == null) {
            return;
        }
        if(e.getHitBlock().getType().toString().contains("SNOW") && getGame().GAME){
            getBlocks().add(e.getHitBlock());
            getBlocksOfRegionVerif().remove(e.getHitBlock());
            e.getHitBlock().setType(Material.AIR);
        }else{
            Material m = e.getHitBlock().getType();
            byte data = e.getHitBlock().getData();
            Bukkit.getScheduler().scheduleSyncDelayedTask(getMain(), new Runnable() {
                @Override
                public void run() {
                    e.getHitBlock().setType(m);
                    e.getHitBlock().setData(data);
                }
            }, 1);
        }
    }

    @EventHandler
    public void entitySpawnEvent(EntitySpawnEvent e){
        if(e.getEntity() instanceof Chicken){
            if(getSpleefSpawn().distance(e.getEntity().getLocation()) <= 100 && getGame().GAME){
                e.setCancelled(true);
                e.getEntity().remove();
            }
        }
    }
}
