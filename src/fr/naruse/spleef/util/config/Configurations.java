package fr.naruse.spleef.util.config;

import fr.naruse.spleef.main.Main;

public class Configurations {
    private MessagesConfiguration messages;
    private CommandsConfiguration commands;
    public Configurations(Main pl){
        this.messages = new MessagesConfiguration(pl);
        this.commands = new CommandsConfiguration(pl);
        setDefault(pl);
    }

    private void setDefault(Main pl) {
        if(pl.getConfig().getString("lang") == null){
            pl.getConfig().set("lang", "english");
        }else{
            pl.getConfig().set("lang", pl.getConfig().getString("lang"));
        }
        if(pl.getConfig().getString("times.wait") == null){
            pl.getConfig().set("times.wait", 10);
        }else{
            pl.getConfig().set("times.wait", pl.getConfig().getInt("times.wait"));
        }
        if(pl.getConfig().getString("allow.snowBalls") == null){
            pl.getConfig().set("allow.snowBalls", false);
        }else{
            pl.getConfig().set("allow.snowBalls", pl.getConfig().getBoolean("allow.snowBalls"));
        }
        if(pl.getConfig().getString("rewards.win") == null){
            pl.getConfig().set("rewards.win", 0);
        }else{
            pl.getConfig().set("rewards.win", pl.getConfig().getInt("rewards.win"));
        }
        if(pl.getConfig().getString("rewards.lose") == null){
            pl.getConfig().set("rewards.lose", 0);
        }else{
            pl.getConfig().set("rewards.lose", pl.getConfig().getInt("rewards.lose"));
        }
        if(pl.getConfig().getString("gameMode.team.glowing") == null){
            pl.getConfig().set("gameMode.team.glowing", false);
        }else{
            pl.getConfig().set("gameMode.team.glowing", pl.getConfig().getBoolean("gameMode.team.glowing"));
        }
        pl.saveConfig();
    }

    public CommandsConfiguration getCommands() {
        return commands;
    }

    public MessagesConfiguration getMessages() {
        return messages;
    }
}
