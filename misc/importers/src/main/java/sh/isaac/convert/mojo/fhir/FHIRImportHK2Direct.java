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
package sh.isaac.convert.mojo.fhir;

import fhir.*;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCode;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidFactory;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.semantic.types.*;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.utility.LanguageMap;

import java.io.File;
import java.io.IOException;
import java.lang.String;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * {@link FHIRImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class FHIRImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private DateTimeFormatter timeParser = new DateTimeFormatterBuilder().appendPattern("yyyy[-MM[-dd['T'HH:mm:ss[.SSS]xxx]]]")
			.parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
			.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
			.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
			.parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
			.toFormatter().withZone(ZoneId.of("UTC"));
	
	private UUID fhirRootConcept;
	private long oldestDate = Long.MAX_VALUE;
	
	//These are attributes on the code system itself, or valueset, rather that ones defined in the code systems
	private static final String URI = "URI";
	private static final String URL = "URL";
	private static final String ID = "id";
	private static final String LANGUAGE = "language";
	private static final String CODESYSTEM_STATUS = "codesystem status";
	private static final String PUBLISHER = "publisher";
	private static final String CONTACT = "contact";
	private static final String EXTENSION = "extension";
	private static final String USE = "use";
	private static final String CODING = "coding";
	private static final String TITLE = "title";
	private static final String DESIGNIATION = "designation";
	private static final String COMMENTS = "comments";
	private static final String VERSION = "version";
	private static final String PUBLICATION_STATUS = "publication status";
	private static final String ALL_FHIR_REFSET = "All FHIR Concepts";
	private static final String PROFILE = "profile";
	private static final String EXPERIMENTAL = "experimental";
	private static final String COPYRIGHT = "copyright";
	private static final String IMMUTABLE = "immutable";
	private static final String IMPLICIT_RULES = "implicit rules";
	private static final String JURISDICTION = "jurisdiction";
	private static final String TYPE = "type";
	private static final String SYSTEM = "system";
	private static final String PURPOSE = "purpose";
	private static final String VERSION_NEEDED = "version needed";
	private static final String CASE_SENSITIVE = "case sensitive";
	private static final String IDENTIFIER = "identifier";
	private static final String META = "meta";
	private static final String TELECOM = "telecom";
	private static final String HIERARCHY_MEANING = "hierarchy meaning";
	
	private HashMap<String, UUID> uriCodeToUUIDMap = new HashMap<>();
	private UUID columnNameGroupConcept;
	
	private final AtomicInteger translationSanityCheck = new AtomicInteger();

	/**
	 * This constructor is for maven and HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link DirectConverter#configure(File, Path, String, StampFilter)} method on it.
	 * For maven and HK2, Must set transaction via void setTransaction(Transaction transaction);
	 */
	protected FHIRImportHK2Direct() {
	}
	protected FHIRImportHK2Direct(Transaction transaction)
	{
		super(transaction);
	}
	
	@Override
	public ConverterOptionParam[] getConverterOptions()
	{
		return new ConverterOptionParam[] {};
	}

	@Override
	public void setConverterOption(String internalName, String... values)
	{
		//noop, we don't require any.
	}
	
	/**
	 * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent(Transaction, Consumer, BiConsumer)}
	 * If this was constructed via the constructor that takes parameters, you do not need to call this.
	 * 
	 * @see DirectConverter#configure(File, Path, String, StampFilter)
	 */
	@Override
	public void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampFilter stampFilter)
	{
		this.outputDirectory = outputDirectory;
		this.inputFileLocationPath = inputFolder;
		this.converterSourceArtifactVersion = converterSourceArtifactVersion;
		this.converterUUID = new ConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, false);
		this.readbackCoordinate = stampFilter == null ? Coordinates.Filter.DevelopmentLatest() : stampFilter;
	}
	
	@Override
	public SupportedConverterTypes[] getSupportedTypes()
	{
		return new SupportedConverterTypes[] {SupportedConverterTypes.MVX};
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Transaction transaction, Consumer, BiConsumer)
	 * @see DirectConverter#convertContent(Transaction transaction, Consumer, BiConsumer)
	 */
	@Override
	public void convertContent(Transaction transaction, Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException
	{

		FHIRReader fr = new FHIRReader(this.inputFileLocationPath);
		statusUpdates.accept("Read " + fr.bundles.size() + " bundles, " + fr.codeSystems.size() + " CodeSystems, and " + fr.valueSets.size() + " ValueSets");

		
		// There is no global release date for FHIR - but each item has its own date.  Iterate over, and find the oldest date we can, for 
		// use on the shared, global FHIR metadata.
		findOldestDate(fr);

		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.FHIR_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, 
				"FHIR", false);
		
		//set up our shared module, with metadata that applies to all code systems and value sets being loaded in the FHIR format.
		setupModule("FHIR metadata", MetaData.FHIR_MODULES____SOLOR.getPrimordialUuid(), Optional.empty(), oldestDate);
		
		//Set up our metadata hierarchy
		dwh.makeMetadataHierarchy(transaction, true, true, false, false, true, false, oldestDate);
	
		columnNameGroupConcept = dwh.makeOtherMetadataRootNode(transaction, "Complex Attribute Column Names", oldestDate);
		UUID version = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "Version", null, null, null, null, null, oldestDate);
		UUID display = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "Display", null, null, null, null, null, oldestDate);
		UUID userSelected = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "User Selected", null, null, null, null, null, oldestDate);
		UUID elementId = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "element id", null, null, null, null, null, oldestDate);
		UUID lastUpdated = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "last updated", null, null, null, null, null, oldestDate);
		UUID versionId = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "version id", null, null, null, null, null, oldestDate);
		UUID represents = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "represents", null, null, null, null, null, oldestDate);
		UUID system = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, SYSTEM, null, null, null, null, null, oldestDate);
		UUID rank = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "rank", null, null, null, null, null, oldestDate);
		UUID periodStart = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "period start", null, null, null, null, null, oldestDate);
		UUID periodEnd = dwh.makeOtherTypeConcept(transaction, columnNameGroupConcept, null, "period end", null, null, null, null, null, oldestDate);

		dwh.makeAttributeTypeConcept(transaction, null, URI, "Uniform Resource Identifier Reference", null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, represents, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(2, elementId, DynamicDataType.STRING, null, false)}, null, null, oldestDate);

		//For whatever silly reason, sometimes in FHIR they define an ID as a complex object with a value an another ID....
		dwh.makeAttributeTypeConcept(transaction, null, ID, null, null, "The logical id of the resource, as used in the URL for the resource. Once assigned, "
				+ "this value never changes.", new DynamicColumnInfo[] {
						new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
						new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)}, null, null, oldestDate);
		
		UUID url = dwh.makeAttributeTypeConcept(transaction, null, URL, "Uniform Resource Locator", null, true, null, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, LANGUAGE, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, CODESYSTEM_STATUS, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, PUBLISHER, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)}, null, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, CONTACT, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)},
		null, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, VERSION, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, PUBLICATION_STATUS, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, EXTENSION, null, null, null, new DynamicColumnInfo[] {
						new DynamicColumnInfo(0, url, DynamicDataType.STRING, null, true), 
						new DynamicColumnInfo(1, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.POLYMORPHIC, null, false),
						new DynamicColumnInfo(2, elementId, DynamicDataType.STRING, null, false)},
				null, null, oldestDate);
		
		dwh.makeAttributeTypeConcept(transaction, null, PROFILE, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true), 
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)},
		null, null, oldestDate);
		
		dwh.makeRefsetTypeConcept(transaction, null, EXPERIMENTAL, null, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, COPYRIGHT, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)},
		null, null, oldestDate);
		
		dwh.makeRefsetTypeConcept(transaction, null, IMMUTABLE, null, null, oldestDate);
		dwh.makeRefsetTypeConcept(transaction, null, CASE_SENSITIVE, null, null, oldestDate);
		dwh.makeAttributeTypeConcept(transaction, null, IMPLICIT_RULES, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)},
		null, null, oldestDate);
		
		dwh.makeAttributeTypeConcept(transaction, null, JURISDICTION, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)}, null, null, oldestDate);
		
		//Same format as Jurisdiction - both are 'codable concept' objects
		dwh.makeAttributeTypeConcept(transaction, null, TYPE, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)}, null, null, oldestDate);
		
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, oldestDate, Coordinates.Filter.DevelopmentLatest());
		
		UUID titleDesc = dwh.makeDescriptionTypeConcept(transaction, null, TITLE, null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, oldestDate);
		dwh.makeDescriptionEnNoDialect(titleDesc, "A short, descriptive, user-friendly title for the code system", 
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, oldestDate);
		
		dwh.makeDescriptionTypeConcept(transaction, null, DESIGNIATION, null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, oldestDate);
		dwh.makeDescriptionTypeConcept(transaction, null, COMMENTS, null, "codesystem-concept-comments", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, oldestDate);
		
		UUID use = dwh.makeAttributeTypeConcept(transaction, null, CODING, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, represents, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(2, version, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(3, MetaData.CODE____SOLOR.getPrimordialUuid(), DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(4, display, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(5, userSelected, DynamicDataType.BOOLEAN, null, false)},
		null, null, oldestDate);

		dwh.makeAttributeTypeConcept(transaction, null, IDENTIFIER, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, use, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(2, elementId, DynamicDataType.STRING, null, false)}, null, null, oldestDate);

		dwh.makeAttributeTypeConcept(transaction, null, PURPOSE, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)},
		null, null, oldestDate);
		
		dwh.makeAttributeTypeConcept(transaction, null, META, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, versionId, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, lastUpdated, DynamicDataType.LONG, null, false),
				new DynamicColumnInfo(2, elementId, DynamicDataType.STRING, null, false)},
		null, null, oldestDate);
		
		dwh.makeAttributeTypeConcept(transaction, null, TELECOM, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
				new DynamicColumnInfo(1, system, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(2, use, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(3, rank, DynamicDataType.INTEGER, null, false),
				new DynamicColumnInfo(4, periodStart, DynamicDataType.LONG, null, false),
				new DynamicColumnInfo(5, periodEnd, DynamicDataType.LONG, null, false),
				new DynamicColumnInfo(6, elementId, DynamicDataType.STRING, null, false)},
		null, null, oldestDate);
		
		dwh.makeRefsetTypeConcept(transaction, null, VERSION_NEEDED, null, null, oldestDate);
		//TODO see if I can get rid of all of these elementIds
		dwh.makeAttributeTypeConcept(transaction, null, HIERARCHY_MEANING, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
				new DynamicColumnInfo(1, elementId, DynamicDataType.STRING, null, false)},
		null, null, oldestDate);
		
		for (Entry<String, CodeSystemProperty> csp : findUniqueProperties(fr).entrySet())
		{
			//These are properties that are defined within the individual code systems, but usually, they are using an external (common) definition, so 
			//we load them all as global metadata to prevent duplicate issues
			DynamicDataType type = null;
			switch (csp.getValue().getType().getValue())
			{
				case BOOLEAN:
					type = DynamicDataType.BOOLEAN;
					break;
				case CODE:
					//putting these in as strings for now, they are mostly externally defined.
					type = DynamicDataType.STRING;
					break;
				case CODING:
					type = DynamicDataType.UUID;
					break;
				case STRING:
					type = DynamicDataType.STRING;
					break;
				case DATE_TIME:
					type = DynamicDataType.LONG;
					break;
				case DECIMAL:
					type = DynamicDataType.DOUBLE;
					break;
				case INTEGER:
					type = DynamicDataType.INTEGER;
					break;
				default :
					throw new RuntimeException("Missing case");
			}
			
			UUID attribute = dwh.makeAttributeTypeConcept(transaction, null, csp.getValue().getCode().getValue(), null, null,
					csp.getValue().getDescription() != null ? csp.getValue().getDescription().getValue() : null, false, type, null, oldestDate);
			if (csp.getValue().getDescription() != null && StringUtils.isNotBlank(csp.getValue().getDescription().getValue()))
			{
				dwh.makeDescriptionEnNoDialect(attribute, csp.getValue().getDescription().getValue(), MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
						Status.ACTIVE, oldestDate);
			}
			//Handle stashed descriptions that were different
			for (Extension e : csp.getValue().getExtension())
			{
				dwh.makeDescriptionEnNoDialect(attribute, e.getValueString().getValue(), MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
						Status.ACTIVE, oldestDate);
			}
		}
		
		// Every time concept created add membership to "All FHIR Concepts"
		dwh.makeRefsetTypeConcept(transaction, null, ALL_FHIR_REFSET, null, null, oldestDate);
		
		// Create root concept under SOLOR_CONCEPT____SOLOR
		fhirRootConcept = dwh.makeConceptEnNoDialect(transaction, null, "FHIR Code Systems", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(),
				new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}, Status.ACTIVE, oldestDate);
				
		log.info("Shared metadata load stats");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}
		
		dwh.clearLoadStats();
		
		statusUpdates.accept("Loading content");
		
		for (Bundle b : fr.bundles)
		{
			if (b.getType().getValue() != BundleTypeList.COLLECTION)
			{
				log.warn("Bundle type {} is not supported", b.getType().getValue());
				continue;
			}
			log.info("Processing bundle {}", b.getId() != null ? b.getId().getValue() : "No Identifier in bundle");
			for (BundleEntry be : b.getEntry())
			{
				if (be.getResource() != null)
				{
					if (be.getResource().getValueSet() != null)
					{
						processValueSet(be.getResource().getValueSet());
					}
					if (be.getResource().getCodeSystem() != null)
					{
						processCodeSystem(be.getResource().getCodeSystem());
					}
				}
			}
		}
		
		for (CodeSystem cs : fr.codeSystems)
		{
			processCodeSystem(cs);
		}
		
		for (ValueSet vs : fr.valueSets)
		{
			processValueSet(vs);
		}

		dwh.processTaxonomyUpdates();
		Get.taxonomyService().notifyTaxonomyListenersToRefresh();
		
		log.info("Load Statistics");

		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}

		// this could be removed from final release. Just added to help debug editor problems.
		if (outputDirectory != null)
		{
			log.info("Dumping UUID Debug File");
			converterUUID.dump(outputDirectory, "fhirUuid");
		}
		converterUUID.clearCache();
		if (translationSanityCheck.get() != 0)
		{
			log.error("Tranlation handling error - should be 0: {}", translationSanityCheck.get());
		}
	}

	private void processValueSet(ValueSet vs)
	{
		log.info("Processing Value Set {}", vs.getName().getValue());
		
		final Optional<String> id = Optional.ofNullable(vs.getId()).map(i -> i.getValue());
		
		long lastUpdatedDate = 0;

		if (vs.getMeta() != null)
		{
			if (vs.getMeta().getLastUpdated() != null)
			{
				lastUpdatedDate = vs.getMeta().getLastUpdated().getValue().toGregorianCalendar().getTimeInMillis();
			}
		}
		
		if (lastUpdatedDate == 0)
		{
			lastUpdatedDate = oldestDate;
		}
		
		Optional<String> uri = getURIFromIdentifier(vs.getIdentifier());
		final Optional<String> version = Optional.ofNullable(vs.getVersion()).map(i -> i.getValue());
		final Optional<String> name = Optional.ofNullable(vs.getName()).map(i -> i.getValue());
		final Optional<String> title = Optional.ofNullable(vs.getTitle()).map(i -> i.getValue());
		if (title.isPresent() && (vs.getTitle().getExtension().size() > 0 || StringUtils.isNotBlank(vs.getTitle().getId())))
		{
			log.warn("Unhandled title attribute");
		}
		final Optional<PublicationStatusList> status = Optional.ofNullable(vs.getStatus()).map(i -> i.getValue());
		
		long valueSetDate = Optional.ofNullable(vs.getDate()).map(i -> Instant.from(timeParser.parse(i.getValue())).toEpochMilli()).orElse(lastUpdatedDate);
		if (lastUpdatedDate > valueSetDate)
		{
			valueSetDate = lastUpdatedDate;
		}
		long valueSetDateFinal = valueSetDate;
		
		//Reset the module for each item being processed.  Names aren't unique, so use id as the FQN / UUID generation basis
		setupModule(id.get(), name, version,  MetaData.FHIR_MODULES____SOLOR.getPrimordialUuid(), uri, valueSetDate);
		
		//normally, we leave the UUID generator set up with the parent module namespace, and only change the module here to represent a version of a terminology.
		//However, in the case of fhir, we need codesystem and valueset specific UUID generation, so I need to reset the namespace to something for this value set, 
		//that doesn't include the version.
		converterUUID.configureNamespace(converterUUID.createNamespaceUUIDFromString(UuidT5Generator.PATH_ID_FROM_FS_DESC, "valueSet:" + id.get()));

		
		//Build up a concept to represent this valueset type
		UUID valueSetConcept = converterUUID.createNamespaceUUIDFromString(id.get());
		
		//FHIR spec files have some duplicate refset names.  Normally, we don't allow these in a loader, hence the error this would produce.
		//Ignore the issue for the FHIR loader, as we don't need this map to get the refset name.
		dwh.removeRefsetTypeMapping(name.get());
		if (title.isPresent())
		{
			dwh.removeRefsetTypeMapping(title.get());
		}
		dwh.makeRefsetTypeConcept(transaction, valueSetConcept, name.get(), (title.isPresent() ? title.get() : null), null, valueSetDate);
		
		handleMeta(valueSetConcept, valueSetDate, vs.getMeta());
		handleIdentifiers(valueSetConcept, valueSetDate, vs.getIdentifier());
		
		if (title.isPresent())
		{
			dwh.makeDescriptionEnNoDialect(valueSetConcept, title.get(), dwh.getDescriptionType(TITLE), Status.ACTIVE, valueSetDate);
		}

		if (status.isPresent() && status.get() == PublicationStatusList.RETIRED)
		{
			throw new RuntimeException("Don't yet handle inactive refset: " + id);
		}
		if (status.isPresent())
		{
			dwh.makeStringAnnotation(dwh.getAttributeType(PUBLICATION_STATUS), valueSetConcept, status.get().name(), valueSetDate);
		}
		
		if (version.isPresent())
		{
			dwh.makeStringAnnotation(dwh.getAttributeType(VERSION), valueSetConcept, version.get(), valueSetDate);
		}
		
		final Optional<String> language = Optional.ofNullable(vs.getLanguage()).map(i -> i.getValue());
		if (language.isPresent() && !language.get().equals("en"))
		{
			log.warn("Non-english language code systems not properly handled yet.");
		}
		
		Optional.ofNullable(vs.getPublisher()).ifPresent(publisher -> 
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(PUBLISHER), valueSetConcept, new DynamicData[] {
					new DynamicStringImpl(publisher.getValue()),
					(StringUtils.isBlank(publisher.getId()) ? null : new DynamicStringImpl(publisher.getId()))}, valueSetDateFinal);
			for (Extension e : publisher.getExtension())
			{
				handleExtension(made, e, valueSetDateFinal);
			}
		});
		for (ContactDetail cd : vs.getContact())
		{
			if (cd.getName() != null && StringUtils.isNotBlank(cd.getName().getValue()))
			{
				UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(CONTACT), valueSetConcept, new DynamicData[] {
						new DynamicStringImpl(cd.getName().getValue()),
						(StringUtils.isBlank(cd.getId()) ? null : new DynamicStringImpl(cd.getId()))}, valueSetDate);
				handleTelecom(made, valueSetDate, cd.getTelecom());
				for (Extension e : cd.getExtension())
				{
					handleExtension(made, e, valueSetDate);
				}
			}
		}
		
		Optional.ofNullable(vs.getUrl()).ifPresent(url -> 
		{
			UUID made = dwh.makeBrittleStringAnnotation(dwh.getAttributeType(URL), valueSetConcept, url.getValue(), valueSetDateFinal);
			if (StringUtils.isNotBlank(url.getId()))
			{
				log.warn("Unhandled url attribute");
			}
			for (Extension e : url.getExtension())
			{
				handleExtension(made, e, valueSetDateFinal);
			}
		});
		
		if (vs.getExperimental() != null && vs.getExperimental().isValue().booleanValue())
		{
			dwh.makeDynamicRefsetMember(dwh.getRefsetType(EXPERIMENTAL), valueSetConcept, valueSetDate);
		}
		
		Optional.ofNullable(vs.getDescription()).map(i -> i.getValue()).ifPresent(description -> 
		{
			dwh.makeDescriptionEnNoDialect(valueSetConcept, description, MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, valueSetDateFinal);
		});
		
		for (Extension extension : vs.getExtension())
		{
			handleExtension(valueSetConcept, extension, valueSetDate);
		}
		
		if (vs.getCompose() != null)
		{
			//TODO 
			log.error("Compose not yet handled in valueset: {}", id);
		}
		//contained are pre-processed in the fhir reader
		// vs.getContained()
		Optional.ofNullable(vs.getCopyright()).ifPresent(copyright ->
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(COPYRIGHT), valueSetConcept, new DynamicData[] {
					new DynamicStringImpl(copyright.getValue()),
					(StringUtils.isBlank(copyright.getId()) ? null : new DynamicStringImpl(copyright.getId()))}, valueSetDateFinal);
			for (Extension e : copyright.getExtension())
			{
				handleExtension(made, e, valueSetDateFinal);
			}
		});
		if (vs.getExpansion() != null)
		{
			log.info("Value Set Expansions are ignored by this loader: {}", id);
		}
		if (vs.getImmutable() != null && vs.getImmutable().isValue().booleanValue())
		{
			dwh.makeDynamicRefsetMember(dwh.getRefsetType(IMMUTABLE), valueSetConcept, valueSetDate);
		}
		
		Optional.ofNullable(vs.getImplicitRules()).ifPresent(implicitRules ->
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(IMPLICIT_RULES), valueSetConcept, new DynamicData[] {
					new DynamicStringImpl(implicitRules.getValue()),
					(StringUtils.isBlank(implicitRules.getId()) ? null : new DynamicStringImpl(implicitRules.getId()))}, valueSetDateFinal);
			for (Extension e : implicitRules.getExtension())
			{
				handleExtension(made, e, valueSetDateFinal);
			}
		});
		if (vs.getJurisdiction().size() > 0)
		{
			for (CodeableConcept j : vs.getJurisdiction())
			{
				handleCodeableConcept(valueSetConcept, valueSetDate, j, JURISDICTION);
			}
		}
		for (Extension ex : vs.getModifierExtension())
		{
			handleExtension(valueSetConcept, ex, valueSetDate);
		}
		if (vs.getPurpose() != null && StringUtils.isNotBlank(vs.getPurpose().getValue()))
		{
			DynamicData[] dataNested = new DynamicData[] {
					new DynamicStringImpl(vs.getPurpose().getValue()),
					StringUtils.isNotBlank(vs.getPurpose().getId()) ? new DynamicStringImpl(vs.getPurpose().getId()) : null};
			UUID purpose = dwh.makeDynamicSemantic(dwh.getAttributeType(PURPOSE), valueSetConcept, dataNested, valueSetDate);
			
			for (Extension e : vs.getPurpose().getExtension())
			{
				handleExtension(purpose, e, valueSetDate);
			}
		}
		if (vs.getUseContext().size() > 0)
		{
			//TODO 
			log.error("Use Context not yet handled in valueset: {}", id);
		}
	}

	private void handleCodeableConcept(UUID attachTo, long date, CodeableConcept cc, String attributeTypeConstant)
	{
		if (cc == null)
		{
			return;
		}
		DynamicData[] dataNested = new DynamicData[] {
				StringUtils.isNotBlank(cc.getText() == null ? null : cc.getText().getValue()) ? new DynamicStringImpl(cc.getText().getValue()) : null, 
						StringUtils.isNotBlank(cc.getId()) ? new DynamicStringImpl(cc.getId()) : null};
		UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(attributeTypeConstant), attachTo, dataNested, date);

		for (Coding c : cc.getCoding())
		{
			handleCoding(made, "coding", c, date);
		}
		for (Extension e : cc.getExtension())
		{
			handleExtension(made, e, date);
		}
	}

	private void handleIdentifiers(UUID forItem, long date, List<Identifier> identifier)
	{
		for (Identifier i : identifier)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(IDENTIFIER), forItem, 
					new DynamicData[] {new DynamicStringImpl(i.getValue().getValue()),
							((i.getUse() == null || i.getUse().getValue() == null) ? null : new DynamicStringImpl(i.getUse().getValue().name())),
							(StringUtils.isBlank(i.getId()) ? null : new DynamicStringImpl(i.getId()))}, date);
			for (Extension e : i.getExtension())
			{
				handleExtension(made, e, date);
			}
			
			//Sanity checks for stupidity in the data model
			if (i.getUse() != null && (i.getUse().getExtension().size() > 0 || StringUtils.isNotBlank(i.getUse().getId())))
			{
				log.error("Unhandled identifier use aspect");
			}
			
			if (i.getValue().getExtension().size() > 0 || StringUtils.isNotBlank(i.getValue().getId()))
			{
				log.error("Unhandled identifier value aspect");
			}
			
			handleCodeableConcept(made, date, i.getType(), TYPE);
			handleUri(made, SYSTEM, date, i.getSystem());
			if (i.getPeriod() != null)
			{
				log.error("Unhandled period in identifier");
			}
			if (i.getAssigner() != null)
			{
				log.error("Unhandled assigner in identifier");
			}
		}
	}
	
	private void handleUri(UUID forItem, String purposeLabel, long date, Uri uri)
	{
		if (uri == null)
		{
			return;
		}
		UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(URI), forItem, new DynamicData[] {
				new DynamicStringImpl(purposeLabel), 
				new DynamicStringImpl(uri.getValue()), 
				StringUtils.isNotBlank(uri.getId()) ? new DynamicStringImpl(uri.getId()) : null}, date);

		for (Extension e : uri.getExtension())
		{
			handleExtension(made, e, date);
		}
	}
	
	private void handleCanonical(UUID forItem, String purposeLabel, long date, Canonical uri)
	{
		if (uri == null)
		{
			return;
		}
		UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(URI), forItem, new DynamicData[] {
				new DynamicStringImpl(purposeLabel), 
				new DynamicStringImpl(uri.getValue()), 
				StringUtils.isNotBlank(uri.getId()) ? new DynamicStringImpl(uri.getId()) : null}, date);

		for (Extension e : uri.getExtension())
		{
			handleExtension(made, e, date);
		}
	}
	

	private void processCodeSystem(CodeSystem cs)
	{
		log.info("Processing Code System {}", cs.getName() == null ? cs.getUrl().getValue() : cs.getName().getValue());
		final Optional<CodeSystemContentModeList> content = Optional.ofNullable(cs.getContent()).map(i -> i.getValue());
		if (content.isPresent() && content.get() != CodeSystemContentModeList.COMPLETE)
		{
			log.warn("Only support COMPLETE marked content at this time, not '{}'.  Will skip.", content.get().name());
			return;
		}

		final Optional<String> name = Optional.ofNullable(cs.getName()).map(i -> i.getValue());
		final Optional<String> id = Optional.ofNullable(cs.getId()).map(i -> i.getValue());
		final Optional<String> version = Optional.ofNullable(cs.getVersion()).map(i -> i.getValue());
		final Optional<String> date = Optional.ofNullable(cs.getDate()).map(i -> i.getValue());
		if (Optional.ofNullable(cs.getUrl()).map(i -> i.getValue()).orElse("").equals("http://hl7.org/fhir/CodeSystem/example"))
		{
			log.info("Skipping example codesystem");
			return;
		}
		
		long codeSystemDate = date.map(i -> Instant.from(timeParser.parse(i)).toEpochMilli()).orElse(oldestDate);
		
		Optional<String> uri = getURIFromIdentifier(cs.getIdentifier());

		//Reset the module for each item being processed.  Names aren't unique, so use id as the FQN / UUID generation basis
		setupModule(id.get(), name, version.isPresent() ? version : date,  MetaData.FHIR_MODULES____SOLOR.getPrimordialUuid(), uri, codeSystemDate);
		
		//normally, we leave the UUID generator set up with the parent module namespace, and only change the module here to represent a version of a terminology.
		//However, in the case of fhir, we need codesystem and valueset specific UUID generation, so I need to reset the namespace to something for this code system, 
		//that doesn't include the version.
		converterUUID.configureNamespace(converterUUID.createNamespaceUUIDFromString(UuidT5Generator.PATH_ID_FROM_FS_DESC, "codeSystem:" + id.get()));
		
		//Properties are handled in initial setup

		final Optional<String> language = Optional.ofNullable(cs.getLanguage()).map(i -> i.getValue());
		if (language.isPresent() && !language.get().equals("en"))
		{
			log.warn("Non-english language code systems not properly handled yet.");
		}
		
		final Optional<String> title = Optional.ofNullable(cs.getTitle()).map(i -> i.getValue());
		if (title.isPresent() && (cs.getTitle().getExtension().size() > 0 || StringUtils.isNotBlank(cs.getTitle().getId())))
		{
			log.warn("Unhandled title attribute");
		}
		final Optional<PublicationStatusList> status = Optional.ofNullable(cs.getStatus()).map(i -> i.getValue());
		
		//Build up a concept to represent the root of the code system.
		UUID codeSystemConcept = dwh.makeConceptEnNoDialect(transaction, null, name.orElse(id.get()), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), new UUID[] {fhirRootConcept},
				status.isPresent() ? (status.get().ordinal() == PublicationStatusList.RETIRED.ordinal() ? Status.INACTIVE : Status.ACTIVE) : Status.ACTIVE, codeSystemDate);
		
		for (ContactDetail cd : cs.getContact())
		{
			if (cd.getName() != null && StringUtils.isNotBlank(cd.getName().getValue()))
			{
				UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(CONTACT), codeSystemConcept, new DynamicData[] {
						new DynamicStringImpl(cd.getName().getValue()),
						(StringUtils.isBlank(cd.getId()) ? null : new DynamicStringImpl(cd.getId()))}, codeSystemDate);
				handleTelecom(made, codeSystemDate, cd.getTelecom());
				for (Extension e : cd.getExtension())
				{
					handleExtension(made, e, codeSystemDate);
				}
			}
		}
		
		if (title.isPresent())
		{
			dwh.makeDescriptionEnNoDialect(codeSystemConcept, title.get(), dwh.getDescriptionType(TITLE), Status.ACTIVE, codeSystemDate);
		}
		
		if (id.isPresent())
		{
			dwh.makeBrittleStringAnnotation(dwh.getAttributeType(ID), codeSystemConcept, id.get(), codeSystemDate);
			
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(ID), codeSystemConcept, new DynamicData[] {
					new DynamicStringImpl(cs.getId().getValue()), 
					StringUtils.isNotBlank(cs.getId().getId()) ? new DynamicStringImpl(cs.getId().getId()) : null}, codeSystemDate);
			for (Extension e : cs.getId().getExtension())
			{
				handleExtension(made, e, codeSystemDate);
			}
		}
		handleMeta(codeSystemConcept, codeSystemDate, cs.getMeta());
		handleIdentifiers(codeSystemConcept, codeSystemDate, cs.getIdentifier());
		
		if (version.isPresent())
		{
			dwh.makeStringAnnotation(dwh.getAttributeType(VERSION), codeSystemConcept, version.get(), codeSystemDate);
		}
		if (status.isPresent())
		{
			dwh.makeStringAnnotation(dwh.getAttributeType(PUBLICATION_STATUS), codeSystemConcept, status.get().name(), codeSystemDate);
		}
		
		for (Extension extension : cs.getExtension())
		{
			handleExtension(codeSystemConcept, extension, codeSystemDate);
		}
		
		//A map of each concept in this code system, to its list of parents, with each one also keeping a date to use when constructing the taxonomy. 
		HashMap<UUID, Pair<HashSet<UUID>, AtomicLong>> taxonomyInfo = new HashMap<>();
		
		for (CodeSystemConcept csc : cs.getConcept())
		{
			buildConcept(codeSystemConcept, csc, codeSystemDate, taxonomyInfo, uri);
		}
		
		for(Entry<UUID, Pair<HashSet<UUID>, AtomicLong>> taxonomy : taxonomyInfo.entrySet())
		{
			//Don't use the concept status on the taxonomy
			dwh.makeParentGraph(transaction, taxonomy.getKey(), taxonomy.getValue().getKey(), Status.ACTIVE, taxonomy.getValue().getValue().get());
		}
		
		if (cs.getCaseSensitive() != null && cs.getCaseSensitive().isValue())
		{
			dwh.makeDynamicRefsetMember(dwh.getRefsetType(CASE_SENSITIVE), codeSystemConcept, codeSystemDate);
		}
		if (cs.getCompositional() != null)
		{
			log.error("compositional not yet handled");
		}

		//contained are pre-processed in the FHIR reader
		//cs.getContained();
		cs.getContent();
		Optional.ofNullable(cs.getCopyright()).ifPresent(copyright ->
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(COPYRIGHT), codeSystemConcept, new DynamicData[] {
					new DynamicStringImpl(copyright.getValue()),
					(StringUtils.isBlank(copyright.getId()) ? null : new DynamicStringImpl(copyright.getId()))}, codeSystemDate);
			for (Extension e : copyright.getExtension())
			{
				handleExtension(made, e, codeSystemDate);
			}
		});
		//cs.getCount();
		
		Optional.ofNullable(cs.getDescription()).map(i -> i.getValue()).ifPresent(description -> 
		{
			dwh.makeDescriptionEnNoDialect(codeSystemConcept, description, MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, codeSystemDate);
		});
		if (cs.getExperimental() != null && cs.getExperimental().isValue().booleanValue())
		{
			dwh.makeDynamicRefsetMember(dwh.getRefsetType(EXPERIMENTAL), codeSystemConcept, codeSystemDate);
		}
		if (cs.getFilter().size() > 0)
		{
			log.error("Code system filter not yet supported");
		}
		if (cs.getHierarchyMeaning() != null)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(HIERARCHY_MEANING), codeSystemConcept, new DynamicData[] {
					new DynamicStringImpl(cs.getHierarchyMeaning().getValue().name()),
					(StringUtils.isBlank(cs.getHierarchyMeaning().getId()) ? null : new DynamicStringImpl(cs.getHierarchyMeaning().getId()))}, codeSystemDate);
			for (Extension e : cs.getHierarchyMeaning().getExtension())
			{
				handleExtension(made, e, codeSystemDate);
			}
		}
		Optional.ofNullable(cs.getImplicitRules()).ifPresent(implicitRules ->
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(IMPLICIT_RULES), codeSystemConcept, new DynamicData[] {
					new DynamicStringImpl(implicitRules.getValue()),
					(StringUtils.isBlank(implicitRules.getId()) ? null : new DynamicStringImpl(implicitRules.getId()))}, codeSystemDate);
			for (Extension e : implicitRules.getExtension())
			{
				handleExtension(made, e, codeSystemDate);
			}
		});
		if (cs.getJurisdiction().size() > 0)
		{
			for (CodeableConcept j : cs.getJurisdiction())
			{
				handleCodeableConcept(codeSystemConcept, codeSystemDate, j, JURISDICTION);
			}
		}

		for (Extension e : cs.getModifierExtension())
		{
			handleExtension(codeSystemConcept, e, codeSystemDate);
		}
		
		Optional.ofNullable(cs.getPublisher()).ifPresent(publisher -> 
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(PUBLISHER), codeSystemConcept, new DynamicData[] {
					new DynamicStringImpl(publisher.getValue()),
					(StringUtils.isBlank(publisher.getId()) ? null : new DynamicStringImpl(publisher.getId()))}, codeSystemDate);
			for (Extension e : publisher.getExtension())
			{
				handleExtension(made, e, codeSystemDate);
			}
		});
		cs.getPurpose();
		if (cs.getPurpose() != null && StringUtils.isNotBlank(cs.getPurpose().getValue()))
		{
			DynamicData[] dataNested = new DynamicData[] {
					new DynamicStringImpl(cs.getPurpose().getValue()),
					StringUtils.isNotBlank(cs.getPurpose().getId()) ? new DynamicStringImpl(cs.getPurpose().getId()) : null};
			UUID purpose = dwh.makeDynamicSemantic(dwh.getAttributeType(PURPOSE), codeSystemConcept, dataNested, codeSystemDate);
			
			for (Extension e : cs.getPurpose().getExtension())
			{
				handleExtension(purpose, e, codeSystemDate);
			}
		}
		//TODO no idea if we ever would need to do anything with the narrative, here or on the valuset
		//cs.getText();
		Optional.ofNullable(cs.getUrl()).ifPresent(url -> 
		{
			UUID made = dwh.makeBrittleStringAnnotation(dwh.getAttributeType(URL), codeSystemConcept, url.getValue(), codeSystemDate);
			if (StringUtils.isNotBlank(url.getId()))
			{
				log.warn("Unhandled url attribute");
			}
			for (Extension e : url.getExtension())
			{
				handleExtension(made, e, codeSystemDate);
			}
		});
		if (cs.getUseContext().size() > 0)
		{
			//TODO 
			log.error("Use Context not yet handled in code system: {}", id);
		}
		if (cs.getValueSet() != null)
		{
			handleCanonical(codeSystemConcept, "canonical", codeSystemDate, cs.getValueSet());
		}
		if (cs.getVersionNeeded() != null && cs.getVersionNeeded().isValue().booleanValue())
		{
			dwh.makeDynamicRefsetMember(dwh.getRefsetType(IMMUTABLE), codeSystemConcept, codeSystemDate);
		}
		
		log.info("Processed {} concepts", cs.getConcept().size());
	}
	
	private void handleTelecom(UUID attachTo, long date, List<ContactPoint> telecom)
	{
		for (ContactPoint cp : telecom)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(TELECOM), attachTo, new DynamicData[] {
					new DynamicStringImpl(cp.getValue().getValue()), 
					(cp.getSystem() == null ? null : new DynamicStringImpl(cp.getSystem().getValue().name())), 
					(cp.getUse() == null ? null : new DynamicStringImpl(cp.getUse().getValue().name())),
					(cp.getRank() == null ? null : new DynamicIntegerImpl(cp.getRank().getValue().intValue())),
					((cp.getPeriod() == null || cp.getPeriod().getStart() == null) ? null : 
						new DynamicLongImpl(Instant.from(timeParser.parse(cp.getPeriod().getStart().getValue())).toEpochMilli())),
					((cp.getPeriod() == null || cp.getPeriod().getEnd() == null) ? null : 
						new DynamicLongImpl(Instant.from(timeParser.parse(cp.getPeriod().getEnd().getValue())).toEpochMilli())),
					StringUtils.isBlank(cp.getId()) ? null : new DynamicStringImpl(cp.getId())}, date);

			for (Extension e : cp.getExtension())
			{
				handleExtension(made, e, date);
			}
			
			if (cp.getValue().getExtension().size() > 0 || StringUtils.isNotBlank(cp.getValue().getId()))
			{
				log.error("Unhandled telecom value attribute");
			}
			if (cp.getSystem() != null && (cp.getSystem().getExtension().size() > 0 || StringUtils.isNotBlank(cp.getSystem().getId())))
			{
				log.error("Unhandled telecom system attribute");
			}
			if (cp.getUse() != null && (cp.getUse().getExtension().size() > 0 || StringUtils.isNotBlank(cp.getUse().getId())))
			{
				log.error("Unhandled telecom use attribute");
			}
			if (cp.getRank() != null && (cp.getRank().getExtension().size() > 0 || StringUtils.isNotBlank(cp.getRank().getId())))
			{
				log.error("Unhandled telecom rank attribute");
			}
			if (cp.getPeriod() != null && cp.getPeriod().getStart() != null 
					&& (cp.getPeriod().getStart().getExtension().size() > 0 || StringUtils.isNotBlank(cp.getPeriod().getStart().getId())))
			{
				log.error("Unhandled telecom start attribute");
			}
			if (cp.getPeriod() != null && cp.getPeriod().getEnd() != null 
					&& (cp.getPeriod().getEnd().getExtension().size() > 0 || StringUtils.isNotBlank(cp.getPeriod().getEnd().getId())))
			{
				log.error("Unhandled telecom end attribute");
			}
		}
	}

	private void handleMeta(UUID attachTo, long date, Meta meta)
	{
		if (meta == null)
		{
			return;
		}
		Long lastUpdated = null;
		if (meta.getLastUpdated() != null)
		{
			lastUpdated = meta.getLastUpdated().getValue().toGregorianCalendar().getTimeInMillis();
		}
		
		UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(META), attachTo, new DynamicData[] {
				(meta.getVersionId() == null ? null : new DynamicStringImpl(meta.getVersionId().getValue())), 
				(lastUpdated == null ? null : new DynamicLongImpl(lastUpdated)), 
				StringUtils.isBlank(meta.getId()) ? null : new DynamicStringImpl(meta.getId())}, date);

		for (Extension e : meta.getExtension())
		{
			handleExtension(made, e, date);
		}
		
		if (meta.getSource() != null)
		{
			handleUri(made, "source", date, meta.getSource());
		}
		
		if (meta.getVersionId() != null && (meta.getVersionId().getExtension().size() > 0 || StringUtils.isNotBlank(meta.getVersionId().getId())))
		{
			log.error("unhandled parts of meta");
		}
		
		for (Canonical p : meta.getProfile())
		{
			dwh.makeDynamicSemantic(dwh.getAttributeType(PROFILE), attachTo, new DynamicData[] {
					new DynamicStringImpl(p.getValue()),
					(StringUtils.isBlank(p.getId()) ? null : new DynamicStringImpl(p.getId()))}, date);
		}
		for (Coding c : meta.getSecurity())
		{
			handleCoding(made, "security", c, date);
		}
		
		for (Coding c : meta.getTag())
		{
			handleCoding(made, "tag", c, date);
		}
	}

	private Optional<String> getURIFromIdentifier(List<Identifier> identifiers)
	{
		Optional<String> uri = Optional.empty();
		for (Identifier identifier : identifiers)
		{
			Optional<String> identifierType = Optional.ofNullable(identifier.getSystem()).map(i -> i.getValue());
			Optional<String> identifierValue = Optional.ofNullable(identifier.getValue()).map(i -> i.getValue());
			
			if (identifierType.isPresent())
			{
				if (identifierType.get().equals("urn:ietf:rfc:3986"))  //Constant for URI, usually contains an OID
				{
					if (uri.isPresent() && !uri.get().equals(identifierValue.orElse(uri.get())))
					{
						log.error("Multiple URI identifiers not currently handled!");
					}
					uri = identifierValue;
				}
				else if (identifierType.get().toLowerCase().startsWith("http"))
				{
					String temp = identifierType.get() + (identifierType.get().endsWith("/") ? "" : "/") + identifierValue.get();
					if (uri.isPresent() && !uri.get().equals(temp))
					{
						log.error("Multiple URI identifiers not currently handled!");
					}
					uri = Optional.of(temp);
				}
				else
				{
					log.error("Unknown identifier type {}", identifierType.get());
				}
			}
			else
			{
				log.error("No identifier type specified!");
			}
		}
		return uri;
	}

	private Optional<UUID> getConceptUUID(String codeSystemURI, String conceptCode)
	{
		//We could compute these, rather than do a hashed lookup, but this gives me a sanity check that we don't have missing refs
		return Optional.ofNullable(uriCodeToUUIDMap.get(codeSystemURI + ":" + conceptCode));
	}

	private void buildConcept(UUID parent, CodeSystemConcept csc, long date, HashMap<UUID, Pair<HashSet<UUID>, AtomicLong>> taxonomyInfo, Optional<String> codeSystemURI)
	{
		String code = csc.getCode().getValue();
		AtomicInteger descCount = new AtomicInteger();
		
		UUID conceptUUID = converterUUID.createNamespaceUUIDFromString(code, true);
		if (codeSystemURI.isPresent())
		{
			uriCodeToUUIDMap.put(codeSystemURI.get() + ":" + code, conceptUUID);
		}
		
		Status status = findStatus(csc);
		dwh.makeConcept(conceptUUID, status, date);
		
		dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), conceptUUID, code, date);
		
		Optional.ofNullable(csc.getDefinition()).map(i -> i.getValue()).ifPresent(definition -> 
		{
			dwh.makeDescriptionEnNoDialect(conceptUUID, definition, MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, date);
		});
		Optional.ofNullable(csc.getDisplay()).map(i -> i.getValue()).ifPresent(display -> 
		{
			descCount.getAndIncrement();
			dwh.makeDescriptionEnNoDialect(conceptUUID, display, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, date);
		});
		Optional.ofNullable(csc.getId()).ifPresent(id -> 
		{
			dwh.makeDynamicSemantic(dwh.getAttributeType(ID), conceptUUID, new DynamicData[] {new DynamicStringImpl(id), null}, date);
		});
		
		for (CodeSystemDesignation csd : csc.getDesignation())
		{
			UUID designation;
			if (csd.getLanguage() == null ||  StringUtils.isBlank(csd.getLanguage().getValue()))
			{
				descCount.getAndIncrement();
				designation = dwh.makeDescriptionEnNoDialect(conceptUUID, csd.getValue().getValue(), dwh.getDescriptionType(DESIGNIATION), Status.ACTIVE, date);
			}
			else
			{
				//There exists designations in some FHIR files which are NOT unique, and only differ by content which is nested on them, like "use"
				//For example:
			//          <designation>
			//            <language value="nl"/>
			//            <use>
			//              <system value="http://terminology.hl7.org/CodeSystem/designation-usage"/>
			//              <code value="display"/>
			//            </use>
			//            <value value="Man"/>
			//          </designation>
			//          <designation>
			//            <language value="nl"/>
			//            <use>
			//              <system value="http://terminology.hl7.org/CodeSystem/designation-usage"/>
			//              <code value="definition"/>
			//            </use>
			//            <value value="Man"/>
			//          </designation>
				//For garbage cases like this, we need to calculate our UUID with some information from the nested use...
				String addOn = "";
				if (csd.getUse() != null && csd.getUse().getCode() != null && StringUtils.isNotBlank(csd.getUse().getCode().getValue()))
				{
					addOn = ":USE:" + csd.getUse().getCode().getValue();
				}
				
				//Same code as the default, but with a bit of USE information added into the text, if it is provided.
				UUID uuidForDescription = UuidFactory.getUuidForDescriptionSemantic(converterUUID.getNamespace(), conceptUUID, MetaData.NOT_APPLICABLE____SOLOR.getPrimordialUuid(), 
						dwh.getDescriptionType(DESIGNIATION), LanguageMap.getConceptForLanguageCode(LanguageCode.getLangCode(csd.getLanguage().getValue())).getPrimordialUuid(),
						csd.getValue().getValue() + addOn, ((input, uuid) -> converterUUID.addMapping(input, uuid)));
				
				descCount.getAndIncrement();
				designation = dwh.makeDescription(uuidForDescription, conceptUUID, csd.getValue().getValue(), dwh.getDescriptionType(DESIGNIATION), 
						LanguageMap.getConceptForLanguageCode(LanguageCode.getLangCode(csd.getLanguage().getValue())).getPrimordialUuid(),
						MetaData.NOT_APPLICABLE____SOLOR.getPrimordialUuid(), Status.ACTIVE, date, null, null);
			}
			
			if (csd.getUse() != null)
			{
				handleCoding(designation, USE, csd.getUse(), date);
			}
		}
		
		for (Extension ex : csc.getExtension())
		{
			handleExtension(conceptUUID, ex, date);
		}
		
		for (Extension ex : csc.getModifierExtension())
		{
			handleExtension(conceptUUID, ex, date);
		}
		
		for (CodeSystemProperty1 property :csc.getProperty())
		{
			handleProperty(conceptUUID, property, date);
		}
		
		for (CodeSystemConcept nested : csc.getConcept())
		{
			buildConcept(conceptUUID, nested, date, taxonomyInfo, codeSystemURI);
		}
		
		collectParents(csc, conceptUUID, date, parent, taxonomyInfo);
		
		dwh.makeDynamicRefsetMember(dwh.getRefsetType(ALL_FHIR_REFSET), conceptUUID, date);
		
		if (descCount.get() == 0)
		{
			//Some fhir code systems have no description (how bizarre) use the code as a description for these.
			dwh.makeDescriptionEnNoDialect(conceptUUID, code, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, date);
		}
	}
	
	private void handleProperty(UUID forItem, CodeSystemProperty1 property, long time)
	{
		DynamicData data = null;
		if (property.getValueBoolean() != null)
		{
			data = new DynamicBooleanImpl(property.getValueBoolean().isValue().booleanValue());
		}
		else if (property.getValueString() != null)
		{
			data = new DynamicStringImpl(property.getValueString().getValue());
		}
		else if (property.getValueCode() != null)
		{
			//handling this as a string for now.
			data = new DynamicStringImpl(property.getValueCode().getValue());
		}
		else if (property.getValueCoding() != null)
		{
			throw new RuntimeException("Not yet supported");
		}
		else if (property.getValueInteger() != null)
		{
			data = new DynamicIntegerImpl(property.getValueInteger().getValue().intValue());
		}
		else if (property.getValueDecimal() != null)
		{
			data = new DynamicDoubleImpl(Double.valueOf(property.getValueDecimal().getValue()));
		}
		else if (property.getValueDateTime() != null)
		{
			data = new DynamicLongImpl(Instant.from(timeParser.parse(property.getValueDateTime().getValue())).toEpochMilli());
		}
		else
		{
			throw new RuntimeException("Unsupported property");
		}
		
		if (data != null)
		{
			dwh.makeDynamicSemantic(dwh.getAttributeType(property.getCode().getValue()), forItem, data, time);
		}
	}


	private Status findStatus(CodeSystemConcept csc)
	{
		for (CodeSystemProperty1 property :csc.getProperty())
		{
			if (property.getCode().getValue().equals("status"))
			{
				if ("retired".equals(property.getValueCode().getValue()))
				{
					return Status.INACTIVE;
				}
			}
		}
		return Status.ACTIVE;
	}
	
	private void collectParents(CodeSystemConcept csc, UUID conceptBeingProcessed, long dateForConceptBeingProcessed, UUID passedParent, 
			HashMap<UUID, Pair<HashSet<UUID>, AtomicLong>> taxonomyInfo)
	{
		if (passedParent != null)
		{
			Pair<HashSet<UUID>, AtomicLong> parentInfo = taxonomyInfo.computeIfAbsent(conceptBeingProcessed, 
					(uuidAgain) -> new Pair<HashSet<UUID>, AtomicLong>(new HashSet<UUID>(), new AtomicLong(0)));
			parentInfo.getKey().add(passedParent);
			if (parentInfo.getValue().get() < dateForConceptBeingProcessed)
			{
				parentInfo.getValue().set(dateForConceptBeingProcessed);
			}
		}
		for (CodeSystemProperty1 property :csc.getProperty())
		{
			if (property.getCode().getValue().equals("child"))
			{
				UUID child = converterUUID.createNamespaceUUIDFromString(property.getValueCode().getValue(), true);
				Pair<HashSet<UUID>, AtomicLong> parentInfo = taxonomyInfo.computeIfAbsent(child, 
						(uuidAgain) -> new Pair<HashSet<UUID>, AtomicLong>(new HashSet<UUID>(), new AtomicLong(0)));
				parentInfo.getKey().add(conceptBeingProcessed);
				if (parentInfo.getValue().get() < dateForConceptBeingProcessed)
				{
					parentInfo.getValue().set(dateForConceptBeingProcessed);
				}
			}
		}
	}

	private void handleExtension(UUID forItem, Extension extension, long time)
	{
		DynamicStringImpl url = new DynamicStringImpl(extension.getUrl());
		
		if ("http://hl7.org/fhir/StructureDefinition/translation".equals(extension.getUrl()))
		{
			//We handle these differently
			translationSanityCheck.getAndIncrement();
			return;
		}
		
		if (extension.getValueString() != null)
		{
			if ("http://hl7.org/fhir/StructureDefinition/codesystem-concept-comments".equals(url.getDataString())) 
			{
				//Most likely, one of these stupid formats:
				//<concept>
				//  <extension url="http://hl7.org/fhir/StructureDefinition/codesystem-concept-comments">
				//    <valueString value="Retained for backwards compatibility only as of v2.6 and CDA R 2. Preferred value is text/xml.">
				//      <extension url="http://hl7.org/fhir/StructureDefinition/translation">
				//        <extension url="lang">
				//          <valueCode value="nl"/>
				//        </extension>
				//        <extension url="content">
				//          <valueString value="Alleen voor backward compatibiliteit vanaf v2.6 en CDAr2. Voorkeurswaarde is text/xml."/>
				//        </extension>
				//      </extension>
				//    </valueString>
				
				//<concept>
				//  <extension url="http://hl7.org/fhir/StructureDefinition/codesystem-concept-comments">
				//    <valueString>
				//      <extension url="http://hl7.org/fhir/StructureDefinition/translation">
				//        <extension url="lang">
				//          <valueString value="nl"/>
				//        </extension>
				//        <extension url="content">
				//          <valueString value="Zo spoedig mogelijk"/>
				//        </extension>
				//      </extension>
				//    </valueString>
				
				
				if (StringUtils.isNotBlank(extension.getValueString().getId()))
				{
					log.warn("Unhandled ID on a comment");
				}
				
				//This seems to be optional....
				if (StringUtils.isNotBlank(extension.getValueString().getValue()))
				{
					UUID made = dwh.makeDescriptionEnNoDialect(forItem, extension.getValueString().getValue(), dwh.getDescriptionType(COMMENTS), Status.ACTIVE, time);
					for (Extension e : extension.getValueString().getExtension())
					{
						//This will skip any that match the translation pattern, just below.
						handleExtension(made, e, time);
					}
				}
				
				for (Extension nestedE : extension.getValueString().getExtension())
				{
					if ("http://hl7.org/fhir/StructureDefinition/translation".equals(nestedE.getUrl()))
					{
						translationSanityCheck.getAndDecrement();
						String lang = null;
						String content = null;
						for (Extension e : nestedE.getExtension())
						{
							if ("lang".equals(e.getUrl()))
							{
								if (e.getValueString() != null)
								{
									lang = e.getValueString().getValue();
								}
								else if (e.getValueCode() != null)
								{
									lang = e.getValueCode().getValue();
								}
								else
								{
									throw new RuntimeException("unhandled translation format");
								}
								
							}
							else if ("content".equals(e.getUrl()))
							{
								content = e.getValueString().getValue();
							}
							else
							{
								throw new RuntimeException("unhandled translation format");
							}
						}
						if (lang != null && content != null)
						{
							try
							{
								dwh.makeDescription(forItem, content, dwh.getDescriptionType(COMMENTS), 
										LanguageMap.getConceptForLanguageCode(LanguageCode.getLangCode(lang)).getPrimordialUuid(), 
										MetaData.NOT_APPLICABLE____SOLOR.getPrimordialUuid(), Status.ACTIVE, time, null, null);
							}
							catch (RuntimeException e1)
							{
								if (e1.getMessage().startsWith("Just made a duplicate UUID"))
								{
									log.debug("Igoring duplicate comment translation: '{}'", content);
								}
								else
								{
									throw e1;
								}
							}
						}
						else
						{
							throw new RuntimeException("unhandled translation format");
						}
					}
				}
			}
			else
			{
				//Not a comment - just a string...
				UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(EXTENSION), forItem, 
						new DynamicData[] {url, new DynamicStringImpl(extension.getValueString().getValue()),
								(StringUtils.isBlank(extension.getValueString().getId()) ? null : new DynamicStringImpl(extension.getValueString().getId()))}, time);
				for (Extension e : extension.getValueString().getExtension())
				{
					handleExtension(made, e, time);
				}
			}
		}
		//Putting these in as strings for now.
		else if (extension.getValueCode() != null)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(EXTENSION), forItem, 
					new DynamicData[] {url, new DynamicStringImpl(extension.getValueCode().getValue()),
							(StringUtils.isBlank(extension.getValueCode().getId()) ? null : new DynamicStringImpl(extension.getValueCode().getId()))}, time);
			for (Extension e : extension.getValueCode().getExtension())
			{
				handleExtension(made, e, time);
			}
		}
		else if (extension.getValueInteger() != null)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(EXTENSION), forItem,
					new DynamicData[] {url, new DynamicIntegerImpl(extension.getValueInteger().getValue()),
							(StringUtils.isBlank(extension.getValueInteger().getId()) ? null : new DynamicStringImpl(extension.getValueInteger().getId()))}, time);
			for (Extension e : extension.getValueInteger().getExtension())
			{
				handleExtension(made, e, time);
			}
		}
		else if (extension.getValueCoding() != null)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(EXTENSION), forItem,
					new DynamicData[] {url, null, null}, time);
			handleCoding(made, "coding", extension.getValueCoding(), time);
			for (Extension e : extension.getValueCoding().getExtension())
			{
				handleExtension(made, e, time);
			}
		}
		else if (extension.getValueBoolean() != null)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(EXTENSION), forItem, 
					new DynamicData[] {url, new DynamicBooleanImpl(extension.getValueBoolean().isValue()),
							(StringUtils.isBlank(extension.getValueBoolean().getId()) ? null : new DynamicStringImpl(extension.getValueBoolean().getId()))}, time);
			for (Extension e : extension.getValueBoolean().getExtension())
			{
				handleExtension(made, e, time);
			}
		}
		else if (extension.getValueCanonical() != null)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(EXTENSION), forItem, 
					new DynamicData[] {url, new DynamicStringImpl(extension.getValueCanonical().getValue()),
							(StringUtils.isBlank(extension.getValueCanonical().getId()) ? null : new DynamicStringImpl(extension.getValueCanonical().getId()))}, time);
			for (Extension e : extension.getValueCanonical().getExtension())
			{
				handleExtension(made, e, time);
			}
		}
		else if (extension.getValueMarkdown() != null)
		{
			UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(EXTENSION), forItem, 
					new DynamicData[] {url, new DynamicStringImpl(extension.getValueMarkdown().getValue()),
							(StringUtils.isBlank(extension.getValueMarkdown().getId()) ? null : new DynamicStringImpl(extension.getValueMarkdown().getId()))}, time);
			for (Extension e : extension.getValueMarkdown().getExtension())
			{
				handleExtension(made, e, time);
			}
		}
		else
		{
			throw new RuntimeException("Extension type not yet handled");
		}
	}
	
	private void handleCoding(UUID forItem, String purpose, Coding c, long time)
	{
		DynamicData[] dataNested = new DynamicData[] {
				new DynamicStringImpl(purpose),
				c.getId() != null ? new DynamicStringImpl(c.getId()) : null,
				c.getVersion() != null ? new DynamicStringImpl(c.getVersion().getValue()) : null,
				c.getCode() != null ? new DynamicStringImpl(c.getCode().getValue()) : null,
				c.getDisplay() != null ? new DynamicStringImpl(c.getDisplay().getValue()) : null,
				c.getUserSelected() != null ? new DynamicBooleanImpl(c.getUserSelected().isValue()) : null,
		};
		UUID coding = dwh.makeDynamicSemantic(dwh.getAttributeType(CODING), forItem, dataNested, time);
		for (Extension e : c.getExtension())
		{
			handleExtension(coding, e, time);
		}
		
		handleUri(coding, SYSTEM, time, c.getSystem());
		
		if (c.getVersion() != null && (StringUtils.isNotBlank(c.getVersion().getId()) || c.getVersion().getExtension().size() > 0))
		{
			log.error("Unhandled coding aspect on version");
		}
		if (c.getCode() != null && (StringUtils.isNotBlank(c.getCode().getId()) || c.getCode().getExtension().size() > 0))
		{
			log.error("Unhandled coding aspect on code");
		}
		if (c.getDisplay() != null && (StringUtils.isNotBlank(c.getDisplay().getId()) || c.getDisplay().getExtension().size() > 0))
		{
			log.error("Unhandled coding aspect on display");
		}
		if (c.getUserSelected() != null && (StringUtils.isNotBlank(c.getUserSelected().getId()) || c.getUserSelected().getExtension().size() > 0))
		{
			log.error("Unhandled coding aspect on userSelected");
		}
	}

	private void actionOnAll(FHIRReader fr, Consumer<DomainResource> consumer)
	{
		for (CodeSystem cs : fr.codeSystems)
		{
			consumer.accept(cs);
		}
		
		for (ValueSet vs : fr.valueSets)
		{
			consumer.accept(vs);
		}
		
		for (Bundle b : fr.bundles)
		{
			for (BundleEntry be : b.getEntry())
			{
				if (be.getResource().getValueSet() != null)
				{
					consumer.accept(be.getResource().getValueSet());
				}
				if (be.getResource().getCodeSystem() != null)
				{
					consumer.accept(be.getResource().getCodeSystem());
				}
			}
		}
	}
	
	private void findOldestDate(FHIRReader fr)
	{
		actionOnAll(fr, dr ->
		{
			if (dr instanceof CodeSystem)
			{
				if (((CodeSystem) dr).getDate() != null)
				{
					long temp = Instant.from(timeParser.parse(((CodeSystem) dr).getDate().getValue())).toEpochMilli();
					if (temp < oldestDate)
					{
						oldestDate = temp;
					}
				}
			}
			else if (dr instanceof ValueSet)
			{
				if (((ValueSet) dr).getDate() != null)
				{
					long temp = Instant.from(timeParser.parse(((ValueSet) dr).getDate().getValue())).toEpochMilli();
					if (temp < oldestDate)
					{
						oldestDate = temp;
					}
				}
			}
			else
			{
				//should be impossible, actionOnAll only gives us the two types
				throw new RuntimeException("Ignoring unsupported DomainResource " + dr.getClass());
			}
		});
	}
	
	private HashMap<String, CodeSystemProperty> findUniqueProperties(FHIRReader fr)
	{
		final HashMap<String, CodeSystemProperty> uniqueProperties = new HashMap<>();
		actionOnAll(fr, dr ->
		{
			if (dr instanceof CodeSystem)
			{
				for (CodeSystemProperty property : ((CodeSystem) dr).getProperty())
				{
					CodeSystemProperty found = uniqueProperties.get(property.getCode().getValue());
					if (found != null)
					{
						mergeProperties(found, property);
						//log errors with properties...
						propertiesEquivalent(found, property);
					}
					else
					{
						uniqueProperties.put(property.getCode().getValue(), property);
					}
				}
			}
		});
		log.info("Read {} unique property types", uniqueProperties.size());
		return uniqueProperties;
	}
	private boolean propertiesEquivalent(CodeSystemProperty left, CodeSystemProperty right)
	{
		if (left.getType().getValue() != right.getType().getValue())
		{
			log.error("Property definition consistency error for code '{}' in the type.  Second value will be ignored. 1: '{}' 2: '{}'", 
					left.getCode().getValue(), left.getType().getValue(), right.getType().getValue());
			return false;
		}
		
		if (!Objects.equals(left.getUri() == null ? null : left.getUri().getValue(), right.getUri() == null ? null : right.getUri().getValue()) 
				&& !"http://.........?".equals(right.getUri() == null ? "" : right.getUri().getValue()))
		{
			log.error("Property definition consistency error for code '{}' in the uri.  Second value will be ignored. 1: '{}' 2: '{}'", 
					left.getCode().getValue(), left.getUri().getValue(), right.getUri().getValue());
			return false;
		}
		return true;
	}

	/**
	 * Note that this stashes "extra" descriptions in as a extension
	 * @param mergeInto
	 * @param mergeFrom
	 */
	private void mergeProperties(CodeSystemProperty mergeInto, CodeSystemProperty mergeFrom)
	{
		if (mergeInto.getDescription() == null || mergeInto.getDescription().getValue() == null)
		{
			mergeInto.setDescription(mergeFrom.getDescription());
		}
		if (mergeInto.getUri() == null || mergeInto.getUri().getValue() == null || mergeInto.getUri().getValue().equals("http://.........?"))
		{
			mergeInto.setUri(mergeFrom.getUri());
		}
		
		//See if they have differing descriptions, if so, keep both.
		if (mergeInto.getDescription() != null && StringUtils.isNotBlank(mergeInto.getDescription().getValue()) 
				&& mergeFrom.getDescription() != null && StringUtils.isNotBlank(mergeFrom.getDescription().getValue()) 
				&& !Objects.equals(mergeInto.getDescription().getValue(), mergeFrom.getDescription().getValue()))
		{
			//See if we have stuffed any descriptions aside already...
			boolean found = false;
			for (Extension e : mergeInto.getExtension())
			{
				if (e.getValueString().getValue().equals(mergeFrom.getDescription().getValue()))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				Extension e = new Extension();
				fhir.String s = new fhir.String();
				s.setValue(mergeFrom.getDescription().getValue());
				e.setValueString(s);
				mergeInto.getExtension().add(e);
			}
		}
	}
}
