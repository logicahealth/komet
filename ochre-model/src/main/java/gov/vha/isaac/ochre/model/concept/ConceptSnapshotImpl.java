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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.concept.ConceptSnapshot;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author kec
 */
public class ConceptSnapshotImpl implements ConceptSnapshot {

    private final ConceptChronologyImpl conceptChronology;
    private final StampCoordinate stampCoordinate;
    private final LanguageCoordinate languageCoordinate;
    private final LatestVersion<ConceptVersionImpl> snapshotVersion;

    public ConceptSnapshotImpl(ConceptChronologyImpl conceptChronology, StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
        this.conceptChronology = conceptChronology;
        this.stampCoordinate = stampCoordinate;
        this.languageCoordinate = languageCoordinate;
        Optional<LatestVersion<ConceptVersionImpl>> optionalVersion = 
                RelativePositionCalculator.getCalculator(stampCoordinate).getLatestVersion(conceptChronology);
        snapshotVersion = optionalVersion.get();
    }

    @Override
    public String getConceptDescriptionText() {
        return getDescription().getText();
    }
    
    @Override
    public ConceptChronologyImpl getChronology() {
        return conceptChronology;
    }

    @Override
    public StampCoordinate getStampCoordinate() {
        return stampCoordinate;
    }

    @Override
    public int getConceptSequence() {
        return conceptChronology.getConceptSequence();
    }

    @Override
    public boolean containsActiveDescription(String descriptionText) {
        return conceptChronology.containsDescription(descriptionText, stampCoordinate);
    }

    @Override
    public int getStampSequence() {
        return snapshotVersion.value().getStampSequence();
    }

    @Override
    public State getState() {
        return snapshotVersion.value().getState();
    }

    @Override
    public long getTime() {
        return snapshotVersion.value().getTime();
    }

    @Override
    public int getAuthorSequence() {
        return snapshotVersion.value().getAuthorSequence();
    }

    @Override
    public int getModuleSequence() {
        return snapshotVersion.value().getModuleSequence();
    }

    @Override
    public int getPathSequence() {
        return snapshotVersion.value().getPathSequence();
    }

    @Override
    public CommitStates getCommitState() {
        return snapshotVersion.value().getCommitState();
    }

    @Override
    public int getNid() {
        return snapshotVersion.value().getNid();
    }

    @Override
    public String toUserString() {
        return snapshotVersion.toString();
    }

    @Override
    public UUID getPrimordialUuid() {
       return snapshotVersion.value().getPrimordialUuid();
    }

    @Override
    public List<UUID> getUuidList() {
        return snapshotVersion.value().getUuidList();
    }

    @Override
    public Optional<? extends Set<? extends StampedVersion>> getContradictions() {
        return snapshotVersion.contradictions();
    }

    @Override
    public LanguageCoordinate getLanguageCoordinate() {
        return languageCoordinate;
    }

    public Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription() {
        return languageCoordinate.getFullySpecifiedDescription(Get.sememeService().getDescriptionsForComponent(getNid()).collect(Collectors.toList()), stampCoordinate);
    }

    public Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription() {
    	return languageCoordinate.getPreferredDescription(
    			Get.sememeService().getDescriptionsForComponent(getNid()).collect(Collectors.toList()), stampCoordinate);
    }

    public DescriptionSememe<?> getDescription() {
        Optional<LatestVersion<DescriptionSememe<?>>> fsd = getFullySpecifiedDescription();
        if (fsd.isPresent()) {
            return fsd.get().value();
        }
        Optional<LatestVersion<DescriptionSememe<?>>> pd = getPreferredDescription();
        if (pd.isPresent()) {
            return pd.get().value();
        }
        
        return Get.sememeService().getDescriptionsForComponent(getNid()).findAny().get().getVersionList().get(0);
    }
    
}
