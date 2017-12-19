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



package sh.isaac.model.observable.version.brittle;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Str1_Str2_Nid3_Nid4_Version;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableSemanticVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_Nid3_Nid4_VersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Observable_Str1_Str2_Nid3_Nid4_VersionImpl
        extends ObservableSemanticVersionImpl
         implements Observable_Str1_Str2_Nid3_Nid4_Version {
   StringProperty  str1Property;
   StringProperty  str2Property;
   IntegerProperty nid3Property;
   IntegerProperty nid4Property;

   //~--- constructors --------------------------------------------------------

   public Observable_Str1_Str2_Nid3_Nid4_VersionImpl(SemanticVersion stampedVersion,
         ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty nid3Property() {
      if (this.nid3Property == null) {
         this.nid3Property = new CommitAwareIntegerProperty(this, ObservableFields.NID3.toExternalString(), getNid3());
         this.nid3Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Nid3_Nid4_Version().setNid3(newValue.intValue());
             });
      }

      return this.nid3Property;
   }

   @Override
   public IntegerProperty nid4Property() {
      if (this.nid4Property == null) {
         this.nid4Property = new CommitAwareIntegerProperty(this, ObservableFields.NID4.toExternalString(), getNid4());
         this.nid4Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Nid3_Nid4_Version().setNid4(newValue.intValue());
             });
      }

      return this.nid4Property;
   }

   @Override
   public StringProperty str1Property() {
      if (this.str1Property == null) {
         this.str1Property = new CommitAwareStringProperty(this, ObservableFields.STR1.toExternalString(), getStr1());
         this.str1Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Nid3_Nid4_Version().setStr1(newValue);
             });
      }

      return this.str1Property;
   }

   @Override
   public StringProperty str2Property() {
      if (this.str2Property == null) {
         this.str2Property = new CommitAwareStringProperty(this, ObservableFields.STR2.toExternalString(), getStr2());
         this.str2Property.addListener(
             (observable, oldValue, newValue) -> {
                getStr1_Str2_Nid3_Nid4_Version().setStr2(newValue);
             });
      }

      return this.str2Property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid3() {
      if (this.nid3Property != null) {
         return this.nid3Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Version().getNid3();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid3(int nid) {
      if (this.nid3Property != null) {
         this.nid3Property.set(nid);
      }

      getStr1_Str2_Nid3_Nid4_Version().setNid3(nid);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid4() {
      if (this.nid4Property != null) {
         return this.nid4Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Version().getNid4();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid4(int nid) {
      if (this.nid4Property != null) {
         this.nid4Property.set(nid);
      }

      getStr1_Str2_Nid3_Nid4_Version().setNid4(nid);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr1() {
      if (this.str1Property != null) {
         return this.str1Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Version().getStr1();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setStr1(String value) {
      if (this.str1Property != null) {
         this.str1Property.set(value);
      }

      getStr1_Str2_Nid3_Nid4_Version().setStr1(value);
   }

   //~--- get methods ---------------------------------------------------------

   private Str1_Str2_Nid3_Nid4_VersionImpl getStr1_Str2_Nid3_Nid4_Version() {
      return (Str1_Str2_Nid3_Nid4_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public String getStr2() {
      if (this.str2Property != null) {
         return this.str2Property.get();
      }

      return getStr1_Str2_Nid3_Nid4_Version().getStr2();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setStr2(String value) {
      if (this.str2Property != null) {
         this.str2Property.set(value);
      }

      getStr1_Str2_Nid3_Nid4_Version().setStr2(value);
   }
}

