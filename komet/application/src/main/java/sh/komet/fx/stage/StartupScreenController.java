package sh.komet.fx.stage;
/**
 * Sample Skeleton for 'StartupScreen.fxml' Controller Class
 */

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import sh.komet.progress.view.TaskProgressNode;
import sh.komet.progress.view.TaskProgressNodeFactory;

public class StartupScreenController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="splashBorderPane"
    private BorderPane splashBorderPane; // Value injected by FXMLLoader

    @FXML // fx:id="statusLabel"
    private Label statusLabel; // Value injected by FXMLLoader

    @FXML // fx:id="progressAnchor"
    private AnchorPane progressAnchor; // Value injected by FXMLLoader

    TaskProgressNode taskProgressNode;
    Node taskDisplayNode;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert splashBorderPane != null : "fx:id=\"splashBorderPane\" was not injected: check your FXML file 'StartupScreen.fxml'.";
        assert statusLabel != null : "fx:id=\"statusLabel\" was not injected: check your FXML file 'StartupScreen.fxml'.";
        assert progressAnchor != null : "fx:id=\"progressAnchor\" was not injected: check your FXML file 'StartupScreen.fxml'.";

        TaskProgressNodeFactory factory = new TaskProgressNodeFactory();
        taskProgressNode  = factory.createNode(null, null);
        taskDisplayNode = taskProgressNode.getNode();
        AnchorPane.setBottomAnchor(taskDisplayNode, 0.0);
        AnchorPane.setTopAnchor(taskDisplayNode, 5.0);
        AnchorPane.setLeftAnchor(taskDisplayNode, 0.0);
        AnchorPane.setRightAnchor(taskDisplayNode, 0.0);
        progressAnchor.getChildren().add(taskDisplayNode);
    }
}
