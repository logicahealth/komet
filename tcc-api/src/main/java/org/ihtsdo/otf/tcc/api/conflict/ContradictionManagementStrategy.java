/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.conflict;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

public abstract class ContradictionManagementStrategy implements ContradictionManagerBI {

    private static final long serialVersionUID = 1L;
    
    protected transient ViewCoordinate vc;
    
    protected transient EditCoordinate ec;

    public void setViewCoordinate(ViewCoordinate vc) {
        this.vc = vc;
    }

    public ViewCoordinate getViewCoordinate() {
        return vc;
    }
    
    public void setEditCoordinate(EditCoordinate ec) {
        this.ec = ec;
    }

    public EditCoordinate getEditCoordinate() {
        return ec;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    private boolean isNull(Object... obj) {
        if (obj == null) {
            return true;
        }
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] == null)
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
         return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    
}
