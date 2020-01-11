/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
 * US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author kec
 */
public class AlphanumComparatorTest
{

	public AlphanumComparatorTest()
	{
	}

	@BeforeClass
	public static void setUpClass()
	{
	}

	@AfterClass
	public static void tearDownClass()
	{
	}

	@Before
	public void setUp()
	{
	}

	@After
	public void tearDown()
	{
	}

	/**
	 * Test of compare method, of class NaturalOrder.
	 */
	@Test
	public void testCompare()
	{
		String s1 = "i";
		String s2 = "I";
		int expResult = 0;
		int result = AlphanumComparator.compare(s1, s2, true);
		assertEquals(expResult, result);

		s1 = "ISAAC metadata (ISAAC)";
		s2 = "health concept";
		result = AlphanumComparator.compare(s1, s2, true);
		assertTrue(result > 0);

		s1 = "1";
		s2 = "10";
		result = AlphanumComparator.compare(s1, s2, true);
		assertTrue(result < 0);

		s1 = "2";
		s2 = "10";
		result = AlphanumComparator.compare(s1, s2, true);
		assertTrue(result < 0);
	}
	
	//Some code for seeing how they sort...
//	public static void main(String[] args)
//	{
//		String[] foo = new String[] {
//				"10X Radonius",
//				"20X Radonius",
//				"20X Radonius Prime",
//				"30X Radonius",
//				"40X Radonius",
//				"200X Radonius",
//				"1000X Radonius Maximus",
//				"Allegia 6R Clasteron",
//				"Allegia 50 Clasteron",
//				"Allegia 50B Clasteron",
//				"Allegia 51 Clasteron",
//				"Allegia 500 Clasteron",
//				"Alpha 2",
//				"Alpha 2A",
//				"Alpha 2A-900",
//				"Alpha 2A-8000",
//				"Alpha 100",
//				"Alpha 200",
//				"Callisto Morphamax",
//				"Callisto Morphamax 500",
//				"Callisto Morphamax 600",
//				"Callisto Morphamax 700",
//				"Callisto Morphamax 5000",
//				"Callisto Morphamax 6000 SE",
//				"Callisto Morphamax 6000 SE2",
//				"Callisto Morphamax 7000",
//				"Xiph Xlater 5",
//				"Xiph Xlater 40",
//				"Xiph Xlater 50",
//				"Xiph Xlater 58",
//				"Xiph Xlater 300",
//				"Xiph Xlater 500",
//				"Xiph Xlater 2000",
//				"Xiph Xlater 5000",
//				"Xiph Xlater 10000",
//				"000345",
//				"000346",
//				"34.5",
//				"35.5",
//				"0.123",
//				"0.124",
//				"-5",
//				"-6",
//				"[fred]",
//				"[jane]",
//				"000.123",
//				"00000345"
//		};
//		
//		Arrays.sort(foo, AlphanumComparator.getCachedInstance(true));
//		for (String s : foo)
//		{
//			System.out.println(s);
//		}
//		
//		Arrays.sort(foo, new NaturalOrder());
//		for (String s : foo)
//		{
//			System.out.println(s);
//		}
//	}
}
