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



package sh.isaac.api.observable.coordinate;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinateImmutable;
import sh.isaac.api.coordinate.LanguageCoordinateProxy;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableLanguageCoordinate.
 *
 * @author kec
 */
public interface ObservableLanguageCoordinate
        extends LanguageCoordinateProxy, ObservableCoordinate<LanguageCoordinateImmutable> {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                languageConceptProperty(),
                descriptionTypePreferenceListProperty(),
                dialectAssemblagePreferenceListProperty(),
                modulePreferenceListForLanguageProperty(),
        };
    }

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        if (nextPriorityLanguageCoordinateProperty().get() != null) {
            return new ObservableCoordinate<?>[]{
                    nextPriorityLanguageCoordinateProperty().get()
            };
        }
        return new ObservableCoordinate<?>[]{};
    }


    /**
     * 
     * @return the language coordinate that this observable wraps. 
     */
     LanguageCoordinate getLanguageCoordinate();

    /**
     * Language concept nid property.
     *
     * @return the integer property
     */
    ObjectProperty<ConceptSpecification> languageConceptProperty();


    /**
    * Description type preference list property.
    *
    * @return the object property
    */
    ListProperty<ConceptSpecification> descriptionTypePreferenceListProperty();

   /**
    * Dialect assemblage preference list property.
    *
    * @return the object property
    */
   ListProperty<ConceptSpecification> dialectAssemblagePreferenceListProperty();

   ListProperty<ConceptSpecification> modulePreferenceListForLanguageProperty();

   /**
    * The next priority language coordinate property. 
    * @return the object property
    */
   ObjectProperty<ObservableLanguageCoordinate> nextPriorityLanguageCoordinateProperty();
       /**
     * 
     * @param dialectAssemblagePreferenceList
     * @deprecated for backward compatability only. 
     */
    @Deprecated
    void setDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList);

    /**
     * 
     * @param descriptionTypePreferenceList 
     * @deprecated for backward compatability only. 
     */
    @Deprecated
   void setDescriptionTypePreferenceList(int[] descriptionTypePreferenceList);
}

