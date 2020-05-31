package sh.komet.gui.provider.classification;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

public class ClassificationResultsNode extends ExplorationNodeAbstract {
    public enum Keys {
        SELECTION_DEFAULT_TEXT
    }

    private final BorderPane classificationResultsPane = new BorderPane();
    {
        titleProperty.setValue("Classifier results");
        toolTipProperty.setValue("Classifier results from unselected instant...");
        menuIconProperty.setValue(Iconography.INFERRED.getIconographic());
    }
    private final ComboBox<ClassifierResults> resultChoices = new ComboBox<>();
    private final IsaacPreferences nodePreferences;
    private final ActivityFeed activityFeed;

    //~--- constructors --------------------------------------------------------
    public ClassificationResultsNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences nodePreferences) {
        super(viewProperties);
        this.activityFeed = activityFeed;
        this.nodePreferences = nodePreferences;
        this.classificationResultsPane.getStyleClass().add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
        this.classificationResultsPane.setCenter(new Label("Classification results"));
        GridPane gridPane = new GridPane();
        GridPane.setConstraints(resultChoices, 0, 0, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
        gridPane.getChildren().add(resultChoices);
        this.classificationResultsPane.setTop(gridPane);

        setupClassificationResults();

        Get.logicService().getClassificationInstants().addListener((ListChangeListener<? super Instant>) c -> {
            setupClassificationResults();
        });


        resultChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.layoutResults(newValue);
        });
        Optional<String> optionalSelection = this.nodePreferences.get(Keys.SELECTION_DEFAULT_TEXT);
        if (optionalSelection.isPresent()) {
            String selectionDefaultText = optionalSelection.get();
            for (ClassifierResults resultChoice: resultChoices.getItems()) {
                if (resultChoice.getDefaultText().equals(selectionDefaultText)) {
                    resultChoices.getSelectionModel().select(resultChoice);
                    break;
                }
            }
        }

        resultChoices.setButtonCell(new ClassifierResultsListCell());
        resultChoices.setCellFactory(new Callback<ListView<ClassifierResults>, ListCell<ClassifierResults>>() {
            @Override public ListCell<ClassifierResults> call(ListView<ClassifierResults> p) {
                return new ClassifierResultsListCell();
            }
        });
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.INFERRED.getIconographic();
    }

    private void setupClassificationResults() {
        ClassifierResults currentSelection = resultChoices.getSelectionModel().getSelectedItem();
        List<ClassifierResults> classifierResults = new ArrayList<>();
        for (Instant instant: Get.logicService().getClassificationInstants()) {
            Optional<ClassifierResults[]> optionalResult = Get.logicService().getClassificationResultsForInstant(instant);
                if (optionalResult.isPresent()) {
                    for (ClassifierResults result: optionalResult.get()) {
                        classifierResults.add(result);
                    }
                }
            }
        classifierResults.sort((o1, o2) -> {
            int compare = o2.getCommitTime().compareTo(o1.getCommitTime());
            if (compare != 0) {
                return compare;
            }
            String editModule1 = Get.conceptDescriptionText(o1.getEditCoordinate().getModuleNid());
            String editModule2 = Get.conceptDescriptionText(o2.getEditCoordinate().getModuleNid());
            return editModule1.compareTo(editModule2);
        });

        resultChoices.getItems().setAll(classifierResults);
        if (currentSelection != null) {
            resultChoices.getSelectionModel().select(currentSelection);
        }
    }

    class ClassifierResultsListCell extends ListCell<ClassifierResults> {
        @Override protected void updateItem(ClassifierResults item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                StringBuilder sb = new StringBuilder();
                DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy; hh:mm a zzz");
                sb.append(FORMATTER.format(item.getCommitTime().atZone(ZoneOffset.systemDefault())));
                sb.append(" to the ");
                sb.append(viewProperties.getPreferredDescriptionText(item.getEditCoordinate().getModuleNid()));
                setText(sb.toString());
            } else {
                setText(null);
            }
        }
    }

    private void layoutResults(ClassifierResults classifierResult) {
        try {
            if (classifierResult != null) {
                String classifierResultDefaultText = classifierResult.getDefaultText();
                nodePreferences.put(Keys.SELECTION_DEFAULT_TEXT, classifierResultDefaultText);
                nodePreferences.sync();
            }
        } catch (BackingStoreException e) {
            FxGet.dialogs().showErrorDialog("Error writing classification results selection to preferences.", e);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/komet/gui/provider/classification/ClassifierResultsInterface.fxml"));
            loader.load();
            ClassifierResultsController resultsController = loader.getController();
            this.classificationResultsPane.setCenter(loader.getRoot());
            resultsController.setResults(classifierResult);
        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }

    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public Node getNode() {
        return classificationResultsPane;
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public ActivityFeed getActivityFeed() {
        return this.activityFeed;
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void savePreferences() {

    }
}
