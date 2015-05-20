/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.model.concept;

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.description.ConceptDescription;
import gov.vha.isaac.ochre.api.component.concept.description.ConceptDescriptionChronology;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.ObjectChronicleImpl;
import gov.vha.isaac.ochre.model.ObjectVersionImpl;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class ConceptChronicleImpl 
    extends ObjectChronicleImpl<ObjectVersionImpl> 
    implements ConceptChronology<ObjectVersionImpl> {

    public ConceptChronicleImpl(UUID primoridalUuid, int nid, int containerSequence) {
        super(primoridalUuid, nid, containerSequence);
    }

    public ConceptChronicleImpl(DataBuffer data) {
        super(data);
    }

    @Override
    protected ObjectVersionImpl makeVersion(int stampSequence, DataBuffer bb) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getConceptSequence() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<? extends ConceptDescriptionChronology<? extends ConceptDescription>> getConceptDescriptionList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
