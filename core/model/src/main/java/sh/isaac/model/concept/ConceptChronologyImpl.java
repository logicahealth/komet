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
 *
 * @author kec
 */
public class ConceptChronologyImpl
        extends ObjectChronologyImpl<ConceptVersionImpl>
         implements ConceptChronology<ConceptVersionImpl>, OchreExternalizable {
   List<RelationshipAdaptorChronologyImpl> conceptOriginRelationshipList;
   List<RelationshipAdaptorChronologyImpl> conceptOriginRelationshipListDefaltCoordinate;
   List<RelationshipAdaptorChronologyImpl> relationshipListWithConceptAsDestination;
   List<RelationshipAdaptorChronologyImpl> relationshipListWithConceptAsDestinationListDefaltCoordinate;

   //~--- constructors --------------------------------------------------------

   private ConceptChronologyImpl() {}

   public ConceptChronologyImpl(UUID primordialUuid, int nid, int containerSequence) {
      super(primordialUuid, nid, containerSequence);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean containsDescription(String descriptionText) {
      return Get.sememeService()
                .getDescriptionsForComponent(getNid())
                .anyMatch((desc) -> desc.getVersionList()
                                        .stream()
                                        .anyMatch((version) -> version.getText()
                                              .equals(descriptionText)));
   }

   @Override
   public boolean containsDescription(String descriptionText, StampCoordinate stampCoordinate) {
      return Get.sememeService()
                .getSnapshot(DescriptionSememe.class, stampCoordinate)
                .getLatestDescriptionVersionsForComponent(getNid())
                .anyMatch((latestVersion) -> latestVersion.value()
                      .getText()
                      .equals(descriptionText));
   }

   @Override
   public ConceptVersionImpl createMutableVersion(int stampSequence) {
      ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence, nextVersionSequence());

      addVersion(newVersion);
      return newVersion;
   }

   @Override
   public ConceptVersionImpl createMutableVersion(State state, EditCoordinate ec) {
      int stampSequence = Get.stampService()
                             .getStampSequence(state,
                                   Long.MAX_VALUE,
                                   ec.getAuthorSequence(),
                                   ec.getModuleSequence(),
                                   ec.getPathSequence());
      ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence, nextVersionSequence());

      addVersion(newVersion);
      return newVersion;
   }

   public static ConceptChronologyImpl make(ByteArrayDataBuffer data) {
      ConceptChronologyImpl conceptChronology = new ConceptChronologyImpl();

      conceptChronology.readData(data);
      return conceptChronology;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();

      builder.append("ConceptChronologyImpl{");
      builder.append(toUserString());
      builder.append(" <");
      builder.append(getConceptSequence());
      builder.append("> ");
      toString(builder);
      return builder.toString();
   }

   @Override
   public String toUserString() {
      List<SememeChronology<? extends DescriptionSememe<?>>> descList = getConceptDescriptionList();

      if (descList.isEmpty()) {
         return "no description for concept: " + getUuidList() + " " + getConceptSequence() + " " + getNid();
      }

      return getConceptDescriptionList().get(0)
                                        .getVersionList()
                                        .get(0)
                                        .getText();
   }

   @Override
   public void writeChronicleData(ByteArrayDataBuffer data) {
      super.writeChronicleData(data);
   }

   @Override
   protected ConceptVersionImpl makeVersion(int stampSequence, ByteArrayDataBuffer bb) {
      return new ConceptVersionImpl(this, stampSequence, bb.getShort());
   }

   @Override
   protected void putAdditionalChronicleFields(ByteArrayDataBuffer out) {
      // nothing to put for ConceptChronology...
   }

   @Override
   protected void skipAdditionalChronicleFields(ByteArrayDataBuffer in) {
      // nothing to read for ConceptChronology...
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   protected void getAdditionalChronicleFields(ByteArrayDataBuffer in) {
      // nothing to read for ConceptChronology...
   }

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

   @Override
   public String getConceptDescriptionText() {
      return Get.conceptDescriptionText(getNid());
   }

   @Override
   public int getConceptSequence() {
      return getContainerSequence();
   }

   @Override
   public byte getDataFormatVersion() {
      return 0;
   }

   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription(
           LanguageCoordinate languageCoordinate,
           StampCoordinate stampCoordinate) {
      return languageCoordinate.getFullySpecifiedDescription(getConceptDescriptionList(), stampCoordinate);
   }

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

      Optional<?> optional = Get.sememeService()
                                .getSnapshot(LogicGraphSememe.class, stampCoordinate)
                                .getLatestSememeVersionsForComponentFromAssemblage(getNid(), assemblageSequence)
                                .findFirst();

      return (Optional<LatestVersion<LogicGraphSememe<?>>>) optional;
   }

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

      Optional<SememeChronology<? extends SememeVersion<?>>> definitionChronologyOptional = Get.sememeService()
                                                                                               .getSememesForComponentFromAssemblage(
                                                                                                  getNid(),
                                                                                                        assemblageSequence)
                                                                                               .findFirst();

      if (definitionChronologyOptional.isPresent()) {
         Collection<LogicGraphSememeImpl> versions =
            (Collection<LogicGraphSememeImpl>) definitionChronologyOptional.get()
                                                                           .getVisibleOrderedVersionList(
                                                                              stampCoordinate);

//       Collection<LogicGraphSememeImpl> versionsList = new ArrayList<>();
//       for (LogicGraphSememeImpl lgs : definitionChronologyOptional.get().getVisibleOrderedVersionList(stampCoordinate)) {
//           
//       }
         StringBuilder builder = new StringBuilder();

         builder.append("_______________________________________________________________________\n");
         builder.append("  Encountered concept '")
                .append(Get.conceptDescriptionText(getNid()))
                .append("' with ")
                .append(versions.size())
                .append(" definition versions:\n");
         builder.append("￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣\n");

         int               version         = 0;
         LogicalExpression previousVersion = null;

         for (LogicGraphSememeImpl lgmv: versions) {
            LogicalExpression lg = lgmv.getLogicalExpression();

            builder.append(" Version ")
                   .append(version++)
                   .append("\n")
                   .append(Get.stampService()
                              .describeStampSequence(lgmv.getStampSequence()))
                   .append("\n");

            if (previousVersion == null) {
               builder.append(lg);
            } else {
               IsomorphicResults solution = lg.findIsomorphisms(previousVersion);

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

   @Override
   public OchreExternalizableObjectType getOchreObjectType() {
      return OchreExternalizableObjectType.CONCEPT;
   }

   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription(LanguageCoordinate languageCoordinate,
         StampCoordinate stampCoordinate) {
      return languageCoordinate.getPreferredDescription(getConceptDescriptionList(), stampCoordinate);
   }

   @Override
   public List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListOriginatingFromConcept() {
      if (conceptOriginRelationshipList == null) {
         conceptOriginRelationshipList = new ArrayList<>();
         Get.logicService()
            .getRelationshipAdaptorsOriginatingWithConcept(this)
            .forEach((relAdaptor) -> {
                        conceptOriginRelationshipList.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                     });
      }

      return conceptOriginRelationshipList;
   }

   @Override
   public List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListOriginatingFromConcept(
           LogicCoordinate logicCoordinate) {
      if (conceptOriginRelationshipList == null) {
         conceptOriginRelationshipList = new ArrayList<>();
         Get.logicService().getRelationshipAdaptorsOriginatingWithConcept(this, logicCoordinate).forEach((relAdaptor) -> {
                        conceptOriginRelationshipList.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                     });
      }

      return conceptOriginRelationshipList;
   }

   @Override
   public List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListWithConceptAsDestination() {
      if (relationshipListWithConceptAsDestinationListDefaltCoordinate == null) {
         relationshipListWithConceptAsDestinationListDefaltCoordinate = new ArrayList<>();
         Get.logicService()
            .getRelationshipAdaptorsWithConceptAsDestination(this)
            .forEach((relAdaptor) -> {
                        relationshipListWithConceptAsDestinationListDefaltCoordinate.add(
                            (RelationshipAdaptorChronologyImpl) relAdaptor);
                     });
      }

      return relationshipListWithConceptAsDestinationListDefaltCoordinate;
   }

   @Override
   public List<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipListWithConceptAsDestination(
           LogicCoordinate logicCoordinate) {
      if (relationshipListWithConceptAsDestination == null) {
         relationshipListWithConceptAsDestination = new ArrayList<>();
         Get.logicService().getRelationshipAdaptorsWithConceptAsDestination(this, logicCoordinate).forEach((relAdaptor) -> {
                        relationshipListWithConceptAsDestination.add((RelationshipAdaptorChronologyImpl) relAdaptor);
                     });
      }

      return relationshipListWithConceptAsDestination;
   }
}

