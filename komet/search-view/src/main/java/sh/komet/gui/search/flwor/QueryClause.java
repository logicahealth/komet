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

import javafx.beans.property.*;
import sh.isaac.api.query.JoinProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import sh.isaac.api.query.LetItemKey;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.controlsfx.control.PropertySheet;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableConceptProxy;
import sh.isaac.api.query.clauses.ChangedBetweenVersions;
import sh.isaac.api.query.properties.*;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.Join;
import sh.isaac.api.query.JoinSpecification;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.komet.gui.control.property.wrapper.PropertySheetBooleanWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetItemIntegerWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetItemObjectListWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapperNoSearch;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

//~--- inner classes -------------------------------------------------------
public class QueryClause {

    SimpleObjectProperty<Clause> clauseProperty;
    SimpleStringProperty clauseName;
    ViewProperties viewProperties;
    SimpleObjectProperty<ConceptSpecification> forSpecProperty = new SimpleObjectProperty<>(this, MetaData.FOR_ASSEMBLAGE____SOLOR.toExternalString());
    //SimpleObjectProperty<LetItemKey> stampKeyProperty = new SimpleObjectProperty<>(this, MetaData.STAMP_COORDINATE____SOLOR.toExternalString());
    ObservableList<JoinProperty> joinProperties;
    PropertySheet clausePropertySheet = null;

    final ForPanel forPropertySheet;
    final LetPropertySheet letPropertySheet;

    List<Property<?>> clauseSpecificProperties = new ArrayList();

    //~--- constructors -----------------------------------------------------
    protected QueryClause(Clause clause, ViewProperties viewProperties, ForPanel forPropertySheet, ObservableList<JoinProperty> joinProperties,
                          LetPropertySheet letPropertySheet) {
        this.viewProperties = viewProperties;
        this.forPropertySheet = forPropertySheet;
        this.letPropertySheet = letPropertySheet;
        this.clauseProperty = new SimpleObjectProperty<>(this, "clauseProperty", clause);
        this.clauseName = new SimpleStringProperty(this, clause.getClauseConcept().toExternalString(), viewProperties.getPreferredDescriptionText(clause.getClauseConcept()));
        this.clauseProperty.addListener(
                (javafx.beans.value.ObservableValue<? extends sh.isaac.api.query.Clause> ov, sh.isaac.api.query.Clause oldClause, sh.isaac.api.query.Clause newClause)
                -> this.clauseName.setValue(viewProperties.getPreferredDescriptionText(newClause.getClauseConcept())));
        this.joinProperties = joinProperties;
    }

