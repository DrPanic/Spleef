package fr.naruse.spleef.event;

import fr.naruse.spleef.main.Main;
import fr.naruse.spleef.game.spleef.Spleef;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class Listeners implements Listener {
    private Main pl;
    public Listeners(Main main) {
        this.pl = main;
    }

    @EventHandler
    public void interact(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(e.getClickedBlock() == null){
            return;
        }
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK){
            if(p.isOp()){
                return;
            }
        }
        if(!(e.getClickedBlock().getState() instanceof Sign)){
            return;
        }
        Sign sign  = (Sign) e.getClickedBlock().getState();
        for(Spleef spleef : pl.spleefs.getSpleefs()){
            if(sign.getLine(0).equalsIgnoreCase("§c§l[§5"+spleef.getName()+"§c§l]")){
                pl.spleefs.addPlayer(p, spleef);
                e.setCancelled(true);
                break;
            }
        }
        if(!p.isOp()){
            return;
        }
        if(sign.getLine(0).equalsIgnoreCase("-!s!-")  && sign.getLine(3).equalsIgnoreCase("-!s!-")){
            if(sign.getLine(1).equalsIgnoreCase(sign.getLine(2))){
                for(Spleef spleef : pl.spleefs.getSpleefs()){
                    if(spleef.getName().equalsIgnoreCase(sign.getLine(1))){
                        sign.setLine(0, "§c§l[§5"+spleef.getName()+"§c§l]");
                        sign.update();
                        spleef.registerNewSigns(p.getWorld());
                        return;
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void command(PlayerCommandPreprocessEvent e){
        if(pl.spleefs.hasSpleef(e.getPlayer())){
            List<String> commands = pl.configurations.getCommands().getConfig().getStringList("commands");
            if(commands.contains(e.getMessage().split(" ")[0].replace("/", ""))){
                e.setCancelled(true);
            }
        }
    }
}
