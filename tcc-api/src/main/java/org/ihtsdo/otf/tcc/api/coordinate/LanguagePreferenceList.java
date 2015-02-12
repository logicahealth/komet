/*
 * Copyright 2014 Informatics, Inc..
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
package org.ihtsdo.otf.tcc.api.coordinate;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 *
 * @author kec
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "languagePreferenceList")
public class LanguagePreferenceList {
        @XmlElement(name = "preference")
        private List<ConceptSpec> preferenceList = new ArrayList<>();
         
        public LanguagePreferenceList() {}
     
        public LanguagePreferenceList(List<ConceptSpec> preferenceList) {
            this.preferenceList = preferenceList;
        }
     
        public List<ConceptSpec> getPreferenceList() {
            return preferenceList;
        }
     
        public void setPreferenceList(List<ConceptSpec> preferenceList) {
            this.preferenceList = preferenceList;
        } 
}
