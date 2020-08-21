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
package sh.komet.gui.control.property;

import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.concept.PropertySheetItemConceptNidWrapper;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.Status;
import sh.komet.gui.control.property.wrapper.PropertySheetStatusWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetTextWrapper;

/**
 *
 * @author kec
 */
public class PropertyToPropertySheetItem {
    public static List<PropertySheet.Item> getItems(List<Property<?>> properties, ManifoldCoordinate manifoldCoordinate) {
        ArrayList<PropertySheet.Item> items = new ArrayList<>();
        
        for (Property<?> property: properties) {
            try {
                if (property instanceof StringProperty) {
                    items.add(new PropertySheetTextWrapper(manifoldCoordinate, (StringProperty) property));
                } else if (property instanceof IntegerProperty) {
                    String lowerCaseName = property.getName().toLowerCase();
                    if (lowerCaseName.contains("nid") || lowerCaseName.contains("component")) {
                        items.add(new PropertySheetItemConceptNidWrapper(manifoldCoordinate,
                                (IntegerProperty) property));
                    }
                    
                } else if (property instanceof ObjectProperty) {
                    if (((ObjectProperty<Object>) property).getValue() instanceof Status) {
                        items.add(new PropertySheetStatusWrapper(manifoldCoordinate,
                                (ObjectProperty<Status>) property));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return items;
    }
}
