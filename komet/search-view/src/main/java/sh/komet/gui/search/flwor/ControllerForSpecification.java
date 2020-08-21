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
package sh.komet.gui.search.flwor;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.query.AttributeFunction;
import sh.isaac.api.query.JoinProperty;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.QueryFieldSpecification;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.control.property.ViewProperties;

import java.util.*;

import static sh.isaac.api.query.AttributeFunction.*;

/**
 *
 * @author kec
 */
public abstract class ControllerForSpecification {

    final SimpleListProperty<ConceptSpecification> forAssemblagesProperty;
    final ViewProperties viewProperties;
    final ObservableList<MenuItem> addFieldItems;
    final ObservableList<JoinProperty> joinProperties;
    LetItemKey lastStampCoordinateKey = null;
    final ObservableMap<LetItemKey, Object> letItemObjectMap;
    final ObservableList<AttributeFunction> attributeFunctions;
    final TableView<List<String>> resultTable;


    public ControllerForSpecification(SimpleListProperty<ConceptSpecification> forAssemblagesProperty,
                                      ViewProperties viewProperties,
                                      ObservableList<LetItemKey> letItemKeys,
                                      ObservableList<MenuItem> addFieldItems,
                                      ObservableList<JoinProperty> joinProperties,
                                      ObservableMap<LetItemKey, Object> letItemObjectMap,
                                      ObservableList<AttributeFunction> attributeFunctions,
                                      TableView<List<String>> resultTable) {
        this.forAssemblagesProperty = forAssemblagesProperty;
        this.viewProperties = viewProperties;
        this.addFieldItems = addFieldItems;
        this.joinProperties = joinProperties;
        this.letItemObjectMap = letItemObjectMap;
        this.attributeFunctions = attributeFunctions;
        this.forAssemblagesProperty.addListener(this::forAssemblagesListener);
        this.resultTable = resultTable;
        letItemKeys.addListener(this::letItemsListListener);
        this.letItemObjectMap.addListener(this::letItemsMapListener);
        
    }

    protected abstract void clearForChange();

    protected void forAssemblagesListener(ListChangeListener.Change<? extends ConceptSpecification> change) {
        this.resultTable.getItems().clear();
        this.joinProperties.clear();
        this.addFieldItems.clear();
        SingleAssemblageSnapshot<Nid1_Int2_Version> snapshot = Get.assemblageService().getSingleAssemblageSnapshot(TermAux.ASSEMBLAGE_SEMANTIC_FIELDS, Nid1_Int2_Version.class, viewProperties.getViewStampFilter());
        for (ConceptSpecification assemblageSpec : change.getList()) {
            for (int i = 0; i < ObservableVersion.PROPERTY_INDEX.SEMANTIC_FIELD_START.getIndex(); i++) {
                ObservableVersion.PROPERTY_INDEX property = ObservableVersion.PROPERTY_INDEX.values()[i];
                if (property != ObservableVersion.PROPERTY_INDEX.COMMITTED_STATE) {
                    
                    String specificationName = viewProperties.getPreferredDescriptionText(assemblageSpec) + ":" + viewProperties.getPreferredDescriptionText(property.getSpec());
                    AttributeFunction attributeFunction = new AttributeFunction(EMPTY);
                    if (property.getSpec().equals(ObservableFields.PRIMORDIAL_UUID_FOR_COMPONENT)) {
                        attributeFunction = new AttributeFunction(PRIMORDIAL_UUID);
                    }
                    QueryFieldSpecification row = makeQueryFieldSpecification(attributeFunction, specificationName, assemblageSpec.getNid(), property.getSpec(), property.getIndex());
                    
                    addFieldItems.add(makeMenuItem(specificationName, row));
                    joinProperties.add(new JoinProperty(assemblageSpec, row.getPropertySpecification(), viewProperties.getManifoldCoordinate()));
                }
            }
            List<LatestVersion<Nid1_Int2_Version>> semanticFields = snapshot.getLatestSemanticVersionsForComponentFromAssemblage(assemblageSpec);
            List<Nid1_Int2_Version> sortedActiveSemanticFields = new ArrayList<>();
            for (LatestVersion<Nid1_Int2_Version> latestSemanticField : semanticFields) {
                if (latestSemanticField.isPresent()) {
                    sortedActiveSemanticFields.add(latestSemanticField.get());
                }
            }
            sortedActiveSemanticFields.sort((Nid1_Int2_Version o1, Nid1_Int2_Version o2) -> {
                if (o1.getInt2() != o2.getInt2()) {
                    return Integer.compare(o1.getInt2(), o2.getInt2());
                }
                return viewProperties.getPreferredDescriptionText(o1.getNid1()).compareTo(viewProperties.getPreferredDescriptionText(o2.getNid1()));
            });
            Optional<SemanticChronology> optionalSemanticType = Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(assemblageSpec.getNid(), MetaData.SEMANTIC_TYPE____SOLOR.getNid()).findFirst();
            if (optionalSemanticType.isPresent()) {
                //TODO, this won't work when there is more than one additional field of a type.
                LatestVersion<ComponentNidVersion> componentNidVersion = optionalSemanticType.get().getLatestVersion(viewProperties.getViewStampFilter());
            }
            for (Nid1_Int2_Version semanticField : sortedActiveSemanticFields) {
                // add a sort...
                // add extra fields (STAMP)
                String specificationName = viewProperties.getPreferredDescriptionText(assemblageSpec) + ":" + viewProperties.getPreferredDescriptionText(semanticField.getNid1());
                QueryFieldSpecification row = makeQueryFieldSpecification(new AttributeFunction(EMPTY), specificationName, assemblageSpec.getNid(), Get.conceptSpecification(semanticField.getNid1()), ObservableVersion.PROPERTY_INDEX.SEMANTIC_FIELD_START.getIndex() + semanticField.getInt2());
                addFieldItems.add(makeMenuItem(specificationName, row));
                joinProperties.add(new JoinProperty(assemblageSpec, row.getPropertySpecification(), viewProperties.getManifoldCoordinate()));
            }
        }
    }

