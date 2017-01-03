package org.ilw.valhalla.dto;

/**
 * Created by Ilja.Winokurow on 02.01.2017.
 */
public class Game {

    private String id;
    private String gamer1;
    private String gamer2;
    private String status;
    private String created_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGamer1() {
        return gamer1;
    }

    public void setGamer1(String gamer1) {
        this.gamer1 = gamer1;
    }

    public String getGamer2() {
        return gamer2;
    }

    public void setGamer2(String gamer2) {
        this.gamer2 = gamer2;
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

    public Game(String id, String gamer1, String gamer2, String status, String created_at) {
        super();
        this.id = id;
        this.gamer1 = gamer1;
        this.gamer2 = gamer2;
        this.status = status;
        this.created_at = created_at;
    }

    public Game() {
        super();
    }
}
