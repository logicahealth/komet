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



package org.ihtsdo.ttk.lookup;

//~--- non-JDK imports --------------------------------------------------------

import org.openide.util.lookup.AbstractLookup;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author kec
 *
 * @param <T>
 */
public class InstanceWrapper<T> extends AbstractLookup.Pair<T> {

   /** Field description */
   private T instance;

   /** Field description */
   private String id;

   /** Field description */
   private String displayName;

   /** Field description */
   private Collection<InstancePropertyBI> instanceProperties;

   /**
    * Constructs ...
    *
    *
    * @param instance
    * @param id
    * @param displayName
    * @param instanceProperties
    */
   public InstanceWrapper(T instance, String id, String displayName, Collection<InstancePropertyBI> instanceProperties) {
      this.instance           = instance;
      this.id                 = id;
      this.displayName        = displayName;
      this.instanceProperties = instanceProperties;
   }

   /**
    * Method description
    *
    *
    * @param obj
    *
    * @return
    */
   @Override
   protected boolean creatorOf(Object obj) {
      return instance == obj;
   }

   /**
    * Method description
    *
    *
    * @param obj
    *
    * @return
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final InstanceWrapper<T> other = (InstanceWrapper<T>) obj;

      if (!Objects.equals(this.id, other.id)) {
         return false;
      }

      return true;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public int hashCode() {
      int hash = 7;

      hash = 97 * hash + Objects.hashCode(this.id);

      return hash;
   }

   /**
    * Method description
    *
    *
    * @param c
    *
    * @return
    */
   @Override
   protected boolean instanceOf(Class<?> c) {
      return c.isInstance(instance);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public String toString() {
      return "InstanceWrapper{" + displayName + ", properties="
             + instanceProperties+ ", id=" + id + '}';
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public String getDisplayName() {
      return displayName;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public String getId() {
      return id;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public T getInstance() {
      return instance;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public Collection<InstancePropertyBI> getInstanceProperties() {
      if (instanceProperties == null) {
         return Collections.EMPTY_LIST;
      }

      return instanceProperties;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public Class<? extends T> getType() {
      return (Class<? extends T>) instance.getClass();
   }
}
