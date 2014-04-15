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
package org.ihtsdo.otf.tcc.api.refexDynamic.data;

import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;


/**
 * {@link RefexDynamicUsageDescriptionBI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface RefexDynamicUsageDescriptionBI
{
	/**
	 * @return The nid of the concept that the rest of the attributes of this type were read from.
	 */
	public int getRefexUsageDescriptorNid();
	
	/**
	 * @return A user-friendly description of the overall purpose of this Refex.
	 */
	public String getRefexUsageDescription();
	
	
	/**
	 * The ordered column information which will correspond with the data returned by {@link RefexDynamicChronicleBI#getData()}
	 * These arrays will be the same size, and in the same order.
	 * @return the column information
	 */
	public RefexDynamicColumnInfoBI[] getColumnInfo();
}
