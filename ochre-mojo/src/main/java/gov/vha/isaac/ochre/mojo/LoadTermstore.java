package gov.vha.isaac.ochre.mojo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.cedarsoftware.util.io.JsonWriter;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.externalizable.StampAlias;
import gov.vha.isaac.ochre.api.externalizable.StampComment;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

/**
 * Goal which loads a database from eConcept files.
 */
@Mojo(name = "load-termstore", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)

public class LoadTermstore extends AbstractMojo
{

	/**
	 * {@code ibdf format} files to import.
	 */
	@Parameter(required = true) 
	private File[] ibdfFiles;
	
	private int conceptCount, sememeCount, stampAliasCount, stampCommentCount, itemCount, itemFailure;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			for (File f : ibdfFiles)
			{
				getLog().info("Loading termstore from " + f.getCanonicalPath());
				BinaryDataReaderService reader = Get.binaryDataReader(f.toPath());
				reader.getStream().forEach((object) -> 
				{
					itemCount++;
					try
					{
						if (object.getOchreObjectType() == OchreExternalizableObjectType.CONCEPT)
						{
							Get.conceptService().writeConcept((ConceptChronology)object);
							conceptCount++;
						}
						else if (object.getOchreObjectType() == OchreExternalizableObjectType.SEMEME)
						{
							Get.sememeService().writeSememe((SememeChronology)object);
							if (((SememeChronology)object).getSememeType() == SememeType.LOGIC_GRAPH)
							{
								Get.taxonomyService().updateTaxonomy((SememeChronology)object);
							}
							sememeCount++;
						}
						else if (object.getOchreObjectType() == OchreExternalizableObjectType.STAMP_ALIAS)
						{
							Get.commitService().addAlias(((StampAlias)object).getStampSequence(), ((StampAlias)object).getStampAlias(), null);
							stampAliasCount++;
						}
						else if (object.getOchreObjectType() == OchreExternalizableObjectType.STAMP_COMMENT)
						{
							Get.commitService().setComment(((StampComment)object).getStampSequence(), ((StampComment)object).getComment());
							stampCommentCount++;
						}
						else
						{
							throw new UnsupportedOperationException("Unknown ochre object type: " + object);
						}
					}
					catch (Exception e)
					{
						itemFailure++;
						getLog().error("Failure at " + conceptCount + " concepts, " + sememeCount + " sememes, " + stampAliasCount + " stampAlias, " 
								+ stampCommentCount + " stampComments", e);
						
						Map<String, Object> args = new HashMap<>();
						args.put(JsonWriter.PRETTY_PRINT, true);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						JsonWriter json = new JsonWriter(baos, args);
						
						UUID primordial = null;
						if (object instanceof ObjectChronology)
						{
							primordial = ((ObjectChronology)object).getPrimordialUuid();
						}
						
						json.write(object);
						getLog().error("Failed on " + (primordial == null ? ": " : "object with primoridial UUID " + primordial.toString() + ": ") +  baos.toString());
						json.close();
						
					}
					
					if (itemCount % 50000 == 0)
					{
						getLog().info("Loaded " + conceptCount + " concepts, " + sememeCount + " sememes, " + stampAliasCount + " stampAlias, " 
								+ stampCommentCount + " stampComment");
					}
				});
				getLog().info("Loaded " + conceptCount + " concepts, " + sememeCount + " sememes, " + stampAliasCount + " stampAlias, " 
						+ stampCommentCount + " stampComment from file " + f.getName());
				conceptCount = 0;
				sememeCount = 0;
				stampAliasCount = 0;
				stampCommentCount = 0;
			}
		}
		catch (Exception ex)
		{
			getLog().info("Loaded " + conceptCount + " concepts, " + sememeCount + " sememes, " + stampAliasCount + " stampAlias, " 
					+ stampCommentCount + " stampComments");
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}
}
