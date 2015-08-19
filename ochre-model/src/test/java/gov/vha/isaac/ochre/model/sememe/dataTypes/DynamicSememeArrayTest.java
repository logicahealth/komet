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

		DynamicSememeInteger[] testDataOne = new DynamicSememeInteger[] {new DynamicSememeInteger(5), new DynamicSememeInteger(8), new DynamicSememeInteger(Integer.MAX_VALUE)};
		
		DynamicSememeArray<DynamicSememeInteger> testOne = new DynamicSememeArray<DynamicSememeInteger>(testDataOne);
		testOne.setNameIfAbsent("bar");
		
		assertEquals(3, testOne.getDataArray().length);
		
		assertEquals(5, testOne.getDataArray()[0].getDataInteger());
		assertEquals(8, testOne.getDataArray()[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, testOne.getDataArray()[2].getDataInteger());

		assertEquals(5, ((DynamicSememeInteger[])testOne.getDataObject())[0].getDataInteger());
		assertEquals(8, ((DynamicSememeInteger[])testOne.getDataObject())[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, ((DynamicSememeInteger[])testOne.getDataObject())[2].getDataInteger());

		assertEquals(5, ((DynamicSememeInteger[])testOne.getDataObjectProperty().get())[0].getDataInteger());
		assertEquals(8, ((DynamicSememeInteger[])testOne.getDataObjectProperty().get())[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, ((DynamicSememeInteger[])testOne.getDataObjectProperty().get())[2].getDataInteger());
		
		assertEquals(testOne.getDynamicSememeDataType(), DynamicSememeDataType.ARRAY);
		assertEquals(testOne.getArrayDataType(), DynamicSememeDataType.INTEGER);
		assertEquals(testOne.getDataObjectProperty().getName(), "bar");
	}
	
	@Test
	public void testSerializationTwo() throws PropertyVetoException, IOException
	{

		DynamicSememeString[] testDataTwo = new DynamicSememeString[] {new DynamicSememeString("hi"), new DynamicSememeString("bye")};
		
		DynamicSememeArray<DynamicSememeString> testTwo = new DynamicSememeArray<DynamicSememeString>(testDataTwo);
		testTwo.setNameIfAbsent("bar");
		
		assertEquals(2, testTwo.getDataArray().length);
		
		assertEquals("hi", testTwo.getDataArray()[0].getDataString());
		assertEquals("bye", testTwo.getDataArray()[1].getDataString());

		assertEquals("hi", ((DynamicSememeString[])testTwo.getDataObject())[0].getDataString());
		assertEquals("bye", ((DynamicSememeString[])testTwo.getDataObject())[1].getDataString());

		assertEquals("hi", ((DynamicSememeString[])testTwo.getDataObjectProperty().get())[0].getDataString());
		assertEquals("bye", ((DynamicSememeString[])testTwo.getDataObjectProperty().get())[1].getDataString());
		
		assertEquals(testTwo.getDynamicSememeDataType(), DynamicSememeDataType.ARRAY);
		assertEquals(testTwo.getArrayDataType(), DynamicSememeDataType.STRING);
		assertEquals(testTwo.getDataObjectProperty().getName(), "bar");
	}
}
