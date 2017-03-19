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



package sh.isaac.utility.export;

//~--- JDK imports ------------------------------------------------------------

import java.io.OutputStream;

import java.text.SimpleDateFormat;

import java.time.Year;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.va.med.term.vhat.xml.model.ActionType;
import gov.va.med.term.vhat.xml.model.DesignationType;
import gov.va.med.term.vhat.xml.model.KindType;
import gov.va.med.term.vhat.xml.model.PropertyType;
import gov.va.med.term.vhat.xml.model.Terminology;
import gov.va.med.term.vhat.xml.model.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation
   .SubsetMemberships.SubsetMembership;
import gov.va.med.term.vhat.xml.model.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships
   .Relationship;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.StringSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.provider.query.associations.AssociationInstance;
import sh.isaac.provider.query.associations.AssociationUtilities;
import sh.isaac.utility.Frills;

//~--- classes ----------------------------------------------------------------

/**
 * The Class VetsExporter.
 */
public class VetsExporter {
   /** The log. */
   private final Logger log = LogManager.getLogger();

   /** The designation types. */
   private final Map<UUID, String> designationTypes = new HashMap<>();

   /** The property types. */
   private final Map<UUID, String> propertyTypes = new HashMap<>();

   /** The relationship types. */
   private final Map<UUID, String> relationshipTypes = new HashMap<>();

   /** The assemblages map. */
   private final Map<UUID, String> assemblagesMap = new HashMap<>();

   /** The subset map. */
   private final Map<String, Long> subsetMap = new HashMap<>();

   /** The stamp coordinates. */
   private StampCoordinate STAMP_COORDINATES = null;

   /** The ts. */
   TaxonomyService ts = Get.taxonomyService();

   // TODO: Source all the following hardcoded UUID values from MetaData, once available
   // ConceptChronology: VHAT Attribute Types <261> uuid:8287530a-b6b0-594d-bf46-252e09434f7e

   /** The vhat property types UUID. */
   // VHAT Metadata -> "Attribute Types"
   final UUID vhatPropertyTypesUUID = UUID.fromString("8287530a-b6b0-594d-bf46-252e09434f7e");

   /** The vhat property types nid. */
   final int vhatPropertyTypesNid = Get.identifierService()
                                       .getNidForUuids(this.vhatPropertyTypesUUID);

   // ConceptChronology: Refsets (ISAAC) <325> uuid:fab80263-6dae-523c-b604-c69e450d8c7f

   /** The vhat refset types UUID. */
   // VHAT Metadata -> "Refsets"
   final UUID vhatRefsetTypesUUID = UUID.fromString("fab80263-6dae-523c-b604-c69e450d8c7f");

   /** The vhat refset types nid. */
   final int vhatRefsetTypesNid = Get.identifierService()
                                     .getNidForUuids(this.vhatRefsetTypesUUID);

   /** The code assemblage UUID. */

   // conceptChronology: CODE (ISAAC) <77> uuid:803af596-aea8-5184-b8e1-45f801585d17
   final UUID codeAssemblageUUID = MetaData.CODE.getPrimordialUuid();

   /** The code assemblage concept seq. */
   final int codeAssemblageConceptSeq = Get.identifierService()
                                           .getConceptSequenceForUuids(this.codeAssemblageUUID);

   // ConceptChronology: VHAT <1129> uuid:6e60d7fd-3729-5dd3-9ce7-6d97c8f75447

   /** The vhat code system UUID. */
   // VHAT CodeSystem
   final UUID vhatCodeSystemUUID = UUID.fromString("6e60d7fd-3729-5dd3-9ce7-6d97c8f75447");

   /** The vhat code system nid. */
   final int vhatCodeSystemNid = Get.identifierService()
                                    .getNidForUuids(this.vhatCodeSystemUUID);

   // ConceptChronology: Preferred Name (ISAAC) <257> uuid:a20e5175-6257-516a-a97d-d7f9655916b8

   /** The preferred name extended type. */
   // VHAT Description Types -> Preferred Name
   final UUID preferredNameExtendedType = UUID.fromString("a20e5175-6257-516a-a97d-d7f9655916b8");

   // ConceptChronology: Association Types (ISAAC) <309> uuid:55f56c52-757a-5db8-bf1e-3ed613711386

   /** The vhat association types UUID. */
   // ISAAC Associations => RelationshipType UUID
   final UUID vhatAssociationTypesUUID = UUID.fromString("55f56c52-757a-5db8-bf1e-3ed613711386");

   // ConceptChronology: Description Types (ISAAC) <254> uuid:09c43aa9-eaed-5217-bc5f-23cacca4df38

   /** The vhat designation types UUID. */
   // ISAAC Descriptions => DesignationType UUID
   final UUID vhatDesignationTypesUUID = UUID.fromString("09c43aa9-eaed-5217-bc5f-23cacca4df38");

   /** The vhat all concepts UUID. */

   // ConceptChronology: All VHAT Concepts (ISAAC) <365> uuid:f2df3cf5-a426-50f9-a660-081a5ca22c70
   final UUID vhatAllConceptsUUID = UUID.fromString("f2df3cf5-a426-50f9-a660-081a5ca22c70");

   /** The missing SDO code systems UUID. */

   // ConceptChronology: Missing SDO Code System Concepts <42268> uuid:52460eeb-1388-512d-a5e4-fddd64fe0aee
   final UUID missingSDOCodeSystemsUUID = UUID.fromString("52460eeb-1388-512d-a5e4-fddd64fe0aee");

   /** The full export mode. */
   boolean fullExportMode = false;

   /** The terminology. */
   private Terminology terminology;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new vets exporter.
    */
   public VetsExporter() {}

   //~--- methods -------------------------------------------------------------

