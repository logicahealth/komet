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

import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeByteArrayTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeByteArrayTest {
   
   /**
    * Test serialization.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testSerialization()
            throws PropertyVetoException, IOException {
      final byte[][] testValues = new byte[][] {
         "".getBytes(), "sdfds".getBytes(), "ksldjflksdjfklsdjlfjsdlkfjdsljflksdjfklsd".getBytes()
      };

      for (final byte[] i: testValues) {
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
   private void test(byte[] value)
            throws PropertyVetoException, IOException {
      final DynamicSememeByteArrayImpl i = new DynamicSememeByteArrayImpl(value);

      assertEquals(value, i.getDataByteArray());
      assertEquals(value, i.getDataObject());
      assertEquals(value, i.getDataObjectProperty()
                                    .get());
      assertEquals(i.getDynamicSememeDataType(), DynamicSememeDataType.BYTEARRAY);
   }
}

