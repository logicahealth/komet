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

import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableLongSememe;

/**
 * Used for path origins by path manager. 
 * @author kec
  */
public class LongSememeImpl extends SememeVersionImpl<LongSememeImpl> 
    implements  MutableLongSememe<LongSememeImpl> {

    long longValue = Long.MAX_VALUE;
    
    public LongSememeImpl(SememeChronologyImpl<LongSememeImpl> container,  
            int stampSequence, short versionSequence, DataBuffer data) {
        super(container, stampSequence, versionSequence);
        this.longValue = data.getLong();
    }

    public LongSememeImpl(SememeChronologyImpl<LongSememeImpl> container,  
            int stampSequence, short versionSequence) {
        super(container, stampSequence, versionSequence);
    }

    
    @Override
    protected void writeVersionData(DataBuffer data) {
        super.writeVersionData(data);
        data.putLong(longValue);
    }

    @Override
    public SememeType getSememeType() {
        return SememeType.LONG;
    };

    @Override
    public long getLongValue() {
        return this.longValue;
    }

    @Override
    public void setLongValue(long time) {
        if (this.longValue != Long.MAX_VALUE) {
            checkUncommitted();
        }
        this.longValue = time;
    }
    
}
