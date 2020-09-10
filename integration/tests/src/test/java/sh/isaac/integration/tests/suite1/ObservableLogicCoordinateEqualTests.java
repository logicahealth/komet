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
package sh.isaac.integration.tests.suite1;

import org.jvnet.testing.hk2testng.HK2;
import org.testng.Assert;
import org.testng.annotations.Test;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.LogicCoordinateImmutable;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;

/**
 * @author a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         some tests to validate equals behavior of the LogicCoordinate
 */
@HK2("integration")
@Test(suiteName = "suite1")
public class ObservableLogicCoordinateEqualTests
{
	@Test(groups = { "logicCoordEqualsTests" }, dependsOnGroups = { "load" })
	public void testEquals()
	{
		LogicCoordinateImmutable l1 = LogicCoordinateImmutable.make(-1, -2, -3, -4, -5, -6, -7);
		LogicCoordinateImmutable l2 = LogicCoordinateImmutable.make(-1, -2, -3, -4, -5, -6, -7);

		Assert.assertTrue(l1.equals(l2));
		Assert.assertTrue(l2.equals(l1));

		Assert.assertTrue(l1.equals(l1));
		Assert.assertTrue(l2.equals(l2));

		ObservableLogicCoordinateImpl ol1 = new ObservableLogicCoordinateImpl(l1);
		ObservableLogicCoordinateImpl ol2 = new ObservableLogicCoordinateImpl(l2);

		Assert.assertTrue(ol1.equals(ol2));
		Assert.assertTrue(ol2.equals(ol1));

		ol1 = new ObservableLogicCoordinateImpl(LogicCoordinateImmutable.make(-1, -2, -3, -4, -5, -6, -7));
		ol2 = new ObservableLogicCoordinateImpl(LogicCoordinateImmutable.make(-1, -2, -3, -4, -5, -6, -7));

		Assert.assertTrue(ol1.equals(ol2));
		Assert.assertTrue(ol2.equals(ol1));

		Assert.assertTrue(l1.equals(ol1.toLogicCoordinateImmutable()));
		Assert.assertTrue(l2.equals(ol2.toLogicCoordinateImmutable()));
		Assert.assertTrue(ol1.toLogicCoordinateImmutable().equals(l1));
		Assert.assertTrue(ol2.toLogicCoordinateImmutable().equals(l2));

		Assert.assertTrue(l1.equals(ol2.toLogicCoordinateImmutable()));
		Assert.assertTrue(l2.equals(ol1.toLogicCoordinateImmutable()));
		Assert.assertTrue(ol1.toLogicCoordinateImmutable().equals(l2));
		Assert.assertTrue(ol2.toLogicCoordinateImmutable().equals(l1));

		Assert.assertFalse(ol1.equals(LogicCoordinateImmutable.make(-11, -2, -3, -4, -5, -6, -7)));
		Assert.assertFalse(ol1.equals(LogicCoordinateImmutable.make(1, 21, 3, 4, 5, 6, 7)));
		Assert.assertFalse(ol1.equals(LogicCoordinateImmutable.make(1, 2, 31, 4, 5, 6, 7)));
		Assert.assertFalse(ol1.equals(LogicCoordinateImmutable.make(1, 2, 3, 41, 5, 6, 7)));
		Assert.assertFalse(ol1.equals(LogicCoordinateImmutable.make(1, 2, 3, 4, 51, 6, 7)));

		Assert.assertTrue(((LogicCoordinate) l1).equals(ol1.toLogicCoordinateImmutable()));
		Assert.assertTrue(((LogicCoordinate) l2).equals(ol2.toLogicCoordinateImmutable()));
	}
}
