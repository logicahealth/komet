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



package sh.komet.gui.control;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.layout.Border;

import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.state.DisclosureState;
import sh.komet.gui.state.ExpandAction;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public final class ExpandControl
        extends Button {
   DisclosureState                    disclosureState      = DisclosureState.CLICK_TO_OPEN;
   SimpleObjectProperty<ExpandAction> expandActionProperty = new SimpleObjectProperty<>(ExpandAction.HIDE_CHILDREN);

   //~--- constructors --------------------------------------------------------

   public ExpandControl() {
      super("", Iconography.TAXONOMY_CLICK_TO_OPEN.getIconographic());
      setBorder(Border.EMPTY);
      setCenterShape(true);
      setAlignment(Pos.CENTER);
      setMinSize(25, 25);
      setMaxSize(25, 25);
      setPrefSize(25, 25);
      getStyleClass().setAll("expand-control");
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void fire() {
      if (isArmed()) {
         switch (disclosureState) {
         case CLICK_TO_CLOSE:
            disclosureState = DisclosureState.CLICK_TO_OPEN;
            setGraphic(Iconography.TAXONOMY_CLICK_TO_OPEN.getIconographic());
            expandActionProperty.set(ExpandAction.HIDE_CHILDREN);
            break;

         case CLICK_TO_OPEN:
            disclosureState = DisclosureState.CLICK_TO_CLOSE;
            setGraphic(Iconography.TAXONOMY_CLICK_TO_CLOSE.getIconographic());
            expandActionProperty.set(ExpandAction.SHOW_CHILDREN);
            break;

         default:
         }
      }

      super.fire();
   }

   ReadOnlyObjectProperty<ExpandAction> expandActionProperty() {
      return this.expandActionProperty;
   }

   //~--- get methods ---------------------------------------------------------

   public ExpandAction getExpandAction() {
      return expandActionProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setExpandAction(ExpandAction expandAction) {
      this.expandActionProperty.set(expandAction);

      switch (expandAction) {
      case HIDE_CHILDREN:
         disclosureState = DisclosureState.CLICK_TO_OPEN;
         setGraphic(Iconography.TAXONOMY_CLICK_TO_OPEN.getIconographic());
         break;

      case SHOW_CHILDREN:
         disclosureState = DisclosureState.CLICK_TO_CLOSE;
         setGraphic(Iconography.TAXONOMY_CLICK_TO_CLOSE.getIconographic());
         break;

      default:
         throw new UnsupportedOperationException("can't handle action: " + expandAction);
      }
   }
}

