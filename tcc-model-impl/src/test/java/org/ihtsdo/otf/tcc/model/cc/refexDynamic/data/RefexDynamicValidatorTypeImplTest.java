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
package org.ihtsdo.otf.tcc.model.cc.refexDynamic.data;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicValidatorType;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicDouble;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicFloat;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicLong;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link RefexDynamicValidatorTypeImplTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicValidatorTypeImplTest
{
	@Test
	public void testOne() throws PropertyVetoException, IOException, ContradictionException
	{
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicDouble(5.0), new RefexDynamicDouble(3.0), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicDouble(1.0), new RefexDynamicDouble(3.0), null));
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicInteger(5), new RefexDynamicDouble(3.0), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicInteger(1), new RefexDynamicDouble(3.0), null));
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicFloat(5.0f), new RefexDynamicFloat(3.0f), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicDouble(1.0), new RefexDynamicFloat(3.0f), null));
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicLong(Long.MAX_VALUE), new RefexDynamicLong(30), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicLong(1), new RefexDynamicLong(3), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicLong(1), new RefexDynamicLong(1), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN.passesValidator(new RefexDynamicLong(1), new RefexDynamicDouble(1), null));
	}
	
	@Test
	public void testTwo() throws PropertyVetoException, IOException, ContradictionException
	{
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicDouble(5.0), new RefexDynamicDouble(3.0), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicDouble(1.0), new RefexDynamicDouble(3.0), null));
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicInteger(5), new RefexDynamicDouble(3.0), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicInteger(1), new RefexDynamicDouble(3.0), null));
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicFloat(5.0f), new RefexDynamicFloat(3.0f), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicDouble(1.0), new RefexDynamicFloat(3.0f), null));
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicLong(Long.MAX_VALUE), new RefexDynamicLong(30), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicLong(1), new RefexDynamicLong(3), null));
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicLong(1), new RefexDynamicLong(1), null));
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN.passesValidator(new RefexDynamicLong(1), new RefexDynamicDouble(1), null));
	}
	
	@Test
	public void testThree() throws PropertyVetoException, IOException, ContradictionException
	{
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicDouble(5.0), new RefexDynamicDouble(3.0), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicDouble(1.0), new RefexDynamicDouble(3.0), null));
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicInteger(5), new RefexDynamicDouble(3.0), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicInteger(1), new RefexDynamicDouble(3.0), null));
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicFloat(5.0f), new RefexDynamicFloat(3.0f), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicDouble(1.0), new RefexDynamicFloat(3.0f), null));
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicLong(Long.MAX_VALUE), new RefexDynamicLong(30), null));
		Assert.assertFalse(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicLong(1), new RefexDynamicLong(3), null));
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicLong(1), new RefexDynamicLong(1), null));
		Assert.assertTrue(RefexDynamicValidatorType.GREATER_THAN_OR_EQUAL.passesValidator(new RefexDynamicLong(1), new RefexDynamicDouble(1), null));
		
	}
	
	@Test
	public void testFour() throws PropertyVetoException, IOException, ContradictionException
	{
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicDouble(5.0), new RefexDynamicDouble(3.0), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicDouble(1.0), new RefexDynamicDouble(3.0), null));
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicInteger(5), new RefexDynamicDouble(3.0), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicInteger(1), new RefexDynamicDouble(3.0), null));
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicFloat(5.0f), new RefexDynamicFloat(3.0f), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicDouble(1.0), new RefexDynamicFloat(3.0f), null));
		Assert.assertFalse(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicLong(Long.MAX_VALUE), new RefexDynamicLong(30), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicLong(1), new RefexDynamicLong(3), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicLong(1), new RefexDynamicLong(1), null));
		Assert.assertTrue(RefexDynamicValidatorType.LESS_THAN_OR_EQUAL.passesValidator(new RefexDynamicLong(1), new RefexDynamicDouble(1), null));
	}
	
	@Test
	public void testInterval() throws PropertyVetoException, IOException, ContradictionException
	{
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicDouble(5.0), new RefexDynamicString("[4, 7]"), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicDouble(5.0), new RefexDynamicString("[4.0, 7.7]"), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicFloat(5.0f), new RefexDynamicString("[4, 7]"), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicFloat(5.0f), new RefexDynamicString("[4.0, 7.7]"), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicLong(5l), new RefexDynamicString("[4, 7]"), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicLong(5l), new RefexDynamicString("[4.0, 7.7]"), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(5), new RefexDynamicString("[4, 7]"), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(5), new RefexDynamicString("[4.0, 7.7]"), null));
		
		Assert.assertFalse(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(Integer.MAX_VALUE), 
				new RefexDynamicString("[4.0, 7.7]"), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(Integer.MAX_VALUE), 
				new RefexDynamicString("[4.0,2147483647]"), null));
		Assert.assertFalse(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(Integer.MAX_VALUE), 
				new RefexDynamicString(" [4.0 , 2147483647) "), null));
		
		Assert.assertFalse(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicDouble(Double.MIN_VALUE), 
				new RefexDynamicString(" [4.0 , 2147483647) "), null));
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(Integer.MIN_VALUE), 
				new RefexDynamicString(" [-2147483648 , 2147483647) "), null));
		Assert.assertFalse(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(Integer.MIN_VALUE), 
				new RefexDynamicString(" (-2147483648 , 2147483647) "), null));
		
		
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(5), 
				new RefexDynamicString(" (4 ,  ) "), null));
		
		Assert.assertTrue(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(5), 
				new RefexDynamicString(" (4 ,]"), null));
		
		Assert.assertFalse(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(3), 
				new RefexDynamicString(" (4 ,  ) "), null));
		
		try
		{
			Assert.assertFalse(RefexDynamicValidatorType.INTERVAL.passesValidator(new RefexDynamicInteger(3), 
					new RefexDynamicString(" (6 ,4) "), null));
			Assert.fail("Should have been an exception");
		}
		catch (Exception e)
		{
			//expected
		}
		
	}
	
	@Test
	public void testRegexp() throws PropertyVetoException, IOException, ContradictionException
	{
		Assert.assertTrue(RefexDynamicValidatorType.REGEXP.passesValidator(new RefexDynamicString("testWord"), new RefexDynamicString(".*"), null));
		Assert.assertTrue(RefexDynamicValidatorType.REGEXP.passesValidator(new RefexDynamicString("testWord"), new RefexDynamicString("[a-zA-Z]*"), null));
		Assert.assertFalse(RefexDynamicValidatorType.REGEXP.passesValidator(new RefexDynamicString("testWord"), new RefexDynamicString("[a-z]*"), null));
		Assert.assertTrue(RefexDynamicValidatorType.REGEXP.passesValidator(new RefexDynamicString("426"), new RefexDynamicString("\\d{3}?") , null));
		Assert.assertFalse(RefexDynamicValidatorType.REGEXP.passesValidator(new RefexDynamicString("4264"), new RefexDynamicString("\\d{3}?") , null));

	}
}
