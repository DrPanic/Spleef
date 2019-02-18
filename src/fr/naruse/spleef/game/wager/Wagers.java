package fr.naruse.spleef.game.wager;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class Wagers {
    private SpleefPlugin pl;
    private List<Wager> wagers = Lists.newArrayList();
    private HashMap<Player, Wager> wagerOfPlayer = new HashMap<>();
    public Wagers(SpleefPlugin pl){
        this.pl = pl;
    }

    public boolean createWager(Player p, Player p2){
        if(wagerOfPlayer.containsKey(p) || wagerOfPlayer.containsKey(p2)){
            return false;
        }
        Wager wager = new Wager(pl, p, p2);
        wagers.add(wager);
        wagerOfPlayer.put(p, wager);
        wagerOfPlayer.put(p2, wager);
        Bukkit.getPluginManager().registerEvents(wager, pl);
        wager.init();
        return true;
    }

    public boolean deleteWager(Wager wager){
        if(!wagers.contains(wager)){
            return false;
        }
        wager.stop();
        wagers.remove(wager);
        return true;
    }

    public boolean loseWager(Player p){
        if(!wagerOfPlayer.containsKey(p)){
            return false;
        }
        Wager wager = wagerOfPlayer.get(p);
        if(wager.getLost() == null){
            wager.setLost(p);
        }else{
            wager.win(p);
        }
        return true;
    }

    public void disable(){
        for(int i = 0; i < wagers.size(); i++){
            Wager wager = wagers.get(i);
            wager.decline();
        }
    }

    public boolean hasWager(Player p){
        return wagerOfPlayer.containsKey(p);
    }

    public HashMap<Player, Wager> getWagerOfPlayer() {
        return wagerOfPlayer;
    }

    public List<Wager> getWagers() {
        return wagers;
    }

}
