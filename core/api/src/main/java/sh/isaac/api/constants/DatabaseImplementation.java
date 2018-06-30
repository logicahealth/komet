/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api.constants;

/**
 * The database implementations that may be selected via configuration.
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public enum DatabaseImplementation {
	FILESYSTEM, XODUS, BDB, MV, DEFAULT;

	/**
	 * @param string parse the value from a string that equals the enum name value.
	 * @return the implementation, or, throw a runtime exception if unknown.
	 */
	public static DatabaseImplementation parse(String string)
	{
		for (DatabaseImplementation di : DatabaseImplementation.values())
		{
			if (di.name().equals(string))
			{
				return di;
			}
		}
		throw new RuntimeException("Invalid value - " + string);
	}
}
