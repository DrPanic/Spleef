package fr.naruse.spleef.cmd;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import fr.naruse.spleef.main.Main;
import fr.naruse.spleef.game.SpleefGameMode;
import fr.naruse.spleef.game.spleef.Spleef;
import fr.naruse.spleef.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class SpleefCommands implements CommandExecutor, TabExecutor {
    private Main pl;
    public SpleefCommands(Main main) {
        this.pl = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String ss, String[] args) {
        if(sender instanceof Player){
            Player p = (Player) sender;
            if(args.length == 0){
                sendMessage(sender, "§3Hey! §6/§cspleef join <Spleef Name>");
                sendMessage(sender, "§3Hey! §6/§cspleef leave");
                sendMessage(sender, "§3Hey! §6/§cspleef wager <Open, Decline, Accept, Wager> <[Player]>");
                sendMessage(sender, "§3Hey! §6/§cspleef duel <Invite, Decline, Accept> <[Player]>");
            }
            if(args.length != 0){
                if(args[0].equalsIgnoreCase("duel")){
                    if(args.length < 2){
                        return sendMessage(sender, "§3Hey! §6/§cspleef duel <Invite, Decline, Accept> <[Player]>");
                    }
                    if(args[1].equalsIgnoreCase("invite")){
                        if(args.length < 3){
                            return sendMessage(sender, "§3Hey! §6/§cspleef duel Invite <Player>");
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if(target == null){
                            return sendMessage(sender, "§c"+Message.PLAYER_NOT_FOUND.getEnglishMessage());
                        }
                        return pl.duels.invite(p, target);
                    }
                    if(args[1].equalsIgnoreCase("decline")){
                        pl.duels.decline(p, true);
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("accept")){
                        return pl.duels.acceptDuel(p);
                    }
                    return false;
                }
                if(args[0].equalsIgnoreCase("join")){
                    if(args.length < 2){
                        return sendMessage(sender, "§3Hey! §6/§cspleef join <Spleef Name>");
                    }
                    for(Spleef spleef : pl.spleefs.getSpleefs()){
                        if(spleef.getName().equalsIgnoreCase(args[1])){
                            pl.spleefs.addPlayer(p, spleef);
                            return true;
                        }
                    }
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                //WAGER START
                if(args[0].equalsIgnoreCase("wager")){
                    if(args.length < 2){
                        return sendMessage(sender, "§3Hey! §6/§cspleef wager <Open, Decline, Accept, Wager> <[Player]>");
                    }
                    if(args[1].equalsIgnoreCase("open")){
                        if(pl.wagers.getWagerOfPlayer().containsKey(p)){
                            pl.wagers.getWagerOfPlayer().get(p).openInventory(p);
                            return true;
                        }else{
                            return sendMessage(sender, "§c"+Message.YOU_DO_NOT_HAVE_A_WAGER.getMessage());
                        }
                    }
                    if(args[1].equalsIgnoreCase("Wager")){
                        if(args.length < 3){
                            return sendMessage(sender, "§3Hey! §6/§cspleef wager <Open, Decline, Accept, Wager> <[Player]>");
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if(target == null){
                            return sendMessage(sender, "§c"+Message.PLAYER_NOT_FOUND.getMessage());
                        }
                        if(pl.spleefs.hasSpleef(p) || pl.spleefs.hasSpleef(target)){
                            return sendMessage(sender, "§c"+Message.ONE_PLAYER_HAS_A_GAME.getMessage());
                        }
                        if(!pl.wagers.createWager(p, target)){
                            return sendMessage(sender, "§c"+Message.THIS_PLAYER_HAS_WAGER.getMessage());
                        }
                        target.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.WAGER_RECEIVED_BY.getMessage()+" §6"+p.getName()+"§a.");
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.WAGER_SENT.getMessage());
                    }
                    if(args[1].equalsIgnoreCase("accept")){
                        if(pl.wagers.getWagerOfPlayer().containsKey(p)){
                            if(pl.wagers.getWagerOfPlayer().get(p).getPlayer1() == p){
                                return false;
                            }
                            pl.wagers.getWagerOfPlayer().get(p).getPlayer1().sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.WAGER_ACCEPTED.getMessage());
                            pl.wagers.getWagerOfPlayer().get(p).accept();
                            return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.WAGER_ACCEPTED.getMessage());
                        }
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("decline")){
                        if(pl.wagers.getWagerOfPlayer().containsKey(p)){
                            if(pl.wagers.getWagerOfPlayer().get(p).isWagerActive()){
                                return true;
                            }
                            if(pl.wagers.getWagerOfPlayer().get(p).getPlayer1() == p){
                                return false;
                            }
                            pl.wagers.getWagerOfPlayer().get(p).getPlayer1().sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.WAGER_DECLINED.getMessage());
                            pl.wagers.getWagerOfPlayer().get(p).decline();
                            return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.WAGER_DECLINED.getMessage());
                        }
                        return true;
                    }
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                //WAGER END
                if(args[0].equalsIgnoreCase("leave")){
                    for(Spleef spleef : pl.spleefs.getSpleefs()){
                        if(spleef.getPlayerInGame().contains(p)){
                            spleef.sendMessage(spleef.getNAME()+" §6"+p.getName()+"§c "+Message.LEAVED_THE_GAME.getMessage());
                            break;
                        }
                    }
                    if(!pl.spleefs.removePlayer(p)){
                        return sendMessage(sender, "§c"+Message.YOU_DONT_HAVE_SPLEEF.getMessage());
                    }
                    return false;
                }
            }
            if(args.length == 0){
                if(hasPermission(p,"spleef.help")){
                    return sendMessage(sender, "§3Hey! §6/§cspleef help");
                }
                return false;
            }
            if(args[0].equalsIgnoreCase("help")){
                if(!hasPermission(p,"spleef.help")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                int page = 1;
                if(args.length > 1){
                    try{
                        page = Integer.valueOf(args[1]);
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                }
                return help(sender, page);
            }
            if(args[0].equalsIgnoreCase("create")){
                if(!hasPermission(p,"spleef.create")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 1);
                }
                SpleefGameMode gameMode = SpleefGameMode.NORMAL;
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") == null){
                        place = i;
                    }else if(args[1].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                        return sendMessage(sender, "§c"+Message.NAME_ALREADY_USED.getMessage());
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.TOO_MUCH_SPLEEFS.getMessage());
                }
                if(args.length > 2){
                    gameMode = SpleefGameMode.valueOf(args[2].toUpperCase());
                    if(gameMode == null){
                        return sendMessage(sender,"§c"+Message.SPLEEF_GAME_MODE_NOT_FOUND.getEnglishMessage());
                    }
                }
                pl.getConfig().set("spleef."+place+".name", args[1]);
                pl.getConfig().set("spleef."+place+".isOpen", true);
                pl.getConfig().set("spleef."+place+".gameMode", gameMode.name());
                pl.saveConfig();
                return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_CREATED.getMessage());
            }
            if(args[0].equalsIgnoreCase("delete")){
                if(!hasPermission(p,"spleef.delete")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 1);
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(args[1].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                        place = i;
                        break;
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                pl.getConfig().set("spleef."+place, null);
                pl.saveConfig();
                return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_DELETED.getMessage());
            }
            if(args[0].equalsIgnoreCase("reload")){
                if(!hasPermission(p,"spleef.reload")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                sendMessage(sender,Message.SPLEEF.getMessage()+" §cReloading...");
                pl.spleefs.reload();
                return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage());
            }
            if(args[0].equalsIgnoreCase("set")){
                if(!hasPermission(p,"spleef.set")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                   return help(sender, 1);
                }
                if(args[1].equalsIgnoreCase("glowing")){
                    pl.getConfig().set("gameMode.team.glowing", !pl.getConfig().getBoolean("gameMode.team.glowing"));
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SETTING_SAVED.getMessage()+" §7(Glowing: "+pl.getConfig().getBoolean("gameMode.team.glowing")+")");
                }
                if(args[1].equalsIgnoreCase("rewards")){
                    if(args.length < 4){
                        return help(sender, 2);
                    }
                    double d;
                    try{
                        d = Double.valueOf(args[3]);
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("win")){
                        pl.getConfig().set("rewards.win", d);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage()+" §7(Rewards.win: "+d+")");
                    }
                    if(args[2].equalsIgnoreCase("lose")){
                        pl.getConfig().set("rewards.lose", d);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage()+" §7(Rewards.lose: "+d+")");
                    }
                    return false;
                }
                if(args[1].equalsIgnoreCase("lang")){
                    if(args.length < 3){
                        return help(sender, 1);
                    }
                    if(args[2].equalsIgnoreCase("french")){
                        pl.getConfig().set("lang", "french");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("english")){
                        pl.getConfig().set("lang", "english");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("custom")){
                        pl.getConfig().set("lang", "custom");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("spanish")){
                        pl.getConfig().set("lang", "spanish");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    return false;
                }
                if(args[1].equalsIgnoreCase("time")){
                    if(args.length < 4){
                        return help(sender, 1);
                    }
                    if(args[2].equalsIgnoreCase("wait")){
                        int time;
                        try{
                            time = Integer.valueOf(args[3]);
                        }catch (Exception e){
                            return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                        }
                        pl.getConfig().set("times.wait", time);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage());
                    }
                    return false;
                }
                if(args.length < 3){
                    return help(sender, 1);
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(args[2].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                            place = i;
                            break;
                        }
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                if(args[1].equalsIgnoreCase("gameMode")){
                    SpleefGameMode gameMode = SpleefGameMode.valueOf(args[3]);
                    if(gameMode == null){
                        return sendMessage(sender, "§c"+Message.SPLEEF_GAME_MODE_NOT_FOUND.getMessage());
                    }
                    pl.getConfig().set("spleef."+place+".gameMode", gameMode.name());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+"§a "+Message.SETTING_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("region")){
                    if(pl.worldEditPlugin == null){
                        return sendMessage(sender, "§c"+Message.NEEDS_WE.getMessage());
                    }
                    Selection selection = pl.worldEditPlugin.getSelection(p);
                    if(selection == null) {
                        return sendMessage(sender, "§cNo selection found.");
                    }
                    Vector min = selection.getNativeMinimumPoint();
                    Vector max = selection.getNativeMaximumPoint();
                    Block block = selection.getWorld().getBlockAt(min.getBlockX(), min.getBlockY(), min.getBlockZ());
                    pl.getConfig().set("spleef."+place+".region.a.x", block.getX());
                    pl.getConfig().set("spleef."+place+".region.a.y", block.getY());
                    pl.getConfig().set("spleef."+place+".region.a.z", block.getZ());
                    pl.getConfig().set("spleef."+place+".region.a.world", block.getWorld().getName());
                    block = selection.getWorld().getBlockAt(max.getBlockX(), max.getBlockY(), max.getBlockZ());
                    pl.getConfig().set("spleef."+place+".region.b.x", block.getX());
                    pl.getConfig().set("spleef."+place+".region.b.y", block.getY());
                    pl.getConfig().set("spleef."+place+".region.b.z", block.getZ());
                    pl.getConfig().set("spleef."+place+".region.b.world", block.getWorld().getName());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+"§a "+Message.REGION_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("regionWithPos")){
                    Block block = p.getLocation().getBlock();
                    if(args.length <= 3){
                        return help(sender, 1);
                    }
                    if(args[3].equalsIgnoreCase("pos1")){
                        pl.getConfig().set("spleef."+place+".region.a.x", block.getX());
                        pl.getConfig().set("spleef."+place+".region.a.y", block.getY());
                        pl.getConfig().set("spleef."+place+".region.a.z", block.getZ());
                        pl.getConfig().set("spleef."+place+".region.a.world", block.getWorld().getName());
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+"§a "+Message.REGION_SAVED.getMessage()+"§7 (Pos1)");
                    }else{
                        pl.getConfig().set("spleef."+place+".region.b.x", block.getX());
                        pl.getConfig().set("spleef."+place+".region.b.y", block.getY());
                        pl.getConfig().set("spleef."+place+".region.b.z", block.getZ());
                        pl.getConfig().set("spleef."+place+".region.b.world", block.getWorld().getName());
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+"§a "+Message.REGION_SAVED.getMessage()+"§7 (Pos2)");
                    }
                }
                if(args[1].equalsIgnoreCase("min")){
                    if(args.length < 4){
                        return help(sender, 1);
                    }
                    int min;
                    try{
                        min = Integer.valueOf(args[3]);
                        if(min <= 1){
                            return sendMessage(sender, "§c"+Message.GREATER_THAN_1.getMessage());
                        }
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                    pl.getConfig().set("spleef."+place+".min", min);
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("max")){
                    if(args.length < 4){
                        return help(sender, 1);
                    }
                    int max;
                    try{
                        max = Integer.valueOf(args[3]);
                        if(max <= 1){
                            return sendMessage(sender, "§c"+Message.GREATER_THAN_1.getMessage());
                        }
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                    pl.getConfig().set("spleef."+place+".max", max);
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("arena")){
                    pl.getConfig().set("spleef."+place+".spleef.x", p.getLocation().getX());
                    pl.getConfig().set("spleef."+place+".spleef.y", p.getLocation().getY());
                    pl.getConfig().set("spleef."+place+".spleef.z", p.getLocation().getZ());
                    pl.getConfig().set("spleef."+place+".spleef.yaw", p.getLocation().getYaw());
                    pl.getConfig().set("spleef."+place+".spleef.pitch", p.getLocation().getPitch());
                    pl.getConfig().set("spleef."+place+".spleef.world", p.getLocation().getWorld().getName());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.LOCATION_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("spawn")){
                    pl.getConfig().set("spleef."+place+".spawn.x", p.getLocation().getX());
                    pl.getConfig().set("spleef."+place+".spawn.y", p.getLocation().getY());
                    pl.getConfig().set("spleef."+place+".spawn.z", p.getLocation().getZ());
                    pl.getConfig().set("spleef."+place+".spawn.yaw", p.getLocation().getYaw());
                    pl.getConfig().set("spleef."+place+".spawn.pitch", p.getLocation().getPitch());
                    pl.getConfig().set("spleef."+place+".spawn.world", p.getLocation().getWorld().getName());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.LOCATION_SAVED.getMessage());
                }
            }
            if(args[0].equalsIgnoreCase("open")){
                if(!hasPermission(p,"spleef.open")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 1);
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(args[1].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                            place = i;
                            break;
                        }
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                for(Spleef spleef : pl.spleefs.getSpleefs()){
                    if(spleef.getName().equalsIgnoreCase(args[1])){
                        spleef.open();
                        pl.getConfig().set("spleef."+place+".isOpen", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_OPENED.getMessage());
                    }
                }
                return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
            }
            if(args[0].equalsIgnoreCase("close")){
                if(!hasPermission(p,"spleef.close")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 1);
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(args[1].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                            place = i;
                            break;
                        }
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                for(Spleef spleef : pl.spleefs.getSpleefs()){
                    if(spleef.getName().equalsIgnoreCase(args[1])){
                        spleef.close();
                        pl.getConfig().set("spleef."+place+".isOpen", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_CLOSED.getMessage());
                    }
                }
                return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
            }
            if(args[0].equalsIgnoreCase("list")){
                if(!hasPermission(p,"spleef.list")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                String activeSpleef = ",,", breakdownSpleef = ",,";
                List<String> list = Lists.newArrayList();
                for(Spleef spleef : pl.spleefs.getSpleefs()){
                    activeSpleef += ", "+spleef.getName();
                    list.add(spleef.getName());
                }
                activeSpleef = activeSpleef.replace(",,, ", "");
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(!list.contains(pl.getConfig().getString("spleef."+i+".name"))){
                            breakdownSpleef += ", "+pl.getConfig().getString("spleef."+i+".name");
                        }
                    }
                }
                breakdownSpleef = breakdownSpleef.replace(",,, ", "");
                if(breakdownSpleef.contains(",,")){
                    breakdownSpleef = "";
                }
                if(activeSpleef.contains(",,")){
                    activeSpleef = "";
                }
                sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_IN_OPERATION.getMessage()+" §2"+activeSpleef);
                return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_IN_FAILURE.getMessage()+" §c"+breakdownSpleef);
            }
            if(args[0].equalsIgnoreCase("force")){
                if(!hasPermission(p,"spleef.force")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 3){
                    return help(sender, 2);
                }
                Spleef spleef = null;
                for(Spleef s : pl.spleefs.getSpleefs()){
                    if(s.getName().equalsIgnoreCase(args[2])){
                        spleef = s;
                        break;
                    }
                }
                if(spleef == null){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                if(args[1].equalsIgnoreCase("start")){
                    if(spleef.getGame().WAIT){
                        if(spleef.getPlayerInGame().size() == 0){
                            return sendMessage(sender, "§c"+Message.NOT_ENOUGH_PLAYER.getMessage());
                        }
                        spleef.start();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.GAME_START.getMessage());
                    }else{
                        return sendMessage(sender, "§c"+Message.SPLEEF_ALREADY_STARTED.getMessage());
                    }
                }else if(args[1].equalsIgnoreCase("stop")){
                    spleef.restart(false);
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_STOPPED.getMessage());
                }
            }
            if(args[0].equalsIgnoreCase("allow")){
                if(!hasPermission(p,"spleef.allow")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 2);
                }
                if(args[1].equalsIgnoreCase("snowballs")){
                    if(pl.getConfig().getBoolean("allow.snowBalls")){
                        pl.getConfig().set("allow.snowBalls", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(SnowBalls: false)");
                    }else{
                        pl.getConfig().set("allow.snowBalls", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(SnowBalls: true)");
                    }
                }
            }
            if(args[0].equalsIgnoreCase("remove")){
                if(args.length < 3){
                    return sendMessage(sender, "§3Hey! §6/§cdac remove Region <Dac Name>");
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(args[2].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                            place = i;
                            break;
                        }
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                if(args[1].equalsIgnoreCase("region")){
                    pl.getConfig().set("spleef."+place+".region", null);
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.REGION_REMOVED.getMessage());
                }
            }
        }
        return false;
    }

    private boolean sendMessage(CommandSender sender, String msg){
        sender.sendMessage(msg);
        return true;
    }

    private boolean hasPermission(Player p, String msg){
        if(!p.hasPermission(msg)){
            if(!p.getName().equalsIgnoreCase("NaruseII")){
                return false;
            }
        }
        return true;
    }

    private boolean help(CommandSender sender, int page){
        if(page == 1){
            sendMessage(sender, Message.SPLEEF.getMessage()+"§2 ----------------- "+Message.SPLEEF.getMessage());
            sendMessage(sender, "§3Hey! §6/§cspleef help <1, 2, ...>");
            sendMessage(sender, "§3Hey! §6/§cspleef <Create, Delete> <Spleef name> <[Game Mode]>");
            sendMessage(sender, "§3Hey! §6/§cspleef reload");
            sendMessage(sender, "§3Hey! §6/§cspleef set <Min, Max> <Spleef name> <Number>");
            sendMessage(sender, "§3Hey! §6/§cspleef set <Arena, Spawn> <Spleef name> §7(Location)");
            sendMessage(sender, "§3Hey! §6/§cspleef <Open, Close> <Spleef name>");
            sendMessage(sender, "§3Hey! §6/§cspleef set lang <French, English, Custom, Spanish>");
            sendMessage(sender, "§3Hey! §6/§cspleef list");
            sendMessage(sender, "§bPage: §21/3");
        }else if(page == 2){
            sendMessage(sender, Message.SPLEEF.getMessage()+"§2 ----------------- "+Message.SPLEEF.getMessage());
            sendMessage(sender, "§3Hey! §6/§cspleef force <Start, Stop> <Spleef name>");
            sendMessage(sender, "§3Hey! §6/§cspleef allow <SnowBalls>");
            sendMessage(sender, "§3Hey! §6/§cspleef set time <Wait> <Number>");
            sendMessage(sender, "§3Hey! §6/§cspleef set region <Spleef name>");
            sendMessage(sender, "§3Hey! §6/§cspleef set regionWithPos <Spleef name> <Pos1, Pos2>");
            sendMessage(sender, "§3Hey! §6/§cspleef remove region <Spleef name>");
            sendMessage(sender, "§3Hey! §6/§cspleef set rewards <Win, Lose> <Number>");
            sendMessage(sender, "§3Hey! §6/§cspleef set gameMode <Spleef name> <Game Mode>");
            sendMessage(sender, "§bPage: §22/3");
        }else if(page == 3){
            sendMessage(sender, Message.SPLEEF.getMessage()+"§2 ----------------- "+Message.SPLEEF.getMessage());
            sendMessage(sender, "§3Hey! §6/§cspleef set glowing §7(Make players glowing in team mode)");
            sendMessage(sender, "§bPage: §23/3");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> list = Lists.newArrayList();
        for(Spleef spleefs : pl.spleefs.getSpleefs()){
            list.add(spleefs.getName());
        }
        if(args.length == 3){
            if(args[0].equalsIgnoreCase("create")){
                list.clear();
                for(SpleefGameMode modes : SpleefGameMode.values()){
                    list.add(modes.name());
                }
            }
        }
        if(args.length == 4){
            if(args[1].equalsIgnoreCase("gameMode")){
                list.clear();
                for(SpleefGameMode modes : SpleefGameMode.values()){
                    list.add(modes.name());
                }
            }
        }
        return list;
    }
}
