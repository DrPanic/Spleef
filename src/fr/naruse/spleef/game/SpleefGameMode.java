package fr.naruse.spleef.game;

public enum SpleefGameMode {

    NORMAL("Normal"),
    DUEL("Duel"),
    TWO_TEAM("Team"),
    SPLEGG("Splegg"),
    ;
    private String name;
    SpleefGameMode(String team) {
        this.name = team;
    }

    public String getName() {
        return name;
    }
}
