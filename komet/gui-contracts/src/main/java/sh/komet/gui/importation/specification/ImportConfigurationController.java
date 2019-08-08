package sh.komet.gui.importation.specification;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.controlsfx.control.CheckListView;

import java.io.File;
import java.nio.file.Path;

/**
 * 2019-05-14
 * aks8m - https://github.com/aks8m
 */
public class ImportConfigurationController {



    private Path pathToImport;

    @FXML
    private CheckListView filesCeckListView;


    @FXML
    private void initialize(){



    }

    public void setFileToImport(Path pathToImport) {
        this.pathToImport = pathToImport;
    }

    public String getFileName(){
        return this.pathToImport.getFileName().toString();
    }
}
