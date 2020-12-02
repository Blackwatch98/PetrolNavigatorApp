package com.example.petrolnavigatorapp;

public class User {

    private String userId;
    private String userName;
    private UserSettings userSettings;

    User(){}

    User(String userId, String userName)
    {
        this.userId = userId;
        this.userName = userName;
        this.userSettings =  new UserSettings("Default", "Default", 1);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public class UserSettings {

        private String prefFuelType;
        private String prefFuel;
        private int searchRadius;

        UserSettings(){}

        UserSettings(String prefFuelType, String prefFuel, int searchRadius)
        {
            this.prefFuelType = prefFuelType;
            this.prefFuel = prefFuel;
            this.searchRadius = searchRadius;
        }

        public String getPrefFuelType() {
            return prefFuelType;
        }

        public void setPrefFuelType(String prefFuelType) {
            this.prefFuelType = prefFuelType;
        }

        public String getPrefFuel() {
            return prefFuel;
        }

        public void setPrefFuel(String prefFuel) {
            this.prefFuel = prefFuel;
        }

        public int getSearchRadius() {
            return searchRadius;
        }

        public void setSearchRadius(int searchRadius) {
            this.searchRadius = searchRadius;
        }
    }
}
