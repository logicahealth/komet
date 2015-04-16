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
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicUUID;
import org.junit.Test;

/**
 * {@link RefexDynamicUUIDTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicUUIDTest
{
	@Test
	public void testSerialization() throws PropertyVetoException, IOException, ContradictionException
	{

		UUID[] testValues = new UUID[] { UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};

		for (UUID uuid : testValues)
		{
			test(uuid);
		}
	}

	private void test(UUID value) throws PropertyVetoException, IOException, ContradictionException
	{
		RefexDynamicUUID uuid = new RefexDynamicUUID(value);
		uuid.setNameIfAbsent("foo");

		assertEquals(value, uuid.getDataUUID());
		assertEquals(value, (UUID) uuid.getDataObject());
		assertEquals(value, (UUID) uuid.getDataObjectProperty().get());
		assertEquals(uuid.getRefexDataType(), RefexDynamicDataType.UUID);
		assertEquals(uuid.getDataObjectProperty().getName(), "foo");
	}
}
