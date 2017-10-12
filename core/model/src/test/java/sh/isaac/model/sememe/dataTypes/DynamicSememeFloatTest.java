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

import sh.isaac.model.semantic.types.DynamicFloatImpl;
import java.beans.PropertyVetoException;

import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeFloatTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeFloatTest {
   /**
    * Test serialization.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testSerialization()
            throws PropertyVetoException, IOException {
      final float[] testValues = new float[] {
         Float.MIN_VALUE, Float.MAX_VALUE, 0, 4, 6, 4.56f, 4.292732f, 984, -234, -29837, 4532, 3289402830942309f,
         -9128934721874891f
      };

      for (final float l: testValues) {
         test(l);
      }
   }

   /**
    * Test.
    *
    * @param value the value
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void test(float value)
            throws PropertyVetoException, IOException {
      final DynamicFloatImpl l = new DynamicFloatImpl(value);

      assertEquals(value, l.getDataFloat(), 0);
      assertEquals(value, (Float) l.getDataObject(), 0);
      assertEquals(value, (Float) l.getDataObjectProperty()
                                   .get(), 0);
      assertEquals(l.getDynamicSememeDataType(), DynamicDataType.FLOAT);
   }
}

