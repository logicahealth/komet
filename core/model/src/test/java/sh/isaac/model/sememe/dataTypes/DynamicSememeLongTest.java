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



package sh.isaac.model.sememe.dataTypes;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.assertEquals;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeLongTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeLongTest {
   @Test
   public void testSerialization()
            throws PropertyVetoException, IOException {
      final long[] testValues = new long[] {
         Long.MIN_VALUE, Long.MAX_VALUE, 0, 4, 6, 984, -234, -29837, 4532, 3289402830942309l, -9128934721874891l
      };

      for (final long l: testValues) {
         test(l);
      }
   }

   private void test(long value)
            throws PropertyVetoException, IOException {
      final DynamicSememeLongImpl l = new DynamicSememeLongImpl(value);

      assertEquals(value, l.getDataLong());
      assertEquals(value, ((Long) l.getDataObject()).longValue());
      assertEquals(value, ((Long) l.getDataObjectProperty()
                                   .get()).longValue());
   }
}

