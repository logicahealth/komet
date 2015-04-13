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
package org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes;

import static org.junit.Assert.assertEquals;
import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.junit.Test;

/**
 * {@link RefexDynamicArrayTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicArrayTest
{
	@Test
	public void testSerializationOne() throws PropertyVetoException, IOException, ContradictionException
	{

		RefexDynamicInteger[] testDataOne = new RefexDynamicInteger[] {new RefexDynamicInteger(5), new RefexDynamicInteger(8), new RefexDynamicInteger(Integer.MAX_VALUE)};
		
		RefexDynamicArray<RefexDynamicInteger> testOne = new RefexDynamicArray<RefexDynamicInteger>(testDataOne);
		testOne.setNameIfAbsent("bar");
		
		assertEquals(3, testOne.getDataArray().length);
		
		assertEquals(5, testOne.getDataArray()[0].getDataInteger());
		assertEquals(8, testOne.getDataArray()[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, testOne.getDataArray()[2].getDataInteger());

		assertEquals(5, ((RefexDynamicInteger[])testOne.getDataObject())[0].getDataInteger());
		assertEquals(8, ((RefexDynamicInteger[])testOne.getDataObject())[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, ((RefexDynamicInteger[])testOne.getDataObject())[2].getDataInteger());

		assertEquals(5, ((RefexDynamicInteger[])testOne.getDataObjectProperty().get())[0].getDataInteger());
		assertEquals(8, ((RefexDynamicInteger[])testOne.getDataObjectProperty().get())[1].getDataInteger());
		assertEquals(Integer.MAX_VALUE, ((RefexDynamicInteger[])testOne.getDataObjectProperty().get())[2].getDataInteger());
		
		assertEquals(testOne.getRefexDataType(), RefexDynamicDataType.ARRAY);
		assertEquals(testOne.getArrayDataType(), RefexDynamicDataType.INTEGER);
		assertEquals(testOne.getDataObjectProperty().getName(), "bar");
	}
	
	@Test
	public void testSerializationTwo() throws PropertyVetoException, IOException, ContradictionException
	{

		RefexDynamicString[] testDataTwo = new RefexDynamicString[] {new RefexDynamicString("hi"), new RefexDynamicString("bye")};
		
		RefexDynamicArray<RefexDynamicString> testTwo = new RefexDynamicArray<RefexDynamicString>(testDataTwo);
		testTwo.setNameIfAbsent("bar");
		
		assertEquals(2, testTwo.getDataArray().length);
		
		assertEquals("hi", testTwo.getDataArray()[0].getDataString());
		assertEquals("bye", testTwo.getDataArray()[1].getDataString());

		assertEquals("hi", ((RefexDynamicString[])testTwo.getDataObject())[0].getDataString());
		assertEquals("bye", ((RefexDynamicString[])testTwo.getDataObject())[1].getDataString());

		assertEquals("hi", ((RefexDynamicString[])testTwo.getDataObjectProperty().get())[0].getDataString());
		assertEquals("bye", ((RefexDynamicString[])testTwo.getDataObjectProperty().get())[1].getDataString());
		
		assertEquals(testTwo.getRefexDataType(), RefexDynamicDataType.ARRAY);
		assertEquals(testTwo.getArrayDataType(), RefexDynamicDataType.STRING);
		assertEquals(testTwo.getDataObjectProperty().getName(), "bar");
	}
}
