package sh.isaac.komet.preferences.paths;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.model.observable.coordinate.ObservableStampPathImpl;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.contract.preferences.PreferencesTreeItem;
import sh.komet.gui.control.property.ViewProperties;

import static sh.isaac.api.util.time.DateTimeUtil.EASY_TO_READ_DATE_TIME_FORMAT;

public class PathItemPanel implements PreferenceGroup {
    private PreferencesTreeItem preferencesTreeItem;
    private final ConceptSnapshot pathConcept;
    private final String pathNameString;
    private final ObservableStampPathImpl observablePathCoordinate;
    final GridPane pathGridPane = new GridPane();
    final ListView<StampPositionImmutable> positionListView = new ListView<>();
    {
        positionListView.setPrefHeight(250);
        positionListView.setMinHeight(250);
        positionListView.setMaxHeight(250);
        positionListView.setCellFactory(c-> new ListCell<>() {
            @Override
            protected void updateItem(StampPositionImmutable item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    this.setText(DateTimeUtil.format(item.getTime(), EASY_TO_READ_DATE_TIME_FORMAT) + "\non path: " +
                            pathConcept.getPreferredDescriptionText(item.getPathForPositionConcept()));
                } else {
                    this.setText("");
                }
            }
        });
    }


    public PathItemPanel(ConceptSnapshot pathConcept) {
        this.pathConcept = pathConcept;
        this.observablePathCoordinate = ObservableStampPathImpl.make(StampPathImmutable.make(pathConcept));
        this.positionListView.setItems(observablePathCoordinate.pathOriginsAsListProperty());
        this.pathNameString = pathConcept.getRegularDescriptionText().get();
        Label pathNameLabel = new Label("Path name");
        pathNameLabel.setPadding(new Insets(5, 20, 5, 10));
        Label pathName = new Label(this.pathNameString);
        pathName.setPadding(new Insets(5, 20, 5, 0));

        Label pathOriginsLabel = new Label("Origins");
        pathOriginsLabel.setPadding(new Insets(5, 20, 5, 10));
        Pane filler = new Pane();

        GridPane.setConstraints(pathNameLabel, 0, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
        GridPane.setConstraints(pathName, 1, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
        GridPane.setConstraints(pathOriginsLabel, 0, 1, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
        GridPane.setConstraints(this.positionListView, 1, 1, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
        GridPane.setConstraints(filler, 1, 2, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
        this.pathGridPane.getChildren().addAll(pathNameLabel, pathName, pathOriginsLabel, this.positionListView, filler);
    }

    @Override
    public Node getCenterPanel(ViewProperties viewProperties) {
        return pathGridPane;
    }

    @Override
    public Node getRightPanel(ViewProperties viewProperties) {
        return null;
    }

    @Override
    public Node getTopPanel(ViewProperties viewProperties) {
        return new ToolBar();
    }

    @Override
    public Node getBottomPanel(ViewProperties viewProperties) {
        return new ToolBar();
    }

    @Override
    public Node getLeftPanel(ViewProperties viewProperties) {
        return null;
    }

    @Override
    public String getGroupName() {
        return null;
    }

    @Override
    public SimpleStringProperty groupNameProperty() {
        return null;
    }

    @Override
    public void save() {
        // nothing to save.
    }

    @Override
    public void revert() {
        // nothing to revert.
    }

    @Override
    public boolean initialized() {
        return true;
    }

    @Override
    public PreferencesTreeItem getTreeItem() {
        return this.preferencesTreeItem;
    }

    @Override
    public void setTreeItem(PreferencesTreeItem preferencesTreeItem) {
        this.preferencesTreeItem = preferencesTreeItem;
    }

    @Override
    public String toString() {
        return this.pathNameString;
    }
}
