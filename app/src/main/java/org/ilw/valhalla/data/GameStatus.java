package org.ilw.valhalla.data;

public enum GameStatus {
    WAITING("WAITING"),
    PREPARE("PREPARE"),
    STARTED("STARTED"),
    ENDED("ENDED");
    private final String value;

    GameStatus(String value) {
        this.value = value;
    }

    public String asString() {
        return this.value;
    }
}

