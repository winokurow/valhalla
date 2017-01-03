package org.ilw.valhalla.data;

public enum GameStatus {
    WAITING("WAITING");

    private final String value;

    GameStatus(String value) {
        this.value = value;
    }

    public String asString() {
        return this.value;
    }
}

