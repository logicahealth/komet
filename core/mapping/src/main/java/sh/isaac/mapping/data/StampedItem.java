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

import java.text.SimpleDateFormat;

import java.util.Comparator;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.beans.property.SimpleStringProperty;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.util.StringUtils;

//~--- classes ----------------------------------------------------------------

/**
 * {@link StampedItem}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class StampedItem {
   public static final Comparator<StampedItem> statusComparator = (o1, o2) -> Boolean.compare(o2.isActive(), o1.isActive());
   public static final Comparator<StampedItem> timeComparator = (o1, o2) -> Long.compare(o1.getTime(), o2.getTime());
   public static final Comparator<StampedItem> authorComparator = (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getAuthorProperty()
       .get(), o2.getAuthorProperty()
                 .get());
   public static final Comparator<StampedItem> moduleComparator = (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getModuleProperty()
       .get(), o2.getModuleProperty()
                 .get());
   public static final Comparator<StampedItem> pathComparator = (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getPathProperty()
       .get(), o2.getPathProperty()
                 .get());

   //~--- fields --------------------------------------------------------------

   private transient boolean              lazyLoadFinished_ = false;
   private transient SimpleStringProperty authorSSP         = new SimpleStringProperty("-");
   private transient SimpleStringProperty moduleSSP         = new SimpleStringProperty("-");;
   private transient SimpleStringProperty pathSSP           = new SimpleStringProperty("-");;
   private transient SimpleStringProperty statusSSP         = new SimpleStringProperty("-");;
   private transient SimpleStringProperty timeSSP           = new SimpleStringProperty("-");;
   private StampedVersion                 componentVersion_;
   private transient UUID                 authorUUID;
   private transient UUID                 moduleUUID;
   private transient UUID                 pathUUID;

   //~--- methods -------------------------------------------------------------

   protected void readStampDetails(StampedVersion componentVersion)
            throws RuntimeException {
      this.componentVersion_ = componentVersion;
   }

   private void lazyLoad() {
      if (!this.lazyLoadFinished_) {
         this.authorUUID = Get.identifierService()
                         .getUuidPrimordialFromConceptId(this.componentVersion_.getAuthorSequence())
                         .get();
         this.moduleUUID = Get.identifierService()
                         .getUuidPrimordialFromConceptId(this.componentVersion_.getModuleSequence())
                         .get();
         this.pathUUID = Get.identifierService()
                       .getUuidPrimordialFromConceptId(this.componentVersion_.getPathSequence())
                       .get();
         Get.workExecutors().getExecutor().execute(() -> {
                        final String authorName = Get.conceptDescriptionText(Get.identifierService()
                                                                          .getConceptSequenceForUuids(this.authorUUID));
                        final String moduleName = Get.conceptDescriptionText(Get.identifierService()
                                                                          .getConceptSequenceForUuids(this.moduleUUID));
                        final String pathName = Get.conceptDescriptionText(Get.identifierService()
                                                                        .getConceptSequenceForUuids(this.pathUUID));

                        Platform.runLater(() -> {
                                             this.authorSSP.set(authorName);
                                             this.moduleSSP.set(moduleName);
                                             this.pathSSP.set(pathName);
                                             this.statusSSP.set(this.isActive() ? "Active"
                     : "Inactive");
                                             this.timeSSP.set(new SimpleDateFormat("MM/dd/yy HH:mm").format(this.getTime()));
                                          });
                     });
      }

      this.lazyLoadFinished_ = true;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the isActive
    */
   public boolean isActive() {
      return this.componentVersion_.getState() == State.ACTIVE;
   }

   public int getAuthor() {
      return this.componentVersion_.getAuthorSequence();
   }

   /**
    * @return the authorName - a UUID that identifies a concept that represents the Author
    */
   public UUID getAuthorName() {
      lazyLoad();
      return this.authorUUID;
   }

   public SimpleStringProperty getAuthorProperty() {
      lazyLoad();
      return this.authorSSP;
   }

   public StampedVersion getComponentVersion() {
      return this.componentVersion_;
   }

   public int getModule() {
      return this.componentVersion_.getModuleSequence();
   }

   public SimpleStringProperty getModuleProperty() {
      lazyLoad();
      return this.moduleSSP;
   }

   /**
    * @return the moduleUUID
    */
   public UUID getModuleUUID() {
      lazyLoad();
      return this.moduleUUID;
   }

   public int getPath() {
      return this.componentVersion_.getPathSequence();
   }

   public SimpleStringProperty getPathProperty() {
      lazyLoad();
      return this.pathSSP;
   }

   /**
    * @return the pathUUID
    */
   public UUID getPathUUID() {
      lazyLoad();
      return this.pathUUID;
   }

   public SimpleStringProperty getStatusProperty() {
      lazyLoad();
      return this.statusSSP;
   }

   /**
    * @return the creationDate
    */
   public long getTime() {
      return this.componentVersion_.getTime();
   }

   public SimpleStringProperty getTimeProperty() {
      lazyLoad();
      return this.timeSSP;
   }
}

