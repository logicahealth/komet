/**
 * Sample Skeleton for 'DatabaseLogin.fxml' Controller Class
 */

package sh.komet.fx.stage;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class DatabaseLoginController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="usernameTextField"
    private TextField usernameTextField; // Value injected by FXMLLoader

    @FXML // fx:id="passwordField"
    private PasswordField passwordField; // Value injected by FXMLLoader

    @FXML // fx:id="databaseTextField"
    private TextField databaseTextField; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert usernameTextField != null : "fx:id=\"usernameTextField\" was not injected: check your FXML file 'DatabaseLogin.fxml'.";
        assert passwordField != null : "fx:id=\"passwordField\" was not injected: check your FXML file 'DatabaseLogin.fxml'.";
        assert databaseTextField != null : "fx:id=\"databaseTextField\" was not injected: check your FXML file 'DatabaseLogin.fxml'.";

    }

    public String getUsername() {
        return usernameTextField.getText();
    }

    public void setUsername(String username) {
        this.usernameTextField.setText(username);
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public void setPassword(String password) {
        this.passwordField.setText(password);
    }

    public String getDatabaseUrl() {
        return databaseTextField.getText();
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseTextField.setText(databaseUrl);
    }
}
