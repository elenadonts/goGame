package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ClientHandler;
import model.TransformerAndDocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

public class CreateRoomController {
    @FXML
    public TextField roomDescriptionField;
    @FXML
    public Label createRoomError;
    private String roomDescription;
    private static ClientHandler clientHandler;
    private static Stage currentStage;
    private static PlayerWindowController playerWindowController;

    @FXML
    public void create() {
        if (roomDescriptionField.getText().isEmpty()) {
            createRoomError.setText("Room name can't be empty!");
        } else {
            roomDescription = roomDescriptionField.getText();

            Document doc = TransformerAndDocumentFactory.newDocument();

            Element root = PlayerWindowController.createXML(doc, "createRoom");

            Element roomName = doc.createElement("roomDescription");
            roomName.appendChild(doc.createTextNode(roomDescription));
            root.appendChild(roomName);

            Element fieldSize = doc.createElement("fieldSize");
            fieldSize.appendChild(doc.createTextNode(playerWindowController.fieldSize5.getId()));
            root.appendChild(fieldSize);

            StringWriter writer = TransformerAndDocumentFactory.transform(doc);

            clientHandler.send(writer.toString());
            currentStage.close();
        }
    }

    public static void setClientHandler(ClientHandler clientHandler) {
        CreateRoomController.clientHandler = clientHandler;
    }

    public static void setCurrentStage(Stage currentStage) {
        CreateRoomController.currentStage = currentStage;
    }

    public static void setPlayerWindowController(PlayerWindowController playerWindowController) {
        CreateRoomController.playerWindowController = playerWindowController;
    }
}
