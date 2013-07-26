/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.api.store;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author kec
 */
public abstract class TermChangeListener {
   private static AtomicInteger listenerIdSequence = new AtomicInteger();

   //~--- fields --------------------------------------------------------------

   private int listenerId = listenerIdSequence.incrementAndGet();

   //~--- methods -------------------------------------------------------------

   public abstract void changeNotify(long sequence, Set<Integer> changedXrefs, Set<Integer> changedComponents);

   //~--- get methods ---------------------------------------------------------

   public int getListenerId() {
      return listenerId;
   }
}
