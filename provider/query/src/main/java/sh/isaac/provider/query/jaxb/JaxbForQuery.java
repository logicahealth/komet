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



package sh.isaac.provider.query.jaxb;

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
import sh.isaac.api.query.And;
import sh.isaac.api.query.AndNot;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.ComponentCollectionTypes;
import sh.isaac.api.query.ForSetSpecification;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.LetMap;
import sh.isaac.api.query.Not;
import sh.isaac.api.query.Or;
import sh.isaac.api.query.ParentClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.QueryFactory;
import sh.isaac.api.query.ReturnTypes;
import sh.isaac.api.query.Where;
import sh.isaac.api.query.Xor;
import sh.isaac.api.query.clauses.ChangedFromPreviousVersion;
import sh.isaac.api.query.clauses.ConceptForComponent;
import sh.isaac.api.query.clauses.ConceptIs;
import sh.isaac.api.query.clauses.ConceptIsChildOf;
import sh.isaac.api.query.clauses.ConceptIsDescendentOf;
import sh.isaac.api.query.clauses.ConceptIsKindOf;
import sh.isaac.api.query.clauses.DescriptionActiveLuceneMatch;
import sh.isaac.api.query.clauses.DescriptionActiveRegexMatch;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.isaac.api.query.clauses.DescriptionRegexMatch;
import sh.isaac.api.query.clauses.FullySpecifiedNameForConcept;
import sh.isaac.api.query.clauses.PreferredNameForConcept;
import sh.isaac.api.query.clauses.AssemblageContainsConcept;
import sh.isaac.api.query.clauses.AssemblageContainsKindOfConcept;
import sh.isaac.api.query.clauses.AssemblageContainsString;
import sh.isaac.api.query.clauses.AssemblageLuceneMatch;
import sh.isaac.api.query.clauses.RelRestriction;

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
               AssemblageContainsConcept.class,
               AssemblageContainsKindOfConcept.class,
               AssemblageContainsString.class,
               AssemblageLuceneMatch.class,
               RelRestriction.class);
      }

      return singleton;
   }
}

