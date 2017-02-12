package org.ilw.valhalla.dto;

/**
 * Created by Ilja.Winokurow on 06.02.2017.
 */

public class Cell {
    private int ground;
    private int gladiator = -1;
    private int gladiatorDirection;
    private int owner  = -1;
    private boolean isBlocked  = false;
    public Cell() {
    }

    public Cell(int ground, int gladiator, int owner) {
        this.ground = ground;
        this.gladiator = gladiator;
        this.owner = owner;
    }

    public int getGround() {
        return ground;
    }

    public void setGround(int ground) {
        this.ground = ground;
    }

    public int getGladiator() {
        return gladiator;
    }

    public void setGladiator(int gladiator) {
        this.gladiator = gladiator;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getGladiatorDirection() {
        return gladiatorDirection;
    }

    public void setGladiatorDirection(int gladiatorDirection) {
        this.gladiatorDirection = gladiatorDirection;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
