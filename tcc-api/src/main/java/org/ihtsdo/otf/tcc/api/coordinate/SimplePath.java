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
package org.ihtsdo.otf.tcc.api.coordinate;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.spec.SimpleConceptSpecification;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "simple-path")
public class SimplePath {
    SimpleConceptSpecification pathConceptSpecification;
    Set<SimplePosition> origins = new HashSet<>();

    public SimplePath(SimpleConceptSpecification pathConceptSpecification) {
        this.pathConceptSpecification = pathConceptSpecification;
    }

    /**
     * no arg constructor for jaxb.
     */
    public SimplePath() {
    }

    public SimpleConceptSpecification getPathConceptSpecification() {
        return pathConceptSpecification;
    }

    public void setPathConceptSpecification(SimpleConceptSpecification pathConceptSpecification) {
        this.pathConceptSpecification = pathConceptSpecification;
    }

    public Set<SimplePosition> getOrigins() {
        return origins;
    }

    public void setOrigins(Set<SimplePosition> origins) {
        this.origins = origins;
    }
}
