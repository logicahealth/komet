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
package gov.va.oia.terminology.converters.sharedUtils.umlsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

public class AbbreviationExpansion
{
	String abbreviation_, expansion_, description_;

	protected AbbreviationExpansion(String abbreviation, String expansion, String description)
	{
		this.abbreviation_ = abbreviation;
		this.expansion_ = expansion;
		this.description_ = description;
	}

	public String getAbbreviation()
	{
		return abbreviation_;
	}

	public String getExpansion()
	{
		return expansion_;
	}

	public String getDescription()
	{
		return description_;
	}
	
	public static HashMap<String, AbbreviationExpansion> load(InputStream is) throws IOException
	{
		HashMap<String, AbbreviationExpansion> results = new HashMap<>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1));
		String line = br.readLine();
		while (line != null)
		{
			if (StringUtils.isBlank(line) || line.startsWith("#"))
			{
				line = br.readLine();
				continue;
			}
			String[] cols = line.split("\t");
			
			AbbreviationExpansion ae = new AbbreviationExpansion(cols[0], cols[1], (cols.length > 2 ? cols[2] : null));
			
			results.put(ae.getAbbreviation(), ae);
			line = br.readLine();
		}
		return results;
	}
}
