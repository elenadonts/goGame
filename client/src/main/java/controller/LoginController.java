package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.ClientHandler;
import model.TransformerAndDocumentFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

/**
 * Class for test connecting to server when user enter login and
 * password and for showing error
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class);
    @FXML
    private Label errorLabel;
    @FXML
    private TextField userLogin;
    @FXML
    private PasswordField userPassword;
    private static ClientHandler clientHandler;

    /**
     * Set client handler object for this class
     *
     * @param currClientHandler the object
     */
    public static void setClientHandler(ClientHandler currClientHandler) {
        clientHandler = currClientHandler;
    }

    /**
     * Take login and password user from fields userLogin and
     * userPassword, and send request to server about connect
     */
    @FXML
    public void connectToServer() {
        LOGGER.info("connecting to server");

        String log = userLogin.getText();
        String pass = userPassword.getText();

        if (log.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Login or password can't be a empty!");
            LOGGER.info("Empty login or password");
        } else if (log.length() < 4 || pass.length() < 4) {
            errorLabel.setText("Login or password must be more 4 char");
            LOGGER.info("Unsatisfied password length");
        } else {
            Document doc = TransformerAndDocumentFactory.newDocument();

            Element root = PlayerWindowController.createXML(doc, "login");

            Element login = doc.createElement("login");
            login.appendChild(doc.createTextNode(log));
            root.appendChild(login);

            Element password = doc.createElement("password");
            password.appendChild(doc.createTextNode(pass));
            root.appendChild(password);

            StringWriter writer = TransformerAndDocumentFactory.transform(doc);

            clientHandler.send(writer.toString());
        }
    }

    /**
     * Hide text in errorLabel when user click on userLogin or userPassword
     * when user have error respond from server
     */
    public void hideError() {
        errorLabel.setText("");
    }

    /**
     * Set error text in errorLabel when user have error respond from server
     *
     * @param text the error
     */
    public void setErrorLabel(String text) {
        errorLabel.setText(text);
    }
}
