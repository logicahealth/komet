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



package sh.isaac.provider.query.clauses;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.provider.query.Clause;
import sh.isaac.provider.query.ClauseComputeType;
import sh.isaac.provider.query.ClauseSemantic;
import sh.isaac.provider.query.ParentClause;
import sh.isaac.provider.query.Query;
import sh.isaac.provider.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 * Computes the set of enclosing concepts for the set of components that
 * are returned from the child clause.
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ConceptForComponent
        extends ParentClause {
   protected ConceptForComponent() {}

   public ConceptForComponent(Query enclosingQuery, Clause child) {
      super(enclosingQuery, child);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public NidSet computeComponents(NidSet incomingComponents) {
      final NidSet incomingPossibleComponentNids = NidSet.of(incomingComponents.stream());
      final NidSet outgoingPossibleConceptNids   = new NidSet();

      for (final Clause childClause: getChildren()) {
         final NidSet childPossibleComponentNids = childClause.computeComponents(incomingPossibleComponentNids);

         outgoingPossibleConceptNids.or(childPossibleComponentNids);
      }

      return outgoingPossibleConceptNids;
   }

   @Override
   public NidSet computePossibleComponents(NidSet incomingPossibleConceptNids) {
      final NidSet incomingPossibleComponentNids = NidSet.of(incomingPossibleConceptNids.stream());
      final NidSet outgoingPossibleConceptNids   = new NidSet();

      for (final Clause childClause: getChildren()) {
         final NidSet childPossibleComponentNids = childClause.computePossibleComponents(incomingPossibleComponentNids);
         final ConceptSequenceSet conceptSet     = ConceptSequenceSet.of(childPossibleComponentNids);

         outgoingPossibleConceptNids.or(NidSet.of(conceptSet));
      }

      return outgoingPossibleConceptNids;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      return POST_ITERATION;
   }

   @Override
   public WhereClause getWhereClause() {
      final WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.CONCEPT_FOR_COMPONENT);

      for (final Clause clause: getChildren()) {
         whereClause.getChildren()
                    .add(clause.getWhereClause());
      }

      return whereClause;
   }
}

