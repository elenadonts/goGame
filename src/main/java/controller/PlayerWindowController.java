package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import model.Player;

public class PlayerWindowController {

    @FXML
    private TextField userInput;

    @FXML
    TextField serverResponse;

    private Player player;

    @FXML
    public void initialize(){//default method that launches main window
        player = new Player();
        player.start();
        player.setGuiController(this);
    }

    @FXML
    private void sendToServer(){
        String input = userInput.getText();
        userInput.setText("");
        player.send(input);
    }
    public void receiveReply(String message){
        serverResponse.setText(message);
    }

}
