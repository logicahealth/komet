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



package sh.isaac.provider.metacontent;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

//~--- classes ----------------------------------------------------------------

public class MVStoreArbitraryTests {
   public static void main(String[] args) {
      long    temp = System.currentTimeMillis();
      final MVStore s    = MVStore.open("foo.test");

      System.out.println("Startup: " + (System.currentTimeMillis() - temp));
      temp = System.currentTimeMillis();

      final MVMap<UUID, String>  map  = s.<UUID, String>openMap("test");
      final MVMap<Integer, UUID> map2 = s.<Integer, UUID>openMap("test2");

      System.out.println("OpenMaps: " + (System.currentTimeMillis() - temp));
      temp = System.currentTimeMillis();

      UUID a = map2.get(5);

      System.out.println(a);
      System.out.println("Time to read by key: " + (System.currentTimeMillis() - temp));
      temp = System.currentTimeMillis();
      System.out.println(map2.containsValue(a));
      System.out.println("Time to find by value: " + (System.currentTimeMillis() - temp));
      temp = System.currentTimeMillis();
      a    = map2.get(5000000);
      System.out.println("Time to read by key: " + (System.currentTimeMillis() - temp));
      temp = System.currentTimeMillis();
      System.out.println(map2.containsValue(a));
      System.out.println("Time to find by value: " + (System.currentTimeMillis() - temp));

//    for (int i = 0; i < 10000000; i++)
//    {
//            UUID t = UUID.randomUUID();
//            map.put(t, i);
//            map2.put(i, t);
//    }
//    temp = System.currentTimeMillis();
//    final AtomicInteger ai = new AtomicInteger();
      System.out.println(map.get(UUID.fromString("36b310fc-434b-4447-9539-5e86c48383d1")));

//    map.keySet().stream().forEach(uuid -> ai.addAndGet(map.get(uuid)));
//    System.out.println("stream iterate " + (System.currentTimeMillis() - temp));
//    
//    temp = System.currentTimeMillis();
//    ai.set(0);
//    map.keySet().parallelStream().forEach(uuid -> ai.addAndGet(map.get(uuid)));
//    System.out.println("parallel  iterate " + (System.currentTimeMillis() - temp));
//
//    s.commit();
//    
//    System.out.println(s.getCacheSizeUsed());
//    System.out.println(map.size());
//    System.out.println(map2.size());
   }
}