   /**
    * Export.
    *
    * @param writeTo the output stream object handling the export
    * @param startDate only export concepts modified on or after this date.  Set to 0, if you want to start from the beginning
    * @param endDate only export concepts where their most recent modified date is on or before this date.  Set to Long.MAX_VALUE to get everything.
    * @param fullExportMode if true, exports all content present at the end of the date range, ignoring start date.  All actions are set to add.
    *   If false - delta mode - calculates the action based on the start and end dates, and includes only the minimum required elements in the xml file.
    */
   public void export(OutputStream writeTo, long startDate, long endDate, boolean fullExportMode) {
      this.fullExportMode = fullExportMode;
      this.STAMP_COORDINATES = new StampCoordinateImpl(StampPrecedence.PATH,
            new StampPositionImpl(endDate, MetaData.DEVELOPMENT_PATH.getConceptSequence()),
            ConceptSequenceSet.EMPTY,
            State.ANY_STATE_SET);

      // Build Assemblages map
      Get.sememeService().getAssemblageTypes().forEach((assemblageSeqId) -> {
                     this.assemblagesMap.put(Get.conceptSpecification(assemblageSeqId)
                           .getPrimordialUuid(),
                           Get.conceptSpecification(assemblageSeqId)
                              .getConceptDescriptionText());
                  });

      // XML object
      this.terminology = new Terminology();

      // Types
      this.terminology.setTypes(new Terminology.Types());

      Terminology.Types.Type xmlType;

      // Subsets/Refsets
      this.terminology.setSubsets(new Terminology.Subsets());

      // CodeSystem
      final Terminology.CodeSystem         xmlCodeSystem = new Terminology.CodeSystem();
      final Terminology.CodeSystem.Version xmlVersion    = new Terminology.CodeSystem.Version();
      final Terminology.CodeSystem.Version.CodedConcepts xmlCodedConcepts =
         new Terminology.CodeSystem.Version.CodedConcepts();

      // Add to map
      Get.taxonomyService().getAllRelationshipOriginSequences(Get.identifierService()
            .getNidForUuids(this.vhatAssociationTypesUUID)).forEach((conceptId) -> {
                     final ConceptChronology<? extends ConceptVersion<?>> concept = Get.conceptService()
                                                                                       .getConcept(conceptId);

                     this.relationshipTypes.put(concept.getPrimordialUuid(),
                           getPreferredNameDescriptionType(concept.getNid()));
                  });

      if (fullExportMode) {
         // Build XML
         for (final String s: this.relationshipTypes.values()) {
            xmlType = new Terminology.Types.Type();
            xmlType.setKind(KindType.RELATIONSHIP_TYPE);
            xmlType.setName(s);
            this.terminology.getTypes()
                            .getType()
                            .add(xmlType);
         }
      }

      // Add to map
      Get.taxonomyService().getAllRelationshipOriginSequences(Get.identifierService()
            .getNidForUuids(this.vhatPropertyTypesUUID)).forEach((conceptId) -> {
                     final ConceptChronology<? extends ConceptVersion<?>> concept = Get.conceptService()
                                                                                       .getConcept(conceptId);

                     this.propertyTypes.put(concept.getPrimordialUuid(),
                                            getPreferredNameDescriptionType(concept.getNid()));
                  });

      if (fullExportMode) {
         // Build XML
         for (final String s: this.propertyTypes.values()) {
            xmlType = new Terminology.Types.Type();
            xmlType.setKind(KindType.PROPERTY_TYPE);
            xmlType.setName(s);
            this.terminology.getTypes()
                            .getType()
                            .add(xmlType);
         }
      }

      // Add to map
      Get.taxonomyService().getAllRelationshipOriginSequences(Get.identifierService()
            .getNidForUuids(this.vhatDesignationTypesUUID)).forEach((conceptId) -> {
                     final ConceptChronology<? extends ConceptVersion<?>> concept = Get.conceptService()
                                                                                       .getConcept(conceptId);

                     this.designationTypes.put(concept.getPrimordialUuid(),
                           getPreferredNameDescriptionType(concept.getNid()));
                  });

      if (fullExportMode) {
         // Build XML
         for (final String s: this.designationTypes.values()) {
            xmlType = new Terminology.Types.Type();
            xmlType.setKind(KindType.DESIGNATION_TYPE);
            xmlType.setName(s);
            this.terminology.getTypes()
                            .getType()
                            .add(xmlType);
         }
      }

      // Get data, Add to map
      Get.taxonomyService().getAllRelationshipOriginSequences(Get.identifierService()
            .getNidForUuids(this.vhatRefsetTypesUUID)).forEach((tcs) -> {
                     final ConceptChronology<? extends ConceptVersion<?>> concept = Get.conceptService()
                                                                                       .getConcept(tcs);

                     // Excluding these:
                     if (concept.getPrimordialUuid().equals(this.vhatAllConceptsUUID) ||
                         concept.getPrimordialUuid().equals(this.missingSDOCodeSystemsUUID) ||
                         Frills.definesMapping(concept.getConceptSequence())) {
                        // Skip
                     } else {
                        final Terminology.Subsets.Subset xmlSubset = new Terminology.Subsets.Subset();

                        xmlSubset.setAction(determineAction(concept, startDate, endDate));
                        xmlSubset.setName(getPreferredNameDescriptionType(concept.getNid()));
                        xmlSubset.setActive(concept.isLatestVersionActive(this.STAMP_COORDINATES));

                        // read VUID
                        xmlSubset.setVUID(Frills.getVuId(concept.getNid(), this.STAMP_COORDINATES)
                              .orElse(null));

                        if (xmlSubset.getVUID() == null) {
                           this.log.warn("Failed to find VUID for subset concept " + concept.getPrimordialUuid());
                        }

                        if (xmlSubset.getAction() != ActionType.NONE) {
                           this.terminology.getSubsets()
                                           .getSubset()
                                           .add(xmlSubset);
                        }

                        this.subsetMap.put(xmlSubset.getName(), xmlSubset.getVUID());
                     }
                  });

      final ConceptChronology<? extends ConceptVersion<?>> vhatConcept = Get.conceptService()
                                                                            .getConcept(this.vhatCodeSystemNid);

      xmlCodeSystem.setAction(ActionType.NONE);
      xmlCodeSystem.setName(getPreferredNameDescriptionType(vhatConcept.getNid()));
      xmlCodeSystem.setVUID(Frills.getVuId(this.vhatCodeSystemNid, null)
                                  .orElse(null));
      xmlCodeSystem.setDescription(
          "VHA Terminology");  // This is in an acceptable synonym, but easier to hard code at the moment...
      xmlCodeSystem.setCopyright(Year.now()
                                     .getValue() + "");
      xmlCodeSystem.setCopyrightURL("");
      xmlCodeSystem.setPreferredDesignationType("Preferred Name");
      xmlVersion.setAppend(Boolean.TRUE);
      xmlVersion.setName("Authoring Version");
      xmlVersion.setDescription("Delta output from ISAAC");

      try {
         final SimpleDateFormat     sdf           = new SimpleDateFormat("yyyy-MM-dd");
         final String               formattedDate = sdf.format(System.currentTimeMillis());
         final XMLGregorianCalendar _xmlEffDate   = DatatypeFactory.newInstance()
                                                                   .newXMLGregorianCalendar(formattedDate);
         final XMLGregorianCalendar _xmlRelDate   = DatatypeFactory.newInstance()
                                                                   .newXMLGregorianCalendar(formattedDate);

         xmlVersion.setEffectiveDate(_xmlEffDate);
         xmlVersion.setReleaseDate(_xmlRelDate);
      } catch (final Exception pe) {
         this.log.error("Misconfiguration of date parser!", pe);
      }

      xmlVersion.setSource("");

      final AtomicInteger                                       skippedForNonVHAT    = new AtomicInteger();
      final AtomicInteger                                       skippedDateRange     = new AtomicInteger();
      final AtomicInteger                                       observedVhatConcepts = new AtomicInteger();
      final AtomicInteger                                       exportedVhatConcepts = new AtomicInteger();
      final List<Terminology.CodeSystem.Version.MapSets.MapSet> xmlMapSetCollection  = new ArrayList<>();

      Get.conceptService().getConceptChronologyStream().forEach((concept) -> {
                     if (fullExportMode) {
                        Get.sememeService()
                           .getSememesForComponentFromAssemblage(concept.getConceptSequence(),
                                 IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE
                                       .getConceptSequence())
                           .forEach(mappingSememe -> {
                                       final Terminology.CodeSystem.Version.MapSets.MapSet xmlMapSet =
                                          new Terminology.CodeSystem.Version.MapSets.MapSet();

                                       xmlMapSet.setAction(determineAction(concept, startDate, endDate));
                                       xmlMapSet.setActive(concept.isLatestVersionActive(this.STAMP_COORDINATES));
                                       xmlMapSet.setCode(getCodeFromNid(concept.getNid()));
                                       xmlMapSet.setName(getPreferredNameDescriptionType(concept.getNid()));
                                       xmlMapSet.setVUID(Frills.getVuId(concept.getNid(), this.STAMP_COORDINATES)
                                             .orElse(null));

                                       // Source and Target CodeSystem
                                       @SuppressWarnings({ "unchecked", "rawtypes" })
                                       final Optional<LatestVersion<? extends DynamicSememe>> mappingSememeVersion =
                                          ((SememeChronology) mappingSememe).getLatestVersion(DynamicSememe.class,
                                                                                              this.STAMP_COORDINATES);

                                       if (mappingSememeVersion.isPresent()) {
                                          // Get referenced component for the MapSet values
                                          final ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService()
                                                                                                       .getConcept(
                                                                                                          mappingSememeVersion.get()
                                                                                                                .value()
                                                                                                                .getReferencedComponentNid());
                                          @SuppressWarnings({ "rawtypes", "unchecked" })
                                          final Optional<LatestVersion<ConceptVersion<?>>> cv =
                                             ((ConceptChronology) cc).getLatestVersion(ConceptVersion.class,
                                                                                       this.STAMP_COORDINATES);

                                          if (cv.isPresent()) {
                                             Get.sememeService()
                                                .getSememesForComponentFromAssemblage(cv.get()
                                                      .value()
                                                      .getChronology()
                                                      .getNid(),
                                                      IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION
                                                            .getConceptSequence())
                                                .forEach(mappingStrExt -> {
                              @SuppressWarnings({ "unchecked", "rawtypes" })
                              final Optional<LatestVersion<? extends DynamicSememe>> mappingStrExtVersion =
                                 ((SememeChronology) mappingStrExt).getLatestVersion(
                                     DynamicSememe.class, this.STAMP_COORDINATES);

                              // TODO:DA review
                              if (mappingStrExtVersion.isPresent()) {
                                 final DynamicSememeData dsd[] = mappingStrExtVersion.get()
                                                                                     .value()
                                                                                     .getData();

                                 if (dsd.length == 2) {
                                    if (dsd[0].getDataObject()
                                              .equals(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM
                                                    .getNid())) {
                                       xmlMapSet.setSourceCodeSystem(dsd[1].getDataObject()
                                             .toString());
                                    } else if (dsd[0].getDataObject()
                                                     .equals(
                                                         IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM_VERSION
                                                               .getNid())) {
                                       xmlMapSet.setSourceVersionName(dsd[1].getDataObject()
                                             .toString());
                                    } else if (dsd[0].getDataObject()
                                                     .equals(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM
                                                           .getNid())) {
                                       xmlMapSet.setTargetCodeSystem(dsd[1].getDataObject()
                                             .toString());
                                    } else if (dsd[0].getDataObject()
                                                     .equals(
                                                         IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM_VERSION
                                                               .getNid())) {
                                       xmlMapSet.setTargetVersionName(dsd[1].getDataObject()
                                             .toString());
                                    }
                                 }
                              }
                           });

                                             // MapEntries
                                             // MapEntry->Properties
                                             // TODO: MapEntry->Designations (currently ignored - none found in import XML, importer doesn't implement)
                                             // TODO: MapEntry->Relationships (currently ignored - none found in import XML, importer doesn't implement)
                                             // TODO:DA review - not using MapEntryType as it doesn't allow for .setProperties(), needed for GEM_Flags
                                             final Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries xmlMapEntries =
                                                new Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries();

                                             for (
                                             final Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry me:
                                                readMapEntryTypes(cv.get()
                                                      .value()
                                                      .getChronology()
                                                      .getNid(),
                                                      startDate,
                                                      endDate)) {
                                                xmlMapEntries.getMapEntry()
                                                      .add(me);
                                             }

                                             xmlMapSet.setMapEntries(xmlMapEntries);
                                          }
                                       }

                                       // Designations
                                       final Terminology.CodeSystem.Version.MapSets.MapSet.Designations xmlMapSetDesignations =
                                          new Terminology.CodeSystem.Version.MapSets.MapSet.Designations();

                                       for (final DesignationType d: getDesignations(concept,
                                             startDate,
                                             endDate,
                                             (() -> new Terminology.CodeSystem.Version.MapSets.MapSet.Designations.Designation()))) {
                                          // MapSets contain a phantom description with no typeName, code or VUID - need to keep those out
                                          // There's probably a more appropriate way to do this - quick and dirty for now
                                          // TODO:DA review
                                          if (!(((d.getTypeName() == null) || d.getTypeName().isEmpty()) &&
                                                ((d.getCode() == null) || d.getCode().isEmpty()) &&
                                                (d.getVUID() == null))) {
                                             xmlMapSetDesignations.getDesignation()
                                                   .add(
                                                   (Terminology.CodeSystem.Version.MapSets.MapSet.Designations.Designation) d);
                                          }
                                       }

                                       xmlMapSet.setDesignations(xmlMapSetDesignations);

                                       // Properties
                                       final Terminology.CodeSystem.Version.MapSets.MapSet.Properties xmlMapSetProperties =
                                          new Terminology.CodeSystem.Version.MapSets.MapSet.Properties();

                                       for (final PropertyType pt: readPropertyTypes(concept.getNid(),
                                             startDate,
                                             endDate,
                                             () -> new Terminology.CodeSystem.Version.MapSets.MapSet.Properties.Property())) {
                                          xmlMapSetProperties.getProperty()
                                                .add(
                                                (Terminology.CodeSystem.Version.MapSets.MapSet.Properties.Property) pt);
                                       }

                                       xmlMapSet.setProperties(xmlMapSetProperties);

                                       if ((xmlMapSet.getAction() != ActionType.NONE) ||
                                           ((xmlMapSet.getMapEntries() != null) &&
                                            (xmlMapSet.getMapEntries().getMapEntry().size() > 0))) {
                                          xmlMapSetCollection.add(xmlMapSet);
                                       }
                                    });
                     }

                     if (!this.ts.wasEverKindOf(concept.getConceptSequence(), this.vhatCodeSystemNid)) {
                        // Needed to ignore all the dynamically created/non-imported concepts
                        skippedForNonVHAT.getAndIncrement();
                     } else if (concept.getNid() == this.vhatCodeSystemNid) {
                        // skip
                     } else {
                        observedVhatConcepts.getAndIncrement();

                        final int conceptNid = concept.getNid();

                        if (!wasConceptOrNestedValueModifiedInDateRange(concept, startDate)) {
                           skippedDateRange.getAndIncrement();
                        } else {
                           exportedVhatConcepts.getAndIncrement();

                           final Terminology.CodeSystem.Version.CodedConcepts.CodedConcept xmlCodedConcept =
                              new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept();

                           xmlCodedConcept.setAction(determineAction(concept, startDate, endDate));
                           xmlCodedConcept.setName(getPreferredNameDescriptionType(conceptNid));
                           xmlCodedConcept.setVUID(Frills.getVuId(conceptNid, null)
                                 .orElse(null));
                           xmlCodedConcept.setCode(getCodeFromNid(conceptNid));
                           xmlCodedConcept.setActive(
                               Boolean.valueOf(concept.isLatestVersionActive(this.STAMP_COORDINATES)));

                           final Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations xmlDesignations =
                              new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations();
                           final Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties xmlProperties =
                              new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties();

                           for (final DesignationType d: getDesignations(concept,
                                 startDate,
                                 endDate,
                                 () -> new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation())) {
                              xmlDesignations.getDesignation()
                                             .add(
                                             (Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation) d);
                           }

                           for (final PropertyType pt: readPropertyTypes(concept.getNid(),
                                 startDate,
                                 endDate,
                                 () -> new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties.Property())) {
                              xmlProperties.getProperty()
                                           .add(
                                           (Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties.Property) pt);
                           }

                           // Relationships
                           final Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships xmlRelationships =
                              new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships();

                           for (final Relationship rel: getRelationships(concept, startDate, endDate)) {
                              xmlRelationships.getRelationship()
                                              .add(rel);
                           }

                           // Try to keep XML output somewhat clean, without empty elements (i.e. <Element/> or <Element></Element>
                           if (xmlDesignations.getDesignation()
                                              .size() > 0) {
                              xmlCodedConcept.setDesignations(xmlDesignations);
                           }

                           if (xmlProperties.getProperty()
                                            .size() > 0) {
                              xmlCodedConcept.setProperties(xmlProperties);
                           }

                           if (xmlRelationships.getRelationship()
                                               .size() > 0) {
                              xmlCodedConcept.setRelationships(xmlRelationships);
                           }

                           // Add all CodedConcept elements
                           xmlCodedConcepts.getCodedConcept()
                                           .add(xmlCodedConcept);
                        }
                     }
                  });

      // Close out XML
      xmlVersion.setCodedConcepts(xmlCodedConcepts);

      // MapSets
      if (xmlMapSetCollection.size() > 0) {
         final Terminology.CodeSystem.Version.MapSets xmlMapSets = new Terminology.CodeSystem.Version.MapSets();

         xmlMapSets.getMapSet()
                   .addAll(xmlMapSetCollection);
         xmlVersion.setMapSets(xmlMapSets);
      }

      xmlCodeSystem.setVersion(xmlVersion);
      this.terminology.setCodeSystem(xmlCodeSystem);
      this.log.info("Skipped " + skippedForNonVHAT.get() + " concepts for non-vhat");
      this.log.info("Skipped " + skippedDateRange.get() + " concepts for outside date range");
      this.log.info("Processed " + observedVhatConcepts.get() + " concepts");
      this.log.info("Exported " + exportedVhatConcepts.get() + " concepts");
      writeXml(writeTo);
   }

