package sh.isaac.provider.query.lucene.indexers;

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

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link DescriptionIndexerTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DescriptionIndexerTest
{

	@Test
	public void adjustBrackets() throws Exception
	{
		Assert.assertEquals("A long \\[test\\] string", DescriptionIndexer.handleBrackets("A long [test] string", '[', ']'));
		Assert.assertEquals("A long \\]test\\[ string", DescriptionIndexer.handleBrackets("A long ]test[ string", '[', ']'));
		Assert.assertEquals("Another [5 TO 6] string \\[ with random \\[stuff\\]", DescriptionIndexer.handleBrackets("Another [5 TO 6] string [ with random [stuff]", '[', ']'));
		Assert.assertEquals("\\[\\[\\[\\[", DescriptionIndexer.handleBrackets("[[[[", '[', ']'));
		Assert.assertEquals("\\]\\[\\]\\[", DescriptionIndexer.handleBrackets("][][", '[', ']'));
		Assert.assertEquals("\\[query\\] [a to b] for a bunch of \\[stuff\\]", DescriptionIndexer.handleBrackets("[query] [a to b] for a bunch of [stuff]", '[', ']'));
		Assert.assertEquals("A long \\{test\\} string", DescriptionIndexer.handleBrackets("A long {test} string", '{', '}'));
	}
	
	@Test
	public void adjustOthers() throws Exception
	{
		Assert.assertEquals("http\\://foo.com", DescriptionIndexer.handleUnsupportedEscapeChars("http://foo.com"));
		Assert.assertEquals("http\\://foo.com", DescriptionIndexer.handleUnsupportedEscapeChars("http\\://foo.com"));
		Assert.assertEquals("fred\\^jane", DescriptionIndexer.handleUnsupportedEscapeChars("fred^jane"));
	}
}