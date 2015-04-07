/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model.sememe.version;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.sememe.version.MutableConceptSequenceSememe;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronicleImpl;
import gov.vha.isaac.ochre.api.sememe.SememeType;

/**
 * 
 * @author kec
 */
public class ConceptSequenceSememeImpl extends SememeVersionImpl implements MutableConceptSequenceSememe {

    int conceptSequence;

    public ConceptSequenceSememeImpl(SememeChronicleImpl<? extends ConceptSequenceSememeImpl> container, State status, long time, int authorSequence, int moduleSequence, int pathSequence) {
        super(container, 
                status, time, authorSequence, moduleSequence, pathSequence);
    }
    
    public ConceptSequenceSememeImpl(SememeChronicleImpl<? extends ConceptSequenceSememeImpl> container, int stampSequence, DataBuffer data) {
        super(container, stampSequence, data);
        this.conceptSequence = data.getInt();
    }
    @Override
    protected void writeVersionData(DataBuffer data) {
        super.writeVersionData(data);
        data.putInt(conceptSequence);
    }
    
    @Override
    public SememeType getSememeType() {
        return SememeType.CONCEPT_SEQUENCE;
    };

    @Override
    public int getConceptSequence() {
        return conceptSequence;
    }

    @Override
    public void setConceptSequence(int conceptSequence) {
        checkUncommitted();
        this.conceptSequence = conceptSequence;
    }
    
}
