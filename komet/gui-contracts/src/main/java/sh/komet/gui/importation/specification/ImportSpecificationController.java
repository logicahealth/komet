package sh.komet.gui.importation.specification;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.komet.gui.control.wizard.EmbeddedWizard;
import sh.komet.gui.control.wizard.WizardDataTypes;
import sh.komet.gui.control.wizard.WizardType;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019-05-01
 * aks8m - https://github.com/aks8m
 */
public class ImportSpecificationController {

    private Manifold manifold;
    private SimpleBooleanProperty closeExplorationNode;
    private EmbeddedWizard embeddedWizard;

    @FXML
    private BorderPane importSpecificationBorderPane;

    @FXML
    private void initialize() {
        final FileChooser chooser = new FileChooser();
        final ArrayList<Path> userSelectedPaths = new ArrayList<>();
        ArrayList<Pair<SupportedConverterTypes, Path>> pathsToImport;

        chooser.setTitle("Specify file(s) for import...");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zip Files", "*.zip"));
        pathsToImport = determinePathsToImport(chooser.showOpenMultipleDialog(null).stream()
                .map(File::toPath).collect(Collectors.toList()));

        if(pathsToImport.size() > 0) {
            EmbeddedWizard.Builder wizardBuilder = EmbeddedWizard.Builder.newInstance();

            pathsToImport.stream()
                    .forEach(converterTypeAndPathPair ->{
                        Map<WizardDataTypes, Object> wizardData = new HashMap<>();

                        wizardData.put(WizardDataTypes.PATH_TO_IMPORT, converterTypeAndPathPair.getValue());
                        wizardData.put(WizardDataTypes.SUPPORTED_IMPORTER_TYPE, converterTypeAndPathPair.getKey());
                        wizardData.put(WizardDataTypes.WIZARD_HEADER_TEXT, "Configure " +
                                converterTypeAndPathPair.getKey().getNiceName() + " Importer");
                        wizardBuilder.addNewWizardView(WizardType.IMPORT_SPECIFICATION_CONFIGURATION, wizardData);
                    });

            this.embeddedWizard = wizardBuilder.build();

            this.importSpecificationBorderPane.centerProperty().setValue(this.embeddedWizard);
            this.embeddedWizard.cancelSelectedProperty().addListener((observableValue, aBoolean, t1) -> {
                this.closeExplorationNode.set(true);
            });
            this.embeddedWizard.finishSelectedProperty().addListener((observableValue, aBoolean, t1) -> {
                //TODO pass in the path, support convertery type (order), and list of files to ignore to the Direct Importer Manager
            });

        } else{
            this.closeExplorationNode.set(true);
        }
    }

    private ArrayList<Pair<SupportedConverterTypes, Path>> determinePathsToImport(List<Path> userSelectedPaths){
        final ArrayList<Pair<SupportedConverterTypes, Path>> pathsToImport = new ArrayList<>();
        SupportedConverterTypes.topologySortBySourceDependencies().stream()
                .forEach(importer ->
                    userSelectedPaths.stream()
                            .forEach(path -> {
                                SupportedConverterTypes pathImportType = SupportedConverterTypes.findByPath(path, true);
                                if( pathImportType != null && pathImportType == importer){
                                    pathsToImport.add(new Pair<>(pathImportType, path));
                                }
                            })
                );
        return pathsToImport;
    }

    public void setManifold(Manifold manifold){
        this.manifold = manifold;
    }

    public void setCloseExplorationNodeProperty(SimpleBooleanProperty closeExplorationNode) {
        this.closeExplorationNode = closeExplorationNode;
    }
}
