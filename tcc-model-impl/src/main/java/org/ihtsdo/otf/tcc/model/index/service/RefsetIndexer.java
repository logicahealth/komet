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
package org.ihtsdo.otf.tcc.model.index.service;

import java.io.IOException;
import java.util.Collection;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.jvnet.hk2.annotations.Contract;

/**
 * The conract interface for the refset indexing service.
 * @author aimeefurber
 */

@Contract
public interface RefsetIndexer extends Indexer{
    public void addRefex(RefexMember refexMember); //TODO needs type
    public void writeToIndex(Collection<RefexMember> items) throws IOException;
    public void writeToIndex(Collection<RefexMember> items, ViewCoordinate viewCoordinate) throws IOException;
}
