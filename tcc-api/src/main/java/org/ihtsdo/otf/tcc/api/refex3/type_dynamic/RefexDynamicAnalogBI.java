/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.refex3.type_dynamic;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI;

/**
 * {@link RefexDynamicAnalogBI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface RefexDynamicAnalogBI<A extends RefexDynamicAnalogBI<A>> extends RefexAnalogBI<A>,
        RefexDynamicVersionBI<A> {
    /**
     * Set the required link to the concept that defines the combination of data
     * columns being used within this Refex. The referenced concept must be a
     * child of //TODO <determine> RefexDataColumn and that Concept must have a
     * Refex extension of RefexDataColumn where the attached data is [int,
     * String, String] where the int value is used to align the order with the
     * data here, and the two string columns are used for the name and
     * description values of the column.
     * 
     * @param componentNid
     */
    void setRefexUsageDescriptorNid(int refexUsageDescriptorNid);

    /**
     * Set all of the data columns that are part of this Refex. See
     * {@link #setData(int, RefexMemberBI)}
     * 
     * @param data
     * @throws PropertyVetoException
     */
    void setData(RefexDataBI[] data) throws PropertyVetoException;

    /**
     * Set the data (if any) in the specified column of the Refex.
     * 
     * For a Refex that is only establishing membership, there will be no data
     * columns.
     * 
     * If there is one or more data columns associated with a Refex membership,
     * then the type of each of data columns would be an extension of
     * {@link RefexDataBI}
     * 
     * @param columnNumber
     * @param data
     * @throws IndexOutOfBoundsException
     * @throws PropertyVetoException
     */
    void setData(int columnNumber, RefexDataBI data) throws IndexOutOfBoundsException, PropertyVetoException;


}
