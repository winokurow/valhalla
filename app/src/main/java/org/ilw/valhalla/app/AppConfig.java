package org.ilw.valhalla.app;

public class AppConfig {

    // Server get field
    public static String BASE_URL = "http://192.168.2.105:8080/evoGame/";
    //public static String BASE_URL = "http://127.0.0.1:8080/evolution/";

    // Server get field
    public static String URL_FIELD = BASE_URL + "fieldservice";

    // Server create new game
    public static String URL_CREATENEWGAME = BASE_URL + "creategameservice";

    // Server user login url
    public static String URL_USER = BASE_URL + "userservice";

    // Server user register url
    public static String URL_REGISTER = BASE_URL + "registrationservice";

    // gladiator service url
    public static String URL_GLADIATOR = BASE_URL + "gladiatorservice";

    // turns service url
    public static String URL_TURNS = BASE_URL + "turnservice";
}
