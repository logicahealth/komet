/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.chronicle.cc.termstore;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

/**
 *
 * @author kec
 */
class ConceptGetter implements ProcessUnfetchedConceptDataBI {
    Map<Integer, ConceptChronicleBI> conceptMap = new ConcurrentHashMap<>();
    NativeIdSetBI cNids;
    //~--- constructors -----------------------------------------------------
    //~--- constructors -----------------------------------------------------
    public ConceptGetter(NativeIdSetBI cNids) {
        super();
        this.cNids = cNids;
    }

    //~--- methods ----------------------------------------------------------
    @Override
    public boolean continueWork() {
        return true;
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
        if (cNids.isMember(cNid)) {
            ConceptChronicle c = (ConceptChronicle) fcfc.fetch();
            conceptMap.put(cNid, c);
        }
    }

    //~--- get methods ------------------------------------------------------
    @Override
    public NativeIdSetBI getNidSet() throws IOException {
        return cNids;
    }

    @Override
    public boolean allowCancel() {
        return false;
    }

    @Override
    public String getTitle() {
        return "Batch fetch of concepts";
    }
    
}
