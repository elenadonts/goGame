package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ClientHandler;
import model.TransformerAndDocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class for take info about new room and send
 * request to server with this info
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class CreateRoomController {
    @FXML
    public TextField roomDescriptionField;
    @FXML
    public Label createRoomError;
    private static ClientHandler clientHandler;
    private static Stage currentStage;
    private static PlayerWindowController playerWindowController;

    /**
     * Take info from created room form and send info to server
     */
    @FXML
    public void create() {
        if (roomDescriptionField.getText().isEmpty()) {
            createRoomError.setText("Room name can't be empty!");
        } else {
            String roomDescription = roomDescriptionField.getText();
            Document document = TransformerAndDocumentFactory.newDocument();
            Element root = PlayerWindowController.createXML(document, "createRoom");
            root.appendChild(TransformerAndDocumentFactory.createElement(document,"roomDescription",roomDescription));
            root.appendChild(TransformerAndDocumentFactory.createElement(document,"fieldSize",playerWindowController.fieldSize5.getId()));
            clientHandler.send(TransformerAndDocumentFactory.transformToString(document));
            currentStage.close();
        }
    }

    /**
     * Set client handler object for this class
     *
     * @param clientHandler the object
     */
    public static void setClientHandler(ClientHandler clientHandler) {
        CreateRoomController.clientHandler = clientHandler;
    }

    /**
     * Set current stage for this class what later be closed
     *
     * @param currentStage the object
     */
    public static void setCurrentStage(Stage currentStage) {
        CreateRoomController.currentStage = currentStage;
    }

    /**
     * Set object playerWindowController class for this class
     *
     * @param playerWindowController the object
     */
    public static void setPlayerWindowController(PlayerWindowController playerWindowController) {
        CreateRoomController.playerWindowController = playerWindowController;
    }
}
