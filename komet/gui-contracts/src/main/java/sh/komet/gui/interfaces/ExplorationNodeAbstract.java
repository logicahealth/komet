package sh.komet.gui.interfaces;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.scene.Node;
import sh.komet.gui.control.concept.ConceptLabelWithDragAndDrop;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;

public abstract class ExplorationNodeAbstract implements ExplorationNode {

    protected final SimpleObjectProperty<ActivityFeed> activityFeedProperty = new SimpleObjectProperty<>();
    protected final SimpleStringProperty toolTipProperty = new SimpleStringProperty("uninitialized tool tip");
    protected final SimpleStringProperty titleProperty = new SimpleStringProperty("empty exploration view");
    protected final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(getMenuIconGraphic());
    protected ConceptLabelWithDragAndDrop titleLabel = null;
    protected ViewProperties viewProperties;

    public ExplorationNodeAbstract(ViewProperties viewProperties) {
            this.viewProperties = viewProperties;
    }

    public ExplorationNodeAbstract(ViewProperties viewProperties, ActivityFeed activityFeed) {
        this.viewProperties = viewProperties;
        this.activityFeedProperty.setValue(activityFeed);
    }

    public ExplorationNodeAbstract() {
        // No arg constructor for FXML
    }

    @Override
    public final SimpleObjectProperty<Node> getMenuIconProperty() {
        return menuIconProperty;
    }

    @Override
    public final ViewProperties getViewProperties() {
        return this.viewProperties;
    }

    @Override
    public final SimpleObjectProperty<ActivityFeed> activityFeedProperty() {
        return activityFeedProperty;
    }

    private Runnable nodeSelectionMethod = () -> {}; // default to an empty operation.


    @Override
    public final ReadOnlyProperty<String> getToolTip() {
        return this.toolTipProperty;
    }

    @Override
    public final void setNodeSelectionMethod(Runnable nodeSelectionMethod) {
        this.nodeSelectionMethod = nodeSelectionMethod;
    }

    protected final Runnable getNodeSelectionMethod() {
        return nodeSelectionMethod;
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public final StringPropertyBase getTitle() {
        return this.titleProperty;
    }
}
