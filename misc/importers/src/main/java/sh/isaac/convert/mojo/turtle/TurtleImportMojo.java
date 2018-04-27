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
package sh.isaac.convert.mojo.turtle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;
import javafx.application.Platform;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCode;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DataWriteListenerImpl;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.directUtils.LoggingConfig;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.DataStore;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.utility.LanguageMap;

/**
 * 
 * {@link TurtleImportMojo}
 * 
 * Goal which converts CPT data into the workbench jbin format
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Mojo(name = "convert-turtle-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class TurtleImportMojo extends ConverterBaseMojo
{
	private Logger log = LogManager.getLogger();

	private ConverterUUID converterUUID;
	private DirectWriteHelper dwh;

	private int moduleNid;
	private int authorNid;
	private UUID rootConcept;

	private HashMap<String, List<Statement>> allStatements;
	private HashMap<UUID, Resource> conceptsToBeBuilt = new HashMap<>();
	private HashSet<UUID> conceptsToHangFromRoot = new HashSet<>();
	
	private final UUID fsn = MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid();
	private final UUID regularName = MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid();
	private final UUID definition = MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid();
	private final UUID insensitive = MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid();
	private final UUID preferred = MetaData.PREFERRED____SOLOR.getPrimordialUuid();
	private final UUID acceptable = MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid();
	
	private HashMap<String, String> possibleAssociations = new HashMap<>();
	private HashMap<String, String> possibleStringAttributes = new HashMap<>();
	private HashMap<String, String> possibleRelationships = new HashMap<>();

	/**
	 * Constructor for maven
	 */
	public TurtleImportMojo()
	{
		// This constructor is for maven
	}

	/**
	 * Constructor for runtime use
	 * 
	 * @param outputDirectory {@link #outputDirectory}
	 * @param inputFileLocation {@link #inputFileLocation}
	 * @param converterVersion {@link #converterVersion}
	 * @param converterSourceArtifactVersion {@link #converterSourceArtifactVersion}
	 */
	public TurtleImportMojo(File outputDirectory, File inputFileLocation, String converterVersion, String converterSourceArtifactVersion)
	{
		this();
		this.outputDirectory = outputDirectory;
		this.inputFileLocation = inputFileLocation;
		this.converterVersion = converterSourceArtifactVersion;
		this.converterSourceArtifactVersion = converterSourceArtifactVersion;
		converterUUID = new ConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, false);
		
		possibleAssociations.put("http://www.w3.org/2000/01/rdf-schema#seeAlso", "see also");
		possibleAssociations.put("http://purl.org/dc/terms/replaces", "replaces");
		possibleAssociations.put("http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "is defined by");
		possibleAssociations.put("http://www.w3.org/2002/07/owl#disjointWith", "disjoint with");
		possibleAssociations.put("http://www.w3.org/2002/07/owl#sameAs", "same as");
		possibleAssociations.put("http://www.w3.org/2000/01/rdf-schema#domain", "domain");
		possibleAssociations.put("http://www.w3.org/2000/01/rdf-schema#range", "range");
		possibleAssociations.put("http://purl.org/dc/terms/hasFormat", "has format");
		possibleAssociations.put("http://www.w3.org/2002/07/owl#inverseOf", "inverse of");
		
		possibleStringAttributes.put("http://www.w3.org/2003/06/sw-vocab-status/ns#term_status", "term status");
		possibleStringAttributes.put("http://purl.org/dc/terms/issued", "issued");
		possibleStringAttributes.put("http://purl.org/dc/terms/modified", "modified");
		possibleStringAttributes.put("http://creativecommons.org/ns#license", "license");
		possibleStringAttributes.put("http://purl.org/dc/terms/identifier", "identifier");
		
		possibleRelationships.put("http://purl.org/vocab/vann/termGroup", "term group");  //TODO figure out how to handle...
	}

	/**
	 * This is the execution path when maven is running.
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			LoggingConfig.configureLogging(outputDirectory, converterOutputArtifactClassifier);

			converterUUID = Get.service(ConverterUUID.class);

			String outputName = converterOutputArtifactId
					+ (StringUtils.isBlank(converterOutputArtifactClassifier) ? "" : "-" + converterOutputArtifactClassifier) + "-"
					+ converterOutputArtifactVersion;
			Path ibdfFileToWrite = new File(outputDirectory, outputName + ".ibdf").toPath();
			ibdfFileToWrite.toFile().delete();

			log.info("Writing IBDF to " + ibdfFileToWrite.toFile().getCanonicalPath());

			File file = new File(outputDirectory, "isaac-db");
			// make sure this is empty
			FileUtils.deleteDirectory(file);

			Get.configurationService().setDataStoreFolderPath(file.toPath());

			LookupService.startupPreferenceProvider();

			Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);

			LookupService.startupIsaac();

			// Don't need to build indexes
			for (IndexBuilderService ibs : LookupService.getServices(IndexBuilderService.class))
			{
				ibs.setEnabled(false);
			}

			DataWriteListenerImpl listener = new DataWriteListenerImpl(ibdfFileToWrite, null);

			// we register this after the metadata has already been written.
			LookupService.get().getService(DataStore.class).registerDataWriteListener(listener);

			processTurtle();

		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * This is the execution path when maven is running, or, when you want to run it directly.
	 * @throws IOException
	 */
	public void processTurtle() throws IOException
	{
		File ttlFile = null;
		for (File f : inputFileLocation.listFiles())
		{
			if (f.getName().toLowerCase().endsWith("ttl"))
			{
				ttlFile = f;
				break;
			}
		}

		getLog().info("Reading " + ttlFile.getCanonicalPath());

		Model model = ModelFactory.createDefaultModel();
		model.read(new FileReader(ttlFile), "", "TURTLE");

		log.info("The read TURTLE graph contains {} objects", model.getGraph().size());

		allStatements = new HashMap<>();
		HashSet<String> allPredicates = new HashSet<>();

		model.listStatements().forEachRemaining(statement -> {
			String key = statement.getSubject().getURI() == null ? statement.getSubject().getId().getLabelString() : statement.getSubject().getURI();

			List<Statement> statements = allStatements.get(key);
			if (statements == null)
			{
				statements = new ArrayList<Statement>();
				allStatements.put(key, statements);
			}
			statements.add(statement);
			allPredicates.add(statement.getPredicate().getURI());
		});

		log.info("The read TURTLE file contains {} subjects", allStatements.size());
//		model.listSubjects().forEachRemaining(subject ->
//		{
//			System.out.println("Subject: " + subject.getClass() + ": " + subject.toString());
//		});

		HashSet<String> processedSubjects = new HashSet<>();

		for (String s : allStatements.keySet())
		{
			System.out.println("Subject: " + s + " :" + allStatements.get(s).size());
			for (Statement st : allStatements.get(s))
			{
				System.out.println("  " + st.toString());
			}
		}
		
		UUID parentModule = null;
		long releaseTime = 0;
		ResIterator typeIterator = model.listSubjectsWithProperty(model.getProperty("http://purl.org/dc/terms/type"));
		while (typeIterator.hasNext())
		{
			if (parentModule != null)
			{
				throw new RuntimeException("Not set up to handle multiple ontologies in a file yet");
			}
			String subject = typeIterator.next().getURI();
			List<Statement> statements = allStatements.get(subject);
			if (statements == null)
			{
				throw new RuntimeException("Can't find statements that define the ontology");
			}
			
			//We found the one that defines the what we will treat as the root of the ontology.
			//Need to create a Module group concept (if not present) and a module version present
			//Finally, create the concept for this set of statements, and hang it under solor_root
			
			releaseTime = findTime(statements);
			String preferredNamespaceUri = findPredicateValue("http://purl.org/vocab/vann/preferredNamespaceUri", statements).asLiteral().getString();  //http://rdfs.co/bevon/
			String title = findPredicateValue("http://purl.org/dc/terms/title", statements).asLiteral().getString(); //"BEVON: Beverage Ontology"
			String identifier = findPredicateValue("http://purl.org/dc/terms/identifier", statements).asLiteral().getString();  //"http://rdfs.co/bevon/0.8"
			String version = findPredicateValue("http://www.w3.org/2002/07/owl#versionInfo", statements).asLiteral().getString();  //"0.8"
			
			authorNid = TermAux.USER.getNid();  // TODO read from somewhere?
			
			if (!subject.equals(identifier))
			{
				throw new RuntimeException("Was expecting these to be the same: " + subject + ", " + identifier);
			}

			parentModule = getConceptUUID(preferredNamespaceUri + " modules");
			
			if (!Get.conceptService().hasConcept(Get.identifierService().getNidForUuids(parentModule)))
			{
				//We don't have a module of our own yet, so put the "grouping" concept on the solor module.
				moduleNid = MetaData.MODULE____SOLOR.getNid();
				dwh = new DirectWriteHelper(authorNid, moduleNid, MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID);
				dwh.makeConcept(parentModule, Status.ACTIVE, releaseTime);
				
				dwh.makeDescriptionEn(parentModule, preferredNamespaceUri + " modules", fsn, 
						insensitive, 
						Status.ACTIVE, releaseTime, preferred);
				
				dwh.makeDescriptionEn(parentModule, title + " modules", regularName, 
						insensitive, 
						Status.ACTIVE, releaseTime, preferred);
				dwh.makeParentGraph(parentModule, Arrays.asList(new UUID[] {MetaData.MODULE____SOLOR.getPrimordialUuid()}), Status.ACTIVE, releaseTime);
			}
			
			//We have now created a Bevon modules grouping concept.  Configure to that module for further work...
			converterUUID.configureNamespace(parentModule);  //UUID generation always using the parent grouping namespace, not a version-specific module.
			moduleNid = Get.identifierService().getNidForUuids(parentModule);
			//Switch the direct write helper to the bevon module for the 'version specific' module...
			if (dwh == null)
			{
				dwh = new DirectWriteHelper(authorNid, moduleNid, MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID);
			}
			else
			{
				dwh.changeModule(moduleNid);
			}

			//Create a module for this version.
			UUID versionModule = getConceptUUID(identifier + " module");
			moduleNid = Get.identifierService().assignNid(versionModule);
			dwh.changeModule(moduleNid);  //change to the version specific module for all future work.

			dwh.makeConcept(versionModule, Status.ACTIVE, releaseTime);
			dwh.makeDescriptionEn(versionModule, identifier + " module", fsn, 
					insensitive, 
					Status.ACTIVE, releaseTime, preferred);
			dwh.makeDescriptionEn(versionModule, title + " " + version + " module", regularName, 
					insensitive, 
					Status.ACTIVE, releaseTime, preferred);
			dwh.makeParentGraph(versionModule, Arrays.asList(new UUID[] {parentModule}), Status.ACTIVE, releaseTime);

			for (RDFNode value : findPredicateValues("http://purl.org/vocab/vann/termGroup", statements))
			{
				conceptsToHangFromRoot.add(getConceptUUID(value.asResource().getURI()));
			}
			
			//Set up our metadata hierarchy
			dwh.makeMetadataHierarchy(title, true, true, true, true, true, releaseTime);
			
			//Create some types...
			for (Entry<String, String> entry : possibleAssociations.entrySet())
			{
				if (allPredicates.contains(entry.getKey()))
				{
					UUID associationUUID = getConceptUUID(entry.getKey());
					dwh.makeConcept(associationUUID, Status.ACTIVE, releaseTime);
					dwh.makeDescriptionEn(associationUUID, entry.getKey(), fsn, insensitive, Status.ACTIVE, releaseTime, preferred);
					dwh.makeDescriptionEn(associationUUID, entry.getValue(), regularName, insensitive, Status.ACTIVE, releaseTime, preferred);
					dwh.makeParentGraph(associationUUID, dwh.getAssociationTypes().get(), Status.ACTIVE, releaseTime);
					dwh.configureConceptAsAssociation(associationUUID, entry.getKey(), null, IsaacObjectType.CONCEPT, null, releaseTime);
				}
			}
			
			for (Entry<String, String> entry : possibleStringAttributes.entrySet())
			{
				if (allPredicates.contains(entry.getKey()))
				{
					UUID stringUUID = getConceptUUID(entry.getKey());
					dwh.makeConcept(stringUUID, Status.ACTIVE, releaseTime);
					dwh.makeDescriptionEn(stringUUID, entry.getKey(), fsn, insensitive, Status.ACTIVE, releaseTime, preferred);
					dwh.makeDescriptionEn(stringUUID, entry.getValue(), regularName, insensitive, Status.ACTIVE, releaseTime, preferred);
					dwh.makeParentGraph(stringUUID, dwh.getAssociationTypes().get(), Status.ACTIVE, releaseTime);
					dwh.configureConceptAsDynamicAssemblage(stringUUID, entry.getKey(), 
							new DynamicColumnInfo[] {
									new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true, true)}, 
							null, null, releaseTime);
				}
			}
			
			rootConcept = process(statements);
			processedSubjects.add(subject);
			dwh.makeParentGraph(rootConcept, Arrays.asList(new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}), Status.ACTIVE, releaseTime);
			
			
			//Technically, we could break here, but would rather validate that this isn't a second entry that fits the criteria - exception will be thrown if we 
			//loop through this code a second time.
		}

		for (Entry<String, List<Statement>> entries : allStatements.entrySet())
		{
			if (processedSubjects.contains(entries.getKey()))
			{
				continue;
			}
			else
			{
				process(entries.getValue());
				processedSubjects.add(entries.getKey());
			}
		}
		
		Iterator<UUID> it = conceptsToBeBuilt.keySet().iterator();
		while (it.hasNext())
		{
			if (Get.conceptService().hasConcept(Get.identifierService().getNidForUuids(it.next())))
			{
				it.remove();
			}
		}
		
		if (conceptsToBeBuilt.size() > 0)
		{
			//There were target objects that pointed to a resource we haven't resolved.  Just make placeholders, and hang them under our tree
			//so we can navigate things.
			UUID unresolvedConcepts = getConceptUUID("Unresolved External References");
			
			dwh.makeConcept(unresolvedConcepts, Status.ACTIVE, releaseTime, (UUID[])null);
			dwh.makeDescriptionEn(unresolvedConcepts, "Unresolved External References", fsn, 
					insensitive, 
					Status.ACTIVE, releaseTime, preferred);
			dwh.makeDescriptionEn(unresolvedConcepts, "These are external ontology references that were not fully resolved while processing this file.  They are created here"
					+ " as placeholders, so they could be manually resolved and also to make the hierarchy navigable.", 
					definition, 
					insensitive, 
					Status.ACTIVE, releaseTime, preferred);
			dwh.makeParentGraph(unresolvedConcepts, Arrays.asList(new UUID[] {rootConcept}), Status.ACTIVE, releaseTime);
			
			for (Entry<UUID, Resource> entry : conceptsToBeBuilt.entrySet())
			{
				dwh.makeConcept(entry.getKey(), Status.ACTIVE, releaseTime, (UUID[])null);
				dwh.makeDescriptionEn(entry.getKey(), entry.getValue().getURI(), fsn, 
						insensitive, 
						Status.ACTIVE, releaseTime, preferred);
				dwh.makeDescriptionEn(entry.getKey(), entry.getValue().getLocalName(), regularName, 
						insensitive, 
						Status.ACTIVE, releaseTime, preferred);
				dwh.makeParentGraph(entry.getKey(), Arrays.asList(new UUID[] {unresolvedConcepts}), Status.ACTIVE, releaseTime);
			}
		}
		
		dwh.processTaxonomyUpdates();
		
		Get.taxonomyService().notifyTaxonomyListenersToRefresh();

		ConsoleUtil.println("Dumping UUID Debug File");
		converterUUID.dump(this.outputDirectory, "TurtleUUID");
		converterUUID.clearCache();
		
		log.info("Load Stats:");
		for (final String s : this.dwh.getLoadStats().getSummary())
		{
			log.info("  " + s);
		}
	}
	
	/**
	 * Same as {@link #findPredicateValues(String, List)}, but only expects to find one entry.
	 * @param predicate
	 * @param statements
	 * @return
	 */
	private RDFNode findPredicateValue(String predicate, List<Statement> statements)
	{
		List<RDFNode> values = findPredicateValues(predicate, statements);
		if (values.size() != 1)
		{
			throw new RuntimeException("There are " + values.size() + " for " + predicate + " when one was expected");
		}
		return values.get(0);
	}
	
	private List<RDFNode> findPredicateValues(String predicate, List<Statement> statements)
	{
		List<RDFNode> values = new ArrayList<>();
		for (Statement s : statements)
		{
			if (s.getPredicate().isResource() && s.getPredicate().asResource().getURI().equals(predicate))
			{
				values.add(s.getObject());
			}
		}
		return values;
	}

	/**
	 * @param statements
	 * @return
	 */
	private long findTime(List<Statement> statements)
	{
		List<RDFNode> times = findPredicateValues("http://purl.org/dc/terms/modified", statements);
		if (times.size() > 1)
		{
			throw new RuntimeException("Multiple modified times?");
		}
		else if (times.size() == 0)
		{
			times = findPredicateValues("http://purl.org/dc/terms/issued", statements);
		}
		
		if (times.size() > 1)
		{
			throw new RuntimeException("Multiple issued times?");
		}
		else if (times.size() == 0)
		{
			throw new RuntimeException("no time found");
		}
		return  ((XSDDateTime) times.get(0).asLiteral().getValue()).asCalendar().getTimeInMillis();
	}

	/**
	 * @param subjectStatements a list of statements which all share the same subject
	 */
	private UUID process(List<Statement> subjectStatements)
	{

		String subjectFQN = subjectStatements.get(0).getSubject().getURI();
		if (subjectFQN == null)
		{
			// TODO special handling for null node...
			String nullNodeId = subjectStatements.get(0).getSubject().getId().getLabelString();
			return null;
		}
		else
		{
			ArrayList<Statement> properties = new ArrayList<>(subjectStatements.size());
			ArrayList<Statement> parentStatements = new ArrayList<>(subjectStatements.size());
			long time = 0;
			Statement timeStatementStash = null;
			Status status = Status.ACTIVE;  //haven't yet seen an attribute that would qualify to inactivate something
			String title = null;
			
			for (Statement s : subjectStatements)
			{
				if (s.getPredicate().isURIResource())
				{
					if (s.getPredicate().getURI().equals("http://purl.org/dc/terms/modified"))
					{
						time = ((XSDDateTime) s.getObject().asLiteral().getValue()).asCalendar().getTimeInMillis();

						if (timeStatementStash != null)
						{
							properties.add(timeStatementStash);
							timeStatementStash = null;
						}
					}
					else if (time == 0 && s.getPredicate().getURI().equals("http://purl.org/dc/terms/issued"))
					{
						((XSDDateTime) s.getObject().asLiteral().getValue()).asCalendar().getTimeInMillis();
						timeStatementStash = s;
					}
					// it may not be correct to treat both of these as isA, but will for now....
					else if (s.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")
							|| (s.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 
									&& !s.getObject().asResource().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag")
									&& !s.getObject().asResource().getURI().equals("http://www.w3.org/2002/07/owl#Class")))
					{
						parentStatements.add(s);
					}
					else if (s.getPredicate().getURI().equals("http://purl.org/dc/terms/title"))
					{
						title = s.getObject().asLiteral().getString();
					}
					else
					{
						properties.add(s);
					}
				}
				else
				{
					throw new RuntimeException("Unhandled Predicate: " + s.getPredicate());
				}
			}

			UUID concept = getConceptUUID(subjectFQN);
			
			UUID additional = null;
			if (title != null)
			{
				additional = getConceptUUID(title);
			}
			
			dwh.makeConcept(concept, status, time, new UUID[] {additional});

			UUID language = MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid();
			UUID dialect = MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid();
			dwh.makeDescription(concept, (title == null ? subjectFQN : title), fsn, language,
					insensitive, status, time, dialect,
					preferred);
			
			
			String subjectRegularName = subjectStatements.get(0).getSubject().getLocalName();
			if (StringUtils.isNotBlank(subjectRegularName))
			{
				dwh.makeDescription(concept, subjectRegularName, regularName, language,
					insensitive, status, time, dialect,
					preferred);
			}
			
			if (title != null)
			{
				dwh.makeDescription(concept, subjectFQN, fsn, language,
						insensitive, status, time, dialect,
						acceptable);
			}

			boolean madeRefset = handleProperties(concept,time, subjectRegularName, properties);

			ArrayList<UUID> parents = new ArrayList<>();
			for (Statement s : parentStatements)
			{
				UUID uuid = getConceptUUID(s.getObject().asResource().getURI());
				parents.add(uuid);
				if (!Get.conceptService().hasConcept(Get.identifierService().getNidForUuids(uuid)))
				{
					conceptsToBeBuilt.put(uuid, s.getObject().asResource());
				}
			}
			if (conceptsToHangFromRoot.contains(concept))
			{
				parents.add(rootConcept);
			}
			if (madeRefset)
			{
				parents.add(dwh.getRefsetTypes().get());
			}
			
			if (parents.size() == 0)
			{
				log.warn("Orphan? {}", subjectFQN);
			}
			else
			{
				dwh.makeParentGraph(concept, parents, status, time);
			}
			return concept;
		}
		
	}

	/**
	 * @param propStatements
	 * @return true, if this concept was turned into a refset definition by the properties we processed
	 */
	private boolean handleProperties(UUID concept, long time, String localName, ArrayList<Statement> propStatements)
	{
		List<RDFNode> typeStatement = findPredicateValues("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", propStatements);
		boolean buildBag = false;
		if (typeStatement.size() == 1 && typeStatement.get(0).asResource().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag"))
		{
			buildBag = true;
			dwh.configureConceptAsDynamicAssemblage(concept, "Owl Bag", null, null, null, time);
		}

		for (Statement s : propStatements)
		{
			if (s.getPredicate().asResource().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && buildBag)
			{
				//skip, already handled
			}
			if (buildBag && s.getPredicate().asResource().getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_"))
			{
				//bag entry
				dwh.makeDynamicRefsetMember(concept, getConceptUUID(s.getObject().asResource().getURI()), time);
			}
			else if (s.getPredicate().asResource().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label") ||
						s.getPredicate().asResource().getURI().equals("http://www.w3.org/2004/02/skos/core#prefLabel"))
			{
				if (s.getObject().asLiteral().getString().equals(localName))
				{
					//skip, otherwise, we make a dupe description
					continue;
				}
				LanguageCode lc = LanguageCode.getLangCode(s.getObject().asLiteral().getLanguage());
				dwh.makeDescription(concept, s.getObject().asLiteral().getString(), regularName, 
						LanguageMap.getConceptForLanguageCode(lc).getPrimordialUuid(), 
						insensitive, Status.ACTIVE, time, 
						LanguageMap.getConceptDialectForLanguageCode(lc).getPrimordialUuid(), 
						acceptable);
			}
			else if (s.getPredicate().asResource().getURI().equals("http://www.w3.org/2004/02/skos/core#definition"))
			{
				LanguageCode lc = LanguageCode.getLangCode(s.getObject().asLiteral().getLanguage());
				dwh.makeDescription(concept, s.getObject().asLiteral().getString(), definition, 
						LanguageMap.getConceptForLanguageCode(lc).getPrimordialUuid(), 
						insensitive, Status.ACTIVE, time, 
						LanguageMap.getConceptDialectForLanguageCode(lc).getPrimordialUuid(), 
						acceptable);
			}
			else if (dwh.isAssociation(getConceptUUID(s.getPredicate().asResource().getURI())))
			{
				dwh.makeAssociation(getConceptUUID(s.getPredicate().asResource().getURI()), concept, getConceptUUID(s.getObject().asResource().getURI()), time);
				conceptsToBeBuilt.put(getConceptUUID(s.getObject().asResource().getURI()), s.getObject().asResource());
			}
			else if (s.getPredicate().asResource().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			{
				dwh.makeComment(concept, s.getObject().asLiteral().getString(), null, time);
			}
			else if (possibleStringAttributes.containsKey(s.getPredicate().asResource().getURI()))
			{
				dwh.makeDynamicSemantic(getConceptUUID(s.getPredicate().asResource().getURI()), concept, 
						new DynamicData[] {new DynamicStringImpl(s.getObject().toString())}, time);
			}
			else
			{
				log.info("property type not yet handled: " + s.getPredicate().asResource().getURI());
				//TODO finish properties
			}
		}
		return buildBag;
	}
	

	/**
	 * Get a UUID from a string WITHOUT doing duplicate generation checking. You should only use this method
	 * if you are sure you aren't creating a duplicate entry...
	 * 
	 * @param input
	 * @return
	 */
	private UUID getConceptUUID(String input)
	{
		UUID temp = converterUUID.createNamespaceUUIDFromString(input, true);
		Get.identifierService().assignNid(temp);
		return temp;
	}

	public static void main(String[] args) throws MojoExecutionException, IOException
	{
		try
		{
			File file = new File("target", "isaac-turtle.data");
			// make sure this is empty
			FileUtils.deleteDirectory(file);

			Get.configurationService().setDataStoreFolderPath(file.toPath());

			Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);

			LookupService.startupIsaac();

			TurtleImportMojo tim = new TurtleImportMojo(new File("../../integration/db-config-builder-ui/target/converter-executor/target/"),
					new File("src/main/java/sh/isaac/convert/mojo/turtle/"), "SNAPSHOT", "0.8");
			// i.inputFileLocation= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
			tim.processTurtle();
		}
		finally
		{
			LookupService.shutdownSystem();
			Platform.exit();
		}
	}
}