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
package org.ihtsdo.otf.tcc.model.cc.refex;

import gov.vha.isaac.ochre.collections.RefexSequenceSet;
import java.util.stream.Stream;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.jvnet.hk2.annotations.Contract;

/**
 * Temporary transition class. 
 * @author kec
 * @deprecated transition to SememeService
 */
@Contract
public interface RefexService {
    
    void writeRefex(RefexMember<?, ?> refex);

    RefexMember<?, ?> getRefex(int refexSequence);

    RefexSequenceSet getRefexSequencesForComponent(int componentNid);

    RefexSequenceSet getRefexSequencesForComponentFromAssemblage(int componentNid, int assemblageSequence);

    RefexSequenceSet getRefexSequencesFromAssemblage(int assemblageSequence);

    Stream<RefexMember<?, ?>> getRefexesForComponent(int componentNid);

    Stream<RefexMember<?, ?>> getRefexesFromAssemblage(int assemblageSequence);

    Stream<RefexMember<?, ?>> getRefexsForComponentFromAssemblage(int componentNid, int assemblageSequence);

    Stream<RefexMember<?, ?>> getRefexStream();
    
    Stream<RefexMember<?, ?>> getParallelRefexStream();

    void forgetXrefPair(int referencedComponentNid, NidPairForRefex nidPairForRefex);

}
