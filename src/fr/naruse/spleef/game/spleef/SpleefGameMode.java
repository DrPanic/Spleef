package fr.naruse.spleef.game.spleef;

public enum SpleefGameMode {

    NORMAL("Normal"),
    DUEL("Duel"),
    TWO_TEAM("Team"),
    SPLEGG("Splegg"),
    BOW("Bow"),
    MELTING("Melting"),
    ;
    private String name;
    SpleefGameMode(String team) {
        this.name = team;
    }

    public String getName() {
        return name;
    }
}
