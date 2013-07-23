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

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

/**
 * The Interface RefexArrayOfBytearrayVersionBI provides methods for interacting
 * with a specific array of byte array type refex version.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface RefexArrayOfBytearrayVersionBI<A extends RefexArrayOfBytearrayAnalogBI<A>>
        extends RefexVersionBI<A> {

    /**
     * Gets the array of byte array associated with this version of an array of
     * byte array refex member.
     *
     * @return the array of byte array with this refex member version
     */
    byte[][] getArrayOfByteArray();
}
