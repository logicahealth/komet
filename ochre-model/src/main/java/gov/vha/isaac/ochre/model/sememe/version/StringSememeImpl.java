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

import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableStringSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;

/**
 *
 * @author kec
 */
public class StringSememeImpl extends SememeVersionImpl<StringSememeImpl> implements StringSememe<StringSememeImpl>, MutableStringSememe<StringSememeImpl> {
    
    private String string = null;

    public StringSememeImpl(SememeChronologyImpl<StringSememeImpl> container, 
            int stampSequence, short versionSequence) {
        super(container, stampSequence, versionSequence);
    }

    public StringSememeImpl(SememeChronologyImpl<StringSememeImpl> container, 
            int stampSequence, short versionSequence, DataBuffer data) {
        super(container, 
                stampSequence, versionSequence);
        this.string = data.readUTF();
    }

    @Override
    protected void writeVersionData(DataBuffer data) {
        super.writeVersionData(data);
        data.putUTF(string);
    }

    @Override
    public SememeType getSememeType() {
        return SememeType.STRING;
    }

    @Override
    public void setString(String string) {
        if (this.string != null) {
            checkUncommitted();
        } 
        this.string = string;
    }

    @Override
    public String getString() {
        return string;
    }    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("String≤");
        sb.append(string);
         toString(sb);
        sb.append('≥');
        return sb.toString();
    }

}
