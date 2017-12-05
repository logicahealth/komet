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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.logic.NodeSemantic;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ConceptChronology.
 *
 * @author kec
 */
public interface ConceptChronology
        extends Chronology, 
                ConceptSpecification {
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
   @Override
   ConceptVersion createMutableVersion(int stampSequence);

   /**
    * Create a mutable version with Long.MAX_VALUE as the time, indicating
    * the version is uncommitted. It is the responsibility of the caller to
    * add the mutable version to the commit manager when changes are complete
    * prior to committing the component.
    * @param state state of the created mutable version
    * @param ec edit coordinate to provide the author, module, and path for the mutable version
    * @return the mutable version
    */
   @Override
   ConceptVersion createMutableVersion(Status state, EditCoordinate ec);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept description list.
    *
    * @return the concept description list
    */
   List<SemanticChronology> getConceptDescriptionList();

   /**
    * Gets the fully specified description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified description
    */
   LatestVersion<? extends DescriptionVersion> getFullyQualifiedNameDescription(LanguageCoordinate languageCoordinate,
         StampCoordinate stampCoordinate);
   
   /**
    * Gets the fully specified description.
    *
    * @param coordinate the manifold coordinate that specifies both the stamp coordinate and the language 
    * coordinate.
    * @return the fully specified description
    */
   default LatestVersion<? extends DescriptionVersion> getFullySpecifiedDescription(ManifoldCoordinate coordinate) {
      return getFullyQualifiedNameDescription(coordinate, coordinate);
   }

   /**
    * Gets the logical definition.
    *
    * @param stampCoordinate the stamp coordinate
    * @param premiseType the premise type
    * @param logicCoordinate the logic coordinate
    * @return the logical definition
    */
   LatestVersion<LogicGraphVersion> getLogicalDefinition(StampCoordinate stampCoordinate,
         PremiseType premiseType,
         LogicCoordinate logicCoordinate);
   
   default boolean isSufficientlyDefined(StampCoordinate stampCoordinate,
         LogicCoordinate logicCoordinate) {
      LatestVersion<LogicGraphVersion> latestDefinition = getLogicalDefinition(stampCoordinate,
         PremiseType.STATED,
         logicCoordinate);
      if (latestDefinition.isPresent()) {
         return latestDefinition.get().getLogicalExpression().contains(NodeSemantic.SUFFICIENT_SET);
      }
      return false;
   }

   /**
    * Return a formatted text report showing chronology of logical definitions
    * for this concept, according to the provided parameters.
    *
    * @param stampCoordinate specifies the ordering and currency of versions.
    * @param premiseType Stated or inferred premise type
    * @param logicCoordinate specifies the assemblages where the definitions are stored.
    * @return the logical definition chronology report
    */
   String getLogicalDefinitionChronologyReport(StampCoordinate stampCoordinate,
         PremiseType premiseType,
         LogicCoordinate logicCoordinate);

   /**
    * Gets the preferred description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the preferred description
    */
   LatestVersion<? extends DescriptionVersion> getPreferredDescription(LanguageCoordinate languageCoordinate,
         StampCoordinate stampCoordinate);


   /**
    * Gets the preferred description.
    *
    * @param coordinate the language coordinate and the stamp coordinate
    * @return the preferred description
    */
   default LatestVersion<? extends DescriptionVersion> getPreferredDescription(ManifoldCoordinate coordinate) {
      return getPreferredDescription(coordinate, coordinate);
   }

}

