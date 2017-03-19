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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.version.ComponentNidSememe;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.coordinate.CoordinateFactory;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.configuration.TaxonomyCoordinates;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;

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
    * Case significance to concept sequence.
    *
    * @param initialCaseSignificant the initial case significant
    * @return the int
    */
   @Override
   public int caseSignificanceToConceptSequence(boolean initialCaseSignificant) {
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
   public TaxonomyCoordinate createDefaultInferredTaxonomyCoordinate() {
      return createInferredTaxonomyCoordinate(createDevelopmentLatestActiveOnlyStampCoordinate(),
            getUsEnglishLanguageFullySpecifiedNameCoordinate(),
            createStandardElProfileLogicCoordinate());
   }

   /**
    * Creates the default stated taxonomy coordinate.
    *
    * @return the taxonomy coordinate
    */
   @Override
   public TaxonomyCoordinate createDefaultStatedTaxonomyCoordinate() {
      return createStatedTaxonomyCoordinate(createDevelopmentLatestActiveOnlyStampCoordinate(),
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
    * Creates the default user veterans administration extension edit coordinate.
    *
    * @return the edits the coordinate
    */
   @Override
   public EditCoordinate createDefaultUserVeteransAdministrationExtensionEditCoordinate() {
      return EditCoordinates.getDefaultUserVeteransAdministrationExtension();
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
   public TaxonomyCoordinate createInferredTaxonomyCoordinate(StampCoordinate stampCoordinate,
         LanguageCoordinate languageCoordinate,
         LogicCoordinate logicCoordinate) {
      return TaxonomyCoordinates.getInferredTaxonomyCoordinate(stampCoordinate, languageCoordinate, logicCoordinate);
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
    * @param allowedStateSet the allowed state set
    * @param dateTimeText the date time text
    * @return the stamp coordinate
    */
   @Override
   public StampCoordinate createStampCoordinate(ConceptSpecification stampPath,
         StampPrecedence precedence,
         List<ConceptSpecification> moduleSpecificationList,
         EnumSet<State> allowedStateSet,
         CharSequence dateTimeText) {
      final StampPositionImpl stampPosition =
         new StampPositionImpl(LocalDateTime.parse(dateTimeText).toEpochSecond(ZoneOffset.UTC),
                               stampPath.getConceptSequence());

      return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, allowedStateSet);
   }

   /**
    * Creates the stamp coordinate.
    *
    * @param stampPath the stamp path
    * @param precedence the precedence
    * @param moduleSpecificationList the module specification list
    * @param allowedStateSet the allowed state set
    * @param temporal the temporal
    * @return the stamp coordinate
    */
   @Override
   public StampCoordinate createStampCoordinate(ConceptSpecification stampPath,
         StampPrecedence precedence,
         List<ConceptSpecification> moduleSpecificationList,
         EnumSet<State> allowedStateSet,
         TemporalAccessor temporal) {
      final StampPositionImpl stampPosition =
         new StampPositionImpl(LocalDateTime.from(temporal).toEpochSecond(ZoneOffset.UTC),
                               stampPath.getConceptSequence());

      return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, allowedStateSet);
   }

   /**
    * Creates the stamp coordinate.
    *
    * @param stampPath the stamp path
    * @param precedence the precedence
    * @param moduleSpecificationList the module specification list
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
         List<ConceptSpecification> moduleSpecificationList,
         EnumSet<State> allowedStateSet,
         int year,
         int month,
         int dayOfMonth,
         int hour,
         int minute,
         int second) {
      final StampPositionImpl stampPosition = new StampPositionImpl(LocalDateTime.of(year,
                                                                                     month,
                                                                                     dayOfMonth,
                                                                                     hour,
                                                                                     minute,
                                                                                     second).toEpochSecond(
                                                                                        ZoneOffset.UTC),
                                                                    stampPath.getConceptSequence());

      return new StampCoordinateImpl(precedence, stampPosition, moduleSpecificationList, allowedStateSet);
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
   public TaxonomyCoordinate createStatedTaxonomyCoordinate(StampCoordinate stampCoordinate,
         LanguageCoordinate languageCoordinate,
         LogicCoordinate logicCoordinate) {
      return TaxonomyCoordinates.getStatedTaxonomyCoordinate(stampCoordinate, languageCoordinate, logicCoordinate);
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

   /**
    * Iso 639 to concept sequence.
    *
    * @param iso639text the iso 639 text
    * @return the int
    */
   @Override
   public int iso639toConceptSequence(String iso639text) {
      return LanguageCoordinates.iso639toConceptSequence(iso639text);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the acceptable concept sequence.
    *
    * @return the acceptable concept sequence
    */
   @Override
   public int getAcceptableConceptSequence() {
      return TermAux.ACCEPTABLE.getConceptSequence();
   }

   /**
    * Gets the fully specified concept sequence.
    *
    * @return the fully specified concept sequence
    */
   @Override
   public int getFullySpecifiedConceptSequence() {
      return TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence();
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
    * Gets the preferred concept sequence.
    *
    * @return the preferred concept sequence
    */
   @Override
   public int getPreferredConceptSequence() {
      return TermAux.PREFERRED.getConceptSequence();
   }

   /**
    * Gets the specified description.
    *
    * @param stampCoordinate the stamp coordinate
    * @param descriptionList the description list
    * @param languageCoordinate the language coordinate
    * @return the specified description
    */
   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getSpecifiedDescription(StampCoordinate stampCoordinate,
         List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList,
         LanguageCoordinate languageCoordinate) {
      for (final int descType: languageCoordinate.getDescriptionTypePreferenceList()) {
         final Optional<LatestVersion<DescriptionSememe<?>>> match = getSpecifiedDescription(stampCoordinate,
                                                                                             descriptionList,
                                                                                             descType,
                                                                                             languageCoordinate);

         if (match.isPresent()) {
            return match;
         }
      }

      return Optional.empty();
   }

   /**
    * Gets the specified description.
    *
    * @param stampCoordinate the stamp coordinate
    * @param descriptionList the description list
    * @param typeSequence the type sequence
    * @param languageCoordinate the language coordinate
    * @return the specified description
    */
   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getSpecifiedDescription(StampCoordinate stampCoordinate,
         List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList,
         int typeSequence,
         LanguageCoordinate languageCoordinate) {
      final SememeSnapshotService<ComponentNidSememe> acceptabilitySnapshot = Get.sememeService()
                                                                                 .getSnapshot(ComponentNidSememe.class,
                                                                                       stampCoordinate);
      final List<DescriptionSememe<?>> descriptionsForLanguageOfType = new ArrayList<>();

      descriptionList.stream().forEach((descriptionChronicle) -> {
                                 @SuppressWarnings("unchecked")
                                 final Optional<LatestVersion<DescriptionSememe<?>>> latestDescription =
                                    ((SememeChronology) descriptionChronicle).getLatestVersion(DescriptionSememe.class,
                                                                                               stampCoordinate);

                                 if (latestDescription.isPresent()) {
                                    final LatestVersion<DescriptionSememe<?>> latestDescriptionVersion =
                                       latestDescription.get();

                                    latestDescriptionVersion.versionStream().forEach((descriptionVersion) -> {
                     if (descriptionVersion.getLanguageConceptSequence() ==
                         languageCoordinate.getLanguageConceptSequence()) {
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
      final LatestVersion<DescriptionSememe<?>> preferredForDialect = new LatestVersion(DescriptionSememe.class);

      IntStream.of(languageCoordinate.getDialectAssemblagePreferenceList()).forEach((dialectAssemblageSequence) -> {
                           if (preferredForDialect.value() == null) {
                              descriptionsForLanguageOfType.forEach((DescriptionSememe description) -> {
                     final Stream<LatestVersion<ComponentNidSememe>> acceptability =
                        acceptabilitySnapshot.getLatestSememeVersionsForComponentFromAssemblage(description.getNid(),
                                                                                                dialectAssemblageSequence);

                     if (acceptability.anyMatch((LatestVersion<ComponentNidSememe> acceptabilityVersion) -> {
                                                   return Get.identifierService()
                                                         .getConceptSequence(acceptabilityVersion.value()
                                                               .getComponentNid()) == getPreferredConceptSequence();
                                                })) {
                        preferredForDialect.addLatest(description);
                     }
                  });
                           }
                        });

      if (preferredForDialect.value() == null) {
         descriptionsForLanguageOfType.forEach((fsn) -> {
                  preferredForDialect.addLatest(fsn);
               });
      }

      if (preferredForDialect.value() == null) {
         return Optional.empty();
      }

      return Optional.of(preferredForDialect);
   }

   /**
    * Gets the synonym concept sequence.
    *
    * @return the synonym concept sequence
    */
   @Override
   public int getSynonymConceptSequence() {
      return TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence();
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

