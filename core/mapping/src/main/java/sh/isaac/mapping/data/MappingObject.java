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

import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.beans.property.SimpleStringProperty;

import sh.isaac.api.Get;
import sh.isaac.api.util.StringUtils;

//~--- classes ----------------------------------------------------------------

public class MappingObject
        extends StampedItem {
   public static final Comparator<MappingObject> editorStatusComparator = (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getEditorStatusName(), o2.getEditorStatusName());

   //~--- fields --------------------------------------------------------------

   protected UUID                       editorStatusConcept         = null;
   protected int                        editorStatusConceptNid      = 0;
   protected final SimpleStringProperty editorStatusConceptProperty = new SimpleStringProperty();
   protected HashMap<UUID, String>      cachedValues                = new HashMap<>();

   //~--- methods -------------------------------------------------------------

   protected void propertyLookup(UUID uuid, SimpleStringProperty property) {
      if (uuid == null) {
         property.set(null);
      } else {
         final String cachedValue = this.cachedValues.get(uuid);

         if (cachedValue != null) {
            property.set(cachedValue);
         } else {
            property.set("-");
            Get.workExecutors().getExecutor().execute(() -> {
                           final String s = Get.conceptDescriptionText(Get.identifierService()
                                                                    .getConceptSequenceForUuids(uuid));

                           this.cachedValues.put(uuid, s);
                           Platform.runLater(() -> {
                                                property.set(s);
                                             });
                        });
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the editorStatusConcept
    */
   public UUID getEditorStatusConcept() {
      return this.editorStatusConcept;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param editorStatusConcept the editorStatusConcept to set
    */
   public void setEditorStatusConcept(UUID editorStatusConcept) {
      this.editorStatusConcept    = editorStatusConcept;
      this.editorStatusConceptNid = getNidForUuidSafe(editorStatusConcept);
      propertyLookup(editorStatusConcept, this.editorStatusConceptProperty);
   }

   //~--- get methods ---------------------------------------------------------

   public int getEditorStatusConceptNid() {
      return this.editorStatusConceptNid;
   }

   public SimpleStringProperty getEditorStatusConceptProperty() {
      return this.editorStatusConceptProperty;
   }

   public String getEditorStatusName() {
      return this.editorStatusConceptProperty.get();
   }

   public static int getNidForUuidSafe(UUID uuid) {
      return (uuid == null) ? 0
                            : Get.identifierService()
                                 .getNidForUuids(uuid);
   }
}

