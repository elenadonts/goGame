package controller;

import javafx.application.Platform;
import model.Server;

import java.io.*;
import java.net.Socket;

public class Player extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private Controller controller;

    private String input;

    public void setController(Controller controller){
        this.controller = controller;
    }


    public void run() {
        try {
            socket = new Socket("localhost", Server.PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//сюда получаем
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            while (true) {
                if (reader.ready()){
                    input = reader.readLine();
                    Platform.runLater(() -> controller.getMeta(input));
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
