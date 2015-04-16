/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
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
package org.ihtsdo.otf.tcc.model.cs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.io.FileIO;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;

/**
 * 
 * {@link ChangeSetLogWriter}
 * Ported over from trek CommitLog
 * 
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ChangeSetLogWriter implements ChangeSetGeneratorBI
{
	private File changeSetFile;
	private File tempFile;

	private transient OutputStreamWriter tempOut;

	public ChangeSetLogWriter(File changeSetFile, File tempFile)
	{
		super();
		this.changeSetFile = new File(changeSetFile.getParent(), TimeHelper.localLongFileFormat.format(new Date()) + "." + changeSetFile.getName());
		this.tempFile = tempFile;
	}

	@Override
	public void commit() throws IOException
	{
		if (tempOut != null)
		{
			tempOut.flush();
			tempOut.close();
			tempOut = null;
			String canonicalFileString = tempFile.getCanonicalPath();
			if (tempFile.exists())
			{
				try
				{
					if (changeSetFile.exists())
					{
						changeSetFile.delete();
					}
					if (tempFile.renameTo(changeSetFile) == false)
					{
						FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile.getCanonicalPath());
					}
				}
				catch (Exception e)
				{
					ChangeSetLogger.logger.warning("FileIO.copyFile failed in CommitLog.");
				}

				tempFile = new File(canonicalFileString);
				tempFile.delete();
			}
		}
	}

	@Override
	public void open(NidSetBI commitSapNids) throws IOException
	{
		if (changeSetFile.exists() == false)
		{
			changeSetFile.getParentFile().mkdirs();
			changeSetFile.createNewFile();
		}
		FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile.getCanonicalPath());
		tempOut = new OutputStreamWriter(new FileOutputStream(tempFile, true));
	}

	@Override
	public void writeChanges(ConceptChronicleBI change, long time) throws IOException
	{
		tempOut.append(TimeHelper.localDateFormat.format(new Date(time)));
		tempOut.append("\tconcept\t");
		tempOut.append(change.toString());
		tempOut.append("\t");
		tempOut.append(change.getUUIDs().toString());
		tempOut.append("\n");
	}

	@Override
	public void setPolicy(ChangeSetGenerationPolicy policy)
	{
		// nothing to do, does not honor policy
	}
}
