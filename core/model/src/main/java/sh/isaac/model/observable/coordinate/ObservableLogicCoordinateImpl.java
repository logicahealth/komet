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
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LogicCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;

/**
 * The Class ObservableLogicCoordinateImpl.
 *
 * @author kec
 */
public class ObservableLogicCoordinateImpl
        extends ObservableCoordinateImpl<LogicCoordinateImmutable>
         implements ObservableLogicCoordinate {

   /** The stated assemblage property. */
   final ObjectProperty<ConceptSpecification> statedAssemblageProperty;

   /** The inferred assemblage property. */
   final ObjectProperty<ConceptSpecification> inferredAssemblageProperty;

   /** The description logic profile property. */
   final ObjectProperty<ConceptSpecification> descriptionLogicProfileProperty;

   /** The classifier property. */
   final ObjectProperty<ConceptSpecification> classifierProperty;

   /** The concept assemblage property. */
   final ObjectProperty<ConceptSpecification> conceptAssemblageProperty;

    /** The concept assemblage property. */
    final ObjectProperty<ConceptSpecification> digraphIdentityProperty;

    //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable logic coordinate impl.
    *
    * @param logicCoordinateImmutable the logic coordinate
    */
   public ObservableLogicCoordinateImpl(LogicCoordinateImmutable logicCoordinateImmutable) {
       super(logicCoordinateImmutable);
        this.classifierProperty = new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.CLASSIFIER_NID_FOR_LOGIC_COORDINATE.toExternalString(),
                logicCoordinateImmutable.getClassifier());

       this.conceptAssemblageProperty = new SimpleEqualityBasedObjectProperty(this,
               ObservableFields.CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE.toExternalString(),
               logicCoordinateImmutable.getConceptAssemblage());

       this.descriptionLogicProfileProperty = new SimpleEqualityBasedObjectProperty(this,
               ObservableFields.DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               logicCoordinateImmutable.getDescriptionLogicProfile());

           this.inferredAssemblageProperty = new SimpleEqualityBasedObjectProperty(this,
                   ObservableFields.INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
                   logicCoordinateImmutable.getInferredAssemblage());

       this.statedAssemblageProperty = new SimpleEqualityBasedObjectProperty(this,
               ObservableFields.STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE.toExternalString(),
               logicCoordinateImmutable.getStatedAssemblage());

       this.digraphIdentityProperty = new SimpleEqualityBasedObjectProperty(this,
               ObservableFields.DIGRAPH_FOR_LOGIC_COORDINATE.toExternalString(),
               logicCoordinateImmutable.getDigraphIdentity());

       addListeners();

   }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends LogicCoordinateImmutable> observable, LogicCoordinateImmutable oldValue, LogicCoordinateImmutable newValue) {
        this.classifierProperty.setValue(newValue.getClassifier());
        this.conceptAssemblageProperty.setValue(newValue.getConceptAssemblage());
        this.descriptionLogicProfileProperty.setValue(newValue.getDescriptionLogicProfile());
        this.inferredAssemblageProperty.setValue(newValue.getInferredAssemblage());
        this.statedAssemblageProperty.setValue(newValue.getStatedAssemblage());
        this.digraphIdentityProperty.setValue(newValue.getDigraphIdentity());
    }

    @Override
    protected void addListeners() {
        this.classifierProperty.addListener(this::classifierConceptChanged);
        this.conceptAssemblageProperty.addListener(this::classifierConceptAssemblageChanged);
        this.descriptionLogicProfileProperty.addListener(this::descriptionLogicProfileChanged);
        this.inferredAssemblageProperty.addListener(this::inferredAssemblageChanged);
        this.statedAssemblageProperty.addListener(this::statedAssemblageChanged);
        this.digraphIdentityProperty.addListener(this::digraphIdentityChanged);
    }

    @Override
    protected void removeListeners() {
        this.classifierProperty.removeListener(this::classifierConceptChanged);
        this.conceptAssemblageProperty.removeListener(this::classifierConceptAssemblageChanged);
        this.descriptionLogicProfileProperty.removeListener(this::descriptionLogicProfileChanged);
        this.inferredAssemblageProperty.removeListener(this::inferredAssemblageChanged);
        this.statedAssemblageProperty.removeListener(this::statedAssemblageChanged);
        this.digraphIdentityProperty.removeListener(this::digraphIdentityChanged);
    }

    //~--- methods -------------------------------------------------------------
   private void digraphIdentityChanged(ObservableValue<? extends ConceptSpecification> observable,
                                        ConceptSpecification oldDigraphIdentity,
                                        ConceptSpecification newDigraphIdentity) {
       this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
               getDescriptionLogicProfileNid(),
               getInferredAssemblageNid(),
               getStatedAssemblageNid(),
               getConceptAssemblageNid(),
               newDigraphIdentity.getNid()));
   }
    private void statedAssemblageChanged(ObservableValue<? extends ConceptSpecification> observable,
                                         ConceptSpecification oldStatedAssemblage,
                                         ConceptSpecification newStatedAssemblage) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                getDescriptionLogicProfileNid(),
                getInferredAssemblageNid(),
                newStatedAssemblage.getNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid()));
    }
    private void inferredAssemblageChanged(ObservableValue<? extends ConceptSpecification> observable,
                                           ConceptSpecification oldInferredAssemblage,
                                           ConceptSpecification newInferredAssemblage) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                getDescriptionLogicProfileNid(),
                newInferredAssemblage.getNid(),
                getStatedAssemblageNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid()));
    }
    private void descriptionLogicProfileChanged(ObservableValue<? extends ConceptSpecification> observable,
                                                ConceptSpecification oldDescriptionLogicProfile,
                                                ConceptSpecification newDescriptionLogicProfile) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                newDescriptionLogicProfile.getNid(),
                getInferredAssemblageNid(),
                getStatedAssemblageNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid()));
    }
    private void classifierConceptAssemblageChanged(ObservableValue<? extends ConceptSpecification> observable,
                                                    ConceptSpecification oldConceptAssemblageConcept,
                                                    ConceptSpecification newConceptAssemblageConcept) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                getDescriptionLogicProfileNid(),
                getInferredAssemblageNid(),
                getStatedAssemblageNid(),
                newConceptAssemblageConcept.getNid(),
                getDigraphIdentityNid()));
    }
    private void classifierConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                          ConceptSpecification oldClassifierConcept,
                                          ConceptSpecification newClassifierConcept) {
        this.setValue(LogicCoordinateImmutable.make(newClassifierConcept.getNid(),
                getDescriptionLogicProfileNid(),
                getInferredAssemblageNid(),
                getStatedAssemblageNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid()));
    }

   @Override
   public LogicCoordinateImmutable  getLogicCoordinate() {
      return getValue();
   }

    /**
     * Classifier property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<ConceptSpecification> classifierProperty() {
         return this.classifierProperty;
    }

   @Override
   public ObjectProperty<ConceptSpecification>  conceptAssemblageProperty() {
      return this.conceptAssemblageProperty;
   }

   /**
    * Description logic profile property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<ConceptSpecification>  descriptionLogicProfileProperty() {
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
      return this.getValue().equals(obj);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.getValue().hashCode();
   }

   /**
    * Inferred assemblage property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<ConceptSpecification>  inferredAssemblageProperty() {
      return this.inferredAssemblageProperty;
   }

   /**
    * Stated assemblage property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<ConceptSpecification>  statedAssemblageProperty() {
      return this.statedAssemblageProperty;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableLogicCoordinateImpl{" + this.getValue().toString() + '}';
   }

    public void setStatedAssemblage(ConceptSpecification statedAssemblage) {
        this.statedAssemblageProperty.setValue(statedAssemblage);
    }

    public void setInferredAssemblage(ConceptSpecification inferredAssemblage) {
        this.inferredAssemblageProperty.setValue(inferredAssemblage);
    }

    public void setDescriptionLogicProfile(ConceptSpecification descriptionLogicProfile) {
        this.descriptionLogicProfileProperty.setValue(descriptionLogicProfile);
    }

    public void setClassifier(ConceptSpecification classifier) {
       this.classifierProperty.setValue(classifier);
    }

    public void setConceptAssemblage(ConceptSpecification conceptAssemblage) {
        this.conceptAssemblageProperty.setValue(conceptAssemblage);
    }

    @Override
    public String toUserString() {
        return this.getValue().toUserString();
    }

    @Override
    public LogicCoordinateImmutable toLogicCoordinateImmutable() {
        return this.getValue();
    }

    @Override
    public ObjectProperty<ConceptSpecification> getDigraphIdentityProperty() {
        return this.digraphIdentityProperty;
    }
}

