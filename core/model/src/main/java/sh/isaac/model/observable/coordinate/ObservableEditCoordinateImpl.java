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
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableEditCoordinateImpl.
 *
 * @author kec
 */
public class ObservableEditCoordinateImpl
        extends ObservableCoordinateImpl<EditCoordinateImmutable>
         implements ObservableEditCoordinate {
   /** The author property. */
   private final SimpleEqualityBasedObjectProperty<ConceptSpecification> authorProperty;

   /** The module property. */
   private final SimpleEqualityBasedObjectProperty<ConceptSpecification> moduleProperty;

   /** The path property. */
   private final SimpleEqualityBasedObjectProperty<ConceptSpecification> pathProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable edit coordinate impl.
    *
    * @param editCoordinate the edit coordinate
    */
   public ObservableEditCoordinateImpl(EditCoordinateImmutable editCoordinate) {
       super(editCoordinate);
       this.authorProperty = new SimpleEqualityBasedObjectProperty<>(this,
               ObservableFields.AUTHOR_NID_FOR_EDIT_COORDINATE.toExternalString(),
               editCoordinate.getAuthor());

       this.moduleProperty = new SimpleEqualityBasedObjectProperty(this,
               ObservableFields.MODULE_NID_FOR_EDIT_COORDINATE.toExternalString(),
               editCoordinate.getModule());

       this.pathProperty = new SimpleEqualityBasedObjectProperty(this,
               ObservableFields.PATH_NID_FOR_EDIT_CORDINATE.toExternalString(),
               editCoordinate.getPath());
       addListeners();
   }

    protected void removeListeners() {
        this.moduleProperty.removeListener(this::moduleConceptChanged);
        this.authorProperty.removeListener(this::authorConceptChanged);
        this.pathProperty.removeListener(this::pathConceptChanged);
    }

    protected void addListeners() {
        this.moduleProperty.addListener(this::moduleConceptChanged);
        this.authorProperty.addListener(this::authorConceptChanged);
        this.pathProperty.addListener(this::pathConceptChanged);
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends EditCoordinateImmutable> observable, EditCoordinateImmutable oldValue, EditCoordinateImmutable newValue) {
        this.authorProperty.setValue(newValue.getAuthor());
        this.moduleProperty.setValue(newValue.getModule());
        this.pathProperty.setValue(newValue.getPath());
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                    ConceptSpecification old,
                                    ConceptSpecification newPathConcept) {
        this.setValue(EditCoordinateImmutable.make(getAuthorNid(), getModuleNid(), newPathConcept.getNid()));
    }

    private void authorConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                      ConceptSpecification oldAuthorConcept,
                                      ConceptSpecification newAuthorConcept) {
        this.setValue(EditCoordinateImmutable.make(newAuthorConcept.getNid(), getModuleNid(), getPathNid()));
    }

    private void moduleConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                      ConceptSpecification old,
                                      ConceptSpecification newModuleConcept) {
        this.setValue(EditCoordinateImmutable.make(getAuthorNid(), newModuleConcept.getNid(), getPathNid()));
    }

    @Override
    public ObjectProperty<ConceptSpecification> authorProperty() {
        return this.authorProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> moduleProperty() {
        return this.moduleProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> pathProperty() {
        return this.pathProperty;
    }

    @Override
    public EditCoordinate getEditCoordinate() {
        return this.getValue();
    }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableEditCoordinateImpl{" + this.getValue().toString() + '}';
   }

}

