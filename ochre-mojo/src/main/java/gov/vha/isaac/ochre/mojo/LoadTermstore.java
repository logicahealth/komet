package gov.vha.isaac.ochre.mojo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.cedarsoftware.util.io.JsonWriter;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderQueueService;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.externalizable.StampAlias;
import gov.vha.isaac.ochre.api.externalizable.StampComment;
import gov.vha.isaac.ochre.api.identity.StampedVersion;

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
	
	public void setibdfFiles(File[] files)
	{
		ibdfFiles = files;
	}
	
	/**
	 * {@code ibdf format} files to import.
	 */
	@Parameter(required = false) 
	private boolean activeOnly = false;
	
	public void setActiveOnly(boolean activeOnly)
	{
		this.activeOnly = activeOnly;
	}
	
	private HashSet<SememeType> sememeTypesToSkip = new HashSet<>();
	public void skipSememeTypes(Collection<SememeType> types )
	{
		sememeTypesToSkip.addAll(types);
	}
	
	private int conceptCount, sememeCount, stampAliasCount, stampCommentCount, itemCount, itemFailure;
	private HashSet<Integer> skippedItems = new HashSet<>();
	private boolean skippedAny = false;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			for (File f : ibdfFiles)
			{
				getLog().info("Loading termstore from " + f.getCanonicalPath() + (activeOnly ? " active items only" : ""));
				BinaryDataReaderQueueService reader = Get.binaryDataQueueReader(f.toPath());
				
				BlockingQueue<OchreExternalizable> queue = reader.getQueue();
				
				while (!queue.isEmpty() || !reader.isFinished())
				{
					OchreExternalizable object = queue.poll(500, TimeUnit.MILLISECONDS);
					if (object != null)
					{
						itemCount++;
						try
						{
							if (object.getOchreObjectType() == OchreExternalizableObjectType.CONCEPT)
							{
								if (!activeOnly || isActive((ObjectChronology)object))
								{
									Get.conceptService().writeConcept(((ConceptChronology)object));
									conceptCount++;
								}
								else
								{
									skippedItems.add(((ObjectChronology)object).getNid());
								}
							}
							else if (object.getOchreObjectType() == OchreExternalizableObjectType.SEMEME)
							{
								SememeChronology sc = (SememeChronology)object;
								if (!sememeTypesToSkip.contains(sc.getSememeType()) && 
									(!activeOnly || (isActive(sc) && !skippedItems.contains(sc.getReferencedComponentNid()))))
								{
									Get.sememeService().writeSememe(sc);
									if (((SememeChronology)object).getSememeType() == SememeType.LOGIC_GRAPH)
									{
										Get.taxonomyService().updateTaxonomy((SememeChronology)object);
									}
									sememeCount++;
								}
								else
								{
									skippedItems.add(sc.getNid());
								}
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
							getLog().info("Read " + itemCount + " entries, " + "Loaded " + conceptCount + " concepts, " + sememeCount + " sememes, " + stampAliasCount + " stampAlias, " 
									+ stampCommentCount + " stampComment");
						}
					}
				};
				
				if (skippedItems.size() > 0)
				{
					skippedAny = true;
				}
				
				getLog().info("Loaded " + conceptCount + " concepts, " + sememeCount + " sememes, " + stampAliasCount + " stampAlias, " 
						+ stampCommentCount + " stampComment"  + (skippedItems.size() > 0 ? ", skipped for inactive " + skippedItems.size() : "") 
						+ (itemFailure > 0 ? " Failures " + itemFailure : "") + " from file " + f.getName());
				conceptCount = 0;
				sememeCount = 0;
				stampAliasCount = 0;
				stampCommentCount = 0;
				skippedItems.clear();
			}
			
			if (skippedAny)
			{
				//Loading with activeOnly set to true causes a number of gaps in the concept / sememe providers
				Get.identifierService().clearUnusedIds();
			}
		}
		catch (Exception ex)
		{
			getLog().info("Loaded " + conceptCount + " concepts, " + sememeCount + " sememes, " + stampAliasCount + " stampAlias, " 
					+ stampCommentCount + " stampComments" + (skippedItems.size() > 0 ? ", skipped for inactive " + skippedItems.size() : ""));
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}
	
	private boolean isActive(ObjectChronology<?> object)
	{
		if (object.getVersionList().size() != 1)
		{
			throw new RuntimeException("Didn't expect version list of size " + object.getVersionList());
		}
		else
		{
			return ((StampedVersion)object.getVersionList().get(0)).getState() == State.ACTIVE;
		}
	}
}
