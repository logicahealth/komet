package sh.isaac.komet.batch.fxml;
/**
 * Sample Skeleton for 'BatchNode.fxml' Controller Class
 */

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

public class BatchNodeController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="batchAnchor"
    private AnchorPane batchAnchor; // Value injected by FXMLLoader

    @FXML // fx:id="listTable"
    private TableView<?> listTable; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert batchAnchor != null : "fx:id=\"batchAnchor\" was not injected: check your FXML file 'BatchNode.fxml'.";
        assert listTable != null : "fx:id=\"listTable\" was not injected: check your FXML file 'BatchNode.fxml'.";

    }
}