   /**
    * Builds the property.
    *
    * @param sememe the sememe
    * @param startDate the start date
    * @param endDate the end date
    * @param constructor the constructor
    * @return A PropertyType object for the property, or null
    */
   private PropertyType buildProperty(SememeChronology<?> sememe,
                                      long startDate,
                                      long endDate,
                                      Supplier<PropertyType> constructor) {
      String  newValue = null;
      String  oldValue = null;
      boolean isActive = false;

      if (sememe.getSememeType() == SememeType.DYNAMIC) {
         @SuppressWarnings({ "unchecked", "rawtypes" })
         final Optional<LatestVersion<? extends DynamicSememe>> sememeVersion =
            ((SememeChronology) sememe).getLatestVersion(DynamicSememe.class,
                                                         this.STAMP_COORDINATES);

         if (sememeVersion.isPresent() &&
               (sememeVersion.get().value().getData() != null) &&
               (sememeVersion.get().value().getData().length > 0)) {
            newValue = (sememeVersion.get()
                                     .value()
                                     .getData()[0] == null) ? null
                  : sememeVersion.get()
                                 .value()
                                 .getData()[0]
                                 .dataToString();

            @SuppressWarnings({ "unchecked", "rawtypes" })
            final List<DynamicSememe<?>> coll =
               ((SememeChronology) sememe).getVisibleOrderedVersionList(this.STAMP_COORDINATES);

            Collections.reverse(coll);

            for (final DynamicSememe<?> s: coll) {
               if (s.getTime() < startDate) {
                  oldValue = (s.getData()[0] != null) ? s.getData()[0]
                        .dataToString()
                        : null;
                  break;
               }
            }

            isActive = sememeVersion.get()
                                    .value()
                                    .getState() == State.ACTIVE;
         }
      } else if (sememe.getSememeType() == SememeType.STRING) {
         @SuppressWarnings({ "unchecked", "rawtypes" })
         final Optional<LatestVersion<? extends StringSememe>> sememeVersion =
            ((SememeChronology) sememe).getLatestVersion(StringSememe.class,
                                                         this.STAMP_COORDINATES);

         if (sememeVersion.isPresent()) {
            newValue = sememeVersion.get()
                                    .value()
                                    .getString();

            @SuppressWarnings({ "unchecked", "rawtypes" })
            final List<StringSememe<?>> coll =
               ((SememeChronology) sememe).getVisibleOrderedVersionList(this.STAMP_COORDINATES);

            Collections.reverse(coll);

            for (final StringSememe<?> s: coll) {
               if (s.getTime() < startDate) {
                  oldValue = s.getString();
                  break;
               }
            }

            isActive = sememeVersion.get()
                                    .value()
                                    .getState() == State.ACTIVE;
         }
      } else {
         this.log.warn("Unexpectedly passed sememe " + sememe + " when we only expected a dynamic or a string type");
         return null;
      }

      if (newValue == null) {
         return null;
      }

      final PropertyType property = (constructor == null) ? new gov.va.med.term.vhat.xml.model.PropertyType()
            : constructor.get();

      property.setAction(determineAction(sememe, startDate, endDate));

      if (isActive && (property.getAction() == ActionType.NONE) && newValue.equals(oldValue)) {
         return null;
      } else if (!newValue.equals(oldValue) && (property.getAction() == ActionType.NONE)) {
         // change action to update if the new and old values are not the same.
         property.setAction(ActionType.UPDATE);
      }

      // got to here, there is change.
      property.setActive(isActive);

      if (((property.getAction() == ActionType.UPDATE) || (property.getAction() == ActionType.ADD)) &&
            !newValue.equals(oldValue)) {
         property.setValueNew(newValue);
      }

      if ((oldValue != null) && (property.getAction() != ActionType.ADD)) {
         property.setValueOld(oldValue);
      }

      if (property.getAction() == ActionType.NONE) {
         return null;
      }

      property.setTypeName(getPreferredNameDescriptionType(Get.identifierService()
            .getConceptNid(sememe.getAssemblageSequence())));
      return property;
   }

