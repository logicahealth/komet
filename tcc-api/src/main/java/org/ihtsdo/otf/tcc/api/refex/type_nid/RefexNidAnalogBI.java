/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.otf.tcc.api.refex.type_nid;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;

/**
 *
 * @author kec
 */
public interface RefexNidAnalogBI<A extends RefexNidAnalogBI<A>> 
	extends RefexAnalogBI<A>, RefexNidVersionBI<A> {
    
     void setNid1(int nid1) throws PropertyVetoException;

}
