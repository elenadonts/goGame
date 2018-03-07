package model;


public class Player {
    private String userName;
    private String userGameCount;
    private String userRating;
    private String userPercentWins;
    private String userStatus;

    public Player(String userName, String userGameCount, String userRating, String userPercentWins) {
        this.userName = userName;
        this.userGameCount = userGameCount;
        this.userRating = userRating;
        this.userPercentWins = userPercentWins;
        userStatus = "online";
    }

    public Player(String userName) {
        this.userName = userName;
        this.userGameCount = "0";
        this.userRating = "0";
        this.userPercentWins = "0";
        userStatus = "offline";

    }

    public String getUserName() {
        return userName;
    }

    public String getUserGameCount() {
        return userGameCount;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public String getUserPercentWins() {
        return userPercentWins;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserGameCount(String userGameCount) {
        this.userGameCount = userGameCount;
    }

    public void setUserPercentWins(String userPercentWins) {
        this.userPercentWins = userPercentWins;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

}
