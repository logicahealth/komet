/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.control.property;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.observable.coordinate.ObservableEditCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateBase;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateWithOverride;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author kec
 */
public class ViewProperties {
    private static final ConcurrentReferenceHashMap<UUID, ViewProperties> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public static ViewProperties get(UUID viewUuid) {
        return SINGLETONS.get(viewUuid);
    }

    public static Collection<ViewProperties> getAll() {
        return SINGLETONS.values();
    }

    public static final String ANY = "any";
    public static final String UNLINKED = "unlinked";
    public static final String SEARCH = "search";
    public static final String NAVIGATION = "navigation";
    public static final String CLASSIFICATION = "classification";
    public static final String CORRELATION = "correlation";
    public static final String LIST = "list";
    public static final String CONCEPT_BUILDER = "concept builder";
    public static final String FLWOR = "flwor";
    public static final String PREFERENCES = "preferences";


    public static final ImmutableList<String> ACTIVITY_FEED_NAMES =
            Lists.immutable.of(ANY, UNLINKED, SEARCH, NAVIGATION, CLASSIFICATION, CORRELATION, LIST, FLWOR, CONCEPT_BUILDER, PREFERENCES);

    public enum Keys {
        NAME_PREFIX, NAME_SUFFIX,
        VIEW_PROPERTIES_UUID,
        VIEW_NAME,
        VIEW_MANIFOLD_COORDINATE,
        VIEW_EDIT_COORDINATE,
    }

    private static final HashMap<String, Supplier<Node>> ICONOGRAPHIC_SUPPLIER = new HashMap<>();

