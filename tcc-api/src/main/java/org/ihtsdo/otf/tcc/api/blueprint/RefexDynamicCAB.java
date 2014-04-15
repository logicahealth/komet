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
package org.ihtsdo.otf.tcc.api.blueprint;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

/**
 * {@link RefexDynamicCAB}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefexDynamicCAB extends CreateOrAmendBlueprint {

    
    /**
     * TODO - still need to determine the methods that the blueprint code needs.
     * 
     * The Blueprint code will create an instance of a RefexBuilderBI - call the setters on that
     * and then return a RefexChronicleBI - so the setters won't be exposed to the end users.
     */
    
    
    /**
     * @param componentUuid
     * @param componentVersion
     * @param viewCoordinate
     * @param idDirective
     * @param refexDirective
     * @throws IOException
     * @throws InvalidCAB
     * @throws ContradictionException
     */
    public RefexDynamicCAB(UUID componentUuid, ComponentVersionBI componentVersion, ViewCoordinate viewCoordinate,
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, InvalidCAB,
            ContradictionException {
        super(componentUuid, componentVersion, viewCoordinate, idDirective, refexDirective);
        // TODO Auto-generated constructor stub
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint#recomputeUuid()
     */
    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB,
            ContradictionException {
        // TODO Auto-generated method stub
        
    }

}
