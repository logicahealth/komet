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
package org.ihtsdo.otf.tcc.test.integration;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author dylangrald
 */
public class NotTest {

    private Query q;

    public NotTest() throws IOException {

        q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                /*
                NativeIdSetBI forSet = new ConcurrentBitSet();
                forSet.add(Snomed.MOTION.getNid());
                forSet.add(Snomed.ACCELERATION.getNid());
                forSet.add(Snomed.CENTRIFUGAL_FORCE.getNid());
                forSet.add(Snomed.CONTINUED_MOVEMENT.getNid());
                forSet.add(Snomed.DECELERATION.getNid());
                forSet.add(Snomed.MOMENTUM.getNid());
                forSet.add(Snomed.VIBRATION.getNid());
                return forSet;
                */
                return Ts.get().getAllConceptNids();
                
            }

            @Override
            protected void Let() throws IOException {
                 let("motion", Snomed.MOTION);
                 let("acceleration", Snomed.ACCELERATION);
                 let("person", Snomed.PERSON);
                 let("allergic-asthma", Snomed.ALLERGIC_ASTHMA);
            }

            @Override
            protected Clause Where() {
                //return And(ConceptIsKindOf("allergic-asthma"), Not(ConceptIsKindOf("non-ige")));
                return Not(Or(ConceptIsKindOf("person"), ConceptIsKindOf("allergic-asthma")));
            }
        };
  
    }
    
    public Query getQuery(){
        return q;
    }
}
