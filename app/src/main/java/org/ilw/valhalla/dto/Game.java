package org.ilw.valhalla.dto;

public class Game {

    private String id;
    private String gamer1_id;
    private String gamer1_name;
    private int gamer1_points;
    private String gamer2_id;
    private String gamer2_name;
    private int gamer2_points;
    private String status;
    private String field;
    private String created_at;
    private String updated_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Game() {
        super();
    }

    public String getGamer1_id() {
        return gamer1_id;
    }

    public void setGamer1_id(String gamer1_id) {
        this.gamer1_id = gamer1_id;
    }

    public String getGamer1_name() {
        return gamer1_name;
    }

    public void setGamer1_name(String gamer1_name) {
        this.gamer1_name = gamer1_name;
    }

    public int getGamer1_points() {
        return gamer1_points;
    }

    public void setGamer1_points(int gamer1_points) {
        this.gamer1_points = gamer1_points;
    }

    public String getGamer2_id() {
        return gamer2_id;
    }

    public void setGamer2_id(String gamer2_id) {
        this.gamer2_id = gamer2_id;
    }

    public String getGamer2_name() {
        return gamer2_name;
    }

    public void setGamer2_name(String gamer2_name) {
        this.gamer2_name = gamer2_name;
    }

    public int getGamer2_points() {
        return gamer2_points;
    }

    public void setGamer2_points(int gamer2_points) {
        this.gamer2_points = gamer2_points;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public Game(String id, String gamer1_id, String gamer1_name, int gamer1_points, String gamer2_id,
                String gamer2_name, int gamer2_points, String status, String field, String created_at, String updated_at) {
        super();
        this.id = id;
        this.gamer1_id = gamer1_id;
        this.gamer1_name = gamer1_name;
        this.gamer1_points = gamer1_points;
        this.gamer2_id = gamer2_id;
        this.gamer2_name = gamer2_name;
        this.gamer2_points = gamer2_points;
        this.status = status;
        this.field = field;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

}
