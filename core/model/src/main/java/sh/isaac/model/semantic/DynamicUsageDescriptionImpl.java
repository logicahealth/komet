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



package sh.isaac.model.semantic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sh.isaac.api.Get;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.brittle.BrittleVersion;
import sh.isaac.api.component.semantic.version.brittle.BrittleVersion.BrittleDataTypes;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicValidatorType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.configuration.StampCoordinates;

/**
 * See {@link DynamicUsageDescription}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
//Even though this class is static, needs to be a service, so that the reset() gets fired at appropriate times.
@Service
@Singleton
public class DynamicUsageDescriptionImpl
         implements DynamicUsageDescription, StaticIsaacCache {

   protected static final Logger logger = LogManager.getLogger(DynamicUsageDescription.class.getName());

   private static Cache<Integer, DynamicUsageDescription> dynamicCache = Caffeine.newBuilder().maximumSize(3000).build();
   private static Cache<Integer, DynamicUsageDescription> staticCache = Caffeine.newBuilder().maximumSize(1000).build();

   int refexUsageDescriptorNid;

   /** The semantic usage description. */
   String semanticUsageDescription;

   /** The name. */
   String name;

   /** The refex column info. */
   DynamicColumnInfo[] refexColumnInfo;

   /** The referenced component type restriction. */
   IsaacObjectType referencedComponentTypeRestriction;

   /** The referenced component type sub restriction. */
   VersionType referencedComponentTypeSubRestriction;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic element usage description impl.
    */
   private DynamicUsageDescriptionImpl() {
      // For use by the mock static method
   }

   /**
    * Read the RefexUsageDescription data from the database for a given sequence or nid.
    *
    * Note that most users should call {@link #read(int)} instead, as that
    * utilizes a cache. This always reads directly from the DB.
    *
    * @param refexUsageDescriptorId sequence or NID of refexUsageDescriptor
    */
   @SuppressWarnings("unchecked")
   public DynamicUsageDescriptionImpl(int refexUsageDescriptorId) {
      final ConceptChronology assemblageConcept = Get.conceptService()
                                                        .getConceptChronology(refexUsageDescriptorId);

      this.refexUsageDescriptorNid = assemblageConcept.getNid();

      final TreeMap<Integer, DynamicColumnInfo> allowedColumnInfo = new TreeMap<>();

      for (final SemanticChronology descriptionSemantic:
            assemblageConcept.getConceptDescriptionList()) {
         @SuppressWarnings("rawtypes")
         final LatestVersion descriptionVersion =
            ((SemanticChronology) descriptionSemantic).getLatestVersion(StampCoordinates.getDevelopmentLatest());

         if (descriptionVersion.isPresent()) {
            final DescriptionVersion ds = (DescriptionVersion) descriptionVersion.get();

            if (ds.getDescriptionTypeConceptNid() == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
               final Optional<SemanticChronology> nestesdSemantic = Get.assemblageService()
                                                                                               .getSemanticChronologyStreamForComponentFromAssemblage(ds.getNid(),
                                                                                                        DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION
                                                                                                              .getNid())
                                                                                               .findAny();
               if (nestesdSemantic.isPresent()) {
                  this.semanticUsageDescription = ds.getText();
               }
            }

            if (ds.getDescriptionTypeConceptNid() ==
                  TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
               this.name = ds.getText();
            }

            if ((this.semanticUsageDescription != null) && (this.name != null)) {
               break;
            }
         }
      }

      if (StringUtils.isEmpty(this.semanticUsageDescription)) {
         throw new RuntimeException(
             "The Assemblage concept: " + assemblageConcept +
             " is not correctly assembled for use as an Assemblage for " +
             "a DynamicSemanticData Refex Type.  It must contain a description of type Definition with an annotation of type " +
             "DynamicSemantic.DYNAMIC_DEFINITION_DESCRIPTION");
      }

      Get.assemblageService()
         .getSemanticChronologyStreamForComponent(assemblageConcept.getNid())
         .forEach(semantic -> {
                     if (semantic.getVersionType() == VersionType.DYNAMIC) {
                        final LatestVersion<? extends DynamicVersion> semanticVersion =
                   ((SemanticChronology) semantic).getLatestVersion(StampCoordinates.getDevelopmentLatest());

                        if (semanticVersion.isPresent()) {
                           final DynamicVersion       ds                  = semanticVersion.get();
                           final DynamicData[] refexDefinitionData = ds.getData();

                           if (semantic.getAssemblageNid()==
                               DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION.getNid()) {
                              if ((refexDefinitionData == null) ||
                                  (refexDefinitionData.length < 3) ||
                                  (refexDefinitionData.length > 7)) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSemanticData Refex Type.  It must contain at least 3 columns in the DynamicSemanticDataBI attachment, and no more than 7.");
                              }

                              // col 0 is the column number,
                              // col 1 is the concept with col name
                              // col 2 is the column data type, stored as a string.
                              // col 3 (if present) is the default column data, stored as a subtype of DynamicData
                              // col 4 (if present) is a boolean field noting whether the column is required (true) or optional (false or null)
                              // col 5 (if present) is the validator {@link DynamicValidatorType}, stored as a string array.
                              // col 6 (if present) is the validatorData for the validator in column 5, stored as a subtype of DynamicData
                              try {
                                 final int  column          = (Integer) refexDefinitionData[0].getDataObject();
                                 final UUID descriptionUUID = (UUID) refexDefinitionData[1].getDataObject();
                                 final DynamicDataType type =
                                    DynamicDataType.valueOf((String) refexDefinitionData[2].getDataObject());
                                 DynamicData defaultData = null;

                                 if (refexDefinitionData.length > 3) {
                                    defaultData = ((refexDefinitionData[3] == null) ? null
                              : refexDefinitionData[3]);
                                 }

                                 if ((defaultData != null) &&
                                     (type.getDynamicMemberClass() !=
                                     refexDefinitionData[3].getDynamicDataType().getDynamicMemberClass())) {
                                    throw new IOException("The Assemblage concept: " + assemblageConcept +
                                    " is not correctly assembled for use as an Assemblage for " +
                                       "a DynamicSemanticData Refex Type.  The type of the column (column 3) must match the type of the defaultData (column 4)");
                                 }

                                 Boolean columnRequired = null;

                                 if (refexDefinitionData.length > 4) {
                                    columnRequired = ((refexDefinitionData[4] == null) ? null
                              : (Boolean) refexDefinitionData[4].getDataObject());
                                 }

                                 DynamicValidatorType[] validators     = null;
                                 DynamicData[]          validatorsData = null;

                                 if (refexDefinitionData.length > 5) {
                                    if ((refexDefinitionData[5] != null) &&
                                        ((DynamicArray<DynamicString>) refexDefinitionData[5]).getDataArray().length >
                                        0) {
                                       final DynamicArray<DynamicString> readValidators =
                                          (DynamicArray<DynamicString>) refexDefinitionData[5];

                                       validators =
                                          new DynamicValidatorType[readValidators.getDataArray().length];

                                       for (int i = 0; i < validators.length; i++) {
                                          validators[i] = DynamicValidatorType.valueOf(
                                              (String) readValidators.getDataArray()[i]
                                                    .getDataObject());
                                       }
                                    }

                                    if (refexDefinitionData.length > 6) {
                                       if ((refexDefinitionData[6] != null) &&
                                           ((DynamicArray<? extends DynamicData>) refexDefinitionData[6]).getDataArray().length >
                                           0) {
                                          final DynamicArray<? extends DynamicData> readValidatorsData =
                                             (DynamicArray<? extends DynamicData>) refexDefinitionData[6];

                                          validatorsData =
                                             new DynamicData[readValidatorsData.getDataArray().length];

                                          if (validators != null) {
                                             for (int i = 0; i < validators.length; i++) {
                                                if (readValidatorsData.getDataArray()[i] != null) {
                                                   validatorsData[i] = readValidatorsData.getDataArray()[i];
                                                } else {
                                                   validatorsData[i] = null;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }

                                 allowedColumnInfo.put(column,
                                                       new DynamicColumnInfo(
                                                       assemblageConcept.getPrimordialUuid(),
                                                       column,
                                                       descriptionUUID,
                                                       type,
                                                       defaultData,
                                                       columnRequired,
                                                       validators,
                                               validatorsData));
                              } catch (final Exception e) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSemanticData Refex Type.  The first column must have a data type of integer, and the third column must be a string " +
                                 "that is parseable as a DynamicSemanticDataType");
                              }
                           } else if (semantic.getAssemblageNid() ==
                           DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getNid()) {
                              if ((refexDefinitionData == null) || (refexDefinitionData.length < 1)) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSemanticData Refex Type.  If it contains a " +
                                 DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getFullyQualifiedName() +
                                 " then it must contain a single column of data, of type string, parseable as a " +
                                 IsaacObjectType.class.getName());
                              }

                              // col 0 is Referenced component restriction information - as a string.
                              try {
                                 final IsaacObjectType type =
                                       IsaacObjectType.parse(refexDefinitionData[0].getDataObject()
                                                                                     .toString(),
                                                               false);

                                 if (type == IsaacObjectType.UNKNOWN) {
                                    // just ignore - it shouldn't have been saved that way anyway.
                                 } else {
                                    this.referencedComponentTypeRestriction = type;
                                 }
                              } catch (final Exception e) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSemanticData Refex Type.  The component type restriction annotation has an invalid value");
                              }

                              // col 1 is an optional Referenced component sub-restriction information - as a string.
                              if ((refexDefinitionData.length > 1) && (refexDefinitionData[1] != null)) {
                                 try {
                                    final VersionType type = VersionType.parse(refexDefinitionData[1].getDataObject()
                                                                                                   .toString(),
                                                                             false);

                                    if (type == VersionType.UNKNOWN) {
                                       // just ignore - it shouldn't have been saved that way anyway.
                                    } else {
                                       this.referencedComponentTypeSubRestriction = type;
                                    }
                                 } catch (final Exception e) {
                                    throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                    " is not correctly assembled for use as an Assemblage for " +
                                    "a DynamicSemanticData Refex Type.  The component type restriction annotation has an invalid value");
                                 }
                              } else {
                                 this.referencedComponentTypeSubRestriction = null;
                              }
                           }
                        }
                     }
                  });
      this.refexColumnInfo = new DynamicColumnInfo[allowedColumnInfo.size()];

      int i = 0;

      for (final int key: allowedColumnInfo.keySet()) {
         if (key != i) {
            throw new RuntimeException(
                "The Assemblage concept: " + assemblageConcept +
                " is not correctly assembled for use as an Assemblage for " +
                "a DynamicSemanticData Refex Type.  It must contain sequential column numbers, with no gaps, which start at 0.");
         }

         this.refexColumnInfo[i++] = allowedColumnInfo.get(key);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final DynamicUsageDescriptionImpl other = (DynamicUsageDescriptionImpl) obj;

      if (this.refexUsageDescriptorNid != other.refexUsageDescriptorNid) {
         return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode() {
      final int prime  = 31;
      int       result = 1;

      result = prime * result + this.refexUsageDescriptorNid;
      return result;
   }

   /**
    * Invent DynamicUsageDescription info for other semantic types (that aren't dynamic), otherwise, calls {@link #read(int)} 
    * if it is a dynamic semantic.
    *
    * This method prefers to mock from metadata, but will fall back to instance data, if metadata is missing for static semantics.
    *
    * This also utilizes a cache.
    *
    * @param assemblageNid the nid of the concept that defines the semantic type.
    * @return the dynamic element usage description
    */
   public static DynamicUsageDescription mockOrRead(int assemblageNid)
   {
      if (isDynamicSemanticFullRead(assemblageNid))
      {
         return read(assemblageNid);
      }
      else
      {
         return staticCache.get(assemblageNid, nidAgain ->
         {
            //the other semantics aren't consistently defined.
            //We need an instance of a semantic, to get the data, or an instance of Keiths latest (inconsistently applied) pattern
            //Try to read from metadata first.
            Optional<SemanticChronology> metadata = Get.assemblageService()
                  .getSemanticChronologyStreamForComponentFromAssemblage(assemblageNid, TermAux.SEMANTIC_TYPE.getNid()).findFirst();
            if (metadata.isPresent())
            {
               //Read this semantic, to find out the type info
               LatestVersion<SemanticVersion> latest = metadata.get().getLatestVersion(StampCoordinates.getDevelopmentLatest());
               if (latest.isPresent())
               {
                  if (latest.get().getSemanticType() == VersionType.COMPONENT_NID)
                  {
                     DynamicUsageDescriptionImpl dsud = new DynamicUsageDescriptionImpl();
                     dsud.name = Get.conceptDescriptionText(assemblageNid);
                     dsud.referencedComponentTypeRestriction = null;
                     dsud.referencedComponentTypeSubRestriction = null;
                     dsud.refexUsageDescriptorNid = assemblageNid;
                     dsud.semanticUsageDescription = "-";

                     if (((ComponentNidVersion) latest.get()).getComponentNid() == TermAux.MEMBERSHIP_SEMANTIC.getNid())
                     {
                        dsud.refexColumnInfo = new DynamicColumnInfo[0];
                     }
                     else //something with columns
                     {
                        if (((ComponentNidVersion) latest.get()).getComponentNid() == TermAux.STRING_SEMANTIC.getNid())
                        {
                           dsud.refexColumnInfo = mockDynamicColumnFromStatic(assemblageNid,
                                 new UUID[] { DynamicConstants.get().DYNAMIC_DT_STRING.getPrimordialUuid() },
                                 new DynamicDataType[] { DynamicDataType.STRING });
                        }
                        else if (((ComponentNidVersion) latest.get()).getComponentNid() == TermAux.INTEGER_SEMANTIC.getNid())
                        {
                           dsud.refexColumnInfo = mockDynamicColumnFromStatic(assemblageNid,
                                 new UUID[] { DynamicConstants.get().DYNAMIC_DT_INTEGER.getPrimordialUuid() },
                                 new DynamicDataType[] { DynamicDataType.INTEGER });
                        }
                        else if (((ComponentNidVersion) latest.get()).getComponentNid() == TermAux.COMPONENT_SEMANTIC.getNid() ||
                           ((ComponentNidVersion) latest.get()).getComponentNid() == TermAux.CONCEPT_SEMANTIC.getNid())
                        {
                           dsud.refexColumnInfo = mockDynamicColumnFromStatic(assemblageNid,
                                 new UUID[] { DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid() },
                                 new DynamicDataType[] { DynamicDataType.NID });
                        }
                        else if (((ComponentNidVersion) latest.get()).getComponentNid() == TermAux.DESCRIPTION_SEMANTIC.getNid())
                        {
                           dsud.refexColumnInfo = mockDynamicColumnFromStatic(assemblageNid, new UUID[] {
                                 DynamicConstants.get().DYNAMIC_DT_STRING.getPrimordialUuid(), DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid(),
                                 DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid(), DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid() },
                                 new DynamicDataType[] { DynamicDataType.STRING, DynamicDataType.NID, DynamicDataType.NID, DynamicDataType.NID });
                        }
                        else if (((ComponentNidVersion) latest.get()).getComponentNid() == TermAux.LOGICAL_EXPRESSION_SEMANTIC.getNid() ||
                              ((ComponentNidVersion) latest.get()).getComponentNid() == TermAux.IMAGE_SEMANTIC.getNid())
                        {
                           dsud.refexColumnInfo = mockDynamicColumnFromStatic(assemblageNid, new UUID[] {
                                 DynamicConstants.get().DYNAMIC_DT_BYTE_ARRAY.getPrimordialUuid()},
                                 new DynamicDataType[] { DynamicDataType.BYTEARRAY});
                        }
                        else
                        {
                           throw new RuntimeException("Unsupported type for mock on " + assemblageNid + " with type "
                                 + Get.conceptDescriptionText(((ComponentNidVersion) latest.get()).getComponentNid()));
                        }
                     }
                     return dsud;
                  }
                  else
                  {
                     throw new RuntimeException("Invalidly constructed static metadata on " + assemblageNid);
                  }
               }
            }
            //Try to figure out from an actual usage
            Optional<SemanticChronology> sc = Get.assemblageService().getSemanticChronologyStream(assemblageNid).findFirst();
            if (sc.isPresent())
            {
               return mockOrRead(sc.get());
            }
            else
            {
               throw new RuntimeException("Can't mock or read a static semantic with no instances in the system, and no valid metadata, "
                     + assemblageNid + " " + Get.conceptDescriptionText(assemblageNid));
            }
         });
      }
   }

   /**
    * Invent DynamicUsageDescription info for other semantic types (that aren't dynamic), otherwise, calls {@link #read(int)}
    * if it is a dynamic semantic.  This method invents the dynamicSemantic from the passed in semantic instance, rather than
    * reading metadata.  This method does NOT utilize a cache for static semantics
    *
    * @param semantic the semantic in question
    * @return the dynamic element usage description
    */
   public static DynamicUsageDescription mockOrRead(SemanticChronology semantic) {
      final DynamicUsageDescriptionImpl dsud = new DynamicUsageDescriptionImpl();

      dsud.name                                  = Get.conceptDescriptionText(semantic.getAssemblageNid());
      dsud.referencedComponentTypeRestriction    = null;
      dsud.referencedComponentTypeSubRestriction = null;
      dsud.refexUsageDescriptorNid          = semantic.getAssemblageNid();
      dsud.semanticUsageDescription                = "-";

      if (semantic.getVersionType() == VersionType.DYNAMIC) {
         return read(semantic.getAssemblageNid());
      }

      Optional<int[]> brittleColumnTitles = readStaticSemanticInfo(semantic.getAssemblageNid());

      switch (semantic.getVersionType()) {
         //Some hard-coded cases...
      case COMPONENT_NID:
            if (brittleColumnTitles.isPresent() && brittleColumnTitles.get().length != 1) {
               logger.error("Brittle semantic info is invalid!  {} != {}", brittleColumnTitles.get().length, 1);
               brittleColumnTitles = Optional.empty();
            }
         dsud.refexColumnInfo = new DynamicColumnInfo[] {
            new DynamicColumnInfo(
                Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()),
                0,
                   brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[0])
                      : DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid(),
                DynamicDataType.NID,
                null,
                true,
                null,
                   null) };
         break;

      case LONG:
            if (brittleColumnTitles.isPresent() && brittleColumnTitles.get().length != 1) {
               logger.error("Brittle semantic info is invalid!  {} != {}", brittleColumnTitles.get().length, 1);
               brittleColumnTitles = Optional.empty();
            }
         dsud.refexColumnInfo = new DynamicColumnInfo[] {
            new DynamicColumnInfo(
                Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()),
                0,
                   brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[0])
                      : DynamicConstants.get().DYNAMIC_DT_LONG.getPrimordialUuid(),
                DynamicDataType.LONG,
                null,
                true,
                null,
                   null) };
         break;

      case DESCRIPTION:
            if (brittleColumnTitles.isPresent() && brittleColumnTitles.get().length != 4) {
               logger.error("Brittle semantic info is invalid!  {} != {}", brittleColumnTitles.get().length, 4);
               brittleColumnTitles = Optional.empty();
            }
          dsud.refexColumnInfo = new DynamicColumnInfo[] {
                  new DynamicColumnInfo(
                      Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()),
                      0,
                  brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[0])
                     : DynamicConstants.get().DYNAMIC_DT_STRING.getPrimordialUuid(),
                      DynamicDataType.STRING,
                      null,
                      true,
                      null,
                  null),
                  new DynamicColumnInfo(
                          Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()),
                          1,
                  brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[1])
                     : DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid(),
                          DynamicDataType.NID,
                          null,
                          true,
                          null,
                  null),
                  new DynamicColumnInfo(
                          Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()),
                          2,
                  brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[2])
                     : DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid(),
                          DynamicDataType.NID,
                          null,
                          true,
                          null,
                  null),
                  new DynamicColumnInfo(
                          Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()),
                          3,
                  brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[3])
                     : DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid(),
                          DynamicDataType.NID,
                          null,
                          true,
                          null,
                  null)};
               break;
      case STRING:
      case LOGIC_GRAPH:
         dsud.refexColumnInfo = new DynamicColumnInfo[] {
            new DynamicColumnInfo(
                Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()),
                0,
                  brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[0])
                     : DynamicConstants.get().DYNAMIC_DT_STRING.getPrimordialUuid(),
                DynamicDataType.STRING,
                null,
                true,
                null,
                  null) };
         break;

      case MEMBER:
         dsud.refexColumnInfo = new DynamicColumnInfo[] {};
         break;

         //computed cases - we have to have a data instance to figure these out due to a lack of metadata
      case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
      case LOINC_RECORD:
      case MEASURE_CONSTRAINTS:
      case Nid1_Int2:
      case Nid1_Int2_Str3_Str4_Nid5_Nid6:
      case Nid1_Nid2:
      case Nid1_Nid2_Int3:
      case Nid1_Nid2_Str3:
      case Nid1_Str2:
      case RF2_RELATIONSHIP:
      case Str1_Nid2_Nid3_Nid4:
      case Str1_Str2:
      case Str1_Str2_Nid3_Nid4:
      case Str1_Str2_Nid3_Nid4_Nid5:
      case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
         LatestVersion<BrittleVersion> version = semantic.getLatestVersion(StampCoordinates.getDevelopmentLatest());
            if (version.isPresent())
            {
               ArrayList<DynamicColumnInfo> dci = new ArrayList<>();
               int col = 0;

                  if (brittleColumnTitles.isPresent() && brittleColumnTitles.get().length != version.get().getFieldTypes().length) {
                     logger.error("Brittle semantic info is invalid!  {} != {}", brittleColumnTitles.get().length, version.get().getFieldTypes().length);
                     brittleColumnTitles = Optional.empty();
                  }

               for (BrittleDataTypes dt : version.get().getFieldTypes())
               {
                  switch (dt) {
                     case BOOLEAN:
                        dci.add(new DynamicColumnInfo(Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()), col++,
                                 brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[col - 1])
                                      : DynamicConstants.get().DYNAMIC_DT_BOOLEAN.getPrimordialUuid(),
                              DynamicDataType.BOOLEAN,
                                 null, true, null, null));
                        break;
                     case FLOAT:
                        dci.add(new DynamicColumnInfo(Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()), col++,
                                 brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[col - 1])
                                      : DynamicConstants.get().DYNAMIC_DT_FLOAT.getPrimordialUuid(),
                              DynamicDataType.FLOAT,
                                 null, true, null, null));
                        break;
                     case INTEGER:
                        dci.add(new DynamicColumnInfo(Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()), col++,
                                 brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[col - 1])
                                      : DynamicConstants.get().DYNAMIC_DT_INTEGER.getPrimordialUuid(),
                              DynamicDataType.INTEGER,
                                 null, true, null, null));
                        break;
                     case NID:
                        dci.add(new DynamicColumnInfo(Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()), col++,
                                 brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[col - 1])
                                      : DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid(),
                              DynamicDataType.NID,
                                 null, true, null, null));
                        break;
                     case STRING:
                        dci.add(new DynamicColumnInfo(Get.identifierService().getUuidPrimordialForNid(semantic.getAssemblageNid()), col++,
                                 brittleColumnTitles.isPresent() ? Get.identifierService().getUuidPrimordialForNid(brittleColumnTitles.get()[col - 1])
                                      : DynamicConstants.get().DYNAMIC_DT_STRING.getPrimordialUuid(),
                              DynamicDataType.STRING,
                                 null, true, null, null));
                        break;
                     default :
                        throw new RuntimeException("Use case not yet supported");
                  }
               }
                  dsud.refexColumnInfo = dci.toArray(new DynamicColumnInfo[dci.size()]);
            }
         break;
         case DYNAMIC:  //dynamic is handled above
         case UNKNOWN:
         case CONCEPT:
      default:
         throw new RuntimeException("Use case not yet supported");
      }

      return dsud;
   }

   /**
    * Method to read the column descriptor info that is on (some) of the static semantics in the system
    * @param assemblageNid
    * @return
    */
   private static Optional<int[]> readStaticSemanticInfo(int assemblageBeingDefinedNid)
   {
      // The (undocumented) pattern that seems to be in place for some (not all) static semantics is multiple instances
      // of a ComponentInteger semantic which tell you the column title, and column order.
      TreeMap<Integer, Integer> columnTitles = new TreeMap<>();
      Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(assemblageBeingDefinedNid, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS.getNid())
         .forEach(semantic ->
         {
            LatestVersion<Nid1_Int2_Version> lvs = semantic.getLatestVersion(StampCoordinates.getDevelopmentLatest());
            if (lvs.isPresent())
            {
               columnTitles.put(lvs.get().getInt2(), lvs.get().getNid1());
            }
         });

      if (columnTitles.size() > 0)
      {
         int[] result = new int[columnTitles.size()];
         int j = 0;
         for (Integer nid : columnTitles.values()) {
            result[j++] = nid;
         }

         return Optional.of(result);
      }
      return Optional.empty();
   }

   /**
    * Helper to mock dynamic info for static columns with or without column label metadata
    * @param assemblageNid
    * @param defaultLabels
    * @param dataTypes
    * @return
    */
   private static DynamicColumnInfo[] mockDynamicColumnFromStatic(int assemblageNid, UUID[] defaultLabels, DynamicDataType[] dataTypes)
   {
      Optional<int[]> columnLabelConcepts = readStaticSemanticInfo(assemblageNid);
      DynamicColumnInfo[] result = new DynamicColumnInfo[defaultLabels.length];
      if (columnLabelConcepts.isPresent()) {
         if (columnLabelConcepts.get().length != defaultLabels.length) {
            throw new RuntimeException("Invalidly constructed static metadata on " + assemblageNid + " bad column labels");
         }
         else {
            for (int col = 0; col < defaultLabels.length; col++) {
               result[col] = new DynamicColumnInfo(Get.identifierService().getUuidPrimordialForNid(assemblageNid),
                     col, Get.identifierService().getUuidPrimordialForNid(columnLabelConcepts.get()[col]),
                     dataTypes[col], null, true, null, null);
            }
         }
      }
      else {
         for (int col = 0; col < defaultLabels.length; col++) {
            result[col] = new DynamicColumnInfo(Get.identifierService().getUuidPrimordialForNid(assemblageNid),
                  col, defaultLabels[col], dataTypes[col], null, true, null, null);
         }
      }
      return result;
   }

   /**
    * Read.
    *
    * @param assemblageNid the assemblage nid or sequence
    * @return the dynamic element usage description
    */
   public static DynamicUsageDescription read(int assemblageNid) {
      // TODO [REFEX] maybe? implement a mechanism to allow the cache to be updated... for now
      // cache is uneditable, and may be wrong, if the user changes the definition of a dynamic element.  Perhaps
      // implement a callback to clear the cache when we know a change of  a certain type happened instead?
      return dynamicCache.get(assemblageNid, nidAgain ->
      {
         logger.trace("Cache miss on DynamicSemanticUsageDescription Cache");
         return new DynamicUsageDescriptionImpl(assemblageNid);
      });
      }

   /**
    * {@inheritDoc}
    */
   @Override
   public DynamicColumnInfo[] getColumnInfo() {
      if (this.refexColumnInfo == null) {
         this.refexColumnInfo = new DynamicColumnInfo[] {};
      }

      return this.refexColumnInfo;
   }

   /**
    * Test if dyn semantic.  Note, this only returns true if it is truely a dynamic semantics.
    * Mocked static semantics will not return true.
    *
    * True responses are cached, but false responses are not.  The Dynamic semantic is fully read and cached, when true.
    *
    * @param assemblageNid the assemblage nid 
    * @return true, if dynamic element
    */
   public static boolean isDynamicSemanticFullRead(int assemblageNid) {
         try {
            read(assemblageNid);
            return true;
         } catch (final Exception e) {
            return false;
         }
   }


   /**
    * An alternative method to determine if a concept defines a dynamic semantic - which will check the cache for positive hits, but does _NOT_ update the cache
    * which is useful to not corrupt the cache for patterns where things happen out-of-order, such as certain loader patterns.
    * This is also faster to check if a concept defines a dynamic semantic, in cases where you don't care about the actual definition.
    * @param assemblageConceptNid
    * @return true if dynamic, false otherwise.
    */
   public static boolean isDynamicSemanticNoRead(int assemblageConceptNid) {

      if (dynamicCache.getIfPresent(assemblageConceptNid) != null) {
         return true;
      }
      final ConceptChronology assemblageConcept = Get.conceptService().getConceptChronology(assemblageConceptNid);
      for (final SemanticChronology descriptionSemantic : assemblageConcept.getConceptDescriptionList()) {
         final LatestVersion<Version> descriptionVersion = ((SemanticChronology) descriptionSemantic).getLatestVersion(StampCoordinates.getDevelopmentLatest());

         if (descriptionVersion.isPresent()) {
            final DescriptionVersion ds = (DescriptionVersion) descriptionVersion.get();

            if (ds.getDescriptionTypeConceptNid() == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
               final Optional<SemanticChronology> nestesdSemantic = Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(ds.getNid(),
                        DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getNid()).findAny();
               if (nestesdSemantic.isPresent()) {
                  return true;
               }
            }
         }
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getDynamicName() {
      return this.name;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getDynamicUsageDescription() {
      return this.semanticUsageDescription;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getDynamicUsageDescriptorNid() {
      return this.refexUsageDescriptorNid;
   }

   /**
    * Gets the referenced component type restriction.
    *
    * @return the referenced component type restriction
    */

   /**
    * {@inheritDoc}
    */
   @Override
   public IsaacObjectType getReferencedComponentTypeRestriction() {
      return this.referencedComponentTypeRestriction;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VersionType getReferencedComponentTypeSubRestriction() {
      return this.referencedComponentTypeSubRestriction;
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public void reset() {
      dynamicCache.invalidateAll();
      staticCache.invalidateAll();
   }

   @Override
   public String toString() {
      return getDynamicName() + "has " + getColumnInfo().length + " columns";
   }
}

