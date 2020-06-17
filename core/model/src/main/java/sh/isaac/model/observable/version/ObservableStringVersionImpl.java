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



package sh.isaac.model.observable.version;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.Property;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.chronicle.Chronology;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.MutableStringVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.observable.commitaware.CommitAwareStringProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableStringVersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements ObservableStringVersion {
   /** The string property. */
   StringProperty stringProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable description impl.
    *
    * @param stampedVersion the stamped version
    * @param chronology the chronology
    */
   public ObservableStringVersionImpl(StringVersion stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, chronology);
   }

   public ObservableStringVersionImpl(ObservableStringVersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setString(versionToClone.getString());
   }
   
   public ObservableStringVersionImpl(UUID primordialUuid, UUID referencedComponentUuid, int assemblageNid) {
      super(VersionType.STRING, primordialUuid, referencedComponentUuid, assemblageNid);
   }
   

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        ObservableStringVersionImpl analog = new ObservableStringVersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }


   //~--- methods -------------------------------------------------------------

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
        StringVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
        ObservableStringVersionImpl newObservableVersion = new ObservableStringVersionImpl(
                newVersion,
                (ObservableSemanticChronology) chronology);

        ((ObservableChronologyImpl) chronology).getVersionList()
                .add(newObservableVersion);
        return (V) newObservableVersion;
    }

    @Override
    public <V extends Version> V makeAnalog(Transaction transaction, int authorNid) {
        StringVersion newVersion = this.stampedVersionProperty.get().makeAnalog(transaction, authorNid);
        ObservableStringVersionImpl newObservableVersion = new ObservableStringVersionImpl(
                newVersion,
                (ObservableSemanticChronology) chronology);

        ((ObservableChronologyImpl) chronology).getVersionList()
                .add(newObservableVersion);
        return (V) newObservableVersion;
    }
   /**
    * string property.
    *
    * @return the string property
    */
   @Override
   public StringProperty stringProperty() {
      if (this.stampedVersionProperty == null && this.stringProperty == null) {
         this.stringProperty = new CommitAwareStringProperty(
             this,
             ObservableFields.STRING_VALUE_FOR_SEMANTIC.toExternalString(),
                 "");
      }
      if (this.stringProperty == null) {
         this.stringProperty = new CommitAwareStringProperty(
             this,
             ObservableFields.STRING_VALUE_FOR_SEMANTIC.toExternalString(),
             getString());
      }

      return this.stringProperty;
   }

   @Override
   public String toString() {
      return "ObservableStringVersionImpl{" + getString() + '}';
   }

   @Override
   protected void updateVersion() {
      if (this.stringProperty != null && !this.stringProperty.get().equals(((MutableStringVersion) this.stampedVersionProperty.get()).getString())) {
         this.stringProperty.set(((MutableStringVersion) this.stampedVersionProperty.get()).getString());
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(stringProperty());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(stringProperty());
      return properties;
    }

   /**
    * Gets the string.
    *
    * @return the string
    */
   @Override
   public String getString() {
      if (this.stringProperty != null) {
         return this.stringProperty.get();
      }

      return ((StringVersion) this.stampedVersionProperty.get()).getString();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the string.
    *
    * @param string the new string
    */
   @Override
   public final void setString(String string) {
       if (this.stampedVersionProperty == null) {
           this.stringProperty();
       }
      if (this.stringProperty != null) {
         this.stringProperty.set(string);
      }

      if (this.stampedVersionProperty != null) {
        ((MutableStringVersion) this.stampedVersionProperty.get()).setString(string);
      }
   }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof ObservableStringVersionImpl) {
            ObservableStringVersionImpl observableAnalog = (ObservableStringVersionImpl) analog;
            observableAnalog.setString(this.getString());
        } else if (analog instanceof StringVersionImpl) {
             StringVersionImpl simpleAnalog = (StringVersionImpl) analog;
             simpleAnalog.setString(this.getString());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        StringVersionImpl newVersion = new StringVersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }
}

