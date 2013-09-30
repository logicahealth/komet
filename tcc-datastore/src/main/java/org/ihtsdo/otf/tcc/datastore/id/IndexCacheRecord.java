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
package org.ihtsdo.otf.tcc.datastore.id;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.otf.tcc.datastore.Bdb;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.version.RelativePositionComputerBI;

/**
 * Stores cross-reference information for origin relationships, destination
 * relationship origins, and refex referenced components in a integer array,
 * minimizing the object allocation burden that would otherwise be associated
 * with this information. This class interprets and manages the contents of that
 * array.
 * <br>
 * <h2>Implementation notes</h2>
 * See the class
 * <code>RelationshipIndexRecord</code> for documentation of the structure of
 * the relationship index data.
 *
 * @see RelationshipIndexRecord
 * @author kec
 */
public class IndexCacheRecord {

    /**
     * Used to determine when a component was last indexed, so that
     * we can determine if it needs to be indexed again. 
     */
    private static final int DESTINATION_OFFSET_INDEX = 0;
    private static final int REFEX_OFFSET_INDEX = 1;
    private static final int RELATIONSHIP_OFFSET = 2;
    private int[] data;

    public IndexCacheRecord() {
        this.data = new int[]{2, 2};
    }

    public IndexCacheRecord(int[] data) {
        this.data = data;

        if (data == null) {
            this.data = new int[]{2, 2};
        }
    }
    
    public boolean isRefexMemberAlreadyThere(int memberNid) {
        int arrayLength = data.length - data[REFEX_OFFSET_INDEX];
        int start = data.length - arrayLength;

        for (int i = start; i < data.length; i++) {
            if (data[i] == memberNid) {
                return true;
            }
        }

        return false;
    }

    public boolean isDestinationRelOriginAlreadyThere(int originNid) {
        int arrayLength = data[REFEX_OFFSET_INDEX] - data[DESTINATION_OFFSET_INDEX];
        int index = Arrays.binarySearch(data, data[DESTINATION_OFFSET_INDEX],
                data[DESTINATION_OFFSET_INDEX] + arrayLength, originNid);

        if (index >= 0) {
            return true;    // origin already there...
        }

        return false;
    }

    public void addDestinationOriginNid(int originNid) {
        if (!isDestinationRelOriginAlreadyThere(originNid)) {
            int arrayLength = data[REFEX_OFFSET_INDEX] - data[DESTINATION_OFFSET_INDEX];
            int[] destinationOriginNids = new int[arrayLength + 1];

            destinationOriginNids[arrayLength] = originNid;
            System.arraycopy(data, data[DESTINATION_OFFSET_INDEX], destinationOriginNids, 0,
                    destinationOriginNids.length - 1);
            Arrays.sort(destinationOriginNids);
            updateData(getRelationshipOutgoingArray(), destinationOriginNids, getRefexIndexArray());
        }
    }

    public void addNidPairForRefex(int refexNid, int memberNid) {
        if (!isRefexMemberAlreadyThere(memberNid)) {
            int arrayLength = data.length - data[REFEX_OFFSET_INDEX];
            int[] nidPairForRefexArray = new int[arrayLength + 2];

            nidPairForRefexArray[arrayLength] = refexNid;
            nidPairForRefexArray[arrayLength + 1] = memberNid;
            System.arraycopy(data, data[REFEX_OFFSET_INDEX], nidPairForRefexArray, 0, nidPairForRefexArray.length - 2);
            updateData(getRelationshipOutgoingArray(), getDestinationOriginNids(), nidPairForRefexArray);
        }
    }

    public void forgetNidPairForRefex(int refexNid, int memberNid) {
        int arrayLength = data.length - data[REFEX_OFFSET_INDEX];
        int start = data.length - arrayLength;

        for (int i = start; i < data.length; i++) {
            if (data[i] == memberNid) {
                int[] nidPairForRefexArray = new int[arrayLength - 2];

                System.arraycopy(data, data[REFEX_OFFSET_INDEX], nidPairForRefexArray, 0, i);
                System.arraycopy(data, i + 2, nidPairForRefexArray, 0, arrayLength - i);
                updateData(getRelationshipOutgoingArray(), getDestinationOriginNids(), nidPairForRefexArray);

                return;
            }
        }
    }

