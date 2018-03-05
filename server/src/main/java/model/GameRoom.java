package model;

import java.io.PrintWriter;

public class GameRoom {
    private static int id = 1000000;
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


    GameRoom(String roomDescription, Player player1, PrintWriter printWriter1) {
        gameField = new GameField();
        roomId = id++;
        this.roomDescription = roomDescription;
        this.playerHost = player1;
        this.printWriterHost = printWriter1;
        hostStatus = "not ready";
        roomOnline = 1;
        gameStatus = "in lobby";
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Player getPlayerHost() {
        return playerHost;
    }

    public void setPlayerHost(Player playerHost) {
        this.playerHost = playerHost;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public PrintWriter getPrintWriterHost() {
        return printWriterHost;
    }

    public void setPrintWriterHost(PrintWriter printWriterHost) {
        this.printWriterHost = printWriterHost;
    }

    public PrintWriter[] getWriters() {
        PrintWriter[] writers;
        if (printWriter != null) {
            writers = new PrintWriter[]{printWriter, printWriterHost};
        } else {
            writers = new PrintWriter[]{printWriterHost};
        }
        return writers;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(String hostStatus) {
        this.hostStatus = hostStatus;
    }

    public String getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(String playerStatus) {
        this.playerStatus = playerStatus;
    }

    public int getRoomOnline() {
        return roomOnline;
    }

    public void setRoomOnline(int roomOnline) {
        this.roomOnline = roomOnline;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public GameField getGameField() {
        return gameField;
    }
}
