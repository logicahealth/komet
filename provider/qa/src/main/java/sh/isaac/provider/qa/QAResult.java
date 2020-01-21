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
package sh.isaac.provider.qa;

import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import sh.isaac.api.qa.QAInfo;
import sh.isaac.api.qa.QAResults;

/**
 * {@link QAResult}
 *
 * A simple class for doing JSON storage of QA results into the metadata store.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class QAResult implements Comparable<QAResult>
{
	/**
	 * The time when this QA was started (in standard java form)
	 */
	@XmlElement
	private long launchTime;
	
	/**
	 * The time when this QA was completed (in standard java form).  Null / not provided if it is still running.
	 */	
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	protected Long completeTime = null;
	
	/**
	 * The ID of this classification run
	 */
	@XmlElement
	private UUID qaId;
	
	/**
	 * A simple summary of the current status, such as Running, Failed, or Completed.
	 * If failed, this will contain some information on the cause of the failure.
	 */
	@XmlElement
	protected String status;
	
	/**
	 * The detailed QA results
	 */
	@XmlElement
	private QAInfo[] result;
	
	protected QAResult()
	{
		//For jaxb
	}
	
	public QAResult(UUID qaId)
	{
		this.qaId = qaId;
		this.launchTime = System.currentTimeMillis();
		this.status = "Queued";
	}
	
	public void completed(QAResults result)
	{
		this.completeTime = System.currentTimeMillis();
		this.status = "Done";
		this.result =  new QAInfo[result.getResult().size()];
		for (int i = 0; i < result.getResult().size(); i++)
		{
			this.result[i] = result.getResult().get(i);
		}
	}
	
	public void failed(String reason)
	{
		this.completeTime = System.currentTimeMillis();
		this.status = "Failed: " + reason ;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(QAResult o)
	{
		return Long.compare(this.launchTime, o.launchTime);
	}

	/**
	 * @return When this QA task was created and queued
	 */
	public long getLaunchTime()
	{
		return launchTime;
	}

	/**
	 * @return When this QA task completed
	 */
	public Long getCompleteTime()
	{
		return completeTime;
	}

	/**
	 * @return Unique ID for this QA run
	 */
	public UUID getQaId()
	{
		return qaId;
	}

	/**
	 * @return  A simple summary of the current status, such as Running, Failed, or Completed.
	 * If failed, this will contain some information on the cause of the failure.
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @return Detailed, individual QA rule outputs
	 */
	public QAInfo[] getResult()
	{
		return result;
	}
}
