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
 * {@link DynamicSememeFloatTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeFloatTest
{
	@Test
	public void testSerialization() throws PropertyVetoException, IOException
	{

		float[] testValues = new float[] { Float.MIN_VALUE, Float.MAX_VALUE, 0, 4, 6, 4.56f, 4.292732f, 984, -234, -29837, 4532, 3289402830942309f, -9128934721874891f };

		for (float l : testValues)
		{
			test(l);
		}
	}

	private void test(float value) throws PropertyVetoException, IOException
	{
		DynamicSememeFloatImpl l = new DynamicSememeFloatImpl(value);
		l.setNameIfAbsent("foo");

		assertEquals(value, l.getDataFloat(), 0);
		assertEquals(value, (Float) l.getDataObject(), 0);
		assertEquals(value, (Float) l.getDataObjectProperty().get(), 0);
		assertEquals(l.getDynamicSememeDataType(), DynamicSememeDataType.FLOAT);
		assertEquals(l.getDataObjectProperty().getName(), "foo");
	}
}
