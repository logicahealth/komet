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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;

import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Nid2_Str3_Version;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableSemanticVersionImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Nid2_Str3_VersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Observable_Nid1_Nid2_Str3_VersionImpl
        extends ObservableSemanticVersionImpl
         implements Observable_Nid1_Nid2_Str3_Version {
   IntegerProperty nid1Property;
   IntegerProperty nid2Property;
   StringProperty  str3Property;

   //~--- constructors --------------------------------------------------------

   public Observable_Nid1_Nid2_Str3_VersionImpl(SemanticVersion stampedVersion,
         ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty nid1Property() {
      if (this.nid1Property == null) {
         this.nid1Property = new CommitAwareIntegerProperty(this, ObservableFields.NID1.toExternalString(), getNid1());
         this.nid1Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Nid2_Str3_Version().setNid1(newValue.intValue());
             });
      }

      return this.nid1Property;
   }

   @Override
   public IntegerProperty nid2Property() {
      if (this.nid2Property == null) {
         this.nid2Property = new CommitAwareIntegerProperty(this, ObservableFields.NID2.toExternalString(), getNid2());
         this.nid2Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Nid2_Str3_Version().setNid2(newValue.intValue());
             });
      }

      return this.nid2Property;
   }

   @Override
   public StringProperty str3Property() {
      if (this.str3Property == null) {
         this.str3Property = new CommitAwareStringProperty(this, ObservableFields.STR3.toExternalString(), getStr3());
         this.str3Property.addListener(
             (observable, oldValue, newValue) -> {
                getNid1_Nid2_Str3_Version().setStr3(newValue);
             });
      }

      return this.str3Property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid1() {
      if (this.nid1Property != null) {
         return this.nid1Property.get();
      }

      return getNid1_Nid2_Str3_Version().getNid1();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid1(int nid) {
      if (this.nid1Property != null) {
         this.nid1Property.set(nid);
      }

      getNid1_Nid2_Str3_Version().setNid1(nid);
   }

   //~--- get methods ---------------------------------------------------------

   private Nid1_Nid2_Str3_VersionImpl getNid1_Nid2_Str3_Version() {
      return (Nid1_Nid2_Str3_VersionImpl) this.stampedVersionProperty.get();
   }

   @Override
   public int getNid2() {
      if (this.nid2Property != null) {
         return this.nid2Property.get();
      }

      return getNid1_Nid2_Str3_Version().getNid2();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid2(int nid) {
      if (this.nid2Property != null) {
         this.nid2Property.set(nid);
      }

      getNid1_Nid2_Str3_Version().setNid2(nid);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr3() {
      if (this.str3Property != null) {
         return this.str3Property.get();
      }

      return getNid1_Nid2_Str3_Version().getStr3();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setStr3(String value) {
      if (this.str3Property != null) {
         this.str3Property.set(value);
      }

      getNid1_Nid2_Str3_Version().setStr3(value);
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(nid1Property());
      properties.add(nid2Property());
      properties.add(str3Property());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(nid1Property());
      properties.add(nid2Property());
      properties.add(str3Property());
      return properties;
    }
}