   /**
    * Builds the subset membership.
    *
    * @param sememe the Chronicle object (concept) representing the Subset
    * @param startDate the start date
    * @param endDate the end date
    * @return the SubsetMembership object built for the sememe, or null
    */
   private SubsetMembership buildSubsetMembership(SememeChronology<?> sememe, long startDate, long endDate) {
      if (sememe.getSememeType() == SememeType.DYNAMIC) {
         final SubsetMembership subsetMembership = new SubsetMembership();

         subsetMembership.setActive(sememe.isLatestVersionActive(this.STAMP_COORDINATES));
         subsetMembership.setAction(determineAction(sememe, startDate, endDate));

         if (subsetMembership.getAction() == ActionType.NONE) {
            return null;
         }

         final long vuid = Frills.getVuId(Get.identifierService()
                                             .getConceptNid(sememe.getAssemblageSequence()),
                                          this.STAMP_COORDINATES)
                                 .orElse(0L)
                                 .longValue();

         if (vuid > 0) {
            subsetMembership.setVUID(vuid);
         } else {
            this.log.warn("No VUID found for Subset UUID: " + sememe.getPrimordialUuid());
         }

         return subsetMembership;
      } else {
         this.log.error("Unexpected sememe type! " + sememe);
         return null;
      }
   }