    static {
        ICONOGRAPHIC_SUPPLIER.put(UNLINKED, () -> Iconography.LINK_BROKEN.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(SEARCH, () -> Iconography.SIMPLE_SEARCH.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(NAVIGATION, () -> Iconography.TAXONOMY_ICON.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(FLWOR, () -> Iconography.FLWOR_SEARCH.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(CORRELATION, () -> new Label(" C"));
        ICONOGRAPHIC_SUPPLIER.put(ANY, () -> new Label(" *"));
        ICONOGRAPHIC_SUPPLIER.put(CLASSIFICATION, () -> Iconography.INFERRED.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(LIST, () -> Iconography.LIST.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(CONCEPT_BUILDER, () -> Iconography.NEW_CONCEPT.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(PREFERENCES, () -> Iconography.SETTINGS_SLIDERS.getIconographic());
    }
    public static Optional<Node> getOptionalGraphicForActivityFeed(ActivityFeed activityFeed) {
        return getOptionalGraphicForActivityFeed(activityFeed.getFeedName());
    }
    public static Optional<Node> getOptionalGraphicForActivityFeed(String activityFeedName) {
        String[] nameParts = activityFeedName.split(":");
        if (nameParts.length > 1) {
            activityFeedName = nameParts[1];
        }
        if (ICONOGRAPHIC_SUPPLIER.get(activityFeedName) != null) {
            return Optional.of(ICONOGRAPHIC_SUPPLIER.get(activityFeedName)
                    .get());
        }
        return Optional.empty();
    }

    private final UUID viewUuid;
    private final ObservableManifoldCoordinate manifoldCoordinate;
    private final ObservableEditCoordinate editCoordinate;
    private final ViewProperties parentViewProperties;

    SimpleStringProperty viewNameProperty = new SimpleStringProperty();
    ObservableMap<String, ActivityFeed> activityFeedMap = FXCollections.observableHashMap();

    private ViewProperties(UUID viewUuid, String viewName, ObservableManifoldCoordinate observableManifoldCoordinate,
                           ObservableEditCoordinate editCoordinate) {
        this.viewUuid = viewUuid;
        this.manifoldCoordinate = observableManifoldCoordinate;
        this.editCoordinate = editCoordinate;
        this.parentViewProperties = null;
        this.viewNameProperty.set(viewName);

        activityFeedMap.put(UNLINKED, ActivityFeed.createActivityFeed(this, UNLINKED));
        activityFeedMap.put(SEARCH, ActivityFeed.createActivityFeed(this, SEARCH));
        activityFeedMap.put(FLWOR, ActivityFeed.createActivityFeed(this, FLWOR));
        activityFeedMap.put(NAVIGATION, ActivityFeed.createActivityFeed(this, NAVIGATION));
        activityFeedMap.put(CLASSIFICATION, ActivityFeed.createActivityFeed(this, CLASSIFICATION));
        activityFeedMap.put(CORRELATION, ActivityFeed.createActivityFeed(this, CORRELATION));
        activityFeedMap.put(LIST, ActivityFeed.createActivityFeed(this, LIST));
        activityFeedMap.put(CONCEPT_BUILDER, ActivityFeed.createActivityFeed(this, CONCEPT_BUILDER));
        activityFeedMap.put(ANY, ActivityFeed.createActivityFeed(this, ANY));
        linkAny();
        SINGLETONS.put(viewUuid, this);
    }

    public UUID getRootUuid() {
        if (parentViewProperties != null) {
            return parentViewProperties.viewUuid;
        }
        return viewUuid;
    }
    private ViewProperties(String viewName, ObservableManifoldCoordinate observableManifoldCoordinate,
                           ObservableEditCoordinate editCoordinate, ViewProperties parentViewProperties) {
        this.viewUuid = parentViewProperties.getViewUuid();
        this.manifoldCoordinate = observableManifoldCoordinate;
        this.editCoordinate = editCoordinate;
        this.parentViewProperties = parentViewProperties;
        this.viewNameProperty.set(viewName);
        activityFeedMap = parentViewProperties.activityFeedMap;
    }

    public ViewProperties makeOverride() {
        return new ViewProperties(this.getViewName(),
                new ObservableManifoldCoordinateWithOverride((ObservableManifoldCoordinateBase) this.getManifoldCoordinate()),
                this.editCoordinate, this);
    }

    public UUID getViewUuid() {
        return viewUuid;
    }

    public ActivityFeed getActivityFeed(String activityFeedId) {
        String[] activityFeedParts = activityFeedId.split(":");
        String activityFeedName;
        UUID viewUuid;
        ViewProperties viewForActivityFeed;
        if (activityFeedParts.length > 1) {
            activityFeedName = activityFeedParts[1];
            viewUuid = UUID.fromString(activityFeedParts[0]);
            viewForActivityFeed = ViewProperties.get(viewUuid);
        } else {
            activityFeedName = activityFeedParts[0];
            viewForActivityFeed = this;
        }
        return viewForActivityFeed.getActivityFeedMap().get(activityFeedName);
    }
    public ActivityFeed getUnlinkedActivityFeed() {
        return activityFeedMap.get(UNLINKED);
    }

    public Collection<ActivityFeed> getActivityFeeds() {
        return activityFeedMap.values();
    }


    private void linkAny() {
        ActivityFeed anyFeed = activityFeedMap.get(ANY);
        for (String key: activityFeedMap.keySet()) {
            if (!key.equals(ANY) && !key.equals(UNLINKED)) {
                ActivityFeed activityFeed = activityFeedMap.get(key);
                anyFeed.removeFeedToSyndicate(activityFeed);
                anyFeed.addFeedToSyndicate(activityFeed);
            }
        }
    }

    public static ViewProperties make(UUID viewUuid,
                                      String viewName,
                                      ObservableManifoldCoordinate providedManifold,
                                      ObservableEditCoordinate observableEditCoordinate) {
        return SINGLETONS.computeIfAbsent(viewUuid, uuid -> new ViewProperties(uuid, viewName, providedManifold, observableEditCoordinate));
    }

    public static ViewProperties make(IsaacPreferences preferencesNode) {
        Optional<UUID> optionalViewUuid = preferencesNode.getUuid(Keys.VIEW_PROPERTIES_UUID);
        if (optionalViewUuid.isEmpty()) {
            throw new IllegalStateException(Keys.VIEW_PROPERTIES_UUID + " for ViewProperties not initialized: " + preferencesNode);
        }
        Optional<String> optionalViewName = preferencesNode.get(Keys.VIEW_NAME);
        if (optionalViewName.isEmpty()) {
            throw new IllegalStateException(Keys.VIEW_NAME + " for ViewProperties not initialized: " + preferencesNode);
        }
        Optional<byte[]> optionalManifoldData = preferencesNode.getByteArray(Keys.VIEW_MANIFOLD_COORDINATE);
        if (optionalManifoldData.isEmpty()) {
            throw new IllegalStateException(Keys.VIEW_MANIFOLD_COORDINATE + " for ViewProperties not initialized: " + preferencesNode);
        }
        Optional<byte[]> optionalEditCoordinateData = preferencesNode.getByteArray(Keys.VIEW_EDIT_COORDINATE);
        if (optionalManifoldData.isEmpty()) {
            throw new IllegalStateException(Keys.VIEW_EDIT_COORDINATE + " for ViewProperties not initialized: " + preferencesNode);
        }

        return SINGLETONS.computeIfAbsent(optionalViewUuid.get(), uuid ->
                new ViewProperties(uuid,
                        optionalViewName.get(),
                        new ObservableManifoldCoordinateImpl(ManifoldCoordinateImmutable.make(new ByteArrayDataBuffer(optionalManifoldData.get()))),
                        new ObservableEditCoordinateImpl(EditCoordinateImmutable.make(new ByteArrayDataBuffer(optionalEditCoordinateData.get())))
                        ));
    }

    public void save(IsaacPreferences preferencesNode) {
        preferencesNode.putUuid(Keys.VIEW_PROPERTIES_UUID, this.getViewUuid());
        preferencesNode.put(Keys.VIEW_NAME, this.getViewName());

        ByteArrayDataBuffer manifoldBuff = new ByteArrayDataBuffer();
        this.getManifoldCoordinate().getValue().marshal(manifoldBuff);
        manifoldBuff.trimToSize();
        preferencesNode.putByteArray(Keys.VIEW_MANIFOLD_COORDINATE, manifoldBuff.getData());

        ByteArrayDataBuffer editBuff = new ByteArrayDataBuffer();
        this.getEditCoordinate().getValue().marshal(editBuff);
        editBuff.trimToSize();
        preferencesNode.putByteArray(Keys.VIEW_EDIT_COORDINATE, editBuff.getData());
    }

    public ObservableMap<String, ActivityFeed> getActivityFeedMap() {
        return activityFeedMap;
    }

    public ObservableManifoldCoordinate getManifoldCoordinate() {
        return this.manifoldCoordinate;
    }

    public ObservableEditCoordinate getEditCoordinate() {
        return this.editCoordinate;
    }

    public String getPreferredDescriptionText(int conceptNid) {
        return getManifoldCoordinate().getPreferredDescriptionText(conceptNid);
    }
    public String getPreferredDescriptionText(ConceptSpecification conceptSpecification) {
        return getManifoldCoordinate().getPreferredDescriptionText(conceptSpecification.getNid());
    }
    public String getFullyQualifiedDescriptionText(ConceptSpecification conceptSpecification) {
        return getManifoldCoordinate().getFullyQualifiedDescriptionText(conceptSpecification.getNid());
    }
    public String getFullyQualifiedDescriptionText(int conceptNid) {
        return getManifoldCoordinate().getFullyQualifiedDescriptionText(conceptNid);
    }

    public String getViewName() {
        return viewNameProperty.get();
    }

    public SimpleStringProperty viewNameProperty() {
        return viewNameProperty;
    }

    public String getDescriptionsAsText(ImmutableList<? extends IdentifiedObject> identifiedObjects) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < identifiedObjects.size(); i++) {
            buffer.append(getManifoldCoordinate().getPreferredDescriptionText(identifiedObjects.get(i).getNid()));
            if (i < identifiedObjects.size() - 1) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }
    public Optional<String> getDescriptionText(int conceptNid) {
        return getManifoldCoordinate().getDescriptionText(conceptNid);
    }
    public Optional<String> getDescriptionText(ConceptSpecification concept) {
        return getManifoldCoordinate().getDescriptionText(concept);
    }

    public ObservableStampFilter getViewStampFilter() {
        return getManifoldCoordinate().getViewStampFilter();
    }
}
