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

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/23/16.
 */
public class IsaacExternalizableSpliterator
         implements Spliterator<IsaacExternalizable> {
   /** The streams. */
   List<Stream<? extends IsaacExternalizable>> streams = new ArrayList<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new ochre externalizable spliterator.
    */
   public IsaacExternalizableSpliterator() {
      this.streams.add(Get.conceptService()
                          .getConceptChronologyStream());
      this.streams.add(Get.assemblageService()
                          .getSemanticChronologyStream());
      this.streams.add(Get.commitService()
                          .getStampAliasStream());
      this.streams.add(Get.commitService()
                          .getStampCommentStream());
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Characteristics.
    *
    * @return the int
    */
   @Override
   public int characteristics() {
      return DISTINCT | NONNULL | IMMUTABLE;
   }

   /**
    * Estimate size.
    *
    * @return the long
    */
   @Override
   public long estimateSize() {
      return Long.MAX_VALUE;
   }

   /**
    * For each remaining.
    *
    * @param action the action
    */
   @Override
   public void forEachRemaining(Consumer<? super IsaacExternalizable> action) {
      for (final Stream<? extends IsaacExternalizable> stream: this.streams) {
         stream.forEach(action);
      }
   }

   /**
    * Try advance.
    *
    * @param action the action
    * @return true, if successful
    */
   @Override
   public boolean tryAdvance(Consumer<? super IsaacExternalizable> action) {
      throw new UnsupportedOperationException();
   }

   /**
    * Try split.
    *
    * @return the spliterator
    */
   @Override
   public Spliterator<IsaacExternalizable> trySplit() {
      return null;
   }
}