   /**
    * Determine action.
    *
    * @param object the object
    * @param startDate the start date
    * @param endDate the end date
    * @return the ActionType object representing the change
    */
   private ActionType determineAction(ObjectChronology<? extends StampedVersion> object, long startDate, long endDate) {
      ActionType action = ActionType.ADD;

      if (!this.fullExportMode) {
         final List<? extends StampedVersion> versions = object.getVersionList();

         versions.sort((o1, o2) -> -1 * Long.compare(o1.getTime(), o2.getTime()));

         boolean latest                      = true;
         int     versionCountInDateRange     = 0;
         int     actionCountPriorToStartDate = 0;
         State   beginState                  = null;
         State   endState                    = null;

         for (final StampedVersion sv: versions) {
            if (sv.getTime() < startDate) {
               // last value prior to start date
               if (beginState == null) {
                  beginState = sv.getState();
               }

               actionCountPriorToStartDate++;
            }

            if ((sv.getTime() <= endDate) && (sv.getTime() >= startDate)) {
               // last value prior to end date
               if (endState == null) {
                  endState = sv.getState();
               }

               versionCountInDateRange++;
            }

            latest = false;
         }

         if ((beginState == null) && (endState != null)) {
            action = ActionType.ADD;
         } else if ((beginState != null) && (endState == null)) {
            action = ActionType.NONE;
         } else {
            if (beginState != endState) {
               action = ActionType.UPDATE;
            } else {
               if (versionCountInDateRange == 0) {
                  action = ActionType.NONE;
               } else if (versionCountInDateRange > 0) {
                  action = ActionType.UPDATE;
               } else if ((actionCountPriorToStartDate > 0) && (versionCountInDateRange > 0)) {
                  return ActionType.UPDATE;
               }

               /*
                *  The UI does not allow Remove. Only Active and Inactive. May be revisited in R3
                * else if (finalStateIsInactive && actionCountPriorToStartDate > 0)
                * {
                *       return ActionType.REMOVE;
                * }
                */
            }
         }
      }

      return action;
   }

