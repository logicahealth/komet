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

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.MutableDescriptionVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.util.StringUtils;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicBooleanImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * {@link DynamicUtility}
 *
 * Convenience methods related to DynamicSemantics.  Implemented as an interface and a singleton to provide
 * lower level code with access to these methods at runtime via HK2.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DynamicUtilityImpl
         implements DynamicUtility {

   /**
    * Configure dynamic element definition data for column.
    *
    * @param ci the ci
    * @return the dynamic element data[]
    */
   @Override
   public DynamicData[] configureDynamicDefinitionDataForColumn(DynamicColumnInfo ci) {
      final DynamicData[] data = new DynamicData[7];

      data[0] = new DynamicIntegerImpl(ci.getColumnOrder());
      data[1] = new DynamicUUIDImpl(ci.getColumnDescriptionConcept());

      if (DynamicDataType.UNKNOWN == ci.getColumnDataType()) {
         throw new RuntimeException("Error in column - if default value is provided, the type cannot be polymorphic");
      }

      data[2] = new DynamicStringImpl(ci.getColumnDataType().name());
      data[3] = convertPolymorphicDataColumn(ci.getDefaultColumnValue(), ci.getColumnDataType());
      data[4] = new DynamicBooleanImpl(ci.isColumnRequired());

      if (ci.getValidator() != null) {
         final DynamicString[] validators = new DynamicString[ci.getValidator().length];

         for (int i = 0; i < validators.length; i++) {
            validators[i] = new DynamicStringImpl(ci.getValidator()[i].name());
         }

         data[5] = new DynamicArrayImpl<>(validators);
      } else {
         data[5] = null;
      }

      if (ci.getValidatorData() != null) {
         final DynamicData[] validatorData = new DynamicData[ci.getValidatorData().length];

         for (int i = 0; i < validatorData.length; i++) {
            validatorData[i] = convertPolymorphicDataColumn(ci.getValidatorData()[i],
                  ci.getValidatorData()[i]
                    .getDynamicDataType());
         }

         data[6] = new DynamicArrayImpl<>(validatorData);
      } else {
         data[6] = null;
      }

      return data;
   }

   /**
    * Configure dynamic element restriction data.
    *
    * @param referencedComponentRestriction the referenced component restriction
    * @param referencedComponentSubRestriction the referenced component sub restriction
    * @return the dynamic element data[]
    */
   @Override
   public DynamicData[] configureDynamicRestrictionData(IsaacObjectType referencedComponentRestriction,
         VersionType referencedComponentSubRestriction) {
      if ((referencedComponentRestriction != null) &&
            (IsaacObjectType.UNKNOWN != referencedComponentRestriction)) {
         int size = 1;

         if ((referencedComponentSubRestriction != null) && (VersionType.UNKNOWN != referencedComponentSubRestriction)) {
            size = 2;
         }

         final DynamicData[] data = new DynamicData[size];

         data[0] = new DynamicStringImpl(referencedComponentRestriction.name());

         if (size == 2) {
            data[1] = new DynamicStringImpl(referencedComponentSubRestriction.name());
         }

         return data;
      }

      return null;
   }

   /**
    * Creates the dynamic string data.
    *
    * @param value the value
    * @return the dynamic element string
    */
   @Override
   public DynamicString createDynamicStringData(String value) {
      return new DynamicStringImpl(value);
   }

   /**
    * Creates the dynamic UUID data.
    *
    * @param value the value
    * @return the dynamic element UUID
    */
   @Override
   public DynamicUUID createDynamicUUIDData(UUID value) {
      return new DynamicUUIDImpl(value);
   }

   @Override
   public DynamicArray<DynamicString> createDynamicStringArrayData(String... values)
   {
      DynamicString[] temp = new DynamicString[values.length];
      for (int i = 0; i < values.length; i++) {
         temp[i] = new DynamicStringImpl(values[i]);
      }
      return new DynamicArrayImpl<DynamicString>(temp);
   }

/**
    * Read the {@link DynamicUsageDescription} for the specified assemblage concept.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @return the dynamic element usage description
    */
   @Override
   public DynamicUsageDescription readDynamicUsageDescription(int assemblageNidOrSequence) {
      return DynamicUsageDescriptionImpl.read(assemblageNidOrSequence);
   }
   
   
   /**
    * {@inheritDoc}
    */
   @Override
   public List<Chronology> configureConceptAsDynamicSemantic(int conceptNid, String semanticDescription, DynamicColumnInfo[] columns,
         IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction, int stampSequence) {
      if (StringUtils.isBlank(semanticDescription)) {
         throw new RuntimeException("Semantic description is required");
      }

      ArrayList<Chronology> builtSemantics = new ArrayList<>();

      // Add the special synonym to establish this as an assemblage concept
      // will specify all T5 uuids in our own namespace, to make sure we don't get dupe UUIDs while still being consistent

      final DescriptionBuilderService descriptionBuilderService = LookupService.getService(DescriptionBuilderService.class);

      DescriptionBuilder<SemanticChronology, ? extends MutableDescriptionVersion> definitionBuilder = descriptionBuilderService
            .getDescriptionBuilder(semanticDescription, conceptNid, TermAux.DEFINITION_DESCRIPTION_TYPE, TermAux.ENGLISH_LANGUAGE);
      definitionBuilder.addPreferredInDialectAssemblage(TermAux.US_DIALECT_ASSEMBLAGE);
      definitionBuilder.setT5UuidNested(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid());

      definitionBuilder.build(stampSequence, builtSemantics);

      Get.semanticBuilderService()
            .getDynamicBuilder(definitionBuilder, DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getNid(), null)
            .setT5UuidNested(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid()).build(stampSequence, builtSemantics);

      // define the data columns (if any)
      if (columns != null) {
         // Ensure that we process in column order - we don't always keep track of that later - we depend on the data being stored in the right
         // order.
         final TreeSet<DynamicColumnInfo> sortedColumns = new TreeSet<>(Arrays.asList(columns));

         for (final DynamicColumnInfo ci : sortedColumns) {
            final DynamicData[] data = configureDynamicDefinitionDataForColumn(ci);
            Get.semanticBuilderService().getDynamicBuilder(conceptNid, DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION.getNid(), data)
                     .setT5UuidNested(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid()).build(stampSequence, builtSemantics);
         }
      }

      final DynamicData[] data = configureDynamicRestrictionData(referencedComponentTypeRestriction, referencedComponentTypeSubRestriction);

      if (data != null) {
            Get.semanticBuilderService().getDynamicBuilder(conceptNid, DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getNid(), data)
                  .setT5UuidNested(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid()).build(stampSequence, builtSemantics);
      }
      return builtSemantics;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SemanticChronology[] configureConceptAsDynamicSemantic(int conceptNid, String semanticDescription, DynamicColumnInfo[] columns,
         IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction, EditCoordinate editCoord) {
      if (StringUtils.isBlank(semanticDescription)) {
         throw new RuntimeException("Semantic description is required");
      }

      final EditCoordinate localEditCoord = ((editCoord == null) ? 
            Get.configurationService().getUserConfiguration(Optional.empty()).getEditCoordinate()
            : editCoord);

      ArrayList<SemanticChronology> builtSemantics = new ArrayList<>();

      // Add the special synonym to establish this as an assemblage concept
      // will specify all T5 uuids in our own namespace, to make sure we don't get dupe UUIDs while still being consistent
      
      final DescriptionBuilderService descriptionBuilderService = LookupService.getService(DescriptionBuilderService.class);

      DescriptionBuilder<SemanticChronology, ? extends MutableDescriptionVersion> definitionBuilder = descriptionBuilderService
            .getDescriptionBuilder(semanticDescription, conceptNid, TermAux.DEFINITION_DESCRIPTION_TYPE, TermAux.ENGLISH_LANGUAGE);
      definitionBuilder.addPreferredInDialectAssemblage(TermAux.US_DIALECT_ASSEMBLAGE);
      definitionBuilder.setT5Uuid(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid(), null);

      final SemanticChronology definitionSemantic = definitionBuilder.build(localEditCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
      builtSemantics.add(definitionSemantic);

      builtSemantics.add(Get.semanticBuilderService()
            .getDynamicBuilder(definitionSemantic.getNid(), DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getNid(), null)
            .setT5Uuid(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid(), null)
            .build(localEditCoord, ChangeCheckerMode.ACTIVE).getNoThrow());

      // define the data columns (if any)
      if (columns != null) {
         // Ensure that we process in column order - we don't always keep track of that later - we depend on the data being stored in the right
         // order.
         final TreeSet<DynamicColumnInfo> sortedColumns = new TreeSet<>(Arrays.asList(columns));

         for (final DynamicColumnInfo ci : sortedColumns) {
            final DynamicData[] data = configureDynamicDefinitionDataForColumn(ci);
            builtSemantics
                  .add(Get.semanticBuilderService().getDynamicBuilder(conceptNid, DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION.getNid(), data)
                        .setT5Uuid(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid(), null)
                        .build(localEditCoord, ChangeCheckerMode.ACTIVE).getNoThrow());
         }
      }

      final DynamicData[] data = configureDynamicRestrictionData(referencedComponentTypeRestriction, referencedComponentTypeSubRestriction);

      if (data != null) {
         builtSemantics.add(
               Get.semanticBuilderService().getDynamicBuilder(conceptNid, DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getNid(), data)
                  .setT5Uuid(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid(), null)
                  .build(localEditCoord, ChangeCheckerMode.ACTIVE).getNoThrow());
      }
      return builtSemantics.toArray(new SemanticChronology[builtSemantics.size()]);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public ArrayList<Chronology> buildUncommittedNewDynamicSemanticColumnInfoConcept(String columnName, String columnDescription, 
         EditCoordinate editCoordinate, UUID[] extraParents) {
         if (StringUtils.isBlank(columnName)) {
            throw new RuntimeException("Column name is required");
         }

         final DescriptionBuilderService descriptionBuilderService = LookupService.getService(DescriptionBuilderService.class);
         final LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();

         ArrayList<Assertion> assertions = new ArrayList<>();
         assertions.add(ConceptAssertion(Get.conceptService().getConceptChronology(DynamicConstants.get().DYNAMIC_COLUMNS.getNid()), defBuilder));
         if (extraParents != null) {
            for (UUID parent : extraParents) {
               assertions.add(ConceptAssertion(Get.conceptService().getConceptChronology(parent), defBuilder));
            }
         }

         NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));

         final LogicalExpression parentDef = defBuilder.build();
         
         final ConceptBuilder builder = Get.conceptBuilderService().getConceptBuilder(columnName, ConceptProxy.METADATA_SEMANTIC_TAG,
              parentDef, 
              TermAux.ENGLISH_LANGUAGE,
              TermAux.US_DIALECT_ASSEMBLAGE,
              Get.configurationService().getGlobalDatastoreConfiguration().getDefaultLogicCoordinate(),
              TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
         
         StringBuilder temp = new StringBuilder();
         temp.append(columnName);
         temp.append(DynamicConstants.get().DYNAMIC_COLUMNS.getPrimordialUuid().toString());
         if (extraParents != null) {
            for (UUID u : extraParents)
            {
               temp.append(u.toString());
            }
         }
         
         builder.setPrimordialUuid(UuidT5Generator.get(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid(), temp.toString()));
         
         if (StringUtils.isNotBlank(columnDescription)) {
            DescriptionBuilder<?, ?> definitionBuilder = descriptionBuilderService.getDescriptionBuilder(columnDescription,
                    builder, TermAux.DEFINITION_DESCRIPTION_TYPE,
                    TermAux.ENGLISH_LANGUAGE);
                definitionBuilder.addPreferredInDialectAssemblage(TermAux.US_DIALECT_ASSEMBLAGE);
                builder.addDescription(definitionBuilder);
         }
         ArrayList<Chronology> builtObjects = new ArrayList<>();
         
         for (SemanticBuilder<?> s : builder.getSemanticBuilders()) {
            s.setT5Uuid(DynamicConstants.get().DYNAMIC_NAMESPACE.getPrimordialUuid(), null);
         }

         builder.build(editCoordinate == null ? Get.configurationService().getGlobalDatastoreConfiguration().getDefaultEditCoordinate() : editCoordinate,
               ChangeCheckerMode.ACTIVE, builtObjects).getNoThrow();

         return builtObjects;
      }

   /**
    * To string.
    *
    * @param data DynamicData[]
    * @return the string
    */
   public static String toString(DynamicData[] data) {
      final StringBuilder sb = new StringBuilder();

      sb.append("[");

      if (data != null) {
         for (final DynamicData dsd: data) {
            if (dsd != null) {
               sb.append(dsd.dataToString());
            }

            sb.append(", ");
         }

         if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
         }
      }

      sb.append("]");
      return sb.toString();
   }

   /**
    * Convert polymorphic data column.
    *
    * @param defaultValue the default value
    * @param columnType the column type
    * @return the dynamic element data
    */
   private static DynamicData convertPolymorphicDataColumn(DynamicData defaultValue,
         DynamicDataType columnType) {
      DynamicData result;

      if (defaultValue != null) {
         try {
            if (DynamicDataType.BOOLEAN == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.BYTEARRAY == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.DOUBLE == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.FLOAT == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.INTEGER == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.LONG == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.NID == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.STRING == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.UUID == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.ARRAY == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.POLYMORPHIC == columnType) {
               throw new RuntimeException(
                   "Error in column - if default value is provided, the type cannot be polymorphic");
            } else {
               throw new RuntimeException("Actually, the implementation is broken.  Ooops.");
            }
         } catch (final ClassCastException e) {
            throw new RuntimeException(
                "Error in column - if default value is provided, the type must be compatible with the the column descriptor type");
         }
      } else {
         result = null;
      }

      return result;
   }
}

