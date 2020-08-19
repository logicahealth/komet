/*
 * Copyright 2020 VetsEZ Inc, Sagebits LLC
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.qa.QAResults;

/**
 * {@link QARunStorage}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@RunLevel(value = LookupService.SL_L6_ISAAC_DEPENDENTS_RUNLEVEL)
public class QARunStorage
{
	private static Logger log = LogManager.getLogger();
	private static final String QA_STORE = "qaStore";

	private QARunStorage()
	{
		//For HK2
	}

	@PostConstruct
	private void startMe()
	{
		cleanAbandoned();
	}

	/**
	 * For use by the read API to read back the classifier results.
	 * 
	 * @param qaId - the qa result to read.
	 * @return The QAResult, if available, null if the key is unknown.
	 */
	public QAResult getQAResults(UUID qaId)
	{
		String temp = Get.metaContentService().<UUID, String> openStore(QA_STORE).get(qaId);
		if (temp == null)
		{
			return null;
		}
		else
		{
			return (QAResult) JsonReader.jsonToJava(temp);
		}
	}

	/**
	 * @return Get all stored qa info, sorted by launch time, most recent to oldest.
	 */
	public List<QAResult> getQAResults()
	{
		ArrayList<QAResult> results = new ArrayList<>();
		for (String s : Get.metaContentService().<UUID, String> openStore(QA_STORE).values())
		{
			QAResult rcr = (QAResult) JsonReader.jsonToJava(s);
			results.add(rcr);
		}
		Collections.sort(results);
		return results;
	}

	public void storeResult(UUID qaId, QAResults qar)
	{
		QAResult rcr = getQAResults(qaId);
		//clearStoredData called during qa
		if (rcr == null)
		{
			rcr = new QAResult(qaId);
		}
		rcr.completed(qar);
		String temp = JsonWriter.objectToJson(rcr);
		log.debug("QA Results:" + qar.toString());
		Get.metaContentService().<UUID, String> openStore(QA_STORE).put(qaId, temp);
	}

	public void storeFailure(UUID id, String reason)
	{
		QAResult rcr = getQAResults(id);
		//clearStoredData called during classify
		if (rcr == null)
		{
			rcr = new QAResult(id);
		}
		rcr.failed(reason);
		String temp = JsonWriter.objectToJson(rcr);
		log.info("QA Failure Results:" + temp);
		Get.metaContentService().<UUID, String> openStore(QA_STORE).put(id, temp);
	}

	/**
	 * Store the initial timestamp, with a status of queued.
	 * 
	 * @param id
	 */
	public void qaQueued(UUID id)
	{
		String temp = JsonWriter.objectToJson(new QAResult(id));
		Get.metaContentService().<UUID, String> openStore(QA_STORE).put(id, temp);
	}

	/**
	 * Change status to running, if currently queued.
	 * 
	 * @param qaId
	 */
	public void qaStarted(UUID qaId)
	{
		QAResult rcr = getQAResults(qaId);
		//clearStoredData called during classify
		if (rcr == null)
		{
			rcr = new QAResult(qaId);
		}
		if (rcr.getStatus().equals("Queued"))
		{
			rcr.status = "Running";
		}
		String temp = JsonWriter.objectToJson(rcr);
		log.debug("QA Running: " + qaId);
		Get.metaContentService().<UUID, String> openStore(QA_STORE).put(qaId, temp);
	}

	public void clearStoredData()
	{
		Get.metaContentService().<UUID, String> openStore(QA_STORE).clear();
		log.info("QA run data cleared");
	}

	/**
	 * Iterate the stored qa executions, and mark any that are still active as failed.
	 * This would typically only be called on system startup, to clean up after any classifier executions that were abandoned due to a server
	 * shutdown.
	 */
	private void cleanAbandoned()
	{
		for (UUID uuid : Get.metaContentService().<UUID, String> openStore(QA_STORE).keySet())
		{
			QAResult cr = getQAResults(uuid);
			if (cr != null && cr.getCompleteTime() == null)
			{
				cr.completeTime = System.currentTimeMillis();
				cr.status = "System shutdown prior to completion";
				Get.metaContentService().<UUID, String> openStore(QA_STORE).put(uuid, JsonWriter.objectToJson(cr));
			}
		}
	}
}
