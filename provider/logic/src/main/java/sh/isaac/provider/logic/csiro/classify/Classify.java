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



package sh.isaac.provider.logic.csiro.classify;

//~--- JDK imports ------------------------------------------------------------

import java.util.BitSet;
import java.util.Set;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.OpenIntHashSet;

import au.csiro.ontology.classification.IReasoner;
import au.csiro.ontology.model.Axiom;
import au.csiro.snorocket.core.SnorocketReasoner;

import sh.isaac.model.logic.LogicalExpressionImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/16/14.
 */

//TODO move to CSIRO specific module
//Create a classifier service...
public class Classify {
   /**
    * Execute.
    *
    * @param conceptSequences the concept sequences
    * @param roleSequences the role sequences
    * @param neverGroupRoleSequences the never group role sequences
    * @param roleGroupConceptSequence the role group concept sequence
    */
   public static void execute(BitSet conceptSequences,
                              OpenIntHashSet roleSequences,
                              OpenIntHashSet neverGroupRoleSequences,
                              int roleGroupConceptSequence) {
      final Stream<LogicalExpressionImpl> logicGraphStream = null;
      final AxiomCollector axiomCollector = new AxiomCollector(conceptSequences,
                                                               roleSequences,
                                                               neverGroupRoleSequences,
                                                               roleGroupConceptSequence);

      if (logicGraphStream != null) {
         final Set<Axiom> axioms = logicGraphStream.collect(axiomCollector);

         // Create a classifier and classify the axioms
         IReasoner r = new SnorocketReasoner();

         r.loadAxioms(axioms);
         r = r.classify();
         r.getClassifiedOntology();
      }
   }
}

