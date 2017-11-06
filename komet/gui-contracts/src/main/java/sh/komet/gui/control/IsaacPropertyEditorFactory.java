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

import java.util.ArrayList;
import java.util.Collection;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Status;
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
      if (propertySheetItem instanceof PropertySheetItemConceptWrapper) {
         return createCustomChoiceEditor((PropertySheetItemConceptWrapper) propertySheetItem);
      } else if (propertySheetItem instanceof PropertySheetStatusWrapper) {
         return Editors.createChoiceEditor(propertySheetItem, Status.makeActiveAndInactiveSet());
      }  else if (propertySheetItem instanceof PropertySheetTextWrapper) {
         return Editors.createTextEditor(propertySheetItem);
      }
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

    private PropertyEditor<?> createCustomChoiceEditor(PropertySheetItemConceptWrapper propertySheetItem){
        Collection<ConceptForControlWrapper> collection = new ArrayList<>();
        propertySheetItem.getAllowedValues().stream().forEach((nid) -> collection.add(new ConceptForControlWrapper(manifoldForDisplay, nid)));

        return Editors.createChoiceEditor(propertySheetItem, collection);
    }
   
   
}