    //~--- methods ----------------------------------------------------------
    public Node getPropertySheet() {
        if (clausePropertySheet != null) {
            return clausePropertySheet;
        }
        clausePropertySheet = new PropertySheet();
        clausePropertySheet.getStyleClass().setAll("clause-properties");
        clausePropertySheet.setSearchBoxVisible(false);
        clausePropertySheet.setModeSwitcherVisible(false);
        clausePropertySheet.setPropertyEditorFactory(new PropertyEditorFactory(viewProperties.getManifoldCoordinate()));
        switch (this.clauseProperty.get().getClauseSemantic()) {
            case AND:
                return new Label("and");
            case AND_NOT:
                return new Label("AND_NOT");
            case ASSEMBLAGE_CONTAINS_COMPONENT:
                return new Label("ASSEMBLAGE_CONTAINS_COMPONENT");
            case CHANGED_FROM_PREVIOUS_VERSION:
                setupAssemblageForIteration("for each");
                return setupDifferenceQuery();
            case CONCEPT_IS: {
                setupAssemblageForIteration("for each");
                return setupConceptClause("is");
            }
            case CONCEPT_IS_CHILD_OF:
                setupAssemblageForIteration("for each");
                setupManifoldClause("manifold");
                return setupConceptClause("is child of");
            case CONCEPT_IS_DESCENDENT_OF:
                setupAssemblageForIteration("for each");
                setupManifoldClause("manifold");
                return setupConceptClause("is descendent of");
            case CONCEPT_IS_KIND_OF:
                setupAssemblageForIteration("for each");
                setupManifoldClause("manifold");
                return setupConceptClause("is kind of");
            case DESCRIPTION_ACTIVE_LUCENE_MATCH:
                return new Label("DESCRIPTION_ACTIVE_LUCENE_MATCH");
            case DESCRIPTION_ACTIVE_REGEX_MATCH:
                return new Label("DESCRIPTION_ACTIVE_REGEX_MATCH");
            case DESCRIPTION_LUCENE_MATCH: {
                DescriptionLuceneMatch descriptionLuceneMatch = (DescriptionLuceneMatch) clauseProperty.get();

                clausePropertySheet.getItems().add(new PropertySheetItemConceptWrapperNoSearch(viewProperties, "For each",
                        forSpecProperty, forPropertySheet.getForAssemblagesProperty()));
                forSpecProperty.addListener((observable, oldValue, newValue) -> {
                    try {
                        descriptionLuceneMatch.setAssemblageForIteration(newValue);
                    } catch (Exception e) {
                        FxGet.dialogs().showErrorDialog("Error updating after forSpecProperty change.", e);
                    }
                });

                SimpleStringProperty queryText = new SimpleStringProperty(this, MetaData.QUERY_STRING____SOLOR.toExternalString());
                queryText.setValue(descriptionLuceneMatch.getQueryText());
                queryText.addListener((observable, oldValue, newValue) -> {
                    descriptionLuceneMatch.let(descriptionLuceneMatch.getQueryStringKey(), newValue);
                });

                clausePropertySheet.getItems().add(new PropertySheetTextWrapper(viewProperties.getManifoldCoordinate(), queryText));
                return clausePropertySheet;
            }
            case DESCRIPTION_REGEX_MATCH:
                return new Label("DESCRIPTION_REGEX_MATCH");
            case NOT:
                return new Label("NOT");
            case OR:
                return new Label("OR");
            case REL_RESTRICTION:
                return new Label("REL_RESTRICTION");
            case REL_TYPE:
                return new Label("REL_TYPE");
            case XOR:
                return new Label("XOR");
            case JOIN:
                Join join = (Join) clauseProperty.get();
                List<JoinSpecificationObservable> joinSpecifications = FXCollections.observableArrayList();
                if (join.getJoinSpecifications().isEmpty()) {
                    JoinSpecificationObservable joinCriterion = new JoinSpecificationObservable();
                    joinSpecifications.add(joinCriterion);
                } else {
                    for (JoinSpecification joinSpec : join.getJoinSpecifications()) {
                        if (joinSpec instanceof JoinSpecificationObservable) {
                            joinSpecifications.add((JoinSpecificationObservable) joinSpec);
                        } else {
                            joinSpecifications.add(new JoinSpecificationObservable(joinSpec));
                        }
                    }
                }
                join.setJoinSpecifications(joinSpecifications);
                int count = 0;
                for (JoinSpecificationObservable joinSpec : joinSpecifications) {
                    setupJoinSpec(count++, joinSpec);
                }

                return clausePropertySheet;

            case COMPONENT_IS_ACTIVE:
                setupAssemblageForIteration("for each");
                return setupStampCoordinateClause("stamp");

            case REFERENCED_COMPONENT_IS:
                setupAssemblageForIteration("for each");
                return setupReferencedComponentClause("RC is");

            case REFERENCED_COMPONENT_IS_KIND_OF:
                setupAssemblageForIteration("for each");
                setupManifoldClause("manifold");
                return setupReferencedComponentClause("RC is kind of");

            case REFERENCED_COMPONENT_IS_MEMBER_OF:
                setupAssemblageForIteration("for each");
                setupManifoldClause("manifold");
                return setupReferencedComponentClause("RC is member of");

            case REFERENCED_COMPONENT_IS_NOT_KIND_OF:
                setupAssemblageForIteration("for each");
                setupManifoldClause("manifold");
                return setupReferencedComponentClause("RC is NOT kind of");
            case REFERENCED_COMPONENT_IS_NOT_MEMBER_OF:
                setupAssemblageForIteration("for each");
                setupManifoldClause("manifold");
                return setupReferencedComponentClause("RC is NOT member of");

            case SEMANTIC_CONTAINS_TEXT:
                setupAssemblageForIteration("for each");
                return setupQueryStringClause("query string");

            case REFERENCED_COMPONENT_IS_ACTIVE:
            case REFERENCED_COMPONENT_IS_INACTIVE:
                setupAssemblageForIteration("for each");
                return setupStampCoordinateClause("stamp key");

            case CONCEPT_HAS_TAXONOMY_DISTANCE_FROM:
                setupAssemblageForIteration("for each");
                setupManifoldClause("manifold");
                setupTaxonomyDistanceClause("taxonomy distance");
                setupDirectedTaxonomyClause("undirected");
                return setupConceptClause("distance from");

            default:
                throw new UnsupportedOperationException("Can't handle: " + this.clauseProperty.get().getClauseSemantic());
        }
    }

