/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.provider.query;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.IntSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.model.coordinate.LanguageCoordinateImpl;
import sh.isaac.model.coordinate.LogicCoordinateImpl;
import sh.isaac.model.coordinate.LogicCoordinateLazyBinding;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.coordinate.TaxonomyCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;
import sh.isaac.provider.query.clauses.ChangedFromPreviousVersion;
import sh.isaac.provider.query.clauses.ComponentsFromSnomedIds;
import sh.isaac.provider.query.clauses.ConceptForComponent;
import sh.isaac.provider.query.clauses.ConceptIs;
import sh.isaac.provider.query.clauses.ConceptIsChildOf;
import sh.isaac.provider.query.clauses.ConceptIsDescendentOf;
import sh.isaac.provider.query.clauses.ConceptIsKindOf;
import sh.isaac.provider.query.clauses.DescriptionActiveLuceneMatch;
import sh.isaac.provider.query.clauses.DescriptionActiveRegexMatch;
import sh.isaac.provider.query.clauses.DescriptionLuceneMatch;
import sh.isaac.provider.query.clauses.DescriptionRegexMatch;
import sh.isaac.provider.query.clauses.FullySpecifiedNameForConcept;
import sh.isaac.provider.query.clauses.PreferredNameForConcept;
import sh.isaac.provider.query.clauses.RefsetContainsConcept;
import sh.isaac.provider.query.clauses.RefsetContainsKindOfConcept;
import sh.isaac.provider.query.clauses.RefsetContainsString;
import sh.isaac.provider.query.clauses.RefsetLuceneMatch;
import sh.isaac.provider.query.clauses.RelRestriction;

//~--- classes ----------------------------------------------------------------

/**
 * The Class JaxbForQuery.
 *
 * @author kec
 */
public class JaxbForQuery {
   
   /** The singleton. */
   public static JAXBContext singleton;

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the.
    *
    * @return the JAXB context
    * @throws JAXBException the JAXB exception
    */
   public static JAXBContext get()
            throws JAXBException {
      if (singleton == null) {
         singleton = JAXBContext.newInstance(And.class,
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

