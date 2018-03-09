package model;

import org.apache.log4j.Logger;

import java.io.PrintWriter;

/**
 * Class with all info about new player
 * on server
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class Player {
    private String userName;
    private String userPassword;
    private String userGameCount;
    private String userRating;
    private String userPercentWins;
    private String userWinGames;
    private boolean admin;
    private PrintWriter writer;
    private static final Logger LOGGER = Logger.getLogger(Player.class);

    public Player() {
    }

    Player(String userPassword, String userName) {
        this.userName = userName;
        this.userPassword = userPassword;
        userGameCount = "0";
        userRating = "100";
        userPercentWins = "0";
        userWinGames = "0";
        LOGGER.info("Player " + userName + " logged in");
    }

    /**
     * Gets user name
     *
     * @return user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets new user name
     *
     * @param userName the new info
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets user password
     *
     * @return user password
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Sets new user password
     *
     * @param userPassword the new info
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Gets user game count
     *
     * @return user game count
     */
    public String getUserGameCount() {
        return userGameCount;
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
     * Gets user rating
     *
     * @return user rating
     */
    public String getUserRating() {
        return userRating;
    }

    /**
     * Sets new user ratung
     *
     * @param userRating the new info
     */
    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    /**
     * Gets user percent wins
     *
     * @return user percent wins
     */
    public String getUserPercentWins() {
        return userPercentWins;
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
     * Sets that user admin
     *
     * @param admin the new info
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * Gets that user admin
     *
     * @return user admin
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Sets new user writer
     *
     * @param writer the new info
     */
    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    /**
     * Gets user writer
     *
     * @return user writer
     */
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * Gets user win games count
     *
     * @return user win games count
     */
    public String getUserWinGames() {
        return userWinGames;
    }

    /**
     * Sets new user win game count
     *
     * @param userWinGames the new info
     */
    public void setUserWinGames(String userWinGames) {
        this.userWinGames = userWinGames;
    }
}
