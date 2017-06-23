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



package sh.isaac.mapping.data;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.MetaData;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
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
import sh.isaac.api.component.sememe.version.MutableDescriptionSememe;
import sh.isaac.api.component.sememe.version.MutableDynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.model.sememe.dataTypes.DynamicSememeStringImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import sh.isaac.model.sememe.version.DynamicSememeImpl;
import sh.isaac.utility.Frills;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MappingSet}
 *
 * A Convenience class to hide unnecessary OTF bits from the Mapping APIs.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MappingSetDAO
        extends MappingDAO {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- methods -------------------------------------------------------------

   /**
    * Create and store a new mapping set in the DB.
    *
    * @param mappingName - The name of the mapping set (used for the FSN and preferred term of the underlying concept)
    * @param inverseName - (optional) inverse name of the mapping set (if it makes sense for the mapping)
    * @param purpose - (optional) - user specified purpose of the mapping set
    * @param description - the intended use of the mapping set
    * @param editorStatus - (optional) user specified status concept of the mapping set
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @return the mapping set
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static MappingSet createMappingSet(String mappingName,
         String inverseName,
         String purpose,
         String description,
         UUID editorStatus,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws IOException {
      // We need to create a new concept - which itself is defining a dynamic sememe - so set that up here.
      final DynamicSememeUsageDescription rdud = Frills.createNewDynamicSememeUsageDescriptionConcept(mappingName,
                                                                                                      mappingName,
                                                                                                      description,
                                                                                                      new DynamicSememeColumnInfo[] {
                                                                                                         new DynamicSememeColumnInfo(
                                                                                                            0,
                                                                                                                  DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(),
                                                                                                                  DynamicSememeDataType.UUID,
                                                                                                                  null,
                                                                                                                  false,
                                                                                                                  false),
                                                                                                               new DynamicSememeColumnInfo(
                                                                                                                  1,
                                                                                                                        IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_EQUIVALENCE_TYPE.getUUID(),
                                                                                                                        DynamicSememeDataType.UUID,
                                                                                                                        null,
                                                                                                                        false,
                                                                                                                        DynamicSememeValidatorType.IS_KIND_OF,
                                                                                                                        new DynamicSememeUUIDImpl(
                                                                                                                           IsaacMappingConstants.get().MAPPING_EQUIVALENCE_TYPES.getUUID()),
                                                                                                                        false) },

//    new DynamicSememeColumnInfo(2, IsaacMappingConstants.get().MAPPING_STATUS.getUUID(), DynamicSememeDataType.UUID, null, false, 
//                    DynamicSememeValidatorType.IS_KIND_OF, new DynamicSememeUUIDImpl(IsaacMappingConstants.get().MAPPING_STATUS.getUUID()), false)}, 
      null,
                                                                                                      ObjectChronologyType.CONCEPT,
                                                                                                      null,
                                                                                                      editCoord);

      // TODO figure out if I need to be doing this for the indexer
//    Get.workExecutors().getExecutor().execute(() ->
//    {
//            try
//            {
//                    SememeIndexerConfiguration.configureColumnsToIndex(rdud.getDynamicSememeUsageDescriptorSequence(), new Integer[] {0, 1}, true);
//            }
//            catch (Exception e)
//            {
//                    LOG.error("Unexpected error enabling the index on newly created mapping set!", e);
//            }
//    });
      // Then, annotate the concept created above as a member of the MappingSet dynamic sememe, and add the inverse name, if present.
      if (!StringUtils.isBlank(inverseName)) {
         final ObjectChronology<?> builtDesc = LookupService.get()
                                                            .getService(DescriptionBuilderService.class)
                                                            .getDescriptionBuilder(inverseName,
                                                                  rdud.getDynamicSememeUsageDescriptorSequence(),
                                                                  MetaData.SYNONYM_ǁISAACǁ,
                                                                  MetaData.ENGLISH_LANGUAGE_ǁISAACǁ)
                                                            .build(editCoord, ChangeCheckerMode.ACTIVE)
                                                            .getNoThrow();

         Get.sememeBuilderService()
            .getDynamicSememeBuilder(builtDesc.getNid(),
                                     DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME
                                           .getSequence())
            .build(editCoord, ChangeCheckerMode.ACTIVE);
      }

      @SuppressWarnings("rawtypes")
      final SememeChronology mappingAnnotation = Get.sememeBuilderService()
                                                    .getDynamicSememeBuilder(Get.identifierService()
                                                          .getConceptNid(
                                                             rdud.getDynamicSememeUsageDescriptorSequence()),
                                                          IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE
                                                                .getSequence(),
                                                          new DynamicSememeData[] {
//       (editorStatus == null ? null : new DynamicSememeUUIDImpl(editorStatus)),
         (StringUtils.isBlank(purpose) ? null
                                       : new DynamicSememeStringImpl(purpose))
      })
                                                    .build(editCoord, ChangeCheckerMode.ACTIVE)
                                                    .getNoThrow();

      try {
         Get.commitService()
            .commit("update mapping item")
            .get();
      } catch (final InterruptedException | ExecutionException e) {
         throw new RuntimeException();
      }

      @SuppressWarnings("unchecked")
      final Optional<LatestVersion<DynamicSememe<?>>> sememe = mappingAnnotation.getLatestVersion(DynamicSememe.class,
                                                                                                  stampCoord);

      // Find the constructed dynamic refset
      return new MappingSet(sememe.get().value(), stampCoord);
   }

   /**
    * Retire mapping set.
    *
    * @param mappingSetPrimordialUUID the mapping set primordial UUID
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void retireMappingSet(UUID mappingSetPrimordialUUID,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws IOException {
      setConceptStatus(mappingSetPrimordialUUID, State.INACTIVE, stampCoord, editCoord);
   }

   /**
    * Un retire mapping set.
    *
    * @param mappingSetPrimordialUUID the mapping set primordial UUID
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void unRetireMappingSet(UUID mappingSetPrimordialUUID,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws IOException {
      setConceptStatus(mappingSetPrimordialUUID, State.ACTIVE, stampCoord, editCoord);
   }

   /**
    * Store the changes (done via set methods) on the passed in mapping set.
    *
    * @param mappingSet - The mappingSet that carries the changes
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws RuntimeException the runtime exception
    */
   @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
   public static void updateMappingSet(MappingSet mappingSet,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws RuntimeException {
      final ConceptChronology mappingConcept = Get.conceptService()
                                                  .getConcept(mappingSet.getPrimordialUUID());

      Get.sememeService().getSememesForComponent(mappingConcept.getNid()).filter(s -> s.getSememeType() == SememeType.DESCRIPTION).forEach(descriptionC -> {
                     final Optional<LatestVersion<DescriptionSememe<?>>> latest =
                        ((SememeChronology) descriptionC).getLatestVersion(DescriptionSememe.class, stampCoord);

                     if (latest.isPresent()) {
                        final DescriptionSememe<?> ds = latest.get()
                                                              .value();

                        if (ds.getDescriptionTypeConceptSequence() == MetaData.SYNONYM_ǁISAACǁ.getConceptSequence()) {
                           if (Frills.isDescriptionPreferred(ds.getNid(), null)) {
                              if (!ds.getText()
                                     .equals(mappingSet.getName())) {
                                 final MutableDescriptionSememe mutable =
                                    ((SememeChronology<DescriptionSememe>) ds.getChronology()).createMutableVersion(
                                        MutableDescriptionSememe.class,
                                        ds.getStampSequence());

                                 mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
                                 mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
                                 mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
                                 mutable.setText(mappingSet.getName());
                                 Get.commitService()
                                    .addUncommitted(ds.getChronology());
                              }
                           } else

                           // see if it is the inverse name
                           {
                              if (Get.sememeService()
                                     .getSememesForComponentFromAssemblage(ds.getNid(),
                                           DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME
                                                 .getSequence())
                                     .anyMatch(sememeC -> {
                                                  return sememeC.isLatestVersionActive(stampCoord);
                                               })) {
                                 if (!ds.getText()
                                        .equals(mappingSet.getInverseName())) {
                                    final MutableDescriptionSememe mutable =
                                       ((SememeChronology<DescriptionSememe>) ds.getChronology()).createMutableVersion(
                                           MutableDescriptionSememe.class,
                                           ds.getStampSequence());

                                    mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
                                    mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
                                    mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
                                    mutable.setText(mappingSet.getInverseName());
                                    Get.commitService()
                                       .addUncommitted(ds.getChronology());
                                 }
                              }
                           }
                        } else if (ds.getDescriptionTypeConceptSequence() ==
                                   MetaData.DEFINITION_DESCRIPTION_TYPE_ǁISAACǁ.getConceptSequence()) {
                           if (Frills.isDescriptionPreferred(ds.getNid(), null)) {
                              if (!mappingSet.getDescription()
                                             .equals(ds.getText())) {
                                 final MutableDescriptionSememe mutable =
                                    ((SememeChronology<DescriptionSememe>) ds.getChronology()).createMutableVersion(
                                        MutableDescriptionSememe.class,
                                        ds.getStampSequence());

                                 mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
                                 mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
                                 mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
                                 mutable.setText(mappingSet.getDescription());
                                 Get.commitService()
                                    .addUncommitted(ds.getChronology());
                              }
                           }
                        }
                     }
                  });

      final Optional<SememeChronology<? extends SememeVersion<?>>> mappingSememe = Get.sememeService()
                                                                                      .getSememesForComponentFromAssemblage(
                                                                                         mappingConcept.getNid(),
                                                                                               IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE
                                                                                                     .getSequence())
                                                                                      .findAny();

      if (!mappingSememe.isPresent()) {
         LOG.error("Couldn't find mapping refex?");
         throw new RuntimeException("internal error");
      }

      final Optional<LatestVersion<DynamicSememe<?>>> latestVersion =
         ((SememeChronology) mappingSememe.get()).getLatestVersion(DynamicSememe.class,
                                                                   stampCoord.makeAnalog(State.ACTIVE,
                                                                         State.INACTIVE));
      final DynamicSememe<?> latest = latestVersion.get()
                                                   .value();

      if (((latest.getData()[0] == null) && (mappingSet.getPurpose() != null)) ||
            ((mappingSet.getPurpose() == null) && (latest.getData()[0] != null)) ||
            ((latest.getData()[0] != null) && (latest.getData()[0] instanceof DynamicSememeUUID) &&
             ((DynamicSememeUUID) latest.getData()[0]).getDataUUID().equals(mappingSet.getEditorStatusConcept())) ||
            ((latest.getData()[1] == null) && (mappingSet.getPurpose() != null)) ||
            ((mappingSet.getPurpose() == null) && (latest.getData()[1] != null)) ||
            ((latest.getData()[1] != null) && (latest.getData()[1] instanceof DynamicSememeString) &&
             ((DynamicSememeString) latest.getData()[1]).getDataString().equals(mappingSet.getPurpose()))) {
         final DynamicSememeImpl mutable =
            (DynamicSememeImpl) ((SememeChronology) mappingSememe.get()).createMutableVersion(
                MutableDynamicSememe.class,
                latest.getStampSequence());

         mutable.setData(new DynamicSememeData[] { ((mappingSet.getEditorStatusConcept() == null) ? null
               : new DynamicSememeUUIDImpl(mappingSet.getEditorStatusConcept())), (StringUtils.isBlank(
                   mappingSet.getPurpose()) ? null
                                            : new DynamicSememeStringImpl(mappingSet.getPurpose())) });
         Get.commitService()
            .addUncommitted(latest.getChronology());
      }

      Get.commitService()
         .commit("Update mapping");
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the mapping concept.
    *
    * @param sememe the sememe
    * @param stampCoord the stamp coord
    * @return the mapping concept
    * @throws RuntimeException the runtime exception
    */
   public static Optional<ConceptVersion<?>> getMappingConcept(DynamicSememe<?> sememe,
         StampCoordinate stampCoord)
            throws RuntimeException {
      final ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService()
                                                                   .getConcept(sememe.getReferencedComponentNid());
      @SuppressWarnings({ "rawtypes", "unchecked" })
      final Optional<LatestVersion<ConceptVersion<?>>> cv =
         ((ConceptChronology) cc).getLatestVersion(ConceptVersion.class,
                                                   stampCoord.makeAnalog(State.ACTIVE,
                                                         State.INACTIVE));

      if (cv.isPresent()) {
         if (cv.get()
               .contradictions()
               .isPresent()) {
            // TODO handle these properly
            LOG.warn("Concept has contradictions!");
         }

         return Optional.of(cv.get()
                              .value());
      }

      return Optional.empty();
   }

   /**
    * Gets the mapping sets.
    *
    * @param stampCoord the stamp coord
    * @return the mapping sets
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static List<MappingSet> getMappingSets(StampCoordinate stampCoord)
            throws IOException {
      final ArrayList<MappingSet> result = new ArrayList<>();

      Get.sememeService()
         .getSememesFromAssemblage(IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE
               .getSequence())
         .forEach(sememeC -> {
                     @SuppressWarnings({ "unchecked", "rawtypes" })
                     final Optional<LatestVersion<DynamicSememe<?>>> latest =
                        ((SememeChronology) sememeC).getLatestVersion(DynamicSememe.class, stampCoord);

                     if (latest.isPresent()) {
                        // TODO handle contradictions properly
                        result.add(new MappingSet(latest.get().value(), stampCoord));

                        if (latest.get()
                                  .contradictions()
                                  .isPresent()) {
                           latest.get()
                                 .contradictions()
                                 .get()
                                 .forEach((contradiction) -> result.add(new MappingSet(contradiction, stampCoord)));
                        }
                     }
                  });
      return result;
   }
}