    public int[] updateData(int[] relationshipOutgoingData, int[] destinationOriginData, int[] refexData) {
        int length = relationshipOutgoingData.length + destinationOriginData.length + refexData.length
                + RELATIONSHIP_OFFSET;

        data = new int[length];
        data[DESTINATION_OFFSET_INDEX] = relationshipOutgoingData.length + RELATIONSHIP_OFFSET;
        data[REFEX_OFFSET_INDEX] = data[DESTINATION_OFFSET_INDEX] + destinationOriginData.length;
        System.arraycopy(relationshipOutgoingData, 0, data, RELATIONSHIP_OFFSET, relationshipOutgoingData.length);
        System.arraycopy(destinationOriginData, 0, data, data[DESTINATION_OFFSET_INDEX], destinationOriginData.length);
        System.arraycopy(refexData, 0, data, data[REFEX_OFFSET_INDEX], refexData.length);

        return data;
    }

    public int[] getData() {
        if (data.length == 3) {
            return null;
        }

        return data;
    }

    /**
     *
     * @return int[] of relationship nids that point to this component
     */
    public int[] getDestRelNids(int cNid) throws IOException {
        HashSet<Integer> returnValues = new HashSet<>();
        int[] originCNids = getDestinationOriginNids();

        for (int originCNid : originCNids) {
            ConceptChronicleBI c = Bdb.getConcept(originCNid);

            for (RelationshipChronicleBI r : c.getRelationshipsOutgoing()) {
                if (r.getDestinationNid() == cNid) {
                    returnValues.add(r.getNid());
                }
            }
        }

        int[] returnValueArray = new int[returnValues.size()];
        int i = 0;

        for (Integer nid : returnValues) {
            returnValueArray[i++] = nid;
        }

        return returnValueArray;
    }

    /**
     *
     * @param relTypes
     * @return int[] of relationship nids that point to this component
     */
    public int[] getDestRelNids(int cNid, NidSetBI relTypes) throws IOException {
        HashSet<Integer> returnValues = new HashSet<>();
        int[] originCNids = getDestinationOriginNids();

        for (int originCNid : originCNids) {
            ConceptChronicleBI c = Ts.get().getConcept(originCNid);

            for (RelationshipChronicleBI r : c.getRelationshipsOutgoing()) {
                if (r.getDestinationNid() == cNid) {
                    for (RelationshipVersionBI rv : r.getVersions()) {
                        if (relTypes.contains(rv.getTypeNid())) {
                            returnValues.add(r.getNid());

                            break;
                        }
                    }
                }
            }
        }

        int[] returnValueArray = new int[returnValues.size()];
        int i = 0;

        for (Integer nid : returnValues) {
            returnValueArray[i++] = nid;
        }

        return returnValueArray;
    }

    /**
     *
     * @param cNid
     * @param relTypes
     * @return
     * @throws IOException
     */
    public NativeIdSetBI getDestRelNidsSet(int cNid, NativeIdSetBI relTypes, ViewCoordinate vc) throws IOException, ContradictionException {
        NativeIdSetBI returnValues = new ConcurrentBitSet();
        int[] originCNids = getDestinationOriginNids();

        for (int originCNid : originCNids) {
            ConceptVersionBI c = Ts.get().getConceptVersion(vc, originCNid);
            NativeIdSetItrBI iter = relTypes.getIterator();
            while (iter.next()) {
                for (ConceptVersionBI concept : c.getRelationshipsOutgoingDestinationsActive(iter.nid())) {
                    if (cNid == concept.getNid()) {
                        returnValues.add(originCNid);
                        break;
                    }
                }
            }

        }
        return returnValues;
    }

