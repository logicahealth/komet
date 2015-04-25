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
package gov.vha.isaac.ochre.model.concept;

import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.ObjectChronicleImpl;

/**
 *
 * @author kec
 * @param <V>
 */
public class ConceptObjectChronicleImpl<V extends ConceptObjectVersionImpl> extends ObjectChronicleImpl<V>  {

    public ConceptObjectChronicleImpl(DataBuffer data) {
        super(data);
        constructorEnd(data);

    }

    @Override
    protected V makeVersion(int stampSequence, DataBuffer bb) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }




}
