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
package sh.isaac.convert.mojo.turtle;

import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.IsaacObjectType;

/**
 * Some helper code for constructing DynamicSemantics from anonymous entries in RDF data sources.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DynamicSemanticHelper
{
	private String niceName = null;
	private IsaacObjectType referencedComponentTypeRestriction = null; 
	private VersionType referencedComponentTypeSubRestriction = null;
	
	public DynamicSemanticHelper(String uri)
	{
		if (uri.contains("/"))
		{
			String tail = uri.substring(uri.lastIndexOf('/') + 1, uri.length());
			if (tail.contains("#"))
			{
				tail = tail.substring(tail.lastIndexOf('#') + 1, tail.length());
			}
			
			StringBuilder sb = new StringBuilder();
			//magic: https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
			for (String s : tail.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"))
			{
				sb.append(s.toLowerCase());
				sb.append(" ");
			}
			sb.setLength(sb.length() - 1);
			niceName = sb.toString();
		}
	}
	
	public DynamicSemanticHelper(String niceName, IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction)
	{
		this.niceName = niceName;
		this.referencedComponentTypeRestriction = referencedComponentTypeRestriction;
		this.referencedComponentTypeSubRestriction = referencedComponentTypeSubRestriction;
	}

	public String getNiceName()
	{
		return niceName;
	}
	
	public IsaacObjectType getReferencedComponentTypeRestriction()
	{
		return referencedComponentTypeRestriction;
	}

	public VersionType getReferencedComponentTypeSubRestriction()
	{
		return referencedComponentTypeSubRestriction;
	}
}
