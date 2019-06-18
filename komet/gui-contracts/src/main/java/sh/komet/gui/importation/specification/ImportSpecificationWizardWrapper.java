package sh.komet.gui.importation.specification;

import javafx.scene.Node;

/**
 * 2019-05-15
 * aks8m - https://github.com/aks8m
 */
public class ImportSpecificationWizardWrapper {

    private final Node node;
    private final ImportConfigurationController importController;

    public ImportSpecificationWizardWrapper(Node node, ImportConfigurationController importController) {
        this.node = node;
        this.importController = importController;
    }

    public ImportConfigurationController getImportController() {

        return importController;
    }

    public Node getNode() {
        return node;
    }
}
