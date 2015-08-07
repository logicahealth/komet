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
package gov.vha.isaac.ochre.model.relationship;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import static gov.vha.isaac.ochre.util.UuidT5Generator.REL_ADAPTOR_NAMESPACE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 *
 * @author kec
 */
public class RelationshipAdaptorChronologyImpl 
    implements SememeChronology<RelationshipVersionAdaptorImpl> {

    private final long primordialUuidMsb;
    /**
     * Primoridal uuid least significant bits for this component
     */
    private final long primordialUuidLsb;

    /**
     * Native identifier of this component
     */
    private final int nid;
    /**
     * Id of the logical expression this adaptor was generated from. 
     */
    private final int referencedComponentNid;

    private final ArrayList<RelationshipVersionAdaptorImpl> versionList = new ArrayList<>();

    public RelationshipAdaptorChronologyImpl(int nid, int referencedComponentNid) {
        this.nid = nid;
        this.referencedComponentNid = referencedComponentNid;
        UUID computedUuid = UuidT5Generator.get(REL_ADAPTOR_NAMESPACE, Integer.toString(nid));
        this.primordialUuidLsb = computedUuid.getLeastSignificantBits();
        this.primordialUuidMsb = computedUuid.getMostSignificantBits();
    }
    
    @Override
    public Optional<LatestVersion<RelationshipVersionAdaptorImpl>> getLatestVersion(Class<RelationshipVersionAdaptorImpl> type, StampCoordinate<?> coordinate) {
        RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);
        return calc.getLatestVersion(this);
    }

    @Override
    public boolean isLatestVersionActive(StampCoordinate<?> coordinate) {
        RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);
        StampSequenceSet latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());
        return !latestStampSequences.isEmpty();
    }

    @Override
    public List<RelationshipVersionAdaptorImpl> getVersionList() {
        return versionList;
    }

    @Override
    public IntStream getVersionStampSequences() {
        IntStream.Builder stampSequences = IntStream.builder();
        versionList.forEach((version) -> {stampSequences.accept(version.stampSequence);});
        return stampSequences.build();
    }

    @Override
    public List<? extends SememeChronology<? extends SememeVersion<?>>> getSememeList() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends SememeChronology<? extends SememeVersion<?>>> getSememeListFromAssemblage(int assemblageSequence) {
        return Collections.emptyList();
    }

    @Override
    public <SV extends SememeVersion> List<? extends SememeChronology<SV>> getSememeListFromAssemblageOfType(int assemblageSequence, Class<SV> type) {
         return Collections.emptyList();
    }

    @Override
    public int getNid() {
        return nid;
    }

    @Override
    public String toUserString() {
        return toString();
    }

    @Override
    public UUID getPrimordialUuid() {
        return new UUID(primordialUuidMsb, primordialUuidLsb);
    }

    @Override
    public List<UUID> getUuidList() {
        return Arrays.asList(new UUID[] { getPrimordialUuid() });
    }

    @Override
    public CommitStates getCommitState() {
        return CommitStates.COMMITTED;
    }

    @Override
    public <M extends RelationshipVersionAdaptorImpl> M createMutableVersion(Class<M> type, State state, EditCoordinate ec) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public <M extends RelationshipVersionAdaptorImpl> M createMutableVersion(Class<M> type, int stampSequence) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public SememeType getSememeType() {
        return SememeType.RELATIONSHIP_ADAPTOR;
    }

    @Override
    public int getSememeSequence() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getAssemblageSequence() {
        throw new UnsupportedOperationException("Not supported."); 
    }

    /**
     * 
     * @return the sememe nid for the logical expression from which
     * this relationship adaptor was derived. 
     * 
     */
    @Override
    public int getReferencedComponentNid() {
        return referencedComponentNid; 
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        versionList.stream().forEach((version) -> {
            sb.append(version);
            sb.append(",\n ");
        });
        sb.delete(sb.length() - 4, sb.length() -1);
        
        sb.append("]");
        Optional<? extends SememeChronology<? extends SememeVersion<?>>> optionalSememe = Get.sememeService().getOptionalSememe(referencedComponentNid);
        if (optionalSememe.isPresent()) {
            return "RelAdaptor{"  + Get.conceptDescriptionText(optionalSememe.get().getAssemblageSequence()) + ": " + sb.toString() + '}';
         }
        return "RelAdaptor{"  + referencedComponentNid + ": " + sb.toString() + '}';
    }
    
}
