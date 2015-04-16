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
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicByteArray;
import org.junit.Test;

/**
 * {@link RefexDynamicByteArrayTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicByteArrayTest
{
	@Test
	public void testSerialization() throws PropertyVetoException, IOException, ContradictionException
	{

		byte[][] testValues = new byte[][] { "".getBytes(), "sdfds".getBytes(), "ksldjflksdjfklsdjlfjsdlkfjdsljflksdjfklsd".getBytes() };

		for (byte[] i : testValues)
		{
			test(i);
		}
	}

	private void test(byte[] value) throws PropertyVetoException, IOException, ContradictionException
	{
		RefexDynamicByteArray i = new RefexDynamicByteArray(value);
		i.setNameIfAbsent("foo");

		assertEquals(value, i.getDataByteArray());
		assertEquals(value, (byte[]) i.getDataObject());
		assertEquals(value, (byte[]) i.getDataObjectProperty().get());
		assertEquals(i.getRefexDataType(), RefexDynamicDataType.BYTEARRAY);
		assertEquals(i.getDataObjectProperty().getName(), "foo");
	}
}
