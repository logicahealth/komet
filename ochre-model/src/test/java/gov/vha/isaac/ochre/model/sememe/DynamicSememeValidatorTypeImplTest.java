/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model.sememe;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeDoubleImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeFloatImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeIntegerImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLongImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;

/**
 * {@link DynamicSememeValidatorTypeImplTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeValidatorTypeImplTest
{
	@Test
	public void testOne() throws PropertyVetoException, IOException
	{
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeDoubleImpl(5.0), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeDoubleImpl(1.0), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeIntegerImpl(5), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeIntegerImpl(1), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeFloatImpl(5.0f), new DynamicSememeFloatImpl(3.0f), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeDoubleImpl(1.0), new DynamicSememeFloatImpl(3.0f), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeLongImpl(Long.MAX_VALUE), new DynamicSememeLongImpl(30), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeLongImpl(3), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeLongImpl(1), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeDoubleImpl(1), null, null));
	}
	
	@Test
	public void testTwo() throws PropertyVetoException, IOException
	{
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeDoubleImpl(5.0), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeDoubleImpl(1.0), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeIntegerImpl(5), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeIntegerImpl(1), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeFloatImpl(5.0f), new DynamicSememeFloatImpl(3.0f), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeDoubleImpl(1.0), new DynamicSememeFloatImpl(3.0f), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeLongImpl(Long.MAX_VALUE), new DynamicSememeLongImpl(30), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeLongImpl(3), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeLongImpl(1), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeDoubleImpl(1), null, null));
	}
	
	@Test
	public void testThree() throws PropertyVetoException, IOException
	{
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeDoubleImpl(5.0), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeDoubleImpl(1.0), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeIntegerImpl(5), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeIntegerImpl(1), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeFloatImpl(5.0f), new DynamicSememeFloatImpl(3.0f), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeDoubleImpl(1.0), new DynamicSememeFloatImpl(3.0f), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeLongImpl(Long.MAX_VALUE), new DynamicSememeLongImpl(30), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeLongImpl(3), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeLongImpl(1), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeDoubleImpl(1), null, null));
		
	}
	
	@Test
	public void testFour() throws PropertyVetoException, IOException
	{
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeDoubleImpl(5.0), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeDoubleImpl(1.0), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeIntegerImpl(5), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeIntegerImpl(1), new DynamicSememeDoubleImpl(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeFloatImpl(5.0f), new DynamicSememeFloatImpl(3.0f), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeDoubleImpl(1.0), new DynamicSememeFloatImpl(3.0f), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeLongImpl(Long.MAX_VALUE), new DynamicSememeLongImpl(30), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeLongImpl(3), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeLongImpl(1), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeLongImpl(1), new DynamicSememeDoubleImpl(1), null, null));
	}
	
	@Test
	public void testInterval() throws PropertyVetoException, IOException
	{
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeDoubleImpl(5.0), new DynamicSememeStringImpl("[4, 7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeDoubleImpl(5.0), new DynamicSememeStringImpl("[4.0, 7.7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeFloatImpl(5.0f), new DynamicSememeStringImpl("[4, 7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeFloatImpl(5.0f), new DynamicSememeStringImpl("[4.0, 7.7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeLongImpl(5l), new DynamicSememeStringImpl("[4, 7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeLongImpl(5l), new DynamicSememeStringImpl("[4.0, 7.7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(5), new DynamicSememeStringImpl("[4, 7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(5), new DynamicSememeStringImpl("[4.0, 7.7]"), null, null));
		
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(Integer.MAX_VALUE), 
				new DynamicSememeStringImpl("[4.0, 7.7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(Integer.MAX_VALUE), 
				new DynamicSememeStringImpl("[4.0,2147483647]"), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(Integer.MAX_VALUE), 
				new DynamicSememeStringImpl(" [4.0 , 2147483647) "), null, null));
		
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeDoubleImpl(Double.MIN_VALUE), 
				new DynamicSememeStringImpl(" [4.0 , 2147483647) "), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(Integer.MIN_VALUE), 
				new DynamicSememeStringImpl(" [-2147483648 , 2147483647) "), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(Integer.MIN_VALUE), 
				new DynamicSememeStringImpl(" (-2147483648 , 2147483647) "), null, null));
		
		
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(5), 
				new DynamicSememeStringImpl(" (4 ,  ) "), null, null));
		
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(5), 
				new DynamicSememeStringImpl(" (4 ,]"), null, null));
		
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(3), 
				new DynamicSememeStringImpl(" (4 ,  ) "), null, null));
		
		try
		{
			Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeIntegerImpl(3), 
					new DynamicSememeStringImpl(" (6 ,4) "), null, null));
			Assert.fail("Should have been an exception");
		}
		catch (Exception e)
		{
			//expected
		}
		
	}
	
	@Test
	public void testRegexp() throws PropertyVetoException, IOException
	{
		Assert.assertTrue(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeStringImpl("testWord"), new DynamicSememeStringImpl(".*"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeStringImpl("testWord"), new DynamicSememeStringImpl("[a-zA-Z]*"), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeStringImpl("testWord"), new DynamicSememeStringImpl("[a-z]*"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeStringImpl("426"), new DynamicSememeStringImpl("\\d{3}?") , null, null));
		Assert.assertFalse(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeStringImpl("4264"), new DynamicSememeStringImpl("\\d{3}?") , null, null));

	}
}
