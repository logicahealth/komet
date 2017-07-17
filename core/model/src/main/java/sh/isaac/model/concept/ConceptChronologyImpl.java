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



package sh.isaac.model.concept;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.relationship.RelationshipVersionAdaptor;
import sh.isaac.model.ObjectChronologyImpl;
import sh.isaac.model.relationship.RelationshipAdaptorChronologyImpl;
import sh.isaac.model.sememe.version.LogicGraphSememeImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ConceptChronologyImpl.
 *
 * @author kec
 */
public class ConceptChronologyImpl
        extends ObjectChronologyImpl<ConceptVersionImpl>
         implements ConceptChronology<ConceptVersionImpl>, OchreExternalizable {
   /** The concept origin relationship list. */
   List<RelationshipAdaptorChronologyImpl> conceptOriginRelationshipList;

   /** The concept origin relationship list defalt coordinate. */
   List<RelationshipAdaptorChronologyImpl> conceptOriginRelationshipListDefaltCoordinate;

   /** The relationship list with concept as destination. */
   List<RelationshipAdaptorChronologyImpl> relationshipListWithConceptAsDestination;

   /** The relationship list with concept as destination list defalt coordinate. */
   List<RelationshipAdaptorChronologyImpl> relationshipListWithConceptAsDestinationListDefaltCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept chronology impl.
    */
   private ConceptChronologyImpl() {}

   /**
    * Instantiates a new concept chronology impl.
    *
    * @param primordialUuid the primordial uuid
    * @param nid the nid
    * @param containerSequence the container sequence
    */
   public ConceptChronologyImpl(UUID primordialUuid, int nid, int containerSequence) {
      super(primordialUuid, nid, containerSequence);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Contains description.
    *
    * @param descriptionText the description text
    * @return true, if successful
    */
   @Override
   public boolean containsDescription(String descriptionText) {
      return Get.sememeService()
                .getDescriptionsForComponent(getNid())
                .anyMatch((desc) -> desc.getVersionList()
                                        .stream()
                                        .anyMatch((version) -> version.getText()
                                              .equals(descriptionText)));
   }

   /**
    * Contains description.
    *
    * @param descriptionText the description text
    * @param stampCoordinate the stamp coordinate
    * @return true, if successful
    */
   @Override
   public boolean containsDescription(String descriptionText, StampCoordinate stampCoordinate) {
      return Get.sememeService()
                .getSnapshot(DescriptionSememe.class, stampCoordinate)
                .getLatestDescriptionVersionsForComponent(getNid())
                .anyMatch((latestVersion) -> latestVersion.value().isPresent() && latestVersion.value().get()
                      .getText()
                      .equals(descriptionText));
   }

   /**
    * Creates the mutable version.
    *
    * @param stampSequence the stamp sequence
    * @return the concept version impl
    */
   @Override
   public ConceptVersionImpl createMutableVersion(int stampSequence) {
      final ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence, nextVersionSequence());

      addVersion(newVersion);
      return newVersion;
   }

   /**
    * Creates the mutable version.
    *
    * @param state the state
    * @param ec the ec
    * @return the concept version impl
    */
   @Override
   public ConceptVersionImpl createMutableVersion(State state, EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                                   .getStampSequence(state,
                                         Long.MAX_VALUE,
                                         ec.getAuthorSequence(),
                                         ec.getModuleSequence(),
                                         ec.getPathSequence());
      final ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence, nextVersionSequence());

      addVersion(newVersion);
      return newVersion;
   }

   /**
    * Make.
    *
    * @param data the data
    * @return the concept chronology impl
    */
   public static ConceptChronologyImpl make(ByteArrayDataBuffer data) {
      final ConceptChronologyImpl conceptChronology = new ConceptChronologyImpl();

      conceptChronology.readData(data);
      return conceptChronology;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder builder = new StringBuilder();

      builder.append("ConceptChronologyImpl{");
      builder.append(toUserString());
      builder.append(" <");
      builder.append(getConceptSequence());
      builder.append("> ");
      toString(builder);
      return builder.toString();
   }

   /**
    * To user string.
    *
    * @return the string
    */
   @Override
   public String toUserString() {
      final List<SememeChronology<? extends DescriptionSememe<?>>> descList = getConceptDescriptionList();

      if (descList.isEmpty()) {
         return "no description for concept: " + getUuidList() + " " + getConceptSequence() + " " + getNid();
      }

      return getConceptDescriptionList().get(0)
                                        .getVersionList()
                                        .get(0)
                                        .getText();
   }

   /**
    * Write chronicle data.
    *
    * @param data the data
    */
   @Override
   public void writeChronicleData(ByteArrayDataBuffer data) {
      super.writeChronicleData(data);
   }

   /**
    * Make version.
    *
    * @param stampSequence the stamp sequence
    * @param bb the bb
    * @return the concept version impl
    */
   @Override
   protected ConceptVersionImpl makeVersion(int stampSequence, ByteArrayDataBuffer bb) {
      return new ConceptVersionImpl(this, stampSequence, bb.getShort());
   }

   /**
    * Put additional chronicle fields.
    *
    * @param out the out
    */
   @Override
   protected void putAdditionalChronicleFields(ByteArrayDataBuffer out) {
      // nothing to put for ConceptChronology...
   }

   /**
    * Skip additional chronicle fields.
    *
    * @param in the in
    */
   @Override
   protected void skipAdditionalChronicleFields(ByteArrayDataBuffer in) {
      // nothing to read for ConceptChronology...
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the additional chronicle fields.
    *
    * @param in the in
    * @return the additional chronicle fields
    */
   @Override
   protected void getAdditionalChronicleFields(ByteArrayDataBuffer in) {
      // nothing to read for ConceptChronology...
   }

   /**
    * Gets the concept description list.
    *
    * @return the concept description list
    */
   @Override
   public List<SememeChronology<? extends DescriptionSememe<?>>> getConceptDescriptionList() {
      if (Get.sememeServiceAvailable()) {
         return Get.sememeService()
                   .getDescriptionsForComponent(getNid())
                   .collect(Collectors.toList());
      } else {
         return new ArrayList<>();
      }
   }

   /**
    * Gets the concept description text.
    *
    * @return the concept description text
    */
   @Override
   public String getFullySpecifiedConceptDescriptionText() {
      return Get.conceptDescriptionText(getNid());
   }

   /**
    * Gets the concept sequence.
    *
    * @return the concept sequence
    */
   @Override
   public int getConceptSequence() {
      return getContainerSequence();
   }

   /**
    * Gets the data format version.
    *
    * @return the data format version
    */
   @Override
   public byte getDataFormatVersion() {
      return 0;
   }

   /**
    * Gets the fully specified description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the fully specified description
    */
   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate) {
      return languageCoordinate.getFullySpecifiedDescription(getConceptDescriptionList(), stampCoordinate);
   }

   /**
    * Gets the logical definition.
    *
    * @param stampCoordinate the stamp coordinate
    * @param premiseType the premise type
    * @param logicCoordinate the logic coordinate
    * @return the logical definition
    */
   @Override
   public Optional<LatestVersion<LogicGraphSememe<?>>> getLogicalDefinition(StampCoordinate stampCoordinate,
         PremiseType premiseType,
         LogicCoordinate logicCoordinate) {
      int assemblageSequence;

      if (premiseType == PremiseType.INFERRED) {
         assemblageSequence = logicCoordinate.getInferredAssemblageSequence();
      } else {
         assemblageSequence = logicCoordinate.getStatedAssemblageSequence();
      }

      final Optional<?> optional = Get.sememeService()
                                      .getSnapshot(LogicGraphSememe.class, stampCoordinate)
                                      .getLatestSememeVersionsForComponentFromAssemblage(getNid(), assemblageSequence)
                                      .findFirst();

      return (Optional<LatestVersion<LogicGraphSememe<?>>>) optional;
   }

   /**
    * Gets the logical definition chronology report.
    *
    * @param stampCoordinate the stamp coordinate
    * @param premiseType the premise type
    * @param logicCoordinate the logic coordinate
    * @return the logical definition chronology report
    */
   @Override
   public String getLogicalDefinitionChronologyReport(StampCoordinate stampCoordinate,
         PremiseType premiseType,
         LogicCoordinate logicCoordinate) {
      int assemblageSequence;

      if (premiseType == PremiseType.INFERRED) {
         assemblageSequence = logicCoordinate.getInferredAssemblageSequence();
      } else {
         assemblageSequence = logicCoordinate.getStatedAssemblageSequence();
      }

      final Optional<SememeChronology<? extends SememeVersion<?>>> definitionChronologyOptional = Get.sememeService()
                                                                                                     .getSememesForComponentFromAssemblage(
                                                                                                        getNid(),
                                                                                                              assemblageSequence)
                                                                                                     .findFirst();

      if (definitionChronologyOptional.isPresent()) {
         final Collection<LogicGraphSememeImpl> versions =
            (Collection<LogicGraphSememeImpl>) definitionChronologyOptional.get()
                                                                           .getVisibleOrderedVersionList(
                                                                              stampCoordinate);

//       Collection<LogicGraphSememeImpl> versionsList = new ArrayList<>();
//       for (LogicGraphSememeImpl lgs : definitionChronologyOptional.get().getVisibleOrderedVersionList(stampCoordinate)) {
//           
//       }
         final StringBuilder builder = new StringBuilder();

         builder.append("_______________________________________________________________________\n");
         builder.append("  Encountered concept '")
                .append(Get.conceptDescriptionText(getNid()))
                .append("' with ")
                .append(versions.size())
                .append(" definition versions:\n");
         builder.append("￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣\n");

         int               version         = 0;
         LogicalExpression previousVersion = null;

         for (final LogicGraphSememeImpl lgmv: versions) {
            final LogicalExpression lg = lgmv.getLogicalExpression();

            builder.append(" Version ")
                   .append(version++)
                   .append("\n")
                   .append(Get.stampService()
                              .describeStampSequence(lgmv.getStampSequence()))
                   .append("\n");

            if (previousVersion == null) {
               builder.append(lg);
            } else {
               final IsomorphicResults solution = lg.findIsomorphisms(previousVersion);

               builder.append(solution);
            }

            builder.append("_______________________________________________________________________\n");
            previousVersion = lg;
         }

         builder.append("￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣\n");
         return builder.toString();
      }

      return "No definition found. ";
   }

   /**
    * Gets the ochre object type.
    *
    * @return the ochre object type
    */
   @Override
   public OchreExternalizableObjectType getOchreObjectType() {
      return OchreExternalizableObjectType.CONCEPT;
   }

   /**
    * Gets the preferred description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampCoordinate the stamp coordinate
    * @return the preferred description
    */
   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription(LanguageCoordinate languageCoordinate,
         StampCoordinate stampCoordinate) {
      return languageCoordinate.getPreferredDescription(getConceptDescriptionList(), stampCoordinate);
   }

   /**
    * Gets the relationship list originating from concept.
    *
    * @return the relationship list originating from concept
    */
   @Override
   public List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListOriginatingFromConcept() {
      if (this.conceptOriginRelationshipList == null) {
         this.conceptOriginRelationshipList = new ArrayList<>();
         Get.logicService()
            .getRelationshipAdaptorsOriginatingWithConcept(this)
            .forEach((relAdaptor) -> {
                        this.conceptOriginRelationshipList.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                     });
      }

      return this.conceptOriginRelationshipList;
   }

   /**
    * Gets the relationship list originating from concept.
    *
    * @param logicCoordinate the logic coordinate
    * @return the relationship list originating from concept
    */
   @Override
   public List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListOriginatingFromConcept(
           LogicCoordinate logicCoordinate) {
      if (this.conceptOriginRelationshipList == null) {
         this.conceptOriginRelationshipList = new ArrayList<>();
         Get.logicService().getRelationshipAdaptorsOriginatingWithConcept(this, logicCoordinate).forEach((relAdaptor) -> {
                        this.conceptOriginRelationshipList.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                     });
      }

      return this.conceptOriginRelationshipList;
   }

   /**
    * Gets the relationship list with concept as destination.
    *
    * @return the relationship list with concept as destination
    */
   @Override
   public List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListWithConceptAsDestination() {
      if (this.relationshipListWithConceptAsDestinationListDefaltCoordinate == null) {
         this.relationshipListWithConceptAsDestinationListDefaltCoordinate = new ArrayList<>();
         Get.logicService()
            .getRelationshipAdaptorsWithConceptAsDestination(this)
            .forEach((relAdaptor) -> {
                        this.relationshipListWithConceptAsDestinationListDefaltCoordinate.add(
                            (RelationshipAdaptorChronologyImpl) relAdaptor);
                     });
      }

      return this.relationshipListWithConceptAsDestinationListDefaltCoordinate;
   }

   /**
    * Gets the relationship list with concept as destination.
    *
    * @param logicCoordinate the logic coordinate
    * @return the relationship list with concept as destination
    */
   @Override
   public List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListWithConceptAsDestination(
           LogicCoordinate logicCoordinate) {
      if (this.relationshipListWithConceptAsDestination == null) {
         this.relationshipListWithConceptAsDestination = new ArrayList<>();
         Get.logicService().getRelationshipAdaptorsWithConceptAsDestination(this, logicCoordinate).forEach((relAdaptor) -> {
                        this.relationshipListWithConceptAsDestination.add(
                            (RelationshipAdaptorChronologyImpl) relAdaptor);
                     });
      }

      return this.relationshipListWithConceptAsDestination;
   }

   @Override
   public Optional<String> getPreferedConceptDescriptionText() {
     return Optional.ofNullable(Get.defaultCoordinate().getPreferredDescriptionText(this.getConceptSequence()));
   }
}

