/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.control.circumstance;

import java.util.ArrayList;
import java.util.List;
import org.controlsfx.control.PropertySheet;
import sh.isaac.model.statement.CircumstanceImpl;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.measure.PropertySheetMeasureWrapper;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public abstract class CircumstancePropertySheet {
    
    protected final ViewProperties viewProperties;
    
    
    private final PropertySheet propertySheet = new PropertySheet();
    {
        this.propertySheet.setMode(PropertySheet.Mode.NAME);
        this.propertySheet.setSearchBoxVisible(false);
        this.propertySheet.setModeSwitcherVisible(false);
        
    }

    public CircumstancePropertySheet(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.propertySheet.setPropertyEditorFactory(new PropertyEditorFactory(this.viewProperties));
    }
    
    public void setCircumstance(CircumstanceImpl circumstance) {
        
        propertySheet.getItems().clear();
        if (circumstance != null) {
            propertySheet.getItems().addAll(getProperties(circumstance));
        }
    }

    private List<PropertySheet.Item> getProperties(CircumstanceImpl circumstance) {
       ArrayList<PropertySheet.Item> itemList = new ArrayList<>();
       
       itemList.add(new PropertySheetMeasureWrapper(viewProperties, circumstance.timingProperty()));
       
       // purpose list

       
       getSubclassProperties(circumstance, itemList);
       return itemList;
    }
    
    protected abstract void getSubclassProperties(CircumstanceImpl circumstance, List<PropertySheet.Item> itemList);

    public PropertySheet getPropertySheet() {
        return propertySheet;
    }
}
