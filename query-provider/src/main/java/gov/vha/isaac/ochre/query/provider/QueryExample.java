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
package gov.vha.isaac.ochre.query.provider;

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.io.IOException;
import java.util.UUID;

/**
 * Demonstrates the syntax to construct and compute a
 * <code>Query</code>.
 *
 * @author kec
 */
public class QueryExample {

    Query q;
    
    public QueryExample(){
        this.q = new Query(TaxonomyCoordinates.getInferredTaxonomyCoordinate(StampCoordinates.getDevelopmentLatestActiveOnly(), 
                Get.configurationService().getDefaultLanguageCoordinate())) {
            @Override
            protected ForSetSpecification ForSetSpecification() {
                return new ForSetSpecification(ComponentCollectionTypes.ALL_CONCEPTS);
            }
            
            @Override
            public void Let()  {
                let("allergic-asthma", new ConceptProxy("Allergic asthma", "531abe20-8324-3db9-9104-8bcdbf251ac7"));
                let("asthma", new ConceptProxy("Asthma (disorder)", "c265cf22-2a11-3488-b71e-296ec0317f96"));
                let("mild asthma", new ConceptProxy("Mild asthma (disorder)", "51971ecc-9a54-3584-9c36-d647ab00b47f"));
            }
            
            @Override
            public Clause Where() {
                return And(ConceptIsKindOf("asthma"),
                        Not(ConceptIsChildOf("allergic-asthma")),
                        ConceptIs("allergic-asthma"));
//                                Union(ConceptIsKindOf("allergic-asthma"),
//                                ConceptIsKindOf("mild asthma")));
            }
        };
    }
    
    public NidSet getResults() throws IOException, Exception{
        return this.q.compute();
    }
}
