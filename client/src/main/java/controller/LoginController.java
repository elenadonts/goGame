package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import model.ClientHandler;
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
    private DocumentBuilder docBuilder;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField userLogin;
    @FXML
    private PasswordField userPassword;
    private static ClientHandler clientHandler;

    public static void setClientHandler(ClientHandler currClientHandler) {
        clientHandler = currClientHandler;
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
                clientHandler.send(output);
            } catch (ParserConfigurationException | TransformerException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void hideError(MouseEvent keyEvent) {
        errorLabel.setText("");
    }

    public void setErrorLabel(String text) {
        errorLabel.setText(text);
    }

    public String getUserLogin() {
        return userLogin.getText();
    }
}
