package sh.komet.gui.control.property;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.list.ImmutableList;
import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import java.util.Optional;
import java.util.UUID;

import static sh.komet.gui.control.property.ViewProperties.NAVIGATION;

public class ActivityFeed implements Marshalable {

    private static final int marshalVersion = 1;

    private static int historySize = 50;

    final UUID activityUuid;
    final ViewProperties owningViewForActivityFeed;
    final String feedName;
    final SimpleListProperty<IdentifiedObject> feedSelectionProperty;
    final SimpleListProperty<IdentifiedObject> feedHistoryProperty;

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

    private ActivityFeed(ViewProperties owningViewForActivityFeed, ByteArrayDataBuffer in) {
        this.owningViewForActivityFeed = owningViewForActivityFeed;
        this.activityUuid = in.getUuid();
        this.feedName = in.getUTF();
        this.feedSelectionProperty = new SimpleListProperty<>(this, feedName + " selection", FXCollections.observableArrayList());
        for (UUID id: in.getUuidArray()) {
            Get.identifiedObjectService().getChronology(id).ifPresent(chronology ->
                    this.feedSelectionProperty.add(chronology));
        }
        this.feedHistoryProperty = new SimpleListProperty<>(this, feedName + " history", FXCollections.observableArrayList());
        for (UUID id: in.getUuidArray()) {
            Get.identifiedObjectService().getChronology(id).ifPresent(chronology ->
                    this.feedHistoryProperty.add(chronology));
        }
        this.feedSelectionProperty.addListener(this::onChanged);
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
         out.putInt(marshalVersion);
        out.putUuid(activityUuid);
        out.putUTF(feedName);
        UUID[] selectedItems = new UUID[feedSelectionProperty.getSize()];

        int i = 0;
        for (IdentifiedObject identifiedObject: feedSelectionProperty) {
            selectedItems[i++] = identifiedObject.getPrimordialUuid();
        }
        out.putUuidArray(selectedItems);

        UUID[] historyItems = new UUID[feedHistoryProperty.getSize()];
        i = 0;
        for (IdentifiedObject identifiedObject: feedHistoryProperty) {
            historyItems[i++] = identifiedObject.getPrimordialUuid();
        }
        out.putUuidArray(historyItems);
    }


    // Using a static method rather than a constructor eliminates the need for
    // a readResolve method, but allows the implementation to decide how
    // to handle special cases.

    @Unmarshaler
    public static ActivityFeed make(ByteArrayDataBuffer in, ViewProperties owningViewForActivityFeed) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new ActivityFeed(owningViewForActivityFeed, in);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
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
                          ComponentProxy historyRecord = new ComponentProxy(additem.getNid(), Get.getTextForComponent(additem.getNid()));
                          addHistory(historyRecord, feedHistoryProperty);
                      }
                  }
              }
    }

    private static void addHistory(IdentifiedObject history, ObservableList<IdentifiedObject> historyDequeue) {
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

    public SimpleListProperty<IdentifiedObject> feedHistoryProperty() {
        return feedHistoryProperty;
    }

    @Override
    public String toString() {
        return "ActivityFeed{" +
                "feedName='" + feedName + '\'' +
                ", activityUuid=" + activityUuid +
                '}';
    }
}
