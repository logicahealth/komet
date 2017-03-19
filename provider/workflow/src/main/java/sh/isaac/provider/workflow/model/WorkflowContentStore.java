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



package sh.isaac.provider.workflow.model;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.provider.workflow.model.contents.AbstractStorableWorkflowContents;

//~--- classes ----------------------------------------------------------------

/**
 * An generic storage class utilized to store all Workflow Content Store classes.
 * Contains fields and methods shared by all such Content Stores.
 *
 * Implements the Map interface, plus a couple of other convenience methods
 *
 * {@link AbstractStorableWorkflowContents}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class WorkflowContentStore<T extends AbstractStorableWorkflowContents>
         implements Map<UUID, T> {
   /** The Logger made available to each Workflow Content Store class */
   protected final Logger logger = LogManager.getLogger();

   /**
    * The storage mechanism of all entries. It is a map of key to Content Store
    * Entry type.  This map is backed by the metacontent store.
    */
   private ConcurrentMap<UUID, byte[]> map = null;
   private final Function<byte[], T>         deserializer_;

   //~--- constructors --------------------------------------------------------

   /**
    * Constructor for each new workflow content store based on the type
    * requested.
    *
    * @param type
    *            The type of workflow content store being instantiated
    * @param deserializer
    *            How to create a new object of type T from the submitted bytes
    */
   public WorkflowContentStore(ConcurrentMap<UUID, byte[]> dataStore, Function<byte[], T> deserializer) {
      this.deserializer_ = deserializer;
      this.map           = dataStore;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds a new entry to the content store.
    *
    * Key is generated and returned (and injected into the object) via {@link AbstractStorableWorkflowContents#setId(UUID)}
    *
    * @param entry
    *            The entry the already populated entry which is to be added
    *
    * @return the key of the new entry
    */
   public UUID add(T entry) {
      if (entry.getId() == null) {
         entry.setId(UUID.randomUUID());
      }

      this.map.put(entry.getId(), entry.getDataToWrite());
      return entry.getId();
   }

   /**
    * @see java.util.Map#clear()
    */
   @Override
   public void clear() {
      this.map.clear();
   }

   /**
    * @see java.util.Map#containsKey(java.lang.Object)
    */
   @Override
   public boolean containsKey(Object key) {
      return this.map.containsKey(key);
   }

   /**
    * Unsupported in this impl.
    */
   @Override
   public boolean containsValue(Object value) {
      throw new UnsupportedOperationException();
   }

   /**
    * @see java.util.Map#entrySet()
    */
   @Override
   public Set<java.util.Map.Entry<UUID, T>> entrySet() {
      final HashMap<UUID, T> retSet = new HashMap<>();

      for (final java.util.Map.Entry<UUID, byte[]> x: this.map.entrySet()) {
         retSet.put(x.getKey(), this.deserializer_.apply(x.getValue()));
      }

      return retSet.entrySet();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof WorkflowContentStore) {
         @SuppressWarnings("unchecked")
		final
         WorkflowContentStore<T> other = (WorkflowContentStore<T>) obj;

         return this.map.equals(other.map);
      } else {
         return false;
      }
   }

   /**
    * @see java.util.Map#keySet()
    */
   @Override
   public Set<UUID> keySet() {
      return this.map.keySet();
   }

   /**
    * @see java.util.Map#put(java.lang.Object, java.lang.Object)
    */
   @Override
   public T put(UUID key, T value) {
      if (value.getId() == null) {
         value.setId(key);
      } else if (!key.equals(value.getId())) {
         throw new RuntimeException("Attempt to store an object with a mis-matched key");
      }

      return this.deserializer_.apply(this.map.put(key, value.getDataToWrite()));
   }

   /**
    * Not implemented in this implementation
    */
   @Override
   public void putAll(Map<? extends UUID, ? extends T> m) {
      throw new UnsupportedOperationException();
   }

   /**
    * @see java.util.Map#remove(java.lang.Object)
    */
   @Override
   public T remove(Object key) {
      return this.deserializer_.apply(this.map.remove(key));
   }

   /**
    * @see java.util.Map#size()
    */
   @Override
   public int size() {
      return this.map.size();
   }

   @Override
   public String toString() {
      final StringBuffer buf = new StringBuffer();
      int          i   = 1;

      for (final UUID key: keySet()) {
         buf.append("\n\tStored Item #" + i++ + ": " + get(key).toString());
         buf.append("\n\n");
      }

      return "Stored Items: " + buf.toString();
   }

   /**
    * @see java.util.Map#values()
    */
   @Override
   public Collection<T> values() {
      return this.map.values()
                .stream()
                .map((bytes) -> this.deserializer_.apply(bytes))
                .collect(Collectors.toList());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @see java.util.Map#isEmpty()
    */
   @Override
   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   /**
    * @see java.util.Map#get(java.lang.Object)
    */
   @Override
   public T get(Object key) {
      return this.deserializer_.apply(this.map.get(key));
   }
}

