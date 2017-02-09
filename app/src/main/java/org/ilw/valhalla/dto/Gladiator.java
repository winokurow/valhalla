package org.ilw.valhalla.dto;

public class Gladiator {
    private int id;
    private int userid;
    private String name;
    private int str;
    private int str_progress;
    private int dex;
    private int dex_progress;
    private int spd;
    private int spd_progress;
    private int con;
    private int con_progress;
    private int intel;
    private int intel_progress;
    private int stamina;
    private int stamina_progress;
    private int mart_art;
    private int mart_art_progress;
    private String created_at;
    private String updated_at;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStr() {
        return str;
    }

    public void setStr(int str) {
        this.str = str;
    }

    public int getStr_progress() {
        return str_progress;
    }

    public void setStr_progress(int str_progress) {
        this.str_progress = str_progress;
    }

    public int getDex() {
        return dex;
    }

    public void setDex(int dex) {
        this.dex = dex;
    }

    public int getDex_progress() {
        return dex_progress;
    }

    public void setDex_progress(int dex_progress) {
        this.dex_progress = dex_progress;
    }

    public int getSpd() {
        return spd;
    }

    public void setSpd(int spd) {
        this.spd = spd;
    }

    public int getSpd_progress() {
        return spd_progress;
    }

    public void setSpd_progress(int spd_progress) {
        this.spd_progress = spd_progress;
    }

    public int getCon() {
        return con;
    }

    public void setCon(int con) {
        this.con = con;
    }

    public int getCon_progress() {
        return con_progress;
    }

    public void setCon_progress(int con_progress) {
        this.con_progress = con_progress;
    }

    public int getMart_art() {
        return mart_art;
    }

    public void setMart_art(int mart_art) {
        this.mart_art = mart_art;
    }

    public int getMart_art_progress() {
        return mart_art_progress;
    }

    public void setMart_art_progress(int mart_art_progress) {
        this.mart_art_progress = mart_art_progress;
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

    public int getIntel() {
        return intel;
    }

    public void setIntel(int intel) {
        this.intel = intel;
    }

    public int getIntel_progress() {
        return intel_progress;
    }

    public void setIntel_progress(int intel_progress) {
        this.intel_progress = intel_progress;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public int getStamina_progress() {
        return stamina_progress;
    }

    public void setStamina_progress(int stamina_progress) {
        this.stamina_progress = stamina_progress;
    }

    public Gladiator(int id, int userid, String name, int str, int str_progress, int dex, int dex_progress, int spd, int spd_progress, int con, int con_progress, int intel, int intel_progress, int stamina, int stamina_progress, int mart_art, int mart_art_progress, String created_at, String updated_at) {
        this.id = id;
        this.userid = userid;
        this.name = name;
        this.str = str;
        this.str_progress = str_progress;
        this.dex = dex;
        this.dex_progress = dex_progress;
        this.spd = spd;
        this.spd_progress = spd_progress;
        this.con = con;
        this.con_progress = con_progress;
        this.intel = intel;
        this.intel_progress = intel_progress;
        this.stamina = stamina;
        this.stamina_progress = stamina_progress;
        this.mart_art = mart_art;
        this.mart_art_progress = mart_art_progress;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}
