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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A position suitable for jaxb marshall/unmarshall. 
 * @author kec
 */
@XmlRootElement(name = "simple-position")
public class SimplePosition {
    private long timePoint;
    private SimplePath path;

    /**
     * No arg constructor for jaxb. 
     */
    public SimplePosition() {
    }

    public SimplePosition(long timePoint, SimplePath path) {
        this.timePoint = timePoint;
        this.path = path;
    }

    /**
     * 
     * @return the timepoint of this position on the path.  
     */
    public long getTimePoint() {
        return timePoint;
    }

    /**
     * 
     * @param timePoint the timepoint of this position on the path.
     */
    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    /**
     * 
     * @return the path this position is on. 
     */
    public SimplePath getPath() {
        return path;
    }

    /**
     * 
     * @param path the path this position is on.
     */
    public void setPath(SimplePath path) {
        this.path = path;
    }
}
