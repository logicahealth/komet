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
package org.ihtsdo.otf.tcc.model.cc.refex4.data;

import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexColumnInfoBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexUsageDescriptionBI;

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
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexUsageDescriptionBI#getRefexUsageDescriptorNid()
	 */
	@Override
	public int getRefexUsageDescriptorNid()
	{
		return refexUsageDescriptorNid_;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexUsageDescriptionBI#getRefexUsageDescription()
	 */
	@Override
	public String getRefexUsageDescription()
	{
		return refexUsageDescription_;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexUsageDescriptionBI#getColumnInfo()
	 */
	@Override
	public RefexColumnInfoBI[] getColumnInfo()
	{
		return refexColumnInfo_;
	}
}
