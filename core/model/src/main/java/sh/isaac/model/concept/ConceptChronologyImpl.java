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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.ModelGet;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;


/**
 * The Class ConceptChronologyImpl.
 *
 * @author kec
 */
public class ConceptChronologyImpl
        extends ChronologyImpl
         implements ConceptChronology, IsaacExternalizable {

   /**
    * Instantiates a new concept chronology impl.
    */
   private ConceptChronologyImpl() {
       this.versionType = VersionType.CONCEPT;
   }

   /**
    * Instantiates a new concept chronology impl.
    *
    * @param primordialUuid the primordial uuid
    * @param assemblageNid the container sequence
    */
   public ConceptChronologyImpl(UUID primordialUuid, int assemblageNid) {
      super(primordialUuid, assemblageNid, VersionType.CONCEPT);
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
      for (SemanticChronology descriptionChronology: Get.assemblageService()
              .getDescriptionsForComponent(getNid())) {
         for (Version version: descriptionChronology.getVersionList()) {
            if (((DescriptionVersion) version).getText().equals(descriptionText)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Contains description.
    *
    * @param descriptionText the description text
    * @param stampFilter the stamp coordinate
    * @return true, if successful
    */
   @Override
   public boolean containsDescription(String descriptionText, StampFilter stampFilter) {
      for (LatestVersion<DescriptionVersion> descVersion: Get.assemblageService()
                .getSnapshot(DescriptionVersion.class, stampFilter)
                .getLatestDescriptionVersionsForComponent(getNid())) {
         if (descVersion.isPresent() && descVersion.get().getText().equals(descriptionText)) {
            return true;
         }
      }
      return false;
   }
   
   @Override
   public ConceptVersionImpl createMutableVersion(int stampSequence) {
      final ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence);
      addVersion(newVersion);
      return newVersion;
   }

   @Override
   public ConceptVersionImpl createMutableVersion(Transaction transaction, int stampSequence) {
      final ConceptVersionImpl newVersion = new ConceptVersionImpl(this, stampSequence);
      transaction.addVersionToTransaction(newVersion);
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
      if (data.getUsed() == 0) {
          throw new IllegalStateException();
      }
       
      if (IsaacObjectType.CONCEPT.getDataFormatVersion() != data.getObjectDataFormatVersion()) {
         throw new UnsupportedOperationException("Data format version not supported: " + data.getObjectDataFormatVersion());
      }
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
      builder.append(getNid());
      builder.append(">");
      builder.append(getUuidList());
      builder.append(" \n");
      for (Version v: getVersionList()) {
         builder.append("   ");
         builder.append(v);
         builder.append("\n");
      }
      return builder.toString();
   }
   
   @Override
   public String toLongString() {
      final StringBuilder builder = new StringBuilder();

      builder.append("ConceptChronologyImpl{");
      builder.append(toUserString());
      builder.append(" <");
      builder.append(getNid());
      builder.append("> ");
      builder.append(getUuidList());
      builder.append(" \n");

      for (Version v: getVersionList()) {
         builder.append("   ");
         builder.append(v);
         builder.append("\n");
      }
      
      
      builder.append("\nTaxonomy record: \n");
      builder.append(ModelGet.taxonomyDebugService().describeTaxonomyRecord(this.getNid()));
      builder.append("\n\n");
      toString(builder, true);
      return builder.toString();
   }

   /**
    * To user string.
    *
    * @return the string
    */
   @Override
   public String toUserString() {
      final List<SemanticChronology> descList = getConceptDescriptionList();

      if (descList.isEmpty()) {
         return "no description for concept: " + getUuidList() + " " + getNid();
      }

      return ((DescriptionVersion) descList.get(0)
            .getVersionList()
            .get(0)).getText();
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
      return new ConceptVersionImpl(this, stampSequence);
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

   //~--- set methods ---------------------------------------------------------

   /**
    * Gets the additional chronicle fields.
    *
    * @param in the in
    */
   @Override
   protected void setAdditionalChronicleFieldsFromBuffer(ByteArrayDataBuffer in) {
      // nothing to read for ConceptChronology...
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept description list.
    *
    * @return the concept description list
    */
   @Override
   public List<SemanticChronology> getConceptDescriptionList() {
      if (Get.assemblageServiceAvailable()) {
         return Get.assemblageService()
                   .getDescriptionsForComponent(getNid());
      } else {
         return new ArrayList<>();
      }
   }

   /**
    * {@inheritDoc}
    * Note, that despite the doc for this method, this may return a preferred term, if no FQN is found, or a description 
    * that specifies "unknown..." if nothing at all is found.
    */
   @Override
   public String getFullyQualifiedName() {
      return Get.conceptDescriptionText(getNid());
   }

   /**
    * Gets the fully specified description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampFilter the stamp coordinate
    * @return the fully specified description
    */
   @Override
   public LatestVersion<DescriptionVersion> getFullyQualifiedNameDescription(LanguageCoordinate languageCoordinate,
                                                                             StampFilter stampFilter) {
      return languageCoordinate.getFullyQualifiedDescription(getConceptDescriptionList(), stampFilter);
   }

   /**
    * Gets the logical definition.
    *
    * @param stampFilter the stamp coordinate
    * @param premiseType the premise type
    * @param logicCoordinate the logic coordinate
    * @return the logical definition
    */
   @Override
   public LatestVersion<LogicGraphVersion> getLogicalDefinition(StampFilter stampFilter,
                                                                PremiseType premiseType,
                                                                LogicCoordinate logicCoordinate) {
      int assemblageSequence;

      if (premiseType == PremiseType.INFERRED) {
         assemblageSequence = logicCoordinate.getInferredAssemblageNid();
      } else {
         assemblageSequence = logicCoordinate.getStatedAssemblageNid();
      }
      List<LatestVersion<LogicGraphVersion>> latestVersionList = Get.assemblageService()
                .getSnapshot(LogicGraphVersion.class, stampFilter)
                .getLatestSemanticVersionsForComponentFromAssemblage(getNid(), assemblageSequence);
      if (latestVersionList.isEmpty()) {
         return new LatestVersion<>();
      }
      return Get.assemblageService()
                .getSnapshot(LogicGraphVersion.class, stampFilter)
                .getLatestSemanticVersionsForComponentFromAssemblage(getNid(), assemblageSequence).get(0);
   }

   /**
    * Gets the logical definition chronology report.
    *
    * @param stampFilter the stamp coordinate
    * @param premiseType the premise type
    * @param logicCoordinate the logic coordinate
    * @return the logical definition chronology report
    */
   @Override
   public String getLogicalDefinitionChronologyReport(StampFilter stampFilter,
                                                      PremiseType premiseType,
                                                      LogicCoordinate logicCoordinate) {
      int assemblageSequence;

      if (premiseType == PremiseType.INFERRED) {
         assemblageSequence = logicCoordinate.getInferredAssemblageNid();
      } else {
         assemblageSequence = logicCoordinate.getStatedAssemblageNid();
      }

      final Optional<SemanticChronology> definitionChronologyOptional = Get.assemblageService()
                                                                         .getSemanticChronologyStreamForComponentFromAssemblage(
                                                                               getNid(),
                                                                                     assemblageSequence)
                                                                         .findFirst();

      if (definitionChronologyOptional.isPresent()) {
         
         final List<LogicGraphVersionImpl> versions =
            definitionChronologyOptional.get().getVisibleOrderedVersionList(stampFilter);

//       Collection<LogicGraphSemanticImpl> versionsList = new ArrayList<>();
//       for (LogicGraphVersionImpl lgs : definitionChronologyOptional.get().getVisibleOrderedVersionList(stampCoordinate)) {
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

         for (final LogicGraphVersionImpl lgmv: versions) {
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
                builder.append(lg.findIsomorphisms(previousVersion));
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
   public IsaacObjectType getIsaacObjectType() {
      return IsaacObjectType.CONCEPT;
   }

   @Override
   public Optional<String> getRegularName() {
      return Optional.ofNullable(Get.defaultCoordinate()
                                    .getPreferredDescriptionText(this.getNid()));
   }

   /**
    * Gets the preferred description.
    *
    * @param languageCoordinate the language coordinate
    * @param stampFilter the stamp coordinate
    * @return the preferred description
    */
   @Override
   public LatestVersion<DescriptionVersion> getPreferredDescription(LanguageCoordinate languageCoordinate,
                                                                    StampFilter stampFilter) {
      return languageCoordinate.getRegularDescription(getConceptDescriptionList(), stampFilter);
   }
}

