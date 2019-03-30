package fr.naruse.spleef.game.spleef;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.util.Message;
import fr.naruse.spleef.util.Utils;
import fr.naruse.spleef.util.board.ScoreboardSign;
import fr.naruse.spleef.util.SpleefPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public abstract class Spleef extends BukkitRunnable implements Listener{
    private SpleefPlugin pl;
    private String NAME;
    private String name;
    private boolean isOpen;
    private Location spleefLoc, spleefSpawn, spleefLobby, a, b;
    private int min, max;
    private int startTimer = 0;
    private List<Sign> signs = Lists.newArrayList();
    private List<Player> playerInGame = Lists.newArrayList();
    private List<Block> blocks = Lists.newArrayList();
    private List<Block> blocksOfRegionVerif = Lists.newArrayList();
    private List<Block> blocksOfRegion = Lists.newArrayList();
    private Game game;
    private ScoreboardSign scoreboardSign;
    private SpleefGameMode gameMode;
    public Spleef(SpleefPlugin pl, SpleefGameMode gameMode, String name, Location spleefLoc, Location spleefSpawn, Location spleefLobby, int min, int max, boolean isOpen){
        this.pl = pl;
        this.gameMode = gameMode;
        this.name = name;
        this.spleefLoc = spleefLoc;
        this.spleefSpawn = spleefSpawn;
        this.spleefLobby = spleefLobby;
        this.min = min;
        this.max = max;
        this.isOpen = isOpen;
        this.scoreboardSign = new ScoreboardSign();
        this.game = new Game();
        this.NAME = "§c§l[§5"+name+"§c§l]";
        this.runTaskTimer(pl, 1, 1);
    }

    public abstract void runScheduler();

    public abstract void runTickScheduler();

    public abstract void removePlayer(Player p);

    public abstract boolean addPlayer(Player p);

    public abstract void updateSigns(Sign sign);

    public abstract void start();

    public abstract void restart(boolean notOnDisable);

    public abstract void updateScoreboards();

    private int time = 0;
    private int timePerMinute = 0;
    private HashMap<Player, Integer> restingTime = new HashMap<>();
    private HashMap<Player, Block> lastBlock = new HashMap<>();
    @Override
    public void run() {
        for(Player p : playerInGame){
            p.setFoodLevel(20);
            p.setHealth(20);
            if(game.GAME){
                if(Utils.compare(lastBlock.get(p).getLocation(), p.getLocation().getBlock().getLocation())){
                    restingTime.put(p, restingTime.get(p)+1);
                }else{
                    restingTime.put(p, 0);
                    lastBlock.put(p, p.getLocation().getBlock());
                }
                if(restingTime.get(p) == 20*5){
                    p.sendMessage(NAME+" §cHey! §a"+Message.DONT_STAY_ON_A_BLOCK.getMessage());
                }
                if(restingTime.get(p) >= 20*7){
                    restingTime.put(p, 0);
                    for(int i = 0; i != 3; i++){
                        for(Block b : Utils.getCircle(p.getLocation().add(0, -1, 0), i)){
                            if(b.getType() == Material.SNOW_BLOCK){
                                blocksOfRegionVerif.remove(b);
                                blocks.add(b);
                                b.setType(Material.AIR);
                                if(pl.getConfig().getBoolean("allow.lightning")){
                                    b.getWorld().strikeLightningEffect(b.getLocation());
                                }
                            }
                        }
                    }
                }
            }
        }
        if(time >= 20){
            time = 0;
            runScheduler();
        }
        if(timePerMinute >= 20*60){
            timePerMinute = 0;
            updateSigns();
        }
        time++;
        timePerMinute++;
        if (getGame().GAME) {
            if(playerInGame.size() == 0){
                return;
            }
            for (int i = 0; i < getPlayerInGame().size(); i++) {
                Player p = getPlayerInGame().get(i);
                if (p == null) {
                    break;
                }
                if (p.getLocation().getBlock().getType() == Material.LAVA || p.getLocation().getBlock().getType() == Material.STATIONARY_LAVA ||
                        p.getLocation().getBlock().getType() == Material.WATER || p.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
                    sendMessage(getNAME() + " §6" + p.getName() + " §c" + Message.FELL_INTO_THE_LAVA.getMessage());
                    getMain().spleefs.removePlayer(p);
                    if (getMain().otherPluginSupport.getVaultPlugin().getEconomy() != null) {
                        if (getMain().getConfig().getInt("rewards.lose") != 0) {
                            getMain().otherPluginSupport.getVaultPlugin().getEconomy().depositPlayer(getPlayerInGame().get(0), getMain().getConfig().getDouble("rewards.lose"));
                        }
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(getMain(), new Runnable() {
                        @Override
                        public void run() {
                            p.setHealth(20);
                            p.setFoodLevel(20);
                        }
                    }, 40);
                }
            }
        }
        runTickScheduler();
    }

    public void runNormalScheduler(){
        if (getGame().WAIT) {
            getScoreboardSign().getObjective().setDisplayName(getNAME() + " §6" + getStartTimer());
            if (getStartTimer() != 0) {
                if (getPlayerInGame().size() >= getMin()) {
                    setStartTimer(getStartTimer() - 1);
                } else {
                    setStartTimer(getOriginalStartTimer());
                }
            } else if (getStartTimer() == 0) {
                setStartTimer(getOriginalStartTimer());
                if (getPlayerInGame().size() >= getMin()) {
                    start();
                } else {
                    sendMessage(getNAME() + " §c" + Message.NOT_ENOUGH_PLAYER.getMessage() + " (" + getPlayerInGame().size() + "/" + getMin() + ")");
                }
            }
        } else if (getGame().GAME) {
            if (getPlayerInGame().size() == 1) {
                Player winner = getPlayerInGame().get(0);
                if(pl.getConfig().getBoolean("allow.broadcast")){
                    Bukkit.broadcastMessage(getNAME() + " §6" + winner.getName() + " §7" + Message.WINS_THE_GAME.getMessage());
                }
                if(!pl.getConfig().getString("rewards.command").equalsIgnoreCase("null")){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), pl.getConfig().getString("rewards.command").replace("{player}", winner.getName()));
                }
                if (getMain().otherPluginSupport.getVaultPlugin().getEconomy() != null) {
                    if (getMain().getConfig().getInt("rewards.win") != 0) {
                        getMain().otherPluginSupport.getVaultPlugin().getEconomy().depositPlayer(winner, getMain().getConfig().getDouble("rewards.win"));
                    }
                }
                getMain().wagers.loseWager(winner);
                restart(false);
                SpleefPlayer spleefPlayer = pl.spleefs.getSpleefPlayer(winner);
                spleefPlayer.getSpleefPlayerStatistics().addWins(1);
                spleefPlayer.getSpleefPlayerStatistics().addLoses(-1);
                spleefPlayer.getSpleefPlayerStatistics().saveStatistics();
            }
            if (getPlayerInGame().size() == 0) {
                restart(false);
            }
        }
    }

    public void sendMessage(String msg){
        for(Player p : playerInGame){
            p.sendMessage(msg);
        }
    }

    public void runNormalJoin(Player p){
        if(spleefLobby != null){
            p.teleport(spleefLobby);
        }
        lastBlock.put(p, p.getLocation().getBlock());
        restingTime.put(p, 0);
    }

    public void runNormalStart(){
        sendMessage(NAME+" §a"+Message.GAME_START.getMessage());
        game.WAIT = false;
        game.GAME = true;
        for(Player p : playerInGame){
            p.teleport(spleefLoc);
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
        scoreboardSign.getObjective().setDisplayName(NAME);
        Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
            @Override
            public void run() {
                for(Player p : playerInGame){
                    ItemStack item = new ItemStack(Material.GOLD_SPADE);
                    ItemMeta meta = item.getItemMeta();
                    meta.spigot().setUnbreakable(true);
                    item.setItemMeta(meta);
                    p.getInventory().addItem(item);
                }
            }
        },20*5);
    }

    public void runNormalRestart(boolean notOnDisable){
        List<Player> list = Lists.newArrayList();
        for(Player p : playerInGame){
            list.add(p);
        }
        for(Player p : list){
            pl.spleefs.removePlayer(p);
            if(!notOnDisable){
                Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
                    @Override
                    public void run() {
                        p.setHealth(20);
                        p.setFoodLevel(20);
                    }
                },40);
                Bukkit.getScheduler().scheduleSyncDelayedTask(getMain(), new Runnable() {
                    @Override
                    public void run() {
                        if(getGame().GAME){
                            getMain().wagers.loseWager(p);
                        }else{
                            if(getMain().wagers.hasWager(p)){
                                Player player = getMain().wagers.getWagerOfPlayer().get(p).getOtherPlayer(p);
                                if(!getPlayerInGame().contains(player)){
                                    return;
                                }
                                sendMessage(getNAME()+" §6"+player.getName()+"§c "+ Message.LEAVED_THE_GAME.getMessage());
                                getMain().spleefs.removePlayer(player);
                            }
                        }
                    }
                },20);
            }
        }
        blocksOfRegionVerif.clear();
        for(Block block : blocksOfRegion){
            blocksOfRegionVerif.add(block);
        }
        for(Block b : blocks){
            b.setType(Material.SNOW_BLOCK);
        }
        game.WAIT = true;
        game.GAME = false;
        updateSigns();
    }

    public void updateSigns(){
        for(Sign sign : signs){
            if(!isOpen){
                sign.setLine(0, "§c§l[§5"+name+"§c§l]");
                sign.setLine(1, "");
                sign.setLine(2, Message.SignColorTag.CLOSE_LINE2.getColorTag()+Message.SPLEEF_CLOSED.getMessage());
                sign.setLine(3, "");
                sign.update();
            }else{
                if(game.WAIT){
                    sign.setLine(0, "§c§l[§5"+name+"§c§l]");
                    if(playerInGame.size() >= (max/4)*3){
                        sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_0.getColorTag()+playerInGame.size()+"/"+max);
                    }else if(playerInGame.size() >= max/2){
                        sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_1.getColorTag()+playerInGame.size()+"/"+max);
                    }else {
                        sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_2.getColorTag()+playerInGame.size()+"/"+max);
                    }
                    if(playerInGame.size() >= min){
                        sign.setLine(2, Message.SignColorTag.OPEN_WAIT_LINE3_0.getColorTag()+Message.READY.getMessage());
                    }else{
                        sign.setLine(2, Message.SignColorTag.OPEN_WAIT_LINE3_1.getColorTag()+(min-playerInGame.size())+" "+Message.MISSING.getMessage());
                    }
                    sign.setLine(3, Message.SignColorTag.OPEN_WAIT_LINE4.getColorTag()+Message.JOIN.getMessage());
                    sign.update();
                }else if(game.GAME){
                    sign.setLine(0, "§c§l[§5"+name+"§c§l]");
                    if(playerInGame.size() >= (max/4)*3){
                        sign.setLine(1, Message.SignColorTag.OPEN_GAME_LINE2_0.getColorTag()+playerInGame.size()+"/"+max);
                    }else if(playerInGame.size() >= max/2){
                        sign.setLine(1, Message.SignColorTag.OPEN_GAME_LINE2_1.getColorTag()+playerInGame.size()+"/"+max);
                    }else {
                        sign.setLine(1, Message.SignColorTag.OPEN_GAME_LINE2_2.getColorTag()+playerInGame.size()+"/"+max);
                    }
                    sign.setLine(2, "");
                    sign.setLine(3, Message.SignColorTag.OPEN_GAME_LINE4_NORMAL.getColorTag()+Message.IN_GAME.getMessage());
                    sign.update();
                }
            }
            this.updateSigns(sign);
        }
    }

    public void stop(){
        this.cancel();
    }

    public void open(){
        isOpen = true;
        updateSigns();
    }

    public void onDisable(boolean notOnDisable) {
        restart(notOnDisable);
        for(Sign sign : signs){
            sign.setLine(0, NAME);
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.setLine(3, "");
            sign.update();
        }
    }

    public void close(){
        isOpen = false;
        updateSigns();
    }

    public String getName() {
        return name;
    }

    public void registerNewSigns(World world) {
        for(Chunk c : world.getLoadedChunks()){
            for(BlockState state : c.getTileEntities()){
                if(state instanceof Sign){
                    Sign sign = (Sign) state;
                    if(sign.getLine(0).equalsIgnoreCase(NAME)){
                        if(!signs.contains(sign)){
                            signs.add(sign);
                        }
                    }
                }
            }
        }
        updateSigns();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void runNormalUpdateScoreboards() {
        scoreboardSign.clearLines();
        int count = 0;
        for(Player p : playerInGame){
            scoreboardSign.setLine(count, "§3"+p.getName());
            count++;
        }
    }

    public Spleef buildRegion(Location a, Location b){
        if(a == null || b == null){
            return this;
        }
        this.a = a;
        this.b = b;
        int size = 0;
        for(Block block : blocksFromTwoPoints(a, b)){
            if(block.getType() == Material.SNOW_BLOCK){
                blocksOfRegionVerif.add(block);
                blocksOfRegion.add(block);
                size++;
            }
        }
        Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §aRegion built for "+name+". §7("+size+" blocks found)");
        return this;
    }

    private List<Block> blocksFromTwoPoints(Location loc1, Location loc2) {
        List<Block> blocks = Lists.newArrayList();
        int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
        int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
        int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
        int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
        int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
        int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
        for(int x = bottomBlockX; x <= topBlockX; x++) {
            for(int z = bottomBlockZ; z <= topBlockZ; z++) {
                for(int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    @EventHandler
    public void damage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(playerInGame.contains(p)){
                e.setCancelled(true);
                if(game.WAIT){
                    e.setCancelled(true);
                    return;
                }
                if(p.getLocation().getBlock().getType() == Material.LAVA || p.getLocation().getBlock().getType() == Material.STATIONARY_LAVA){
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void damager(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(playerInGame.contains(p)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent e){
        Player p = e.getPlayer();
        if(e.getBlock() == null){
            return;
        }
        if(getPlayerInGame().contains(p) && gameMode == SpleefGameMode.SPLEGG){
            e.setCancelled(true);
            return;
        }
        if(blocksOfRegionVerif.contains(e.getBlock())){
            if(!playerInGame.contains(p)){
                e.setCancelled(true);
            }
        }
        if(!playerInGame.contains(p)){
            return;
        }
        if(e.getBlock().getType().toString().contains("SNOW") && game.GAME){
            blocks.add(e.getBlock());
            blocksOfRegionVerif.remove(e.getBlock());
        }else{
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void item(ItemSpawnEvent e){
        if(e.getEntity().getItemStack().getType() == Material.SNOW_BALL){
            if(game.GAME){
                if(blocks.contains(e.getEntity().getLocation().getBlock())){
                    if(pl.getConfig().getBoolean("allow.snowBalls")){
                        e.setCancelled(false);
                    }else{
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void interact(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(!playerInGame.contains(p)){
            return;
        }
        if(e.getItem() == null){
            return;
        }
        if(e.getItem().getType() == Material.MAGMA_CREAM){
            sendMessage(NAME+" §6"+p.getName()+"§c "+Message.LEAVED_THE_GAME.getMessage());
            pl.spleefs.removePlayer(p);
        }
        if(e.getItem().getType() != Material.GOLD_SPADE){
            e.setCancelled(true);
        }
        if(e.getItem().getType() == Material.EGG){
            p.getInventory().addItem(new ItemStack(Material.EGG, 16));
            e.setCancelled(false);
        }
        if(e.getItem().getType() == Material.BOW){
            p.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            e.setCancelled(false);
        }
        if(e.getItem().getType() == Material.SNOW_BALL){
            if(pl.getConfig().getBoolean("allow.snowBalls")){
                e.setCancelled(false);
            }
        }
    }

    @EventHandler
    public void drop(PlayerDropItemEvent e){
        if(playerInGame.contains(e.getPlayer())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        if(playerInGame.contains(p)){
            if(pl.spleefs.hasSpleefPlayer(p)) {
                SpleefPlayer spleefPlayer = pl.spleefs.getSpleefPlayer(p);
                spleefPlayer.setPlayerInventory();
            }
            pl.spleefs.removePlayer(p);
        }
    }

    @EventHandler
    public void hitEvent(ProjectileHitEvent e){
        if(!pl.getConfig().getBoolean("allow.snowBalls")){
            return;
        }
        if(e.getHitEntity() != null){
            if(e.getHitEntity() instanceof Player && e.getEntity().getShooter() instanceof Player){
                if(playerInGame.contains(e.getHitEntity())){
                    e.getHitEntity().setVelocity(genVector(((Player) e.getEntity().getShooter()).getLocation(), e.getHitEntity().getLocation()).multiply(0.5));
                }
            }
        }
    }

    public Vector genVector(Location a, Location b) {
        double dX = a.getX() - b.getX();
        double dY = a.getY() - b.getY();
        double dZ = a.getZ() - b.getZ();
        double yaw = Math.atan2(dZ, dX);
        double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);
        Vector vector = new Vector(x, z, y);
        return vector;
    }

    public int getOriginalStartTimer(){
        return pl.getConfig().getInt("times.wait");
    }

    public Game getGame() {
        return game;
    }

    public String getNAME() {
        return NAME;
    }

    public ScoreboardSign getScoreboardSign(){
        return this.scoreboardSign;
    }

    public int getStartTimer() {
        return startTimer;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Block> getBlocksOfRegion() {
        return blocksOfRegion;
    }

    public List<Block> getBlocksOfRegionVerif() {
        return blocksOfRegionVerif;
    }

    public List<Sign> getSigns() {
        return signs;
    }

    public Location getA() {
        return a;
    }

    public Location getB() {
        return b;
    }

    public Location getSpleefLoc() {
        return spleefLoc;
    }

    public Location getSpleefSpawn() {
        return spleefSpawn;
    }

    public void setStartTimer(int startTimer){
       this.startTimer = startTimer;
    }

    public List<Player> getPlayerInGame() {
        return playerInGame;
    }

    public SpleefPlugin getMain() {
        return pl;
    }

    public SpleefGameMode getGameMode() {
        return gameMode;
    }

    public Location getSpleefLobby() {
        return spleefLobby;
    }

    public class Game{
        public boolean WAIT = true;
        public boolean GAME = false;
    }
}
