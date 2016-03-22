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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.query.provider.lucene;

/**
 * {@link LuceneDescriptionType}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public enum LuceneDescriptionType
{
	FSN, SYNONYM, DEFINITION;
	
	public static LuceneDescriptionType fromOrdinal(int ordinal)
	{
		for (LuceneDescriptionType ldt : LuceneDescriptionType.values())
		{
			if (ordinal == ldt.ordinal())
			{
				return ldt;
			}
		}
		throw new RuntimeException("No Match!");
	}

	public static LuceneDescriptionType parse(String descriptionType)
	{
		for (LuceneDescriptionType ldt : LuceneDescriptionType.values())
		{
			if (ldt.name().equalsIgnoreCase(descriptionType))
			{
				return ldt;
			}
		}
		return null;
	}
}
