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



package sh.isaac.model.semantic;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Assert;
import org.junit.Test;

import sh.isaac.api.component.semantic.version.dynamic.DynamicValidatorType;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicDoubleImpl;
import sh.isaac.model.semantic.types.DynamicFloatImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicLongImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;

//~--- classes ----------------------------------------------------------------

/**
 * {@link LogicCoordinateTests}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeValidatorTypeImplTest {
   /**
    * Test four.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testFour()
            throws PropertyVetoException, IOException {
      Assert.assertFalse(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicDoubleImpl(5.0),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicDoubleImpl(1.0),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertFalse(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicIntegerImpl(5),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicIntegerImpl(1),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertFalse(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicFloatImpl(5.0f),
            new DynamicFloatImpl(3.0f),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicDoubleImpl(1.0),
            new DynamicFloatImpl(3.0f),
            -1));
      Assert.assertFalse(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicLongImpl(Long.MAX_VALUE),
                new DynamicLongImpl(30),
                -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicLongImpl(1),
            new DynamicLongImpl(3),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicLongImpl(1),
            new DynamicLongImpl(1),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicLongImpl(1),
            new DynamicDoubleImpl(1),
            -1));
   }

   /**
    * Test interval.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testInterval()
            throws PropertyVetoException, IOException {
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicDoubleImpl(5.0),
            new DynamicStringImpl("[4, 7]"),
            -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicDoubleImpl(5.0),
            new DynamicStringImpl("[4.0, 7.7]"),
            -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicFloatImpl(5.0f),
            new DynamicStringImpl("[4, 7]"),
            -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicFloatImpl(5.0f),
            new DynamicStringImpl("[4.0, 7.7]"),
            -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicLongImpl(5l),
            new DynamicStringImpl("[4, 7]"),
            -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicLongImpl(5l),
            new DynamicStringImpl("[4.0, 7.7]"),
            -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(5),
            new DynamicStringImpl("[4, 7]"),
            -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(5),
            new DynamicStringImpl("[4.0, 7.7]"),
            -1));
      Assert.assertFalse(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(Integer.MAX_VALUE),
                new DynamicStringImpl("[4.0, 7.7]"),
                -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(Integer.MAX_VALUE),
                new DynamicStringImpl("[4.0,2147483647]"),
                -1));
      Assert.assertFalse(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(Integer.MAX_VALUE),
                new DynamicStringImpl(" [4.0 , 2147483647) "),
                -1));
      Assert.assertFalse(DynamicValidatorType.INTERVAL.passesValidator(new DynamicDoubleImpl(Double.MIN_VALUE),
                new DynamicStringImpl(" [4.0 , 2147483647) "),
                -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(Integer.MIN_VALUE),
                new DynamicStringImpl(" [-2147483648 , 2147483647) "),
                -1));
      Assert.assertFalse(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(Integer.MIN_VALUE),
                new DynamicStringImpl(" (-2147483648 , 2147483647) "),
                -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(5),
            new DynamicStringImpl(" (4 ,  ) "),
            -1));
      Assert.assertTrue(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(5),
            new DynamicStringImpl(" (4 ,]"),
            -1));
      Assert.assertFalse(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(3),
            new DynamicStringImpl(" (4 ,  ) "),
            -1));

      try {
         Assert.assertFalse(DynamicValidatorType.INTERVAL.passesValidator(new DynamicIntegerImpl(3),
               new DynamicStringImpl(" (6 ,4) "),
               -1));
         Assert.fail("Should have been an exception");
      } catch (final Exception e) {
         // expected
      }
   }

   /**
    * Test one.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testOne()
            throws PropertyVetoException, IOException {
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicDoubleImpl(5.0),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicDoubleImpl(1.0),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicIntegerImpl(5),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicIntegerImpl(1),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicFloatImpl(5.0f),
            new DynamicFloatImpl(3.0f),
            -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicDoubleImpl(1.0),
            new DynamicFloatImpl(3.0f),
            -1));
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicLongImpl(Long.MAX_VALUE),
                new DynamicLongImpl(30),
                -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicLongImpl(1),
            new DynamicLongImpl(3),
            -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicLongImpl(1),
            new DynamicLongImpl(1),
            -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN.passesValidator(new DynamicLongImpl(1),
            new DynamicDoubleImpl(1),
            -1));
   }

   /**
    * Test regexp.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testRegexp()
            throws PropertyVetoException, IOException {
      Assert.assertTrue(DynamicValidatorType.REGEXP.passesValidator(new DynamicArrayImpl(new DynamicData[] { new DynamicStringImpl("testWord") }),
              new DynamicStringImpl(".*"),
              -1));
      Assert.assertTrue(DynamicValidatorType.REGEXP.passesValidator(new DynamicArrayImpl(new DynamicData[] { new DynamicStringImpl("testWord") }),
              new DynamicStringImpl("[a-zA-Z]*"),
              -1));
      Assert.assertFalse(DynamicValidatorType.REGEXP.passesValidator(new DynamicStringImpl("testWord"),
            new DynamicStringImpl("[a-z]*"),
            -1));
      Assert.assertTrue(DynamicValidatorType.REGEXP.passesValidator(new DynamicStringImpl("426"),
            new DynamicStringImpl("\\d{3}?"),
            -1));
      Assert.assertFalse(DynamicValidatorType.REGEXP.passesValidator(new DynamicStringImpl("4264"),
            new DynamicStringImpl("\\d{3}?"),
            -1));
   }

   /**
    * Test three.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testThree()
            throws PropertyVetoException, IOException {
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicDoubleImpl(5.0),
                new DynamicDoubleImpl(3.0),
                -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicDoubleImpl(1.0),
                new DynamicDoubleImpl(3.0),
                -1));
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicIntegerImpl(5),
                new DynamicDoubleImpl(3.0),
                -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicIntegerImpl(1),
                new DynamicDoubleImpl(3.0),
                -1));
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicFloatImpl(5.0f),
                new DynamicFloatImpl(3.0f),
                -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicDoubleImpl(1.0),
                new DynamicFloatImpl(3.0f),
                -1));
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicLongImpl(Long.MAX_VALUE),
                new DynamicLongImpl(30),
                -1));
      Assert.assertFalse(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicLongImpl(1),
            new DynamicLongImpl(3),
            -1));
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicLongImpl(1),
            new DynamicLongImpl(1),
            -1));
      Assert.assertTrue(DynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicLongImpl(1),
            new DynamicDoubleImpl(1),
            -1));
   }

   /**
    * Test two.
    *
    * @throws PropertyVetoException the property veto exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testTwo()
            throws PropertyVetoException, IOException {
      Assert.assertFalse(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicDoubleImpl(5.0),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicDoubleImpl(1.0),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertFalse(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicIntegerImpl(5),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicIntegerImpl(1),
            new DynamicDoubleImpl(3.0),
            -1));
      Assert.assertFalse(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicFloatImpl(5.0f),
            new DynamicFloatImpl(3.0f),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicDoubleImpl(1.0),
            new DynamicFloatImpl(3.0f),
            -1));
      Assert.assertFalse(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicLongImpl(Long.MAX_VALUE),
            new DynamicLongImpl(30),
            -1));
      Assert.assertTrue(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicLongImpl(1),
            new DynamicLongImpl(3),
            -1));
      Assert.assertFalse(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicLongImpl(1),
            new DynamicLongImpl(1),
            -1));
      Assert.assertFalse(DynamicValidatorType.LESS_THAN.passesValidator(new DynamicLongImpl(1),
            new DynamicDoubleImpl(1),
            -1));
   }
}

