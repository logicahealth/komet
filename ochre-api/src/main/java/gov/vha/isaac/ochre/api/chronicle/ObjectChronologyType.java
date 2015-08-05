/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api.chronicle;

/**
 *
 * @author kec
 */
public enum ObjectChronologyType {
	//TODO dan find out why other exists
    CONCEPT("Concept"), SEMEME("Sememe"), REFEX("Refex"), OTHER("Other"), UNKNOWN_NID("Unknown");
	
	private String niceName_;
	
	private ObjectChronologyType(String niceName)
	{
		niceName_ = niceName;
	}

	/**
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString()
	{
		return niceName_;
	}
	
	public static ObjectChronologyType parse(String name)
	{
		for (ObjectChronologyType ct : values())
		{
			if (ct.name().equals(name) || ct.niceName_.equals(name))
			{
				return ct;
			}
		}
		return UNKNOWN_NID;
	}
}
