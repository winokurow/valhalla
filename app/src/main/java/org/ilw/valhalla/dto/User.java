package org.ilw.valhalla.dto;

public class User {

    private String id;
    private String name;
    private String email;
    private int points;
    private int level;
    private String created_at;
    private String updated_at;

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User() {
        super();
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public User(String id, String name, String email, int points, int level, String created_at, String updated_at) {
        super();
        this.id = id;
        this.name = name;
        this.email = email;
        this.points = points;
        this.level = level;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

}
