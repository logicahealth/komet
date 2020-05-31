package sh.komet.gui.interfaces;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.komet.gui.control.concept.ConceptLabelWithDragAndDrop;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DetailNodeAbstract extends ExplorationNodeAbstract implements DetailNode<IdentifiedObject> {
    public enum Keys {
        ACTIVITY_FEED_NAME,
        ACTIVITY_SELECTION_INDEX,
        DETAIL_NODE_INSTANCE
    }

    protected final SimpleObjectProperty<IdentifiedObject> identifiedObjectFocusProperty = new SimpleObjectProperty<>();
    protected final SimpleIntegerProperty selectionIndexProperty = new SimpleIntegerProperty(0);
    /**
     * Not that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each reference will create
     * a new method reference, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ListChangeListener<? super IdentifiedObject> selectionChangedListener = this::selectionChanged;

    protected final BorderPane conceptDetailPane = new BorderPane();

    public DetailNodeAbstract(ViewProperties viewProperties, ActivityFeed activityFeed) {
        super(viewProperties, activityFeed);
        conceptDetailPane.getProperties().put(Keys.DETAIL_NODE_INSTANCE, this);
        this.conceptDetailPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                // would happen if tab closed. Removing the listener.
                this.activityFeedProperty.get().feedSelectionProperty().removeListener(getSelectionChangedListener());
                return;
            }
            setSelectionIndex();
        });
        this.activityFeedProperty.addListener(this::activityFeedChanged);
        if (this.getActivityFeed().isLinked()) {
            this.getActivityFeed().feedSelectionProperty().addListener(getSelectionChangedListener());
        }
        Platform.runLater(() -> updateMenuGraphic());

    }

    private void activityFeedChanged(ObservableValue<? extends ActivityFeed> observable, ActivityFeed oldValue, ActivityFeed newValue) {
            if (oldValue != null) {
                oldValue.feedSelectionProperty().removeListener(getSelectionChangedListener());
            }
            if (newValue.isLinked()) {
                this.selectionListChanged((ObservableList<? extends IdentifiedObject>) newValue.feedSelectionProperty());
                newValue.feedSelectionProperty().addListener(getSelectionChangedListener());
            }
            updateMenuGraphic();
            savePreferences();
    }

    /**
     * TODO: make other tabs reuse this icon update capability...
     */
    private void updateMenuGraphic() {
        String feedName = getActivityFeed().getFeedName();
        if (feedName.equals(ViewProperties.UNLINKED)) {
            menuIconProperty.set(IconographyHelper.combine(getMenuIconGraphic(), Iconography.LINK_BROKEN.getIconographic()));
        } else {
            Optional<Node> optionalIcon = ViewProperties.getOptionalGraphicForActivityFeed(feedName);
            if (optionalIcon.isPresent()) {
                menuIconProperty.set(IconographyHelper.combine(getMenuIconGraphic(), optionalIcon.get()));
            } else {
                menuIconProperty.set(getMenuIconGraphic());
            }
        }
    }

    @Override
    public final ListChangeListener<? super IdentifiedObject> getSelectionChangedListener() {
        return (ListChangeListener<? super IdentifiedObject>) this.selectionChangedListener;
    }

    private void selectionChanged(ListChangeListener.Change<? extends IdentifiedObject> c) {
        selectionListChanged(c.getList());
    }

    protected final void selectionListChanged(ObservableList<? extends IdentifiedObject> list) {
        if (list.size() > 1) {
            if (getScene() == null) {
                this.getActivityFeed().feedSelectionProperty().removeListener((ListChangeListener<? super IdentifiedObject>) selectionChangedListener);
                return;
            }
            if (this.selectionIndexProperty.get() == 0) {
                setSelectionIndex();
            }
        } else {
            this.selectionIndexProperty.set(0);
        }
        Optional<IdentifiedObject> newFocus = Optional.empty();
        if (selectionIndexProperty.get() < list.size()) {
            newFocus = Optional.of(list.get(selectionIndexProperty.get()));
        }

        newFocus.ifPresentOrElse(identifiedObject -> {
            this.identifiedObjectFocusProperty.set(identifiedObject);
            this.titleProperty.set(this.viewProperties.getPreferredDescriptionText(identifiedObject.getNid()));
            Platform.runLater(() -> {
                this.setFocus(identifiedObject);
            });
        }, () -> {
            this.titleProperty.set(ConceptLabelWithDragAndDrop.EMPTY_TEXT);
            Platform.runLater(() -> {
                this.setFocus(null);
            });
        });
        if (selectInTabOnChange()) {
            Platform.runLater(this.getNodeSelectionMethod());
        }

    }

    private void setSelectionIndex() {
        Parent root = getScene().getRoot();
        List equivalentNodes = new ArrayList();
        getEquivalentNodesInWindow(root, equivalentNodes);
        final int myIndex = equivalentNodes.indexOf(this);
        this.selectionIndexProperty.set(myIndex);
    }

    protected abstract void setFocus(IdentifiedObject component);

    protected void getEquivalentNodesInWindow(Parent parent, List equivalentNodes) {
        for (Node child: parent.getChildrenUnmodifiable()) {
            if (child.getProperties().containsKey(Keys.DETAIL_NODE_INSTANCE)) {
                DetailNodeAbstract possiblyEquivalentNode = (DetailNodeAbstract) child.getProperties().get(Keys.DETAIL_NODE_INSTANCE);
                if (possiblyEquivalentNode.getActivityFeed().getFullyQualifiedActivityFeedName().equals(getActivityFeed().getFullyQualifiedActivityFeedName())) {
                    if (possiblyEquivalentNode.getClass().getName().equals(this.getClass().getName())) {
                        equivalentNodes.add(possiblyEquivalentNode);
                    }
                }
            } else {
                if (child instanceof Parent) {
                    getEquivalentNodesInWindow((Parent) child, equivalentNodes);
                }
            }
        }
    }

    @Override
    public final SimpleIntegerProperty selectionIndexProperty() {
        return this.selectionIndexProperty;
    }

    public final void unlinkFromActivityFeed() {
        if (!activityFeedProperty().get().getFeedName()
                .equals(ViewProperties.UNLINKED)) {
            activityFeedProperty().get().feedSelectionProperty().removeListener(getSelectionChangedListener());
            activityFeedProperty().set(getViewProperties().getUnlinkedActivityFeed());
            activityFeedProperty().get().feedSelectionProperty().addListener(getSelectionChangedListener());
            selectionIndexProperty().set(0);
        }
    }

    @Override
    public final Optional<Node> getTitleNode() {
        if (titleLabel == null) {
            this.titleLabel = new ConceptLabelWithDragAndDrop(getViewProperties(),
                    (SimpleObjectProperty<IdentifiedObject>) identifiedObjectFocusProperty(),
                    ConceptLabelWithDragAndDrop::setPreferredText,
                    selectionIndexProperty(),
                    () -> unlinkFromActivityFeed());
            this.titleLabel.setGraphic(getTitleIconGraphic());
            this.titleProperty.set("");
        }

        return Optional.of(titleLabel);
    }

    @Override
    public final ActivityFeed getActivityFeed() {
        return this.activityFeedProperty.get();
    }
    @Override
    public final void setIdentifiedObjectFocus(IdentifiedObject identifiedObject) {
        this.identifiedObjectFocusProperty.setValue(identifiedObject);
    }

    @Override
    public final Optional<IdentifiedObject> getIdentifiedObjectFocus() {
        return Optional.ofNullable(identifiedObjectFocusProperty.get());
    }

    @Override
    public final SimpleObjectProperty<IdentifiedObject> identifiedObjectFocusProperty() {
        return identifiedObjectFocusProperty;
    }

}
