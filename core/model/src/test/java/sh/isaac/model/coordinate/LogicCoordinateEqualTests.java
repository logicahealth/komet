/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.model.coordinate;

import org.junit.Assert;
import org.junit.Test;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;

/**
 * @author  a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * some tests to validate equals behavior of the LogicCoordinate
 */
public class LogicCoordinateEqualTests
{
	@Test
	public void testEquals()
	{
		LogicCoordinateImpl l1 = new LogicCoordinateImpl(1, 2, 3, 4, 5);
		LogicCoordinateImpl l2 = new LogicCoordinateImpl(1, 2, 3, 4, 5);
		
		Assert.assertTrue(l1.equals(l2));
		Assert.assertTrue(l2.equals(l1));
		
		Assert.assertTrue(l1.equals(l1));
		Assert.assertTrue(l2.equals(l2));
		
		ObservableLogicCoordinateImpl ol1 = new ObservableLogicCoordinateImpl(l1);
		ObservableLogicCoordinateImpl ol2 = new ObservableLogicCoordinateImpl(l2);
		
		Assert.assertTrue(ol1.equals(ol2));
		Assert.assertTrue(ol2.equals(ol1));
		
		ol1 = new ObservableLogicCoordinateImpl(new LogicCoordinateImpl(1, 2, 3, 4, 5));
		ol2 = new ObservableLogicCoordinateImpl(new LogicCoordinateImpl(1, 2, 3, 4, 5));
		
		Assert.assertTrue(ol1.equals(ol2));
		Assert.assertTrue(ol2.equals(ol1));
		
		Assert.assertTrue(l1.equals(ol1));
		Assert.assertTrue(l2.equals(ol2));
		Assert.assertTrue(ol1.equals(l1));
		Assert.assertTrue(ol2.equals(l2));
		
		Assert.assertTrue(l1.equals(ol2));
		Assert.assertTrue(l2.equals(ol1));
		Assert.assertTrue(ol1.equals(l2));
		Assert.assertTrue(ol2.equals(l1));
		
		Assert.assertFalse(ol1.equals(new LogicCoordinateImpl(11, 2, 3, 4, 5)));
		Assert.assertFalse(ol1.equals(new LogicCoordinateImpl(1, 21, 3, 4, 5)));
		Assert.assertFalse(ol1.equals(new LogicCoordinateImpl(1, 2, 31, 4, 5)));
		Assert.assertFalse(ol1.equals(new LogicCoordinateImpl(1, 2, 3, 41, 5)));
		Assert.assertFalse(ol1.equals(new LogicCoordinateImpl(1, 2, 3, 4, 51)));
		
		Assert.assertTrue(((LogicCoordinate)l1).equals(ol1));
		Assert.assertTrue(((LogicCoordinate)l2).equals(ol2));
	}
}
