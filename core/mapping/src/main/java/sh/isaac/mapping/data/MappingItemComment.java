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

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.component.sememe.version.DynamicSememe;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MappingItemComment}
 *
 * @author David Triglianos
 */
public class MappingItemComment
        extends StampedItem {
   private String commentText;
   private String commentContext;
   private UUID   mappingItemUUID;
   private UUID   primoridalUUID;

   //~--- constructors --------------------------------------------------------

   protected MappingItemComment(DynamicSememe<?> comment)
            throws RuntimeException {
      read(comment);
   }

   //~--- methods -------------------------------------------------------------

   private void read(DynamicSememe<?> commentRefex)
            throws RuntimeException {
      this.commentText = commentRefex.getData()[0]
                                .getDataObject()
                                .toString();
      this.commentContext = (((commentRefex.getData().length > 1) &&
                         (commentRefex.getData()[1] != null)) ? commentRefex.getData()[1]
                               .getDataObject()
                               .toString()
            : null);
      this.mappingItemUUID = Get.identifierService()
                           .getUuidPrimordialForNid(commentRefex.getReferencedComponentNid())
                           .get();
      this.primoridalUUID  = commentRefex.getPrimordialUuid();
      readStampDetails(commentRefex);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the commentContext
    */
   public String getCommentContext() {
      return this.commentContext;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param commentContext the commentContext to set - optional field on a comment used for arbitrary purposed by an editor.
    */
   public void setCommentContext(String commentContext) {
      this.commentContext = commentContext;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the commentText
    */
   public String getCommentText() {
      return this.commentText;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param commentText the commentText to set
    */
   public void setCommentText(String commentText) {
      this.commentText = commentText;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the mappingItemUUID - which is the identifier of the thing that the comment is attached to
    */
   public UUID getMappingItemUUID() {
      return this.mappingItemUUID;
   }

   /**
    * @return the primoridalUUID - the identifier of this comment - which I'll note isn't globally unique - If you edit this comment,
    * it will retain the same UUID - but the DB will now contain two versions of the comment - the old and the new - you would need
    * this variable and the creationDate to be globally unique.
    */
   public UUID getPrimordialUUID() {
      return this.primoridalUUID;
   }
}

