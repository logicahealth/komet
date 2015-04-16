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
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicLong;
import org.junit.Test;

/**
 * {@link RefexDynamicLongTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicLongTest
{
	@Test
	public void testSerialization() throws PropertyVetoException, IOException, ContradictionException
	{

		long[] testValues = new long[] { Long.MIN_VALUE, Long.MAX_VALUE, 0, 4, 6, 984, -234, -29837, 4532, 3289402830942309l, -9128934721874891l };

		for (long l : testValues)
		{
			test(l);
		}
	}

	private void test(long value) throws PropertyVetoException, IOException, ContradictionException
	{
		RefexDynamicLong l = new RefexDynamicLong(value);
		l.setNameIfAbsent("foo");
		
		assertEquals(value, l.getDataLong());
		assertEquals(value, ((Long) l.getDataObject()).longValue());
		assertEquals(value, ((Long) l.getDataObjectProperty().get()).longValue());
		assertEquals(l.getDataObjectProperty().getName(), "foo");
	}
}