    protected void setupJoinSpec(int count, JoinSpecificationObservable joinSpec) {
        String suffix = "";
        if (count > 0) {
            suffix = " " + count;
        }
        clausePropertySheet.getItems().add(new PropertySheetItemConceptWrapperNoSearch(viewProperties, "join" + suffix,
                joinSpec.firstAssemblageProperty(), forPropertySheet.getForAssemblagesProperty()));
        clausePropertySheet.getItems().add(new PropertySheetItemConceptWrapperNoSearch(viewProperties, "with",
                joinSpec.secondAssemblageProperty(), forPropertySheet.getForAssemblagesProperty()));
        // need field list here
        clausePropertySheet.getItems().add(new PropertySheetItemObjectListWrapper("where",
                joinSpec.firstFieldProperty(), joinProperties));
        clausePropertySheet.getItems().add(new PropertySheetItemObjectListWrapper("equals",
                joinSpec.secondFieldProperty, joinProperties));

        clausePropertySheet.getItems().add(new PropertySheetItemObjectListWrapper("stamp",
                joinSpec.stampCoordinateKeyProperty(), letPropertySheet.getStampFilterKeys()));
        if (joinSpec.getStampFilterKey() == null & !letPropertySheet.getStampFilterKeys().isEmpty()) {
            joinSpec.setStampCoordinateKey(letPropertySheet.getStampFilterKeys().get(0));
        }
        joinSpec.stampCoordinateKeyProperty().set(joinSpec.getStampFilterKey());
    }
    protected PropertySheet setupDifferenceQuery() {
        ChangedBetweenVersions changedClause = (ChangedBetweenVersions) clauseProperty.get();
        setupStampKeyProperty(changedClause::getStampCoordinateOneKey,  changedClause::setStampCoordinateOneKey, "Start STAMP");
        return setupStampKeyProperty(changedClause::getStampCoordinateTwoKey,  changedClause::setStampCoordinateTwoKey, "End STAMP");
    }

    private PropertySheet setupStampKeyProperty(Supplier<LetItemKey> supplier, Consumer<LetItemKey> consumer, String label) {
        throw new UnsupportedOperationException();
//        SimpleObjectProperty<LetItemKey> stampKeyForClauseProperty = new SimpleObjectProperty<>(this, MetaData.ORIGIN_STAMP_COORDINATE_KEY_FOR_MANIFOLD____SOLOR.toExternalString());
//        this.clauseSpecificProperties.add(stampKeyForClauseProperty);
//        if (supplier.get() == null & !letPropertySheet.getStampFilterKeys().isEmpty()) {
//            consumer.accept(letPropertySheet.getStampFilterKeys().get(0));
//        }
//        stampKeyForClauseProperty.set(supplier.get());
//
//        clausePropertySheet.getItems().add(new PropertySheetItemObjectListWrapper(label,
//                stampKeyForClauseProperty, letPropertySheet.getStampFilterKeys()));
//        stampKeyForClauseProperty.addListener((observable, oldValue, newValue) -> {
//            consumer.accept(newValue);
//        });
//        return clausePropertySheet;
    }

    protected PropertySheet setupStampCoordinateClause(String keyName) {
        StampCoordinateClause stampClause = (StampCoordinateClause) clauseProperty.get();
        return setupStampKeyProperty(stampClause::getStampCoordinateKey, stampClause::setStampCoordinateKey, keyName);
    }


