/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.mojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.logging.Log;

/**
 * Redirect maven logging to Log4j
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class Log4jAdapter implements Log
{
	private Logger LOG;
	
	/**
	 * @param loggerName the logger name to route to
	 */
	public Log4jAdapter(Class<? extends LoadTermstore> loggerName)
	{
		LOG = LogManager.getLogger(loggerName);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDebugEnabled()
	{
		return LOG.isDebugEnabled();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void debug(CharSequence content)
	{
		LOG.debug(content);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void debug(CharSequence content, Throwable error)
	{
		LOG.debug(content, error);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void debug(Throwable error)
	{
		LOG.debug(error);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInfoEnabled()
	{
		return LOG.isInfoEnabled();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void info(CharSequence content)
	{
		LOG.info(content);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void info(CharSequence content, Throwable error)
	{
		LOG.info(content, error);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void info(Throwable error)
	{
		LOG.info(error);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWarnEnabled()
	{
		return LOG.isWarnEnabled();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void warn(CharSequence content)
	{
		LOG.warn(content);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void warn(CharSequence content, Throwable error)
	{
		LOG.warn(content, error);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void warn(Throwable error)
	{
		LOG.warn(error);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isErrorEnabled()
	{
		return LOG.isErrorEnabled();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void error(CharSequence content)
	{
		LOG.error(content);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void error(CharSequence content, Throwable error)
	{
		LOG.error(content,  error);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void error(Throwable error)
	{
		LOG.error( error);
	}
}
