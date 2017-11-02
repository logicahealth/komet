/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model;

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.externalizable.IsaacObjectType;

/**
 *
 * @author kec
 */
@Contract
public interface ContainerSequenceService extends IdentifierService {
   
   
   /**
    * This object type is normally set when the first object is written to the assemblage, so there 
    * is no need for calling this method directly. 
    * @param nid
    * @param assemblageNid
    * @param objectType 
    */
   void setupNid(int nid, int assemblageNid, IsaacObjectType objectType);

   
   /**
    * A sequence for the semantic chronology, that is only unique within the 
    * particular assemblage. 
    * @param nid
    * @param assemblageNid
    * @return 
    */
   int getElementSequenceForNid(int nid, int assemblageNid);
   
   
   /**
    * A sequence for the semantic chronology, that is only unique within the 
    * particular assemblage. 
    * @param nid
    * @return 
    */
   int getElementSequenceForNid(int nid);
   
   /**
    * Returns the nid for the concept defining the assemblage that the nid's 
    * chronology is defined within. 
    * @param nid
    * @return the nid for the assemblage the chronicle is defined within of. 
    */
   int getAssemblageNidForNid(int nid);
   
   /**
    * 
    * @param semanticSequence the sequence of the semantic chronology
    * @param assemblageNid
    * @return the nid for the associated chronology specified by the assemblageSequence within the assemblage identified by the Nid
    */
   int getNidForElementSequence(int semanticSequence, int assemblageNid);
   
}
