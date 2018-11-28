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



package sh.isaac.model.observable.coordinate;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- JDK imports ------------------------------------------------------------


//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.model.coordinate.LogicCoordinateImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableLogicCoordinateImpl.
 *
 * @author kec
 */
public class ObservableLogicCoordinateImpl
        extends ObservableCoordinateImpl
         implements ObservableLogicCoordinate {
   /** The logic coordinate. */
   LogicCoordinateImpl logicCoordinate;

   /** The stated assemblage property. */
   ObjectProperty<ConceptSpecification> statedAssemblageProperty;

   /** The inferred assemblage property. */
   ObjectProperty<ConceptSpecification> inferredAssemblageProperty;

   /** The description logic profile property. */
   ObjectProperty<ConceptSpecification> descriptionLogicProfileProperty;

   /** The classifier property. */
   ObjectProperty<ConceptSpecification> classifierProperty;

   /** The concept assemblage property. */
   ObjectProperty<ConceptSpecification> conceptAssemblageProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable logic coordinate impl.
    */
   @SuppressWarnings("unused")
   private ObservableLogicCoordinateImpl() {
      // for jaxb
   }

   /**
    * Instantiates a new observable logic coordinate impl.
    *
    * @param logicCoordinate the logic coordinate
    */
   public ObservableLogicCoordinateImpl(LogicCoordinate logicCoordinate) {
      if (logicCoordinate instanceof ObservableLogicCoordinateImpl) {
         this.logicCoordinate = ((ObservableLogicCoordinateImpl) logicCoordinate).logicCoordinate;
      } else {
         this.logicCoordinate = (LogicCoordinateImpl) logicCoordinate;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Classifier property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<ConceptSpecification>  classifierProperty() {
      if (this.classifierProperty == null) {
         this.classifierProperty = new SimpleObjectProperty(this,
               ObservableFields.CLASSIFIER_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getClassifier());
         addListenerReference(this.logicCoordinate.setClassifierProperty(this.classifierProperty));
         this.classifierProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.classifierProperty;
   }

   @Override
   public ObjectProperty<ConceptSpecification>  conceptAssemblageProperty() {
      if (this.conceptAssemblageProperty == null) {
         this.conceptAssemblageProperty = new SimpleObjectProperty(this,
               ObservableFields.CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE.toExternalString(),
               getConceptAssemblage());
         addListenerReference(this.logicCoordinate.setConceptAssemblageProperty(this.conceptAssemblageProperty));
         this.conceptAssemblageProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.conceptAssemblageProperty;
   }

   /**
    * Description logic profile property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<ConceptSpecification>  descriptionLogicProfileProperty() {
      if (this.descriptionLogicProfileProperty == null) {
         this.descriptionLogicProfileProperty = new SimpleObjectProperty(this,
               ObservableFields.DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getDescriptionLogicProfile());
         addListenerReference(this.logicCoordinate.setDescriptionLogicProfileProperty(this.descriptionLogicProfileProperty));
         this.descriptionLogicProfileProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.descriptionLogicProfileProperty;
   }

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      return this.logicCoordinate.equals(obj);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.logicCoordinate.hashCode();
   }

   /**
    * Inferred assemblage property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<ConceptSpecification>  inferredAssemblageProperty() {
      if (this.inferredAssemblageProperty == null) {
         this.inferredAssemblageProperty = new SimpleObjectProperty(this,
               ObservableFields.INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getInferredAssemblage());
         this.inferredAssemblageProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      addListenerReference(this.logicCoordinate.setInferredAssemblageProperty(this.inferredAssemblageProperty));
      return this.inferredAssemblageProperty;
   }

   /**
    * Stated assemblage property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<ConceptSpecification>  statedAssemblageProperty() {
      if (this.statedAssemblageProperty == null) {
         this.statedAssemblageProperty = new SimpleObjectProperty(this,
               ObservableFields.STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               getStatedAssemblage());
         addListenerReference(this.logicCoordinate.setStatedAssemblageProperty(this.statedAssemblageProperty));
         this.statedAssemblageProperty.addListener((invalidation) -> fireValueChangedEvent());
      }

      return this.statedAssemblageProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableLogicCoordinateImpl{" + this.logicCoordinate.toString() + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the classifier.
    *
    * @return the classifier
    */
   @Override
   public int getClassifierNid() {
      if (this.classifierProperty != null) {
         return this.classifierProperty.get().getNid();
      }

      return this.logicCoordinate.getClassifierNid();
   }
  /**
    * Gets the concept assemblage nid.
    *
    * @return the classifier nid
    */
   @Override
   public int getConceptAssemblageNid() {
      if (this.conceptAssemblageProperty != null) {
         return this.conceptAssemblageProperty.get().getNid();
      }

      return this.logicCoordinate.getConceptAssemblageNid();
   }

   /**
    * Gets the description logic profile nid.
    *
    * @return the description logic profile nid
    */
   @Override
   public int getDescriptionLogicProfileNid() {
      if (this.descriptionLogicProfileProperty != null) {
         return this.descriptionLogicProfileProperty.get().getNid();
      }

      return this.logicCoordinate.getDescriptionLogicProfileNid();
   }

   /**
    * Gets the inferred assemblage nid.
    *
    * @return the inferred assemblage nid
    */
   @Override
   public int getInferredAssemblageNid() {
      if (this.inferredAssemblageProperty != null) {
         return this.inferredAssemblageProperty.get().getNid();
      }

      return this.logicCoordinate.getInferredAssemblageNid();
   }

   /**
    * Gets the stated assemblage nid.
    *
    * @return the stated assemblage nid
    */
   @Override
   public int getStatedAssemblageNid() {
      if (this.statedAssemblageProperty != null) {
         return this.statedAssemblageProperty.get().getNid();
      }
      return this.logicCoordinate.getStatedAssemblageNid();
   }
   
   
   @Override
   public ObservableLogicCoordinateImpl deepClone() {
      return new ObservableLogicCoordinateImpl(logicCoordinate.deepClone());
   }

    public ConceptSpecification getStatedAssemblage() {
        return logicCoordinate.getStatedAssemblage();
    }

    public void setStatedAssemblage(ConceptSpecification statedAssemblage) {
        logicCoordinate.setStatedAssemblage(statedAssemblage);
    }

    public ConceptSpecification getInferredAssemblage() {
        return logicCoordinate.getInferredAssemblage();
    }

    public void setInferredAssemblage(ConceptSpecification inferredAssemblage) {
        logicCoordinate.setInferredAssemblage(inferredAssemblage);
    }

    public ConceptSpecification getDescriptionLogicProfile() {
        return logicCoordinate.getDescriptionLogicProfile();
    }

    public void setDescriptionLogicProfile(ConceptSpecification descriptionLogicProfile) {
        logicCoordinate.setDescriptionLogicProfile(descriptionLogicProfile);
    }

    public ConceptSpecification getClassifier() {
        return logicCoordinate.getClassifier();
    }

    public void setClassifier(ConceptSpecification classifier) {
        logicCoordinate.setClassifier(classifier);
    }

    public ConceptSpecification getConceptAssemblage() {
        return logicCoordinate.getConceptAssemblage();
    }

    public void setConceptAssemblage(ConceptSpecification conceptAssemblage) {
        logicCoordinate.setConceptAssemblage(conceptAssemblage);
    }
    
    
}

