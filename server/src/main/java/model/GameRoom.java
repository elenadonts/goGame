package model;


import org.apache.log4j.Logger;

import java.io.PrintWriter;

/**
 * Class with all info about new game room
 * on server
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class GameRoom {
    private static int id = 1;
    private int roomId;
    private Player playerHost;
    private Player player;
    private PrintWriter printWriterHost;
    private PrintWriter printWriter;
    private String roomDescription;
    private String hostStatus;
    private String playerStatus;
    private String gameStatus;
    private int roomOnline;
    private GameField gameField;
    private String fieldSizeId;
    private boolean playerPassed;
    private boolean hostPassed;
    private static final Logger LOGGER = Logger.getLogger(GameRoom.class);

    GameRoom(String roomDescription, Player player1, PrintWriter printWriter1) {
        gameField = new GameField();
        roomId = id++;
        this.roomDescription = roomDescription;
        this.playerHost = player1;
        this.printWriterHost = printWriter1;
        hostStatus = "not ready";
        roomOnline = 1;
        gameStatus = "in lobby";
        LOGGER.info("New room created");
    }

    /**
     * @return player host
     */
    public Player getPlayerHost() {
        return playerHost;
    }

    /**
     * Sets new player name
     *
     * @param player the new info
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * @return host writer
     */
    public PrintWriter getPrintWriterHost() {
        return printWriterHost;
    }

    /**
     * @return host and simpler writer
     */
    public PrintWriter[] getWriters() {
        PrintWriter[] writers;
        if (printWriter != null) {
            writers = new PrintWriter[]{printWriter, printWriterHost};
        } else {
            writers = new PrintWriter[]{printWriterHost};
        }
        return writers;
    }

    /**
     * @return simple player writer
     */
    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    /**
     * Sets writer for simple player
     *
     * @param printWriter new writer
     */
    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    /**
     * @return game room description
     */
    public String getRoomDescription() {
        return roomDescription;
    }

    /**
     * @return room id
     */
    public int getRoomId() {
        return roomId;
    }

    /**
     * @return host status
     */
    public String getHostStatus() {
        return hostStatus;
    }

    /**
     * Sets host status
     *
     * @param hostStatus the new info
     */
    public void setHostStatus(String hostStatus) {
        this.hostStatus = hostStatus;
    }

    /**
     * Sets simple player status
     *
     * @param playerStatus the new info
     */
    public void setPlayerStatus(String playerStatus) {
        this.playerStatus = playerStatus;
    }

    /**
     * Gets online in game room
     *
     * @return online in game room
     */
    public int getRoomOnline() {
        return roomOnline;
    }

    /**
     * Sets room online
     *
     * @param roomOnline the new info
     */
    public void setRoomOnline(int roomOnline) {
        this.roomOnline = roomOnline;
    }

    /**
     * Sets game status
     *
     * @param gameStatus the new info
     */
    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    /**
     * @return game status
     */
    public String getGameStatus() {
        return gameStatus;
    }

    /**
     * @return game field
     */
    public GameField getGameField() {
        return gameField;
    }

    /**
     * Sets new field size id
     *
     * @param fieldSizeId the new info
     */
    public void setFieldSizeId(String fieldSizeId) {
        this.fieldSizeId = fieldSizeId;
    }

    /**
     * @return field size id
     */
    public String getFieldSizeId() {
        return fieldSizeId;
    }

    /**
     * @return player passed
     */
    public boolean isPlayerPassed() {
        return playerPassed;
    }

    /**
     * Sets that player passed
     *
     * @param playerPassed the new info
     */
    public void setPlayerPassed(boolean playerPassed) {
        this.playerPassed = playerPassed;
    }

    /**
     * @return host passed
     */
    public boolean isHostPassed() {
        return hostPassed;
    }

    /**
     * Sets that host passed
     *
     * @param hostPassed the new info
     */
    public void setHostPassed(boolean hostPassed) {
        this.hostPassed = hostPassed;
    }

    /**
     * @return simple player
     */
    public Player getPlayer() {
        return player;
    }
}
