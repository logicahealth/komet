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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.Observable;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.observable.ObservableVersion.PROPERTY_INDEX;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ReturnSpecificationController {

    final SimpleListProperty<ConceptSpecification> forAssemblagesProperty;
    final ObservableList<ReturnSpecificationRow> returnSpecificationRows = 
            FXCollections.observableArrayList(returnSpecificationRow  ->
            new Observable[] {
                returnSpecificationRow.includeInResultsProperty(),
                returnSpecificationRow.columnNameProperty(),
            });
    final Manifold manifold;

    public ReturnSpecificationController(SimpleListProperty<ConceptSpecification> forAssemblagesProperty,
            Manifold manifold) {
        this.forAssemblagesProperty = forAssemblagesProperty;
        this.forAssemblagesProperty.addListener(this::forAssemblagesListener);
        this.manifold = manifold;
    }

    public ObservableList<ReturnSpecificationRow> getReturnSpecificationRows() {
        return returnSpecificationRows;
    }

    private void forAssemblagesListener(ListChangeListener.Change<? extends ConceptSpecification> change) {
        returnSpecificationRows.clear();
        SingleAssemblageSnapshot<Nid1_Int2_Version> snapshot = 
                Get.assemblageService().getSingleAssemblageSnapshot(TermAux.ASSEMBLAGE_SEMANTIC_FIELDS, Nid1_Int2_Version.class, manifold);
        for (ConceptSpecification assemblageSpec : change.getList()) {
            
            for (int i = 0; i < PROPERTY_INDEX.SEMANTIC_FIELD_START.getIndex(); i++) {
                PROPERTY_INDEX property = PROPERTY_INDEX.values()[i];
                if (property != PROPERTY_INDEX.COMMITTED_STATE) {
                    ReturnSpecificationRow row = new ReturnSpecificationRow(
                            manifold.getPreferredDescriptionText(assemblageSpec),
                            manifold.getPreferredDescriptionText(property.getSpec()),
                            "",
                            manifold.getPreferredDescriptionText(assemblageSpec) + ":"
                            + manifold.getPreferredDescriptionText(property.getSpec()),
                            assemblageSpec.getNid(), property.getSpec()
                    );
                    returnSpecificationRows.add(row);
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
            
            for (Nid1_Int2_Version semanticField : sortedActiveSemanticFields) {
                // add a sort...
                // add extra fields (STAMP)
                    ReturnSpecificationRow row = new ReturnSpecificationRow(
                            manifold.getPreferredDescriptionText(assemblageSpec),
                            manifold.getPreferredDescriptionText(semanticField.getNid1()),
                            "",
                            manifold.getPreferredDescriptionText(assemblageSpec) + ":"
                            + manifold.getPreferredDescriptionText(semanticField.getNid1()),
                            assemblageSpec.getNid(), Get.conceptSpecification(semanticField.getNid1())
                    );
                    returnSpecificationRows.add(row);
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
