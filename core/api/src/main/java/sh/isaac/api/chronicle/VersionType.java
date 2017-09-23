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

//~--- JDK imports ------------------------------------------------------------

import java.security.InvalidParameterException;

import java.util.Locale;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.observable.sememe.version.ObservableSememeVersion;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionVersion;
import sh.isaac.api.component.sememe.version.ComponentNidVersion;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.LongVersion;
import sh.isaac.api.component.sememe.version.StringVersion;
import sh.isaac.api.observable.sememe.version.ObservableComponentNidVersion;

//~--- enums ------------------------------------------------------------------

/**
 * The Enum VersionType.
 *
 * @author kec
 */
public enum VersionType {
   /** The member. */
   MEMBER((byte) 0, "Member", "MEMBER"),

   /** A component nid version*/
   COMPONENT_NID((byte) 1, "Component Nid", "COMPONENT"),

   /** A long version. */
   LONG((byte) 2, "Long", "INT"),

   /** A logic graph version. */
   LOGIC_GRAPH((byte) 4, "Logic Graph", "DEF"),

   /** A string version. */
   STRING((byte) 5, "String", "STR"),

   /** A dynamic version. */
   DYNAMIC((byte) 6, "Dynamic Sememe", "DYNAMIC"),

   /** A description version. */
   DESCRIPTION((byte) 7, "Description", "DESC"),

   /* A relationship adaptor version -- deprecated/removed. */
   //RELATIONSHIP_ADAPTOR((byte) 8, "Relationship Adapter"),
   
   /** A concept version */
   CONCEPT((byte) 9, "Concept", "CONCEPT"),

   /** An unknown type of version. */
   UNKNOWN(Byte.MAX_VALUE, "Unknown", "UNKNOWN");

   /** The sememe token. */
   final byte sememeToken;

   /** The nice name. */
   final String niceName;

   /** The what name for use in the what column of tables. */
   final String whatName;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sememe type.
    *
    * @param sememeToken the sememe token
    * @param niceName the nice name
    */
   private VersionType(byte sememeToken, String niceName, String whatName) {
      this.sememeToken = sememeToken;
      this.niceName   = niceName;
      this.whatName   = whatName;
   }

   //~--- methods -------------------------------------------------------------

   public String getWhatName() {
      return whatName;
   }

   /**
    * Parses the.
    *
    * @param nameOrEnumId the name or enum id
    * @param exceptionOnParseFail the exception on parse fail
    * @return the sememe type
    */
   public static VersionType parse(String nameOrEnumId, boolean exceptionOnParseFail) {
      if (nameOrEnumId == null) {
         return null;
      }

      final String clean = nameOrEnumId.toLowerCase(Locale.ENGLISH)
              .trim();
      
      if (StringUtils.isBlank(clean)) {
         return null;
      }

      for (final VersionType ct: values()) {
         if (ct.name().toLowerCase(Locale.ENGLISH).equals(clean) ||
                 ct.niceName.toLowerCase(Locale.ENGLISH).equals(clean) ||
                 (ct.ordinal() + "").equals(clean)) {
            return ct;
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("Could not determine SememeType from " + nameOrEnumId);
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

   //~--- get methods ---------------------------------------------------------

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

      default:
         throw new UnsupportedOperationException("Can't handle: " + token);
      }
   }

   /**
    * Gets the observable sememe version class.
    *
    * @return the observable sememe version class
    */
   @SuppressWarnings("rawtypes")
   public Class<? extends ObservableSememeVersion> getObservableSememeVersionClass() {
      switch (this) {
      case COMPONENT_NID:
         return ObservableComponentNidVersion.class;

      case DESCRIPTION:
         return ObservableDescriptionVersion.class;

      case MEMBER:
         return ObservableSememeVersion.class;

      case DYNAMIC:

      // TODO implement Observable pattern
      case LOGIC_GRAPH:

      // TODO implement Observable pattern
      case LONG:

      // TODO implement Observable pattern
      case STRING:

      // TODO implement Observable pattern
      default:
         throw new RuntimeException("Can't handle: " + this);
      }
   }

   /**
    * Gets the sememe token.
    *
    * @return the sememe token
    */
   public byte getSememeToken() {
      return this.sememeToken;
   }

   /**
    * Gets the sememe version class.
    *
    * @return the sememe version class
    */
   @SuppressWarnings("rawtypes")
   public Class<? extends SememeVersion> getSememeVersionClass() {
      switch (this) {
      case COMPONENT_NID:
         return ComponentNidVersion.class;

      case DESCRIPTION:
         return DescriptionVersion.class;

      case MEMBER:
         return SememeVersion.class;

      case DYNAMIC:
         return DynamicSememe.class;

      case LOGIC_GRAPH:
         return LogicGraphVersion.class;

      case LONG:
         return LongVersion.class;

      case STRING:
         return StringVersion.class;

      default:
         throw new RuntimeException("Can't handle: " + this);
      }
   }
}

