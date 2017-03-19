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

/**
 * The Class MappingObject.
 */
public class MappingObject
        extends StampedItem {
   
   /** The Constant editorStatusComparator. */
   public static final Comparator<MappingObject> editorStatusComparator = (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getEditorStatusName(), o2.getEditorStatusName());

   //~--- fields --------------------------------------------------------------

   /** The editor status concept. */
   protected UUID                       editorStatusConcept         = null;
   
   /** The editor status concept nid. */
   protected int                        editorStatusConceptNid      = 0;
   
   /** The editor status concept property. */
   protected final SimpleStringProperty editorStatusConceptProperty = new SimpleStringProperty();
   
   /** The cached values. */
   protected HashMap<UUID, String>      cachedValues                = new HashMap<>();

   //~--- methods -------------------------------------------------------------

   /**
    * Property lookup.
    *
    * @param uuid the uuid
    * @param property the property
    */
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
    * Gets the editor status concept.
    *
    * @return the editorStatusConcept
    */
   public UUID getEditorStatusConcept() {
      return this.editorStatusConcept;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the editor status concept.
    *
    * @param editorStatusConcept the editorStatusConcept to set
    */
   public void setEditorStatusConcept(UUID editorStatusConcept) {
      this.editorStatusConcept    = editorStatusConcept;
      this.editorStatusConceptNid = getNidForUuidSafe(editorStatusConcept);
      propertyLookup(editorStatusConcept, this.editorStatusConceptProperty);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the editor status concept nid.
    *
    * @return the editor status concept nid
    */
   public int getEditorStatusConceptNid() {
      return this.editorStatusConceptNid;
   }

   /**
    * Gets the editor status concept property.
    *
    * @return the editor status concept property
    */
   public SimpleStringProperty getEditorStatusConceptProperty() {
      return this.editorStatusConceptProperty;
   }

   /**
    * Gets the editor status name.
    *
    * @return the editor status name
    */
   public String getEditorStatusName() {
      return this.editorStatusConceptProperty.get();
   }

   /**
    * Gets the nid for uuid safe.
    *
    * @param uuid the uuid
    * @return the nid for uuid safe
    */
   public static int getNidForUuidSafe(UUID uuid) {
      return (uuid == null) ? 0
                            : Get.identifierService()
                                 .getNidForUuids(uuid);
   }
}

