package sh.komet.gui.control.property;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import org.eclipse.collections.api.list.ImmutableList;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.identity.IdentifiedObject;

import java.util.Optional;
import java.util.UUID;

public class ActivityFeed implements ManifoldCoordinate {

    final UUID activityUuid;
    final ViewProperties owningViewForActivityFeed;
    final String feedName;
    final SimpleListProperty<IdentifiedObject> feedSelectionProperty;
    final SimpleListProperty<ImmutableList<IdentifiedObject>> feedHistoryProperty;

    private ActivityFeed(ViewProperties owningViewForActivityFeed, String feedName) {
        this(owningViewForActivityFeed, feedName, UUID.randomUUID());
    }

    private ActivityFeed(ViewProperties owningViewForActivityFeed, String feedName, UUID activityUuid) {
        this.owningViewForActivityFeed = owningViewForActivityFeed;
        this.activityUuid = activityUuid;
        this.feedName = feedName;
        this.feedSelectionProperty = new SimpleListProperty<>(this, feedName + " selection", FXCollections.observableArrayList());
        this.feedHistoryProperty = new SimpleListProperty<>(this, feedName + " history", FXCollections.observableArrayList());
    }

    /**
     *
     * @return true if feedName.equals(ViewProperties.UNLINKED);
     */
    public boolean isUnlinked() {
        return this.feedName.equals(ViewProperties.UNLINKED);
    }

    /**
     *
     * @return true if feedName is NOT ViewProperties.UNLINKED
     */
    public boolean isLinked() {
        return !isUnlinked();
    }

    public void addFeedToSyndicate(ActivityFeed activityFeed) {
        activityFeed.feedSelectionProperty().addListener(this::syndicationListener);
    }
    public void removeFeedToSyndicate(ActivityFeed activityFeed) {
        activityFeed.feedSelectionProperty().removeListener(this::syndicationListener);
    }
    public void syndicationListener(ListChangeListener.Change<? extends IdentifiedObject> c) {
        feedSelectionProperty.setAll(c.getList());
    }

    public String getFullyQualifiedActivityFeedName() {
        return owningViewForActivityFeed.getViewUuid().toString() + ":" + feedName;
    }

    public static ActivityFeed createActivityFeed(ViewProperties owningViewForActivityFeed, String feedName) {
        return new ActivityFeed(owningViewForActivityFeed, feedName);
    }

    public static ActivityFeed createActivityFeed(ViewProperties owningViewForActivityFeed,
                                                  String feedName, UUID activityUuid) {
        return new ActivityFeed(owningViewForActivityFeed, feedName, activityUuid);
    }
    public static Optional<IdentifiedObject> getOptionalFocusedComponent(int indexInSelection, ImmutableList<IdentifiedObject> list) {

        if (indexInSelection < 0) {
            return Optional.empty();
        }
        if (indexInSelection >= list.size()) {
            return Optional.empty();
        }
        return Optional.of(list.get(indexInSelection));
    }

    public Optional<IdentifiedObject> getOptionalFocusedComponent(int indexInSelection) {

        if (indexInSelection < 0) {
            return Optional.empty();
        }
        if (indexInSelection >= feedSelectionProperty.size()) {
            return Optional.empty();
        }
        return Optional.of(feedSelectionProperty.get(indexInSelection));
    }


    public ViewProperties getOwningView() {
        return owningViewForActivityFeed;
    }

    public String getFeedName() {
        return feedName;
    }

    public SimpleListProperty<IdentifiedObject> feedSelectionProperty() {
        return feedSelectionProperty;
    }

    public SimpleListProperty<ImmutableList<IdentifiedObject>> feedHistoryProperty() {
        return feedHistoryProperty;
    }

    @Override
    public ManifoldCoordinateImmutable toManifoldCoordinateImmutable() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().toManifoldCoordinateImmutable();
    }

    @Override
    public NavigationCoordinate getNavigationCoordinate() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getNavigationCoordinate();
    }

    @Override
    public VertexSort getVertexSort() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getVertexSort();
    }

    @Override
    public LogicCoordinate getLogicCoordinate() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getLogicCoordinate();
    }

    @Override
    public LanguageCoordinate getLanguageCoordinate() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getLanguageCoordinate();
    }

    @Override
    public TaxonomySnapshot getDigraphSnapshot() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getDigraphSnapshot();
    }

    @Override
    public StampFilter getVertexStampFilter() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getVertexStampFilter();
    }

    @Override
    public StampFilter getEdgeStampFilter() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getEdgeStampFilter();
    }

    @Override
    public StampFilter getLanguageStampFilter() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getLanguageStampFilter();
    }

    @Override
    public String toString() {
        return "ActivityFeed{" +
                "feedName='" + feedName + '\'' +
                ", activityUuid=" + activityUuid +
                '}';
    }
}
