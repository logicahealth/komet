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
package gov.vha.isaac.ochre.model;

/**
 * A interface that can write OCHRE objects in a UUID-based format for universal exchange without 
 * depending on centrally allocated identifiers. References to other objects are only allowed via UUID, and
 so each object is atomic, and no read-resolve or other methods for resolving object identity is required. 
 When an OchreExternalizable object is reconstructed, an instance is created using the public no-arg constructor, 
 then the getInternal method called.
 * @author kec
 */
public interface OchreExternalizable {
    
    /**
     * The object implements the putExternal method to save its contents 
 by calling the methods of ByteArrayDataBuffer for primitive values.
     * @param out the ByteArrayDataBuffer to write to. 
     */
    void putExternal(ByteArrayDataBuffer out);
    
    /**
     * 
     * @return the data format version
     */
    byte getDataFormatVersion();
    
    /**
     * 
     * @return the type of the object. 
     */
    OchreExternalizableObjectType getOchreObjectType();
    
}
