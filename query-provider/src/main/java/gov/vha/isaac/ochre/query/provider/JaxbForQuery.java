/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package gov.vha.isaac.ochre.query.provider;

import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.collections.IntSet;
import gov.vha.isaac.ochre.model.coordinate.LanguageCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.LogicCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.LogicCoordinateLazyBinding;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import gov.vha.isaac.ochre.model.coordinate.TaxonomyCoordinateImpl;
import gov.vha.isaac.ochre.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import gov.vha.isaac.ochre.model.observable.coordinate.ObservableLogicCoordinateImpl;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import gov.vha.isaac.ochre.query.provider.clauses.ChangedFromPreviousVersion;
import gov.vha.isaac.ochre.query.provider.clauses.ComponentsFromSnomedIds;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptForComponent;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptIs;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptIsChildOf;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptIsDescendentOf;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptIsKindOf;
import gov.vha.isaac.ochre.query.provider.clauses.DescriptionActiveLuceneMatch;
import gov.vha.isaac.ochre.query.provider.clauses.DescriptionActiveRegexMatch;
import gov.vha.isaac.ochre.query.provider.clauses.DescriptionLuceneMatch;
import gov.vha.isaac.ochre.query.provider.clauses.DescriptionRegexMatch;
import gov.vha.isaac.ochre.query.provider.clauses.FullySpecifiedNameForConcept;
import gov.vha.isaac.ochre.query.provider.clauses.PreferredNameForConcept;
import gov.vha.isaac.ochre.query.provider.clauses.RefsetContainsConcept;
import gov.vha.isaac.ochre.query.provider.clauses.RefsetContainsKindOfConcept;
import gov.vha.isaac.ochre.query.provider.clauses.RefsetContainsString;
import gov.vha.isaac.ochre.query.provider.clauses.RefsetLuceneMatch;
import gov.vha.isaac.ochre.query.provider.clauses.RelRestriction;

/**
 *
 * @author kec
 */
public class JaxbForQuery {

    public static JAXBContext singleton;

    public static JAXBContext get() throws JAXBException {
        if (singleton == null) {
            singleton = JAXBContext.newInstance(
                    And.class,
                    AndNot.class,
                    IntSet.class,
                    LanguageCoordinateImpl.class,
                    StampCoordinateImpl.class,
                    StampPositionImpl.class,
                    TaxonomyCoordinateImpl.class,
                    LogicCoordinateImpl.class,
                    LogicCoordinateLazyBinding.class,
                    ObservableLogicCoordinateImpl.class,
                    ObservableLanguageCoordinateImpl.class,
                    Where.class,
                    ForSetSpecification.class,
                    ComponentCollectionTypes.class,
                    StampPosition.class,
                    LetMap.class,
                    StampPath.class,
                    Query.class,
                    QueryFactory.class,
                    QueryFactory.QueryFromFactory.class,
                    ConceptSpecification.class,
                    ReturnTypes.class,
                    HashMap.class,
                    Not.class,
                    Or.class,
                    Xor.class,
                    ParentClause.class,
                    LeafClause.class,
                    Clause.class,
                    ConceptIsKindOf.class,
                    ChangedFromPreviousVersion.class,
                    ComponentsFromSnomedIds.class,
                    ConceptForComponent.class,
                    ConceptIs.class,
                    ConceptIsChildOf.class,
                    ConceptIsDescendentOf.class,
                    ConceptIsKindOf.class,
                    DescriptionActiveLuceneMatch.class,
                    DescriptionActiveRegexMatch.class,
                    DescriptionLuceneMatch.class,
                    DescriptionRegexMatch.class,
                    FullySpecifiedNameForConcept.class,
                    PreferredNameForConcept.class,
                    RefsetContainsConcept.class,
                    RefsetContainsKindOfConcept.class,
                    RefsetContainsString.class,
                    RefsetLuceneMatch.class,
                    RelRestriction.class);
        }
        return singleton;
    }
}
