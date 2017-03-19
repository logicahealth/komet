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



package sh.isaac.provider.query.associations;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.provider.query.lucene.indexers.SememeIndexerConfiguration;
import sh.isaac.utility.Frills;

//~--- classes ----------------------------------------------------------------

public class AssociationType {
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private int              associationSequence_;
   private String           associationName_;
   private Optional<String> associationInverseName_;
   private String           description_;

   //~--- constructors --------------------------------------------------------

   private AssociationType(int conceptNidOrSequence) {
      this.associationSequence_ = Get.identifierService()
                                     .getConceptSequence(conceptNidOrSequence);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Create and store a new mapping set in the DB.
    * @param associationName - The name of the association (used for the FSN and preferred term of the underlying concept)
    * @param associationInverseName - (optional) inverse name of the association (if it makes sense for the association)
    * @param description - (optional) description that describes the purpose of the association
    * @param referencedComponentRestriction - (optional) - may be null - if provided - this restricts the type of object referenced by the nid or
    * UUID that is set for the referenced component in an instance of this sememe.  If {@link ObjectChronologyType#UNKNOWN_NID} is passed, it is ignored, as
    * if it were null.
    * @param referencedComponentSubRestriction - (optional) - may be null - subtype restriction for {@link ObjectChronologyType#SEMEME} restrictions
    * @param stampCoord - optional - used during the readback to create the return object.  See {@link #read(int, StampCoordinate)}
    * @param editCoord - optional - the edit coordinate to use when creating the association.  Uses the system default if not provided.
    * @return the concept sequence of the created concept that carries the association definition
    */
   @SuppressWarnings("deprecation")
   public static AssociationType createAssociation(String associationName,
         String associationInverseName,
         String description,
         ObjectChronologyType referencedComponentRestriction,
         SememeType referencedComponentSubRestriction,
         StampCoordinate stampCoord,
         EditCoordinate editCoord) {
      try {
         EditCoordinate localEditCoord = ((editCoord == null) ? Get.configurationService()
                                                                   .getDefaultEditCoordinate()
               : editCoord);

         // We need to create a new concept - which itself is defining a dynamic sememe - so set that up here.
         DynamicSememeUsageDescription rdud = Frills.createNewDynamicSememeUsageDescriptionConcept(associationName,
                                                                                                   associationName,
                                                                                                   StringUtils.isBlank(description)
                                                                                                   ? "Defines the association type " +
                                                                                                     associationInverseName
               : description,
                                                                                                   new DynamicSememeColumnInfo[] {
                                                                                                      new DynamicSememeColumnInfo(
                                                                                                         0,
                                                                                                               DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(),
                                                                                                               DynamicSememeDataType.UUID,
                                                                                                               null,
                                                                                                               false,
                                                                                                               true) },
                                                                                                   DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME
                                                                                                         .getNid(),
                                                                                                   referencedComponentRestriction,
                                                                                                   referencedComponentSubRestriction,
                                                                                                   editCoord);

         Get.workExecutors().getExecutor().execute(() -> {
                        try {
                           SememeIndexerConfiguration.configureColumnsToIndex(
                               rdud.getDynamicSememeUsageDescriptorSequence(),
                               new Integer[] { 0 },
                               true);
                        } catch (Exception e) {
                           log.error("Unexpected error enabling the index on newly created association!", e);
                        }
                     });

         // Then add the inverse name, if present.
         if (!StringUtils.isBlank(associationInverseName)) {
            ObjectChronology<?> builtDesc = LookupService.get()
                                                         .getService(DescriptionBuilderService.class)
                                                         .getDescriptionBuilder(associationInverseName,
                                                               rdud.getDynamicSememeUsageDescriptorSequence(),
                                                               MetaData.SYNONYM,
                                                               MetaData.ENGLISH_LANGUAGE)
                                                         .build(localEditCoord, ChangeCheckerMode.ACTIVE)
                                                         .getNoThrow();

            Get.sememeBuilderService()
               .getDynamicSememeBuilder(builtDesc.getNid(),
                                        DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME
                                              .getSequence())
               .build(localEditCoord, ChangeCheckerMode.ACTIVE)
               .getNoThrow();
            Get.commitService()
               .commit("add description to association")
               .get();
         }

         // Add the association marker sememe
         Get.sememeBuilderService()
            .getDynamicSememeBuilder(Get.identifierService()
                                        .getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
                                     DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME
                                           .getSequence())
            .build(localEditCoord, ChangeCheckerMode.ACTIVE)
            .getNoThrow();
         Get.commitService()
            .commit("mark assocation as association type sememe")
            .get();

         // final get is to wait for commit completion
         return read(rdud.getDynamicSememeUsageDescriptorSequence(),
                     stampCoord,
                     LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate());
      } catch (Exception e) {
         log.error("Unexpected error creating association", e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Read all details that define an Association.
    * @param conceptNidOrSequence The concept that represents the association
    * @param stamp optional - uses system default if not provided.
    * @param language optional - uses system default if not provided
    * @return
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public static AssociationType read(int conceptNidOrSequence, StampCoordinate stamp, LanguageCoordinate language) {
      AssociationType    at            = new AssociationType(conceptNidOrSequence);
      int                conceptNid    = Get.identifierService()
                                            .getConceptNid(at.getAssociationTypeSequenece());
      StampCoordinate    localStamp    = ((stamp == null) ? Get.configurationService()
                                                               .getDefaultStampCoordinate()
            : stamp);
      LanguageCoordinate localLanguage = ((language == null) ? Get.configurationService()
                                                                  .getDefaultLanguageCoordinate()
            : language);

      at.associationName_ = Get.conceptService()
                               .getSnapshot(localStamp, localLanguage)
                               .conceptDescriptionText(conceptNid);

      // Find the inverse name
      for (DescriptionSememe<?> desc: Frills.getDescriptionsOfType(conceptNid,
            MetaData.SYNONYM,
            localStamp.makeAnalog(State.ACTIVE))) {
         if (Get.sememeService()
                .getSememesForComponentFromAssemblage(desc.getNid(),
                      DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME
                                            .getSequence())
                .anyMatch(nestedSememe -> {
                             if (nestedSememe.getSememeType() == SememeType.DYNAMIC) {
                                return ((SememeChronology) nestedSememe).getLatestVersion(DynamicSememe.class,
                                      localStamp)
                                      .isPresent();
                             }

                             return false;
                          })) {
            at.associationInverseName_ = Optional.of(desc.getText());
         }
      }

      // find the description
      for (DescriptionSememe<?> desc: Frills.getDescriptionsOfType(Get.identifierService()
            .getConceptNid(at.getAssociationTypeSequenece()),
            MetaData.DEFINITION_DESCRIPTION_TYPE,
            localStamp.makeAnalog(State.ACTIVE))) {
         if (Frills.isDescriptionPreferred(desc.getNid(), localStamp) &&Get.sememeService().getSememesForComponentFromAssemblage(desc.getNid(),
               DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION.getSequence()).anyMatch(
                   nestedSememe -> {
                      if (nestedSememe.getSememeType() == SememeType.DYNAMIC) {
                         return ((SememeChronology) nestedSememe).getLatestVersion(DynamicSememe.class, localStamp)
                               .isPresent();
                      }

                      return false;
                   })) {
            at.description_ = desc.getText();
         }
      }

      if (at.associationInverseName_ == null) {
         at.associationInverseName_ = Optional.empty();
      }

      if (at.description_ == null) {
         at.description_ = "-No description on path!-";
      }

      return at;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the inverse name of the association (if present) (Read from the association type concept)
    */
   public Optional<String> getAssociationInverseName() {
      return associationInverseName_;
   }

   public String getAssociationName() {
      return associationName_;
   }

   /**
    * @return the association type concept
    */
   public ConceptChronology<? extends ConceptVersion<?>> getAssociationTypeConcept() {
      return Get.conceptService()
                .getConcept(associationSequence_);
   }

   /**
    * @return the concept sequence of the association type concept
    */
   public int getAssociationTypeSequenece() {
      return associationSequence_;
   }

   public String getDescription() {
      return description_;
   }
}

