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



package sh.isaac.api.component.sememe;

//~--- JDK imports ------------------------------------------------------------

import java.security.InvalidParameterException;

import java.util.Locale;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.component.sememe.version.ComponentNidSememe;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.component.sememe.version.LongSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.StringSememe;
import sh.isaac.api.observable.sememe.version.ObservableComponentNidSememe;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionSememe;
import sh.isaac.api.observable.sememe.version.ObservableSememeVersion;

//~--- enums ------------------------------------------------------------------

/**
 * The Enum SememeType.
 *
 * @author kec
 */
public enum SememeType {
   /** The member. */
   MEMBER((byte) 0, "Member"),

   /** The component nid. */
   COMPONENT_NID((byte) 1, "Component Nid"),

   /** The long. */
   LONG((byte) 2, "Long"),

   /** The logic graph. */
   LOGIC_GRAPH((byte) 4, "Logic Graph"),

   /** The string. */
   STRING((byte) 5, "String"),

   /** The dynamic. */
   DYNAMIC((byte) 6, "Dynamic Sememe"),

   /** The description. */
   DESCRIPTION((byte) 7, "Description"),

   /** The relationship adaptor. */
   RELATIONSHIP_ADAPTOR((byte) 8, "Relationship Adapter"),

   /** The unknown. */
   UNKNOWN(Byte.MAX_VALUE, "Unknown");

   /** The sememe token. */
   final byte sememeToken;

   /** The nice name. */
   final String niceName;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sememe type.
    *
    * @param sememeToken the sememe token
    * @param niceName the nice name
    */
   private SememeType(byte sememeToken, String niceName) {
      this.sememeToken = sememeToken;
      this.niceName   = niceName;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Parses the.
    *
    * @param nameOrEnumId the name or enum id
    * @param exceptionOnParseFail the exception on parse fail
    * @return the sememe type
    */
   public static SememeType parse(String nameOrEnumId, boolean exceptionOnParseFail) {
      if (nameOrEnumId == null) {
         return null;
      }

      final String clean = nameOrEnumId.toLowerCase(Locale.ENGLISH)
                                       .trim();

      if (StringUtils.isBlank(clean)) {
         return null;
      }

      for (final SememeType ct: values()) {
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
   public static SememeType getFromToken(byte token) {
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
         return ObservableComponentNidSememe.class;

      case DESCRIPTION:
         return ObservableDescriptionSememe.class;

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
         return ComponentNidSememe.class;

      case DESCRIPTION:
         return DescriptionSememe.class;

      case MEMBER:
         return SememeVersion.class;

      case DYNAMIC:
         return DynamicSememe.class;

      case LOGIC_GRAPH:
         return LogicGraphSememe.class;

      case LONG:
         return LongSememe.class;

      case STRING:
         return StringSememe.class;

      default:
         throw new RuntimeException("Can't handle: " + this);
      }
   }
}

