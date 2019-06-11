/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.provider.coordinate;

//~--- JDK imports ------------------------------------------------------------
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------
import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.CoordinateFactory;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.configuration.ManifoldCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticSnapshotService;

//~--- classes ----------------------------------------------------------------
/**
 * The Class CoordinateFactoryProvider.
 *
 * @author kec
 */
@Service
@Singleton
public class CoordinateFactoryProvider
        implements CoordinateFactory {

    /**
     * Case significance to concept nid.
     *
     * @param initialCaseSignificant the initial case significant
     * @return the int
     */
    @Override
    public int caseSignificanceToConceptNid(boolean initialCaseSignificant) {
        return LanguageCoordinates.caseSignificanceToConceptSequence(initialCaseSignificant);
    }

    /**
     * Concept id to case significance.
     *
     * @param id the id
     * @return true, if successful
     */
    @Override
    public boolean conceptIdToCaseSignificance(int id) {
        return LanguageCoordinates.conceptIdToCaseSignificance(id);
    }

    /**
     * Concept id to iso 639.
     *
     * @param nid the nid
     * @return the string
     */
    @Override
    public String conceptIdToIso639(int nid) {
        return LanguageCoordinates.conceptNidToIso639(nid);
    }

    /**
     * Creates the classifier solor overlay edit coordinate.
     *
     * @return the edits the coordinate
     */
    @Override
    public EditCoordinate createClassifierSolorOverlayEditCoordinate() {
        return EditCoordinates.getClassifierSolorOverlay();
    }

    /**
     * Creates the default inferred taxonomy coordinate.
     *
     * @return the taxonomy coordinate
     */
    @Override
    public ManifoldCoordinate createDefaultInferredManifoldCoordinate() {
        return createInferredManifoldCoordinate(
                createDevelopmentLatestActiveOnlyStampCoordinate(),
                getUsEnglishLanguageFullySpecifiedNameCoordinate(),
                createStandardElProfileLogicCoordinate());
    }

    /**
     * Creates the default stated taxonomy coordinate.
     *
     * @return the taxonomy coordinate
     */
    @Override
    public ManifoldCoordinate createDefaultStatedManifoldCoordinate() {
        return createStatedManifoldCoordinate(
                createDevelopmentLatestActiveOnlyStampCoordinate(),
                getUsEnglishLanguageFullySpecifiedNameCoordinate(),
                createStandardElProfileLogicCoordinate());
    }

    /**
     * Creates the default user metadata edit coordinate.
     *
     * @return the edits the coordinate
     */
    @Override
    public EditCoordinate createDefaultUserMetadataEditCoordinate() {
        return EditCoordinates.getDefaultUserMetadata();
    }

    /**
     * Creates the default user solor overlay edit coordinate.
     *
     * @return the edits the coordinate
     */
    @Override
    public EditCoordinate createDefaultUserSolorOverlayEditCoordinate() {
        return EditCoordinates.getDefaultUserSolorOverlay();
    }

    /**
     * Creates the development latest active only stamp coordinate.
     *
     * @return the stamp coordinate
     */
    @Override
    public StampCoordinate createDevelopmentLatestActiveOnlyStampCoordinate() {
        return StampCoordinates.getDevelopmentLatestActiveOnly();
    }

    /**
     * Creates the development latest stamp coordinate.
     *
     * @return the stamp coordinate
     */
    @Override
    public StampCoordinate createDevelopmentLatestStampCoordinate() {
        return StampCoordinates.getDevelopmentLatest();
    }

    /**
     * Creates the inferred taxonomy coordinate.
     *
     * @param stampCoordinate the stamp coordinate
     * @param languageCoordinate the language coordinate
     * @param logicCoordinate the logic coordinate
     * @return the taxonomy coordinate
     */
    @Override
    public ManifoldCoordinate createInferredManifoldCoordinate(StampCoordinate stampCoordinate,
            LanguageCoordinate languageCoordinate,
            LogicCoordinate logicCoordinate) {
        return ManifoldCoordinates.getInferredManifoldCoordinate(stampCoordinate, languageCoordinate, logicCoordinate);
    }

    /**
     * Creates the master latest active only stamp coordinate.
     *
     * @return the stamp coordinate
     */
    @Override
    public StampCoordinate createMasterLatestActiveOnlyStampCoordinate() {
        return StampCoordinates.getMasterLatestActiveOnly();
    }

    /**
     * Creates the master latest stamp coordinate.
     *
     * @return the stamp coordinate
     */
    @Override
    public StampCoordinate createMasterLatestStampCoordinate() {
        return StampCoordinates.getMasterLatest();
    }

    /**
     * Creates the stamp coordinate.
     *
     * @param stampPath the stamp path
     * @param precedence the precedence
     * @param moduleSpecificationList the module specification list
     * @param modulePriorityList
     * @param allowedStateSet the allowed state set
     * @param dateTimeText the date time text
     * @return the stamp coordinate
     */
    @Override
    public StampCoordinate createStampCoordinate(ConceptSpecification stampPath, StampPrecedence precedence, List<ConceptSpecification> moduleSpecificationList, java.util.List modulePriorityList, EnumSet<Status> allowedStateSet, CharSequence dateTimeText) {
        final StampPositionImpl stampPosition = new StampPositionImpl(
                LocalDateTime.parse(dateTimeText).toEpochSecond(ZoneOffset.UTC),
                stampPath.getNid());

        return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, modulePriorityList, allowedStateSet);
    }

    /**
     * Creates the stamp coordinate.
     *
     * @param stampPath the stamp path
     * @param precedence the precedence
     * @param moduleSpecificationList the module specification list
     * @param modulePriorityList
     * @param allowedStateSet the allowed state set
     * @param temporal the temporal
     * @return the stamp coordinate
     */
    @Override
    public StampCoordinate createStampCoordinate(ConceptSpecification stampPath,
            StampPrecedence precedence,
            Collection<ConceptSpecification> moduleSpecificationList,
            List<ConceptSpecification> modulePriorityList,
            EnumSet<Status> allowedStateSet,
            TemporalAccessor temporal) {
        final StampPositionImpl stampPosition = new StampPositionImpl(
                LocalDateTime.from(temporal).toEpochSecond(ZoneOffset.UTC),
                stampPath.getNid());

        return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, modulePriorityList, allowedStateSet);
    }

    /**
     * Creates the stamp coordinate.
     *
     * @param stampPath the stamp path
     * @param precedence the precedence
     * @param moduleSpecificationList the module specification list
     * @param modulePriorityList
     * @param allowedStateSet the allowed state set
     * @param year the year
     * @param month the month
     * @param dayOfMonth the day of month
     * @param hour the hour
     * @param minute the minute
     * @param second the second
     * @return the stamp coordinate
     */
    @Override
    public StampCoordinate createStampCoordinate(ConceptSpecification stampPath,
            StampPrecedence precedence,
            Collection<ConceptSpecification> moduleSpecificationList,
            List<ConceptSpecification> modulePriorityList,
            EnumSet<Status> allowedStateSet,
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        final StampPositionImpl stampPosition = new StampPositionImpl(
                LocalDateTime.of(
                        year,
                        month,
                        dayOfMonth,
                        hour,
                        minute,
                        second).toEpochSecond(ZoneOffset.UTC),
                stampPath.getNid());

        return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, modulePriorityList, allowedStateSet);
    }

    /**
     * Creates the standard el profile logic coordinate.
     *
     * @return the logic coordinate
     */
    @Override
    public LogicCoordinate createStandardElProfileLogicCoordinate() {
        return LogicCoordinates.getStandardElProfile();
    }

    /**
     * Creates the stated taxonomy coordinate.
     *
     * @param stampCoordinate the stamp coordinate
     * @param languageCoordinate the language coordinate
     * @param logicCoordinate the logic coordinate
     * @return the taxonomy coordinate
     */
    @Override
    public ManifoldCoordinate createStatedManifoldCoordinate(StampCoordinate stampCoordinate,
            LanguageCoordinate languageCoordinate,
            LogicCoordinate logicCoordinate) {
        return ManifoldCoordinates.getStatedManifoldCoordinate(stampCoordinate, languageCoordinate, logicCoordinate);
    }

    /**
     * Iso 639 to concept nid.
     *
     * @param iso639text the iso 639 text
     * @return the int
     */
    @Override
    public int iso639toConceptNid(String iso639text) {
        return LanguageCoordinates.iso639toConceptNid(iso639text);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the acceptable concept nid.
     *
     * @return the acceptable concept nid
     */
    @Override
    public int getAcceptableConceptNid() {
        return TermAux.ACCEPTABLE.getNid();
    }

    /**
     * Gets the fully specified concept nid.
     *
     * @return the fully specified concept nid
     */
    @Override
    public int getFullySpecifiedConceptNid() {
        return TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid();
    }

    /**
     * Gets the gb english language fully specified name coordinate.
     *
     * @return the gb english language fully specified name coordinate
     */
    public static LanguageCoordinate getGbEnglishLanguageFullySpecifiedNameCoordinate() {
        return LanguageCoordinates.getGbEnglishLanguageFullySpecifiedNameCoordinate();
    }

    /**
     * Gets the gb english language preferred term coordinate.
     *
     * @return the gb english language preferred term coordinate
     */
    @Override
    public LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate() {
        return LanguageCoordinates.getGbEnglishLanguagePreferredTermCoordinate();
    }

    /**
     * Gets the preferred concept nid.
     *
     * @return the preferred concept nid
     */
    @Override
    public int getPreferredConceptNid() {
        return TermAux.PREFERRED.getNid();
    }

    /**
     * @see sh.isaac.api.LanguageCoordinateService#getSpecifiedDescription(sh.isaac.api.coordinate.StampCoordinate, java.util.List, sh.isaac.api.coordinate.LanguageCoordinate)
     */
    @Override
    public LatestVersion<DescriptionVersion> getSpecifiedDescription(StampCoordinate stampCoordinate, List<SemanticChronology> descriptionList,
            LanguageCoordinate languageCoordinate) {
        final List<DescriptionVersion> descriptionsForLanguageOfType = new ArrayList<>();

        //Find all descriptions that match the language and description type - moving through the desired description types until 
        //we find at least one.
        for (final int descType : languageCoordinate.getDescriptionTypePreferenceList()) {
            for (SemanticChronology descriptionChronicle : descriptionList) {
                final LatestVersion<DescriptionVersion> latestDescription = ((SemanticChronology) descriptionChronicle).getLatestVersion(stampCoordinate);
    
                if (latestDescription.isPresent()) {
                    for (DescriptionVersion descriptionVersion : latestDescription.versionList()) {
                        if ((descriptionVersion.getLanguageConceptNid() == languageCoordinate.getLanguageConceptNid() ||
                              languageCoordinate.getLanguageConceptNid() == TermAux.LANGUAGE.getNid())
                                && descriptionVersion.getDescriptionTypeConceptNid() == descType) {
                            descriptionsForLanguageOfType.add(descriptionVersion);
                        }
                    }
                }
            }
            if (!descriptionsForLanguageOfType.isEmpty()) {
                //If we found at least one that matches the language and type, go on to rank by dialect
                break;
            }
        }

        if (descriptionsForLanguageOfType.isEmpty()) {
            //Didn't find any that matched any of the allowed description types.  See if there is another priority coordinate to continue with
            Optional<LanguageCoordinate> nextPriorityCoordinate = languageCoordinate.getNextProrityLanguageCoordinate();
            if (nextPriorityCoordinate.isPresent()) {
                return getSpecifiedDescription(stampCoordinate, descriptionList, nextPriorityCoordinate.get());
            }
            else {
                return new LatestVersion<>();
            }
        }

        // handle dialect...
        final LatestVersion<DescriptionVersion> preferredForDialect = new LatestVersion<>(DescriptionVersion.class);
        final SemanticSnapshotService<ComponentNidVersion> acceptabilitySnapshot = Get.assemblageService().getSnapshot(ComponentNidVersion.class,
                stampCoordinate);
        if (languageCoordinate.getDialectAssemblagePreferenceList() != null) {
            for (int dialectAssemblageNid : languageCoordinate.getDialectAssemblagePreferenceList()) {
                if (!preferredForDialect.isPresent()) {
                    for (DescriptionVersion description : descriptionsForLanguageOfType) {
                        for (LatestVersion<ComponentNidVersion> acceptabilityVersion : acceptabilitySnapshot
                                .getLatestSemanticVersionsForComponentFromAssemblage(description.getNid(), dialectAssemblageNid)) {
                            acceptabilityVersion.ifPresent((acceptability) -> {
                                if (acceptability.getComponentNid() == getPreferredConceptNid()) {
                                    preferredForDialect.addLatest(description);
                                }
                            });
                        }
                    }
                }
            }
        }

        //If none matched the dialect rank list, just ignore the dialect, and keep all that matched the type and language.
        if (!preferredForDialect.isPresent()) {
            descriptionsForLanguageOfType.forEach((description) -> {
                preferredForDialect.addLatest(description);
            });
        }

        // add in module preferences if there is more than one. 
        if (languageCoordinate.getModulePreferenceListForLanguage() != null && languageCoordinate.getModulePreferenceListForLanguage().length != 0) {
            for (int preference : languageCoordinate.getModulePreferenceListForLanguage()) {
                for (DescriptionVersion descriptionVersion : preferredForDialect.versionList()) {
                    if (descriptionVersion.getModuleNid() == preference) {
                        LatestVersion<DescriptionVersion> preferredForModule = new LatestVersion<>(descriptionVersion);
                        for (DescriptionVersion alternateVersion : preferredForDialect.versionList()) {
                            if (alternateVersion != preferredForModule.get()) {
                                preferredForModule.addLatest(alternateVersion);
                            }
                        }
                        return preferredForModule;
                    }
                }
            }
        }
        return preferredForDialect;
    }

    /**
     * Gets the synonym concept nid.
     *
     * @return the synonym concept nid
     */
    @Override
    public int getSynonymConceptNid() {
        return TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid();
    }

    /**
     * Gets the us english language fully specified name coordinate.
     *
     * @return the us english language fully specified name coordinate
     */
    @Override
    public LanguageCoordinate getUsEnglishLanguageFullySpecifiedNameCoordinate() {
        return LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
    }

    /**
     * Gets the us english language preferred term coordinate.
     *
     * @return the us english language preferred term coordinate
     */
    @Override
    public LanguageCoordinate getUsEnglishLanguagePreferredTermCoordinate() {
        return LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate();
    }
}
