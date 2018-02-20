package client.model;

import client.controller.PlayerWindowController;
import server.controller.Server;

import java.io.*;
import java.net.Socket;

public class Player extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private PlayerWindowController guiController;

    public void setGuiController(PlayerWindowController guiController) {
        this.guiController = guiController;
    }


    public void run() {
        try {
            socket = new Socket("localhost", Server.PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//сюда получаем
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            String input;
            while (true) {
                if (reader.ready()){
                    input = reader.readLine();
                    guiController.getMeta(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void send(String message){
        writer.println(message);
    }

}
