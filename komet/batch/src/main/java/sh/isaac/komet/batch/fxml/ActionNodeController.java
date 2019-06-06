
package sh.isaac.komet.batch.fxml;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class ActionNodeController {

    @FXML
    private AnchorPane anchorPane;


    @FXML
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'ActionNode.fxml'.";
    }

    @FXML
    void delete(ActionEvent event) {

    }

    @FXML
    void moveDown(ActionEvent event) {

    }

    @FXML
    void moveUp(ActionEvent event) {

    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }
}
