package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class Controller {
    private Player player;
    private ActionEvent actionConnect;
    private DocumentBuilder docBuilder;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField userLogin;
    @FXML
    private PasswordField userPassword;
    @FXML
    private Label nameTest;
    @FXML
    public void initialize() {//default method that launches main window
        player = new Player();
        player.start();
        player.setController(this);
    }

    @FXML
    public void connectToServer(ActionEvent actionEvent) {
        String log = userLogin.getText();
        String pass = userPassword.getText();
        if (log.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Login or password can't be a empty!");
        } else if (log.length() < 4 || pass.length() < 4) {
            errorLabel.setText("Login or password must be more 4 char");
        } else {
            try {
                System.out.println("Login:" + log + "; Password:" + pass);
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();

                Element root = doc.createElement("body");
                doc.appendChild(root);

                Element meta = doc.createElement("meta-info");
                meta.appendChild(doc.createTextNode("login"));
                root.appendChild(meta);

                Element login = doc.createElement("login");
                login.appendChild(doc.createTextNode(log));
                root.appendChild(login);

                Element password = doc.createElement("password");
                password.appendChild(doc.createTextNode(pass));
                root.appendChild(password);

                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                String output = writer.toString();
                actionConnect=actionEvent;
                player.send(output);
            } catch (ParserConfigurationException | TransformerException ex) {
                ex.printStackTrace();
            }
        }
    }


    private Stage createScene() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/playerWindow.fxml"));
        Stage playerWindow = new Stage();
        playerWindow.setResizable(false);
        playerWindow.setTitle("GoGame");
        playerWindow.setScene(new Scene(root));
        playerWindow.setOnCloseRequest(we -> {
            System.exit(0);
        });
        return playerWindow;
    }

    public void hideError(MouseEvent keyEvent) {
        errorLabel.setText("");
    }

    public void getMeta(String input) {
        try {
            Document document = docBuilder.parse(new InputSource(new StringReader(input)));
            Node user = document.getElementsByTagName("body").item(0);
            String meta = ((Element) user).getElementsByTagName("meta-info").item(0).getTextContent();
            switch (meta) {
                case "connect":
                    Stage playerWindow = createScene();
                    ((javafx.scene.Node)actionConnect.getSource()).getScene().getWindow().hide();
                    playerWindow.show();
                    nameTest.setText("Hi boys!");
                    break;
                case "incorrect":
                    errorLabel.setText("Login or password incorrect!");
                    break;
            }
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }


}
