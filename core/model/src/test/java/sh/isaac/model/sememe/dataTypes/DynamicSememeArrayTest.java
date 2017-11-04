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

import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import java.beans.PropertyVetoException;

import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeArrayTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeArrayTest {
   /**
    * Test serialization one.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testSerializationOne()
            throws PropertyVetoException, IOException {
      final DynamicIntegerImpl[] testDataOne = new DynamicIntegerImpl[] { new DynamicIntegerImpl(5),
                                                                                      new DynamicIntegerImpl(8),
                                                                                      new DynamicIntegerImpl(
                                                                                         Integer.MAX_VALUE) };
      final DynamicArrayImpl<DynamicIntegerImpl> testOne =
         new DynamicArrayImpl<>(testDataOne);

      assertEquals(3, testOne.getDataArray().length);
      assertEquals(5, testOne.getDataArray()[0]
                             .getDataInteger());
      assertEquals(8, testOne.getDataArray()[1]
                             .getDataInteger());
      assertEquals(Integer.MAX_VALUE, testOne.getDataArray()[2]
            .getDataInteger());
      assertEquals(5, ((DynamicIntegerImpl[]) testOne.getDataObject())[0].getDataInteger());
      assertEquals(8, ((DynamicIntegerImpl[]) testOne.getDataObject())[1].getDataInteger());
      assertEquals(Integer.MAX_VALUE, ((DynamicIntegerImpl[]) testOne.getDataObject())[2].getDataInteger());
      assertEquals(5, ((DynamicIntegerImpl[]) testOne.getDataObjectProperty()
            .get())[0].getDataInteger());
      assertEquals(8, ((DynamicIntegerImpl[]) testOne.getDataObjectProperty()
            .get())[1].getDataInteger());
      assertEquals(Integer.MAX_VALUE,
                   ((DynamicIntegerImpl[]) testOne.getDataObjectProperty()
                         .get())[2].getDataInteger());
      assertEquals(testOne.getDynamicDataType(), DynamicDataType.ARRAY);
      assertEquals(testOne.getDataArray()[0]
                          .getDynamicDataType(), DynamicDataType.INTEGER);
   }

   /**
    * Test serialization two.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testSerializationTwo()
            throws PropertyVetoException, IOException {
      final DynamicStringImpl[] testDataTwo = new DynamicStringImpl[] { new DynamicStringImpl("hi"),
                                                                                    new DynamicStringImpl(
                                                                                       "bye") };
      final DynamicArrayImpl<DynamicStringImpl> testTwo =
         new DynamicArrayImpl<>(testDataTwo);

      assertEquals(2, testTwo.getDataArray().length);
      assertEquals("hi", testTwo.getDataArray()[0]
                                .getDataString());
      assertEquals("bye", testTwo.getDataArray()[1]
                                 .getDataString());
      assertEquals("hi", ((DynamicStringImpl[]) testTwo.getDataObject())[0].getDataString());
      assertEquals("bye", ((DynamicStringImpl[]) testTwo.getDataObject())[1].getDataString());
      assertEquals("hi", ((DynamicStringImpl[]) testTwo.getDataObjectProperty()
            .get())[0].getDataString());
      assertEquals("bye", ((DynamicStringImpl[]) testTwo.getDataObjectProperty()
            .get())[1].getDataString());
      assertEquals(testTwo.getDynamicDataType(), DynamicDataType.ARRAY);
      assertEquals(testTwo.getDataArray()[0]
                          .getDynamicDataType(), DynamicDataType.STRING);
   }
}

