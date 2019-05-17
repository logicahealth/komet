package sh.isaac.komet.batch.fxml;
/**
 * 'BatchNode.fxml' Controller Class
 */
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.table.version.VersionTable;

public class BatchNodeController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane batchAnchor;

    @FXML
    private BorderPane batchBorderPane;

    VersionTable versionTable;

    Manifold manifold;

    @FXML
    void initialize() {
        assert batchAnchor != null : "fx:id=\"batchAnchor\" was not injected: check your FXML file 'BatchNode.fxml'.";
        assert batchBorderPane != null : "fx:id=\"batchBorderPane\" was not injected: check your FXML file 'BatchNode.fxml'.";
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        this.versionTable = new VersionTable(manifold);
        this.batchBorderPane.setCenter(this.versionTable.getRootNode());
    }
}
