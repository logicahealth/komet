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

package org.ihtsdo.otf.tcc.api.refex.type_boolean;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;

/**
 *
 * @author kec
 */
public interface RefexBooleanAnalogBI<A extends RefexBooleanAnalogBI<A>> 
	extends RefexAnalogBI<A>, RefexBooleanVersionBI<A> {
    
     void setBoolean1(boolean b) throws PropertyVetoException;

}
