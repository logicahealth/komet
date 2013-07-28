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
package org.ihtsdo.otf.tcc.chronicle.cc.refex;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.AnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

/**
 *
 * @author kec
 */
public interface RefexAnalogBI<R extends RefexRevision<R, ?>>
        extends RefexVersionBI, AnalogBI {

    void setRefexExtensionNid(int refexExtensionNid) throws PropertyVetoException;

    void setReferencedComponentNid(int componentNid) throws PropertyVetoException;

    @Override
    R makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid);
}
