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
package org.ihtsdo.otf.tcc.model.index.service;

import org.jvnet.hk2.annotations.Contract;

/**
 * {@link IndexStatusListenerBI}
 * 
 * Any @Service annotated class which implements this interface will get the notifications below, when 
 * index events happen.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface IndexStatusListenerBI
{
	/**
	 * Called when an index configuration change happens that listeners might be interested in.
	 * @param indexConfigurationThatChanged - the indexer that had a change
	 */
	public void indexConfigurationChanged(IndexerBI indexConfigurationThatChanged);
	
	/**
	 * Called when a reindex sequence begins
	 * @param index - the index being recreated
	 */
	public void reindexBegan(IndexerBI index);
	
	/**
	 * Called when a reindex sequence completes
	 * @param index - the index that was recreated
	 */
	public void reindexCompleted(IndexerBI index);
}
