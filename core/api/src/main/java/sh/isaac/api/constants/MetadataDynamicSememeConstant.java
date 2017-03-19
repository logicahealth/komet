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

import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;

//~--- classes ----------------------------------------------------------------

public class MetadataDynamicSememeConstant
        extends MetadataConceptConstant {
   private String                    dynamicSememeDescription_;
   private DynamicSememeColumnInfo[] dynamicSememeColumns_;
   private ObjectChronologyType      referencedComponentRestriction_;
   private SememeType                referencedComponentSubRestriction_;

   //~--- constructors --------------------------------------------------------

   /**
    * @param fsn
    * @param uuid - optional - the UUID to assign to this sememe
    * @param sememeDescription - describe the purpose of the use of this dynamic sememe
    * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
    */
   public MetadataDynamicSememeConstant(String fsn,
         UUID uuid,
         String sememeDescription,
         DynamicSememeColumnInfo[] columns) {
      this(fsn, uuid, sememeDescription, columns, null, null, null, null);
   }

   /**
    * @param fsn
    * @param uuid - optional - the UUID to assign to this sememe
    * @param sememeDescription - describe the purpose of the use of this dynamic sememe
    * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
    * @param synonyms - optional - extra synonyms
    */
   public MetadataDynamicSememeConstant(String fsn,
         UUID uuid,
         String sememeDescription,
         DynamicSememeColumnInfo[] columns,
         String[] synonyms) {
      this(fsn, uuid, sememeDescription, columns, synonyms, null, null, null);
   }

   /**
    * @param fsn
    * @param uuid - optional - the UUID to assign to this sememe
    * @param alternateName - optional - the non-preferred synonym to add to this concept
    * @param sememeDescription - describe the purpose of the use of this dynamic sememe
    * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
    */
   public MetadataDynamicSememeConstant(String fsn,
         UUID uuid,
         String alternateName,
         String sememeDescription,
         DynamicSememeColumnInfo[] columns) {
      this(fsn, uuid, sememeDescription, columns, new String[] { alternateName }, null, null, null);
   }

   /**
    * @param fsn
    * @param uuid - optional - the UUID to assign to this sememe
    * @param sememeDescription - describe the purpose of the use of this dynamic sememe
    * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
    * @param synonyms - optional - extra synonyms
    * @param definitions - optional - extra definitions
    * @param requiresIndex - optional - used to specify that this particular DynamicSememe should always be indexed.  If null or empty - no indexing will
    * be performed.  The Integer array should be something like "new Integer[]{0, 2, 3}" - where the 0 indexed values correspond to the columns that
    * should also be indexed.
    */
   public MetadataDynamicSememeConstant(String fsn,
         UUID uuid,
         String sememeDescription,
         DynamicSememeColumnInfo[] columns,
         String[] synonyms,
         String[] definitions,
         Integer[] requiresIndex) {
      this(fsn, uuid, sememeDescription, columns, synonyms, definitions, null, null);
   }

   /**
    * @param fsn
    * @param preferredSynonym
    * @param uuid - optional - the UUID to assign to this sememe
    * @param sememeDescription - describe the purpose of the use of this dynamic sememe
    * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
    * @param synonyms - optional - extra synonyms
    * @param definitions - optional - extra definitions
    * @param referencedComponentRestriction - optional - used to limit the type of nid that can be used as the referenced component in an instance
    * of this sememe.
    * @param referencedComponentSubRestriction - optional - used to limit the type of sememe that can be used as the referenced component in an instance
    * of this sememe.
    */
   public MetadataDynamicSememeConstant(String fsn,
         UUID uuid,
         String sememeDescription,
         DynamicSememeColumnInfo[] columns,
         String[] synonyms,
         String[] definitions,
         ObjectChronologyType referencedComponentRestriction,
         SememeType refererenceComponentSubRestriction) {
      super(fsn, uuid);

      if (definitions != null) {
         for (String s: definitions) {
            addDefinition(s);
         }
      }

      if (synonyms != null) {
         for (String s: synonyms) {
            addSynonym(s);
         }
      }

      dynamicSememeDescription_          = sememeDescription;
      dynamicSememeColumns_              = columns;
      referencedComponentRestriction_    = referencedComponentRestriction;
      referencedComponentSubRestriction_ = refererenceComponentSubRestriction;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the sememeColumns
    */
   public DynamicSememeColumnInfo[] getDynamicSememeColumns() {
      return dynamicSememeColumns_;
   }

   /**
    * @return The limit (if any) on which {@link SememeType} this dynamic sememe is restricted to.
    */
   public SememeType getReferencedComponentSubTypeRestriction() {
      return referencedComponentSubRestriction_;
   }

   /**
    * @return The limit (if any) on which {@link ObjectChronologyType} this DynamicSememe is restricted to.
    */
   public ObjectChronologyType getReferencedComponentTypeRestriction() {
      return referencedComponentRestriction_;
   }

   /**
    * @return the dynamicSememeDescription_
    */
   public String getSememeAssemblageDescription() {
      return dynamicSememeDescription_;
   }
}

