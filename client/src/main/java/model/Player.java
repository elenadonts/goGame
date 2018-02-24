package model;


public class Player {
    private String userName;
    private String userGameCount;
    private String userRating;
    private String userPercentWins;
    private String userStatus;

    @Override
    public String toString() {
        return "Player{" +
                "userName='" + userName + '\'' +
                ", userGameCount='" + userGameCount + '\'' +
                ", userRating='" + userRating + '\'' +
                ", userPercentWins='" + userPercentWins + '\'' +
                ", userStatus='" + userStatus + '\'' +
                '}';
    }

    public Player(String name, String gameCount, String rating, String  percentWins) {
        userName = name;
        userGameCount = gameCount;
        userRating = rating;
        userPercentWins = percentWins;
        userStatus = "online";
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
}
