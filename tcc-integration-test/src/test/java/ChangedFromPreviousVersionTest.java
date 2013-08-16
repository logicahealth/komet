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


import java.io.IOException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Query;

/**
 * Computes components that have undergone change since a specified previous
 * version, which is specified by a <code>ViewCoordinate</code>.
 *
 * @author dylangrald
 */
public class ChangedFromPreviousVersionTest {

    Query q;
    SettingViewCoordinate setViewCoordinate = new SettingViewCoordinate(2002, 1, 31, 0, 0);

    public ChangedFromPreviousVersionTest() throws IOException {
        q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                NativeIdSetBI forSet = new ConcurrentBitSet();
                forSet.add(Snomed.BARANYS_SIGN.getNid());
                forSet.add(Snomed.NEUROLOGICAL_SYMPTOM.getNid());
                forSet.add(Snomed.ACCELERATION.getNid());
                return forSet;
                //return Ts.get().getAllConceptNids();
            }

            @Override
            protected void Let() throws IOException {
                let("v2", setViewCoordinate.getViewCoordinate());
            }

            @Override
            protected Clause Where() {
                return And(ConceptForComponent(ChangedFromPreviousVersion("v2")));
                //return Or(ConceptIsKindOf("allergic-asthma"), ConceptIsKindOf("respiratory disorder"));
            }
        };

    }

    public Query getQuery() {
        return q;
    }
}
