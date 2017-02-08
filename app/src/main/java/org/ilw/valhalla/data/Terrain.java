package org.ilw.valhalla.data;

/**
 * Created by Ilja.Winokurow on 07.02.2017.
 */

public enum Terrain {
    START1(98, 5, "Starting point"),
    START2(99, 5, "Starting point"),
    CONCRETE(100, 5, "Concrete");

    private int id;
    private double speed;
    private String description;

    Terrain(int id, double speed, String description) {
        this.id = id;
        this.speed = speed;
        this.description = description;
    }

    public static Terrain fromId (int id)
    {
            for (Terrain b : Terrain.values()) {
                if (id == b.getId()) {
                    return b;
                }
            }
            return null;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
