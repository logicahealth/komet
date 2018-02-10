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

package sh.isaac.convert.mojo.sopt.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link EnumValidatedTableData}
 *
 * Columnar data loader which should work with multiple COLUMNS enum versions
 * 
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 * @param <COLUMNS>
 */
public class EnumValidatedTableData<COLUMNS extends Enum<COLUMNS>>
{
	private List<Map<COLUMNS, String>> rows = new ArrayList<>();

	public List<Map<COLUMNS, String>> rows()
	{
		return rows;
	}

	public List<String> getValues(COLUMNS column)
	{
		List<String> values = new ArrayList<>();
		for (Map<COLUMNS, String> map : rows)
		{
			values.add(map.get(column));
		}

		return Collections.unmodifiableList(values);
	}

	public Set<String> getDistinctValues(COLUMNS column)
	{
		Set<String> values = new HashSet<>();
		for (Map<COLUMNS, String> map : rows)
		{
			values.add(map.get(column));
		}

		return Collections.unmodifiableSet(values);
	}
}
