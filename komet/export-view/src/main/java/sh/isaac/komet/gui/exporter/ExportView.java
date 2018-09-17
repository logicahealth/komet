package sh.isaac.komet.gui.exporter;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sh.komet.gui.manifold.Manifold;

import java.io.IOException;

/*
 * aks8m - 5/15/18
 */
public class ExportView {

    final Stage stage;
    ExportViewController exportController;

    public ExportView(Manifold manifold){
        try {
            this.stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExportView.fxml"));
            Parent root = loader.load();
            this.exportController = loader.getController();
            this.exportController.setExportStage(stage);
            this.exportController.setManifold(manifold);

            //create scene with set width, height and color
            Scene scene = new Scene(root, 500, 150, Color.WHITESMOKE);

            //set scene to stage
            stage.setScene(scene);

            //set title to stage
            stage.setTitle("Data export");

            stage.sizeToScene();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void show(Manifold manifold) {
        ExportView exportView = new ExportView(manifold);
        exportView.stage.centerOnScreen();
        exportView.stage.show();
    }

}
