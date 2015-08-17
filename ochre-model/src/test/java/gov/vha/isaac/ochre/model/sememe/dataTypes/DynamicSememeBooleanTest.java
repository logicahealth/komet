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
 * {@link DynamicSememeBooleanTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeBooleanTest
{
	@Test
	public void testSerialization() throws PropertyVetoException, IOException
	{

		boolean[] testValues = new boolean[] { true, false };

		for (boolean i : testValues)
		{
			test(i);
		}
	}

	private void test(boolean value) throws PropertyVetoException, IOException
	{
		DynamicSememeBoolean i = new DynamicSememeBoolean(value);
		i.setNameIfAbsent("foo");

		assertEquals(value, i.getDataBoolean());
		assertEquals(value, (Boolean) i.getDataObject());
		assertEquals(value, (Boolean) i.getDataObjectProperty().get());
		assertEquals(i.getDynamicSememeDataType(), DynamicSememeDataType.BOOLEAN);
		assertEquals(i.getDataObjectProperty().getName(), "foo");
	}
}
