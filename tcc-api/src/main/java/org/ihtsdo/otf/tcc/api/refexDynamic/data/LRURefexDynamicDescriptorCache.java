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

import java.util.LinkedHashMap;
import java.util.Map.Entry;

class LRURefexDynamicDescriptorCache<K, V> extends LinkedHashMap<K, V>
{
	private static final long serialVersionUID = 1L;
	private final int maxSize_;
	
	public LRURefexDynamicDescriptorCache(int maxSize)
	{
		super(16, .75f, true);
		maxSize_ = maxSize;
	}

	/**
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest)
	{
		return super.size() > maxSize_;
	}
}