    /**
     * Computes the
     * <code>NativeIdSetBI</code> of .
     *
     * @param cNid
     * @param relType
     * @param vc
     * @return
     * @throws IOException
     * @throws ContradictionException
     */
    public NativeIdSetBI getDestRelNidsSet(int cNid, int relType, ViewCoordinate vc) throws IOException, ContradictionException {
        NativeIdSetBI returnValues = new ConcurrentBitSet();
        int[] originCNids = getDestinationOriginNids();
        for (int originCNid : originCNids) {
            ConceptVersionBI c = Ts.get().getConceptVersion(vc, originCNid);
            for (ConceptVersionBI concept : c.getRelationshipsOutgoingDestinationsActive(relType)) {
                if (cNid == concept.getNid()) {
                    returnValues.add(originCNid);
                    break;
                }
            }
        }
        return returnValues;
    }

    public NativeIdSetBI getOutgoingRelNidSet(int cNid, NativeIdSetBI relTypes) throws IOException {
        NativeIdSetBI returnValues = new ConcurrentBitSet();
        int[] outgoingNids = getRelationshipOutgoingArray();

        for (int outgoingNid : outgoingNids) {
            ConceptChronicleBI c = Ts.get().getConcept(outgoingNid);
            for (RelationshipChronicleBI r : c.getRelationshipsOutgoing()) {
                if (r.getDestinationNid() == cNid) {
                    for (RelationshipVersionBI rv : r.getVersions()) {
                        if (relTypes.contains(rv.getTypeNid())) {
                            returnValues.add(r.getNid());

                            break;
                        }
                    }
                }
            }
        }
        return returnValues;


    }

    /**
     *
     * @param vc
     * @return int[] of relationship nids that point to this component
     */
    public int[] getDestRelNids(int cNid, ViewCoordinate vc) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Collection<Relationship> getDestRels(int cNid) throws IOException {
        HashSet<Relationship> returnValues = new HashSet<>();
        int[] originCNids = getDestinationOriginNids();

        for (int originCNid : originCNids) {
            ConceptChronicle c = ConceptChronicle.get(originCNid);

            for (Relationship r : c.getRelationshipsOutgoing()) {
                if (r.getDestinationNid() == cNid) {
                    returnValues.add(r);
                }
            }
        }

        return returnValues;
    }

    /**
     *
     * @return int[] of concept nids with relationships that point to this
     * component
     */
    public int[] getDestinationOriginNids() {
        int arrayLength = data[REFEX_OFFSET_INDEX] - data[DESTINATION_OFFSET_INDEX];
        int[] destinationOriginNids = new int[arrayLength];

        System.arraycopy(data, data[DESTINATION_OFFSET_INDEX], destinationOriginNids, 0, arrayLength);

        return destinationOriginNids;
    }

    public NidPairForRefex[] getNidPairsForRefsets() {
        int arrayLength = data.length - data[REFEX_OFFSET_INDEX];

        assert arrayLength % 2 == 0;

        if (arrayLength < 2) {
            return new NidPairForRefex[0];
        }

        NidPairForRefex[] returnValues = new NidPairForRefex[arrayLength / 2];
        int start = data[REFEX_OFFSET_INDEX];
        int returnIndex = 0;

        for (int i = start; i < data.length; i = i + 2) {
            returnValues[returnIndex++] = NidPairForRefex.getRefexNidMemberNidPair(data[i], data[i + 1]);
        }

        return returnValues;
    }

    public int[] getRefexIndexArray() {
        int arrayLength = data.length - data[REFEX_OFFSET_INDEX];
        int[] relationshipOutgoingArray = new int[arrayLength];

        if (arrayLength > 0) {
            System.arraycopy(data, data[REFEX_OFFSET_INDEX], relationshipOutgoingArray, 0, arrayLength);
        }

        return relationshipOutgoingArray;
    }

    public int[] getRelationshipOutgoingArray() {
        int arrayLength = data[DESTINATION_OFFSET_INDEX] - RELATIONSHIP_OFFSET;
        int[] relationshipOutgoingArray = new int[arrayLength];

        if (arrayLength > 0) {
            System.arraycopy(data, RELATIONSHIP_OFFSET, relationshipOutgoingArray, 0, arrayLength);
        }

        return relationshipOutgoingArray;
    }

