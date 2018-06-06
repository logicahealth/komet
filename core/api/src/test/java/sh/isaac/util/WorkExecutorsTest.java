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

package sh.isaac.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import sh.isaac.api.util.WorkExecutors;

public class WorkExecutorsTest {
   
   
   /**
    * Just some code for playing with the threading behavior...
    *
    * @param args the arguments
    * @throws InterruptedException the interrupted exception
    * @throws SecurityException 
    * @throws NoSuchMethodException 
    * @throws InvocationTargetException 
    * @throws IllegalArgumentException 
    * @throws IllegalAccessException 
    * @throws InstantiationException 
    */
   public static void main(String[] args)
            throws Exception {
      
      Constructor<WorkExecutors> c = WorkExecutors.class.getDeclaredConstructor();
      c.setAccessible(true);
      final WorkExecutors we = c.newInstance();
      
      Method m = WorkExecutors.class.getDeclaredMethod("startMe");
      m.setAccessible(true);
      m.invoke(we);

      final AtomicInteger counter = new AtomicInteger();

      for (int i = 0; i < 24; i++) {
         System.out.println("submit " + i);
         we.getPotentiallyBlockingExecutor().submit(() -> {
                      final int id = counter.getAndIncrement();

                      System.out.println(id + " started");

                      try {
                         Thread.sleep(5000);
                      } catch (final InterruptedException e) {
                         e.printStackTrace();
                      }

                      System.out.println(id + " finished");
                   });
      }

      Thread.sleep(7000);
      System.out.println("Blocking test over");

      for (int i = 24; i < 48; i++) {
         System.out.println("submit " + i);
         we.getExecutor().submit(() -> {
                      final int id = counter.getAndIncrement();

                      System.out.println(id + " started");

                      try {
                         Thread.sleep(5000);
                      } catch (final InterruptedException e) {
                         e.printStackTrace();
                      }

                      System.out.println(id + " finished");
                   });
      }

      while (we.getExecutor()
               .getQueue()
               .size() > 0) {
         Thread.sleep(1000);
      }

      Thread.sleep(7000);
   }
}
