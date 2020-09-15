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
package sh.isaac.misc.exporters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.misc.associations.AssociationInstance;
import sh.isaac.misc.associations.AssociationUtilities;
import sh.isaac.misc.constants.VHATConstants;
import sh.isaac.misc.constants.terminology.data.*;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.SubsetMemberships.SubsetMembership;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship;
import sh.isaac.utility.Frills;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;



public class VetsExporter {

   private Logger log = LogManager.getLogger();

   private Map<UUID, String> designationTypes = new HashMap<>();
   private Map<UUID, String> propertyTypes = new HashMap<>();
   private Map<UUID, String> relationshipTypes = new HashMap<>();

   private Map<String, Long> subsetMap = new HashMap<>();

   private Terminology terminology;

   private StampFilter STAMP_COORDINATES = null;

   TaxonomySnapshot tss;
   
   boolean fullExportMode = false;

   public VetsExporter()
   {
   }

   /**
    *
    * @param writeTo the output stream object handling the export
    * @param startDate only export concepts modified on or after this date.  Set to 0, if you want to start from the beginning
    * @param endDate only export concepts where their most recent modified date is on or before this date.  Set to Long.MAX_VALUE to get everything.
    * @param fullExportMode if true, exports all content present at the end of the date range, ignoring start date.  All actions are set to add.
    *   If false - delta mode - calculates the action based on the start and end dates, and includes only the minimum required elements in the xml file.
    */
   public void export(OutputStream writeTo, long startDate, long endDate, boolean fullExportMode) {

      this.fullExportMode = fullExportMode;

      STAMP_COORDINATES = StampFilterImmutable.make(StatusSet.ACTIVE_AND_INACTIVE, StampPositionImmutable.make(endDate, MetaData.DEVELOPMENT_PATH____SOLOR.getNid()),
              IntSets.immutable.empty(), IntLists.immutable.empty());
      
      tss = Get.taxonomyService().getSnapshot(ManifoldCoordinateImmutable.makeStated(STAMP_COORDINATES, Coordinates.Language.UsEnglishFullyQualifiedName()));

      // XML object
      terminology = new Terminology();

      // Types
      terminology.setTypes(new Terminology.Types());
      Terminology.Types.Type xmlType;

      // Subsets/Refsets
      terminology.setSubsets(new Terminology.Subsets());

      // CodeSystem
      Terminology.CodeSystem xmlCodeSystem = new Terminology.CodeSystem();
      Terminology.CodeSystem.Version xmlVersion = new Terminology.CodeSystem.Version();
      Terminology.CodeSystem.Version.CodedConcepts xmlCodedConcepts = new Terminology.CodeSystem.Version.CodedConcepts();

      // For any added types to export
      Set<String> newDesignationTypes = new HashSet<>();
      Set<String> newPropertyTypes = new HashSet<>();
      Set<String> newRelationshipTypes = new HashSet<>();
      
      // Add to map
      Arrays.stream(Get.taxonomyService().getSnapshot(Coordinates.Manifold.DevelopmentStatedRegularNameSort())
      .getTaxonomyChildConceptNids(
            VHATConstants.VHAT_ASSOCIATION_TYPES.getNid())).forEach((conceptId) -> {
               ConceptChronology concept = Get.conceptService().getConceptChronology(conceptId);
               String preferredNameDescriptionType = getPreferredNameDescriptionType(concept.getNid());
               relationshipTypes.put(concept.getPrimordialUuid(), preferredNameDescriptionType);
               
               if (determineAction(concept, startDate, endDate).equals(ActionType.ADD))
               {
                  newRelationshipTypes.add(preferredNameDescriptionType);
               }
            });
      
      if (fullExportMode)
      {
         // Adds all of the elements in the specified collection to this set if they're not already present 
         newRelationshipTypes.addAll(relationshipTypes.values());
      }

      for (String s : newRelationshipTypes)
      {
         xmlType = new Terminology.Types.Type();
         xmlType.setKind(KindType.RELATIONSHIP_TYPE);
         xmlType.setName(s);
         terminology.getTypes().getType().add(xmlType);
      }

      // Add to map
      Arrays.stream(Get.taxonomyService().getSnapshot(Coordinates.Manifold.DevelopmentStatedRegularNameSort())
         .getTaxonomyChildConceptNids(
            VHATConstants.VHAT_ATTRIBUTE_TYPES.getNid())).forEach((conceptId) -> {
               ConceptChronology concept = Get.conceptService().getConceptChronology(conceptId);
               String preferredNameDescriptionType = getPreferredNameDescriptionType(concept.getNid());
               propertyTypes.put(concept.getPrimordialUuid(), preferredNameDescriptionType);
               
               if (determineAction(concept, startDate, endDate).equals(ActionType.ADD))
               {
                  newPropertyTypes.add(preferredNameDescriptionType);
               }
            });

      if (fullExportMode)
      {
         // Adds all of the elements in the specified collection to this set if they're not already present 
         newPropertyTypes.addAll(propertyTypes.values());
      }

      for (String s : newPropertyTypes)
      {
         xmlType = new Terminology.Types.Type();
         xmlType.setKind(KindType.PROPERTY_TYPE);
         xmlType.setName(s);
         terminology.getTypes().getType().add(xmlType);
      }

      // Add to map
      Arrays.stream(Get.taxonomyService().getSnapshot(Coordinates.Manifold.DevelopmentStatedRegularNameSort())
            .getTaxonomyChildConceptNids(
            Get.identifierService().getNidForUuids(VHATConstants.VHAT_DESCRIPTION_TYPES.getPrimordialUuid()))).forEach((conceptId) -> {
               ConceptChronology concept = Get.conceptService().getConceptChronology(conceptId);
               String preferredNameDescriptionType = getPreferredNameDescriptionType(concept.getNid());
               designationTypes.put(concept.getPrimordialUuid(), preferredNameDescriptionType);
               
               if (determineAction(concept, startDate, endDate).equals(ActionType.ADD))
               {
                  newDesignationTypes.add(preferredNameDescriptionType);
               }
            });

      if (fullExportMode)
      {
         // Adds all of the elements in the specified collection to this set if they're not already present 
         newDesignationTypes.addAll(designationTypes.values());
      }

      for (String s : newDesignationTypes)
      {
         xmlType = new Terminology.Types.Type();
         xmlType.setKind(KindType.DESIGNATION_TYPE);
         xmlType.setName(s);
         terminology.getTypes().getType().add(xmlType);
      }

      // Get data, Add to map
      Arrays.stream(Get.taxonomyService().getSnapshot(Coordinates.Manifold.DevelopmentStatedRegularNameSort())
            .getTaxonomyChildConceptNids(VHATConstants.VHAT_REFSETS.getNid())).forEach((tcs) ->
      {
         ConceptChronology concept = Get.conceptService().getConceptChronology(tcs);
         // Excluding these:
         if (concept.getPrimordialUuid().equals(VHATConstants.VHAT_ALL_CONCEPTS.getPrimordialUuid())
               || concept.getPrimordialUuid().equals(VHATConstants.VHAT_MISSING_SDO_CODE_SYSTEM_CONCEPTS.getPrimordialUuid())
               || Frills.definesMapping(concept.getNid()) ) 
         { 
            // Skip
         } else {
            Terminology.Subsets.Subset xmlSubset = new Terminology.Subsets.Subset();
            xmlSubset.setAction(determineAction(concept, startDate, endDate));
            xmlSubset.setName(getPreferredNameDescriptionType(concept.getNid()));
            xmlSubset.setActive(concept.isLatestVersionActive(STAMP_COORDINATES));

            //read VUID
            xmlSubset.setVUID(Frills.getVuId(concept.getNid(), STAMP_COORDINATES).orElse(null));

            if (xmlSubset.getVUID() == null)
            {
               log.warn("Failed to find VUID for subset concept " + concept.getPrimordialUuid());
            }

            if (xmlSubset.getAction() != ActionType.NONE)
            {
               terminology.getSubsets().getSubset().add(xmlSubset);
            }
            subsetMap.put(xmlSubset.getName(), xmlSubset.getVUID());
         }
      });


      ConceptChronology vhatConcept = Get.conceptService().getConceptChronology(VHATConstants.VHAT_ROOT_CONCEPT.getNid());

      xmlCodeSystem.setAction(ActionType.NONE);
      xmlCodeSystem.setName(getPreferredNameDescriptionType(vhatConcept.getNid()));
      xmlCodeSystem.setVUID(Frills.getVuId(VHATConstants.VHAT_ROOT_CONCEPT.getNid()).orElse(null));
      xmlCodeSystem.setDescription("VHA Terminology");  //This is in an acceptable synonym, but easier to hard code at the moment...
      xmlCodeSystem.setCopyright(Year.now().getValue() + "");
      xmlCodeSystem.setCopyrightURL("");
      xmlCodeSystem.setPreferredDesignationType("Preferred Name");

      xmlVersion.setAppend(Boolean.TRUE);
      xmlVersion.setName("Authoring Version");
      xmlVersion.setDescription("Delta output from ISAAC");

      try
      {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         String formattedDate = sdf.format(System.currentTimeMillis());
         XMLGregorianCalendar _xmlEffDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(formattedDate);
         XMLGregorianCalendar _xmlRelDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(formattedDate);
         xmlVersion.setEffectiveDate(_xmlEffDate);
         xmlVersion.setReleaseDate(_xmlRelDate);
      }
      catch (Exception pe)
      {
         log.error("Misconfiguration of date parser!", pe);
      }

      xmlVersion.setSource("");

      AtomicInteger skippedForNonVHAT = new AtomicInteger();
      AtomicInteger skippedDateRange = new AtomicInteger();
      AtomicInteger observedVhatConcepts = new AtomicInteger();
      AtomicInteger exportedVhatConcepts = new AtomicInteger();

      List<Terminology.CodeSystem.Version.MapSets.MapSet> xmlMapSetCollection = new ArrayList<>();

      Get.conceptService().getConceptChronologyStream().forEach((concept) ->
      {
         if (fullExportMode) 
         {
            Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage( concept.getNid(),
                  IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getNid()).forEach(mappingSemantic -> 
            {
               Terminology.CodeSystem.Version.MapSets.MapSet xmlMapSet = new Terminology.CodeSystem.Version.MapSets.MapSet();
               xmlMapSet.setAction(determineAction(concept, startDate, endDate));
               xmlMapSet.setActive(concept.isLatestVersionActive(STAMP_COORDINATES));
               xmlMapSet.setCode(getCodeFromNid(concept.getNid()));
               xmlMapSet.setName(getPreferredNameDescriptionType(concept.getNid()));
               xmlMapSet.setVUID(Frills.getVuId(concept.getNid(), STAMP_COORDINATES).orElse(null));

               // Source and Target CodeSystem
               LatestVersion<DynamicVersion> mappingSemanticVersion = mappingSemantic.getLatestVersion(STAMP_COORDINATES);

               if (mappingSemanticVersion.isPresent()) 
               {
                  // Get referenced component for the MapSet values
                  ConceptChronology cc = Get.conceptService().getConceptChronology(mappingSemanticVersion.get().getReferencedComponentNid());

                  LatestVersion<ConceptVersion> cv = cc.getLatestVersion(STAMP_COORDINATES);

                  if (cv.isPresent()) 
                  {
                     Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(cv.get().getChronology().getNid(),
                           IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getNid()).forEach(mappingStrExt -> {
                        LatestVersion<DynamicVersion> mappingStrExtVersion = mappingStrExt.getLatestVersion(STAMP_COORDINATES);

                        // TODO:DA review
                        if (mappingStrExtVersion.isPresent())
                        {
                           DynamicData dsd[] = mappingStrExtVersion.get().getData();
                           if (dsd.length == 2) 
                           {
                              if (dsd[0].getDataObject().equals(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM.getNid())) 
                              {
                                 xmlMapSet.setSourceCodeSystem(dsd[1].getDataObject().toString());
                              }
                              else if (dsd[0].getDataObject().equals(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM_VERSION.getNid())) 
                              {
                                 xmlMapSet.setSourceVersionName(dsd[1].getDataObject().toString());
                              }
                              else if (dsd[0].getDataObject().equals(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM.getNid()))
                              {
                                 xmlMapSet.setTargetCodeSystem(dsd[1].getDataObject().toString());
                              }
                              else if (dsd[0].getDataObject().equals(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM_VERSION.getNid()))
                              {
                                 xmlMapSet.setTargetVersionName(dsd[1].getDataObject().toString());
                              }
                           }
                        }
                     });

                     // MapEntries
                     // MapEntry->Properties
                     // TODO: MapEntry->Designations (currently ignored - none found in import XML, importer doesn't implement)
                     // TODO: MapEntry->Relationships (currently ignored - none found in import XML, importer doesn't implement)
                     // TODO:DA review - not using MapEntryType as it doesn't allow for .setProperties(), needed for GEM_Flags
                     Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries xmlMapEntries 
                           = new Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries();
                     for (Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry me : 
                           readMapEntryTypes(cv.get().getChronology().getNid(), startDate, endDate))
                     {
                        xmlMapEntries.getMapEntry().add(me);
                     }
                     xmlMapSet.setMapEntries(xmlMapEntries);
                  }
               }

               // Designations
               Terminology.CodeSystem.Version.MapSets.MapSet.Designations xmlMapSetDesignations 
                     = new Terminology.CodeSystem.Version.MapSets.MapSet.Designations();

               for (DesignationType d : getDesignations(concept, startDate, endDate,
                     (() -> new Terminology.CodeSystem.Version.MapSets.MapSet.Designations.Designation())))
               {
                  // MapSets contain a phantom description with no typeName, code or VUID - need to keep those out
                  // There's probably a more appropriate way to do this - quick and dirty for now
                  // TODO:DA review
                  if (!((d.getTypeName() == null || d.getTypeName().isEmpty())
                        && (d.getCode() == null || d.getCode().isEmpty()) && (d.getVUID() == null)))
                  {
                     xmlMapSetDesignations.getDesignation().add((Terminology.CodeSystem.Version.MapSets.MapSet.Designations.Designation) d);
                  }
               }

               xmlMapSet.setDesignations(xmlMapSetDesignations);

               // Properties
               Terminology.CodeSystem.Version.MapSets.MapSet.Properties xmlMapSetProperties 
                     = new Terminology.CodeSystem.Version.MapSets.MapSet.Properties();

               for (PropertyType pt : readPropertyTypes(concept.getNid(), startDate, endDate,
                     () -> new Terminology.CodeSystem.Version.MapSets.MapSet.Properties.Property()))
               {
                  xmlMapSetProperties.getProperty().add((Terminology.CodeSystem.Version.MapSets.MapSet.Properties.Property) pt);
               }
               xmlMapSet.setProperties(xmlMapSetProperties);

               if (xmlMapSet.getAction() != ActionType.NONE || (xmlMapSet.getMapEntries() != null
                     && xmlMapSet.getMapEntries().getMapEntry().size() > 0)) 
               {
                  xmlMapSetCollection.add(xmlMapSet);
               }
            });
         }
         
         if (!tss.isKindOf(concept.getNid(), VHATConstants.VHAT_ROOT_CONCEPT.getNid()))
         {
            // Needed to ignore all the dynamically created/non-imported concepts
            skippedForNonVHAT.getAndIncrement();
         }
         else if (concept.getNid() == VHATConstants.VHAT_ROOT_CONCEPT.getNid())
         {
            //skip
         }
         else
         {
            observedVhatConcepts.getAndIncrement();
            int conceptNid = concept.getNid();

            if (!wasConceptOrNestedValueModifiedInDateRange(concept, startDate))
            {
               skippedDateRange.getAndIncrement();
            }
            else
            {
               exportedVhatConcepts.getAndIncrement();

               Terminology.CodeSystem.Version.CodedConcepts.CodedConcept xmlCodedConcept 
                     = new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept();
               xmlCodedConcept.setAction(determineAction(concept, startDate, endDate));
               xmlCodedConcept.setName(getPreferredNameDescriptionType(conceptNid));
               xmlCodedConcept.setVUID(Frills.getVuId(conceptNid, STAMP_COORDINATES).orElse(null));
               xmlCodedConcept.setCode(getCodeFromNid(conceptNid));
               xmlCodedConcept.setActive(Boolean.valueOf(concept.isLatestVersionActive(STAMP_COORDINATES)));

               Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations xmlDesignations =
                     new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations();
               Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties xmlProperties =
                     new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties();


               for (DesignationType d : getDesignations(concept, startDate, endDate,
                     () -> new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation()))
               {
                  xmlDesignations.getDesignation().add((Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation)d);
               }

               for (PropertyType pt : readPropertyTypes(concept.getNid(), startDate, endDate,
                     () -> new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties.Property()))
               {
                  xmlProperties.getProperty().add((Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties.Property)pt);
               }


               // Relationships
               Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships xmlRelationships
                     = new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships();
               for (Relationship rel : getRelationships(concept, startDate, endDate))
               {
                  xmlRelationships.getRelationship().add(rel);
               }

               // Try to keep XML output somewhat clean, without empty elements (i.e. <Element/> or <Element></Element>
               if (xmlDesignations.getDesignation().size() > 0) {
                  xmlCodedConcept.setDesignations(xmlDesignations);
               }

               if (xmlProperties.getProperty().size() > 0) {
                  xmlCodedConcept.setProperties(xmlProperties);
               }

               if (xmlRelationships.getRelationship().size() > 0) {
                  xmlCodedConcept.setRelationships(xmlRelationships);
               }

               // Add all CodedConcept elements
               xmlCodedConcepts.getCodedConcept().add(xmlCodedConcept);
            }
         }
      });


      // Close out XML
      xmlVersion.setCodedConcepts(xmlCodedConcepts);

      // MapSets
      if (xmlMapSetCollection.size() > 0)
      {
         Terminology.CodeSystem.Version.MapSets xmlMapSets = new Terminology.CodeSystem.Version.MapSets();
         xmlMapSets.getMapSet().addAll(xmlMapSetCollection);
         xmlVersion.setMapSets(xmlMapSets);
      }

      xmlCodeSystem.setVersion(xmlVersion);
      terminology.setCodeSystem(xmlCodeSystem);

      log.info("Skipped " + skippedForNonVHAT.get() + " concepts for non-vhat");
      log.info("Skipped " + skippedDateRange.get() + " concepts for outside date range");
      log.info("Processed " + observedVhatConcepts.get() + " concepts");
      log.info("Exported " + exportedVhatConcepts.get() + " concepts");

      writeXml(writeTo);
   }

