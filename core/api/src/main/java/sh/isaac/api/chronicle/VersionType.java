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

package sh.isaac.api.chronicle;

import java.security.InvalidParameterException;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.ImageVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.observable.semantic.version.ObservableImageVersion;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;

/**
 * The Enum VersionType.
 *
 * @author kec
 */
public enum VersionType {
   MEMBER((byte) 0, "Member", "MBR"),
   COMPONENT_NID((byte) 1, "Component Nid", "REF"),
   LONG((byte) 2, "Long", "INT"),
   LOGIC_GRAPH((byte) 4, "Logic Graph", "DEF"),
   STRING((byte) 5, "String", "STR"),
   DYNAMIC((byte) 6, "Dynamic", "DYN"),
   DESCRIPTION((byte) 7, "Description", "DESC"),
   /* deprecated/removed. */
   //RELATIONSHIP_ADAPTOR((byte) 8, "Relationship Adapter"),
   CONCEPT((byte) 9, "Concept", "CON"),
   RF2_RELATIONSHIP((byte) 10, "RF2 Relationship", "REL"),

   /* deprecated/removed. */
   //LOINC_RECORD((byte) 11, "LOINC Record", "LOINC"),
   IMAGE((byte) 12, "Image", "IMG"),

   // Ideally, all of the below would be represented as dynamic semantics,
   // but quick, removable implementation for now. 
   Nid1_Long2((byte) (Byte.MAX_VALUE - 14), "Component Long", "C1_Long2"),

   MEASURE_CONSTRAINTS((byte) (Byte.MAX_VALUE - 13), "Measure constraints", "Measure constraints"),
   Str1_Nid2_Nid3_Nid4((byte) (Byte.MAX_VALUE - 12), "String Component Component Component", "Str1_C2_C3_C4"),
   Str1_Str2_Nid3_Nid4_Nid5((byte) (Byte.MAX_VALUE - 11), "String String Component Component Component", "Str1_Str2_C3_C4_C5"),
   Nid1_Nid2((byte) (Byte.MAX_VALUE - 10), "Component Component", "C1_C2"),
   Nid1_Nid2_Int3((byte) (Byte.MAX_VALUE - 9), "Component Component Integer", "C1_C2_Int3"),
   Nid1_Nid2_Str3((byte) (Byte.MAX_VALUE - 8), "Component Component String", "C1_C2_Str3"),
   Nid1_Int2((byte) (Byte.MAX_VALUE - 7), "Component Integer", "C1_Int2"),
   Nid1_Str2((byte) (Byte.MAX_VALUE - 6), "Component String", "C1_Str2"),
   Nid1_Int2_Str3_Str4_Nid5_Nid6((byte) (Byte.MAX_VALUE - 5), "Component Integer String String Component Component", "C1_Int2_Str3_Str4_C5_C6"),
   Int1_Int2_Str3_Str4_Str5_Nid6_Nid7((byte) (Byte.MAX_VALUE - 4), "Integer Integer String String String Component Component", "Int1_Int2_Str3_Str4_Str5_C6_C7"),
   Str1_Str2((byte) (Byte.MAX_VALUE - 3), "String String", "Str1_Str2"),
   Str1_Str2_Nid3_Nid4((byte) (Byte.MAX_VALUE - 2), "String String Component Component", "Str1_Str2_C3_C4"),
   Str1_Str2_Str3_Str4_Str5_Str6_Str7((byte) (Byte.MAX_VALUE - 1), "String String String String String String String", "Str1_Str2_Str3_Str4_Str5_Str6_Str7"),
   
   UNKNOWN(Byte.MAX_VALUE, "Unknown", "UNKNOWN");

   final byte versionTypeToken;

   final String niceName;

   /** The what name for use in the what column of tables. */
   final String whatName;

   /**
    * Instantiates a new semantic type.
    *
    * @param versionTypeToken the semantic token
    * @param niceName the nice name
    */
   private VersionType(byte versionTypeToken, String niceName, String whatName) {
      this.versionTypeToken = versionTypeToken;
      this.niceName   = niceName;
      this.whatName   = whatName;
   }

   public String getWhatName() {
      return whatName;
   }

