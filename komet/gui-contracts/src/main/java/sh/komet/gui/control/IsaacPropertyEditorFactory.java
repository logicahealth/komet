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
package sh.komet.gui.control;

import sh.komet.gui.control.concept.PropertySheetItemConceptNidWrapper;
import sh.komet.gui.control.concept.ConceptLabel;
import sh.komet.gui.control.concept.ConceptForControlWrapper;
import sh.komet.gui.control.measure.PropertySheetMeasureWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Status;
import sh.isaac.model.statement.MeasureImpl;
import sh.isaac.model.statement.ResultImpl;
import sh.komet.gui.control.circumstance.CircumstanceEditor;
import sh.komet.gui.control.circumstance.PropertySheetCircumstanceWrapper;
import sh.komet.gui.control.measure.MeasureEditor;
import sh.komet.gui.control.result.PropertySheetResultWrapper;
import sh.komet.gui.control.result.ResultEditor;
import sh.komet.gui.manifold.HistoryRecord;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class IsaacPropertyEditorFactory implements Callback<PropertySheet.Item, PropertyEditor<?>> {
   Manifold manifoldForDisplay;
   public IsaacPropertyEditorFactory(Manifold manifoldForDisplay) {
      this.manifoldForDisplay = manifoldForDisplay;
   }
   @Override
   public PropertyEditor<?> call(PropertySheet.Item propertySheetItem) {
      if (propertySheetItem instanceof PropertySheetItemConceptNidWrapper) {
         return createCustomChoiceEditor((PropertySheetItemConceptNidWrapper) propertySheetItem);
      } else if (propertySheetItem instanceof PropertySheetStatusWrapper) {
         return Editors.createChoiceEditor(propertySheetItem, Status.makeActiveAndInactiveSet());
      }  else if (propertySheetItem instanceof PropertySheetTextWrapper) {
         return Editors.createTextEditor(propertySheetItem);
      } else if (propertySheetItem instanceof PropertySheetMeasureWrapper) {
          PropertySheetMeasureWrapper measureWrapper = (PropertySheetMeasureWrapper) propertySheetItem;
          MeasureEditor measureEditor = new MeasureEditor(manifoldForDisplay);
          measureEditor.setValue((MeasureImpl) measureWrapper.getValue());
         return measureEditor;
      } else if (propertySheetItem instanceof PropertySheetCircumstanceWrapper) {
          PropertySheetCircumstanceWrapper circumstanceWrapper = (PropertySheetCircumstanceWrapper) propertySheetItem;
         return new CircumstanceEditor(circumstanceWrapper.getObservableValue().get(), manifoldForDisplay);
      } else if (propertySheetItem instanceof PropertySheetResultWrapper) {
          PropertySheetResultWrapper resultWrapper = (PropertySheetResultWrapper) propertySheetItem;
         return new ResultEditor((ObservableValue<ResultImpl>) resultWrapper.getObservableValue().get(), manifoldForDisplay);
      }
      throw new UnsupportedOperationException("Not supported yet: " + propertySheetItem.getClass().getName()); 
   }

    private PropertyEditor<?> createCustomChoiceEditor(PropertySheetItemConceptNidWrapper propertySheetItem){
        if (!propertySheetItem.getAllowedValues().isEmpty()) {
            Collection<ConceptForControlWrapper> collection = new ArrayList<>();
            propertySheetItem.getAllowedValues().stream().forEach((nid) -> collection.add(new ConceptForControlWrapper(manifoldForDisplay, nid)));

            return Editors.createChoiceEditor(propertySheetItem, collection);
        } else {
            ConceptLabel conceptLabel = new ConceptLabel(manifoldForDisplay, ConceptLabel::setPreferredText, (label) -> {
                List<MenuItem> labelMenu = new ArrayList<>();
                
                for (String manifoldGroup: Manifold.getGroupNames()) {
                    Menu manifoldHistory = new Menu(manifoldGroup);
                    labelMenu.add(manifoldHistory);
                    Collection<HistoryRecord> groupHistory = Manifold.getGroupHistory(manifoldGroup);
                    for (HistoryRecord record: groupHistory) {
                        MenuItem conceptItem = new MenuItem(
                                manifoldForDisplay.getPreferredDescriptionText(record.getComponentId())
                        );
                        conceptItem.setOnAction((ActionEvent event) -> {
                            label.setValue(record.getComponentId());
                        });
                        manifoldHistory.getItems().add(conceptItem);
                    }
                }
                return labelMenu;
            });
            
            
            
            return conceptLabel;
        }
    }
   
   
}
