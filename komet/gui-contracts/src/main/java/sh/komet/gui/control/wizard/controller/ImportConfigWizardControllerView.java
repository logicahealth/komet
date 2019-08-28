package sh.komet.gui.control.wizard.controller;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.util.Pair;
import org.controlsfx.control.CheckListView;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.komet.gui.control.wizard.WizardController;
import sh.komet.gui.control.wizard.WizardDataTypes;
import sh.komet.gui.control.wizard.WizardType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 * 8/21/19
 * aks8m - https://github.com/aks8m
 */
public class ImportConfigWizardControllerView implements WizardController {

    private SupportedConverterTypes supportedConverterTypes;
    private Path pathToImport;
    private final List<String> pathsNotSelectedForImport = new ArrayList<>();
    private Map<WizardDataTypes, Object> wizardDatamap;
    private WizardType wizardType;

    @FXML
    TextArea sourceDescriptionTextArea;
    @FXML
    CheckListView<String> fileCheckListView;

    @FXML
    private void initialize() {
        //Nothing to initialize since the initializationModel is need for the wizard view
    }

    @Override
    public void setWizardData(Map<WizardDataTypes, Object> wizardDataMap) {
        this.wizardDatamap = wizardDataMap;
        this.supportedConverterTypes = (SupportedConverterTypes) wizardDataMap.get(WizardDataTypes.SUPPORTED_IMPORTER_TYPE);
        this.pathToImport = (Path) wizardDataMap.get(WizardDataTypes.PATH_TO_IMPORT);
        initializeWizardView();
    }

    @Override
    public Map<WizardDataTypes, Object> getWizardData() {
        return this.wizardDatamap;
    }

    @Override
    public void setWizardType(WizardType wizardType) {
        this.wizardType = wizardType;
    }

    @Override
    public WizardType getWizardType() {
        return this.wizardType;
    }

    public void initializeWizardView() {
        this.sourceDescriptionTextArea.setText(this.supportedConverterTypes.getSourceVersionDescription());

        try(ZipFile zipFile = new ZipFile(this.pathToImport.toFile())) {
            zipFile.stream()
                    .forEach(zipEntry -> {
                String displayString = zipEntry.toString().replace(this.pathToImport.getFileName().toString()
                        .replace(".zip", "/"), "");
                if(!zipEntry.isDirectory() && !displayString.equals("")){
                    this.fileCheckListView.getItems().add(displayString);
                }
            });
        }catch (IOException exception){
            exception.printStackTrace();
        }

        for(int i = 0; i < this.fileCheckListView.getItems().size(); i++){
            this.fileCheckListView.getCheckModel().check(i);
        }

        this.fileCheckListView.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                change.next();
                pathsNotSelectedForImport.addAll(change.getRemoved());
            }
        });
    }
}
