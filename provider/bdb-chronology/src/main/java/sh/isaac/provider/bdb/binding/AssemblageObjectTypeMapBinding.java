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
package sh.isaac.provider.bdb.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.concurrent.ConcurrentHashMap;
import sh.isaac.api.externalizable.IsaacObjectType;

/**
 *
 * @author kec
 */
public class AssemblageObjectTypeMapBinding extends TupleBinding<ConcurrentHashMap<Integer, IsaacObjectType>> {

   @Override
   public ConcurrentHashMap<Integer, IsaacObjectType> entryToObject(TupleInput input) {
      ConcurrentHashMap<Integer, IsaacObjectType> data = new ConcurrentHashMap<>();
      int size = input.readInt();
      for (int i = 0; i < size; i++) {
         int key = input.readInt();
         byte typeToken = input.readByte();
         data.put(key, IsaacObjectType.fromToken(typeToken));
      }
      return data;
   }

   @Override
   public void objectToEntry(ConcurrentHashMap<Integer, IsaacObjectType> data, TupleOutput output) {
      output.writeInt(data.size());
      data.forEach((key, value) -> {
         output.writeInt(key);
         output.writeByte(value.getToken());
      });
   }
}