    protected PropertySheet setupQueryStringClause(String keyName) {
        QueryStringClause queryStringClause = (QueryStringClause) clauseProperty.get();
        SimpleObjectProperty<LetItemKey> queryKeyProperty = new SimpleObjectProperty<>(this, MetaData.LET_ITEM_KEY____SOLOR.toExternalString());
        queryKeyProperty.setValue(queryStringClause.getQueryStringKey());
        this.clauseSpecificProperties.add(queryKeyProperty);
        clausePropertySheet.getItems().add(new PropertySheetItemObjectListWrapper("string key",
                queryKeyProperty, letPropertySheet.getStringKeys()));
        if (queryStringClause.getQueryText() == null) {
            letPropertySheet.getLetItemObjectMap().put(queryStringClause.getQueryStringKey(), "");
        }

        SimpleStringProperty queryStringProperty = new SimpleStringProperty(this, MetaData.QUERY_STRING____SOLOR.toExternalString(),
                (String) letPropertySheet.getLetItemObjectMap().get(queryKeyProperty.get()));

        queryStringProperty.addListener((observable, oldValue, newValue) -> {
            letPropertySheet.getLetItemObjectMap().put(queryKeyProperty.get(), newValue);
        });

        clausePropertySheet.getItems().add(new PropertySheetTextWrapper(viewProperties.getManifoldCoordinate(),
                queryStringProperty));

        SimpleBooleanProperty regexProperty = new SimpleBooleanProperty(this, MetaData.QUERY_STRING_IS_REGEX____SOLOR.toExternalString(), queryStringClause.isRegex());
        this.clauseSpecificProperties.add(regexProperty);
        regexProperty.addListener((observable, oldValue, newValue) -> {
            queryStringClause.setRegex(newValue);
        });
        clausePropertySheet.getItems().add(new PropertySheetBooleanWrapper("is regex", regexProperty));

        return this.clausePropertySheet;
    }

    protected PropertySheet setupAssemblageForIteration(String keyName) {
        AssemblageForIterationClause assemblageForIterationClause = (AssemblageForIterationClause) clauseProperty.get();
        clausePropertySheet.getItems().add(new PropertySheetItemConceptWrapperNoSearch(viewProperties, keyName,
                forSpecProperty, forPropertySheet.getForAssemblagesProperty()));

        if (assemblageForIterationClause.getAssemblageForIteration() == null || assemblageForIterationClause.getAssemblageForIteration().equals(TermAux.UNINITIALIZED_COMPONENT_ID)) {
            if (!forPropertySheet.getForAssemblagesProperty().isEmpty()) {
                assemblageForIterationClause.setAssemblageForIteration(forPropertySheet.getForAssemblagesProperty().get(0));
            }
        }

        forSpecProperty.addListener((observable, oldValue, newValue) -> {
            assemblageForIterationClause.setAssemblageForIteration(newValue);
        });

        forPropertySheet.getForAssemblagesProperty().addListener((ListChangeListener.Change<? extends ConceptSpecification> c) -> {
            if (assemblageForIterationClause.getAssemblageForIteration().equals(TermAux.UNINITIALIZED_COMPONENT_ID)) {
                assemblageForIterationClause.setAssemblageForIteration(forPropertySheet.getForAssemblagesProperty().get(0));
            }
        });

        if (!forPropertySheet.getForAssemblagesProperty().isEmpty() && assemblageForIterationClause.getAssemblageForIteration().equals(TermAux.UNINITIALIZED_COMPONENT_ID)) {
            assemblageForIterationClause.setAssemblageForIteration(forPropertySheet.getForAssemblagesProperty().get(0));
        }
        return this.clausePropertySheet;
    }

    protected PropertySheet setupManifoldClause(String keyName) {
        ManifoldClause manifoldClause = (ManifoldClause) clauseProperty.get();
        SimpleObjectProperty<LetItemKey> manifoldKeyProperty = new SimpleObjectProperty<>(this, MetaData.MANIFOLD_COORDINATE_REFERENCE____SOLOR.toExternalString());
        manifoldKeyProperty.set(manifoldClause.getManifoldCoordinateKey());
        this.clauseSpecificProperties.add(manifoldKeyProperty);
        clausePropertySheet.getItems().add(new PropertySheetItemObjectListWrapper("manifold",
                manifoldKeyProperty, letPropertySheet.getManifoldCoordinateKeys()));
        manifoldKeyProperty.addListener((observable, oldValue, newValue) -> {
            manifoldClause.setManifoldCoordinateKey((LetItemKey) newValue);
        });
        return clausePropertySheet;
    }