   /**
    * 
    * @param componentNid
    * @param startDate
    * @param endDate
    * @param constructor
    * @return a List of the PropertyType objects for the specific component
    */
   private List<PropertyType> readPropertyTypes(int componentNid, long startDate, long endDate, Supplier<PropertyType> constructor)
   {
      ArrayList<PropertyType> pts = new ArrayList<>();

      Get.assemblageService().getSemanticChronologyStreamForComponent(componentNid).forEach((semantic) ->
      {
         //skip code and vuid properties - they have special handling
         if (semantic.getAssemblageNid() != MetaData.VUID____SOLOR.getNid() && semantic.getAssemblageNid() != MetaData.CODE____SOLOR.getNid()
               && tss.isKindOf(semantic.getAssemblageNid(), VHATConstants.VHAT_ATTRIBUTE_TYPES.getNid()))
         {
            PropertyType property = buildProperty(semantic, startDate, endDate, constructor);
            if (property != null)
            {
               pts.add(property);
            }
         }
      });
      return pts;
   }
   

   /**
    * 
    * @param componentNid
    * @param startDate
    * @param endDate
    * @return a List of the MapEntry objects for the MapSet item
    */
   private List<Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry> readMapEntryTypes(int componentNid, long startDate, long endDate)
   {
      ArrayList<Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry> mes = new ArrayList<>();
      
      Get.assemblageService().getSemanticChronologyStream(componentNid).forEach(semantic ->
      {
         LatestVersion<DynamicVersion> semanticVersion = semantic.getLatestVersion(STAMP_COORDINATES);
         if (semanticVersion.isPresent() && semanticVersion.get().getData() != null && semanticVersion.get().getData().length > 0)
         {
            try {
               Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry me = new Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry();
               
               me.setAction(ActionType.ADD); // TODO: There is currently no requirement or ability to deploy MapSet deltas, this if for the full export only
               me.setVUID(Frills.getVuId(semantic.getNid(), STAMP_COORDINATES).orElse(null));
               String code = getCodeFromNid(semanticVersion.get().getReferencedComponentNid());
               if (null == code)
               {
                  code = Frills.getDescription(semanticVersion.get().getReferencedComponentNid(), null, null).orElse("");
               }
               me.setSourceCode(code);
               boolean isActive = semanticVersion.get().getStatus() == Status.ACTIVE;
               me.setActive(isActive);
               
               DynamicUtility ls =  LookupService.get().getService(DynamicUtility.class);
               if (ls == null)
               {
                  throw new RuntimeException("An implementation of DynamicSemanticUtility is not available on the classpath");
               }
               else
               {
                  DynamicColumnInfo[] dsci = ls.readDynamicUsageDescription(semanticVersion.get().getAssemblageNid()).getColumnInfo();
                  DynamicData dsd[] = semanticVersion.get().getData();
                  
                  for (DynamicColumnInfo d : dsci)
                  {
                     UUID columnUUID = d.getColumnDescriptionConcept();
                     int col = d.getColumnOrder();
                     
                     if (null != dsd[col] && null != columnUUID)
                     {
                        if (columnUUID.equals(DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid()))
                        {
                           me.setTargetCode(Frills.getDescription(UUID.fromString(dsd[col].getDataObject().toString()), null, null).orElse(""));
                        }
                        else if (columnUUID.equals(IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_EQUIVALENCE_TYPE.getPrimordialUuid()))
                        {
                           // Currently ignored, no XML representation
                        }
                        else if (columnUUID.equals(IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_SEQUENCE.getPrimordialUuid()))
                        {
                           me.setSequence(Integer.parseInt(dsd[col].getDataObject().toString()));
                        }
                        else if (columnUUID.equals(IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_GROUPING.getPrimordialUuid()))
                        {
                           me.setGrouping(Long.parseLong(dsd[col].getDataObject().toString()));
                        }
                        else if (columnUUID.equals(IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_EFFECTIVE_DATE.getPrimordialUuid()))
                        {
                           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                           String formattedDate = sdf.format(dsd[col].getDataObject());
                           me.setEffectiveDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(formattedDate));
                        }
                        else if (columnUUID.equals(IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_GEM_FLAGS.getPrimordialUuid()))
                        {
                           Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties.Property gem_prop 
                              = new Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties.Property();
                           Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties props
                              = new Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties();
                           gem_prop.setAction(ActionType.ADD); // See 'TODO' above
                           gem_prop.setActive(Boolean.TRUE); // TODO: Defaulting to true as it appears in the import XML, not sure how to determine otherwise
                           gem_prop.setTypeName("GEM_Flags");
                           gem_prop.setValueNew(dsd[col].getDataObject().toString());
                           props.getProperty().add(gem_prop);
                           me.setProperties(props);
                        }
                        else
                        {
                           log.warn("No mapping match found for UUID: ", columnUUID);
                        }
                     }
                  }
                  mes.add(me);
               }
            } 
            catch (NumberFormatException nfe)
            {
               log.error("Misconfiguration of integer parser!", nfe);
            }
            catch (IllegalArgumentException iae)
            {
               log.error("Misconfiguration of date parser!", iae);
            }
            catch (NullPointerException npe)
            {
               log.error("Misconfiguration of date parser!", npe);
            }
            catch (DatatypeConfigurationException dce)
            {
               log.error("Misconfiguration of date parser!", dce);
            }
            catch (Exception e)
            {
               log.error("General MapEntry failure!", e);
            }
         }
      });
      return mes;
   }

   /**
    * 
    * @param semantic
    * @param startDate
    * @param endDate
    * @param constructor
    * @return A PropertyType object for the property, or null
    */
   private PropertyType buildProperty(SemanticChronology semantic, long startDate, long endDate, Supplier<PropertyType> constructor)
   {
      String newValue = null;
      String oldValue = null;
      boolean isActive = false;
      if (semantic.getVersionType() == VersionType.DYNAMIC)
      {
         LatestVersion<DynamicVersion> semanticVersion = semantic.getLatestVersion(STAMP_COORDINATES);
         if (semanticVersion.isPresent() && semanticVersion.get().getData() != null && semanticVersion.get().getData().length > 0)
         {
            newValue = semanticVersion.get().getData()[0] == null ? null : semanticVersion.get().getData()[0].dataToString();
            List<DynamicVersion> coll = semantic.getVisibleOrderedVersionList(STAMP_COORDINATES);
            Collections.reverse(coll);
            for(DynamicVersion s : coll)
            {
               if (s.getTime() < startDate)
               {
                  oldValue = (s.getData()[0] != null) ? s.getData()[0].dataToString() : null;
                  break;
               }
            }
            isActive = semanticVersion.get().getStatus() == Status.ACTIVE;
         }
      }
      else if (semantic.getVersionType() == VersionType.STRING)
      {
         LatestVersion<StringVersion> semanticVersion = semantic.getLatestVersion(STAMP_COORDINATES);
         if (semanticVersion.isPresent())
         {
            newValue = semanticVersion.get().getString();
            List<StringVersion> coll = semantic.getVisibleOrderedVersionList(STAMP_COORDINATES);
            Collections.reverse(coll);
            for(StringVersion s : coll)
            {
               if (s.getTime() < startDate)
               {
                  oldValue = s.getString();
                  break;
               }
            }
            isActive = semanticVersion.get().getStatus() == Status.ACTIVE;
         }
      }
      else
      {
         log.warn("Unexpectedly passed semantic " + semantic + " when we only expected a dynamic or a string type");
         return null;
      }

      if (newValue == null)
      {
         return null;
      }

      PropertyType property = constructor == null ? new PropertyType() : constructor.get();
      property.setAction(determineAction(semantic, startDate, endDate));
      if (isActive && property.getAction() == ActionType.NONE && newValue.equals(oldValue))
      {
         return null;
      }
      else if(!newValue.equals(oldValue) && property.getAction() == ActionType.NONE)
      {
         // change action to update if the new and old values are not the same.
         property.setAction(ActionType.UPDATE);
      }
      //got to here, there is change.
      property.setActive(isActive);
      
      if ((property.getAction() == ActionType.UPDATE || property.getAction() == ActionType.ADD)
            && !newValue.equals(oldValue))
      {
         property.setValueNew(newValue);         
      }
      
      if (oldValue != null && property.getAction() != ActionType.ADD)
      {
         property.setValueOld(oldValue);
      }
      
      if (property.getAction() == ActionType.NONE)
      {
         return null;
      }
      
      property.setTypeName(getPreferredNameDescriptionType(semantic.getAssemblageNid()));
      return property;
   }

   /**
    * 
    * @param concept
    * @param startDate
    * @param endDate
    * @param constructor
    * @return a List of DesignationTypes for the concept
    */
   private List<DesignationType> getDesignations(ConceptChronology concept, long startDate, long endDate, Supplier<DesignationType> constructor) {

      List<DesignationType> designations = new ArrayList<>();

      Get.assemblageService().getSemanticChronologyStreamForComponent(concept.getNid()).forEach(semantic ->
      {
         if (semantic.getVersionType() == VersionType.DESCRIPTION)
         {
            boolean hasChild = false;
            LatestVersion<DescriptionVersion> descriptionVersion = semantic.getLatestVersion( STAMP_COORDINATES);
            if (descriptionVersion.isPresent())
            {
               DesignationType d = constructor.get();
               
               d.setAction(determineAction(semantic, startDate, endDate));
               
               if (d.getAction() != ActionType.ADD)
               {
                  List<DescriptionVersion> coll = semantic.getVisibleOrderedVersionList(STAMP_COORDINATES);
                  Collections.reverse(coll);
                  for(DescriptionVersion s : coll)
                  {
                     if (s.getTime() < startDate) {
                        d.setValueOld(s.getText());
                        break;
                     }
                  }
               }
               
               if (d.getAction() == ActionType.UPDATE || d.getAction() == ActionType.ADD) {
                  d.setValueNew(descriptionVersion.get().getText());
               }
               
               if (d.getValueNew() != null && d.getValueOld() != null && d.getValueNew().equals(d.getValueOld()))
               {
                  d.setValueOld(null);
                  d.setValueNew(null);
               }
               
               d.setCode(getCodeFromNid(semantic.getNid()));
               d.setVUID(Frills.getVuId(semantic.getNid(), STAMP_COORDINATES).orElse(null));
               d.setActive(descriptionVersion.get().getStatus() == Status.ACTIVE);

               //Get the extended type
               Optional<UUID> descType = Frills.getDescriptionExtendedTypeConcept(STAMP_COORDINATES, semantic.getNid(), true);
               if (descType.isPresent())
               {
                  d.setTypeName(designationTypes.get(descType.get()));
               }
               else
               {
                  log.warn("No extended description type present on description " + semantic.getPrimordialUuid() + " " + descriptionVersion.get().getText());
               }

               if (d instanceof Designation)
               {
                  //Read any nested properties or subset memberships on this description
                  Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.Properties xmlDesignationProperties =
                        new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.Properties();
                  Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.SubsetMemberships xmlSubsetMemberships =
                        new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.SubsetMemberships();


                  Get.assemblageService().getSemanticChronologyStreamForComponent(semantic.getNid()).forEach((nestedSemantic) -> {
                     //skip code and vuid properties - they are handled already
                     
                     if (nestedSemantic.getAssemblageNid() != MetaData.VUID____SOLOR.getNid() 
                           && nestedSemantic.getAssemblageNid() != MetaData.CODE____SOLOR.getNid())
                     {
                        if (tss.isKindOf(nestedSemantic.getAssemblageNid(), VHATConstants.VHAT_ATTRIBUTE_TYPES.getNid()))
                        {
                           PropertyType property = buildProperty(nestedSemantic, startDate, endDate, null);
                           if (property != null)
                           {
                              xmlDesignationProperties.getProperty().add(property);
                           }
                        }
                        //a refset that doesn't represent a mapset
                        else if (tss.isKindOf(nestedSemantic.getAssemblageNid(), VHATConstants.VHAT_REFSETS.getNid()) &&
                              ! tss.isKindOf(nestedSemantic.getAssemblageNid(), IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getNid()))
                        {
                           SubsetMembership sm = buildSubsetMembership(nestedSemantic, startDate, endDate);
                           if (sm != null)
                           {
                              xmlSubsetMemberships.getSubsetMembership().add(sm);
                           }
                        }
                     }
                  });

                  if (xmlDesignationProperties.getProperty().size() > 0) 
                  {
                     ((Designation)d).setProperties(xmlDesignationProperties);
                     hasChild = true;
                  }

                  if (xmlSubsetMemberships.getSubsetMembership().size() > 0) 
                  {
                     ((Designation)d).setSubsetMemberships(xmlSubsetMemberships);
                     hasChild = true;
                  }
               }

               if (d.getAction() != ActionType.NONE || hasChild)
               {
                  designations.add(d);
               }
            }
         }
      });
      return designations;
   }


   /**
    *
    * @param semantic the Chronicle object (concept) representing the Subset
    * @param startDate
    * @param endDate
    * @return the SubsetMembership object built for the semantic, or null
    */
   private SubsetMembership buildSubsetMembership(SemanticChronology semantic, long startDate, long endDate)
   {
      if (semantic.getVersionType() == VersionType.DYNAMIC)
      {
            SubsetMembership subsetMembership = new SubsetMembership();
            subsetMembership.setActive(semantic.isLatestVersionActive(STAMP_COORDINATES));
            subsetMembership.setAction(determineAction(semantic, startDate, endDate));
            
            if (subsetMembership.getAction() == ActionType.NONE)
            {
               return null;
            }
            
            Optional<Long> vuid = Frills.getVuId(semantic.getAssemblageNid(),STAMP_COORDINATES);
            if (vuid.isPresent())
            {
               subsetMembership.setVUID(vuid.get());
            }
            else
            {
               log.warn("No VUID found for Subset UUID: " + semantic.getPrimordialUuid());
            }
            
            return subsetMembership;
      }
      else
      {
         log.error("Unexpected semantic type! " + semantic);
         return null;
      }
   }

   /**
    *
    * @param concept
    * @param startDate
    * @param endDate
    * @return a List of Relationship objects for the concept
    */
   private List<Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship> getRelationships(ConceptChronology concept,
         long startDate, long endDate)
   {

      List<Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship> relationships = new ArrayList<>();

      for (AssociationInstance ai : AssociationUtilities.getSourceAssociations(concept.getNid(), STAMP_COORDINATES))
      {
         SemanticChronology sc = ai.getData().getChronology();
         ActionType action = determineAction(sc, startDate, endDate);
         
         Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship xmlRelationship =
               new Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship();
         
         try
         {
            String newTargetCode = null;
            String oldTargetCode = null;
            if (ai.getTargetComponent().isPresent())
            {
               newTargetCode = getCodeFromNid(Get.identifierService().getNidForUuids(ai.getTargetComponent().get().getPrimordialUuid()));
               if (newTargetCode == null || newTargetCode.isEmpty())
               {
                  log.warn("Failed to find new target code for concept " + ai.getTargetComponent().get().getPrimordialUuid());
               }
            }
            
            if (action == ActionType.UPDATE) 
            {
               // This is an active/inactive change
               oldTargetCode = newTargetCode;
               newTargetCode = null;
            }
            else if (action != ActionType.ADD)
            {
               // Get the old target value
               List<DynamicVersion> coll = sc.getVisibleOrderedVersionList(STAMP_COORDINATES);
               Collections.reverse(coll);
               for(DynamicVersion s : coll)
               {
                  if (s.getTime() < startDate) {
                     AssociationInstance assocInst = AssociationInstance.read(s, null);
                     oldTargetCode = getCodeFromNid(Get.identifierService().getNidForUuids(assocInst.getTargetComponent().get().getPrimordialUuid()));
                     if (oldTargetCode == null || oldTargetCode.isEmpty())
                     {
                        log.error("Failed to find old target code for concept " + ai.getTargetComponent().get().getPrimordialUuid());
                     }
                     break;
                  }
               }
               // if NONE && old != new => UPDATE
               if (newTargetCode != null && !newTargetCode.equals(oldTargetCode) && action == ActionType.NONE) {
                  action = ActionType.UPDATE;
               }
            }
            
            xmlRelationship.setAction(action);
            xmlRelationship.setActive(ai.getData().getStatus() == Status.ACTIVE);
            xmlRelationship.setTypeName(ai.getAssociationType().getAssociationName());
            if (oldTargetCode != null) {
               xmlRelationship.setOldTargetCode(oldTargetCode);
            }
            if (newTargetCode != null) {
               xmlRelationship.setNewTargetCode(newTargetCode);
            }
            
            if (action != ActionType.NONE)
            {
               relationships.add(xmlRelationship);
            }         
         }
         catch (Exception e)
         {
            // Per Dan, catch()-ing to protect against export failure if this were to cause a problem
            // as this code is being added very late to Release 2
            log.error("Association build failure");
         }
      }
         
      return relationships;
   }

   /**
    * 
    * @param conceptNid
    * @return the preferred description type for the concept 
    */
   private String getPreferredNameDescriptionType(int conceptNid)
   {
      ArrayList<String> descriptions = new ArrayList<>(1);
      ArrayList<String> inActiveDescriptions = new ArrayList<>(1);
      Get.assemblageService().getDescriptionsForComponent(conceptNid).forEach(semanticChronology ->
      {
         LatestVersion<DescriptionVersion> latestVersion = semanticChronology.getLatestVersion(STAMP_COORDINATES);
         if (latestVersion.isPresent()
               && VHATConstants.VHAT_PREFERRED_NAME.getPrimordialUuid()
                  .equals(Frills.getDescriptionExtendedTypeConcept(STAMP_COORDINATES, semanticChronology.getNid(), true).orElse(null)))
         {
            if (latestVersion.get().getStatus() == Status.ACTIVE)
            {
               descriptions.add(latestVersion.get().getText());
            }
            else
            {
               inActiveDescriptions.add(latestVersion.get().getText());
            }
         }
      });

      if (descriptions.size() == 0)
      {
         descriptions.addAll(inActiveDescriptions);
      }
      if (descriptions.size() == 0)
      {
         //This doesn't happen for concept that represent subsets, for example.
         log.debug("Failed to find a description flagged as preferred on concept " + Get.identifierService().getUuidPrimordialForNid(conceptNid));
         String description = Frills.getDescription(conceptNid, STAMP_COORDINATES,
                 Coordinates.Language.UsEnglishRegularName()).orElse("ERROR!");
         if (description.equals("ERROR!"))
         {
            log.error("Failed to find any description on concept " + Get.identifierService().getUuidPrimordialForNid(conceptNid));
         }
         return description;
      }
      if (descriptions.size() > 1)
      {
         log.warn("Found " + descriptions.size() + " descriptions flagged as the 'Preferred' vhat type on concept "
               + Get.identifierService().getUuidPrimordialForNid(conceptNid));
      }
      return descriptions.get(0);
   }

   /**
    * 
    * @param object
    * @param startDate
    * @param endDate
    * @return the ActionType object representing the change
    */
   private ActionType determineAction(Chronology object, long startDate, long endDate)
   {
      ActionType action = ActionType.ADD;
      
      if (!fullExportMode)
      {
         List<? extends StampedVersion> versions = object.getVersionList();
         versions.sort(new Comparator<StampedVersion>()
         {
            @Override
            public int compare(StampedVersion o1, StampedVersion o2)
            {
               return -1 * Long.compare(o1.getTime(), o2.getTime());
            }
         });
   
         int versionCountInDateRange = 0;
         int actionCountPriorToStartDate = 0;
         Status beginState = null;
         Status endState = null;
      
         for (StampedVersion sv : versions)
         {
            
            if (sv.getTime() < startDate)
            {
               //last value prior to start date
               if (beginState == null )
               {
                  beginState = sv.getStatus();
               }
               actionCountPriorToStartDate++;
            }
            if (sv.getTime() <= endDate && sv.getTime() >= startDate)
            {
               //last value prior to end date
               if (endState == null)
               {
                  endState = sv.getStatus();
               }
               versionCountInDateRange++;
            }
         }
         
         if (beginState == null && endState != null)
         {
            action = ActionType.ADD;
         }
         else if (beginState != null && endState == null)
         {
            action = ActionType.NONE;
         }
         else
         {
            if (beginState != endState)
            {
               action = ActionType.UPDATE;
            }
            else
            {
               if (versionCountInDateRange == 0)
               {
                  action = ActionType.NONE;
               }
               else if (versionCountInDateRange > 0)
               {
                  action = ActionType.UPDATE;
               }
               else if (actionCountPriorToStartDate > 0 && versionCountInDateRange > 0)
               {
                  return ActionType.UPDATE;
               }
               /* The UI does not allow Remove. Only Active and Inactive. May be revisited in R3 
               else if (finalStateIsInactive && actionCountPriorToStartDate > 0)
               {
                  return ActionType.REMOVE;
               }
               */
            }
         }
      }
      
      return action;
   }


   /**
    * Scan through all (nested) components associated with this concept, and the concept itself, and see if the latest edit
    * date for any component is within our filter range.
    * @return true or false, if the concept or a nested value was modified within the date range
    */
   private boolean wasConceptOrNestedValueModifiedInDateRange(ConceptChronology concept, long startDate)
   {
      LatestVersion<ConceptVersion> cv = concept.getLatestVersion(STAMP_COORDINATES);
      if (cv.isPresent())
      {
         if (cv.get().getTime() >= startDate)
         {
            return true;
         }
      }

      return hasSemanticModifiedInDateRange(concept.getNid(), startDate);
   }

   /**
    * 
    * @param nid
    * @param startDate
    * @return true or false, if the semantic was modified in the date range
    */
   private boolean hasSemanticModifiedInDateRange(int nid, long startDate)
   {
      //Check all the nested semantics
      return Get.assemblageService().getSemanticChronologyStreamForComponent(nid).anyMatch(sc ->
      {
         LatestVersion<Version> sv = sc.getLatestVersion(STAMP_COORDINATES);
         if (sv.isPresent())
         {
            if (sv.get().getTime() > startDate)
            {
               return true;
            }
         }
         //recurse
         if (hasSemanticModifiedInDateRange(sc.getNid(), startDate))
         {
            return true;
         }
         return false;
      });
   }

   /**
    * 
    * @param componentNid
    * @return the Code value found based on the Nid
    */
   private String getCodeFromNid(int componentNid)
   {

      Optional<SemanticChronology> sc = Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(componentNid,
            MetaData.CODE____SOLOR.getNid()).findFirst();
      if (sc.isPresent())
      {
         //There was a bug in the older terminology loaders which loaded 'Code' as a static semantic, but marked it as a dynamic semantic.
         //So during edits, new entries would get saves as dynamic semantics, while old entries were static.  Handle either....

         if (sc.get().getVersionType() == VersionType.STRING)
         {
            LatestVersion<StringVersion> sv = sc.get().getLatestVersion(STAMP_COORDINATES);
            if (sv.isPresent())
            {
               return sv.get().getString();
            }
         }
         else if (sc.get().getVersionType() == VersionType.DYNAMIC)  //this path will become dead code, after the data is fixed.
         {
            LatestVersion<DynamicVersion> sv = sc.get().getLatestVersion(STAMP_COORDINATES);
            if (sv.isPresent())
            {
               if (sv.get().getData() != null && sv.get().getData().length == 1)
               {
                  return sv.get().getData()[0].dataToString();
               }
            }
         }
         else
         {
            log.error("Unexpected semantic type for 'Code' semantic on nid " + componentNid);
         }
      }
      return null;
   }

   /**
    * 
    * @param writeTo
    */
   private void writeXml(OutputStream writeTo)
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(Terminology.class);
         Marshaller marshaller = jaxbContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(terminology, writeTo);

      }
      catch (Exception e)
      {
         log.error("Unexpected", e);
         throw new RuntimeException(e);
      }
   }
}