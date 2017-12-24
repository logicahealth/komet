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
package sh.isaac.provider.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 *
 * @author kec
 */
public class LongSpineBinding extends TupleBinding<AtomicLongArray> {

   @Override
   public AtomicLongArray entryToObject(TupleInput input) {
      int size = input.readInt();
      long[] data = new long[size];
      for (int i = 0; i < size; i++) {
         data[i] = input.readLong();
      }
      return new AtomicLongArray(data);
   }

   @Override
   public void objectToEntry(AtomicLongArray data, TupleOutput output) {
      int length = data.length();
      output.writeInt(length);
      for (int i = 0; i < length; i++) {
         output.writeLong(data.get(i));
      }
   }
}