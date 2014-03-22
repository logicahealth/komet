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
package org.ihtsdo.otf.tcc.api.refex2.data;


/**
 * {@link RefexDataBI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public interface RefexDataBI
{
	/**
	 * @return The data object itself.  For a getData() method that doesn't require casting
	 * of the output, see the getDataXXX() method available within extensions of the {@link RefexDataBI} interface.
	 */
	public Object getData();
	
	/**
	 * @return The type information of the data
	 */
	public RefexDataType getRefexDataType();
}
