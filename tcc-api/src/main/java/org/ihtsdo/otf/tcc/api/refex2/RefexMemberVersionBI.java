/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.refex2;

import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI;

/**
 *
 * @author kec
 */
public interface RefexMemberVersionBI<A extends RefexMemberAnalogBI<A>> extends RefexVersionBI<A>
{
	/**
	 * @return The nid of the component that defines the columns of data used in this Refex, and provides a description
	 * for the purpose of this Refex linkage.
	 * See {@link #getRefexUsageDescription()} for a much more useful (but more expensive) convenience method.
	 */
	int getRefexUsageDescriptorNid();

	/**
	 * A convenience method that reads the concept referenced in {@link #getColumnDescriptorNid()} and
	 * returns the actual column information that is contained within that concept.
	 * 
	 * @return
	 */
	RefexUsageDescriptionBI getRefexUsageDescription();

	/**
	 * @return All of the data columns that are part of this Refex. See {@link #getData(int)}.
	 * May be empty, will not be null.
	 */
	RefexDataBI[] getData();

	/**
	 * The type and data (if any) in the specified column of the Refex.
	 * 
	 * @param columnNumber
	 * @return The RefexMemberBI which contains the type and data (if any) for the specified column
	 * @throws IndexOutOfBoundsException
	 */
	RefexDataBI getData(int columnNumber) throws IndexOutOfBoundsException;
}
