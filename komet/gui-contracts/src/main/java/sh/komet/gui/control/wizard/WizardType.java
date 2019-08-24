package sh.komet.gui.control.wizard;

import java.nio.file.Path;

/**
 * 8/21/19
 * aks8m - https://github.com/aks8m
 */
public enum WizardType {

    IMPORT_SPECIFICATION_CONFIGURATION("/fxml/wizard/ImportConfigWizardView.fxml");

    private String fxmlPath;

    WizardType(String fxmlPath){
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
