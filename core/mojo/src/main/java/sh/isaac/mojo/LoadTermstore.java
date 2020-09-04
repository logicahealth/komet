/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */

package sh.isaac.mojo;


import com.cedarsoftware.util.io.JsonWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.model.datastream.BinaryDatastreamReader;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.external.ConceptNodeWithUuids;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import static sh.isaac.api.logic.LogicalExpressionBuilder.*;

/**
 * Goal which loads a database from ibdf files. try {@link LoadTermstoreSemaphore} for a newer implementation, however
 * TODO We need to figure out if/how to integrate the merge logic into the LoadTermstoreSemaphore
 * in the meantime, the bug with missing random entries near the end of the file has been fixed.
 */
@Mojo(name = "load-termstore", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class LoadTermstore extends AbstractMojo
{

	/**
	 * Constructor for maven
	 */
	public LoadTermstore()
	{
		//for maven
	}

	/**
	 * Constructor for runtime usage
	 * 
	 * @param ibdfFiles the files to import
	 * @param routeLogToLog4j if true, route logging messages into Log4j. If false, uses the default maven SystemStreamLog.
	 * @param mergeLogicGraphs true, if you want the loader to attempt to merge the logic graphs in the incoming with the currently existing graphs.
	 *            False, to simply load the IBDF as is.
	 */
	public LoadTermstore(Path[] ibdfFiles, boolean routeLogToLog4j, boolean mergeLogicGraphs)
	{
		setibdfFiles(ibdfFiles);
		if (routeLogToLog4j)
		{
			setLog(new Log4jAdapter(this.getClass()));
		}
		this.mergeLogicGraphs = mergeLogicGraphs;
	}

	/**
	 * Constructor for runtime usage
	 * 
	 * @param ibdfStreams the streams representing IBDF files to import
	 * @param routeLogToLog4j if true, route logging messages into Log4j. If false, uses the default maven SystemStreamLog.
	 * @param mergeLogicGraphs true, if you want the loader to attempt to merge the logic graphs in the incoming with the currently existing graphs.
	 *            False, to simply load the IBDF as is.
	 */
	public LoadTermstore(InputStream[] ibdfStreams, boolean routeLogToLog4j, boolean mergeLogicGraphs)
	{
		inputIBDFStreams = ibdfStreams;
		if (routeLogToLog4j)
		{
			setLog(new Log4jAdapter(this.getClass()));
		}
		this.mergeLogicGraphs = mergeLogicGraphs;
	}

	/**
	 * Constructor for runtime usage
	 * 
	 * @param ibdfContainingFolder the folder containing ibdf files to import
	 * @param routeLogToLog4j if true, route logging messages into Log4j. If false, uses the default maven SystemStreamLog.
	 * @param mergeLogicGraphs true, if you want the loader to attempt to merge the logic graphs in the incoming with the currently existing graphs.
	 *            False, to simply load the IBDF as is.
	 */
	public LoadTermstore(File ibdfContainingFolder, boolean routeLogToLog4j, boolean mergeLogicGraphs)
	{
		setibdfFilesFolder(ibdfContainingFolder);
		if (routeLogToLog4j)
		{
			setLog(new Log4jAdapter(this.getClass()));
		}
		this.mergeLogicGraphs = mergeLogicGraphs;
	}

	//Maven available parameters

	/**
	 * Only load concepts and semantics with a state of active
	 */
	@Parameter(required = false)
	private boolean activeOnly = false;

	/**
	 * Only load concepts and semantics with a state of active
	 * 
	 * @param activeOnly set the flat
	 */
	public void setActiveOnly(boolean activeOnly)
	{
		this.activeOnly = activeOnly;
	}

	@Parameter(required = false)
	private int duplicatesToPrint = 20;

	public void setDuplicatesToPrint(int duplicatesToPrint)
	{
		this.duplicatesToPrint = duplicatesToPrint;
	}

	/**
	 * The preferred mechanism for specifying ibdf files - provide a folder that contains IBDF files, all found IBDF files in this
	 * folder will be processed.
	 */
	@Parameter(required = false)
	private File ibdfFileFolder;

	/**
	 * The preferred mechanism for specifying ibdf files - provide a folder that contains IBDF files, all found IBDF files in this
	 * folder will be processed.
	 * 
	 * @param folder the folder to look in for ibdf files
	 */
	public void setibdfFilesFolder(File folder)
	{
		ibdfFileFolder = folder;
	}

	private Path[] ibdfFilePaths;

	/**
	 * A second way to specify IBDF files for preload - used for non-maven loads
	 * 
	 * @param filePaths the individual ibdf files to process
	 */
	public void setibdfFiles(Path[] filePaths)
	{
		this.ibdfFilePaths = filePaths;
	}

	//internal use / non-maven exposed parameters

	private boolean mergeLogicGraphs = true;

	private final HashSet<VersionType> semanticTypesToSkip = new HashSet<>();

	private boolean skippedAny = false;

	private boolean setDBBuildMode = true;

	private int itemCount;

	private InputStream[] inputIBDFStreams;

	private final Set<Integer> deferredActionNids = new ConcurrentSkipListSet<>();

	/**
	 * Execute.
	 *
	 * @throws MojoExecutionException the mojo execution exception
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public void execute() throws MojoExecutionException
	{
		//Quiet down some noisy xodus loggers
		SLF4jUtils.quietDownXodus();

		if (setDBBuildMode)
		{
			Get.configurationService().setDBBuildMode(BuildMode.DB);
		}

		// Load IsaacMetadataAuxiliary first, otherwise, we have issues....
		final AtomicBoolean hasMetadata = new AtomicBoolean(false);
		Set<Path> mergedFiles;

		mergedFiles = new HashSet<>();

		if (this.ibdfFilePaths != null)
		{
			for (final Path f : this.ibdfFilePaths)
			{
				mergedFiles.add(f.normalize());
			}
		}

		if (this.ibdfFileFolder != null)
		{
			if (!this.ibdfFileFolder.isDirectory())
			{
				throw new MojoExecutionException("If ibdfFileFolder is provided, it must point to a folder");
			}

			for (final File f : this.ibdfFileFolder.listFiles())
			{
				if (!f.isFile())
				{
					getLog().info("The file " + f.getAbsolutePath() + " is not a file - ignoring.");
				}
				else if (!f.getName().toLowerCase(Locale.ENGLISH).endsWith(".ibdf"))
				{
					getLog().info("The file " + f.getAbsolutePath() + " does not match the expected type of ibdf - ignoring.");
				}
				else
				{
					mergedFiles.add(f.toPath().normalize());
				}
			}
		}

		final Path[] temp = mergedFiles.toArray(new Path[mergedFiles.size()]);

		Arrays.sort(temp, (o1, o2) -> {
			if (o1.toString().endsWith("IsaacMetadataAuxiliary.ibdf"))
			{
				hasMetadata.set(true);
				return -1;
			}
			else if (o2.toString().endsWith("IsaacMetadataAuxiliary.ibdf"))
			{
				hasMetadata.set(true);
				return 1;
			}
			else
			{
				try
				{
					return ((Files.size(o1) - Files.size(o2)) > 0 ? 1 : ((Files.size(o1) - Files.size(o2)) < 0 ? -1 : 0));
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		});

		if (temp.length == 1 && temp[0].toString().endsWith("IsaacMetadataAuxiliary.ibdf"))
		{
			hasMetadata.set(true);
		}

		if (!hasMetadata.get())
		{
			if (setDBBuildMode)
			{
				getLog().warn("No Metadata IBDF file found!  This probably isn't good....");
			}
		}

		if (temp.length == 0 && (inputIBDFStreams == null || inputIBDFStreams.length == 0))
		{
			throw new MojoExecutionException("Failed to find any ibdf files to load");
		}

		if (temp.length == 0)
		{
			getLog().info("Identified " + inputIBDFStreams.length + " input streams");
		}
		else
		{
			getLog().info("Identified " + temp.length + " ibdf files");
		}

		try
		{
			for (final Path f : temp)
			{
				getLog().info("Loading termstore from " + f + (this.activeOnly ? " active items only" : ""));
				FileHandler fh = new FileHandler(f.toString());
				final BinaryDatastreamReader reader = new BinaryDatastreamReader(item -> fh.process(item), f);
				Get.executor().submit(reader).get();
				fh.summarize();
			}

			if (inputIBDFStreams != null)
			{
				for (final InputStream is : inputIBDFStreams)
				{
					getLog().info("Loading termstore inputStream " + (this.activeOnly ? " active items only" : ""));
					FileHandler fh = new FileHandler(is.toString());
					final BinaryDatastreamReader reader = new BinaryDatastreamReader(item -> fh.process(item), is);
					Get.executor().submit(reader).get();
					fh.summarize();
				}
			}

			Get.service(VersionManagmentPathService.class).rebuildPathMap();

			getLog().info("Completing processing on " + deferredActionNids.size() + " defered items");
			
			ThreadPoolExecutor tpe = Get.workExecutors().getPotentiallyBlockingExecutor();
			ArrayList<Future<?>> futures = new ArrayList<>();

			for (final int nid : deferredActionNids)
			{
				if (IsaacObjectType.SEMANTIC == Get.identifierService().getObjectTypeForComponent(nid))
				{
					final SemanticChronology sc = Get.assemblageService().getSemanticChronology(nid);

					if (sc.getVersionType() == VersionType.LOGIC_GRAPH)
					{
						futures.add(tpe.submit(() -> 
						{
							try
							{
								Get.taxonomyService().updateTaxonomy(sc);
							}
							catch (Exception e)
							{
								Map<String, Object> args = new HashMap<>();
								args.put(JsonWriter.PRETTY_PRINT, true);
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								JsonWriter json = new JsonWriter(baos, args);
	
								UUID primordial = sc.getPrimordialUuid();
								json.write(sc);
								getLog().error("Failed on taxonomy update for object with primordial UUID " + primordial.toString() + ": " + baos.toString(), e);
								json.close();
							}
						}));
					}
					else
					{
						throw new UnsupportedOperationException("1 Unexpected nid in deferred set: " + nid + " " + sc);
					}
				}
				else
				{
					throw new UnsupportedOperationException("2 Unexpected nid in deferred set: " + nid);
				}
			}
			//make sure they are all done
			for (Future<?> f : futures)
			{
				f.get();
			}

			if (this.skippedAny)
			{
				// Loading with activeOnly set to true causes a number of gaps in the concept /
				getLog().warn("Skipped components during import.");
			}
			getLog().info("Final item count: " + this.itemCount);
			LookupService.syncAll();
			Get.startIndexTask().get();

		}
		catch (final ExecutionException | InterruptedException | UnsupportedOperationException ex)
		{
			getLog().error("Loaded with exception");
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	class FileHandler
	{
		private int conceptCount, semanticCount, stampAliasCount, stampCommentCount, itemFailure, mergeCount;
		int duplicateCount = 0;
		final int statedNid = Get.identifierService().getNidForUuids(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getPrimordialUuid());
		private final HashSet<Integer> skippedItems = new HashSet<>();
		String inputIdentifier;

		protected FileHandler(String fileName)
		{
			inputIdentifier = fileName;
		}

		private void process(IsaacExternalizable object)
		{
			if (object != null)
			{
				LoadTermstore.this.itemCount++;
				try
				{
					if (null != object.getIsaacObjectType())
					{
						switch (object.getIsaacObjectType())
						{
							case CONCEPT:
								if (!LoadTermstore.this.activeOnly || isActive((Chronology) object))
								{
									try
									{
										Get.conceptService().writeConcept(((ConceptChronology) object));
										this.conceptCount++;
									}
									catch (Exception e)
									{
										getLog().error("Write Error - ", e);
										throw e;
									}
								}
								else
								{
									this.skippedItems.add(((Chronology) object).getNid());
								}

								break;

							case SEMANTIC:
								SemanticChronology sc = (SemanticChronology) object;
								if (mergeLogicGraphs && sc.getAssemblageNid() == statedNid)
								{
									final ImmutableIntSet nids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(sc.getReferencedComponentNid(), statedNid);
									if (!nids.isEmpty())
									{
										// We already loaded a stated logic graph for this concept.  Now the incoming content has another stated
										// graph for this tree.  If this is for something that involves metadata, merge them.  Otherwise, log a warning.
										final List<LogicalExpression> listToMerge = new ArrayList<>();
										final HashSet<Integer> uniqueNids = new HashSet<>();
										boolean foundMetadataModule = false;
										
										SemanticChronology existingChronology = null;
										for (int nid : nids.toArray())
										{
											Optional<? extends SemanticChronology> eco = Get.assemblageService().getOptionalSemanticChronology(nid);
											int i = 0;
											while (!eco.isPresent())
											{
												//This can happen, due to threading within the BinaryDataStreamReader.  Keep retrying until the data is present.
												try
												{
													Thread.sleep(1);
													if (i++ > 200)
													{
														break;
													}
													eco = Get.assemblageService().getOptionalSemanticChronology(nid);
												}
												catch (InterruptedException e)
												{
													// noop
												}
											}
											if (!eco.isPresent()) {
												//This should no longer happen, with the loop above, but just incase - ignore, we aren't merging those anyway.
												getLog().warn("Nid " + nid + " was listed on an assemblage, but not available from the semantic service! uuid: " + 
														Get.identifierService().getUuidPrimordialStringForNid(nid) + " referenced component: " + 
														Get.identifierService().getUuidPrimordialStringForNid(sc.getReferencedComponentNid()));
												continue;
											}
											
											for (Version v : eco.get().getVersionList())
											{
												if (v.getModuleNid()== TermAux.PRIMORDIAL_MODULE.getNid())
												{
													foundMetadataModule = true;
												}
											}
											
											existingChronology = eco.get();
											LatestVersion<Version> lgvo = existingChronology.getLatestVersion(Coordinates.Filter.DevelopmentLatestActiveOnly());
											if (lgvo.isPresent())
											{
												LogicGraphVersion lgv = ((LogicGraphVersion) lgvo.get());
												listToMerge.add(lgv.getLogicalExpression());
												uniqueNids.add(nid);
											}
										}

										LogicGraphVersion latestIncoming = getLatestLogicGraphVersion(sc);

										for (Version v : sc.getVersionList())
										{
											if (v.getModuleNid()== TermAux.PRIMORDIAL_MODULE.getNid())
											{
												foundMetadataModule = true;
											}
										}

										if (latestIncoming.isActive())
										{
											listToMerge.add(latestIncoming.getLogicalExpression());
											uniqueNids.add(sc.getNid());
										}

										if (listToMerge.size() > 1)
										{
											if (foundMetadataModule)
											{
												//This involves metadata, go ahead and merge the parents into a new graph.
												
												if (nids.size() > 1)
												{
													//We shouldn't have loaded more than one semantics that involved metadata for a concept, if merge
													//is working correctly.  Fail.
													throw new RuntimeException("Multiple loaded, active stated graphs for concept prior to addition of: " + sc);
												}
												
												Set<Integer> mergedParents = new HashSet<>();
												for (LogicalExpression le : listToMerge)
												{
													mergedParents.addAll(getParentConceptSequencesFromLogicExpression(le));
												}
												Assertion[] assertions = new Assertion[mergedParents.size()];
												LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
												int i = 0;
												for (Integer parent : mergedParents)
												{
													assertions[i++] = ConceptAssertion(parent, leb);
												}
	
												NecessarySet(And(assertions));
												byte[][] data = leb.build().getData(DataTarget.INTERNAL);
												mergeCount++;
	
												int stampSequence = Get.stampService().getStampSequence(Status.ACTIVE, System.currentTimeMillis(),
														TermAux.USER.getNid(), TermAux.SOLOR_MODULE.getNid(), TermAux.DEVELOPMENT_PATH.getNid());
												MutableLogicGraphVersion newVersion = (MutableLogicGraphVersion) existingChronology.createMutableVersion(stampSequence);
												newVersion.setGraphData(data);
												sc = existingChronology;
											}
											else if (uniqueNids.size() > 1)
											{
												//don't merge non-metadata graphs.  Builders of IBDF files should have their own file built properly.
												//But, if there is more than one nid, that means we have multiple stated graphs (not multiple versions of a single
												//graph - thats an error
												if (duplicateCount < duplicatesToPrint)
												{
													getLog().warn("Multiple active stated graphs found for concept.  Not merging.  New graph: " + sc);
												}
												duplicateCount++;
											}
										}
									}
								}

								if (!LoadTermstore.this.semanticTypesToSkip.contains(sc.getVersionType())
										&& (!LoadTermstore.this.activeOnly || (isActive(sc) && !this.skippedItems.contains(sc.getReferencedComponentNid()))))
								{
									try
									{
										Get.assemblageService().writeSemanticChronology(sc);
										if (sc.getVersionType() == VersionType.LOGIC_GRAPH)
										{
											deferredActionNids.add(sc.getNid());
										}

										this.semanticCount++;
									}
									catch (Exception e)
									{
										getLog().error("Write Error - while processing: " + sc.toUserString(), e);
										throw e;
									}
								}
								else
								{
									this.skippedItems.add(sc.getNid());
								}

								break;

							case STAMP_ALIAS:
								Get.commitService().addAlias(((StampAlias) object).getStampSequence(), ((StampAlias) object).getStampAlias(), null);
								this.stampAliasCount++;
								break;

							case STAMP_COMMENT:
								Get.commitService().setComment(((StampComment) object).getStampSequence(), ((StampComment) object).getComment());
								this.stampCommentCount++;
								break;

							default :
								throw new UnsupportedOperationException("Unknown object type: " + object);
						}
					}
				}
				catch (final UnsupportedOperationException e)
				{
					this.itemFailure++;
					getLog().error("Failure at " + this.conceptCount + " concepts, " + this.semanticCount + " semantics, " + this.stampAliasCount
							+ " stampAlias, " + this.stampCommentCount + " stampComments", e);

					final Map<String, Object> args = new HashMap<>();

					args.put(JsonWriter.PRETTY_PRINT, true);

					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonWriter json = new JsonWriter(baos, args))
					{
						UUID primordial = null;

						if (object instanceof Chronology)
						{
							primordial = ((Chronology) object).getPrimordialUuid();
						}

						json.write(object);
						getLog().error(
								"Failed on " + ((primordial == null) ? ": " : "object with primordial UUID " + primordial.toString() + ": ") + baos.toString());
					}
				}

				if (LoadTermstore.this.itemCount % 50000 == 0)
				{
					getLog().info("Read " + LoadTermstore.this.itemCount + " entries, " + "Loaded " + this.conceptCount + " concepts, " + this.semanticCount
							+ " semantics, " + this.stampAliasCount + " stampAlias, " + this.stampCommentCount + " stampComment");
				}
			}
		}

		protected void summarize()
		{
			if (this.skippedItems.size() > 0)
			{
				LoadTermstore.this.skippedAny = true;
			}

			getLog().info("Loaded " + this.conceptCount + " concepts, " + this.semanticCount + " semantics, " + this.stampAliasCount + " stampAlias, "
					+ stampCommentCount + " stampComments, " + mergeCount + " merged semantics"
					+ (skippedItems.size() > 0 ? ", skipped for inactive " + skippedItems.size() : "")
					+ ((duplicateCount > 0) ? " Duplicates " + duplicateCount : "") + ((this.itemFailure > 0) ? " Failures " + this.itemFailure : "") + " from "
					+ inputIdentifier);
			getLog().info("running item count: " + LoadTermstore.this.itemCount);
			this.conceptCount = 0;
			this.semanticCount = 0;
			this.stampAliasCount = 0;
			this.stampCommentCount = 0;
			this.skippedItems.clear();
		}
	}

	/**
	 * Skip semantic types.
	 *
	 * @param types the types
	 */
	public void skipVersionTypes(Collection<VersionType> types)
	{
		this.semanticTypesToSkip.addAll(types);
	}

	/**
	 * Don't put us into DB Build mode on startup
	 */
	public void dontSetDBMode()
	{
		this.setDBBuildMode = false;
	}
	
	/**
	 * Returns true, if any items were skipped during processing, false otherwise.
	 * @return
	 */
	public boolean skippedAny()
	{
		return skippedAny;
	}

	/**
	 * Checks if active.
	 *
	 * @param object the object
	 * @return true, if active
	 */
	private boolean isActive(Chronology object)
	{
		Boolean foundStatus = null;
		for (Version v : object.getVersionList())
		{
			if (foundStatus == null)
			{
				foundStatus = v.getStatus() == Status.ACTIVE;
			}
			else if (foundStatus != (v.getStatus() == Status.ACTIVE))
			{
				throw new RuntimeException("Simple implementation can't handle version list with differing status values");
			}
		}
		if (foundStatus == null)
		{
			throw new RuntimeException("no version found???");
		}
		return foundStatus.booleanValue();
	}

	/**
	 * Gets the latest logic graph version
	 *
	 * @param sc the sc
	 * @return the latest logical expression
	 */
	private static LogicGraphVersion getLatestLogicGraphVersion(SemanticChronology sc)
	{
		LogicGraphVersion latestVersion = null;

		for (final StampedVersion version : sc.getVersionList())
		{
			if (latestVersion == null || latestVersion.getTime() < version.getTime())
			{
				latestVersion = (LogicGraphVersion) version;
			}
		}
		return latestVersion;
	}

	/**
	 * Shamelessly copied from FRILLS, as I can't use it there, due to dependency chain issues. But then modified a bit,
	 * so it fails if it encounters things it can't handle.
	 */
	private Set<Integer> getParentConceptSequencesFromLogicExpression(LogicalExpression logicExpression)
	{
		Set<Integer> parentConceptSequences = new HashSet<>();
		List<LogicNode> necessaryNodes = logicExpression.getNodesOfType(NodeSemantic.NECESSARY_SET);
		int necessaryCount = 0;
		int allCount = 1;  //start at 1, for root.
		for (LogicNode necessarySetNode : necessaryNodes)
		{
			necessaryCount++;
			allCount++;
			for (LogicNode childOfNecessarySetNode : necessarySetNode.getChildren())
			{
				allCount++;
				if (null == childOfNecessarySetNode.getNodeSemantic())
				{
					// we don't understand this log graph.  Return an empty set to our call above doesn't use this mechanism
					return new HashSet<>();
				}
				else
					switch (childOfNecessarySetNode.getNodeSemantic())
					{
						case AND:
							AndNode andNode = (AndNode) childOfNecessarySetNode;
							for (AbstractLogicNode childOfAndNode : andNode.getChildren())
							{
								allCount++;
								if (childOfAndNode.getNodeSemantic() == NodeSemantic.CONCEPT)
								{
									if (childOfAndNode instanceof ConceptNodeWithNids)
									{
										ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) childOfAndNode;
										parentConceptSequences.add(conceptNode.getConceptNid());
									}
									else if (childOfAndNode instanceof ConceptNodeWithUuids)
									{
										ConceptNodeWithUuids conceptNode = (ConceptNodeWithUuids) childOfAndNode;
										parentConceptSequences.add(Get.identifierService().getNidForUuids(conceptNode.getConceptUuid()));
									}
									else
									{
										// Should never happen - return an empty set to our call above doesn't use this mechanism
										return new HashSet<>();
									}
								}
							}
							break;
						case CONCEPT:
							if (childOfNecessarySetNode instanceof ConceptNodeWithNids)
							{
								ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) childOfNecessarySetNode;
								parentConceptSequences.add(conceptNode.getConceptNid());
							}
							else if (childOfNecessarySetNode instanceof ConceptNodeWithUuids)
							{
								ConceptNodeWithUuids conceptNode = (ConceptNodeWithUuids) childOfNecessarySetNode;
								parentConceptSequences.add(Get.identifierService().getNidForUuids(conceptNode.getConceptUuid()));
							}
							else
							{
								// Should never happen - return an empty set to our call above doesn't use this mechanism
								return new HashSet<>();
							}
							break;
						default :
							// we don't understand this log graph.  Return an empty set to our call above doesn't use this mechanism
							return new HashSet<>();
					}
			}
		}

		if (logicExpression.getRoot().getChildren().length != necessaryCount || allCount != logicExpression.getNodeCount())
		{
			// we don't understand this log graph.  Return an empty set to our call above doesn't use this mechanism
			return new HashSet<>();
		}

		return parentConceptSequences;
	}
}