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
package org.ihtsdo.otf.tcc.model.cc.refex2;

import org.ihtsdo.otf.tcc.api.refex2.RefexColumnInfoBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI;

/**
 * 
 * {@link RefexUsageDescription}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexUsageDescription implements RefexUsageDescriptionBI
{
	int refexUsageDescriptorNid_;
	String refexUsageDescription_;
	RefexColumnInfo[] refexColumnInfo_;

	/**
	 * Read the RefexUsageDescription data from the database for a given nid.
	 * 
	 * @param refexUsageDescriptorNid
	 */
	public RefexUsageDescription(int refexUsageDescriptorNid)
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI#getRefexUsageDescriptorNid()
	 */
	@Override
	public int getRefexUsageDescriptorNid()
	{
		return refexUsageDescriptorNid_;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI#getRefexUsageDescription()
	 */
	@Override
	public String getRefexUsageDescription()
	{
		return refexUsageDescription_;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI#getColumnInfo()
	 */
	@Override
	public RefexColumnInfoBI[] getColumnInfo()
	{
		return refexColumnInfo_;
	}

	/**
	 * A convenience method for creating the meta-data concept within the DB that will describe the usage of a Refex
	 * 
	 * Concept will be created as a child of //TODO and will have the metadata place automatically.
	 * 
	 * @param conceptFSN The FSN to use on the concept
	 * @param conceptPreferredName The Preferred Name to use on the concept
	 * @param refexUsageDescription The purpose of this Refex linkage
	 * @param refexColumnInfo The details for each column of data that is attached to the Refex
	 * @return
	 */
	public static RefexUsageDescription createRefexUsageDescriptionConcept(String conceptFSN, String conceptPreferredName, String refexUsageDescription,
			RefexColumnInfoBI[] refexColumnInfo)
	{
		return null;
		// TODO implement
	}
}
