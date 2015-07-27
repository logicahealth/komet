/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model.sememe.version;

import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememeData;

/**
 *
 * @author kec
 */
public class DynamicSememeImpl extends SememeVersionImpl<DynamicSememeImpl> 
    implements MutableDynamicSememe<DynamicSememeImpl> {

    public DynamicSememeImpl(SememeChronologyImpl<DynamicSememeImpl> container, 
            int stampSequence, short versionSequence, 
            DataBuffer db) {
        super(container, stampSequence, versionSequence);
        throw new UnsupportedOperationException();
    }

    public DynamicSememeImpl(SememeChronologyImpl<DynamicSememeImpl> container, 
            int stampSequence, short versionSequence) {
        super(container, stampSequence, versionSequence);
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void writeVersionData(DataBuffer data) {
        super.writeVersionData(data);
        throw new UnsupportedOperationException();
    }
    @Override
    public SememeType getSememeType() {
        return SememeType.DYNAMIC;
    };

    @Override
    public DynamicSememeData[] getData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
