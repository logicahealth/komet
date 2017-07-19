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



package sh.isaac.api.relationship;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.PremiseType;

//~--- interfaces -------------------------------------------------------------

/**
 * A transient component derived from a logical expression. Attempts to persist
 * this component will result in runtime errors.
 * The native identifier and UUID for this component is synthetic, and guaranteed unique,
 * within the life span of the ConceptChronology object from which it is derived.
 * The native identifier will not participate in UUID to nid maps or other aspects
 * of the identifier service. This object is not allowed to have associated sememes
 * because it is transient.
 * <br/>
 * The RelationshipVersionAdaptor objects cannot be retrieved from the
 * {@code IdentifiedObjectService} at this time, since they are transient. They can
 * only be retrieved by calling getRelationshipListWithConceptAsDestination or
 * getRelationshipListOriginatingFromConcept on the ConceptChronology objects.
 * <br/>
 * Components that use relationships should transition to using logic graphs directly.
 *
 * @author kec
 * @param <T> the generic type
 */
public interface RelationshipVersionAdaptor<T extends RelationshipVersionAdaptor<T>>
        extends SememeVersion {
   /**
    * Gets the chronicle key.
    *
    * @return the chronicle key
    */
   RelationshipAdaptorChronicleKey getChronicleKey();

   /**
    * Gets the destination sequence.
    *
    * @return the destination sequence
    */
   int getDestinationSequence();

   /**
    * Gets the group.
    *
    * @return the group
    */
   int getGroup();

   /**
    * Gets the node sequence.
    *
    * @return sequence of the node in the logical expression
    * from which this adaptor originated.
    */
   short getNodeSequence();

   /**
    * Gets the origin sequence.
    *
    * @return the origin sequence
    */
   int getOriginSequence();

   /**
    * Gets the premise type.
    *
    * @return the premise type
    */
   PremiseType getPremiseType();

   /**
    * Gets the type sequence.
    *
    * @return the type sequence
    */
   int getTypeSequence();
}

