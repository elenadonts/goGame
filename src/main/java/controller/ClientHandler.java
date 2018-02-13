package controller;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread{

    private BufferedReader reader;
    private PrintWriter writer;

    private Socket clientSocket;

    public ClientHandler(Socket client) {
        this.clientSocket = client;
        start();
    }

    @Override
    public void run() {
        try {
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);//send to client
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//receive from client
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true){
            String msgIn;
            try {
                if (reader.ready()){
                    msgIn = reader.readLine();//receives from this thread's client
                    writer.println(msgIn + " received!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
