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
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeDouble;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeFloat;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeInteger;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLong;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeString;

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
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeDouble(5.0), new DynamicSememeDouble(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeDouble(1.0), new DynamicSememeDouble(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeInteger(5), new DynamicSememeDouble(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeInteger(1), new DynamicSememeDouble(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeFloat(5.0f), new DynamicSememeFloat(3.0f), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeDouble(1.0), new DynamicSememeFloat(3.0f), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeLong(Long.MAX_VALUE), new DynamicSememeLong(30), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeLong(1), new DynamicSememeLong(3), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeLong(1), new DynamicSememeLong(1), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN.passesValidator(new DynamicSememeLong(1), new DynamicSememeDouble(1), null, null));
	}
	
	@Test
	public void testTwo() throws PropertyVetoException, IOException
	{
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeDouble(5.0), new DynamicSememeDouble(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeDouble(1.0), new DynamicSememeDouble(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeInteger(5), new DynamicSememeDouble(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeInteger(1), new DynamicSememeDouble(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeFloat(5.0f), new DynamicSememeFloat(3.0f), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeDouble(1.0), new DynamicSememeFloat(3.0f), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeLong(Long.MAX_VALUE), new DynamicSememeLong(30), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeLong(1), new DynamicSememeLong(3), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeLong(1), new DynamicSememeLong(1), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN.passesValidator(new DynamicSememeLong(1), new DynamicSememeDouble(1), null, null));
	}
	
	@Test
	public void testThree() throws PropertyVetoException, IOException
	{
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeDouble(5.0), new DynamicSememeDouble(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeDouble(1.0), new DynamicSememeDouble(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeInteger(5), new DynamicSememeDouble(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeInteger(1), new DynamicSememeDouble(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeFloat(5.0f), new DynamicSememeFloat(3.0f), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeDouble(1.0), new DynamicSememeFloat(3.0f), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeLong(Long.MAX_VALUE), new DynamicSememeLong(30), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeLong(1), new DynamicSememeLong(3), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeLong(1), new DynamicSememeLong(1), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new DynamicSememeLong(1), new DynamicSememeDouble(1), null, null));
		
	}
	
	@Test
	public void testFour() throws PropertyVetoException, IOException
	{
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeDouble(5.0), new DynamicSememeDouble(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeDouble(1.0), new DynamicSememeDouble(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeInteger(5), new DynamicSememeDouble(3.0), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeInteger(1), new DynamicSememeDouble(3.0), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeFloat(5.0f), new DynamicSememeFloat(3.0f), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeDouble(1.0), new DynamicSememeFloat(3.0f), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeLong(Long.MAX_VALUE), new DynamicSememeLong(30), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeLong(1), new DynamicSememeLong(3), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeLong(1), new DynamicSememeLong(1), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new DynamicSememeLong(1), new DynamicSememeDouble(1), null, null));
	}
	
	@Test
	public void testInterval() throws PropertyVetoException, IOException
	{
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeDouble(5.0), new DynamicSememeString("[4, 7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeDouble(5.0), new DynamicSememeString("[4.0, 7.7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeFloat(5.0f), new DynamicSememeString("[4, 7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeFloat(5.0f), new DynamicSememeString("[4.0, 7.7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeLong(5l), new DynamicSememeString("[4, 7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeLong(5l), new DynamicSememeString("[4.0, 7.7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(5), new DynamicSememeString("[4, 7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(5), new DynamicSememeString("[4.0, 7.7]"), null, null));
		
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(Integer.MAX_VALUE), 
				new DynamicSememeString("[4.0, 7.7]"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(Integer.MAX_VALUE), 
				new DynamicSememeString("[4.0,2147483647]"), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(Integer.MAX_VALUE), 
				new DynamicSememeString(" [4.0 , 2147483647) "), null, null));
		
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeDouble(Double.MIN_VALUE), 
				new DynamicSememeString(" [4.0 , 2147483647) "), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(Integer.MIN_VALUE), 
				new DynamicSememeString(" [-2147483648 , 2147483647) "), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(Integer.MIN_VALUE), 
				new DynamicSememeString(" (-2147483648 , 2147483647) "), null, null));
		
		
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(5), 
				new DynamicSememeString(" (4 ,  ) "), null, null));
		
		Assert.assertTrue(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(5), 
				new DynamicSememeString(" (4 ,]"), null, null));
		
		Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(3), 
				new DynamicSememeString(" (4 ,  ) "), null, null));
		
		try
		{
			Assert.assertFalse(DynamicSememeValidatorType.INTERVAL.passesValidator(new DynamicSememeInteger(3), 
					new DynamicSememeString(" (6 ,4) "), null, null));
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
		Assert.assertTrue(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeString("testWord"), new DynamicSememeString(".*"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeString("testWord"), new DynamicSememeString("[a-zA-Z]*"), null, null));
		Assert.assertFalse(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeString("testWord"), new DynamicSememeString("[a-z]*"), null, null));
		Assert.assertTrue(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeString("426"), new DynamicSememeString("\\d{3}?") , null, null));
		Assert.assertFalse(DynamicSememeValidatorType.REGEXP.passesValidator(new DynamicSememeString("4264"), new DynamicSememeString("\\d{3}?") , null, null));

	}
}
