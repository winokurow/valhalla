package org.ilw.valhalla.dto;

public class Turn {

    private int gamedid;
    private int turn;
    private String host;
    private String action;
    private String value1;
    private String value2;
    private String value3;
    private String created_at;
    private String updated_at;

    public int getGamedid() {
        return gamedid;
    }

    public void setGamedid(int gamedid) {
        this.gamedid = gamedid;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Turn(int gamedid, int turn, String host, String action, String value1, String value2, String value3,
                String created_at, String updated_at) {
        super();
        this.gamedid = gamedid;
        this.turn = turn;
        this.host = host;
        this.action = action;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

}
