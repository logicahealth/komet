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
package gov.vha.isaac.ochre.query.provider.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * {@link PerFieldAnalyzer}
 * An analyzer that indexes everything with the {@link StandardAnalyzer} unless the field ends
 * with the string "_wsa" - in which case, it is indexed with the {@link WhitespaceAnalyzer} and 
 * a {@link LowerCaseFilter}
 * 
 * This can be enhanced in the future to properly handle other languages as well.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class PerFieldAnalyzer extends AnalyzerWrapper
{
	private WhitespaceAnalyzer wa = new WhitespaceAnalyzer();
	private StandardAnalyzer sa = new StandardAnalyzer();
	
	public static final String WHITE_SPACE_FIELD_MARKER = "_wsa";
	
	public PerFieldAnalyzer()
	{
		super(Analyzer.PER_FIELD_REUSE_STRATEGY);
	}
	
	/**
	 * @see org.apache.lucene.analysis.AnalyzerWrapper#getWrappedAnalyzer(java.lang.String)
	 */
	@Override
	protected Analyzer getWrappedAnalyzer(String fieldName)
	{
		if (fieldName.endsWith(WHITE_SPACE_FIELD_MARKER))
		{
			return wa;
		}
		else
		{
			return sa;
		}
	}

	/**
	 * @see org.apache.lucene.analysis.AnalyzerWrapper#wrapComponents(java.lang.String, org.apache.lucene.analysis.Analyzer.TokenStreamComponents)
	 */
	@Override
	protected TokenStreamComponents wrapComponents(String fieldName, TokenStreamComponents components)
	{
		if (fieldName.endsWith(WHITE_SPACE_FIELD_MARKER))
		{
			return new TokenStreamComponents(components.getTokenizer(), new LowerCaseFilter(components.getTokenStream()));
		}
		else
		{
			return components;
		}
	}

	@Override
	public String toString()
	{
		return "PerFieldAnalyzer(default=" + sa + ", fields ending with '" + WHITE_SPACE_FIELD_MARKER + "': " + wa +")";
	}
}
