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

import sh.isaac.api.component.concept.ConceptSpecificationWithLabel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.Observable;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableVersion.PROPERTY_INDEX;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ReturnSpecificationController {

    final ObservableList<ConceptSpecification> joinProperties;
    final SimpleListProperty<ConceptSpecification> forAssemblagesProperty;
    final ObservableList<ReturnSpecificationRow> returnSpecificationRows
            = FXCollections.observableArrayList(returnSpecificationRow
                    -> new Observable[]{
                returnSpecificationRow.includeInResultsProperty(),
                returnSpecificationRow.columnNameProperty(),});
    final Manifold manifold;

    final ObservableMap<LetItemKey, Object> letItemObjectMap;
    final ObservableList<CellFunction> cellFunctions;
    LetItemKey lastStampCoordinateKey = null;

    public ReturnSpecificationController(SimpleListProperty<ConceptSpecification> forAssemblagesProperty,
            ObservableMap<LetItemKey, Object> letItemObjectMap,
            ObservableList<CellFunction> cellFunctions,
            ObservableList<ConceptSpecification> joinProperties,
            Manifold manifold) {
        this.forAssemblagesProperty = forAssemblagesProperty;
        this.forAssemblagesProperty.addListener(this::forAssemblagesListener);
        this.letItemObjectMap = letItemObjectMap;
        this.letItemObjectMap.addListener(this::letItemsListener);
        this.cellFunctions = cellFunctions;
        this.joinProperties = joinProperties;
        this.manifold = manifold;
    }

    private void letItemsListener(MapChangeListener.Change<? extends LetItemKey, ? extends Object> change) {
        LetItemKey key = change.getKey();
        if (change.wasRemoved()) {
            if (key == lastStampCoordinateKey) {
                lastStampCoordinateKey = null;
            }
            for (ReturnSpecificationRow row : returnSpecificationRows) {
                if (row.getStampCoordinateKey().equals(key)) {
                    row.setStampCoordinateKey(null);
                }
            }
            ArrayList<CellFunction> toDelete = new ArrayList();
            for (CellFunction cellFunction: cellFunctions) {
                if (key.getItemName().startsWith(cellFunction.functionName)) {
                    toDelete.add(cellFunction);
                }
            }
            cellFunctions.removeAll(toDelete);
        }
        if (change.wasAdded()) {
            if (change.getValueAdded() instanceof StampCoordinate) {
                for (ReturnSpecificationRow row : returnSpecificationRows) {
                    this.lastStampCoordinateKey = key;
                    if (row.getStampCoordinateKey() == null) {
                        row.setStampCoordinateKey(key);
                    }
                }
            }
            if (change.getValueAdded() instanceof LanguageCoordinate) {
                LanguageCoordinate lc = (LanguageCoordinate) change.getValueAdded();
                cellFunctions.add(new CellFunction(key.getItemName() + " preferred name", (t, u) -> {
                    int nid = Integer.parseInt(t);
                    LatestVersion<DescriptionVersion> description = lc.getPreferredDescription(nid, u);
                    if (description.isPresent()) {
                        return description.get().getText();
                    }
                    return "No current preferred name";
                    
                }));
                cellFunctions.add(new CellFunction(key.getItemName() + " FQN", (t, u) -> {
                    int nid = Integer.parseInt(t);
                    LatestVersion<DescriptionVersion> description = lc.getFullySpecifiedDescription(nid, u);
                    if (description.isPresent()) {
                        return description.get().getText();
                    }
                    return "No current FQN";
                    
                }));

                cellFunctions.add(new CellFunction(key.getItemName() + " definition", (t, u) -> {
                    int nid = Integer.parseInt(t);
                    LatestVersion<DescriptionVersion> description = lc.getDefinitionDescription(Get.concept(nid).getConceptDescriptionList(), u);
                    if (description.isPresent()) {
                        return description.get().getText();
                    }
                    return "No current definition";
                    
                }));

            }
        }

    }

    public ObservableList<ReturnSpecificationRow> getReturnSpecificationRows() {
        return returnSpecificationRows;
    }

    public ObservableList<ConceptSpecification> getJoinProperties() {
        return joinProperties;
    }

    private void forAssemblagesListener(ListChangeListener.Change<? extends ConceptSpecification> change) {
        returnSpecificationRows.clear();
        joinProperties.clear();
        
        
        SingleAssemblageSnapshot<Nid1_Int2_Version> snapshot
                = Get.assemblageService().getSingleAssemblageSnapshot(TermAux.ASSEMBLAGE_SEMANTIC_FIELDS, Nid1_Int2_Version.class, manifold);
        for (ConceptSpecification assemblageSpec : change.getList()) {

            for (int i = 0; i < PROPERTY_INDEX.SEMANTIC_FIELD_START.getIndex(); i++) {
                PROPERTY_INDEX property = PROPERTY_INDEX.values()[i];
                if (property != PROPERTY_INDEX.COMMITTED_STATE) {
                    ReturnSpecificationRow row = new ReturnSpecificationRow(
                            manifold.getPreferredDescriptionText(assemblageSpec),
                            manifold.getPreferredDescriptionText(property.getSpec()),
                            new CellFunction("", (t, u) -> {
                                return t;
                            }),
                            manifold.getPreferredDescriptionText(assemblageSpec) + ":"
                            + manifold.getPreferredDescriptionText(property.getSpec()),
                            assemblageSpec.getNid(), property.getSpec(), property.getIndex()
                    );
                    row.setStampCoordinateKey(lastStampCoordinateKey);
                    returnSpecificationRows.add(row);
                    joinProperties.add(new ConceptSpecificationWithLabel(row.getPropertySpecification(), row.getColumnName()));
                }
            }
            List<LatestVersion<Nid1_Int2_Version>> semanticFields
                    = snapshot.getLatestSemanticVersionsForComponentFromAssemblage(assemblageSpec);

            List<Nid1_Int2_Version> sortedActiveSemanticFields = new ArrayList<>();
            for (LatestVersion<Nid1_Int2_Version> latestSemanticField : semanticFields) {
                if (latestSemanticField.isPresent()) {
                    sortedActiveSemanticFields.add(latestSemanticField.get());
                }
            }

            sortedActiveSemanticFields.sort((o1, o2) -> {
                if (o1.getInt2() != o2.getInt2()) {
                    return Integer.compare(o1.getInt2(), o2.getInt2());
                }
                return manifold.getPreferredDescriptionText(o1.getNid1()).compareTo(manifold.getPreferredDescriptionText(o2.getNid1()));
            });
            Optional<SemanticChronology> optionalSemanticType = Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(assemblageSpec.getNid(), MetaData.SEMANTIC_TYPE____SOLOR.getNid()).findFirst();

            if (optionalSemanticType.isPresent()) {
                //TODO, this won't work when there is more than one additional field of a type. 
                LatestVersion<ComponentNidVersion> componentNidVersion = optionalSemanticType.get().getLatestVersion(manifold);
                
            }
            
            
            for (Nid1_Int2_Version semanticField : sortedActiveSemanticFields) {
                // add a sort...
                // add extra fields (STAMP)
                ReturnSpecificationRow row = new ReturnSpecificationRow(
                        manifold.getPreferredDescriptionText(assemblageSpec),
                        manifold.getPreferredDescriptionText(semanticField.getNid1()),
                        new CellFunction("", (t, u) -> {
                            return t;
                        }),
                        manifold.getPreferredDescriptionText(assemblageSpec) + ":"
                        + manifold.getPreferredDescriptionText(semanticField.getNid1()),
                        assemblageSpec.getNid(), Get.conceptSpecification(semanticField.getNid1()),
                        PROPERTY_INDEX.SEMANTIC_FIELD_START.getIndex() + semanticField.getInt2()
                );
                returnSpecificationRows.add(row);
                joinProperties.add(new ConceptSpecificationWithLabel(row.getPropertySpecification(), row.getColumnName()));
            }
        }
    }

    public void addReturnSpecificationListener(ListChangeListener<? super ReturnSpecificationRow> listener) {
        returnSpecificationRows.addListener(listener);
    }

    public void removeReturnSpecificationListener(ListChangeListener<? super ReturnSpecificationRow> listener) {
        returnSpecificationRows.removeListener(listener);
    }

}
