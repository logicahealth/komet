package sh.komet.gui.importation.specification;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.controlsfx.control.CheckListView;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.komet.gui.control.wizard.WizardView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipFile;

/**
 * 8/21/19
 * aks8m - https://github.com/aks8m
 */
public class ImportConfigWizardViewController implements WizardView {

    private SupportedConverterTypes supportedConverterTypes;
    private Path pathToImport;
    private final List<String> pathsNotSelectedForImport = new ArrayList<>();
    private Map<String, Object> wizardViewData;

    @FXML
    TextArea sourceDescriptionTextArea;
    @FXML
    CheckListView<String> fileCheckListView;

    @FXML
    public void initialize() {
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

        //this.pathToImport = (Path) wizardDataMap.get(WizardDataTypes.PATH_TO_IMPORT);
    }


    @Override
    public void setWizardViewData(Map<String, Object> wizardViewData) {
        this.wizardViewData = wizardViewData;
    }

    @Override
    public Map<String, Object> getWizardViewData() {
        return null;
    }

    @Override
    public String getWizardViewFXML() {
        return getClass().getResource("/fxml/").getPath();
    }

    @Override
    public UUID getWizardViewUUID() {
        return UUID.randomUUID();
    }

}
