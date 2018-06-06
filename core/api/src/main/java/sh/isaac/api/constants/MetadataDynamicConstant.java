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



package sh.isaac.api.constants;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.externalizable.IsaacObjectType;

//~--- classes ----------------------------------------------------------------

/**
 * The Class MetadataDynamicSemanticConstant.
 */
public class MetadataDynamicConstant
        extends MetadataConceptConstant {
   /** The dynamic description. */
   private final String dynamicDescription;

   /** The dynamic columns. */
   private final DynamicColumnInfo[] dynamicColumns;

   /** The referenced component restriction. */
   private final IsaacObjectType referencedComponentRestriction;

   /** The referenced component sub restriction. */
   private final VersionType referencedComponentSubRestriction;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new metadata dynamic constant.
    *
    * @param fqn the fully qualified name
    * @param uuid - optional - the UUID to assign to this semantic
    * @param description - describe the purpose of the use of this dynamic element
    * @param columns - The definitions of the attached data columns that are allowed  (may be empty)
    */
   public MetadataDynamicConstant(String fqn,
         UUID uuid,
         String description,
         DynamicColumnInfo[] columns) {
      this(fqn, uuid, description, columns, null, null, null, null);
   }

   /**
    * Instantiates a new metadata dynamic constant.
    *
    * @param fqn the fully qualified name
    * @param uuid - optional - the UUID to assign to this semantic
    * @param description - describe the purpose of the use of this dynamic element
    * @param columns - The definitions of the attached data columns that are allowed (may be empty)
    * @param synonyms - optional - extra synonyms
    */
   public MetadataDynamicConstant(String fqn,
         UUID uuid,
         String description,
         DynamicColumnInfo[] columns,
         String[] synonyms) {
      this(fqn, uuid, description, columns, synonyms, null, null, null);
   }

   /**
    * Instantiates a new metadata dynamic constant.
    *
    * @param fqn the fully qualified name
    * @param uuid - optional - the UUID to assign to this semantic
    * @param alternateName - optional - the non-preferred synonym to add to this concept
    * @param description - describe the purpose of the use of this dynamic element
    * @param columns - The definitions of the attached data columns that are allowed on this dynamic (may be empty)
    */
   public MetadataDynamicConstant(String fqn,
         UUID uuid,
         String alternateName,
         String description,
         DynamicColumnInfo[] columns) {
      this(fqn, uuid, description, columns, new String[] { alternateName }, null, null, null);
   }

   /**
    * Instantiates a new metadata dynamic constant.
    *
    * @param fqn the Fully qualified name
    * @param uuid - optional - the UUID to assign to this semantic
    * @param description - describe the purpose of the use of this dynamic element
    * @param columns - The definitions of the attached data columns that are allowed on this dynamic (may be empty)
    * @param synonyms - optional - extra synonyms
    * @param definitions - optional - extra definitions
    * @param requiresIndex - optional - used to specify that this particular DynamicDynamic should always be indexed.  If null or empty - no indexing will
    * be performed.  The Integer array should be something like "new Integer[]{0, 2, 3}" - where the 0 indexed values correspond to the columns that
    * should also be indexed.
    */
   public MetadataDynamicConstant(String fqn,
         UUID uuid,
         String description,
         DynamicColumnInfo[] columns,
         String[] synonyms,
         String[] definitions,
         Integer[] requiresIndex) {
      this(fqn, uuid, description, columns, synonyms, definitions, null, null);
   }

   /**
    * Instantiates a new metadata dynamic constant.
    *
    * @param fqn the fully qualified name
    * @param uuid - optional - the UUID to assign to this semantic
    * @param description - describe the purpose of the use of this dynamic element
    * @param columns - The definitions of the attached data columns that are allowed on this dynamic (may be empty)
    * @param synonyms - optional - extra synonyms
    * @param definitions - optional - extra definitions
    * @param referencedComponentRestriction - optional - used to limit the type of nid that can be used as the referenced component in an instance
    * of this semantic.
    * @param refererenceComponentSubRestriction the reference component sub restriction
    */
   public MetadataDynamicConstant(String fqn,
         UUID uuid,
         String description,
         DynamicColumnInfo[] columns,
         String[] synonyms,
         String[] definitions,
         IsaacObjectType referencedComponentRestriction,
         VersionType refererenceComponentSubRestriction) {
      super(fqn, uuid);

      if (definitions != null) {
         for (final String s: definitions) {
            addDefinition(s);
         }
      }

      if (synonyms != null) {
         for (final String s: synonyms) {
            addSynonym(s);
         }
      }

      this.dynamicDescription          = description;
      this.dynamicColumns              = columns;
      this.referencedComponentRestriction    = referencedComponentRestriction;
      this.referencedComponentSubRestriction = refererenceComponentSubRestriction;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the dynamic columns.
    *
    * @return the semanticColumns
    */
   public DynamicColumnInfo[] getDynamicColumns() {
      return this.dynamicColumns;
   }

   /**
    * Gets the referenced component sub type restriction.
    *
    * @return The limit (if any) on which {@link VersionType} this dynamic is restricted to.
    */
   public VersionType getReferencedComponentSubTypeRestriction() {
      return this.referencedComponentSubRestriction;
   }

   /**
    * Gets the referenced component type restriction.
    *
    * @return The limit (if any) on which {@link IsaacObjectType} this DynamicDynamic is restricted to.
    */
   public IsaacObjectType getReferencedComponentTypeRestriction() {
      return this.referencedComponentRestriction;
   }

   /**
    * Gets the dynamic assemblage description.
    *
    * @return the dynamicSemanticDescription_
    */
   public String getAssemblageDescription() {
      return this.dynamicDescription;
   }
}

