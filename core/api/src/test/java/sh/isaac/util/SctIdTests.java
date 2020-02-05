/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */

package sh.isaac.util;

import org.junit.Assert;
import org.junit.Test;
import sh.isaac.api.util.SctId;
import sh.isaac.api.util.SctId.TYPE;

/**
 * {@link SctIdTests}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SctIdTests
{
	@Test
	public void sctIDTestOne() throws Exception
	{
		//valid SCTIDs from https://confluence.ihtsdotools.org/display/DOCRELFMT/6.8+Example+SNOMED+CT+identifiers
		long[] tests = new long[] { 100005,100014, 100022, 1290023401004l, 1290023401015l, 9940000001029l, 11000001102l, 10989121108l,
				1290989121103l, 1290000001117l, 9940000001126l, 999999990989121104l};

		for (long l : tests)
		{
			Assert.assertTrue(SctId.isValidSctId(l));
		}
	}
	
	@Test
	public void constructTests() throws Exception
	{
		//valid SCTIDs from https://confluence.ihtsdotools.org/display/DOCRELFMT/6.8+Example+SNOMED+CT+identifiers
		long[] results = new long[] {100005,100014, 100022, 1290023401004l, 1290023401015l, 9940000001029l,
				11000001102l, 10989121108l, 1290989121103l, 1290000001117l, 9940000001126l, 999999990989121104l};
		
		TYPE[] inputTypes = new TYPE[] {TYPE.CONCEPT, TYPE.DESCRIPTION, TYPE.RELATIONSHIP, TYPE.CONCEPT, TYPE.DESCRIPTION, TYPE.RELATIONSHIP,
				TYPE.CONCEPT_LF, TYPE.CONCEPT_LF, TYPE.CONCEPT_LF, TYPE.DESCRIPTION_LF, TYPE.RELATIONSHIP_LF, TYPE.CONCEPT_LF};
		long[] inputIDs = new long[] {100, 100, 100, 1290023401l, 1290023401l, 9940000001l,
				1, 1, 129, 129, 994, 99999999};
		String[] inputNamespaces = new String[] {"", "", "", "", "", "",
				"1000001", "0989121", "0989121", "0000001", "0000001", "0989121"};
		
		for (int i = 0; i < results.length; i++)
		{
			SctId id = new SctId(results[i] + "");
			Assert.assertEquals(inputTypes[i], id.getType());
			Assert.assertEquals((long)inputIDs[i], id.getItemId());
			Assert.assertEquals(inputNamespaces[i], id.getNamespace().orElseGet(() -> ""));
		}
	}

	
	@Test
	public void genTests() throws Exception
	{
		//valid SCTIDs from https://confluence.ihtsdotools.org/display/DOCRELFMT/6.8+Example+SNOMED+CT+identifiers
		long[] results = new long[] {11000001102l, 10989121108l, 1290989121103l, 1290000001117l, 9940000001126l, 999999990989121104l};
		
		TYPE[] inputTypes = new TYPE[] {TYPE.CONCEPT_LF, TYPE.CONCEPT_LF, TYPE.CONCEPT_LF, TYPE.DESCRIPTION_LF, TYPE.RELATIONSHIP_LF, TYPE.CONCEPT_LF};
		int[] inputIDs = new int[] {1, 1, 129, 129, 994, 99999999};
		String[] inputNamespaces = new String[] {"1000001", "0989121", "0989121", "0000001", "0000001", "0989121"};
		
		for (int i = 0; i < results.length; i++)
		{
			Assert.assertEquals(Long.toString(results[i]), SctId.generate(inputIDs[i], inputNamespaces[i], inputTypes[i])); 
		}
	}

	@Test
	public void sctIDTestTwo() throws Exception
	{
		//invalid SCTIDs 
		long[] tests = new long[] { 10005,100012, 100072, 1223401004l};

		for (long l : tests)
		{
			Assert.assertFalse(SctId.isValidSctId(l));
		}
	}
}
