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
package sh.komet.gui.action.dashboard;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class AssemblageDashboardRow {

    private final int assemblageNid;
    
    public int getAssemblageNid() {
        return assemblageNid;
    }
    
    public ManifoldCoordinate getManifoldCoordinate() {
        return viewProperties.getManifoldCoordinate();
    }
    private final ViewProperties viewProperties;
    private final StringProperty assemblageName;
    private SimpleIntegerProperty semanticCount;
    private SimpleIntegerProperty assemblageMemoryUsage;
    private SimpleIntegerProperty assemblageDiskSpaceUsage;
    
    public AssemblageDashboardRow(int assemblageNid, ViewProperties viewProperties) {
        this.assemblageNid = assemblageNid;
        this.viewProperties = viewProperties;
        this.assemblageName = new SimpleStringProperty(this, "Assemblage Name", viewProperties.getPreferredDescriptionText(assemblageNid));
    }
    
    public final String getAssemblageName() {
        return assemblageName.get();
    }
    
    public final void setAssemblageName(String value) {
        assemblageName.set(value);
    }
    
    public StringProperty assemblageNameProperty() {
        return assemblageName;
    }

    public int getSemanticCount() {
        return semanticCountProperty().get();
    }

    public void setSemanticCount(int count) {
        semanticCountProperty().set(count);
    }

    public IntegerProperty semanticCountProperty() {
        if (semanticCount == null) {
            int count = Get.assemblageService().getSemanticCount(assemblageNid);
            semanticCount = new SimpleIntegerProperty(this, "semantic count", count);
        }
        return semanticCount;
    }

    public IntegerProperty assemblageMemoryUsageProperty() {
        if (assemblageMemoryUsage == null) {
            int size = Get.assemblageService().getAssemblageMemoryInUse(assemblageNid);
            assemblageMemoryUsage = new SimpleIntegerProperty(this, "assemblage memory used", size);
        }
        return assemblageMemoryUsage;
    }

    public int getAssemblageMemoryUsage() {
        return assemblageMemoryUsageProperty().get();
    }

    public void setAssemblageMemoryUsage(int sizeInBytes) {
        assemblageMemoryUsageProperty().set(sizeInBytes);
    }


    public IntegerProperty assemblageDiskSpaceUsageProperty() {
        if (assemblageDiskSpaceUsage == null) {
            int size = Get.assemblageService().getAssemblageSizeOnDisk(assemblageNid);
            assemblageDiskSpaceUsage = new SimpleIntegerProperty(this, "assemblage size on disk", size);
        }
        return assemblageDiskSpaceUsage;
    }

    public int getAssemblageDiskSpaceUsage() {
        return assemblageDiskSpaceUsageProperty().get();
    }

    public void setAssemblageDiskSpaceUsage(int sizeInBytes) {
        assemblageDiskSpaceUsageProperty().set(sizeInBytes);
    }
        
}
