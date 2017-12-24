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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author kec
 */
public class SequenceGeneratorBinding extends TupleBinding<ConcurrentMap<Integer, AtomicInteger>> {

   @Override
   public ConcurrentMap<Integer, AtomicInteger> entryToObject(TupleInput input) {
      ConcurrentMap<Integer, AtomicInteger> data = new ConcurrentHashMap<>();
      int size = input.readInt();
      for (int i = 0; i < size; i++) {
         int key = input.readInt();
         int nextSequence = input.readInt();
         data.put(key, new AtomicInteger(nextSequence));
      }
      return data;
   }

   @Override
   public void objectToEntry(ConcurrentMap<Integer, AtomicInteger> data, TupleOutput output) {
      output.writeInt(data.size());
      data.forEach((key, value) -> {
         output.writeInt(key);
         output.writeInt(value.get());
      });
   }
   
}