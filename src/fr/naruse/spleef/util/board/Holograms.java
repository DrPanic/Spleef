package fr.naruse.spleef.util.board;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.Main;
import fr.naruse.spleef.util.Message;
import fr.naruse.spleef.util.Reflections;
import fr.naruse.spleef.util.SpleefPlayerStatistics;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Holograms extends BukkitRunnable implements Listener {
    private Main pl;
    private List<ArmorStand> entities = Lists.newArrayList();
    private ArmorStand[] armorStands;
    private Location location;
    private HashMap<OfflinePlayer, Long> playerPoints = new HashMap<>();
    private boolean isRunning = false;
    public Holograms(Main main) {
        this.pl = main;
        if(pl.getConfig().getString("holograms.location.x") == null){
            return;
        }
        if(!pl.getConfig().getBoolean("holograms.enable")){
            return;
        }
        this.location = new Location(Bukkit.getWorld(pl.getConfig().getString("holograms.location.world")), pl.getConfig().getDouble("holograms.location.x"),
                pl.getConfig().getDouble("holograms.location.y"), pl.getConfig().getDouble("holograms.location.z"));
        if(location == null){
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, pl);
        for(int i = 0; i != 11; i++){
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            try {
                Reflections.setNoGravity(armorStand, true);
                Reflections.setInvisible(armorStand, true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            armorStand.setCustomNameVisible(false);
            armorStand.setCustomName("§a");
            entities.add(armorStand);
            location.add(0, 0.3, 0);
        }
        armorStands = entities.toArray(new ArmorStand[] {});
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            if(pl.getConfig().getString(p.getUniqueId().toString()) != null){
                long points = pl.getConfig().getInt(p.getUniqueId().toString());
                playerPoints.put(p, points);
            }
        }
        armorStands[10].setCustomName("§6"+ Message.SPLEEF_PLAYER_RANKING.getMessage());
        armorStands[10].setCustomNameVisible(true);
        this.runTaskTimer(pl, 20*5, 20*5);
        this.run();
        this.isRunning = true;
    }

    @Override
    public void run() {
        addPlayers();
        HashMap<Long, List<OfflinePlayer>> placeAndPlayer = getLeaderBoard();
        int count = 1, count2 = 10;
        List<ArmorStand> armorStandUsed = Lists.newArrayList();
        for(int o = placeAndPlayer.size()-1; o >= 0; o--){
            String name = "§d-§6"+count+":,";
            long i = intList.get(o);
            if(placeAndPlayer.containsKey(i)){
                for(OfflinePlayer p : placeAndPlayer.get(i)){
                    if(p != null){
                        name += ", §a"+p.getName()+" §e(§6Wins: "+getSpleefPlayer(p).getWins()+"§e, §6Loses: "
                                +getSpleefPlayer(p).getLoses()+"§e)";
                    }
                }
                name = name.replace(",,", "");
                armorStands[count2-1].setCustomName(name);
                armorStands[count2-1].setCustomNameVisible(true);
                if(!armorStandUsed.contains(armorStands[count2-1])){
                    armorStandUsed.add(armorStands[count2-1]);
                }
                if(count == 10 || count2 == 1){
                    break;
                }
                count++;
                count2--;
            }
        }
        for(ArmorStand as : entities){
            if(!armorStandUsed.contains(as) && armorStands[10] != as){
                if(as.isCustomNameVisible()){
                    as.setCustomNameVisible(false);
                }
            }
        }
        intList.clear();
    }

    private List<Long> intList = Lists.newArrayList();
    private List<String> nameUsed = Lists.newArrayList();
    private HashMap<Long, List<OfflinePlayer>> getLeaderBoard(){
        HashMap<Long, List<OfflinePlayer>> pAndP = new HashMap<>();
        for(OfflinePlayer p : playerPoints.keySet()){
            if(!nameUsed.contains(p.getName())){
                long lives = playerPoints.get(p);
                intList.add(lives);
                if(!pAndP.containsKey(lives)){
                    pAndP.put(lives, Lists.newArrayList());
                }
                pAndP.get(lives).add(p);
                nameUsed.add(p.getName());
            }
        }
        Collections.sort(intList);
        nameUsed.clear();
        HashMap<Long, List<OfflinePlayer>> placeAndPlayer = new HashMap<>();
        for(long i : intList){
            placeAndPlayer.put(i, pAndP.get(i));
        }
        return placeAndPlayer;
    }

    public void addPlayerPoints(OfflinePlayer p, long points){
        if(playerPoints.containsKey(p)){
            playerPoints.remove(p);
        }
        playerPoints.put(p, points);
    }

    public void removeLeaderBoard(){
        if(isRunning){
            this.cancel();
        }
        for(ArmorStand as : entities){
            as.remove();
        }
    }

    public void addPlayers(){
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            if(getSpleefPlayer(p).getWins() != 0){
                addPlayerPoints(p, getSpleefPlayer(p).getWins());
            }
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent e){
        if(entities.contains(e.getEntity())){
            e.setCancelled(true);
        }
    }

    private HashMap<OfflinePlayer, SpleefPlayerStatistics> spleefPlayerHashMap = new HashMap<>();
    private SpleefPlayerStatistics getSpleefPlayer(OfflinePlayer p) {
        if(spleefPlayerHashMap.containsKey(p)){
            spleefPlayerHashMap.get(p).refreshStatisticFromConfig();
            return spleefPlayerHashMap.get(p);
        }
        SpleefPlayerStatistics spleefPlayerStatistics = new SpleefPlayerStatistics(pl, p.getName());
        spleefPlayerHashMap.put(p, spleefPlayerStatistics);
        return spleefPlayerStatistics;
    }
}
