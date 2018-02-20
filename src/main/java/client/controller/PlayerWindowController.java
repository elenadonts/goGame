package client.controller;

import client.controller.LoginController;
import client.view.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import client.model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;


public class PlayerWindowController {
    @FXML
    public Label helloUser;
    private DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder docBuilder;
    private Player player;
    private Stage loginStage = new Stage();
    private LoginController loginController;

    @FXML
    public void initialize() throws IOException, ParserConfigurationException {
        docBuilder = docFactory.newDocumentBuilder();
        player = new Player();
        player.start();
        player.setGuiController(this);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent login = fxmlLoader.load();
        loginController = fxmlLoader.getController();
        loginStage.setTitle("GoGame login");
        loginStage.setScene(new Scene(login));
        loginStage.setResizable(false);
        loginStage.initOwner(Main.mainStage);
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.show();
        LoginController.setPlayer(player);
    }

    public void getMeta(String input) {
        try {
            Document document = docBuilder.parse(new InputSource(new StringReader(input)));
            Node user = document.getElementsByTagName("body").item(0);
            String meta = ((Element) user).getElementsByTagName("meta-info").item(0).getTextContent();
            switch (meta) {
                case "connect":
                    System.out.println("connect");
                    Platform.runLater(() -> {
                        Main.mainStage.show();
                        loginStage.close();
                    });

                    break;
                case "incorrect":
                    Platform.runLater(() -> loginController.setErrorLabel("Login or password are incorrect!!!"));
                    break;
            }
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }


}