   /**
    * Read map entry types.
    *
    * @param componentNid the component nid
    * @param startDate the start date
    * @param endDate the end date
    * @return a List of the MapEntry objects for the MapSet item
    */
   private List<Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry> readMapEntryTypes(int componentNid,
         long startDate,
         long endDate) {
      final ArrayList<Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry> mes = new ArrayList<>();

      Get.sememeService().getSememesFromAssemblage(Get.identifierService()
                                      .getConceptSequence(componentNid)).forEach(sememe -> {
                     @SuppressWarnings({ "unchecked", "rawtypes" })
                     final Optional<LatestVersion<? extends DynamicSememe>> sememeVersion =
                        ((SememeChronology) sememe).getLatestVersion(DynamicSememe.class, this.STAMP_COORDINATES);

                     if (sememeVersion.isPresent() && (sememeVersion.get().value().getData() !=
                     null) && (sememeVersion.get().value().getData().length > 0)) {
                        try {
                           final Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry me =
                              new Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry();

                           me.setAction(
                           ActionType.ADD);  // TODO: There is currently no requirement or ability to deploy MapSet deltas, this if for the full export only
                           me.setVUID(Frills.getVuId(sememe.getNid(), this.STAMP_COORDINATES)
                                            .orElse(null));

                           String code = getCodeFromNid(sememeVersion.get()
                                                                     .value()
                                                                     .getReferencedComponentNid());

                           if (null == code) {
                              code = Frills.getDescription(sememeVersion.get()
                                    .value()
                                    .getReferencedComponentNid())
                                           .orElse("");
                           }

                           me.setSourceCode(code);

                           final boolean isActive = sememeVersion.get()
                                                                 .value()
                                                                 .getState() == State.ACTIVE;

                           me.setActive(isActive);

                           final DynamicSememeUtility ls = LookupService.get()
                                                                        .getService(DynamicSememeUtility.class);

                           if (ls == null) {
                              throw new RuntimeException(
                                  "An implementation of DynamicSememeUtility is not available on the classpath");
                           } else {
                              final DynamicSememeColumnInfo[] dsci =
                                 ls.readDynamicSememeUsageDescription(sememeVersion.get()
                                                                                   .value()
                                                                                   .getAssemblageSequence())
                                   .getColumnInfo();
                              final DynamicSememeData dsd[] = sememeVersion.get()
                                                                           .value()
                                                                           .getData();

                              for (final DynamicSememeColumnInfo d: dsci) {
                                 final UUID columnUUID = d.getColumnDescriptionConcept();
                                 final int  col        = d.getColumnOrder();

                                 if ((null != dsd[col]) && (null != columnUUID)) {
                                    if (columnUUID.equals(
                                        DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT
                                              .getPrimordialUuid())) {
                                       me.setTargetCode(Frills.getDescription(UUID.fromString(dsd[col].getDataObject()
                                             .toString()))
                                                              .orElse(""));
                                    } else if (
                                       columnUUID.equals(
                                           IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_EQUIVALENCE_TYPE
                                                 .getPrimordialUuid())) {
                                       // Currently ignored, no XML representation
                                    } else if (
                                       columnUUID.equals(
                                           IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_SEQUENCE
                                                 .getPrimordialUuid())) {
                                       me.setSequence(Integer.parseInt(dsd[col].getDataObject()
                                             .toString()));
                                    } else if (
                                       columnUUID.equals(
                                           IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_GROUPING
                                                 .getPrimordialUuid())) {
                                       me.setGrouping(dsd[col].getDataObject());
                                    } else if (
                                       columnUUID.equals(
                                           IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_EFFECTIVE_DATE
                                                 .getPrimordialUuid())) {
                                       final SimpleDateFormat sdf           = new SimpleDateFormat("yyyy-MM-dd");
                                       final String           formattedDate = sdf.format(dsd[col].getDataObject());

                                       me.setEffectiveDate(DatatypeFactory.newInstance()
                                             .newXMLGregorianCalendar(formattedDate));
                                    } else if (
                                       columnUUID.equals(
                                           IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_GEM_FLAGS
                                                 .getPrimordialUuid())) {
                                       final Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties.Property gem_prop =
                                          new Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties.Property();
                                       final Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties props =
                                          new Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties();

                                       gem_prop.setAction(ActionType.ADD);  // See 'TODO' above
                                       gem_prop.setActive(
                                       Boolean.TRUE);  // TODO: Defaulting to true as it appears in the import XML, not sure how to determine otherwise
                                       gem_prop.setTypeName("GEM_Flags");
                                       gem_prop.setValueNew(dsd[col].getDataObject()
                                             .toString());
                                       props.getProperty()
                                            .add(gem_prop);
                                       me.setProperties(props);
                                    } else {
                                       this.log.warn("No mapping match found for UUID: ", columnUUID);
                                    }
                                 }
                              }

                              mes.add(me);
                           }
                        } catch (final NumberFormatException nfe) {
                           this.log.error("Misconfiguration of integer parser!", nfe);
                        } catch (final IllegalArgumentException iae) {
                           this.log.error("Misconfiguration of date parser!", iae);
                        } catch (final NullPointerException npe) {
                           this.log.error("Misconfiguration of date parser!", npe);
                        } catch (final DatatypeConfigurationException dce) {
                           this.log.error("Misconfiguration of date parser!", dce);
                        } catch (final Exception e) {
                           this.log.error("General MapEntry failure!", e);
                        }
                     }
                  });
      return mes;
   }

   /**
    * Read property types.
    *
    * @param componentNid the component nid
    * @param startDate the start date
    * @param endDate the end date
    * @param constructor the constructor
    * @return a List of the PropertyType objects for the specific component
    */
   private List<PropertyType> readPropertyTypes(int componentNid,
         long startDate,
         long endDate,
         Supplier<PropertyType> constructor) {
      final ArrayList<PropertyType> pts = new ArrayList<>();

      Get.sememeService()
         .getSememesForComponent(componentNid)
         .forEach((sememe) -> {
         // skip code and vuid properties - they have special handling
                     if ((sememe.getAssemblageSequence() != MetaData.VUID.getConceptSequence()) &&
                         (sememe.getAssemblageSequence() != this.codeAssemblageConceptSeq) &&
                         this.ts.wasEverKindOf(sememe.getAssemblageSequence(), this.vhatPropertyTypesNid)) {
                        final PropertyType property = buildProperty(sememe, startDate, endDate, constructor);

                        if (property != null) {
                           pts.add(property);
                        }
                     }
                  });
      return pts;
   }

   /**
    * Scan through all (nested) components associated with this concept, and the concept itself, and see if the latest edit
    * date for any component is within our filter range.
    *
    * @param concept the concept
    * @param startDate the start date
    * @return true or false, if the concept or a nested value was modified within the date range
    */
   @SuppressWarnings("rawtypes")
   private boolean wasConceptOrNestedValueModifiedInDateRange(ConceptChronology concept, long startDate) {
      @SuppressWarnings("unchecked")
      final Optional<LatestVersion<ConceptVersion>> cv = concept.getLatestVersion(ConceptVersion.class,
                                                                                  this.STAMP_COORDINATES);

      if (cv.isPresent()) {
         if (cv.get()
               .value()
               .getTime() >= startDate) {
            return true;
         }
      }

      return hasSememeModifiedInDateRange(concept.getNid(), startDate);
   }

