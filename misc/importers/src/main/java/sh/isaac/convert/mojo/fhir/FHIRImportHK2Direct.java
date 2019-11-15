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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import fhir.Bundle;
import fhir.BundleEntry;
import fhir.BundleTypeList;
import fhir.CodeSystem;
import fhir.CodeSystemConcept;
import fhir.CodeSystemContentModeList;
import fhir.CodeSystemDesignation;
import fhir.CodeSystemProperty;
import fhir.CodeSystemProperty1;
import fhir.ContactDetail;
import fhir.DomainResource;
import fhir.Extension;
import fhir.Identifier;
import fhir.PublicationStatusList;
import fhir.ValueSet;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCode;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicBooleanImpl;
import sh.isaac.model.semantic.types.DynamicDoubleImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.utility.LanguageMap;

/**
 * {@link FHIRImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class FHIRImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private int conceptCount = 0;
	
	private DateTimeFormatter timeParser = new DateTimeFormatterBuilder().appendPattern("yyyy[-MM[-dd['T'HH:mm:ss[.SSS]xxx]]]")
			.parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
			.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
			.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
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
	private static final String VERSION = "version";
	private static final String CODE_SYSTEM_STATUS = "code system status";
	private static final String ALL_FHIR_REFSET = "All FHIR Concepts";
	
	private HashSet<UUID> createdConcepts = new HashSet<>();
	private HashSet<UUID> referencedParents = new HashSet<>();
	
	private UUID columnNameGroupConcept;
	
	/**
	 * This constructor is for maven and HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	protected FHIRImportHK2Direct()
	{
		
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
	 * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent(Consumer, BiConsumer)}
	 * If this was constructed via the constructor that takes parameters, you do not need to call this.
	 * 
	 * @see sh.isaac.convert.directUtils.DirectConverter#configure(File, Path, String, StampCoordinate)
	 */
	@Override
	public void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate)
	{
		this.outputDirectory = outputDirectory;
		this.inputFileLocationPath = inputFolder;
		this.converterSourceArtifactVersion = converterSourceArtifactVersion;
		this.converterUUID = new ConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, false);
		this.readbackCoordinate = stampCoordinate == null ? StampCoordinates.getDevelopmentLatest() : stampCoordinate;
	}
	
	@Override
	public SupportedConverterTypes[] getSupportedTypes()
	{
		return new SupportedConverterTypes[] {SupportedConverterTypes.MVX};
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer)
	 * @see DirectConverter#convertContent(Consumer, BiConsumer)
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException 
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
		dwh.makeMetadataHierarchy(true, true, false, false, true, false, oldestDate);
		
		dwh.makeAttributeTypeConcept(null, URI, "Uniform Resource Identifier Reference", null, true, null, null, oldestDate);
		dwh.makeAttributeTypeConcept(null, ID, null, null, "The logical id of the resource, as used in the URL for the resource. Once assigned, "
				+ "this value never changes.", true, null, null, oldestDate);
		UUID url = dwh.makeAttributeTypeConcept(null, URL, "Uniform Resource Locator", null, true, null, null, oldestDate);
		dwh.makeAttributeTypeConcept(null, LANGUAGE, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(null, CODESYSTEM_STATUS, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(null, PUBLISHER, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(null, CONTACT, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(null, VERSION, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(null, CODE_SYSTEM_STATUS, null, null, false, DynamicDataType.STRING, null, oldestDate);
		dwh.makeAttributeTypeConcept(null, EXTENSION, null, null, null, new DynamicColumnInfo[] {
						new DynamicColumnInfo(0, url, DynamicDataType.STRING, null, true), 
						new DynamicColumnInfo(1, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.POLYMORPHIC, null, false)},
				null, null, oldestDate);
		
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, oldestDate, StampCoordinates.getDevelopmentLatest());
		
		UUID titleDesc = dwh.makeDescriptionTypeConcept(null, TITLE, null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, oldestDate);
		dwh.makeDescriptionEnNoDialect(titleDesc, "A short, descriptive, user-friendly title for the code system", 
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, oldestDate);
		
		dwh.makeDescriptionTypeConcept(null, DESIGNIATION, null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, oldestDate);
		
		columnNameGroupConcept = dwh.makeOtherMetadataRootNode("Complex Attribute Column Names", oldestDate);
		UUID id = dwh.makeOtherTypeConcept(columnNameGroupConcept, null, "Id", null, null, null, null, null, oldestDate);
		UUID system = dwh.makeOtherTypeConcept(columnNameGroupConcept, null, "System", null, null, null, null, null, oldestDate);
		UUID version = dwh.makeOtherTypeConcept(columnNameGroupConcept, null, "Version", null, null, null, null, null, oldestDate);
		UUID display = dwh.makeOtherTypeConcept(columnNameGroupConcept, null, "Display", null, null, null, null, null, oldestDate);
		UUID userSelected = dwh.makeOtherTypeConcept(columnNameGroupConcept, null, "User Selected", null, null, null, null, null, oldestDate);
		//Use is actually defined as a type of "Coding", but is usually named use. May need to rename this, or add a column, if we determine we need to use 
		//it in cases where it isnt' referred to as a 'use'.
		dwh.makeAttributeTypeConcept(null, USE, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, id, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, system, DynamicDataType.STRING, null, false), 
				new DynamicColumnInfo(2, version, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(3, MetaData.CODE____SOLOR.getPrimordialUuid(), DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(4, display, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(5, userSelected, DynamicDataType.BOOLEAN, null, false)},
		null, null, oldestDate);
		
		dwh.makeAttributeTypeConcept(null, CODING, null, null, null, new DynamicColumnInfo[] {
				new DynamicColumnInfo(0, id, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(1, system, DynamicDataType.STRING, null, false), 
				new DynamicColumnInfo(2, version, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(3, MetaData.CODE____SOLOR.getPrimordialUuid(), DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(4, display, DynamicDataType.STRING, null, false),
				new DynamicColumnInfo(5, userSelected, DynamicDataType.BOOLEAN, null, false)},
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
			
			UUID attribute = dwh.makeAttributeTypeConcept(null, csp.getValue().getCode().getValue(), null, null, 
					csp.getValue().getDescription() != null ? csp.getValue().getDescription().getValue() : null, false, type, null, oldestDate);
			if (csp.getValue().getDescription() != null && StringUtils.isNotBlank(csp.getValue().getDescription().getValue()))
			{
				dwh.makeDescriptionEnNoDialect(attribute, csp.getValue().getDescription().getValue(), MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
						Status.ACTIVE, oldestDate);
			}
		}
		
		// Every time concept created add membership to "All FHIR Concepts"
		dwh.makeRefsetTypeConcept(null, ALL_FHIR_REFSET, null, null, oldestDate);
		
		// Create root concept under SOLOR_CONCEPT____SOLOR
		fhirRootConcept = dwh.makeConceptEnNoDialect(null, "FHIR Code Systems", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
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
		
		log.info("Processed " + conceptCount + " concepts");
		statusUpdates.accept("Processed " + conceptCount + " concepts");
		
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
	}

	private void processValueSet(ValueSet vs)
	{
		log.info("Processing Value Set {}", vs.getName().getValue());
		//Reset the module for each item being processed
		//setupModule(termName, Optional.of(foo),  MetaData.FHIR_MODULES____SOLOR.getPrimordialUuid(), Optional.empty(uri), date.getTime());

		
	}

	private void processCodeSystem(CodeSystem cs)
	{
		log.info("Processing Code System {}", cs.getName().getValue());
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
		final Optional<String> url = Optional.ofNullable(cs.getUrl()).map(i -> i.getValue());
		if (url.orElse("").equals("http://hl7.org/fhir/CodeSystem/example"))
		{
			log.info("Skipping example codesystem");
			return;
		}
		
		long codeSystemDate = date.map(i -> Instant.from(timeParser.parse(i)).toEpochMilli()).orElse(oldestDate);
		
		Optional<String> uri = Optional.empty();
		for (Identifier identifier : cs.getIdentifier())
		{
			Optional<String> identifierType = Optional.ofNullable(identifier.getSystem()).map(i -> i.getValue());
			Optional<String> identifierValue = Optional.ofNullable(identifier.getValue()).map(i -> i.getValue());
			
			if (identifierType.isPresent())
			{
				if (identifierType.get().equals("urn:ietf:rfc:3986"))  //Constant for URI, usually contains an OID
				{
					if (uri.isPresent() && uri.get().equals(identifierValue.orElse("")))
					{
						log.error("Multiple URI identifiers not currently handled!");
					}
					uri = identifierValue;
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

		//Reset the module for each item being processed.  Names aren't unique, so use id as the FQN / UUID generation basis
		setupModule(id.get(), name, version.isPresent() ? version : date,  MetaData.FHIR_MODULES____SOLOR.getPrimordialUuid(), uri, codeSystemDate);
		
		//normally, we leave the UUID generator set up with the parent module namespace, and only change the module here to represent a version of a terminology.
		//However, in the case of fhir, we need codesystem and valueset specific UUID generation, so I need to reset the namespace to something for this code system, 
		//that doesn't include the version.
		converterUUID.configureNamespace(converterUUID.createNamespaceUUIDFromString(UuidT5Generator.PATH_ID_FROM_FS_DESC, id.get()));
		
		//Properties are handled in initial setup

		final Optional<String> language = Optional.ofNullable(cs.getLanguage()).map(i -> i.getValue());
		if (language.isPresent() && !language.get().equals("en"))
		{
			log.warn("Non-english language code systems not properly handled yet.");
		}
		
		final Optional<String> title = Optional.ofNullable(cs.getTitle()).map(i -> i.getValue());
		final Optional<PublicationStatusList> status = Optional.ofNullable(cs.getStatus()).map(i -> i.getValue());
		final Optional<String> publisher = Optional.ofNullable(cs.getPublisher()).map(i -> i.getValue());
		ArrayList<String> contacts = new ArrayList<>();
		for (ContactDetail cd : cs.getContact())
		{
			if (cd.getName() != null && StringUtils.isNotBlank(cd.getName().getValue()))
			{
				contacts.add(cd.getName().getValue());
			}
		}
		
		//Build up a concept to represent the root of the code system.
		UUID codeSystemConcept = dwh.makeConceptEnNoDialect(null, name.get(), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), new UUID[] {fhirRootConcept}, 
				status.isPresent() ? (status.get() == PublicationStatusList.RETIRED ? Status.INACTIVE : Status.ACTIVE) : Status.ACTIVE, codeSystemDate);
		
		if (title.isPresent())
		{
			dwh.makeDescriptionEnNoDialect(codeSystemConcept, title.get(), dwh.getDescriptionType(TITLE), Status.ACTIVE, codeSystemDate);
		}
		
		if (id.isPresent())
		{
			dwh.makeBrittleStringAnnotation(dwh.getAttributeType(ID), codeSystemConcept, id.get(), codeSystemDate);
		}
		if (url.isPresent())
		{
			dwh.makeBrittleStringAnnotation(dwh.getAttributeType(URL), codeSystemConcept, url.get(), codeSystemDate);
		}
		if (uri.isPresent())
		{
			dwh.makeBrittleStringAnnotation(dwh.getAttributeType(URI), codeSystemConcept, uri.get(), codeSystemDate);
		}
		if (version.isPresent())
		{
			dwh.makeStringAnnotation(dwh.getAttributeType(VERSION), codeSystemConcept, version.get(), codeSystemDate);
		}
		if (status.isPresent())
		{
			dwh.makeStringAnnotation(dwh.getAttributeType(CODE_SYSTEM_STATUS), codeSystemConcept, status.get().name(), codeSystemDate);
		}
		if (publisher.isPresent())
		{
			dwh.makeStringAnnotation(dwh.getAttributeType(PUBLISHER), codeSystemConcept, publisher.get(), codeSystemDate);
		}
		for (String s : contacts)
		{
			dwh.makeStringAnnotation(dwh.getAttributeType(CONTACT), codeSystemConcept, s, codeSystemDate);
		}
		
		for (Extension extension : cs.getExtension())
		{
			handleExtension(codeSystemConcept, extension, codeSystemDate);
		}
		
		for (CodeSystemConcept csc : cs.getConcept())
		{
			buildConcept(codeSystemConcept, csc, codeSystemDate);
		}
	}

	private void buildConcept(UUID parent, CodeSystemConcept csc, long date)
	{
		String code = csc.getCode().getValue();
		
		UUID conceptUUID = converterUUID.createNamespaceUUIDFromString(code, true);
		createdConcepts.add(conceptUUID);
		
		Status status = findStatus(csc);
		dwh.makeConcept(conceptUUID, status, date);
		
		dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), conceptUUID, code, date);
		
		Optional.ofNullable(csc.getDefinition()).map(i -> i.getValue()).ifPresent(definition -> 
		{
			dwh.makeDescriptionEnNoDialect(conceptUUID, definition, MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), status, date);
		});
		Optional.ofNullable(csc.getDisplay()).map(i -> i.getValue()).ifPresent(display -> 
		{
			dwh.makeDescriptionEnNoDialect(conceptUUID, display, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), status, date);
		});
		Optional.ofNullable(csc.getId()).ifPresent(id -> 
		{
			dwh.makeBrittleStringAnnotation(dwh.getAttributeType(ID), conceptUUID, id, date);
		});
		
		for (CodeSystemDesignation csd : csc.getDesignation())
		{
			UUID designation;
			if (csd.getLanguage() == null ||  StringUtils.isBlank(csd.getLanguage().getValue()))
			{
				designation = dwh.makeDescriptionEnNoDialect(conceptUUID, csd.getValue().getValue(), dwh.getDescriptionType(DESIGNIATION), status, date);
			}
			else
			{
				designation = dwh.makeDescription(conceptUUID, csd.getValue().getValue(), dwh.getDescriptionType(DESIGNIATION), 
						LanguageMap.getConceptForLanguageCode(LanguageCode.getLangCode(csd.getLanguage().getValue())).getPrimordialUuid(),
						MetaData.NOT_APPLICABLE____SOLOR.getPrimordialUuid(), status, date, null, null);
			}
			
			if (csd.getUse() != null)
			{
				UUID use = dwh.makeDynamicSemantic(dwh.getAttributeType(USE), designation, new DynamicData[] {
						csd.getUse().getId() != null ? new DynamicStringImpl(csd.getUse().getId()) : null,
						csd.getUse().getSystem() != null ? new DynamicStringImpl(csd.getUse().getSystem().getValue()) : null,
						csd.getUse().getVersion() != null ? new DynamicStringImpl(csd.getUse().getVersion().getValue()) : null,
						csd.getUse().getCode() != null ? new DynamicStringImpl(csd.getUse().getCode().getValue()) : null,
						csd.getUse().getDisplay() != null ? new DynamicStringImpl(csd.getUse().getDisplay().getValue()) : null,
						csd.getUse().getUserSelected() != null ? new DynamicBooleanImpl(csd.getUse().getUserSelected().isValue()) : null,
				}, date);
				if (csd.getUse().getCode() != null && csd.getUse().getCode().getExtension() != null)
				{
					for (Extension extension : csd.getUse().getCode().getExtension())
					{
						handleExtension(use, extension, date);
					}
				}
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
			buildConcept(conceptUUID, nested, date);
		}
		
		ArrayList<UUID> parents = findParents(csc);
		referencedParents.addAll(parents);
		
		if (parent != null)
		{
			parents.add(parent);
		}
		if (parents.size() > 0)
		{
			dwh.makeParentGraph(conceptUUID, parents, status, date);
		}
		
		dwh.makeDynamicRefsetMember(dwh.getRefsetType(ALL_FHIR_REFSET), conceptUUID, date);
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
			//data = new DynamicUUIDImpl(converterUUID.createNamespaceUUIDFromString(property.getValueCoding().g.getValue(), true));
			//TODO would need to properly calculate the UUID for a concept from any URI...
			log.error("Coding type not supported yet!");
		}
		else if (property.getValueInteger() != null)
		{
			data = new DynamicIntegerImpl(property.getValueInteger().getValue().intValue());
		}
		else if (property.getValueDecimal() != null)
		{
			data = new DynamicDoubleImpl(Double.valueOf(property.getValueDecimal().getValue()));
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
	
	private ArrayList<UUID> findParents(CodeSystemConcept csc)
	{
		ArrayList<UUID> parents = new ArrayList<>();
		for (CodeSystemProperty1 property :csc.getProperty())
		{
			if (property.getCode().getValue().equals("child"))
			{
				parents.add(converterUUID.createNamespaceUUIDFromString(property.getValueCode().getValue(), true));
			}
		}
		return parents;
	}

	private void handleExtension(UUID forItem, Extension extension, long time)
	{
		DynamicData[] data = new DynamicData[2];
		data[0] = new DynamicStringImpl(extension.getUrl());
		DynamicData[] dataNested = null;
		UUID assemablageNested = null;
		
		if (extension.getValueString() != null)
		{
			if (StringUtils.isNotBlank(extension.getValueString().getValue()))
			{
				data[1] = new DynamicStringImpl(extension.getValueString().getValue());
			}
			else if (extension.getValueString().getExtension() != null && extension.getValueString().getExtension().size() > 0) 
			{
				//This is some particularly stupid schema design we have to handle here....
				//Example content:
//				<valueString>
//	              <extension url="http://hl7.org/fhir/StructureDefinition/translation">
//	                <extension url="lang">
//	                  <valueString value="nl"/>
//	                </extension>
//	                <extension url="content">
//	                  <valueString value="Zo spoedig mogelijk"/>
//	                </extension>
//	              </extension>
//	            </valueString>
				
				
				
				
			}
		}
		//Putting these in as strings for now.
		else if (extension.getValueCode() != null)
		{
			data[1] = new DynamicStringImpl(extension.getValueCode().getValue());
		}
		else if (extension.getValueInteger() != null)
		{
			data[1] = new DynamicIntegerImpl(extension.getValueInteger().getValue());
		}
		else if (extension.getValueCoding() != null)
		{
			data[1] = null;
			assemablageNested = dwh.getAttributeType(CODING);
			dataNested = new DynamicData[] {
					extension.getValueCoding().getId() != null ? new DynamicStringImpl(extension.getValueCoding().getId()) : null,
					extension.getValueCoding().getSystem() != null ? new DynamicStringImpl(extension.getValueCoding().getSystem().getValue()) : null,
					extension.getValueCoding().getVersion() != null ? new DynamicStringImpl(extension.getValueCoding().getVersion().getValue()) : null,
					extension.getValueCoding().getCode() != null ? new DynamicStringImpl(extension.getValueCoding().getCode().getValue()) : null,
					extension.getValueCoding().getDisplay() != null ? new DynamicStringImpl(extension.getValueCoding().getDisplay().getValue()) : null,
					extension.getValueCoding().getUserSelected() != null ? new DynamicBooleanImpl(extension.getValueCoding().getUserSelected().isValue()) : null,
			};
		}
		else
		{
			throw new RuntimeException("Extension type not yet handled");
		}
		
		UUID made = dwh.makeDynamicSemantic(dwh.getAttributeType(EXTENSION), forItem, data, time);
		if (dataNested != null) {
			dwh.makeDynamicSemantic(assemablageNested, made, dataNested, time);
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
		if (!Objects.equals(left.getDescription() == null ? null : left.getDescription().getValue(), 
				right.getDescription() == null ? null : right.getDescription().getValue()))
		{
			log.error("Property definition consistency error for code '{}' in the description.  Second value will be ignored. 1: '{}' 2: '{}'", 
					left.getCode().getValue(), left.getDescription().getValue(), right.getDescription().getValue());
			return false;
		}
		return true;
	}

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
	}
}
