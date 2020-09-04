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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.LogicCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

/**
 * The Class ObservableLogicCoordinateBase.
 *
 * @author kec
 */
public abstract class ObservableLogicCoordinateBase
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

    /** The classifier property. */
    final ObjectProperty<ConceptSpecification> rootConceptProperty;

    //~--- constructors --------------------------------------------------------
    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptSpecification> classifierListener = this::classifierConceptChanged;
    private final ChangeListener<ConceptSpecification> conceptAssemblageListener = this::classifierConceptAssemblageChanged;
    private final ChangeListener<ConceptSpecification> descriptionLogicProfileListener = this::descriptionLogicProfileChanged;
    private final ChangeListener<ConceptSpecification> inferredAssemblageListener = this::inferredAssemblageChanged;
    private final ChangeListener<ConceptSpecification> statedAssemblageListener = this::statedAssemblageChanged;
    private final ChangeListener<ConceptSpecification> digraphIdentityListener = this::digraphIdentityChanged;
    private final ChangeListener<ConceptSpecification> rootConceptListener = this::rootConceptChanged;

   /**
    * Instantiates a new observable logic coordinate impl.
    *
    * @param logicCoordinate the logic coordinate
    */
   protected ObservableLogicCoordinateBase(LogicCoordinate logicCoordinate, String coordinateName) {
       super(logicCoordinate.toLogicCoordinateImmutable(), coordinateName);

       this.classifierProperty = makeClassifierProperty(logicCoordinate);

       this.conceptAssemblageProperty = makeConceptAssemblageProperty(logicCoordinate);

       this.descriptionLogicProfileProperty = makeDescriptionLogicProfileProperty(logicCoordinate);

       this.inferredAssemblageProperty = makeInferredAssemblageProperty(logicCoordinate);

       this.statedAssemblageProperty = makeStatedAssemblageProperty(logicCoordinate);

       this.digraphIdentityProperty = makeDigraphIdentityProperty(logicCoordinate);

       this.rootConceptProperty = makeRootConceptProperty(logicCoordinate);

       addListeners();

   }

    protected abstract ObjectProperty<ConceptSpecification> makeClassifierProperty(LogicCoordinate logicCoordinate);
    protected abstract ObjectProperty<ConceptSpecification> makeConceptAssemblageProperty(LogicCoordinate logicCoordinate);
    protected abstract ObjectProperty<ConceptSpecification> makeDescriptionLogicProfileProperty(LogicCoordinate logicCoordinate);
    protected abstract ObjectProperty<ConceptSpecification> makeInferredAssemblageProperty(LogicCoordinate logicCoordinate);
    protected abstract ObjectProperty<ConceptSpecification> makeStatedAssemblageProperty(LogicCoordinate logicCoordinate);
    protected abstract ObjectProperty<ConceptSpecification> makeDigraphIdentityProperty(LogicCoordinate logicCoordinate);
    protected abstract ObjectProperty<ConceptSpecification> makeRootConceptProperty(LogicCoordinate logicCoordinate);

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends LogicCoordinateImmutable> observable, LogicCoordinateImmutable oldValue, LogicCoordinateImmutable newValue) {
        this.classifierProperty.setValue(newValue.getClassifier());
        this.conceptAssemblageProperty.setValue(newValue.getConceptAssemblage());
        this.descriptionLogicProfileProperty.setValue(newValue.getDescriptionLogicProfile());
        this.inferredAssemblageProperty.setValue(newValue.getInferredAssemblage());
        this.statedAssemblageProperty.setValue(newValue.getStatedAssemblage());
        this.digraphIdentityProperty.setValue(newValue.getDigraphIdentity());
        this.rootConceptProperty.setValue(newValue.getRoot());
    }

    @Override
    protected void addListeners() {
        this.classifierProperty.addListener(this.classifierListener);
        this.conceptAssemblageProperty.addListener(this.conceptAssemblageListener);
        this.descriptionLogicProfileProperty.addListener(this.descriptionLogicProfileListener);
        this.inferredAssemblageProperty.addListener(this.inferredAssemblageListener);
        this.statedAssemblageProperty.addListener(this.statedAssemblageListener);
        this.digraphIdentityProperty.addListener(this.digraphIdentityListener);
        this.rootConceptProperty.addListener(this.rootConceptListener);
    }

    @Override
    protected void removeListeners() {
        this.classifierProperty.removeListener(this.classifierListener);
        this.conceptAssemblageProperty.removeListener(this.conceptAssemblageListener);
        this.descriptionLogicProfileProperty.removeListener(this.descriptionLogicProfileListener);
        this.inferredAssemblageProperty.removeListener(this.inferredAssemblageListener);
        this.statedAssemblageProperty.removeListener(this.statedAssemblageListener);
        this.digraphIdentityProperty.removeListener(this.digraphIdentityListener);
        this.rootConceptProperty.removeListener(this.rootConceptListener);
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
               newDigraphIdentity.getNid(),
               getRootNid()));
   }
    private void statedAssemblageChanged(ObservableValue<? extends ConceptSpecification> observable,
                                         ConceptSpecification oldStatedAssemblage,
                                         ConceptSpecification newStatedAssemblage) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                getDescriptionLogicProfileNid(),
                getInferredAssemblageNid(),
                newStatedAssemblage.getNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid(),
                getRootNid()));
    }
    private void inferredAssemblageChanged(ObservableValue<? extends ConceptSpecification> observable,
                                           ConceptSpecification oldInferredAssemblage,
                                           ConceptSpecification newInferredAssemblage) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                getDescriptionLogicProfileNid(),
                newInferredAssemblage.getNid(),
                getStatedAssemblageNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid(),
                getRootNid()));
    }
    private void descriptionLogicProfileChanged(ObservableValue<? extends ConceptSpecification> observable,
                                                ConceptSpecification oldDescriptionLogicProfile,
                                                ConceptSpecification newDescriptionLogicProfile) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                newDescriptionLogicProfile.getNid(),
                getInferredAssemblageNid(),
                getStatedAssemblageNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid(),
                getRootNid()));
    }
    private void classifierConceptAssemblageChanged(ObservableValue<? extends ConceptSpecification> observable,
                                                    ConceptSpecification oldConceptAssemblageConcept,
                                                    ConceptSpecification newConceptAssemblageConcept) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                getDescriptionLogicProfileNid(),
                getInferredAssemblageNid(),
                getStatedAssemblageNid(),
                newConceptAssemblageConcept.getNid(),
                getDigraphIdentityNid(),
                getRootNid()));
    }
    private void classifierConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                          ConceptSpecification oldClassifierConcept,
                                          ConceptSpecification newClassifierConcept) {
        this.setValue(LogicCoordinateImmutable.make(newClassifierConcept.getNid(),
                getDescriptionLogicProfileNid(),
                getInferredAssemblageNid(),
                getStatedAssemblageNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid(),
                getRootNid()));
    }
    private void rootConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                          ConceptSpecification oldRootConcept,
                                          ConceptSpecification newRootConcept) {
        this.setValue(LogicCoordinateImmutable.make(getClassifierNid(),
                getDescriptionLogicProfileNid(),
                getInferredAssemblageNid(),
                getStatedAssemblageNid(),
                getConceptAssemblageNid(),
                getDigraphIdentityNid(),
                newRootConcept.getNid()));
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
      return "ObservableLogicCoordinateBase{" + this.getValue().toString() + '}';
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
    public ObjectProperty<ConceptSpecification> digraphIdentityProperty() {
        return this.digraphIdentityProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> rootConceptProperty() {
        return this.rootConceptProperty;
    }
}

