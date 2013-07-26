/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.chronicle.cc.concept.processor;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ProcessComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */
public class AdjudicationAnalogCreator implements ProcessComponentChronicleBI {

    private EditCoordinate ec;
    private ViewCoordinate vc;
    private boolean componentChanged = false;

    public AdjudicationAnalogCreator(EditCoordinate ec, ViewCoordinate vc) {
        this.ec = ec;
        this.vc = vc;
    }
    
    
    
    @Override
    public void process(ComponentChronicleBI cc) throws Exception {
        if (cc.makeAdjudicationAnalogs(ec, vc)) {
            componentChanged = true;
        }
    }
    
    public boolean isComponentChanged() {
        return componentChanged;
    }


}