    protected PropertySheet setupDirectedTaxonomyClause(String keyName) {
        UndirectedTaxonomyClause undirectedTaxonomyClause = (UndirectedTaxonomyClause) clauseProperty.get();
        SimpleObjectProperty<LetItemKey> undirectedTaxonomyKeyProperty = new SimpleObjectProperty<>(this, TermAux.BOOLEAN_REFERENCE.toExternalString());

        LetItemKey undirectedTaxonomyKey = undirectedTaxonomyClause.getUndirectedTaxonomyKey();
        if (undirectedTaxonomyKey == null) {
            undirectedTaxonomyKey = new LetItemKey(keyName);
            undirectedTaxonomyKeyProperty.set(undirectedTaxonomyKey);
            letPropertySheet.getLetItemObjectMap().put(undirectedTaxonomyKey, Boolean.TRUE);
        }

        this.clauseSpecificProperties.add(undirectedTaxonomyKeyProperty);
        SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(this, MetaData.BOOLEAN_FIELD____SOLOR.toExternalString());
        booleanProperty.set((Boolean) letPropertySheet.getLetItemObjectMap().get(undirectedTaxonomyKey));
        this.clauseSpecificProperties.add(booleanProperty);
        this.clausePropertySheet.getItems().add(new PropertySheetBooleanWrapper(this.viewProperties.getManifoldCoordinate(), booleanProperty));
        return clausePropertySheet;
    }


    protected PropertySheet setupTaxonomyDistanceClause(String keyName) {
        TaxonomyDistanceClause taxonomyDistanceClause = (TaxonomyDistanceClause) clauseProperty.get();
        SimpleObjectProperty<LetItemKey> taxonomyDistanceKeyProperty = new SimpleObjectProperty<>(this, TermAux.INTEGER_REFERENCE.toExternalString());

        LetItemKey taxonomyDistanceKey = taxonomyDistanceClause.getTaxonomyDistanceKey();
        if (taxonomyDistanceKey == null) {
            taxonomyDistanceKey = new LetItemKey(keyName);
            taxonomyDistanceClause.setTaxonomyDistanceKey(taxonomyDistanceKey);
            letPropertySheet.getLetItemObjectMap().put(taxonomyDistanceKey, 2);
        }


        this.clauseSpecificProperties.add(taxonomyDistanceKeyProperty);
        SimpleIntegerProperty integerProperty = new SimpleIntegerProperty(this, MetaData.INTEGER_FIELD____SOLOR.toExternalString());
        this.clauseSpecificProperties.add(integerProperty);
        integerProperty.set((Integer) letPropertySheet.getLetItemObjectMap().get(taxonomyDistanceKey));
        this.clausePropertySheet.getItems().add(new PropertySheetItemIntegerWrapper(this.viewProperties, integerProperty));
        return clausePropertySheet;
    }

