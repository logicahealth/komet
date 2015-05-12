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

import gov.vha.isaac.ochre.api.component.sememe.version.MutableConceptSequenceTimeSememe;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronicleImpl;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;

/**
 * Used for path origins by path manager. 
 * @author kec
 */
public class ConceptSequenceTimeSememeImpl extends ConceptSequenceSememeImpl
        implements MutableConceptSequenceTimeSememe {

    long sememeTime = Long.MAX_VALUE;
    
    public ConceptSequenceTimeSememeImpl(SememeChronicleImpl<ConceptSequenceTimeSememeImpl> container,  int stampSequence, DataBuffer data) {
        super(container, stampSequence, data);
        this.sememeTime = data.getLong();
    }

    public ConceptSequenceTimeSememeImpl(SememeChronicleImpl<ConceptSequenceTimeSememeImpl> container,  
            int stampSequence) {
        super(container, stampSequence);
    }

    
    @Override
    protected void writeVersionData(DataBuffer data) {
        super.writeVersionData(data);
        data.putLong(sememeTime);
    }

    @Override
    public SememeType getSememeType() {
        return SememeType.CONCEPT_SEQUENCE_TIME;
    };

    @Override
    public long getSememeTime() {
        return this.sememeTime;
    }

    @Override
    public void setSememeTime(long time) {
        if (this.sememeTime != Long.MAX_VALUE) {
            checkUncommitted();
        }
        this.sememeTime = time;
    }
    
}
