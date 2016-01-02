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
package gov.vha.isaac.ochre.model.sememe.dataTypes;

import static org.junit.Assert.assertEquals;
import java.beans.PropertyVetoException;
import java.io.IOException;
import org.junit.Test;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;

/**
 * {@link DynamicSememeArrayTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeArrayTest
{
	@Test
	public void testSerializationOne() throws PropertyVetoException, IOException
	{

		DynamicSememeIntegerImpl[] testDataOne = new DynamicSememeIntegerImpl[] {new DynamicSememeIntegerImpl(5), new DynamicSememeIntegerImpl(8), new DynamicSememeIntegerImpl(Integer.MAX_VALUE)};
		
		DynamicSememeArrayImpl<DynamicSememeIntegerImpl> testOne = new DynamicSememeArrayImpl<DynamicSememeIntegerImpl>(testDataOne);
		testOne.setNameIfAbsent("bar");
		
		assertEquals(3, testOne.getDataArray().length);
		
		assertEquals(5, testOne.getDataArray()[0].getDataInteger());
		assertEquals(8, testOne.getDataArray()[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, testOne.getDataArray()[2].getDataInteger());

		assertEquals(5, ((DynamicSememeIntegerImpl[])testOne.getDataObject())[0].getDataInteger());
		assertEquals(8, ((DynamicSememeIntegerImpl[])testOne.getDataObject())[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, ((DynamicSememeIntegerImpl[])testOne.getDataObject())[2].getDataInteger());

		assertEquals(5, ((DynamicSememeIntegerImpl[])testOne.getDataObjectProperty().get())[0].getDataInteger());
		assertEquals(8, ((DynamicSememeIntegerImpl[])testOne.getDataObjectProperty().get())[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, ((DynamicSememeIntegerImpl[])testOne.getDataObjectProperty().get())[2].getDataInteger());
		
		assertEquals(testOne.getDynamicSememeDataType(), DynamicSememeDataType.ARRAY);
		assertEquals(testOne.getDataArray()[0].getDynamicSememeDataType(), DynamicSememeDataType.INTEGER);
		assertEquals(testOne.getDataObjectProperty().getName(), "bar");
	}
	
	@Test
	public void testSerializationTwo() throws PropertyVetoException, IOException
	{

		DynamicSememeStringImpl[] testDataTwo = new DynamicSememeStringImpl[] {new DynamicSememeStringImpl("hi"), new DynamicSememeStringImpl("bye")};
		
		DynamicSememeArrayImpl<DynamicSememeStringImpl> testTwo = new DynamicSememeArrayImpl<DynamicSememeStringImpl>(testDataTwo);
		testTwo.setNameIfAbsent("bar");
		
		assertEquals(2, testTwo.getDataArray().length);
		
		assertEquals("hi", testTwo.getDataArray()[0].getDataString());
		assertEquals("bye", testTwo.getDataArray()[1].getDataString());

		assertEquals("hi", ((DynamicSememeStringImpl[])testTwo.getDataObject())[0].getDataString());
		assertEquals("bye", ((DynamicSememeStringImpl[])testTwo.getDataObject())[1].getDataString());

		assertEquals("hi", ((DynamicSememeStringImpl[])testTwo.getDataObjectProperty().get())[0].getDataString());
		assertEquals("bye", ((DynamicSememeStringImpl[])testTwo.getDataObjectProperty().get())[1].getDataString());
		
		assertEquals(testTwo.getDynamicSememeDataType(), DynamicSememeDataType.ARRAY);
		assertEquals(testTwo.getDataArray()[0].getDynamicSememeDataType(), DynamicSememeDataType.STRING);
		assertEquals(testTwo.getDataObjectProperty().getName(), "bar");
	}
}
