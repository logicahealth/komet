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
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
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
public class DescriptionRegexMatchTest {

    Query q;
    
    public DescriptionRegexMatchTest() throws IOException {
        q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                NativeIdSetBI forSet = new ConcurrentBitSet();
                forSet.add(Snomed.MOTION.getNid());
                forSet.add(Snomed.ACCELERATION.getNid());
                forSet.add(Snomed.CENTRIFUGAL_FORCE.getNid());
                forSet.add(Snomed.CONTINUED_MOVEMENT.getNid());
                forSet.add(Snomed.DECELERATION.getNid());
                forSet.add((Snomed.MOMENTUM.getNid()));
                forSet.add(Snomed.VIBRATION.getNid());
                return forSet;
            }
            
            @Override
            protected void Let() throws IOException {
            }
            
            @Override
            protected Clause Where() {
                String regex = "[Cc]entrifugal";
                return And(DescriptionRegexMatch(regex));
                //return Or(ConceptIsKindOf("allergic-asthma"), ConceptIsKindOf("respiratory disorder"));
            }
        };
        
    }
    
    public Query getQuery() {
        return q;
    }
}
