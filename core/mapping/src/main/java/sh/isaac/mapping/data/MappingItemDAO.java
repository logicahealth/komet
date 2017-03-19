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

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.model.sememe.dataTypes.DynamicSememeUUIDImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class MappingItemDAO.
 */
public class MappingItemDAO
        extends MappingDAO {
   
   /**
    * Construct (and save to the DB) a new MappingItem.
    *
    * @param sourceConcept - the primary ID of the source concept
    * @param mappingSetID - the primary ID of the mapping type
    * @param targetConcept - the primary ID of the target concept
    * @param qualifierID - (optional) the primary ID of the qualifier concept
    * @param editorStatusID - (optional) the primary ID of the status concept
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @return the mapping item
    * @throws RuntimeException the runtime exception
    */
   public static MappingItem createMappingItem(ConceptSnapshot sourceConcept,
         UUID mappingSetID,
         ConceptSnapshot targetConcept,
         UUID qualifierID,
         UUID editorStatusID,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws RuntimeException {
      final SememeBuilder<? extends SememeChronology<?>> sb = Get.sememeBuilderService()
                                                           .getDynamicSememeBuilder(sourceConcept.getNid(),
                                                                 Get.identifierService()
                                                                       .getConceptSequenceForUuids(mappingSetID),
                                                                 new DynamicSememeData[] {
                                                                    ((targetConcept == null) ? null
            : new DynamicSememeUUIDImpl(targetConcept.getPrimordialUuid())), ((qualifierID == null) ? null
            : new DynamicSememeUUIDImpl(qualifierID)), ((editorStatusID == null) ? null
            : new DynamicSememeUUIDImpl(editorStatusID)) });
      final UUID mappingItemUUID = UuidT5Generator.get(IsaacMappingConstants.get().MAPPING_NAMESPACE
                                                                      .getUUID(),
                                                 sourceConcept.getPrimordialUuid()
                                                       .toString() + "|" + mappingSetID.toString() + "|" +
                                                          ((targetConcept == null) ? ""
            : targetConcept.getPrimordialUuid()
                           .toString()) + "|" + ((qualifierID == null) ? ""
            : qualifierID.toString()));

      if (Get.identifierService()
             .hasUuid(mappingItemUUID)) {
         throw new RuntimeException(
             "A mapping with the specified source, target and qualifier already exists in this set.  Please edit that mapping.");
      }

      sb.setPrimordialUuid(mappingItemUUID);

      @SuppressWarnings("rawtypes")
	final
      SememeChronology             built = sb.build(editCoord, ChangeCheckerMode.ACTIVE)
                                             .getNoThrow();
      @SuppressWarnings("deprecation")
	final
      Task<Optional<CommitRecord>> task  = Get.commitService()
                                              .commit("Added comment");

      try {
         task.get();
      } catch (final Exception e) {
         throw new RuntimeException();
      }

      @SuppressWarnings({ "unchecked" })
	final
      Optional<LatestVersion<DynamicSememe<?>>> latest = built.getLatestVersion(DynamicSememe.class,
                                                                                stampCoord.makeAnalog(State.ACTIVE,
                                                                                      State.INACTIVE));

      return new MappingItem(latest.get().value());
   }

   /**
    * Retire mapping item.
    *
    * @param mappingItemPrimordial - The identifier of the mapping item to be retired
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void retireMappingItem(UUID mappingItemPrimordial,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws IOException {
      setSememeStatus(mappingItemPrimordial, State.INACTIVE, stampCoord, editCoord);
   }

   /**
    * Un retire mapping item.
    *
    * @param mappingItemPrimordial - The identifier of the mapping item to be re-activated
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void unRetireMappingItem(UUID mappingItemPrimordial,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws IOException {
      setSememeStatus(mappingItemPrimordial, State.ACTIVE, stampCoord, editCoord);
   }

   /**
    * Just test / demo code.
    *
    * @param mappingItem the mapping item
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws IOException Signals that an I/O exception has occurred.
    */

   /*
    * public static void generateRandomMappingItems(UUID mappingSetUUID)
    * {
    *       try
    *       {
    *               LuceneDescriptionIndexer ldi = AppContext.getService(LuceneDescriptionIndexer.class);
    *               List<SearchResult> result = ldi.query("acetaminophen", ComponentProperty.DESCRIPTION_TEXT, 100);
    *
    *               for (int i = 0; i < 10; i++)
    *               {
    *                       UUID source;
    *                       UUID target = null;
    *
    *                       int index = (int) (Math.random() * 100);
    *                       source = ExtendedAppContext.getDataStore().getConceptForNid(result.get(index).getNid()).getPrimordialUuid();
    *
    *                       while (target == null || target.equals(source))
    *                       {
    *                               index = (int) (Math.random() * 100);
    *                               target = ExtendedAppContext.getDataStore().getConceptForNid(result.get(index).getNid()).getPrimordialUuid();
    *                       }
    *
    *                       createMappingItem(source, mappingSetUUID, target, UUID.fromString("c1068428-a986-5c12-9583-9b2d3a24fdc6"),
    *                                       UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9"));
    *               }
    *       }
    *       catch (Exception e)
    *       {
    *               LOG.error("oops", e);
    *       }
    * }
    */

   /**
    * Store the values passed in as a new revision of a mappingItem (the old revision remains in the DB)
    * @param mappingItem - The MappingItem with revisions (contains fields where the setters have been called)
    * @throws IOException
    */
   public static void updateMappingItem(MappingItem mappingItem,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws IOException {
      final DynamicSememe<?>    rdv  = readCurrentRefex(mappingItem.getPrimordialUUID(), stampCoord);
      final DynamicSememeData[] data = rdv.getData();

      data[2] = ((mappingItem.getEditorStatusConcept() != null)
                 ? new DynamicSememeUUIDImpl(mappingItem.getEditorStatusConcept())
                 : null);
      Get.sememeBuilderService()
         .getDynamicSememeBuilder(rdv.getReferencedComponentNid(), rdv.getAssemblageSequence(), data)
         .build(editCoord, ChangeCheckerMode.ACTIVE);

      @SuppressWarnings("deprecation")
	final
      Task<Optional<CommitRecord>> task = Get.commitService()
                                             .commit("update mapping item");

      try {
         task.get();
      } catch (final Exception e) {
         throw new RuntimeException();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Read all of the mappings items which are defined as part of the specified mapping set.
    *
    * @param mappingSetID - the mapping set that contains the mapping items
    * @param stampCoord the stamp coord
    * @return the mapping items
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static List<MappingItem> getMappingItems(UUID mappingSetID, StampCoordinate stampCoord)
            throws IOException {
      final ArrayList<MappingItem> result = new ArrayList<>();

      Get.sememeService().getSememesFromAssemblage(Get.identifierService()
                                      .getNidForUuids(mappingSetID)).forEach(sememeC -> {
                     @SuppressWarnings({ "unchecked", "rawtypes" })
					final
                     Optional<LatestVersion<DynamicSememe<?>>> latest =
                        ((SememeChronology) sememeC).getLatestVersion(DynamicSememe.class, stampCoord);

                     if (latest.isPresent()) {
                        // TODO figure out how to handle contradictions!
                        result.add(new MappingItem(latest.get().value()));

                        if (latest.get()
                                  .contradictions()
                                  .isPresent()) {
                           latest.get()
                                 .contradictions()
                                 .get()
                                 .forEach((contradiction) -> result.add(new MappingItem(contradiction)));
                        }
                     }
                  });
      return result;
   }
}

