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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleStringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.StringUtils;
import sh.isaac.utility.Frills;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MappingSet}
 *
 * A Convenience class to hide unnecessary OTF bits from the Mapping APIs.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MappingSet
        extends MappingObject {
   /** The Constant LOG. */
   private static final Logger LOG = LoggerFactory.getLogger(MappingSet.class);

   /** The Constant nameComparator. */
   public static final Comparator<MappingSet> nameComparator =
      (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getName(),
                                                       o2.getName());

   /** The Constant purposeComparator. */
   public static final Comparator<MappingSet> purposeComparator =
      (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getPurpose(),
                                                       o2.getPurpose());

   /** The Constant descriptionComparator. */
   public static final Comparator<MappingSet> descriptionComparator =
      (o1, o2) -> StringUtils.compareStringsIgnoreCase(o1.getDescription(),
                                                       o2.getDescription());

   //~--- fields --------------------------------------------------------------

   /** The name property. */
   private final SimpleStringProperty nameProperty = new SimpleStringProperty();

   /** The purpose property. */
   private final SimpleStringProperty purposeProperty = new SimpleStringProperty();

   /** The description property. */
   private final SimpleStringProperty descriptionProperty = new SimpleStringProperty();

   /** The inverse name property. */
   private final SimpleStringProperty inverseNameProperty = new SimpleStringProperty();

   /** The primordial UUID. */

   // private String name, inverseName, description, purpose;
   private UUID primordialUUID;

   //~--- constructors --------------------------------------------------------

   /**
    * Read an existing mapping set from the database.
    *
    * @param refex DynamicSememeChronicleBI<?>
    * @param stampCoord the stamp coord
    * @throws RuntimeException the runtime exception
    */
   protected MappingSet(DynamicSememe<?> refex, StampCoordinate stampCoord)
            throws RuntimeException {
      this.readFromRefex(refex, stampCoord);  // Sets Name, inverseName and Description, etc
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Read from refex.
    *
    * @param refex the refex
    * @param stampCoord the stamp coord
    * @throws RuntimeException the runtime exception
    */
   private void readFromRefex(DynamicSememe<?> refex, StampCoordinate stampCoord)
            throws RuntimeException {
      final Optional<ConceptVersion<?>> mappingConcept = MappingSetDAO.getMappingConcept(refex, stampCoord);

      if (mappingConcept.isPresent()) {
         this.primordialUUID = mappingConcept.get()
               .getPrimordialUuid();
         readStampDetails(mappingConcept.get());

         // setEditorStatusConcept((refex.getData().length > 0 && refex.getData()[0] != null ? ((DynamicSememeUUID) refex.getData()[0]).getDataUUID() : null));
         if ((refex.getData().length > 0) && (refex.getData()[0] != null)) {
            setPurpose(((DynamicSememeString) refex.getData()[0]).getDataString());
         }

         Get.sememeService().getSememesForComponent(mappingConcept.get()
               .getNid()).filter(s -> s.getSememeType() == SememeType.DESCRIPTION).forEach(descriptionC -> {
                        if ((getName() != null) && (getDescription() != null) && (getInverseName() != null)) {
                           // noop... sigh... can't short-circuit in a forEach....
                        } else {
                           @SuppressWarnings({ "rawtypes", "unchecked" })
                           final Optional<LatestVersion<DescriptionSememe<?>>> latest =
                              ((SememeChronology) descriptionC).getLatestVersion(DescriptionSememe.class, stampCoord);

                           // TODO handle contradictions
                           if (latest.isPresent()) {
                              final DescriptionSememe<?> ds = latest.get()
                                                                    .value();

                              if (ds.getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence()) {
                                 if (Frills.isDescriptionPreferred(ds.getNid(), null)) {
                                    setName(ds.getText());
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
                                       setInverseName(ds.getText());
                                    }
                                 }
                              } else if (ds.getDescriptionTypeConceptSequence() ==
                                         MetaData.DEFINITION_DESCRIPTION_TYPE.getConceptSequence()) {
                                 if (Frills.isDescriptionPreferred(ds.getNid(), null)) {
                                    setDescription(ds.getText());
                                 }
                              }
                           }
                        }
                     });
      } else {
         final String error = "cannot read mapping concept!";

         LOG.error(error);
         throw new RuntimeException(error);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the comments.
    *
    * @param stampCoord the stamp coord
    * @return Any comments attached to this mapping set.
    * @throws RuntimeException the runtime exception
    */
   public List<MappingItemComment> getComments(StampCoordinate stampCoord)
            throws RuntimeException {
      return MappingItemCommentDAO.getComments(getPrimordialUUID(), stampCoord);
   }

   /**
    * Gets the description.
    *
    * @return - The user specified description of the mapping set.
    */
   public String getDescription() {
      return this.descriptionProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the description.
    *
    * @param description - specify the description of the mapping set
    */
   public void setDescription(String description) {
      this.descriptionProperty.set(description);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description property.
    *
    * @return the description property
    */
   public SimpleStringProperty getDescriptionProperty() {
      return this.descriptionProperty;
   }

   /**
    * Gets the inverse name.
    *
    * @return - The inverse name of the mapping set - may return null
    */
   public String getInverseName() {
      return this.inverseNameProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the inverse name.
    *
    * @param inverseName - Change the inverse name of the mapping set
    */
   public void setInverseName(String inverseName) {
      this.inverseNameProperty.set(inverseName);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the inverse name property.
    *
    * @return the inverse name property
    */
   public SimpleStringProperty getInverseNameProperty() {
      return this.inverseNameProperty;
   }

   /**
    * Gets the mapping items.
    *
    * @param stampCoord the stamp coord
    * @return the mapping items
    */
   public List<MappingItem> getMappingItems(StampCoordinate stampCoord) {
      List<MappingItem> mappingItems = null;

      try {
         mappingItems = MappingItemDAO.getMappingItems(this.getPrimordialUUID(), stampCoord);
      } catch (final Exception e) {
         LOG.error("Error retrieving Mapping Items for " + this.getName(), e);
         mappingItems = new ArrayList<MappingItem>();
      }

      return mappingItems;
   }

   /**
    * Gets the name.
    *
    * @return the name of the mapping set
    */
   public String getName() {
      return this.nameProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the name.
    *
    * @param name - Change the name of the mapping set
    */
   public void setName(String name) {
      this.nameProperty.set(name);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the name property.
    *
    * @return the name property
    */
   public SimpleStringProperty getNameProperty() {
      return this.nameProperty;
   }

   /**
    * Gets the primordial UUID.
    *
    * @return the identifier of this mapping set
    */
   public UUID getPrimordialUUID() {
      return this.primordialUUID;
   }

   /**
    * Gets the purpose.
    *
    * @return - the 'purpose' of the mapping set - may be null
    */
   public String getPurpose() {
      return this.purposeProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the purpose.
    *
    * @param purpose - The 'purpose' of the mapping set. May specify null.
    */
   public void setPurpose(String purpose) {
      this.purposeProperty.set(purpose);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the purpose property.
    *
    * @return the purpose property
    */
   public SimpleStringProperty getPurposeProperty() {
      return this.purposeProperty;
   }

   /**
    * Gets the summary.
    *
    * @param stampCoord the stamp coord
    * @return The summary of the mapping set
    */
   public String getSummary(StampCoordinate stampCoord) {
      List<MappingItem> mappingItems;

      mappingItems = this.getMappingItems(stampCoord);
      return Integer.toString(mappingItems.size()) + " Mapping Items";
   }
}

