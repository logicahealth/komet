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



package sh.isaac.model.semantic.dataTypes;

//~--- JDK imports ------------------------------------------------------------

import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import java.beans.PropertyVetoException;

import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeIntegerTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeIntegerTest {
   /**
    * Test serialization.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testSerialization()
            throws PropertyVetoException, IOException {
      final int[] testValues = new int[] {
         Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 4, 6, 984, -234, -29837, 4532
      };

      for (final int i: testValues) {
         test(i);
      }
   }

   /**
    * Test.
    *
    * @param value the value
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void test(int value)
            throws PropertyVetoException, IOException {
      final DynamicIntegerImpl i = new DynamicIntegerImpl(value);

      assertEquals(value, i.getDataInteger());
      assertEquals(value, ((Integer) i.getDataObject()).intValue());
      assertEquals(value, ((Integer) i.getDataObjectProperty()
                                      .get()).intValue());
      assertEquals(i.getDynamicDataType(), DynamicDataType.INTEGER);
   }
}

