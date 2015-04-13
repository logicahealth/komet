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
package org.ihtsdo.otf.tcc.model.cc.refexDynamic.sample;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicBoolean;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;

/**
 * {@link Sample}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class Sample {
    public Sample() throws IOException, ContradictionException
    {
        
        //An example showing what a Refex would look like when it is attached to a description in another concept.
        ConceptChronicle c = null;
        
        //Cast is to pretend we get back a refex4 rather than a refex (other code hasn't yet been updated - there would have to be a new 'getRefexes' method)
        @SuppressWarnings({ "rawtypes", "unchecked", "null" })
        RefexDynamicVersionBI<? extends RefexDynamicChronicleBI<?>> refex = (RefexDynamicVersionBI)((RefexDynamicChronicleBI) c.getDescriptions().iterator().next().getRefexes().iterator().next()).getVersion(null);
        
        RefexDynamicUsageDescription refexDescription= refex.getRefexDynamicUsageDescription();
        
        //This would print something like:
        /*
         * "The reference set 'UCUM Tags' is used to denote the 'unified code for units of measure' concepts and values that are identified
         * within the descriptions of another concept."
         */
        System.out.println(refexDescription.getRefexUsageDescription());
        
        //Assuming this refex was defined as taking a UCUM code constant (as a string) and a value (as a float) the output of this would look like:
        
        /*
         * 0 - UCUM unit - Contains the UCUM unit code - such as "mL" or "cm"
         * 1 - UCUM value - Contains the value from the description that was identified as having an attached UCUM unit - such as "5.3". 
         */
        for (RefexDynamicColumnInfo ci : refexDescription.getColumnInfo())
        {
            System.out.println(ci.getColumnOrder() + " - " + ci.getColumnName() + " - " + ci.getColumnDescription());
        }
        
        
        //Note that the order of the items returned by getColumnInfo() will align with the order of the items returned by refex.getData().  
        //Or, the output of ci.getColumnOrder() can be used as input to refex.getData(columnNumber)
        
        
        for (RefexDynamicDataBI data : refex.getData())
        {
            if (data == null)
            {
                System.out.println("null");
                continue;
            }
            //serialized object access (most shouldn't need this)
            System.out.println(data.getData());
            
            // object access
            System.out.println(data.getDataObject());
            
            //cast method one:
            if (data.getRefexDataType() == RefexDynamicDataType.STRING)
            {
                String s = (String)data.getDataObject();
                System.out.println(s);
            }
            if (data.getRefexDataType() == RefexDynamicDataType.BOOLEAN)
            {
                Boolean b = (Boolean)data.getDataObject();
                System.out.println(b);
            }
            
            //cast method two:
            
            if (data.getRefexDataType() == RefexDynamicDataType.STRING)
            {
                RefexDynamicString rs = (RefexDynamicString) data;
                System.out.println(rs.getDataString());
            }
            //Another variation
            if (data.getRefexDataType().getRefexMemberClass().isAssignableFrom(RefexDynamicBoolean.class))
            {
                RefexDynamicBoolean rs = (RefexDynamicBoolean) data;
                System.out.println(rs.getDataBoolean());
            }
            
            //And, you can also get back properties...
            
            System.out.println(data.getDataObjectProperty());
            
        }
    }
}
