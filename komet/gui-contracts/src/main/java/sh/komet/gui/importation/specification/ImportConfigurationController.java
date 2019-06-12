package sh.komet.gui.importation.specification;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.controlsfx.control.CheckListView;

import java.io.File;

/**
 * 2019-05-14
 * aks8m - https://github.com/aks8m
 */
public class ImportConfigurationController {



    private File fileToImport;

    @FXML
    private CheckListView filesCeckListView;


    @FXML
    private void initialize(){



    }

    public void setFileToImport(File fileToImport) {
        this.fileToImport = fileToImport;
    }

    public String getFileName(){
        return this.fileToImport.getName();
    }
}
