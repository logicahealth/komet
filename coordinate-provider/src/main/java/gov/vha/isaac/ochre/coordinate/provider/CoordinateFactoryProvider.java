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
package gov.vha.isaac.ochre.coordinate.provider;

import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.CoordinateFactory;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service 
@Singleton
public class CoordinateFactoryProvider implements CoordinateFactory {

    @Override
    public StampCoordinate createStampCoordinate(ConceptSpecification stampPath, StampPrecedence precedence, List<ConceptSpecification> moduleSpecificationList, EnumSet<State> allowedStateSet, int year, int month, int dayOfMonth, int hour, int minute, int second) {
        StampPositionImpl stampPosition = new StampPositionImpl(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second).toEpochSecond(ZoneOffset.UTC), stampPath.getConceptSequence());
        return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, allowedStateSet);
    }

    @Override
    public StampCoordinate createStampCoordinate(ConceptSpecification stampPath, StampPrecedence precedence, List<ConceptSpecification> moduleSpecificationList, EnumSet<State> allowedStateSet, TemporalAccessor temporal) {
        StampPositionImpl stampPosition = new StampPositionImpl(LocalDateTime.from(temporal).toEpochSecond(ZoneOffset.UTC), stampPath.getConceptSequence());
        return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, allowedStateSet);
    }

    @Override
    public StampCoordinate createStampCoordinate(ConceptSpecification stampPath, StampPrecedence precedence, List<ConceptSpecification> moduleSpecificationList, EnumSet<State> allowedStateSet, CharSequence dateTimeText) {
        StampPositionImpl stampPosition = new StampPositionImpl(LocalDateTime.parse(dateTimeText).toEpochSecond(ZoneOffset.UTC), stampPath.getConceptSequence());
        return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, allowedStateSet);
    }

    @Override
    public StampCoordinate createDevelopmentLatestStampCoordinate() {
        return StampCoordinates.getDevelopmentLatest();
    }

    @Override
    public StampCoordinate createDevelopmentLatestActiveOnlyStampCoordinate() {
        return StampCoordinates.getDevelopmentLatestActiveOnly();
    }

    @Override
    public StampCoordinate createMasterLatestStampCoordinate() {
        return StampCoordinates.getMasterLatest();
    }

    @Override
    public StampCoordinate createMasterLatestActiveOnlyStampCoordinate() {
        return StampCoordinates.getMasterLatestActiveOnly();
    }

    @Override
    public LogicCoordinate createStandardElProfileLogicCoordinate() {
        return LogicCoordinates.getStandardElProfile();
    }

    @Override
    public TaxonomyCoordinate createInferredTaxonomyCoordinate(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        return TaxonomyCoordinates.getInferredTaxonomyCoordinate(stampCoordinate, languageCoordinate, logicCoordinate);
    }

    @Override
    public TaxonomyCoordinate createStatedTaxonomyCoordinate(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        return TaxonomyCoordinates.getStatedTaxonomyCoordinate(stampCoordinate, languageCoordinate, logicCoordinate);
    }
 
    @Override
    public LanguageCoordinate getUsEnglishLanguagePreferredTermCoordinate() {
        return LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate();
    }
    
    @Override
    public LanguageCoordinate getUsEnglishLanguageFullySpecifiedNameCoordinate() {
        return LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
    }
    
    @Override
    public LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate() {
        return LanguageCoordinates.getGbEnglishLanguagePreferredTermCoordinate();
    }
    
    public static LanguageCoordinate getGbEnglishLanguageFullySpecifiedNameCoordinate() {
        return LanguageCoordinates.getGbEnglishLanguageFullySpecifiedNameCoordinate();
    }
    
    @Override
    public int iso639toConceptNid(String iso639text) {
        return LanguageCoordinates.iso639toConceptNid(iso639text);
    }
    
    @Override
    public int iso639toConceptSequence(String iso639text) {
        return LanguageCoordinates.iso639toConceptSequence(iso639text);
    }
    
    @Override
    public String conceptIdToIso639(int nid) {
        return LanguageCoordinates.conceptNidToIso639(nid);
    }
    
    @Override
    public int caseSignificanceToConceptSequence(boolean initialCaseSignificant) {
        return LanguageCoordinates.caseSignificanceToConceptSequence(initialCaseSignificant);
    }

    @Override
    public boolean conceptIdToCaseSignificance(int id) {
        return LanguageCoordinates.conceptIdToCaseSignificance(id);
    }

    @Override
    public int getFullySpecifiedConceptSequence() {
        return TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence();
    }

    @Override
    public int getSynonymConceptSequence() {
        return TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence();
    }

    @Override
    public int getPreferredConceptSequence() {
        return TermAux.PREFERRED.getConceptSequence();
    }

    @Override
    public int getAcceptableConceptSequence() {
        return TermAux.ACCEPTABLE.getConceptSequence();
    }

    @Override
    public Optional<LatestVersion<DescriptionSememe<?>>> getSpecifiedDescription(StampCoordinate stampCoordinate, 
            List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList, LanguageCoordinate languageCoordinate) {
        for (int descType: languageCoordinate.getDescriptionTypePreferenceList()) {
            Optional<LatestVersion<DescriptionSememe<?>>>  match = getSpecifiedDescription(stampCoordinate, 
            descriptionList,  descType, languageCoordinate);
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<LatestVersion<DescriptionSememe<?>>> getSpecifiedDescription(StampCoordinate stampCoordinate, 
            List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList, 
            int typeSequence, LanguageCoordinate languageCoordinate) {
        SememeSnapshotService<ComponentNidSememe> acceptabilitySnapshot = Get.sememeService().getSnapshot(ComponentNidSememe.class, stampCoordinate);
        
        List<DescriptionSememe<?>> descriptionsForLanguageOfType = new ArrayList<>();
        
        descriptionList.stream().forEach((descriptionChronicle) -> {
            @SuppressWarnings("unchecked")
            Optional<LatestVersion<DescriptionSememe<?>>> latestDescription
                    = ((SememeChronology)descriptionChronicle).getLatestVersion(DescriptionSememe.class, stampCoordinate);
            if (latestDescription.isPresent()) {
                LatestVersion<DescriptionSememe<?>> latestDescriptionVersion = latestDescription.get();
                latestDescriptionVersion.versionStream().forEach((descriptionVersion) -> {
                    if (descriptionVersion.getLanguageConceptSequence() == languageCoordinate.getLanguageConceptSequence()) {
                        if (descriptionVersion.getDescriptionTypeConceptSequence() == typeSequence) {
                            descriptionsForLanguageOfType.add(descriptionVersion);
                        }
                    }
                });

            }
        });

        if (descriptionsForLanguageOfType.isEmpty()) {
            return Optional.empty();
        }

        // handle dialect...
        LatestVersion<DescriptionSememe<?>> preferredForDialect = new LatestVersion(DescriptionSememe.class);
        IntStream.of(languageCoordinate.getDialectAssemblagePreferenceList()).forEach((dialectAssemblageSequence) -> {
            if (preferredForDialect.value() == null) {
                descriptionsForLanguageOfType.forEach((DescriptionSememe description) -> {
                    Stream<LatestVersion<ComponentNidSememe>> acceptability
                            = acceptabilitySnapshot.getLatestSememeVersionsForComponentFromAssemblage(description.getNid(), dialectAssemblageSequence);

                    if (acceptability.anyMatch((LatestVersion<ComponentNidSememe> acceptabilityVersion) -> {
                         return Get.identifierService().getConceptSequence(acceptabilityVersion.value().getComponentNid()) == getPreferredConceptSequence();
                    })) {
                        preferredForDialect.addLatest(description);
                    }
                });
            }
        });

        if (preferredForDialect.value() == null) {
            descriptionsForLanguageOfType.forEach((fsn) -> {preferredForDialect.addLatest(fsn);});
        }
        if (preferredForDialect.value() == null) { 
            return Optional.empty();
        }
        return Optional.of((LatestVersion<DescriptionSememe<?>>) preferredForDialect);
    }

    @Override
    public EditCoordinate createDefaultUserSolorOverlayEditCoordinate() {
        return EditCoordinates.getDefaultUserSolorOverlay();
    }

    @Override
    public EditCoordinate createClassifierSolorOverlayEditCoordinate() {
        return EditCoordinates.getClassifierSolorOverlay();
    }

    @Override
    public EditCoordinate createDefaultUserVeteransAdministrationExtensionEditCoordinate() {
        return EditCoordinates.getDefaultUserVeteransAdministrationExtension();
    }

    @Override
    public EditCoordinate createDefaultUserMetadataEditCoordinate() {
        return EditCoordinates.getDefaultUserMetadata();
    }

    @Override
    public TaxonomyCoordinate createDefaultInferredTaxonomyCoordinate() {
        return createInferredTaxonomyCoordinate(createDevelopmentLatestActiveOnlyStampCoordinate(), 
                getUsEnglishLanguageFullySpecifiedNameCoordinate(), 
                createStandardElProfileLogicCoordinate());
    }

    @Override
    public TaxonomyCoordinate createDefaultStatedTaxonomyCoordinate() {
         return createStatedTaxonomyCoordinate(createDevelopmentLatestActiveOnlyStampCoordinate(), 
                getUsEnglishLanguageFullySpecifiedNameCoordinate(), 
                createStandardElProfileLogicCoordinate());
    }
       
}
