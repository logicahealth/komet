package sh.komet.gui.control.wizard.controller;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.util.Pair;
import org.controlsfx.control.CheckListView;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.komet.gui.control.wizard.WizardType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * 8/21/19
 * aks8m - https://github.com/aks8m
 */
public class ImportConfigWizardControllerView implements WizardModel<Pair<SupportedConverterTypes, Path>, List<String>> {

    private Pair<SupportedConverterTypes, Path> initializationModel;
    private final List<String> pathsNotSelectedForImport = new ArrayList<>();

    @FXML
    TextArea sourceDescriptionTextArea;
    @FXML
    CheckListView<String> fileCheckListView;

    @FXML
    private void initialize() {
        //Nothing to initialize since the initializationModel is need for the wizard view
    }

    @Override
    public List<String> getModel() {
        return this.pathsNotSelectedForImport;
    }

    @Override
    public WizardType getWizardType() {
        return WizardType.IMPORT_SPECIFICATION_CONFIGURATION;
    }

    @Override
    public void initializeWizardModel(Pair<SupportedConverterTypes, Path> initialWizardModel) {
        this.initializationModel = initialWizardModel;
        this.sourceDescriptionTextArea.setText(initialWizardModel.getKey().getSourceVersionDescription());

        try(ZipFile zipFile = new ZipFile(initialWizardModel.getValue().toFile())) {
            zipFile.stream()
                    .forEach(zipEntry -> {
                String displayString = zipEntry.toString().replace(initialWizardModel.getValue().getFileName().toString()
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
