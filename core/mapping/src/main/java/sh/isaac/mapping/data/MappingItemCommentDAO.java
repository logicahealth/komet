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

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.sememe.dataTypes.DynamicSememeStringImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class MappingItemCommentDAO.
 */
public class MappingItemCommentDAO
        extends MappingDAO {
   
   /**
    * Create (and store to the DB) a new comment.
    *
    * @param pMappingItemUUID - The item the comment is being added to
    * @param pCommentText - The text of the comment
    * @param commentContext - (optional) field for storing other arbitrary info about the comment.  An editor may wish to put certain keywords on
    * some comments - this field is indexed, so a search for comments could query this field.
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @return the mapping item comment
    * @throws RuntimeException the runtime exception
    */
   public static MappingItemComment createMappingItemComment(UUID pMappingItemUUID,
         String pCommentText,
         String commentContext,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws RuntimeException {
      if (pMappingItemUUID == null) {
         throw new RuntimeException("UUID of component to attach the comment to is required");
      }

      if (StringUtils.isBlank(pCommentText)) {
         throw new RuntimeException("The comment is required");
      }

      final SememeChronology<? extends DynamicSememe<?>> built = Get.sememeBuilderService()
                                                              .getDynamicSememeBuilder(Get.identifierService()
                                                                    .getNidForUuids(pMappingItemUUID),
                                                                    DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE
                                                                          .getSequence(),
                                                                    new DynamicSememeData[] {
                                                                       new DynamicSememeStringImpl(pCommentText),
                                                                       (StringUtils.isBlank(commentContext) ? null
            : new DynamicSememeStringImpl(commentContext)) })
                                                              .build(editCoord, ChangeCheckerMode.ACTIVE)
                                                              .getNoThrow();
      @SuppressWarnings("deprecation")
	final
      Task<Optional<CommitRecord>> task = Get.commitService()
                                             .commit("Added comment");

      try {
         task.get();
      } catch (final Exception e) {
         throw new RuntimeException();
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
	final
      Optional<LatestVersion<DynamicSememe<?>>> latest =
         ((SememeChronology) built).getLatestVersion(DynamicSememe.class,
                                                     stampCoord.makeAnalog(State.ACTIVE,
                                                           State.INACTIVE));

      return new MappingItemComment(latest.get().value());
   }

   /**
    * Retire comment.
    *
    * @param commentPrimordialUUID - The ID of the comment to be retired
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void retireComment(UUID commentPrimordialUUID,
                                    StampCoordinate stampCoord,
                                    EditCoordinate editCoord)
            throws IOException {
      setSememeStatus(commentPrimordialUUID, State.INACTIVE, stampCoord, editCoord);
   }

   /**
    * Un retire comment.
    *
    * @param commentPrimordialUUID - The ID of the comment to be re-activated
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void unRetireComment(UUID commentPrimordialUUID,
                                      StampCoordinate stampCoord,
                                      EditCoordinate editCoord)
            throws IOException {
      setSememeStatus(commentPrimordialUUID, State.ACTIVE, stampCoord, editCoord);
   }

   /**
    * Store the values passed in as a new revision of a comment (the old revision remains in the DB).
    *
    * @param comment - The MappingItemComment with revisions (contains fields where the setters have been called)
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void updateComment(MappingItemComment comment,
                                    StampCoordinate stampCoord,
                                    EditCoordinate editCoord)
            throws IOException {
      final DynamicSememe<?> rdv = readCurrentRefex(comment.getPrimordialUUID(), stampCoord);

      Get.sememeBuilderService()
         .getDynamicSememeBuilder(rdv.getReferencedComponentNid(),
                                  DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE
                                        .getSequence(),
                                  new DynamicSememeData[] { new DynamicSememeStringImpl(comment.getCommentText()),
                                  (StringUtils.isBlank(comment.getCommentContext()) ? null
            : new DynamicSememeStringImpl(comment.getCommentContext())) })
         .build(editCoord, ChangeCheckerMode.ACTIVE);

      @SuppressWarnings("deprecation")
	final
      Task<Optional<CommitRecord>> task = Get.commitService()
                                             .commit("Added comment");

      try {
         task.get();
      } catch (final Exception e) {
         throw new RuntimeException();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Read all comments for a particular mapping item (which could be a mapping set, or a mapping item).
    *
    * @param mappingUUID - The UUID of a MappingSet or a MappingItem
    * @param stampCoord the stamp coord
    * @return the comments
    * @throws RuntimeException the runtime exception
    */
   public static List<MappingItemComment> getComments(UUID mappingUUID,
         StampCoordinate stampCoord)
            throws RuntimeException {
      final List<MappingItemComment> comments = new ArrayList<MappingItemComment>();

      Get.sememeService().getSememesForComponentFromAssemblage(Get.identifierService()
            .getNidForUuids(mappingUUID),
            DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE
                                  .getSequence()).forEach(sememeC -> {
                     @SuppressWarnings({ "unchecked", "rawtypes" })
					final
                     Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology) sememeC).getLatestVersion(
                                                                           DynamicSememe.class, stampCoord.makeAnalog(
                                                                              State.ACTIVE, State.INACTIVE));

                     if (latest.isPresent()) {
                        comments.add(new MappingItemComment(latest.get().value()));

                        if (latest.get()
                                  .contradictions()
                                  .isPresent()) {
                           // TODO handle contradictions properly
                           latest.get()
                                 .contradictions()
                                 .get()
                                 .forEach((contradiction) -> comments.add(new MappingItemComment(contradiction)));
                        }
                     }
                  });
      return comments;
   }
}

