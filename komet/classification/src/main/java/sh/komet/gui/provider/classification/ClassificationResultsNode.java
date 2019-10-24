package sh.komet.gui.provider.classification;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClassificationResultsNode implements ExplorationNode {

    private final BorderPane classificationResultsPane = new BorderPane();
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("Classifier results");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Classifier results from unselected instant...");
    private final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(Iconography.INFERRED.getIconographic());
    private final Manifold classificationResultsManifold;
    private final ComboBox<ClassifierResults> resultChoices = new ComboBox<>();

    //~--- constructors --------------------------------------------------------
    public ClassificationResultsNode(Manifold classificationResultsManifold) {
        this.classificationResultsManifold = classificationResultsManifold;
        this.classificationResultsPane.getStyleClass().add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
        this.classificationResultsPane.setCenter(new Label("Classification results"));
        GridPane gridPane = new GridPane();
        GridPane.setConstraints(resultChoices, 0, 0, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
        gridPane.getChildren().add(resultChoices);
        this.classificationResultsPane.setTop(gridPane);

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

        resultChoices.getItems().addAll(classifierResults);
        resultChoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.layoutResults(newValue);
        });

        resultChoices.setButtonCell(new ClassifierResultsListCell());
        resultChoices.setCellFactory(new Callback<ListView<ClassifierResults>, ListCell<ClassifierResults>>() {
            @Override public ListCell<ClassifierResults> call(ListView<ClassifierResults> p) {
                return new ClassifierResultsListCell();
            }
        });
    }

    class ClassifierResultsListCell extends ListCell<ClassifierResults> {
        @Override protected void updateItem(ClassifierResults item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                StringBuilder sb = new StringBuilder();
                DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy; hh:mm a zzz");
                sb.append(FORMATTER.format(item.getCommitTime().atZone(ZoneOffset.systemDefault())));
                sb.append(" to the ");
                sb.append(classificationResultsManifold.getPreferredDescriptionText(item.getEditCoordinate().getModuleNid()));
                setText(sb.toString());
            } else {
                setText(null);
            }
        }
    }

    private void layoutResults(ClassifierResults classifierResult) {
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
    public ReadOnlyProperty<String> getTitle() {
        return titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipProperty;
    }

    @Override
    public Manifold getManifold() {
        return null;
    }

    @Override
    public Node getNode() {
        return classificationResultsPane;
    }

    @Override
    public ObjectProperty<Node> getMenuIconProperty() {
        return menuIconProperty;
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void savePreferences() {

    }
}
