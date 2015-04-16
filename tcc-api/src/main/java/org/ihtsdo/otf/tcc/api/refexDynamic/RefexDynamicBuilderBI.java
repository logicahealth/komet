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

package org.ihtsdo.otf.tcc.api.refexDynamic;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;


public interface RefexDynamicBuilderBI {

    /**
     * Assemblage is an assembled collection of objects. Used to identify the Refex that this item is a 
     * member of.  Used instead of RefexExtensionId because of confusion with the component the 
     * Refex Extends, or the ReferencedComponentId.
     * 
     * Note that in the new RefexDynanamicAPI - this linked concept must contain the column definitions 
     * for using this concept as a Refex container.  See the class description of {@link RefexDynamicUsageDescription}
     * for more details on the requirements.
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
    void setData(RefexDynamicDataBI[] data) throws PropertyVetoException;

    /**
     * Set the data (if any) in the specified column of the Refex.
     * 
     * For a Refex that is only establishing membership, there will be no data
     * columns.
     * 
     * If there is one or more data columns associated with a Refex membership,
     * then the type of each of data columns would be an extension of
     * {@link RefexDynamicDataBI}
     * 
     * @param columnNumber
     * @param data
     * @throws IndexOutOfBoundsException
     * @throws PropertyVetoException
     */
    void setData(int columnNumber, RefexDynamicDataBI data) throws IndexOutOfBoundsException, PropertyVetoException;
    
    /**
     * Stuff from deprecated AnalogBI - not sure what the replacement for this is supposed to be now
     */
    void setNid(int nid) throws PropertyVetoException;
    void setStatus(Status nid) throws PropertyVetoException;
    void setAuthorNid(int nid) throws PropertyVetoException;
    void setModuleNid(int nid) throws PropertyVetoException;
    void setPathNid(int nid) throws PropertyVetoException;
    void setTime(long time) throws PropertyVetoException;
    
    /**
     * Something else we appear to need....
     */
    int getNid();

}
