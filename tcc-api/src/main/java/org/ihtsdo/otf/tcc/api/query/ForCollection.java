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
package org.ihtsdo.otf.tcc.api.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "for")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ForCollection {

    public enum ForCollectionContents {

        CONCEPT, COMPONENT, CUSTOM;
    }
    ForCollectionContents forCollection = ForCollectionContents.CONCEPT;
    List<UUID> customCollection = new ArrayList<>();

    public NativeIdSetBI getCollection() throws IOException {
        TerminologyStoreDI ts = Ts.get();
        switch (forCollection) {
            case COMPONENT:
                //TODO Dylan to check this, and replace with a real operation from
                // the database directly to ensure all components are included 
                // (set everything up to current maxid)
                ConcurrentBitSet componentBitSet = new ConcurrentBitSet(ts.getAllConceptNids());
                componentBitSet.andNot(new ConcurrentBitSet());
                return componentBitSet;
            case CONCEPT:
                return ts.getAllConceptNids();
            case CUSTOM:
                ConcurrentBitSet cbs = new ConcurrentBitSet();
                for (UUID uuid : customCollection) {
                    cbs.add(ts.getNidForUuids(uuid));
                }
                return cbs;

            default:
                throw new UnsupportedOperationException("Can't handle: " + forCollection);
        }
    }

    public String getForCollectionString() {
        return forCollection.name();
    }

    public void setForCollectionString(String forCollectionString) {
        this.forCollection = ForCollectionContents.valueOf(forCollectionString);
    }

    @XmlTransient
    public ForCollectionContents getForCollection() {
        return forCollection;
    }

    public void setForCollection(ForCollectionContents forCollection) {
        this.forCollection = forCollection;
    }

    public List<UUID> getCustomCollection() {
        return customCollection;
    }

    public void setCustomCollection(List<UUID> customCollection) {
        this.customCollection = customCollection;
    }
}
