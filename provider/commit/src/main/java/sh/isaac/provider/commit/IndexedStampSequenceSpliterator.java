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



package sh.isaac.provider.commit;

//~--- JDK imports ------------------------------------------------------------

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.list.IntArrayList;

import sh.isaac.api.commit.StampService;

//~--- classes ----------------------------------------------------------------

/**
 * The Class IndexedStampSequenceSpliterator.
 *
 * @author kec
 * @param <T> the generic type
 */
public abstract class IndexedStampSequenceSpliterator<T>
         implements Spliterator<T> {
   /** The iterator. */
   final PrimitiveIterator.OfInt iterator;

   /** The size. */
   final int size;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new indexed stamp sequence spliterator.
    *
    * @param keys the keys
    */
   public IndexedStampSequenceSpliterator(IntArrayList keys) {
      this.size     = keys.size();
      this.iterator = IntStream.of(keys.elements())
                               .iterator();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Characteristics.
    *
    * @return the int
    */
   @Override
   public final int characteristics() {
      return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SIZED;
   }

   /**
    * Estimate size.
    *
    * @return the long
    */
   @Override
   public final long estimateSize() {
      return this.size;
   }

   /**
    * Try split.
    *
    * @return the spliterator
    */
   @Override
   public final Spliterator<T> trySplit() {
      return null;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the iterator.
    *
    * @return the iterator
    */
   public PrimitiveIterator.OfInt getIterator() {
      return this.iterator;
   }
}

