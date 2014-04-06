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
package org.ihtsdo.otf.tcc.model.cc.refex4.sample;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.refex4.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex4.data.RefexColumnInfoBI;
import org.ihtsdo.otf.tcc.api.refex4.data.RefexDataBI;
import org.ihtsdo.otf.tcc.api.refex4.data.RefexDataType;
import org.ihtsdo.otf.tcc.api.refex4.data.RefexUsageDescriptionBI;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.refex4.data.dataTypes.RefexBoolean;
import org.ihtsdo.otf.tcc.model.cc.refex4.data.dataTypes.RefexString;

/**
 * {@link Sample}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class Sample {
    public Sample() throws IOException
    {
        
        //An example showing what a Refex would look like when it is attached to a description in another concept.
        ConceptChronicle c = null;
        
        //Cast is to pretend we get back a refex4 rather than a refex (other code hasn't yet been updated - there would have to be a new 'getRefexes' method)
        @SuppressWarnings({ "rawtypes", "unchecked", "null" })
        RefexChronicleBI<? extends RefexChronicleBI<?>> refex = (RefexChronicleBI) c.getDescriptions().iterator().next().getRefexes().iterator().next();
        
        RefexUsageDescriptionBI refexDescription= refex.getRefexUsageDescription();
        
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
        for (RefexColumnInfoBI ci : refexDescription.getColumnInfo())
        {
            System.out.println(ci.getColumnOrder() + " - " + ci.getColumnName() + " - " + ci.getColumnDescription());
        }
        
        
        //Note that the order of the items returned by getColumnInfo() will align with the order of the items returned by refex.getData().  
        //Or, the output of ci.getColumnOrder() can be used as input to refex.getData(columnNumber)
        
        
        for (RefexDataBI data : refex.getData())
        {
            //serialized object access (most shouldn't need this)
            System.out.println(data.getData());
            
            // object access
            System.out.println(data.getDataObject());
            
            //cast method one:
            if (data.getRefexDataType() == RefexDataType.STRING)
            {
                String s = (String)data.getDataObject();
                System.out.println(s);
            }
            if (data.getRefexDataType() == RefexDataType.BOOLEAN)
            {
                Boolean b = (Boolean)data.getDataObject();
                System.out.println(b);
            }
            
            //cast method two:
            
            if (data.getRefexDataType() == RefexDataType.STRING)
            {
                RefexString rs = (RefexString) data;
                System.out.println(rs.getDataString());
            }
            //Another variation
            if (data.getRefexDataType().getRefexMemberClass().isAssignableFrom(RefexBoolean.class))
            {
                RefexBoolean rs = (RefexBoolean) data;
                System.out.println(rs.getDataBoolean());
            }
            
            //And, you can also get back propeties...
            
            System.out.println(data.getDataObjectProperty());
            
        }
    }
}