   /**
    * Parses a passed in string or enum id info a {@link VersionType}
    *
    * @param nameOrEnumId the value to parse
    * @param exceptionOnParseFail if true, will throw an exception if the passed in string isn't parseable.
    * @return the parsed {@link VersionType}, or {@link VersionType#UNKNOWN} if exceptionParseOnFail is false 
    * @throws IllegalArgumentException  if the passed in value is null, empty, or unparseable, and exceptionParseOnFail is true
    */
   public static VersionType parse(String nameOrEnumId, boolean exceptionOnParseFail) throws IllegalArgumentException {
      if (nameOrEnumId == null) {
         if (exceptionOnParseFail) {
            throw new IllegalArgumentException("Could not determine VersionType from 'null'");
         }
         return UNKNOWN;
      }

      final String clean = nameOrEnumId.toLowerCase(Locale.ENGLISH).trim();
      
      if (StringUtils.isBlank(clean)) {
         if (exceptionOnParseFail) {
            throw new IllegalArgumentException("Could not determine VersionType from 'null'");
         }
         return UNKNOWN;
      }

      for (final VersionType ct: values()) {
         if (ct.name().toLowerCase(Locale.ENGLISH).equals(clean) ||
                 ct.niceName.toLowerCase(Locale.ENGLISH).equals(clean) ||
                 (ct.ordinal() + "").equals(clean)) {
            return ct;
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("Could not determine VersionType from '" + nameOrEnumId + "'");
      }

      return UNKNOWN;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return this.niceName;
   }

   /**
    * Gets the from token.
    *
    * @param token the token
    * @return the from token
    */
   public static VersionType getFromToken(byte token) {
      switch (token) {
      case 0:
         return MEMBER;

      case 1:
         return COMPONENT_NID;

      case 2:
         return LONG;

      case 4:
         return LOGIC_GRAPH;

      case 5:
         return STRING;

      case 6:
         return DYNAMIC;

      case 7:
         return DESCRIPTION;

      case 9:
         return CONCEPT;

      case 10:
         return RF2_RELATIONSHIP;

      case 12:
          return IMAGE;
         
   // Ideally, all of the below would be represented as dynamic semantics, 
   // but quick, removable implementation for now. 
      case Byte.MAX_VALUE - 14:
         return Nid1_Long2;

      case Byte.MAX_VALUE - 13:
         return MEASURE_CONSTRAINTS;

      case Byte.MAX_VALUE - 12:
         return Str1_Nid2_Nid3_Nid4;

      case Byte.MAX_VALUE - 11:
         return Str1_Str2_Nid3_Nid4_Nid5;

      case Byte.MAX_VALUE - 10:
         return Nid1_Nid2;

      case Byte.MAX_VALUE - 9:
         return Nid1_Nid2_Int3;

      case Byte.MAX_VALUE - 8:
         return Nid1_Nid2_Str3;

      case Byte.MAX_VALUE - 7:
         return Nid1_Int2;

      case Byte.MAX_VALUE - 6:
         return Nid1_Str2;

      case Byte.MAX_VALUE - 5:
         return Nid1_Int2_Str3_Str4_Nid5_Nid6;

      case Byte.MAX_VALUE - 4:
         return Int1_Int2_Str3_Str4_Str5_Nid6_Nid7;

      case Byte.MAX_VALUE - 3:
         return Str1_Str2;

      case Byte.MAX_VALUE - 2:
         return Str1_Str2_Nid3_Nid4;

      case Byte.MAX_VALUE - 1:
         return Str1_Str2_Str3_Str4_Str5_Str6_Str7;
         
      default:
         return UNKNOWN;
      }
   }

   /**
    * Gets the observable semantic version class.
    *
    * @return the observable semantic version class
    */
      public Class<? extends ObservableSemanticVersion> getObservableSemanticVersionClass() {
      switch (this) {
      case COMPONENT_NID:
         return ObservableComponentNidVersion.class;

      case DESCRIPTION:
         return ObservableDescriptionVersion.class;

      case MEMBER:
         return ObservableSemanticVersion.class;

      case LONG:
         return ObservableLongVersion.class;

      case STRING:
         return ObservableStringVersion.class;

      case IMAGE:
         return ObservableImageVersion.class;
      
      // TODO implement Observable pattern
      case DYNAMIC:
      case LOGIC_GRAPH:
      default:
         throw new RuntimeException("No Observable class available for " + this);
      }
   }

   /**
    * Gets the version type token.
    *
    * @return the version type token
    */
   public byte getVersionTypeToken() {
      return this.versionTypeToken;
   }

   /**
    * Gets the version class.
    *
    * @return the version class
    */
      public Class<? extends SemanticVersion> getVersionClass() {
      switch (this) {
      case COMPONENT_NID:
         return ComponentNidVersion.class;

      case DESCRIPTION:
         return DescriptionVersion.class;

      case MEMBER:
         return SemanticVersion.class;

      case DYNAMIC:
         return DynamicVersion.class;

      case LOGIC_GRAPH:
         return LogicGraphVersion.class;

      case LONG:
         return LongVersion.class;

      case STRING:
         return StringVersion.class;

      case IMAGE:
          return ImageVersion.class;

      default:
         throw new RuntimeException("No Version Class avaiable for: " + this);
      }
   }
}

