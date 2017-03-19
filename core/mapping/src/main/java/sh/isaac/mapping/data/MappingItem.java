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

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.beans.property.SimpleStringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.isaac.api.Get;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.StringUtils;
import sh.isaac.utility.Frills;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MappingItem}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MappingItem
        extends MappingObject {
   /** The Constant LOG. */
   private static final Logger LOG = LoggerFactory.getLogger(MappingItem.class);

   /** The Constant NO_MAP_NAME. */
   private static final String NO_MAP_NAME = "(not mapped)";

   /** The Constant sourceComparator. */
   public static final Comparator<MappingItem> sourceComparator =
      (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getSourceConceptProperty()
                                                         .get(),
                                                       o2.getSourceConceptProperty()
                                                             .get());

   /** The Constant targetComparator. */
   public static final Comparator<MappingItem> targetComparator =
      (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getTargetConceptProperty()
                                                         .get(),
                                                       o2.getTargetConceptProperty()
                                                             .get());

   /** The Constant qualifierComparator. */
   public static final Comparator<MappingItem> qualifierComparator =
      (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getQualifierConceptProperty()
                                                         .get(),
                                                       o2.getQualifierConceptProperty()
                                                             .get());

   //~--- fields --------------------------------------------------------------

   /** The lazy load complete. */
   private transient boolean lazyLoadComplete = false;

   /** The source concept property. */
   private transient final SimpleStringProperty sourceConceptProperty = new SimpleStringProperty();

   /** The target concept property. */
   private transient final SimpleStringProperty targetConceptProperty = new SimpleStringProperty();

   /** The qualifier concept property. */
   private transient final SimpleStringProperty qualifierConceptProperty = new SimpleStringProperty();

   /** The comments property. */
   private transient final SimpleStringProperty commentsProperty = new SimpleStringProperty();

   /** The uuids. */
   private List<UUID> uuids;

   /** The mapping set sequence. */
   private int sourceConceptNid, mappingSetSequence;

   /** The target concept. */
   private UUID qualifierConcept, targetConcept;

   /** The data. */
   private DynamicSememeData[] data;

   /** The source concept. */
   private transient UUID mappingSetIDConcept, sourceConcept;

   /** The qualifier concept nid. */
   private transient int targetConceptNid, qualifierConceptNid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new mapping item.
    *
    * @param sememe the sememe
    * @throws RuntimeException the runtime exception
    */
   protected MappingItem(DynamicSememe<?> sememe)
            throws RuntimeException {
      read(sememe);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Add a comment to this mapping set.
    *
    * @param commentText - the text of the comment
    * @param stampCoord the stamp coord
    * @param editCoord the edit coord
    * @return - the added comment
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public MappingItemComment addComment(String commentText,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws IOException {
      // TODO do we want to utilize the other comment field (don't have to)
      return MappingItemCommentDAO.createMappingItemComment(this.getPrimordialUUID(),
            commentText,
            null,
            stampCoord,
            editCoord);
   }

   /**
    * Refresh comments property.
    *
    * @param stampCoord the stamp coord
    */
   public void refreshCommentsProperty(StampCoordinate stampCoord) {
      Get.workExecutors().getExecutor().execute(() -> {
                     final StringBuilder commentValue = new StringBuilder();

                     try {
                        final List<MappingItemComment> comments = getComments(stampCoord);

                        if (comments.size() > 0) {
                           commentValue.append(comments.get(0)
                                 .getCommentText());
                        }

                        if (comments.size() > 1) {
                           commentValue.append(" (+" + Integer.toString(comments.size() - 1) + " more)");
                        }
                     } catch (final IOException e) {
                        LOG.error("Error reading comments!", e);
                     }

                     Platform.runLater(() -> {
                                          this.commentsProperty.set(commentValue.toString());
                                       });
                  });
   }

   /**
    * Lazy load.
    */
   private void lazyLoad() {
      if (!this.lazyLoadComplete) {
         this.mappingSetIDConcept = Get.identifierService()
                                       .getUuidPrimordialForNid(this.mappingSetSequence)
                                       .get();
         setSourceConcept(Get.identifierService()
                             .getUuidPrimordialForNid(this.sourceConceptNid)
                             .get());

         // TODO remove this
         setEditorStatusConcept((((this.data != null) &&
                                  (this.data.length > 2) &&
                                  (this.data[2] != null)) ? ((DynamicSememeUUID) this.data[2]).getDataUUID()
               : null));
         this.targetConceptNid    = getNidForUuidSafe(this.targetConcept);
         this.qualifierConceptNid = getNidForUuidSafe(this.qualifierConcept);
      }

      this.lazyLoadComplete = true;
   }

   /**
    * Read.
    *
    * @param sememe the sememe
    * @throws RuntimeException the runtime exception
    */
   private void read(DynamicSememe<?> sememe)
            throws RuntimeException {
      readStampDetails(sememe);
      this.mappingSetSequence = sememe.getAssemblageSequence();
      this.sourceConceptNid   = sememe.getReferencedComponentNid();
      this.uuids              = sememe.getUuidList();
      this.data              = sememe.getData();
      setTargetConcept((((this.data != null) &&
                         (this.data.length > 0) &&
                         (this.data[0] != null)) ? ((DynamicSememeUUID) this.data[0]).getDataUUID()
            : null));
      setQualifierConcept((((this.data != null) &&
                            (this.data.length > 1) &&
                            (this.data[1] != null)) ? ((DynamicSememeUUID) this.data[1]).getDataUUID()
            : null));
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the comments.
    *
    * @param stampCoord the stamp coord
    * @return Any comments attached to this mapping set.
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public List<MappingItemComment> getComments(StampCoordinate stampCoord)
            throws IOException {
      return MappingItemCommentDAO.getComments(getPrimordialUUID(), stampCoord);
   }

   /**
    * Gets the comments property.
    *
    * @param stampCoord the stamp coord
    * @return the comments property
    */
   public SimpleStringProperty getCommentsProperty(StampCoordinate stampCoord) {
      refreshCommentsProperty(stampCoord);
      return this.sourceConceptProperty;
   }

   /**
    * Gets the map set sequence.
    *
    * @return the map set sequence
    */
   public int getMapSetSequence() {
      return this.mappingSetSequence;
   }

   /**
    * Gets the mapping set ID concept.
    *
    * @return the mapping set ID concept
    */
   public UUID getMappingSetIDConcept() {
      lazyLoad();
      return this.mappingSetIDConcept;
   }

   /**
    * Gets the primordial UUID.
    *
    * @return the primordialUUID of this Mapping Item.  Note that this doesn't uniquely identify a mapping item within the system
    * as changes to the mapping item will retain the same ID - there will now be multiple versions.  They will differ by date.
    */
   public UUID getPrimordialUUID() {
      return this.uuids.get(0);
   }

   /**
    * Gets the qualifier concept.
    *
    * @return the qualifier concept
    */
   public UUID getQualifierConcept() {
      return this.qualifierConcept;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the qualifier concept.
    *
    * @param qualifierConcept the new qualifier concept
    */
   private void setQualifierConcept(UUID qualifierConcept) {
      this.qualifierConcept = qualifierConcept;
      propertyLookup(qualifierConcept, this.qualifierConceptProperty);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the qualifier concept nid.
    *
    * @return the qualifier concept nid
    */
   public int getQualifierConceptNid() {
      lazyLoad();
      return this.qualifierConceptNid;
   }

   /**
    * Gets the qualifier concept property.
    *
    * @return the qualifier concept property
    */
   public SimpleStringProperty getQualifierConceptProperty() {
      lazyLoad();
      return this.qualifierConceptProperty;
   }

   /**
    * Gets the source concept.
    *
    * @return the source concept
    */
   public UUID getSourceConcept() {
      lazyLoad();
      return this.sourceConcept;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the source concept.
    *
    * @param sourceConcept the new source concept
    */
   private void setSourceConcept(UUID sourceConcept) {
      this.sourceConcept = sourceConcept;
      propertyLookup(sourceConcept, this.sourceConceptProperty);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the source concept nid.
    *
    * @return the source concept nid
    */
   public int getSourceConceptNid() {
      return this.sourceConceptNid;
   }

   /**
    * Gets the source concept property.
    *
    * @return the source concept property
    */
   public SimpleStringProperty getSourceConceptProperty() {
      lazyLoad();
      return this.sourceConceptProperty;
   }

   /**
    * Gets the summary.
    *
    * @return the summary
    */
   public String getSummary() {
      return (isActive() ? "Active "
                         : "Retired ") + "Mapping: " + Frills.getDescription(this.sourceConcept).get() + "-" +
                                         Frills.getDescription(this.mappingSetIDConcept).get() + "-" +
                                         ((this.targetConcept == null) ? "not mapped"
            : Frills.getDescription(this.targetConcept)
                    .get()) + "-" + ((this.qualifierConcept == null) ? "no qualifier"
            : Frills.getDescription(this.qualifierConcept)
                    .get()) + "-" + ((this.editorStatusConcept == null) ? "no status"
            : Frills.getDescription(this.editorStatusConcept)
                    .get()) + "-" + this.uuids.get(0).toString();
   }

   /**
    * Gets the target concept.
    *
    * @return the target concept
    */
   public UUID getTargetConcept() {
      return this.targetConcept;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the target concept.
    *
    * @param targetConcept the new target concept
    */
   private void setTargetConcept(UUID targetConcept) {
      this.targetConcept = targetConcept;

      if (targetConcept == null) {
         this.targetConceptProperty.set(NO_MAP_NAME);
      } else {
         propertyLookup(targetConcept, this.targetConceptProperty);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the target concept nid.
    *
    * @return the target concept nid
    */
   public int getTargetConceptNid() {
      lazyLoad();
      return this.targetConceptNid;
   }

   /**
    * Gets the target concept property.
    *
    * @return the target concept property
    */
   public SimpleStringProperty getTargetConceptProperty() {
      lazyLoad();
      return this.targetConceptProperty;
   }

   /**
    * Gets the UUI ds.
    *
    * @return the UUIDs of this Mapping Item.  Note that this doesn't uniquely identify a mapping item within the system
    * as changes to the mapping item will retain the same ID - there will now be multiple versions.  They will differ by date.
    * There will typically be only one entry in this list (identical to the value of {@link #getPrimordialUUID}
    */
   public List<UUID> getUUIDs() {
      return this.uuids;
   }
}