   /**
    * Write xml.
    *
    * @param writeTo the write to
    */
   private void writeXml(OutputStream writeTo) {
      try {
         final JAXBContext jaxbContext = JAXBContext.newInstance(Terminology.class);
         final Marshaller  marshaller  = jaxbContext.createMarshaller();

         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(this.terminology, writeTo);
      } catch (final Exception e) {
         this.log.error("Unexpected", e);
         throw new RuntimeException(e);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the code from nid.
    *
    * @param componentNid the component nid
    * @return the Code value found based on the Nid
    */
   private String getCodeFromNid(int componentNid) {
      final Optional<SememeChronology<? extends SememeVersion<?>>> sc = Get.sememeService()
                                                                           .getSememesForComponentFromAssemblage(
                                                                              componentNid,
                                                                                    this.codeAssemblageConceptSeq)
                                                                           .findFirst();

      if (sc.isPresent()) {
         // There was a bug in the older terminology loaders which loaded 'Code' as a static sememe, but marked it as a dynamic sememe.
         // So during edits, new entries would get saves as dynamic sememes, while old entries were static.  Handle either....
         if (sc.get()
               .getSememeType() == SememeType.STRING) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Optional<LatestVersion<StringSememe<?>>> sv =
               ((SememeChronology) sc.get()).getLatestVersion(StringSememe.class,
                                                              this.STAMP_COORDINATES);

            if (sv.isPresent()) {
               return sv.get()
                        .value()
                        .getString();
            }
         } else if (sc.get()
                      .getSememeType() == SememeType.DYNAMIC)  // this path will become dead code, after the data is fixed.
         {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Optional<LatestVersion<? extends DynamicSememe>> sv =
               ((SememeChronology) sc.get()).getLatestVersion(DynamicSememe.class,
                                                              this.STAMP_COORDINATES);

            if (sv.isPresent()) {
               if ((sv.get().value().getData() != null) && (sv.get().value().getData().length == 1)) {
                  return sv.get()
                           .value()
                           .getData()[0]
                           .dataToString();
               }
            }
         } else {
            this.log.error("Unexpected sememe type for 'Code' sememe on nid " + componentNid);
         }
      }

      return null;
   }

   /**
    * Gets the designations.
    *
    * @param concept the concept
    * @param startDate the start date
    * @param endDate the end date
    * @param constructor the constructor
    * @return a List of DesignationTypes for the concept
    */
   private List<DesignationType> getDesignations(ConceptChronology<?> concept,
         long startDate,
         long endDate,
         Supplier<DesignationType> constructor) {
      final List<DesignationType> designations = new ArrayList<>();

      Get.sememeService()
         .getSememesForComponent(concept.getNid())
         .forEach(sememe -> {
                     if (sememe.getSememeType() == SememeType.DESCRIPTION) {
                        boolean hasChild = false;
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        final Optional<LatestVersion<DescriptionSememe>> descriptionVersion =
                           ((SememeChronology) sememe).getLatestVersion(DescriptionSememe.class,
                                                                        this.STAMP_COORDINATES);

                        if (descriptionVersion.isPresent()) {
                           final DesignationType d = constructor.get();

                           d.setAction(determineAction(sememe, startDate, endDate));

                           if (d.getAction() != ActionType.ADD) {
                              @SuppressWarnings({ "unchecked", "rawtypes" })
                              final List<DescriptionSememe<?>> coll =
                                 ((SememeChronology) sememe).getVisibleOrderedVersionList(this.STAMP_COORDINATES);

                              Collections.reverse(coll);

                              for (final DescriptionSememe<?> s: coll) {
                                 if (s.getTime() < startDate) {
                                    d.setValueOld(s.getText());
                                    break;
                                 }
                              }
                           }

                           if ((d.getAction() == ActionType.UPDATE) || (d.getAction() == ActionType.ADD)) {
                              d.setValueNew(descriptionVersion.get()
                                    .value()
                                    .getText());
                           }

                           if ((d.getValueNew() != null) &&
                               (d.getValueOld() != null) &&
                               d.getValueNew().equals(d.getValueOld())) {
                              d.setValueOld(null);
                              d.setValueNew(null);
                           }

                           d.setCode(getCodeFromNid(sememe.getNid()));
                           d.setVUID(Frills.getVuId(sememe.getNid(), this.STAMP_COORDINATES)
                                           .orElse(null));
                           d.setActive(descriptionVersion.get()
                                 .value()
                                 .getState() == State.ACTIVE);

                           // Get the extended type
                           final Optional<UUID> descType =
                              Frills.getDescriptionExtendedTypeConcept(this.STAMP_COORDINATES,
                                                                       sememe.getNid());

                           if (descType.isPresent()) {
                              d.setTypeName(this.designationTypes.get(descType.get()));
                           } else {
                              this.log.warn("No extended description type present on description " +
                                            sememe.getPrimordialUuid() + " " +
                                            descriptionVersion.get().value().getText());
                           }

                           if (d instanceof
                           gov.va.med.term.vhat.xml.model.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation) {
                              // Read any nested properties or subset memberships on this description
                              final Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.Properties xmlDesignationProperties =
                                 new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.Properties();
                              final Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.SubsetMemberships xmlSubsetMemberships =
                                 new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.SubsetMemberships();

                              Get.sememeService()
                                 .getSememesForComponent(sememe.getNid())
                                 .forEach((nestedSememe) -> {
                                 // skip code and vuid properties - they are handled already
                                             if ((nestedSememe.getAssemblageSequence() !=
                                                  MetaData.VUID.getConceptSequence()) &&
                                                 (nestedSememe.getAssemblageSequence() !=
                                                  this.codeAssemblageConceptSeq)) {
                                                if (this.ts.wasEverKindOf(nestedSememe.getAssemblageSequence(),
                                                      this.vhatPropertyTypesNid)) {
                                                   final PropertyType property = buildProperty(nestedSememe,
                                                                                               startDate,
                                                                                               endDate,
                                                                                               null);

                                                   if (property != null) {
                                                      xmlDesignationProperties.getProperty()
                                                            .add(property);
                                                   }
                                                }

                                                // a refset that doesn't represent a mapset
                                                else if (this.ts.wasEverKindOf(nestedSememe.getAssemblageSequence(),
                                                      this.vhatRefsetTypesNid) &&
                                                         !this.ts.wasEverKindOf(nestedSememe.getAssemblageSequence(),
                                                               IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getNid())) {
                                                   final SubsetMembership sm = buildSubsetMembership(nestedSememe,
                                                                                                     startDate,
                                                                                                     endDate);

                                                   if (sm != null) {
                                                      xmlSubsetMemberships.getSubsetMembership()
                                                            .add(sm);
                                                   }
                                                }
                                             }
                                          });

                              if (xmlDesignationProperties.getProperty()
                                    .size() > 0) {
                                 ((gov.va.med.term.vhat.xml.model.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation) d).setProperties(
                                     xmlDesignationProperties);
                                 hasChild = true;
                              }

                              if (xmlSubsetMemberships.getSubsetMembership()
                                    .size() > 0) {
                                 ((gov.va.med.term.vhat.xml.model.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation) d).setSubsetMemberships(
                                     xmlSubsetMemberships);
                                 hasChild = true;
                              }
                           }

                           if ((d.getAction() != ActionType.NONE) || hasChild) {
                              designations.add(d);
                           }
                        }
                     }
                  });
      return designations;
   }

   /**
    * Gets the preferred name description type.
    *
    * @param conceptNid the concept nid
    * @return the preferred description type for the concept
    */
   private String getPreferredNameDescriptionType(int conceptNid) {
      final ArrayList<String> descriptions         = new ArrayList<>(1);
      final ArrayList<String> inActiveDescriptions = new ArrayList<>(1);

      Get.sememeService()
         .getDescriptionsForComponent(conceptNid)
         .forEach(sememeChronology -> {
                     @SuppressWarnings({ "rawtypes", "unchecked" })
                     final Optional<LatestVersion<DescriptionSememe<?>>> latestVersion =
                        ((SememeChronology) sememeChronology).getLatestVersion(DescriptionSememe.class,
                                                                               this.STAMP_COORDINATES);

                     if (latestVersion.isPresent() &&
                         this.preferredNameExtendedType.equals(
                             Frills.getDescriptionExtendedTypeConcept(this.STAMP_COORDINATES,
                                   sememeChronology.getNid()).orElse(null))) {
                        if (latestVersion.get()
                                         .value()
                                         .getState() == State.ACTIVE) {
                           descriptions.add(latestVersion.get()
                                 .value()
                                 .getText());
                        } else {
                           inActiveDescriptions.add(latestVersion.get()
                                 .value()
                                 .getText());
                        }
                     }
                  });

      if (descriptions.size() == 0) {
         descriptions.addAll(inActiveDescriptions);
      }

      if (descriptions.size() == 0) {
         // This doesn't happen for concept that represent subsets, for example.
         this.log.debug("Failed to find a description flagged as preferred on concept " +
                        Get.identifierService().getUuidPrimordialForNid(conceptNid));

         final String description = Frills.getDescription(conceptNid,
                                                          this.STAMP_COORDINATES,
                                                          LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate())
                                          .orElse("ERROR!");

         if (description.equals("ERROR!")) {
            this.log.error("Failed to find any description on concept " +
                           Get.identifierService().getUuidPrimordialForNid(conceptNid));
         }

         return description;
      }

      if (descriptions.size() > 1) {
         this.log.warn("Found " + descriptions.size() +
                       " descriptions flagged as the 'Preferred' vhat type on concept " +
                       Get.identifierService().getUuidPrimordialForNid(conceptNid));
      }

      return descriptions.get(0);
   }

   /**
    * Gets the relationships.
    *
    * @param concept the concept
    * @param startDate the start date
    * @param endDate the end date
    * @return a List of Relationship objects for the concept
    */
   private List<Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship> getRelationships(
           ConceptChronology<?> concept,
           long startDate,
           long endDate) {
      final List<Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship> relationships =
         new ArrayList<>();

      for (final AssociationInstance ai: AssociationUtilities.getSourceAssociations(concept.getNid(),
            this.STAMP_COORDINATES)) {
         final SememeChronology<?> sc = ai.getData()
                                          .getChronology();
         ActionType action = determineAction(sc, startDate, endDate);
         final Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship xmlRelationship =
            new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship();

         try {
            String newTargetCode = null;
            String oldTargetCode = null;

            if (ai.getTargetComponent()
                  .isPresent()) {
               newTargetCode = getCodeFromNid(Get.identifierService()
                                                 .getNidForUuids(ai.getTargetComponent()
                                                       .get()
                                                       .getPrimordialUuid()));

               if ((newTargetCode == null) || newTargetCode.isEmpty()) {
                  this.log.warn("Failed to find new target code for concept " +
                                ai.getTargetComponent().get().getPrimordialUuid());
               }
            }

            if (action == ActionType.UPDATE) {
               // This is an active/inactive change
               oldTargetCode = newTargetCode;
               newTargetCode = null;
            } else if (action != ActionType.ADD) {
               // Get the old target value
               @SuppressWarnings({ "unchecked", "rawtypes" })
               final List<DynamicSememe<?>> coll =
                  ((SememeChronology) sc).getVisibleOrderedVersionList(this.STAMP_COORDINATES);

               Collections.reverse(coll);

               for (final DynamicSememe<?> s: coll) {
                  if (s.getTime() < startDate) {
                     final AssociationInstance assocInst = AssociationInstance.read(s, null);

                     oldTargetCode = getCodeFromNid(Get.identifierService()
                                                       .getNidForUuids(assocInst.getTargetComponent()
                                                             .get()
                                                             .getPrimordialUuid()));

                     if ((oldTargetCode == null) || oldTargetCode.isEmpty()) {
                        this.log.error("Failed to find old target code for concept " +
                                       ai.getTargetComponent().get().getPrimordialUuid());
                     }

                     break;
                  }
               }

               // if NONE && old != new => UPDATE
               if ((newTargetCode != null) &&!newTargetCode.equals(oldTargetCode) && (action == ActionType.NONE)) {
                  action = ActionType.UPDATE;
               }
            }

            xmlRelationship.setAction(action);
            xmlRelationship.setActive(ai.getData()
                                        .getState() == State.ACTIVE);
            xmlRelationship.setTypeName(ai.getAssociationType()
                                          .getAssociationName());
            xmlRelationship.setOldTargetCode(oldTargetCode);
            xmlRelationship.setNewTargetCode(newTargetCode);

            if (action != ActionType.NONE) {
               relationships.add(xmlRelationship);
            }
         } catch (final Exception e) {
            // Per Dan, catch()-ing to protect against export failure if this were to cause a problem
            // as this code is being added very late to Release 2
            this.log.error("Association build failure");
         }
      }

      return relationships;
   }

   /**
    * Checks for sememe modified in date range.
    *
    * @param nid the nid
    * @param startDate the start date
    * @return true or false, if the sememe was modified in the date range
    */
   private boolean hasSememeModifiedInDateRange(int nid, long startDate) {
      // Check all the nested sememes
      return Get.sememeService()
                .getSememesForComponent(nid)
                .anyMatch(sc -> {
                             @SuppressWarnings({ "unchecked", "rawtypes" })
                             final Optional<LatestVersion<SememeVersion>> sv =
                                ((SememeChronology) sc).getLatestVersion(SememeVersion.class,
                                                                         this.STAMP_COORDINATES);

                             if (sv.isPresent()) {
                                if (sv.get()
                                      .value()
                                      .getTime() > startDate) {
                                   return true;
                                }
                             }

                             // recurse
                             if (hasSememeModifiedInDateRange(sc.getNid(), startDate)) {
                                return true;
                             }

                             return false;
                          });
   }
}

