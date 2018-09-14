 /*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.model.observable.version;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.model.semantic.SemanticChronologyImpl;

/**
 *
 * @author kec
 */
public class ObservableComponentNidVersionImpl 
        extends ObservableAbstractSemanticVersionImpl 
        implements ObservableComponentNidVersion {
   /** The component nid property. */
   IntegerProperty componentNidProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable component nid version impl.
    *
    * @param version the stamped version
    * @param chronology the chronology
    */
   public ObservableComponentNidVersionImpl(ComponentNidVersion version,
                                    ObservableSemanticChronology chronology) {
      super(version, 
              chronology);
   }
   

   public ObservableComponentNidVersionImpl(ObservableComponentNidVersion versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setComponentNid(versionToClone.getComponentNid());
   }

    public ObservableComponentNidVersionImpl(UUID primordialUuid, UUID referencedComponentUuid, 
            int assemblageNid) {
        super(VersionType.COMPONENT_NID, primordialUuid, referencedComponentUuid, 
                assemblageNid);
    }
   
   

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        ObservableComponentNidVersionImpl analog = new ObservableComponentNidVersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }


   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      ComponentNidVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      ObservableComponentNidVersionImpl newObservableVersion = 
              new ObservableComponentNidVersionImpl(newVersion, (ObservableSemanticChronology) chronology);
      ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Case significance concept nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty componentNidProperty() {
      if (this.stampedVersionProperty == null && componentNidProperty == null) {
         this.componentNidProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.COMPONENT_NID_FOR_SEMANTIC.toExternalString(),
                 0);
      }
      if (this.componentNidProperty == null) {
         this.componentNidProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.COMPONENT_NID_FOR_SEMANTIC.toExternalString(),
               getComponentNid());
         this.componentNidProperty.addListener((observable, oldValue, newValue) -> {
            ((ComponentNidVersionImpl) this.stampedVersionProperty.get()).setComponentNid(newValue.intValue());
         });
      }

      return this.componentNidProperty;
   }


   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the component nid.
    *
    * @return the case significance concept nid
    */
   @Override
   public int getComponentNid() {
      if (this.componentNidProperty != null) {
         return this.componentNidProperty.get();
      }

      return ((ComponentNidVersionImpl) this.stampedVersionProperty.get()).getComponentNid();
   }

   @Override
   protected void updateVersion() {
      if (this.componentNidProperty != null && this.componentNidProperty.get() != ((ComponentNidVersionImpl) this.stampedVersionProperty.get()).getComponentNid()) {
         this.componentNidProperty.set(((ComponentNidVersionImpl) this.stampedVersionProperty.get()).getComponentNid());
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept nid.
    *
    * @param componentNid the new case significance concept nid
    */
   @Override
   public final void setComponentNid(int componentNid) {
       if (this.stampedVersionProperty == null) {
           this.componentNidProperty();
       }
      if (this.componentNidProperty != null) {
         this.componentNidProperty.set(componentNid);
      }
      if (this.stampedVersionProperty != null) {
        ((ComponentNidVersionImpl) this.stampedVersionProperty.get()).setComponentNid(componentNid);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String toString() {
       if (this.stampedVersionProperty != null && this.stampedVersionProperty.get() != null) {
           return "ObservableComponentNidVersionImpl{" + this.stampedVersionProperty.get() + '}';
       }
      return "ObservableComponentNidVersionImpl{component:" + Get.conceptDescriptionText(getComponentNid()) + " Uncommitted Observable Version}";
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();
      properties.add(componentNidProperty());
      return properties;
   }  

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(componentNidProperty());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof ObservableComponentNidVersionImpl) {
            ObservableComponentNidVersionImpl observableAnalog = (ObservableComponentNidVersionImpl) analog;
            observableAnalog.setComponentNid(this.getComponentNid());
        } else if (analog instanceof ComponentNidVersionImpl) {
             ComponentNidVersionImpl simpleAnalog = (ComponentNidVersionImpl) analog;
             simpleAnalog.setComponentNid(this.getComponentNid());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, 
                getPrimordialUuid(), 
                getAssemblageNid(), 
                this.getReferencedComponentNid());
        ComponentNidVersionImpl newVersion = new ComponentNidVersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }
}
   
