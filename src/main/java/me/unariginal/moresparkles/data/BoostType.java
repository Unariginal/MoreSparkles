package me.unariginal.moresparkles.data;

public enum BoostType {
    SHINY("Shiny"), // Done
    EXPERIENCE("Experience"), // Done
    HIDDEN_ABILITY("Hidden Ability"), // Done
    EV("EV"), // Done
    IV("IV"), // Done
    BERRY("Berry"), // Done
    EGG("Egg Laying"),
    HATCH("Egg Hatching"),
    CATCH_RATE("Catch Rate"), // Done
    MARK("Mark"), // Done
    ALPHA("Alpha"), // Done
    SPAWN_BUCKET("Spawn Bucket"); // Done

    private final String displayName;

    BoostType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
