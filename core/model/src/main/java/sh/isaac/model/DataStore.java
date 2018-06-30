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


import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.DatastoreServices;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;

/**
 *
 * @author kec
 */
@Contract
public interface DataStore
        extends DatastoreServices {
   
   /**
    * Store the specified chronology data.  
    * @param chronology The object to write
    */
   void putChronologyData(ChronologyImpl chronology);

   /**
    * @return an array of nids for the concepts that define assemblages. 
    */
   int[] getAssemblageConceptNids();

   /**
    * Return the type, if known, for the specified assemblageNid.  
    * @param assemblageNid  The nid to lookup
    * @return The type, or {@link IsaacObjectType#UNKNOWN} if it is unknown.
    */
   IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid);
   
   /**
    * Return the concept nids of all assemblages that are of the specified type
    * @param type The type to look for
    * @return all matching assemblage Nids.
    */
   NidSet getAssemblageNidsForType(IsaacObjectType type);
   
   /**
    * Store the type information for an assemblage.  
    * @param assemblageNid the nid to store type information for
    * @param type the type
    * @throws IllegalStateException If the assemblage already has a type which doesn't match the provided type
    */
   void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException;

   /**
    * Return the stored chronology data for the specified nid, or an empty optional, if no data is stored.
    * @param nid
    * @return the data
    */
   Optional<ByteArrayDataBuffer> getChronologyData(int nid);

   /**
    * Gets the SemanticChronology nids for component.
    *
    * @param componentNid the component nid
    * @return the SemanticChronology nids for component.  Should not return null, rather, return an empty array, if none
    */
   int[] getSemanticNidsForComponent(int componentNid);
   
   /**
    * Get the assemblage nid id that contains the specified nid.
    * @param nid The nid of the object to find the assemblage container for
    * @return the assemblage nid that contains the nid
    */
   OptionalInt getAssemblageOfNid(int nid);
   
   /**
    * Assign a nid to the specified assemblage.  Nids may only belong to a single assemblage, if 
    * you try to reassign a nid to a new assemblage, this will throw an IllegalArgumentException.
    * @param nid The nid to assign
    * @param assemblage The assemblage to assign it to
    * @throws IllegalArgumentException If you attempt to re-assign a nid to a new assemblage
    */
   void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException;
   
   /**
    * Return the stored taxonomy data for the specified concept in the given assemblage.
    * @param assemblageNid The assemblage to read the data from
    * @param conceptNid The concept within the assemblage to read the data from
    * @return The taxonomy data
    */
   int[] getTaxonomyData(int assemblageNid, int conceptNid);
   
   /**
    * Atomically updates the element at index {@code conceptNid} with the  results of applying the given function 
    * to the current and given values, returning the updated value. The function should  be side-effect-free, since 
    * it may be re-applied when attempted updates fail due to contention among threads.  The function is applied with 
    * the current value at index {@code conceptNid} as its first  argument, and the given update as the second argument.
    * @param assemblageNid The assemblage to read the data from
    * @param conceptNid The concept within the assemblage to read the data from, and to store the new data on
    * @param newData The new value
    * @param accumulatorFunction The function to merge the old and new values
    * @return The new, merged value.
    */
   int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction);

   /**
    * Return the version type, if known, for the specified assemblageNid.  
    * @param assemblageNid  The nid to lookup
    * @return The type, or {@link VersionType#UNKNOWN} if it is unknown.
    */
   VersionType getVersionTypeForAssemblageNid(int assemblageNid);
   
   /**
    * Store the type information for an assemblage.  
    * @param assemblageNid the nid to store version type information for
    * @param type the type
    * @throws IllegalStateException If the assemblage already has a version type which doesn't match the provided type
    */
   void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException;
   
   int getAssemblageMemoryInUse(int assemblageNid);

   int getAssemblageSizeOnDisk(int assemblageNid);
   
   /**
    * return true if the store has data for this nid, of the expected type, false otherwise.  
    * This operation will be quicker than {@link #getChronologyData(int)} if you are just testing for existence, 
    * as it saves some data copying time.  If you need the data, however, you should use the get with the optional return.
    * @param nid
    * @param ofType The expected type of the nid 
    * @return true if it has data of the matching type
    */
   boolean hasChronologyData(int nid, IsaacObjectType ofType);
   
   /**
    * Allow the addition of a dataWriteListener, that will get a copy of all data written via the 
    * {@link #putChronologyData(ChronologyImpl)} method.  The listener will be notified of the write after the 
    * {@link DataStore} implementations finishes writing the data.
    * @param dataWriteListener the listener to notify
    */
   void registerDataWriteListener(DataWriteListener dataWriteListener);
   
   /**
    * Remove a listener previously registered via {@link #registerDataWriteListener(DataWriteListener)}
    * @param dataWriteListener the listener to stop sending write events to
    */
   void unregisterDataWriteListener(DataWriteListener dataWriteListener);

   /**
    * @param assemblageNid
    * @return the stream of nids in the assemblage
    */
   IntStream getNidsForAssemblage(int assemblageNid);
   
   /**
    * @return true, if this implementation also implements {@link SequenceStore}
    */
   boolean implementsSequenceStore();
   
   /**
    * If a store chooses to implement the 'extended' methods, which allow the DataStore to store all of the 
    * data for ISAAC, they should override this method and return true.  In addition, they should implement the 
    * {@link ExtendedStore} interface.
    * @return true, if the extended store API is implemented
    */
   default boolean implementsExtendedStoreAPI() {
      return false;
   }
}

