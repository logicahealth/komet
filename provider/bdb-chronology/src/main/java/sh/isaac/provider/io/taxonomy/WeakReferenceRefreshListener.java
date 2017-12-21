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
package sh.isaac.provider.io.taxonomy;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.UUID;
import sh.isaac.api.RefreshListener;


   /**
    * The Class ChangeListenerReference.
    */
class WeakReferenceRefreshListener
           extends WeakReference<RefreshListener>
           implements Comparable<WeakReferenceRefreshListener> {

      /**
       * The listener uuid.
       */
      UUID listenerUuid;

      //~--- constructors -----------------------------------------------------
      /**
       * Instantiates a new change listener reference.
       *
       * @param referent the referent
       */
      public WeakReferenceRefreshListener(RefreshListener referent) {
         super(referent);
         this.listenerUuid = referent.getListenerUuid();
      }

      /**
       * Instantiates a new change listener reference.
       *
       * @param referent the referent
       * @param q the q
       */
      public WeakReferenceRefreshListener(RefreshListener referent,
              ReferenceQueue<? super RefreshListener> q) {
         super(referent, q);
         this.listenerUuid = referent.getListenerUuid();
      }

      //~--- methods ----------------------------------------------------------
      /**
       * Compare to.
       *
       * @param o the o
       * @return the int
       */
      @Override
      public int compareTo(WeakReferenceRefreshListener o) {
         return this.listenerUuid.compareTo(o.listenerUuid);
      }

      /**
       * Equals.
       *
       * @param obj the obj
       * @return true, if successful
       */
      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }

         if (getClass() != obj.getClass()) {
            return false;
         }

         final WeakReferenceRefreshListener other = (WeakReferenceRefreshListener) obj;

         return Objects.equals(this.listenerUuid, other.listenerUuid);
      }

      /**
       * Hash code.
       *
       * @return the int
       */
      @Override
      public int hashCode() {
         int hash = 3;

         hash = 67 * hash + Objects.hashCode(this.listenerUuid);
         return hash;
      }
   }
