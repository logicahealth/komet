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

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.ObjectChronologyImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author kec
 */
public class ConceptChronologyImpl
    extends ObjectChronologyImpl<ConceptVersionImpl> 
    implements ConceptChronology<ConceptVersionImpl> {

    public ConceptChronologyImpl(UUID primoridalUuid, int nid, int containerSequence) {
        super(primoridalUuid, nid, containerSequence);
    }

    public ConceptChronologyImpl(DataBuffer data) {
        super(data);
        constructorEnd(data);

    }
    @Override
    public void writeChronicleData(DataBuffer data) {
        super.writeChronicleData(data);
    }

    @Override
    public ConceptVersionImpl createMutableVersion(State state, EditCoordinate ec) {
        int stampSequence = getCommitService().getStampSequence(state, Long.MAX_VALUE, 
                ec.getAuthorSequence(), ec.getModuleSequence(), ec.getPathSequence());
        ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence, nextVersionSequence());
        addVersion(newVersion);
        return newVersion;
    }

    @Override
    public ConceptVersionImpl createMutableVersion(int stampSequence) {
        ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence, nextVersionSequence());
        addVersion(newVersion);
        return newVersion;
    }

    @Override
    protected ConceptVersionImpl makeVersion(int stampSequence, DataBuffer bb) {
        return new ConceptVersionImpl(this, stampSequence, bb.getShort());
    }

    @Override
    public int getConceptSequence() {
        return getContainerSequence();
    }

    @Override
    public List<? extends SememeChronology<? extends DescriptionSememe>> getConceptDescriptionList() {
        return getSememeService().getDescriptionsForComponent(getNid()).collect(Collectors.toList());
    }

    @Override
    public boolean containsDescription(String descriptionText) {
        return getSememeService().getDescriptionsForComponent(getNid())
                .anyMatch((desc)-> desc.getVersionList().stream().
                        anyMatch((version) -> version.getText().equals(descriptionText)));
    }

    @Override
    public boolean containsActiveDescription(String descriptionText, StampCoordinate stampCoordinate) {
        return getSememeService().getSnapshot(DescriptionSememe.class, stampCoordinate)
                .getLatestActiveDescriptionVersionsForComponent(getNid())
                .anyMatch((latestVersion) -> latestVersion.value().getText().equals(descriptionText));
    }

    @Override
    public String toUserString() {
        List<? extends SememeChronology<? extends DescriptionSememe>> descList = getConceptDescriptionList();
        if (descList.isEmpty()) {
            return "no description for concept: " + getUuidList() + " " + getConceptSequence()
                    + " " + getNid();
        }
        return getConceptDescriptionList().get(0).getVersionList().get(0).getText();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConceptChronologyImpl{");
        builder.append(toUserString());
        builder.append(" ");
        toString(builder);
        return builder.toString();
    }

    @Override
    public Optional<LatestVersion<DescriptionSememe>> getFullySpecifiedDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate) {
        return languageCoordinate.getFullySpecifiedDescription((List<SememeChronology<DescriptionSememe>>) getConceptDescriptionList(), stampCoordinate);
    }

    @Override
    public Optional<LatestVersion<DescriptionSememe>> getPreferredDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate) {
        return languageCoordinate.getPreferredDescription((List<SememeChronology<DescriptionSememe>>) getConceptDescriptionList(), stampCoordinate);
    }
    
    
}
