/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray;

import java.beans.PropertyVetoException;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;

/**
 * The Interface RefexArrayOfBytearrayAnalogBI editing array of byte array type refex analog. The preferred method of editing terminology is through a
 * blueprint.
 *
 * @param <A> the type of object returned by the analog generator
 * @see AnalogBI
 * @eee CreateOrAmendBlueprintc
 */
public interface RefexArrayOfBytearrayAnalogBI<A extends RefexArrayOfBytearrayAnalogBI<A>>
        extends RefexAnalogBI<A>, RefexArrayOfBytearrayVersionBI<A> {

    /**
     * Sets the array of byte array based on the given
     * <code>arrayOfByteArray</code> for this array of byte array refex.
     *
     * @param arrayOfByteArray the array of byte array to be associated with this refex member
     * @throws PropertyVetoException if the new value is not valid
     */
    void setArrayOfByteArray(byte[][] arrayOfByteArray) throws PropertyVetoException;
}