    protected void letItemsMapListener(MapChangeListener.Change<? extends LetItemKey, ? extends Object> c) {
        setupAttributeFunctions();
    }
    protected void letItemsListListener(ListChangeListener.Change<? extends LetItemKey> c) {
        setupAttributeFunctions();
    }
    protected final void setupAttributeFunctions() {
        this.resultTable.getItems().clear();
        this.attributeFunctions.clear();
        this.attributeFunctions.add(new AttributeFunction(EMPTY));
        this.attributeFunctions.add(new AttributeFunction(PRIMORDIAL_UUID));
        this.attributeFunctions.add(new AttributeFunction(ALL_UUIDS));
        this.attributeFunctions.add(new AttributeFunction(EPOCH_TO_8601_DATETIME));
        this.attributeFunctions.add(new AttributeFunction(SCT_ID));
        
        List<Map.Entry<LetItemKey, Object>> manifolds = new ArrayList<>();
        List<Map.Entry<LetItemKey, Object>> conceptSpecs = new ArrayList<>();

        for (Map.Entry<LetItemKey, Object> entry: letItemObjectMap.entrySet()) {
            if (entry.getValue() instanceof ManifoldCoordinate) {
                manifolds.add(entry);
            }
            if (entry.getValue() instanceof ConceptSpecification) {
                conceptSpecs.add(entry);
            }
            if (entry.getValue() instanceof StampFilter &! (entry.getValue() instanceof ManifoldCoordinate)) {
                this.lastStampCoordinateKey = entry.getKey();
                for (QueryFieldSpecification row : getSpecificationRows()) {
                    if (row.getStampFilterKey() == null) {
                        row.setStampCoordinateKey(entry.getKey());
                    }
                }
            }
        }
        for (Map.Entry<LetItemKey, Object> entry: letItemObjectMap.entrySet()) {
            if (entry.getValue() instanceof LanguageCoordinate &! (entry.getValue() instanceof ManifoldCoordinate)) {
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + DESCRIPTION));
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + PREFERRED_NAME));
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + FQN));
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + DEFINITION));
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + DESCRIPTION_UUID));
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + PREFERRED_NAME_UUID));
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + FQN_UUID));
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + DEFINITION_UUID));
                attributeFunctions.add(new AttributeFunction(entry.getKey().getItemName() + IS_PREFERRED));
            }
        }
        for (Map.Entry<LetItemKey, Object> manifoldForFunction: manifolds) {
            for (Map.Entry<LetItemKey, Object> conceptSpec: conceptSpecs) {
                attributeFunctions.add(new AttributeFunction(KIND_OF_PREFIX + conceptSpec.getKey() + MANIFOLD_PREFIX + manifoldForFunction.getKey()));
            }
            for (Map.Entry<LetItemKey, Object> conceptSpec: conceptSpecs) {
                attributeFunctions.add(new AttributeFunction(CHILD_OF_PREFIX + conceptSpec.getKey() + MANIFOLD_PREFIX + manifoldForFunction.getKey()));
            }
            for (Map.Entry<LetItemKey, Object> conceptSpec: conceptSpecs) {
                attributeFunctions.add(new AttributeFunction(DESCENDENT_OF_PREFIX + conceptSpec.getKey() + MANIFOLD_PREFIX + manifoldForFunction.getKey()));
            }
        }
    }
    
    protected abstract Collection<? extends QueryFieldSpecification> getSpecificationRows();
    
    protected abstract MenuItem makeMenuItem(String specificationName, QueryFieldSpecification queryFieldSpecification);
    
    protected abstract QueryFieldSpecification makeQueryFieldSpecification(
            AttributeFunction attributeFunction, String specificationName, int assemblageNid,
            ConceptSpecification propertySpecification, int propertyIndex);
}
