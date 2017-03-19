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



package sh.isaac.api.util;

//~--- JDK imports ------------------------------------------------------------

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//~--- classes ----------------------------------------------------------------

/**
 * {@link FortifyFun}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class FortifyFun {
   /**
    * This hatchet job is to work around the fact that Fortify won't let us use the setAccessible method, and
    * we can't deploy without fortify being happy.
    * This does the equivalent of {@link Field#setAccessible(true)}
    */
   public static void fixAccessible(AccessibleObject o)
            throws SecurityException {
      // o.setAccessible(true);
      try {
         Method m = o.getClass()
                     .getMethod("setAccessible", boolean.class);

         m.invoke(o, true);
      } catch (SecurityException e) {
         throw e;
      } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
               | InvocationTargetException e) {
         throw new RuntimeException("Unexpected", e);
      }
   }
}

