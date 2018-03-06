package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.ClientHandler;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class CreateRoomController {
    @FXML
    public TextField roomDescriptionField;
    @FXML
    public Label createRoomError;
    private String roomDescription;
    private static ClientHandler clientHandler;
    private static Stage currentStage;
    private DocumentBuilder docBuilder;
    private static PlayerWindowController playerWindowController;
    @FXML
    public void create(MouseEvent mouseEvent) throws ParserConfigurationException, TransformerException {
        if (roomDescriptionField.getText().isEmpty()){
            createRoomError.setText("Room name can't be empty!");
        }else {
            roomDescription = roomDescriptionField.getText();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element root = doc.createElement("body");
            doc.appendChild(root);

            Element meta = doc.createElement("meta-info");
            meta.appendChild(doc.createTextNode("createRoom"));
            root.appendChild(meta);

            Element roomName = doc.createElement("roomDescription");
            roomName.appendChild(doc.createTextNode(roomDescription));
            root.appendChild(roomName);

            Element fieldSize = doc.createElement("fieldSize");
            fieldSize.appendChild(doc.createTextNode(playerWindowController.fieldSize5.getId()));
            root.appendChild(fieldSize);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
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
