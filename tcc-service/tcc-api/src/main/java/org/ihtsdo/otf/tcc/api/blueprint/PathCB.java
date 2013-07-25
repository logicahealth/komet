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
package org.ihtsdo.otf.tcc.api.blueprint;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

/**
 *
 * @author akf
 */
public class PathCB {
    /**
     * Concept that identifies the new path
     */
    private ConceptCB pathBp;
    /**
     * Concept that identifies the path refset
     */
    private RefexCAB pathRefsetBp;
    /**
     * Concept that identifies the path origin refset
     */
    private RefexCAB pathOriginRefsetBp;
    
    private RefexCAB pathOriginRefsetPathForPathAsOriginBp;
    private Collection<ConceptChronicleBI> origins = new TreeSet<ConceptChronicleBI>();
    
    /**
     * 
     * @param pathBp Concept blueprint that will identify the new path
     * @param pathRefsetBp Refex member blueprint that adds this path to the path refex
     * @param pathOriginRefsetBp Refex member blueprint that adds this path to the path origin refex
     * @param origins A vararg of concepts that identify the origins of this new path. 
     *      Each origin will have a time of "latest" with respect to the new path. 
     */
    public PathCB(ConceptCB pathBp, 
            RefexCAB pathRefsetBp,
            RefexCAB pathOriginRefsetBp,
            ConceptChronicleBI... origins){
        this.pathBp = pathBp;
        this.pathRefsetBp = pathRefsetBp;
        this.pathOriginRefsetBp = pathOriginRefsetBp;
        this.pathOriginRefsetPathForPathAsOriginBp = null;
        if (origins != null) {
            this.origins.addAll(Arrays.asList(origins));
        }
    }
    
    /**
     * 
     * @param pathBp Concept blueprint that will identify the new path
     * @param pathRefsetBp Refex member blueprint that adds this path to the path refex
     * @param pathOriginRefsetBp Refex member blueprint that adds this path to the path origin refex
     * @param pathOriginRefsetPathForPathAsOriginBp Allows the path created by this blueprint 
     * to be set as the origins for an existing path.
     * @param origins A vararg of concepts that identify the origins of this new path. 
     *      Each origin will have a time of "latest" with respect to the new path. 
     */
    public PathCB(ConceptCB pathBp, 
            RefexCAB pathRefsetBp,
            RefexCAB pathOriginRefsetBp,
            RefexCAB pathOriginRefsetPathForPathAsOriginBp,
            ConceptChronicleBI... origins){
        this.pathBp = pathBp;
        this.pathRefsetBp = pathRefsetBp;
        this.pathOriginRefsetBp = pathOriginRefsetBp;
        this.pathOriginRefsetPathForPathAsOriginBp = pathOriginRefsetPathForPathAsOriginBp;
        if (origins != null) {
            this.origins.addAll(Arrays.asList(origins));
        }
    }

    public Collection<ConceptChronicleBI> getOrigins() {
        return origins;
    }

    public ConceptCB getPathBp() {
        return pathBp;
    }

    public RefexCAB getPathOriginRefsetBp() {
        return pathOriginRefsetBp;
    }

    public RefexCAB getPathRefsetBp() {
        return pathRefsetBp;
    }
    /**
     * Use if making the path created by this
     * <code>PathCB</code> as an origin of an existing path.
     *
     * @return a <code>RefexCAB</code> representing the membership of this path
     * as an origin of an existing path.
     */
    public RefexCAB getPathAsOriginBp() {
        return pathOriginRefsetPathForPathAsOriginBp;
    }

    /**
     * Use if making the path created by this
     * <code>PathCB</code> as an origin of an existing path.
     *
     * @param pathOriginRefsetPathAsOriginBp a <code>RefexCAB</code>
     * representing the membership of this path as an origin of an existing path
     */
    public void setPathAsOriginBp(RefexCAB pathOriginRefsetPathAsOriginBp) {
        this.pathOriginRefsetPathForPathAsOriginBp = pathOriginRefsetPathAsOriginBp;
    }
}
