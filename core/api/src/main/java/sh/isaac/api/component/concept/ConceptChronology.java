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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.component.concept;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.relationship.RelationshipVersionAdaptor;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 * @param <V>
 */
public interface ConceptChronology<V extends ConceptVersion<V>>
        extends ObjectChronology<V>, ConceptSpecification {
   /**
    * A test for validating that a concept contains a description. Used
    * to validate concept proxies or concept specs at runtime.
    * @param descriptionText text to match against.
    * @return true if any version of a description matches this text.
    */
   boolean containsDescription(String descriptionText);

   /**
    * A test for validating that a concept contains an active description. Used
    * to validate concept proxies or concept specifications at runtime.
    * @param descriptionText text to match against.
    * @param stampCoordinate coordinate to determine if description is active.
    * @return true if any version of a description matches this text.
    */
   boolean containsDescription(String descriptionText, StampCoordinate stampCoordinate);

   /**
    * Create a mutable version the specified stampSequence. It is the responsibility of the caller to
    * add persist the chronicle when changes to the mutable version are complete .
    * @param stampSequence stampSequence that specifies the status, time, author, module, and path of this version.
    * @return the mutable version
    */
   V createMutableVersion(int stampSequence);

   /**
    * Create a mutable version with Long.MAX_VALUE as the time, indicating
    * the version is uncommitted. It is the responsibility of the caller to
    * add the mutable version to the commit manager when changes are complete
    * prior to committing the component.
    * @param state state of the created mutable version
    * @param ec edit coordinate to provide the author, module, and path for the mutable version
    * @return the mutable version
    */
   V createMutableVersion(State state, EditCoordinate ec);

   //~--- get methods ---------------------------------------------------------

   List<SememeChronology<? extends DescriptionSememe<?>>> getConceptDescriptionList();

   Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription(LanguageCoordinate languageCoordinate,
         StampCoordinate stampCoordinate);

   Optional<LatestVersion<LogicGraphSememe<?>>> getLogicalDefinition(StampCoordinate stampCoordinate,
         PremiseType premiseType,
         LogicCoordinate logicCoordinate);

   /**
    * Return a formatted text report showing chronology of logical definitions
    * for this concept, according to the provided parameters.
    * @param stampCoordinate specifies the ordering and currency of versions.
    * @param premiseType Stated or inferred premise type
    * @param logicCoordinate specifies the assemblages where the definitions are stored.
    * @return
    */
   String getLogicalDefinitionChronologyReport(StampCoordinate stampCoordinate,
         PremiseType premiseType,
         LogicCoordinate logicCoordinate);

   Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription(LanguageCoordinate languageCoordinate,
         StampCoordinate stampCoordinate);

   /**
    * Uses the default logic coordinate.
    * @return
    */
   List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListOriginatingFromConcept();

   List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListOriginatingFromConcept(
           LogicCoordinate logicCoordinate);

   /**
    * Uses the default logic coordinate.
    * @return
    */
   List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListWithConceptAsDestination();

   List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListWithConceptAsDestination(
           LogicCoordinate logicCoordinate);
}

