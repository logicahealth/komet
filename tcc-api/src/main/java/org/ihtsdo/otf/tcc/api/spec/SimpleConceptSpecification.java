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
package org.ihtsdo.otf.tcc.api.spec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to hold the data for a ConceptSpec, which can be easily sent via JAXB.
 *
 * @author kec
 */
@XmlRootElement(name = "simple-concept-specification")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SimpleConceptSpecification {

    private String description;
    private String uuid;

    /**
     * No arg constructor for jaxb.
     */
    public SimpleConceptSpecification() {
    }

    /**
     *
     * @param description text of a description on a concept identified by the
     * uuid.
     * @param uuid uuid for a concept that contains a description with text
     * specified by the description.
     */
    public SimpleConceptSpecification(String description, String uuid) {
        this.description = description;
        this.uuid = uuid;
    }

    /**
     *
     * @return the description of the specified concept
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description set the description for the specified concept
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return the uuid for the specified concept
     */
    public String getUuid() {
        return uuid;
    }

    /**
     *
     * @param uuid set the uuid for the specified concept
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
