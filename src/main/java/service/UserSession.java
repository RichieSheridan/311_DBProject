package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static UserSession instance;

    private String userName;

    private String password;
    private String privileges;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    //Synchronized for thread safety
    public static synchronized UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            instance = new UserSession(userName, password, privileges);
        } else {
            instance.userName = userName;
            instance.password = password;
            instance.privileges = privileges;
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.put("USERNAME", userName);
            userPreferences.put("PASSWORD", password);
            userPreferences.put("PRIVILEGES", privileges);
        }
        return instance;
    }

    public static synchronized UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    public String getUserName() {return this.userName;}

    public String getPassword() {return this.password;}

    public String getPrivileges() {return this.privileges;}

    public synchronized void cleanUserSession() {
        this.userName = ""; // or null
        this.password = "";
        this.privileges = ""; // or null
        //Clear preferences
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.remove("USERNAME");
        userPreferences.remove("PASSWORD");
        userPreferences.remove("PRIVILEGES");
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges=" + this.privileges +
                '}';
    }
}
