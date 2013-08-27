/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.ddo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object containing a list to enable JAXB marshal/unmarshal of 
 * lists of query results. Result list may be an integer list, UUID list
 * or concrete subtype of ComponentChronicleDdo or ComponentVersionDdo. 
 * @author kec
 */
@XmlRootElement
public class ResultList {
    List<Object> resultList = new ArrayList();


    /**
     * 
     * @return the <code>resultList</code>
     */
    public List<Object> getResultList() {
        return resultList;
    }

    /**
     * 
     * @param resultList the <code>resultList</code> to set. 
     */
    public void setResultList(List<Object> resultList) {
        this.resultList = resultList;
    }
    
}
