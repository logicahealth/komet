package sh.komet.fx.stage;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sh.isaac.api.constants.SystemPropertyConstants;

import java.io.File;

public class SelectDataSource implements Runnable {
    private final MainApp mainApp;
    private final Stage stage;

    public SelectDataSource(MainApp mainApp, Stage stage) {
        this.mainApp = mainApp;
        this.stage = stage;
    }

    @Override
    public void run() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Data Source...");

        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Solor"));
        File selectedFile = directoryChooser.showDialog(stage);
        if (selectedFile != null) {
            if (!selectedFile.getName().equals("data")) {
                selectedFile = new File(selectedFile, "data");
            }
            System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, selectedFile.getAbsolutePath());
        }
        Platform.runLater(new StartupAfterSelection(mainApp));

    }
}
