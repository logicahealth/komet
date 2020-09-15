package sh.komet.gui.control.property;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.list.ImmutableList;
import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.identity.IdentifiedObject;

import java.util.Optional;
import java.util.UUID;

public class ActivityFeed implements ManifoldCoordinate {
    private static int historySize = 50;

    final UUID activityUuid;
    final ViewProperties owningViewForActivityFeed;
    final String feedName;
    final SimpleListProperty<IdentifiedObject> feedSelectionProperty;
    final SimpleListProperty<ComponentProxy> feedHistoryProperty;

    private ActivityFeed(ViewProperties owningViewForActivityFeed, String feedName) {
        this(owningViewForActivityFeed, feedName, UUID.randomUUID());
    }

    private ActivityFeed(ViewProperties owningViewForActivityFeed, String feedName, UUID activityUuid) {
        this.owningViewForActivityFeed = owningViewForActivityFeed;
        this.activityUuid = activityUuid;
        this.feedName = feedName;
        this.feedSelectionProperty = new SimpleListProperty<>(this, feedName + " selection", FXCollections.observableArrayList());
        this.feedHistoryProperty = new SimpleListProperty<>(this, feedName + " history", FXCollections.observableArrayList());
        this.feedSelectionProperty.addListener(this::onChanged);
    }

    private void onChanged(ListChangeListener.Change<? extends IdentifiedObject> c) {
        while (c.next()) {
                  if (c.wasPermutated()) {
                      // nothing to do...
                  } else if (c.wasUpdated()) {
                      // nothing to do...
                  } else {
                      for (IdentifiedObject remitem : c.getRemoved()) {
                          // nothing to do...;
                      }
                      for (IdentifiedObject additem : c.getAddedSubList()) {
                          ComponentProxy historyRecord = new ComponentProxy(additem.getNid(), getFullyQualifiedDescriptionText(additem.getNid()));
                          addHistory(historyRecord, feedHistoryProperty);
                      }
                  }
              }
    }

    private static void addHistory(ComponentProxy history, ObservableList<ComponentProxy> historyDequeue) {
        if (history.getNid() == MetaData.UNINITIALIZED_COMPONENT____SOLOR.getNid()) {
            return;
        }
        if (historyDequeue.isEmpty() || !historyDequeue.get(0).equals(history)) {
            historyDequeue.add(0, history);

            while (historyDequeue.size() > historySize) {
                historyDequeue.remove(historySize, historyDequeue.size());
            }
        }
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

    public SimpleListProperty<ComponentProxy> feedHistoryProperty() {
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
    public TaxonomySnapshot getNavigationSnapshot() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getNavigationSnapshot();
    }

    @Override
    public StatusSet getVertexStatusSet() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getVertexStatusSet();
    }

    @Override
    public StampFilter getViewStampFilter() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getViewStampFilter();
    }

    @Override
    public EditCoordinate getEditCoordinate() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getEditCoordinate();
    }

    @Override
    public Activity getCurrentActivity() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getCurrentActivity();
    }

    @Override
    public ManifoldCoordinate makeCoordinateAnalog(long classifyTimeInEpochMillis) {
        return this.owningViewForActivityFeed.getManifoldCoordinate().makeCoordinateAnalog(classifyTimeInEpochMillis);
    }

    @Override
    public ManifoldCoordinate makeCoordinateAnalog(PremiseType premiseType) {
        return this.owningViewForActivityFeed.getManifoldCoordinate().makeCoordinateAnalog(premiseType);
    }

	@Override
    public PremiseSet getPremiseTypes() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getPremiseTypes();
    }

    @Override
    public StampFilter getVertexStampFilter() {
        return this.owningViewForActivityFeed.getManifoldCoordinate().getVertexStampFilter();
    }

    @Override
    public String toString() {
        return "ActivityFeed{" +
                "feedName='" + feedName + '\'' +
                ", activityUuid=" + activityUuid +
                '}';
    }
}
