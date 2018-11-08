/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.xml;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author kec
 */
@XmlRootElement (name="Let")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbMap {
    private Map<String, Object> map = new HashMap();

    public JaxbMap() {
    }
 
    public JaxbMap(Map<String, Object> map) {
        this.map = map;
    }
 
    
    public Map<String, Object> getMap() {
        return map;
    }
 
    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
    
    public static JaxbMap of(Map<String, Object> map) {
        return new JaxbMap(map);
    }
}