    protected PropertySheet setupConceptClause(String keyName) {
        SimpleObjectProperty<LetItemKey> conceptSpecificationKeyProperty = new SimpleObjectProperty<>(this, MetaData.CONCEPT_REFERENCE____SOLOR.toExternalString());
        this.clauseSpecificProperties.add(conceptSpecificationKeyProperty);
        SimpleObjectProperty<ConceptSpecification> conceptSpecProperty = new SimpleObjectProperty<>(this, MetaData.CONCEPT_FIELD____SOLOR.toExternalString());
        this.clauseSpecificProperties.add(conceptSpecProperty);
        ConceptClause conceptClauseAbstract = (ConceptClause) clauseProperty.get();

        LetItemKey conceptKey = conceptClauseAbstract.getConceptSpecKey();
        if (conceptKey == null) {
            conceptKey = new LetItemKey(keyName);
            conceptClauseAbstract.setConceptSpecKey(conceptKey);
            letPropertySheet.getLetItemObjectMap().put(conceptKey, TermAux.UNINITIALIZED_COMPONENT_ID);
        }

        clausePropertySheet.getItems().add(new PropertySheetItemObjectListWrapper("key",
                conceptSpecificationKeyProperty, this.letPropertySheet.getConceptSpecificationKeys()));

        conceptSpecificationKeyProperty.addListener((observable, oldValue, newValue) -> {
            conceptClauseAbstract.setConceptSpecKey((LetItemKey) newValue);
            conceptSpecProperty.set((ConceptSpecification) letPropertySheet.getLetItemObjectMap().get(newValue));
        });
        letPropertySheet.getLetItemObjectMap().addListener((MapChangeListener.Change<? extends LetItemKey, ? extends Object> change) -> {
            LetItemKey key = change.getKey();
            if (key.equals(conceptSpecificationKeyProperty.get())) {
                if (change.wasAdded()) {
                    if (change.getValueAdded() instanceof ObservableConceptProxy) {
                        conceptSpecProperty.setValue(((ObservableConceptProxy) change.getValueAdded()).get());
                    } else {
                        conceptSpecProperty.setValue((ConceptSpecification) change.getValueAdded());
                    }
                    
                }
            }
        });

        conceptSpecificationKeyProperty.set(conceptKey);

        clausePropertySheet.getItems().add(new PropertySheetItemConceptWrapper(viewProperties.getManifoldCoordinate(),
                "concept", conceptSpecProperty));
        conceptSpecProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue = new ConceptProxy(newValue);
            }
            letPropertySheet.getLetItemObjectMap().put(conceptClauseAbstract.getConceptSpecKey(), newValue);
        });
        return clausePropertySheet;
    }

    protected PropertySheet setupReferencedComponentClause(String keyName) {
        SimpleObjectProperty<LetItemKey> conceptSpecificationKeyProperty = new SimpleObjectProperty<>(this, MetaData.CONCEPT_REFERENCE____SOLOR.toExternalString());
        this.clauseSpecificProperties.add(conceptSpecificationKeyProperty);
        SimpleObjectProperty<ConceptSpecification> conceptSpecProperty = new SimpleObjectProperty<>(this, MetaData.CONCEPT_FIELD____SOLOR.toExternalString());
        this.clauseSpecificProperties.add(conceptSpecProperty);
        ReferencedComponentClause referencedComponentAbstract = (ReferencedComponentClause) clauseProperty.get();

        LetItemKey referencedComponentKey = referencedComponentAbstract.getReferencedComponentSpecKey();
        if (referencedComponentKey == null) {
            referencedComponentKey = new LetItemKey(keyName);
            referencedComponentAbstract.setReferencedComponentSpecKey(referencedComponentKey);
            letPropertySheet.getLetItemObjectMap().put(referencedComponentKey, TermAux.UNINITIALIZED_COMPONENT_ID);
        }

        clausePropertySheet.getItems().add(new PropertySheetItemObjectListWrapper(keyName + " key",
                conceptSpecificationKeyProperty, this.letPropertySheet.getConceptSpecificationKeys()));

        conceptSpecificationKeyProperty.addListener((observable, oldValue, newValue) -> {
            referencedComponentAbstract.setReferencedComponentSpecKey((LetItemKey) newValue);
            conceptSpecProperty.set((ConceptSpecification) letPropertySheet.getLetItemObjectMap().get(newValue));
        });
        letPropertySheet.getLetItemObjectMap().addListener((MapChangeListener.Change<? extends LetItemKey, ? extends Object> change) -> {
            LetItemKey key = change.getKey();
            if (key.equals(conceptSpecificationKeyProperty.get())) {
                if (change.wasRemoved() & !change.wasAdded()) {
                    conceptSpecProperty.setValue(null);
                }
                if (change.wasAdded()) {
                    conceptSpecProperty.setValue((ConceptSpecification) change.getValueAdded());
                }
            }
        });

        conceptSpecificationKeyProperty.set(referencedComponentKey);

        clausePropertySheet.getItems().add(new PropertySheetItemConceptWrapper(viewProperties.getManifoldCoordinate(),
                "concept", conceptSpecProperty));
        conceptSpecProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue = new ConceptProxy(newValue);
            }
            letPropertySheet.getLetItemObjectMap().put(referencedComponentAbstract.getReferencedComponentSpecKey(), newValue);
        });
        return clausePropertySheet;
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
