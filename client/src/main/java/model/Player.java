package model;

/**
 * Class with all info about new player
 * for player list
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
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

    /**
     * @return user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return user game count
     */
    public String getUserGameCount() {
        return userGameCount;
    }

    /**
     * @return user status
     */
    public String getUserStatus() {
        return userStatus;
    }

    /**
     * @return user percent wins
     */
    public String getUserPercentWins() {
        return userPercentWins;
    }

    /**
     * @return user rating
     */
    public String getUserRating() {
        return userRating;
    }

    /**
     * Sets new user game count
     *
     * @param userGameCount the new info
     */
    public void setUserGameCount(String userGameCount) {
        this.userGameCount = userGameCount;
    }

    /**
     * Sets new user percent wins
     *
     * @param userPercentWins the new info
     */
    public void setUserPercentWins(String userPercentWins) {
        this.userPercentWins = userPercentWins;
    }

    /**
     * Sets new user rating
     *
     * @param userRating the new info
     */
    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

}
