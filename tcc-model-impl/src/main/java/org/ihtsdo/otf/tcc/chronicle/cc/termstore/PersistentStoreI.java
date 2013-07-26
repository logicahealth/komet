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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.ihtsdo.otf.tcc.chronicle.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptDataFetcherI;
import org.ihtsdo.otf.tcc.chronicle.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.ddo.store.FxTerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetPolicy;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;

/**
 *
 * @author kec
 */
public interface PersistentStoreI extends TerminologyStoreDI, FxTerminologyStoreDI {

    int getStamp(Status status, long time, int authorNid, int moduleNid, int pathNid);

    int getMaxReadOnlyStamp();

    void xrefAnnotation(RefexChronicleBI annotation) throws IOException;

    boolean hasConcept(int cNid) throws IOException;

    long getLastCancel();

    long getLastCommit();

    long incrementAndGetSequence();

    void waitTillWritesFinished();

    boolean commit(ConceptChronicleBI cc, ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) throws IOException;

    Map<String, String> getProperties() throws IOException;

    String getProperty(String key) throws IOException;

    void setProperty(String key, String value) throws IOException;

    void cancelAfterCommit(NidSetBI commitSapNids) throws IOException;

    // Method to wrap for client...
    ConceptDataFetcherI getConceptDataFetcher(int cNid) throws IOException;

    // Methods to remove from this interface...
    void addXrefPair(int nid, NidPairForRefex pair) throws IOException;

    void forgetXrefPair(int nid, NidPairForRefex pair) throws IOException;

    /**
     * @TODO modify the write concept routine to update the identifiers map (UUIDs, etc) Possibly remove
     * identifiers from Lucene?
     */
    List<NidPairForRefex> getRefexPairs(int nid) throws IOException;

    int[] getDestRelOriginNids(int cNid, NidSetBI relTypes) throws IOException;
    int[] getDestRelOriginNids(int cNid) throws IOException;
    Collection<Relationship> getDestRels(int cNid) throws IOException;
    void setConceptNidForNid(int cNid, int nid) throws IOException;

    void resetConceptNidForNid(int cNid, int nid) throws IOException;

    public void addRelOrigin(int destinationCNid, int originCNid) throws IOException;

    public void put(UUID uuid, int nid);
}
