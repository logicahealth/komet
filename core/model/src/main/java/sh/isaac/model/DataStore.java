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



package sh.isaac.model;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.jvnet.hk2.annotations.Contract;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DatabaseServices;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedIntIntMap;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.model.collections.SpinedNidNidSetMap;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
@Contract
public interface DataStore
        extends DatabaseServices {
   void putChronologyData(ChronologyImpl chronology);

   //~--- get methods ---------------------------------------------------------

   int[] getAssemblageConceptNids();

   ConcurrentHashMap<Integer, IsaacObjectType> getAssemblageTypeMap();

   Optional<ByteArrayDataBuffer> getChronologyData(int nid);

   SpinedNidNidSetMap getComponentToSemanticNidsMap();

   ConcurrentMap<Integer, AtomicInteger> getSequenceGeneratorMap();

   SpinedIntIntMap getAssemblageNid_ElementSequenceToNid_Map(int assemblageNid);
   
   SpinedNidIntMap getNidToAssemblageNidMap();
   
   SpinedNidIntMap getNidToElementSequenceMap();

   SpinedIntIntArrayMap getTaxonomyMap(int assemblageNid);
}

