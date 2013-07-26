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
package org.ihtsdo.otf.tcc.dto;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.ttk.lookup.Looker;

/**
 *
 * @author kec
 */
public class ChronicleConverter {
    
    public static interface TtkConceptChronicleConverterBI {
        ConceptChronicleBI convert(TtkConceptChronicle chronicle) throws IOException;
    }
    
    private static TtkConceptChronicleConverterBI ttkConceptChronicleConverter;
    /**
     * 
     * @param chronicle a <code>TtkConceptChronicle</code> (an external representation
     * based on UUIDS) to be converted to a <code>ConceptChronicleBI</code> (an internal 
     * representation based on sequential integers, and not transferable between databases). 
     * @return a transient <code>ConceptChronicleBI</code> corresponding to the 
     *          <code>TtkConceptChronicle</code>. As a transient component, the  
     *          returned <code>ConceptChronicleBI</code> uses native identifiers 
     *          from the database, but the conversion will have no impact other
     *          than persisting uuid/integer identifier relationships in the underlying datastore. 
     * @throws IOException 
     */
    public static ConceptChronicleBI convert(TtkConceptChronicle chronicle) throws IOException {
        if (ttkConceptChronicleConverter == null) {
            ttkConceptChronicleConverter = Looker.lookup(TtkConceptChronicleConverterBI.class);
        }
        return ttkConceptChronicleConverter.convert(chronicle);
    }
    
    public static TtkConceptChronicle convert(ConceptChronicleBI chronicle) throws IOException {
        return new TtkConceptChronicle(chronicle);
    }
    
}
