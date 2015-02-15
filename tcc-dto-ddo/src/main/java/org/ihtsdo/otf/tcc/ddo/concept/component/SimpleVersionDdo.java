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
package org.ihtsdo.otf.tcc.ddo.concept.component;

import java.io.IOException;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.ddo.TimeReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.SimpleDescriptionVersionDdo;

/**
 *
 * @author dylangrald
 */
@XmlSeeAlso({SimpleDescriptionVersionDdo.class})
public abstract class SimpleVersionDdo implements Serializable {

    private static final long serialVersionUID = 1;
    private TimeReference fxTime;

    public SimpleVersionDdo() {
        super();
    }

    public SimpleVersionDdo(TerminologySnapshotDI ss, ComponentVersionBI another)
            throws IOException, ContradictionException {
        super();
        fxTime = new TimeReference(another.getTime());

    }

}
