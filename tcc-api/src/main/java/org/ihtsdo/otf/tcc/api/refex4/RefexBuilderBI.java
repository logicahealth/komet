/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.otf.tcc.api.refex4;

import java.beans.PropertyVetoException;
import java.io.IOException;

import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refex4.data.RefexDataBI;


public interface RefexBuilderBI {

    /**
     * Assemblage an assembled collection of objects. Used to identify the Refex
     * that this item is a member of. Used instead of RefexExtensionId because
     * of confusion with the component the Refex Extends, or the
     * ReferencedComponentId.
     * 
     * Note that in the new RefexAPI - this linked concept must contain the column definitions 
     * for using this concept as a Refex container.  The linked concept must define the combination of data
     * columns being used within this Refex. The referenced concept must contains a Refex extension of 
     * RefexDataColumn //TODO <concept> where the attached data is [int, String, String] where the int value is used to 
     * align the order with the data here, and the two string columns are used for the name and
     * description values of the column.
     * 
     * The referenced concept should also contain a description of type //TODO determine where the description contains 
     * the overall purpose of this Refex.
     * 
     * @param assemblageNid
     * @throws IOException
     * @throws PropertyVetoException
     */
    void setAssemblageNid(int assemblageNid) throws IOException, PropertyVetoException;

    /**
     * The concept or component being added to the Refex
     */
    void setReferencedComponentNid(int componentNid) throws IOException, PropertyVetoException;

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
    
    
    /**
     * Stuff from deprecated AnalogBI - not sure what the replacement for this is supposed to be now
     */
    void setNid(int nid) throws PropertyVetoException;
    void setStatus(Status nid) throws PropertyVetoException;
    void setAuthorNid(int nid) throws PropertyVetoException;
    void setModuleNid(int nid) throws PropertyVetoException;
    void setPathNid(int nid) throws PropertyVetoException;
    void setTime(long time) throws PropertyVetoException;

}
