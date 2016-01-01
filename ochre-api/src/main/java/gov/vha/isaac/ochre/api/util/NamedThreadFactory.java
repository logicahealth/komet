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
package gov.vha.isaac.ochre.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class NamedThreadFactory implements ThreadFactory
{
	private ThreadGroup threadGroup = null;
	private String threadNamePrefix = null;
	private int threadPriority;
	private boolean daemon;
	
	
	public NamedThreadFactory(boolean daemon)
	{
		this(null, null, Thread.NORM_PRIORITY, daemon);
	}
	
	/**
	 * @param threadNamePrefix optional
	 * @param daemon
	 */
	public NamedThreadFactory(String threadNamePrefix, boolean daemon)
	{
		this(null, threadNamePrefix, Thread.NORM_PRIORITY, daemon);
	}

	/**
	 * @param threadGroup optional
	 * @param threadNamePrefix optional
	 */
	public NamedThreadFactory(ThreadGroup threadGroup, String threadNamePrefix)
	{
		this(threadGroup, threadNamePrefix, Thread.NORM_PRIORITY, true);
	}

	/**
	 * @param threadGroup optional
	 * @param threadNamePrefix optional
	 * @param threadPriority
	 * @param daemon
	 */
	public NamedThreadFactory(ThreadGroup threadGroup, String threadNamePrefix, int threadPriority, boolean daemon)
	{
		super();
		this.threadGroup = threadGroup;
		this.threadNamePrefix = threadNamePrefix;
		this.threadPriority = threadPriority;
		this.daemon = daemon;
		if (threadGroup != null && threadGroup.getMaxPriority() < threadPriority)
		{
			threadGroup.setMaxPriority(threadPriority);
		}
	}

	@Override
	public Thread newThread(Runnable r)
	{
		Thread t =  threadGroup == null ? new Thread(r) : new Thread(threadGroup, r);
		t.setName((threadNamePrefix == null ? "" : threadNamePrefix + " ") + t.getId());
		t.setPriority(threadPriority);
		t.setDaemon(daemon);
		return t;
	}
}