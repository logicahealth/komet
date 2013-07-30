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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author dylangrald
 */
public class ChangedFromPreviousVersionTest {
    
    Query q;
    
    public ChangedFromPreviousVersionTest() throws IOException{
        q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            private ViewCoordinate v2 = null;
            @Override
            protected NativeIdSetBI For() throws IOException {
                return Ts.get().getAllConceptNids();
            }

            @Override
            protected void Let() throws IOException {
                let("motion", Snomed.MOTION);
                let("v2", Ts.get().getViewCoordinate(UUID.fromString("2b684fe1-8baf-34ef-9d2a-df03142c915a")));
            }

            @Override
            protected Clause Where() {
                try {
                    Collection<ViewCoordinate> v2Group = Ts.get().getViewCoordinates();
                    v2 = v2Group.iterator().next();
                } catch (IOException ex) {
                    Logger.getLogger(ChangedFromPreviousVersionTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return And(ChangedFromPreviousVersion("v2"));
                //return Or(ConceptIsKindOf("allergic-asthma"), ConceptIsKindOf("respiratory disorder"));
            }
        };
        
    }
    
    public Query getQuery(){
        return q;
    }
    
}
