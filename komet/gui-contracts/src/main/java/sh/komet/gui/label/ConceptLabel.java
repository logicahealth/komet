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



package sh.komet.gui.label;

//~--- JDK imports ------------------------------------------------------------


//~--- non-JDK imports --------------------------------------------------------

import java.util.function.Consumer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import javafx.scene.control.Label;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionVersion;

import sh.komet.gui.contract.Manifold;

//~--- classes ----------------------------------------------------------------

/**  
 *
 * @author kec
 */
public class ConceptLabel
        extends Label { 
   SimpleObjectProperty<ConceptChronology> conceptProperty;
   SimpleObjectProperty<Manifold>          manifoldProperty;
   Consumer<ConceptLabel>                  descriptionTextUpdater;

   //~--- constructors --------------------------------------------------------

   public ConceptLabel(SimpleObjectProperty<ConceptChronology> conceptProperty,
                       SimpleObjectProperty<Manifold> manifoldProperty,
                       Consumer<ConceptLabel> descriptionTextUpdater) {
      this.conceptProperty        = conceptProperty;
      this.manifoldProperty       = manifoldProperty;
      this.descriptionTextUpdater = descriptionTextUpdater;
      this.conceptProperty.addListener((ObservableValue<? extends ConceptChronology> observable,
           ConceptChronology oldValue,
           ConceptChronology newValue) -> {
             this.descriptionTextUpdater.accept(this);
          });
   }

   //~--- set methods ---------------------------------------------------------
   private void setDescriptionText(DescriptionVersion latestDescriptionVersion) {
      if (latestDescriptionVersion != null) {
         this.setText(latestDescriptionVersion.getText());
      } 
   }

   private void setEmptyText() {
      this.setText("empty");
   }

   public static void setFullySpecifiedText(ConceptLabel label) {
      label.conceptProperty.get()
                     .getFullySpecifiedDescription(label.manifoldProperty.get())
                     .ifPresent(label::setDescriptionText)
                     .ifAbsent(label::setEmptyText);
   }

   public static void setPreferredText(ConceptLabel label) {
      label.conceptProperty.get()
                     .getPreferredDescription(label.manifoldProperty.get())
                     .ifPresent(label::setDescriptionText)
                     .ifAbsent(label::setEmptyText);
   }
}

