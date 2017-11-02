/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.externalizable;

/**
 * A interface that can write OCHRE objects in a UUID-based format for universal exchange without
 * depending on centrally allocated identifiers. References to other objects are only allowed via UUID, and
 * so each object is atomic, and no read-resolve or other methods for resolving object identity is required.
 * @author kec
 */
public interface IsaacExternalizable {
   /**
    * The object implements the putExternal method to save its contents
    * by calling the methods of ByteArrayDataBuffer for primitive values.
    * @param out the ByteArrayDataBuffer to write to.
    */
   void putExternal(ByteArrayDataBuffer out);

   /**
    * Gets the externalizable object type.
    *
    * @return the type of the object.
    */
   IsaacObjectType getIsaacObjectType();
   
}

