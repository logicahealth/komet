/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.observable.model.version;

import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableSememeVersion;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;

/**
 *
 * @author kec
 * @param <V>
 */
public class ObservableSememeVersionImpl<V extends  ObservableSememeVersionImpl<V>>

    extends ObservableVersionImpl<V, SememeVersionImpl<?>> 

    implements ObservableSememeVersion<V> {
    
    public ObservableSememeVersionImpl(SememeVersionImpl<?> stampedVersion, ObservableSememeChronology<V> chronology) {
        super(stampedVersion, chronology);
    }

    @Override
    public int getSememeSequence() {
        return stampedVersion.getSememeSequence();
    }

    @Override
    public int getAssemblageSequence() {
        return stampedVersion.getAssemblageSequence();
    }

    @Override
    public int getReferencedComponentNid() {
        return stampedVersion.getReferencedComponentNid();
    }

    @Override
    public ObservableSememeChronology<V> getChronology() {
        return (ObservableSememeChronology<V>) chronology;
    }
    
}
