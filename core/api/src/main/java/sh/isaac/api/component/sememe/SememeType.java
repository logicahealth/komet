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
 *
 * @author kec
 */
public enum SememeType {
   MEMBER((byte) 0, "Member"),
   COMPONENT_NID((byte) 1, "Component Nid"),
   LONG((byte) 2, "Long"),
   LOGIC_GRAPH((byte) 4, "Logic Graph"),
   STRING((byte) 5, "String"),
   DYNAMIC((byte) 6, "Dynamic Sememe"),
   DESCRIPTION((byte) 7, "Description"),
   RELATIONSHIP_ADAPTOR((byte) 8, "Relationship Adapter"),
   UNKNOWN(Byte.MAX_VALUE, "Unknown");

   final byte   sememeToken;
   final String niceName_;

   //~--- constructors --------------------------------------------------------

   private SememeType(byte sememeToken, String niceName) {
      this.sememeToken = sememeToken;
      this.niceName_   = niceName;
   }

   //~--- methods -------------------------------------------------------------

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
               ct.niceName_.toLowerCase(Locale.ENGLISH).equals(clean) ||
               (ct.ordinal() + "").equals(clean)) {
            return ct;
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("Could not determine SememeType from " + nameOrEnumId);
      }

      return UNKNOWN;
   }

   @Override
   public String toString() {
      return this.niceName_;
   }

   //~--- get methods ---------------------------------------------------------

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

   public byte getSememeToken() {
      return this.sememeToken;
   }

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

