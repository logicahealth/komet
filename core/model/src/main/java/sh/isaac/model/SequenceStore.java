/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.model;

import sh.isaac.model.collections.SpinedByteArrayArrayMap;

/**
 * If a backend data store maintains sequences per assemblage, it may implement
 * this interface.  Various Spined Data structures like 
 * {@link SpinedByteArrayArrayMap} can take advantage of the sequence ids, 
 * if present.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public interface SequenceStore
{
    public int getElementSequenceForNid(int nid); 
    
    public int getElementSequenceForNid(int nid, int assemblageNid);
}
