package fr.naruse.spleef.util.board;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardSign {
    private Scoreboard sb;
    private Objective obj;
    private Team blueTeam, redTeam;
    public ScoreboardSign(){
        this.sb = Bukkit.getScoreboardManager().getNewScoreboard();
        this.obj = sb.registerNewObjective("dac", "dummy");
        this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        redTeam = sb.registerNewTeam("red");
        redTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        redTeam.setPrefix("§c");
        blueTeam = sb.registerNewTeam("blue");
        blueTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        blueTeam.setPrefix("§3");
    }

    public void setLine(int line, String msg){
        obj.getScore(msg).setScore(line);
    }

    public void clearLines(){
        for(String line : sb.getEntries()){
            sb.resetScores(line);
        }
    }

    public Objective getObjective() {
        return obj;
    }

    public Scoreboard getScoreboard() {
        return sb;
    }

    public Team getBlueTeam() {
        return blueTeam;
    }

    public Team getRedTeam() {
        return redTeam;
    }
}
