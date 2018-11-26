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
package sh.isaac.api.query;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;
import java.util.Map;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------
/**
 * Demonstrates the syntax to construct and compute a <code>Query</code>.
 *
 * @author kec
 */
public class QueryExample {

    /**
     * The query.
     */
    Query query;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new query example.
     */
    public QueryExample() {
        LetItemKey allergicAsthmaKey = new LetItemKey("allergic-asthma");
        LetItemKey asthmaKey = new LetItemKey("asthma");
        LetItemKey mildAsthmaKey = new LetItemKey("mild asthma");
        LetItemKey stampCoordinateKey = new LetItemKey("stamp coordinate");

        this.query
                = new Query(TermAux.SOLOR_CONCEPT_ASSEMBLAGE) {
            @Override
            public void Let() {

                let(allergicAsthmaKey, new ConceptProxy("Allergic asthma", "531abe20-8324-3db9-9104-8bcdbf251ac7"));
                let(asthmaKey, new ConceptProxy("Asthma (disorder)", "c265cf22-2a11-3488-b71e-296ec0317f96"));
                let(mildAsthmaKey, new ConceptProxy("Mild asthma (disorder)", "51971ecc-9a54-3584-9c36-d647ab00b47f"));
                let(stampCoordinateKey, Get.defaultCoordinate());
            }

            @Override
            public Clause Where() {
                return And(ConceptIsKindOf(allergicAsthmaKey),
                        Not(ConceptIsChildOf(allergicAsthmaKey), stampCoordinateKey),
                        ConceptIs(allergicAsthmaKey));

//          Union(ConceptIsKindOf(allergicAsthmaKey),
//          ConceptIsKindOf(mildAsthmaKey)));
            }
        };
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the results.
     *
     * @return the results
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws Exception the exception
     */
    public Map<ConceptSpecification, NidSet> getResults()
            throws IOException, Exception {
        return this.query.compute();
    }
}
