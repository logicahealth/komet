package sh.komet.gui.control.wizard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.komet.gui.control.wizard.controller.WizardModel;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

/**
 * 2019-08-12
 * aks8m - https://github.com/aks8m
 */
public class EmbeddedWizard extends AnchorPane {

    private final Wizard wizard = new Wizard();
    private final BooleanProperty cancelSelected = new SimpleBooleanProperty(false);
    private final BooleanProperty finishSelected = new SimpleBooleanProperty(false);
    private final HashSet<String> previousButtonFilteredViews = new HashSet<>();
    private final LinkedHashMap<UUID, WizardModel> mvcMap;

    private EmbeddedWizard(Builder builder){
        this.mvcMap = builder.mvcMap;
        this.wizard.setFlow(new Wizard.LinearFlow(builder.wizardPanes));

        try {
            Method getDialogMethod = Wizard.class.getDeclaredMethod("getDialog", null);
            getDialogMethod.setAccessible(true);
            Dialog<ButtonType> wizardDialog = (Dialog<ButtonType>) getDialogMethod.invoke(this.wizard, null);

            updateAnchorPane(wizardDialog.getDialogPane());
            wizardDialog.dialogPaneProperty().addListener((observableValue, dialogPane, newDialogPane) -> {
                updateAnchorPane(newDialogPane);
            });
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception){
            exception.printStackTrace();
        }
    }

    private void updateAnchorPane(DialogPane dialogPane){
        AnchorPane.setBottomAnchor(dialogPane,0.0);
        AnchorPane.setLeftAnchor(dialogPane,0.0);
        AnchorPane.setRightAnchor(dialogPane,0.0);
        AnchorPane.setTopAnchor(dialogPane,0.0);

        this.getChildren().clear();
        this.getChildren().add(dialogPane);

        if(this.previousButtonFilteredViews.add(dialogPane.getId())) {
            addTerminationButtonEventFilters(dialogPane);
        }
    }

    private void addTerminationButtonEventFilters(DialogPane dialogPane){
        Node finishButton = dialogPane.lookupButton(ButtonType.FINISH);
        Node cancelButton = dialogPane.lookupButton(ButtonType.CANCEL);

        if (finishButton != null) {
            finishButton.addEventFilter(ActionEvent.ACTION, actionEvent -> {
                actionEvent.consume();
                finishSelectedProperty().setValue(true);
            });
        }
        if (cancelButton != null) {
            cancelButton.addEventFilter(ActionEvent.ACTION, actionEvent -> {
                actionEvent.consume();
                cancelSelectedProperty().setValue(true);
            });
        }
    }

    public BooleanProperty cancelSelectedProperty() {
        return cancelSelected;
    }

    public BooleanProperty finishSelectedProperty() {
        return finishSelected;
    }

    public WizardModel getModel(int wizardViewIndex){


        return this.mvcMap.entrySet().stream().skip(wizardViewIndex - 1).findFirst().get().getValue();
    }

    public WizardModel getModel(UUID wizardViewUUID){
        return this.mvcMap.get(wizardViewUUID);
    }

    public List<WizardModel> getModels(){
        return new ArrayList<>(this.mvcMap.values());
    }

    public List<WizardModel> getModels(WizardType wizardType){
        return null;
    }

    public List<WizardModel> getModels(UUID... wizardViewUUID){
        List<UUID> uuids = Arrays.asList(wizardViewUUID);

        return null;
    }

    public static class Builder{

        private final ArrayList<WizardPane> wizardPanes = new ArrayList<>();
        private final LinkedHashMap<UUID, WizardModel> mvcMap = new LinkedHashMap<>();

        private Builder(){}

        public static Builder newInstance(){
            return new Builder();
        }

        public Builder createWizardView(WizardType wizardType, Object wizardViewData){
            try{
                WizardPane newWizardPane = new WizardPane();
                UUID newWizardPaneUUID = UUID.randomUUID();

                FXMLLoader loader = new FXMLLoader(getClass().getResource(wizardType.getFxmlPath()));
                newWizardPane.setId(newWizardPaneUUID.toString());
                newWizardPane.setContent(loader.load());

                switch (wizardType){
                    case IMPORT_SPECIFICATION_CONFIGURATION:
                        WizardModel<Pair<SupportedConverterTypes, Path>, List<String>> wizardModel = loader.getController();
                        wizardModel.initializeWizardModel((Pair<SupportedConverterTypes, Path>) wizardViewData);
                        this.mvcMap.put(newWizardPaneUUID, wizardModel);
                        newWizardPane.setHeaderText("Configure " +
                                ((Pair<SupportedConverterTypes, Path>) wizardViewData).getKey().getNiceName() + " " +
                                "Import");
                }

                this.wizardPanes.add(newWizardPane);
            }catch (IOException exception){
                throw new RuntimeException(exception);
            }

            return this;
        }

        public EmbeddedWizard build(){
            return new EmbeddedWizard(this);
        }
    }
}
