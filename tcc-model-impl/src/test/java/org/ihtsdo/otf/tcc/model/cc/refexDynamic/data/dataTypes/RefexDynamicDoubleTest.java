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
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicDouble;
import org.junit.Test;

/**
 * {@link RefexDynamicDoubleTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicDoubleTest
{
	@Test
	public void testSerialization() throws PropertyVetoException, IOException, ContradictionException
	{

		double[] testValues = new double[] { Double.MIN_VALUE, Double.MAX_VALUE, 0, 4, 6, 4.56, 4.292732, 984, -234, -29837, 4532, 3289402830942309d, -9128934721874891d };

		for (double l : testValues)
		{
			test(l);
		}
	}

	private void test(double value) throws PropertyVetoException, IOException, ContradictionException
	{
		RefexDynamicDouble l = new RefexDynamicDouble(value);
		l.setNameIfAbsent("foo");

		assertEquals(value, l.getDataDouble(), 0);
		assertEquals(value, (Double) l.getDataObject(), 0);
		assertEquals(value, (Double) l.getDataObjectProperty().get(), 0);
		assertEquals(l.getRefexDataType(), RefexDynamicDataType.DOUBLE);
		assertEquals(l.getDataObjectProperty().getName(), "foo");
	}
}
