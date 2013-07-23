/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author kec
 */
public class PathSpec implements SpecBI {
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(pathConcept);
        out.writeObject(originConcept);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            pathConcept = (ConceptSpec) in.readObject();
            originConcept = (ConceptSpec) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    private ConceptSpec pathConcept;

    private ConceptSpec originConcept;

    public PathSpec(ConceptSpec pathConcept, ConceptSpec originConcept) {
        this.pathConcept = pathConcept;
        this.originConcept = originConcept;
    }

    public PathSpec() {
        super();
    }
 

    public ConceptSpec getOriginConcept() {
        return originConcept;
    }

    public void setOriginConcept(ConceptSpec originConcept) {
        this.originConcept = originConcept;
    }

    public ConceptSpec getPathConcept() {
        return pathConcept;
    }

    public void setPathConcept(ConceptSpec pathConcept) {
        this.pathConcept = pathConcept;
    }

    @Override
    public String toString() {
        return "PathSpec[pathConcept: " + pathConcept +
                " originConcept: " + originConcept + "]";
    }

}
