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
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicString;
import org.junit.Test;

/**
 * {@link RefexDynamicStringTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicStringTest
{
	@Test
	public void testSerialization() throws PropertyVetoException, IOException, ContradictionException
	{

		String[] testValues = new String[] { "", "sdfds", "ksldjflksdjfklsdjlfjsdlkfjdsljflksdjfklsd" };

		for (String i : testValues)
		{
			test(i);
		}
	}

	private void test(String value) throws PropertyVetoException, IOException, ContradictionException
	{
		RefexDynamicString i = new RefexDynamicString(value);
		i.setNameIfAbsent("foo");

		assertEquals(value, i.getDataString());
		assertEquals(value, (String) i.getDataObject());
		assertEquals(value, (String) i.getDataObjectProperty().get());
		assertEquals(i.getRefexDataType(), RefexDynamicDataType.STRING);
		assertEquals(i.getDataObjectProperty().getName(), "foo");
	}
}
