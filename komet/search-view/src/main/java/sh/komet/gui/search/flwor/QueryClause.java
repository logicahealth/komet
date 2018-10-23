/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.controlsfx.control.PropertySheet;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapperNoSearch;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.manifold.Manifold;

//~--- inner classes -------------------------------------------------------
public class QueryClause {
 
    SimpleObjectProperty<Clause> clauseProperty;
    SimpleStringProperty clauseName;
    Manifold manifold;
    SimpleListProperty<ConceptSpecification> forList;
    SimpleObjectProperty<ConceptSpecification> forSpecProperty = new SimpleObjectProperty<>(this, MetaData.FOR_ASSEMBLAGE____SOLOR.toExternalString()); 
 
    //~--- constructors -----------------------------------------------------
    protected QueryClause(Clause clause, Manifold manifold, SimpleListProperty<ConceptSpecification> forList) {
        this.manifold = manifold;
        this.forList = forList;
        this.clauseProperty = new SimpleObjectProperty<>(this, "clauseProperty", clause);
        this.clauseName = new SimpleStringProperty(this, clause.getClauseConcept().toExternalString(), manifold.getManifoldCoordinate().getPreferredDescriptionText(clause.getClauseConcept()));
        this.clauseProperty.addListener(
                (javafx.beans.value.ObservableValue<? extends sh.isaac.api.query.Clause> ov, sh.isaac.api.query.Clause oldClause, sh.isaac.api.query.Clause newClause)
                -> this.clauseName.setValue(manifold.getManifoldCoordinate().getPreferredDescriptionText(newClause.getClauseConcept())));
    }

    //~--- methods ----------------------------------------------------------
    public Node getPropertySheet() {
                PropertySheet clausePropertySheet = new PropertySheet();
                clausePropertySheet.getStyleClass().setAll("clause-properties");
                clausePropertySheet.setSearchBoxVisible(false);
                clausePropertySheet.setModeSwitcherVisible(false);
                clausePropertySheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
        switch (this.clauseProperty.get().getClauseSemantic()) {
            case AND:
                return new Pane();
            case AND_NOT:
                return new Pane();
            case ASSEMBLAGE_CONTAINS_COMPONENT:
                return new Pane();
            case ASSEMBLAGE_CONTAINS_CONCEPT:
                return new Pane();
            case ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT:
                return new Pane();
           case ASSEMBLAGE_CONTAINS_STRING:
                return new Pane();
            case ASSEMBLAGE_LUCENE_MATCH:
                return new Pane();
            case CHANGED_FROM_PREVIOUS_VERSION:
                return new Pane();
            case CONCEPT_FOR_COMPONENT:
                return new Pane();
            case CONCEPT_IS:
                return new Pane();
            case CONCEPT_IS_CHILD_OF:
                return new Pane();
            case CONCEPT_IS_DESCENDENT_OF:
                return new Pane();
            case CONCEPT_IS_KIND_OF:
                return new Pane();
            case DESCRIPTION_ACTIVE_LUCENE_MATCH:
                return new Pane();
            case DESCRIPTION_ACTIVE_REGEX_MATCH:
                 return new Pane();
            case DESCRIPTION_LUCENE_MATCH: {
                DescriptionLuceneMatch descriptionLuceneMatch = (DescriptionLuceneMatch) clauseProperty.get();
                
                clausePropertySheet.getItems().add(new PropertySheetItemConceptWrapperNoSearch(manifold, "For each", 
                forSpecProperty, forList));
                forSpecProperty.addListener((observable, oldValue, newValue) -> {
                    descriptionLuceneMatch.setAssemblageForIteration(newValue);
                });
               
                
                SimpleStringProperty queryText = new SimpleStringProperty(this, MetaData.QUERY_STRING____SOLOR.toExternalString());
                queryText.addListener((observable, oldValue, newValue) -> {
                    descriptionLuceneMatch.setParameterString(newValue);
                });

                clausePropertySheet.getItems().add(new PropertySheetTextWrapper(manifold, queryText));
                return clausePropertySheet;
            }
            case DESCRIPTION_REGEX_MATCH:
                return new Pane();
            case FULLY_QUALIFIED_NAME_FOR_CONCEPT:
                return new Pane();
            case NOT:
                return new Pane();
            case OR:
                return new Pane();
            case PREFERRED_NAME_FOR_CONCEPT:
                return new Pane();
            case RELATIONSHIP_IS_CIRCULAR:
                return new Pane();
            case REL_RESTRICTION:
                return new Pane();
            case REL_TYPE:
                return new Pane();
            case XOR:
                return new Pane();
            case JOIN:
                return new Pane();
            default:
                throw new UnsupportedOperationException("Can't handle: " + this.clauseProperty.get().getClauseSemantic());

        }
    }

    @Override
    public String toString() {
        return clauseName.get();
    }

    //~--- get methods ------------------------------------------------------
    public Clause getClause() {
        return clauseProperty.get();
    }

    public String getName() {
        return clauseName.getValue();
    }

}
