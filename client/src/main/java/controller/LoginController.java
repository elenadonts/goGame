package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import model.Player;
import org.apache.log4j.Logger;
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

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class);
    private DocumentBuilder docBuilder;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField userLogin;
    @FXML
    private PasswordField userPassword;
    private static Player player;

    public static void setPlayer(Player currPlayer) {
        player  = currPlayer;
    }

    @FXML
    public void connectToServer() {
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
                player.send(output);
            } catch (ParserConfigurationException | TransformerException e) {
                logger.error("Exception", e);
            }
        }
    }

    public void hideError(MouseEvent keyEvent) {
        errorLabel.setText("");
    }

    public void setErrorLabel(String text){
        errorLabel.setText(text);
    }
}