    /**
     *
     * @return a <code>RelationshipIndexRecord</code> backed by the data in this
     * array.
     */
    public RelationshipIndexRecord getRelationshipsRecord() {
        return new RelationshipIndexRecord(data, RELATIONSHIP_OFFSET, data[DESTINATION_OFFSET_INDEX]);
    }

    boolean isKindOf(int parentNid, ViewCoordinate vc, RelativePositionComputerBI computer)
            throws IOException, ContradictionException {
        HashSet<Integer> visitedSet = new HashSet<>();

        return isKindOfWithVisitedSet(parentNid, vc, computer, visitedSet);
    }

    boolean isKindOfWithVisitedSet(int parentNid, ViewCoordinate vc, RelativePositionComputerBI computer,
            HashSet<Integer> visitedSet)
            throws IOException, ContradictionException {
        if (data[DESTINATION_OFFSET_INDEX] > RELATIONSHIP_OFFSET) {
            for (RelationshipIndexRecord record : getRelationshipsRecord()) {
                if (!visitedSet.contains(record.getDestinationNid())) {
                    if (record.isActiveTaxonomyRelationship(vc, computer)) {
                        visitedSet.add(record.getDestinationNid());

                        if (record.getDestinationNid() == parentNid) {
                            return true;
                        } else {
                            IndexCacheRecord possibleParentRecord =
                                    Bdb.getNidCNidMap().getIndexCacheRecord(record.getDestinationNid());

                            if (possibleParentRecord.isKindOfWithVisitedSet(parentNid, vc, computer, visitedSet)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    boolean isChildOf(int parentNid, ViewCoordinate vc, RelativePositionComputerBI computer)
            throws IOException, ContradictionException {
        if (data[DESTINATION_OFFSET_INDEX] > RELATIONSHIP_OFFSET) {
            for (RelationshipIndexRecord record : getRelationshipsRecord()) {
                if (record.isActiveTaxonomyRelationship(vc, computer)) {
                    if (record.getDestinationNid() == parentNid) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Relationships:\n");

        if (data[DESTINATION_OFFSET_INDEX] > RELATIONSHIP_OFFSET) {
            for (RelationshipIndexRecord record : getRelationshipsRecord()) {
                try {
                    sb.append("  ").append(ConceptChronicle.get(record.getTypeNid()).toString()).append(" [").append(
                            record.getDestinationNid()).append("]: ").append(
                            ConceptChronicle.get(record.getDestinationNid()).toString()).append(" [").append(
                            record.getDestinationNid()).append("]\n");


                } catch (IOException ex) {
                    Logger.getLogger(IndexCacheRecord.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        sb.append("\nRelationship origins:\n");

        for (int destinationOrigin : getDestinationOriginNids()) {
            try {
                sb.append("  ").append(ConceptChronicle.get(destinationOrigin).toString()).append(" [").append(
                        destinationOrigin).append("]\n");;


            } catch (IOException ex) {
                Logger.getLogger(IndexCacheRecord.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        sb.append("\nRefsets:\n");

        for (NidPairForRefex pair : getNidPairsForRefsets()) {
            try {
                sb.append("  ").append(ConceptChronicle.get(pair.getRefexNid()).toString()).append(" [").append(
                        pair.getRefexNid()).append("], memberNid: ").append(pair.getMemberNid()).append("\n");


            } catch (IOException ex) {
                Logger.getLogger(IndexCacheRecord.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        return sb.toString();
    }

    NativeIdSetBI isKindOfSet(int parentNid, ViewCoordinate vc, RelativePositionComputerBI computer) {
        NativeIdSetBI resultSet = new ConcurrentBitSet();
        if (data[DESTINATION_OFFSET_INDEX] > RELATIONSHIP_OFFSET) {
            for (RelationshipIndexRecord record : getRelationshipsRecord()) {
                resultSet.add(record.getDestinationNid());
            }
        }
        return resultSet;
    }
}
