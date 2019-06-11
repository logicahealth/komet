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
package sh.isaac.convert.mojo.hl7v3;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.poi.util.CloseIgnoringInputStream;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import mifschema.CodeBasedContentDefinition;
import mifschema.CodeSystem;
import mifschema.CodeSystemVersion;
import mifschema.CombinedContentDefinition;
import mifschema.ComplexMarkupWithLanguage;
import mifschema.Concept;
import mifschema.ConceptCode;
import mifschema.ConceptDomain;
import mifschema.ConceptDomainProperty;
import mifschema.ConceptDomainPropertyKind;
import mifschema.ConceptDomainRef;
import mifschema.ConceptProperty;
import mifschema.ConceptPropertyTypeKind;
import mifschema.ConceptRelationship;
import mifschema.ConceptRelationshipKind;
import mifschema.ContentDefinition;
import mifschema.Flow;
import mifschema.GlobalVocabularyModel;
import mifschema.HistoryItem;
import mifschema.IncludeRelatedCodes;
import mifschema.Inline;
import mifschema.ObjectFactory;
import mifschema.PackageKind;
import mifschema.PrintName;
import mifschema.PropertyDefaultHandlingKind;
import mifschema.Reflexivity;
import mifschema.RelationshipTraversalKind;
import mifschema.SupportedConceptProperty;
import mifschema.SupportedConceptRelationship;
import mifschema.Symmetry;
import mifschema.Transitivity;
import mifschema.ValueSet;
import mifschema.ValueSetVersion;
import mifschema.VocabularyModel;
import mifschema.VocabularyValueSetRef;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
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
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * {@link HL7v3ImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class HL7v3ImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private UUID rootConceptUUID;

	private HashMap<String, String> codeSystemNameToOID = new HashMap<>();
	private HashMap<String, String> valueSetNameToOID = new HashMap<>();

	Marshaller complexMarkupMarshaller = null;
	Marshaller inlineMarshaller = null;
	Pattern oidMatcher;

	int valueSetMemberCount = 0;

	// We collect a set of pointers from code system OID -> a list of codes in that code system, to aid in computing value sets
	private HashMap<String, List<UUID>> codeUUIDPointers = new HashMap<>();
	private HashMap<String, List<Concept>> codeConceptPointers = new HashMap<>();

	// valueSetUUID-> nid members of the valueSet
	private HashMap<UUID, Set<Integer>> inProgressRefsetMembers = new HashMap<>();
	// valueSetUUIDs that are 'complete'
	private HashSet<UUID> fullyCalculatedRefsetMembers = new HashSet<>();

	// valueSetUUIDs -> cache of value sets to go back to finish calculating
	private HashMap<UUID, IncompleteValueSetData> incompleteRefsetData = new HashMap<>();

	private HashMap<String, String> inverseRelNameMap = new HashMap<>();
	
	/**
	 * This constructor is for maven and HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	protected HL7v3ImportHK2Direct()
	{
		//For HK2 and maven
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
	 * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent()}
	 * If this was constructed via the constructor that takes parameters, you do not need to call this.
	 * 
	 * @see sh.isaac.convert.directUtils.DirectConverter#configure(java.io.File, java.io.File, java.lang.String, sh.isaac.api.coordinate.StampCoordinate)
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
		return new SupportedConverterTypes[] {SupportedConverterTypes.HL7v3};
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer))
	 * @see DirectConverter#convertContent(Consumer, BiConsumer))
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdates) throws IOException 
	{
		try
		{
			complexMarkupMarshaller = JAXBContext.newInstance(ComplexMarkupWithLanguage.class).createMarshaller();
			complexMarkupMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			inlineMarshaller = JAXBContext.newInstance(Inline.class).createMarshaller();
			inlineMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			oidMatcher = Pattern.compile("(\\d+\\.)+\\d+");

			log.info("Reading data");
			GlobalVocabularyModel gvm = readXML(statusUpdates);

			if (PackageKind.VERSION != gvm.getPackageKind())
			{
				throw new MojoExecutionException("Expected 'packageKind' of " + PackageKind.VERSION.name());
			}

			String schemaVersion = gvm.getSchemaVersion();
			if (!"2.1.7".equals(schemaVersion) && !"2.1.6".equals(schemaVersion))  // I'm currently compiled against 2.1.6, have tested against 2.1.7.
			{
				throw new MojoExecutionException("Untested schema version: " + schemaVersion);
			}

			long contentTime = gvm.getHeader().getRenderingInformation().getRenderingTime().toGregorianCalendar().getTimeInMillis();
			log.debug("Default time from content: " + new Date(contentTime));
			
			statusUpdates.accept("Setting up metadata");
			
			//Right now, we are configured for the CPT grouping modules nid
			dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.HL7_V3_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, 
					"HL7v3", false);
			
			UUID versionModule = setupModule("HL7v3", MetaData.HL7_V3_MODULES____SOLOR.getPrimordialUuid(), 
					Optional.of("http://terminology.hl7.org/CodeSystem/v3-"), contentTime);
			
			//Set up our metadata hierarchy
			dwh.makeMetadataHierarchy(true, true, false, true, true, false, contentTime);

			//description types
			dwh.makeDescriptionTypeConcept(null, "name", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
			dwh.makeDescriptionTypeConcept(null, "code", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
			dwh.makeDescriptionTypeConcept(null, "print name", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
			dwh.makeDescriptionTypeConcept(null, "title", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
			dwh.makeDescriptionTypeConcept(null, "documentation", null, null, MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
			dwh.makeDescriptionTypeConcept(null, "description", null, null, MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, contentTime);
			
			//association types
			dwh.makeAssociationTypeConcept(null, "specializes domain", null, null, null, null, null, null, null, contentTime);
			dwh.makeAssociationTypeConcept(null, "specialized by domain", null, null, null, null, null, null, null, contentTime);
			
			//attributes
			makeAttributes(contentTime);
			
			makeTerminologySpecificAttributes(gvm, contentTime);

			log.info("Metadata load stats");
			for (String line : dwh.getLoadStats().getSummary())
			{
				log.info(line);
			}
			
			dwh.clearLoadStats();
			
			log.info("Loading content");
			statusUpdates.accept("Loading content");

			dwh.makeDynamicSemantic(dwh.getAttributeType("vocabulary model"), versionModule, 
					new DynamicData[] { 
						new DynamicStringImpl(gvm.getName()), 
						new DynamicStringImpl(gvm.getTitle()), 
						new DynamicStringImpl(gvm.getPackageKind().value()), 
						new DynamicStringImpl(gvm.getDefinitionKind().value()),
						new DynamicStringImpl(gvm.getSchemaVersion()) },
					contentTime);

			DynamicStringImpl[] realms = new DynamicStringImpl[gvm.getPackageLocation().getRealmNamespace() == null ? 0
					: gvm.getPackageLocation().getRealmNamespace().size()];
			for (int i = 0; i < realms.length; i++)
			{
				realms[i] = new DynamicStringImpl(gvm.getPackageLocation().getRealmNamespace().get(i));
			}

			dwh.makeDynamicSemantic(dwh.getAttributeType("package location"), versionModule, 
					new DynamicData[] { 
						new DynamicStringImpl(gvm.getPackageLocation().getCombinedId()),
						new DynamicStringImpl(gvm.getPackageLocation().getRoot().value()), 
						new DynamicStringImpl(gvm.getPackageLocation().getArtifact().value()),
						new DynamicArrayImpl<DynamicString>(realms), 
						new DynamicStringImpl(gvm.getPackageLocation().getVersion()) },
					contentTime);
			
			dwh.makeDynamicSemantic(dwh.getAttributeType("rendering information"), versionModule, 
					new DynamicData[] { 
							new DynamicStringImpl(gvm.getHeader().getRenderingInformation().getRenderingTime().toString()),
							new DynamicStringImpl(gvm.getHeader().getRenderingInformation().getApplication()) },
					contentTime);

			DynamicStringImpl[] copyrightYears = new DynamicStringImpl[gvm.getHeader().getLegalese().getCopyrightYears() == null ? 0
					: gvm.getHeader().getLegalese().getCopyrightYears().size()];

			for (int i = 0; i < realms.length; i++)
			{
				copyrightYears[i] = new DynamicStringImpl(gvm.getHeader().getLegalese().getCopyrightYears().get(i) + "");
			}

			dwh.makeDynamicSemantic(dwh.getAttributeType("legalese"), versionModule, 
					new DynamicData[] { 
						new DynamicStringImpl(gvm.getHeader().getLegalese().getCopyrightOwner()), 
						new DynamicArrayImpl<DynamicString>(copyrightYears) },
					contentTime);

			rootConceptUUID = dwh.makeConceptEnNoDialect(null, "HL7v3", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
					new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}, Status.ACTIVE, contentTime);
			
			UUID conceptDomains = dwh.makeConceptEnNoDialect(null, "Concept Domains", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
					new UUID[] {rootConceptUUID}, Status.ACTIVE, contentTime); 

			// Build a hashmap from codeSystem name -> OID
			for (CodeSystem cs : gvm.getCodeSystem())
			{
				String in = codeSystemNameToOID.put(cs.getName().toLowerCase(), cs.getCodeSystemId());
				if (in != null)
				{
					throw new MojoExecutionException("Non-unique code system name!");
				}
			}

			// TODO handle these
//			gvm.getAnnotations();
//			gvm.getBindingRealm();
//			gvm.getBusinessName();
//			gvm.getCodeSystemSupplement();
//			gvm.getCodeTranslations();
//			gvm.getContextBinding();
//			gvm.getDependsOnVocabModel();
//			gvm.getReplacedBy();
//			gvm.getReplaces();
//			gvm.getSecondaryId();
//			gvm.getSortKey();

			// Process the concept domains
			log.info("Processing Concept Domains");
			statusUpdates.accept("Processing Concept Domains");
			int conceptDomainCount = 0;
			int deprecatedConceptDomains = 0;

			for (ConceptDomain cd : gvm.getConceptDomain())
			{
				UUID domainUUID = createConceptDomainUUID(cd.getName());
				AtomicReference<Status> status = new AtomicReference<>(Status.ACTIVE);
				for (HistoryItem hi : cd.getHistoryItem())
				{
					boolean retiredFlag = handleHistoryItem(hi, domainUUID, contentTime);
					if (retiredFlag)
					{
						status.set(Status.INACTIVE);
					}
				}

				if (cd.getAnnotations() != null && cd.getAnnotations().getAppInfo() != null && cd.getAnnotations().getAppInfo().getDeprecationInfo() != null
						&& StringUtils.isNotBlank(cd.getAnnotations().getAppInfo().getDeprecationInfo().getDeprecationEffectiveVersion()))
				{
					deprecatedConceptDomains++;
					continue;
				}

				UUID conceptDomain = dwh.makeConceptEnNoDialect(null, cd.getName(), MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
						new UUID[] {conceptDomains}, status.get(), contentTime);
				
				conceptDomainCount++;
				if (conceptDomainCount % 100 == 0)
				{
					showProgress();
				}

				if (cd.getAnnotations() != null && cd.getAnnotations().getDocumentation() != null
						&& cd.getAnnotations().getDocumentation().getDefinition() != null)
				{
					// TODO any other annotations
					flatten(cd.getAnnotations().getDocumentation().getDefinition().getText(), s -> {
						dwh.makeDescriptionEnNoDialect(conceptDomain, s, dwh.getDescriptionType("documentation"), status.get(), contentTime);
					});
				}
				for (ConceptDomainRef sd : cd.getSpecializesDomain())
				{
					dwh.makeAssociation(dwh.getAssociationType("specializes domain"), conceptDomain, createConceptDomainUUID(sd.getName()), contentTime);
				}

				for (ConceptDomainRef sbd : cd.getSpecializedByDomain())
				{
					dwh.makeAssociation(dwh.getAssociationType("specialized by domain"), conceptDomain, createConceptDomainUUID(sbd.getName()), contentTime);
				}

				for (ConceptDomainProperty p : cd.getProperty())
				{
					UUID propType = dwh.getAttributeType(p.getName().value());
					if (propType == null)
					{
						throw new MojoExecutionException("Oops - no handler for " + p.getName().value());
					}

					// Split data like this to code system / code "ActClass.ADJUD"

					int split = p.getValue().indexOf('.');
					if (split < 0)
					{
						throw new MojoExecutionException("Oops: " + p.getValue());
					}

					dwh.makeDynamicSemantic(propType, conceptDomain, new DynamicData[] {
							new DynamicNidImpl(createConceptCodeUUID(p.getValue().substring(0, split), p.getValue().substring((split + 1), p.getValue().length()),
									true))}, contentTime);
				}

				// TODO handle these
//				cd.isIsBindable();
//				cd.getExampleConcept();
//				cd.getBusinessName();
//				cd.getSortKey();
			}
			
			advanceProgressLine();
			log.info("Processed " + conceptDomainCount + " concept domains");
			log.info("Skipped " + deprecatedConceptDomains + " deprecated concept domains");

			UUID codeSystems = dwh.makeConceptEnNoDialect(null, "Code Systems", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
					new UUID[] {rootConceptUUID}, Status.ACTIVE, contentTime);  

			// Process the Code systems
			log.info("Processing Code Systems");
			statusUpdates.accept("Processing code systems");
			int codeSystemCount = 0;
			int conceptCount = 0;
			int valueSetCount = 0;
			int totalConceptCount = 0;
			int skippedEmptyCodeSystems = 0;
			int skippedDeprecatedCodeSystems = 0;
			int skippedDeprecatedValueSets = 0;
			for (CodeSystem cs : gvm.getCodeSystem())
			{
				List<UUID> codeSystemCodePointers = codeUUIDPointers.get(cs.getCodeSystemId());
				List<Concept> codeSystemConceptPointers = codeConceptPointers.get(cs.getCodeSystemId());
				if (codeSystemCodePointers == null)
				{
					codeSystemCodePointers = new ArrayList<>();
					codeUUIDPointers.put(cs.getCodeSystemId(), codeSystemCodePointers);

					codeSystemConceptPointers = new ArrayList<>();
					codeConceptPointers.put(cs.getCodeSystemId(), codeSystemConceptPointers);
				}
				else
				{
					throw new RuntimeException("unexpected");
				}

				if (cs.getAnnotations() != null && cs.getAnnotations().getAppInfo() != null && cs.getAnnotations().getAppInfo().getDeprecationInfo() != null
						&& StringUtils.isNotBlank(cs.getAnnotations().getAppInfo().getDeprecationInfo().getDeprecationEffectiveVersion()))
				{
					skippedDeprecatedCodeSystems++;
					continue;
				}

				int temp = 0;
				for (CodeSystemVersion csv : cs.getReleasedVersion())
				{
					temp += csv.getConcept().size();
				}
				if (temp == 0)
				{
					skippedEmptyCodeSystems++;
					continue;
				}
				codeSystemCount++;
				if (codeSystemCount % 100 == 0)
				{
					showProgress();
				}
				
				UUID codeSystem = dwh.makeConceptEnNoDialect(createCodeSystemUUID(cs.getName()), cs.getName(), 
						dwh.getDescriptionType("name"), new UUID[] {codeSystems}, Status.ACTIVE, contentTime);

				dwh.makeDescriptionEnNoDialect(codeSystem, cs.getTitle(), dwh.getDescriptionType("title"), Status.ACTIVE, contentTime);

				// TODO handle these
//				cs.isHasHomonymy();
//				cs.isHasSynonymy();
//				cs.isIsCaseSensitive();
//				cs.isIsWhitespaceSensitive();
//				cs.getApproxCodeCount();
//				cs.getBusinessName();
//				cs.getHeader();
//				cs.getHistoryItem();
//				cs.getPrimaryLanguage();
//				cs.getPrimaryRealm();
//				cs.getPropertyGroup();
//				cs.getSortKey();

				if (cs.getAnnotations() != null && cs.getAnnotations().getDocumentation() != null
						&& cs.getAnnotations().getDocumentation().getDescription() != null)
				{
					flatten(cs.getAnnotations().getDocumentation().getDescription().getText(), s -> {
						dwh.makeDescriptionEnNoDialect(codeSystem, s, dwh.getDescriptionType("description"), Status.ACTIVE, contentTime);
					});
				}

				dwh.makeBrittleStringAnnotation(dwh.getAttributeType("OID"), codeSystem, cs.getCodeSystemId(), contentTime);

				for (CodeSystemVersion csv : cs.getReleasedVersion())
				{
					dwh.makeDynamicSemantic(dwh.getAttributeType("released version"), codeSystem, new DynamicData[] { 
							new DynamicStringImpl(csv.getReleaseDate().toString()),
							csv.getPublisherVersionId() == null ? null : new DynamicStringImpl(csv.getPublisherVersionId()),
							new DynamicBooleanImpl(csv.isHl7MaintainedIndicator()), 
							new DynamicBooleanImpl(csv.isCompleteCodesIndicator()),
							new DynamicBooleanImpl(csv.isHl7ApprovedIndicator()) }, 
						contentTime);

					for (SupportedConceptRelationship scr : csv.getSupportedConceptRelationship())
					{
						dwh.makeDynamicSemantic(dwh.getAttributeType("supported concept relationship"), codeSystem, 
								new DynamicData[] {new DynamicNidImpl(dwh.getAssociationType(scr.getName()))}, contentTime);
					}
					for (SupportedConceptProperty scp : csv.getSupportedConceptProperty())
					{
						dwh.makeDynamicSemantic(dwh.getAttributeType("supported concept property"), codeSystem, 
								new DynamicData[] {
										new DynamicNidImpl(dwh.getAttributeType(scp.getPropertyName())),
										new DynamicNidImpl(dwh.getAttributeType(scp.getType().name())),
										new DynamicBooleanImpl(scp.isIsMandatoryIndicator()), new DynamicBooleanImpl(scp.isApplyToValueSetsIndicator()),
										StringUtils.isBlank(scp.getDefaultValue()) ? null : new DynamicStringImpl(scp.getDefaultValue()) },
							contentTime);
					}
					// TODO handle all these
					// csv.getAnnotations();
					// csv.getHistoryItem();
					// csv.getPublicTerminologyServer();
					// csv.getSupportedCodeProperty();
					// csv.getSupportedLanguage();

					for (Concept c : csv.getConcept())
					{
						if (c.getCode().size() < 1)
						{
							throw new MojoExecutionException("No code on concept " + c.getPrintName().get(0).getText());
						}
						conceptCount++;
						totalConceptCount++;

						UUID conceptUUID = createConceptCodeUUID(cs.getName(), c.getCode().get(0).getCode(), false);
						
						ArrayList<UUID> additionalUUIDs = new ArrayList<>();
						// Need to add additional primary UUIDs
						for (int i = 1; i < c.getCode().size(); i++)
						{
							additionalUUIDs.add(createConceptCodeUUID(cs.getName(), c.getCode().get(i).getCode(), false));
						}
						
						UUID concept = dwh.makeConcept(conceptUUID, Status.ACTIVE, contentTime, additionalUUIDs.toArray(new UUID[additionalUUIDs.size()]));

						codeSystemCodePointers.add(conceptUUID);
						codeSystemConceptPointers.add(c);

						dwh.makeParentGraph(concept, codeSystem, Status.ACTIVE, contentTime);

						dwh.makeDynamicSemantic(dwh.getAttributeType("is selectable"), concept, new DynamicBooleanImpl(c.isIsSelectable()), contentTime);

						// TODO the rest of the annotations nested annotation possibilities
						if (c.getAnnotations() != null && c.getAnnotations().getDocumentation() != null
								&& c.getAnnotations().getDocumentation().getDefinition() != null)
						{
							flatten(c.getAnnotations().getDocumentation().getDefinition().getText(), s -> {
								dwh.makeDescriptionEnNoDialect(concept, s, dwh.getDescriptionType("documentation"), Status.ACTIVE, contentTime);
							});

						}
						for (PrintName d : c.getPrintName())
						{
							// TODO icon
							if (!d.getLanguage().equals("en"))
							{
								throw new MojoExecutionException("unhandled language!");
							}
							dwh.makeDescriptionEnNoDialect(concept, d.getText(), dwh.getDescriptionType("print name"), Status.ACTIVE, contentTime);
						}
						for (ConceptCode cc : c.getCode())
						{
							cc.getCode();
							// TODO handle all these - code in as an attribute with all the nested stuff?
//								cc.getCodeProperty();
//								cc.getEffectiveDate();
//								cc.getPrintName();
//								cc.getPropertyGroup();
//								cc.getRetirementDate();
//								cc.getStatus();
							dwh.makeDescriptionEnNoDialect(concept, cc.getCode(), dwh.getDescriptionType("code"), Status.ACTIVE, contentTime);
							dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), concept, cc.getCode(), contentTime);
						}

						for (ConceptProperty cp : c.getConceptProperty())
						{
							UUID attributeType = dwh.getAttributeType(cp.getName());
							if (attributeType == null)
							{
								// Data bug...
								if (cp.getName().equals("Name:role:scoper:Entity"))
								{
									attributeType = dwh.getAttributeType("Name:Role:scoper:Entity");
								}
								else
								{
									throw new MojoExecutionException("Can't find attribute definition for '" + cp.getName() + "'");
								}
							}
							// TODO status concept property needs special handling
							if (attributeType.equals(dwh.getAttributeType("OID")))
							{
								dwh.makeBrittleStringAnnotation(attributeType, concept, cp.getValue(), contentTime);
							}
							else
							{
								dwh.makeStringAnnotation(attributeType, concept, cp.getValue(), contentTime);
							}
						}

						//We need to delay processing the relationships until creating all of the concepts, because of the way 
						//they have defined their targets, sometimes they use one code, sometimes another, then later, merge those two codes
						//into a single concept, causing us issues merging UUIDs onto a single concept.
						

						// c.getEffectiveDate();
						// c.getHistoryItem();
						// c.getIntendedUse();
						// c.getPropertyGroup();

					}
					
					//process delayed relationships
					for (Concept c : csv.getConcept())
					{
						for (ConceptRelationship r : c.getConceptRelationship())
						{
							dwh.makeAssociation(dwh.getAssociationType(r.getRelationshipName()), 
									createConceptCodeUUID(cs.getName(), c.getCode().get(0).getCode(), false), 
									createConceptCodeUUID(
											StringUtils.isBlank(r.getTargetConcept().getCodeSystem()) ? cs.getName() : r.getTargetConcept().getCodeSystem(),
											r.getTargetConcept().getCode(), true),
									contentTime);
							// TODO handle these
							// r.getProperty()
							// r.isIsDerived();
						}
					}
					advanceProgressLine();
					log.info("Added " + conceptCount + " concepts to " + cs.getName());
					conceptCount = 0;
				}
			}

			for (ValueSet vs : gvm.getValueSet())
			{
				// TODO handle these
//				vs.isIsImmutable();
//				vs.getBusinessName();
//				vs.getSortKey();

				valueSetCount++;
				UUID refset = dwh.getRefsetType(vs.getName());

				if (vs.getAnnotations() != null && vs.getAnnotations().getAppInfo() != null && vs.getAnnotations().getAppInfo().getDeprecationInfo() != null
						&& StringUtils.isNotBlank(vs.getAnnotations().getAppInfo().getDeprecationInfo().getDeprecationEffectiveVersion()))
				{
					skippedDeprecatedValueSets++;
					continue;
				}

				dwh.makeBrittleStringAnnotation(dwh.getAttributeType("OID"), refset, vs.getId(), contentTime);

				if (vs.getAnnotations() != null && vs.getAnnotations().getDocumentation() != null
						&& vs.getAnnotations().getDocumentation().getDescription() != null)
				{
					// TODO any other annotations
					flatten(vs.getAnnotations().getDocumentation().getDescription().getText(), s -> {
						dwh.makeDescriptionEnNoDialect(refset, s, dwh.getDescriptionType("description"), Status.ACTIVE, contentTime);
					});
				}

				for (HistoryItem hi : vs.getHistoryItem())
				{
					handleHistoryItem(hi, refset, contentTime);
				}
				boolean fullyResolved = true;

				for (ValueSetVersion vsv : vs.getVersion())
				{
					// TODO all these
//					vsv.getAssociatedConceptProperty();
//					vsv.getEnumeratedContent();
//					vsv.getExampleContent();
//					vsv.getHistoryItem();
//					vsv.getNonSelectableContent();
//					vsv.getSupportedLanguage();
//					vsv.getUsesCodeSystemSupplement();
//					vsv.getVersionDate();
//					vsv.getVersionTime();

					// TODO
//					for (String scs : vsv.getSupportedCodeSystem())
//					{
//						//These are OIDs - map to the concepts that represent the code system
//						
//					}

					if (!addContentDefinitionToRefSet(vsv.getContent(), refset))
					{
						fullyResolved = false;
					}
				}

				if (fullyResolved)
				{
					fullyCalculatedRefsetMembers.add(refset);
				}
			}

			int unresolvedValueSets = incompleteRefsetData.size();
			int last = -1;
			while (unresolvedValueSets > 0)
			{
				if (last == unresolvedValueSets)
				{
					throw new RuntimeException("Unable to resolve all valuesets?!?");
				}

				List<UUID> cleanup = new ArrayList<>();
				for (Entry<UUID, IncompleteValueSetData> x : incompleteRefsetData.entrySet())
				{
					x.getValue().resolve();
					if (x.getValue().unprocessed.size() == 0)
					{
						cleanup.add(x.getKey());
					}
				}

				for (UUID uuid : cleanup)
				{
					incompleteRefsetData.remove(uuid);
					fullyCalculatedRefsetMembers.add(uuid);
				}

				last = unresolvedValueSets;
				unresolvedValueSets = incompleteRefsetData.size();
			}

			// Now that we have fully resolved all of the value sets, and removed duplicate entries (some of the unions on the hl7 content make
			// duplicates) populate our refsets.
			for (Entry<UUID, Set<Integer>> refsetData : inProgressRefsetMembers.entrySet())
			{
				for (Integer refsetMember : refsetData.getValue())
				{
					dwh.makeDynamicRefsetMember(refsetData.getKey(), Get.identifierService().getUuidPrimordialForNid(refsetMember), contentTime);
					valueSetMemberCount++;
				}
			}
			
			dwh.processTaxonomyUpdates();
			Get.taxonomyService().notifyTaxonomyListenersToRefresh();

			log.info("Created " + codeSystemCount + " code systems");
			log.info("Skipped " + skippedEmptyCodeSystems + " code systems for being empty");
			log.info("Skipped " + skippedDeprecatedCodeSystems + " code systems for being deprecated");
			log.info("Skipped " + skippedDeprecatedValueSets + " value sets for being deprecated");
			log.info("Created " + totalConceptCount + " concepts");
			log.info("Created " + valueSetCount + " value sets");
			log.info("Created " + valueSetMemberCount + " value set memebers");

			// this could be removed from final release. Just added to help debug editor problems.
			log.info("Dumping UUID Debug File");
			converterUUID.dump(outputDirectory, "hl7v3Uuid");

			log.info("Load Statistics");
			for (String line : dwh.getLoadStats().getSummary())
			{
				log.info(line);
			}
		}
		catch (Exception ex)
		{
			System.out.println("Dieing .... dumping UUID debug file: ");
			try
			{
				converterUUID.dump(outputDirectory, "vhatUuid");
			}
			catch (IOException e)
			{
				System.out.println("Failed dumping debug file...");
			}
			throw new RuntimeException(ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * Setup attributes and associations that come from individual values sets and code sets in HL7
	 * @param contentTime
	 */
	private void makeTerminologySpecificAttributes(GlobalVocabularyModel gvm, long contentTime)
	{
		HashMap<String, SupportedConceptRelationship> supportedConceptRelationshipsToSCR = new HashMap<>();
		HashMap<String, SupportedConceptProperty> supportedConceptPropertiesToSCP = new HashMap<>();
		HashSet<String> uniqueReleasedVersions = new HashSet<>();
		// Build out the association types, property types
		// Also see if we have any other 'releasedVersions' in the file that we might need to deal with path issues on.
		for (CodeSystem cs : gvm.getCodeSystem())
		{
			for (CodeSystemVersion csv : cs.getReleasedVersion())
			{
				uniqueReleasedVersions.add(csv.getReleaseDate().toString());
				for (SupportedConceptRelationship scr : csv.getSupportedConceptRelationship())
				{
					
					SupportedConceptRelationship known = supportedConceptRelationshipsToSCR.get(scr.getName());
					if (known != null)
					{
						// TODO handle all these
//						known.getAllowedForSources();
//						known.getAllowedForTargets();
//						known.getDefiningConcept();
//						known.getId();
//						known.getReflexivity();
//						known.getRelationshipKind();
//						known.getRequiredForSources();
//						known.getRequiredForTargets();
//						known.getSupportedProperty();
//						known.getSymmetry();
//						known.getTransitivity();
//						known.isFunctionalism();
//						known.isIsNavigable();

						if (inverseRelNameMap.get(scr.getName()) == null)
						{
							inverseRelNameMap.put(scr.getName(), scr.getInverseName());
						}
						else
						{
							if (!inverseRelNameMap.get(scr.getName()).equals(scr.getInverseName()))
							{
								throw new RuntimeException("oops");
							}
						}

						// Make sure that everything we know about is what we stored.
						if (known.getInverseName() == null && scr.getInverseName() != null
								|| (known.getInverseName() != null && !known.getInverseName().equals(scr.getInverseName()))
								|| known.isIsNavigable() != scr.isIsNavigable() || known.getReflexivity() != scr.getReflexivity()
								|| known.getSymmetry() != scr.getSymmetry() || known.getTransitivity() != scr.getTransitivity()
								|| !(flatten(known.getDescription())).equals(flatten(scr.getDescription())))
						{
							throw new RuntimeException("concept relationship defined differently: " + known.getName());
						}
					}
					else
					{
						UUID assnType = dwh.makeAssociationTypeConcept(null, scr.getName(), null, null, flatten(scr.getDescription()), scr.getInverseName(), 
								null, null, null, contentTime);
						supportedConceptRelationshipsToSCR.put(scr.getName(), scr);

						if (scr.getReflexivity() != null)
						{
							dwh.makeDynamicSemantic(dwh.getAttributeType("reflexivity"), assnType, 
									new DynamicData[] {new DynamicNidImpl(dwh.getAttributeType(scr.getReflexivity().name()))},
									contentTime);
						}

						if (scr.getSymmetry() != null)
						{
							dwh.makeDynamicSemantic(dwh.getAttributeType("symmetry"), assnType, 
									new DynamicData[] {new DynamicNidImpl(dwh.getAttributeType(scr.getSymmetry().name()))},
									contentTime);
						}

						if (scr.getTransitivity() != null)
						{
							dwh.makeDynamicSemantic(dwh.getAttributeType("transitivity"), assnType, 
									new DynamicData[] {new DynamicNidImpl(dwh.getAttributeType(scr.getTransitivity().name()))},
									contentTime);
						}

						if (scr.getRelationshipKind() != null)
						{
							dwh.makeDynamicSemantic(dwh.getAttributeType("relationship kind"), assnType, 
									new DynamicData[] {new DynamicNidImpl(dwh.getAttributeType(scr.getRelationshipKind().name()))},
									contentTime);
						}
						dwh.makeDynamicSemantic(dwh.getAttributeType("is navigable"), assnType, 
								new DynamicData[] {new DynamicBooleanImpl(scr.isIsNavigable())},
								contentTime);
					}
				}

				for (SupportedConceptProperty scp : csv.getSupportedConceptProperty())
				{
					SupportedConceptProperty known = supportedConceptPropertiesToSCP.get(scp.getPropertyName());
					if (known != null)
					{
						// just need to check the attributes that are part of the attribute semantic definition
						// Make sure that everything we know about is what we stored.
						if ((known.getDefaultValue() == null && scp.getDefaultValue() != null)
								|| (known.getDefaultValue() != null && !known.getDefaultValue().equals(scp.getDefaultValue()))
								|| !flatten(known.getDescription()).equals(flatten(scp.getDescription())))
						{
							throw new RuntimeException("concept property defined differently: " + known.getPropertyName());
						}
					}
					else
					{
						if (scp.getPropertyName().equals("OID"))
						{
							log.debug("skip oid property");
						}
						else
						{
							dwh.makeAttributeTypeConcept(null, scp.getPropertyName(), null, null, flatten(scp.getDescription()), 
									new DynamicColumnInfo[] {
											new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), 
													DynamicDataType.STRING, StringUtils.isNotBlank(scp.getDefaultValue()) ? new DynamicStringImpl(scp.getDefaultValue()) : null,
															true, true)
									}, null, null, contentTime);
							
							supportedConceptPropertiesToSCP.put(scp.getPropertyName(), scp);
							// TODO handle these
//							scp.isApplyToValueSetsIndicator();
//							scp.isIsMandatoryIndicator();
//							scp.getDefaultHandlingCode();
//							scp.getEnumerationValue();
//							scp.getType();
						}
					}
				}
			}
		}

		if (uniqueReleasedVersions.size() > 1)
		{
			log.error("Don't currently handle multiple releasedVersions!");
			for (String s : uniqueReleasedVersions)
			{
				log.error(s);
			}
			throw new RuntimeException("Failing due to previous errors");
		}

		for (ValueSet vs : gvm.getValueSet())
		{
			HashSet<String> uniqueValueSetReleasedVersions = new HashSet<>();
			for (ValueSetVersion vsv : vs.getVersion())
			{
				uniqueValueSetReleasedVersions.add(vsv.getVersionDate().toString());
			}

			if (uniqueValueSetReleasedVersions.size() > 1)
			{
				log.error("Don't currently handle multiple value set releasedVersions!");
				for (String s : uniqueReleasedVersions)
				{
					log.error(s);
				}
				throw new RuntimeException("Failing due to previous errors");
			}

			String in = valueSetNameToOID.put(vs.getName().toLowerCase(), vs.getId());
			if (in != null)
			{
				throw new RuntimeException("Non-unique value set name!");
			}

			dwh.makeRefsetTypeConcept(null, vs.getName(), null, null, contentTime);
		}
	}

	/**
	 * Setup the attribute metadata for hl7v3
	 * @param contentTime
	 */
	private void makeAttributes(long contentTime)
	{
		dwh.makeAttributeTypeConcept(null, "OID", "HL7 Object Identifier", null, null, true, null, null, contentTime);
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, contentTime, readbackCoordinate);
		dwh.makeAttributeTypeConcept(null, "vocabulary model", null, null, null, 
				new DynamicColumnInfo[] {
						new DynamicColumnInfo(0, 
								//reuse description type name
								dwh.getDescriptionType("name"), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(1, 
								//Reuse description type 
								dwh.getDescriptionType("title"), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(2, 
								makeOrGetAttributeColumn("package kind", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(3, 
								makeOrGetAttributeColumn("definition kind", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(4, 
								makeOrGetAttributeColumn("schema version", contentTime), DynamicDataType.STRING, null, false, true)
						}, 
				null, null, contentTime);
		dwh.makeAttributeTypeConcept(null, "package location", null, null, null, 
				new DynamicColumnInfo[] {
						new DynamicColumnInfo(0, 
								makeOrGetAttributeColumn("combined id", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(1, 
								makeOrGetAttributeColumn("root", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(2, 
								makeOrGetAttributeColumn("artifact", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(3, 
								makeOrGetAttributeColumn("realm namespace", contentTime), DynamicDataType.ARRAY, null, false, true),
						new DynamicColumnInfo(4, 
								makeOrGetAttributeColumn("version", contentTime), DynamicDataType.STRING, null, false, true)
						}, 
				null, null, contentTime);
				dwh.makeAttributeTypeConcept(null, "rendering information", null, null, null, 
						new DynamicColumnInfo[] {
								new DynamicColumnInfo(0, 
										makeOrGetAttributeColumn("rendering time", contentTime), DynamicDataType.STRING, null, false, true),
								new DynamicColumnInfo(1, 
										makeOrGetAttributeColumn("application", contentTime), DynamicDataType.STRING, null, false, true)
								}, 
				null, null, contentTime);
				dwh.makeAttributeTypeConcept(null, "legalese", null, null, null, 
						new DynamicColumnInfo[] {
								new DynamicColumnInfo(0, 
										makeOrGetAttributeColumn("copyright owner", contentTime), DynamicDataType.STRING, null, false, true),
								new DynamicColumnInfo(1, 
										makeOrGetAttributeColumn("copyright years", contentTime), DynamicDataType.ARRAY, null, false, true)
								}, 
				null, null, contentTime);
		dwh.makeAttributeTypeConcept(null, "history item", null, null, null, 
				new DynamicColumnInfo[] {
						new DynamicColumnInfo(0, 
								makeOrGetAttributeColumn("datetime", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(1,
								makeOrGetAttributeColumn("responsible person name", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(2, 
								makeOrGetAttributeColumn("id", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(3, 
								makeOrGetAttributeColumn("is substantive change", contentTime), DynamicDataType.BOOLEAN, null, false, true),
						new DynamicColumnInfo(4, 
								makeOrGetAttributeColumn("is backward compatible change", contentTime), DynamicDataType.BOOLEAN, null, false, true),
						new DynamicColumnInfo(5, 
								//reuse description type
								dwh.getDescriptionType("description"), DynamicDataType.STRING, null, false, true)
						}, 
				null, null, contentTime);
		
		//Items for associations
		dwh.makeAttributeTypeConcept(null, "is navigable", null, null, false, DynamicDataType.BOOLEAN, null, contentTime);
		dwh.makeAttributeTypeConcept(null, "reflexivity", null, null, false, DynamicDataType.NID, null, contentTime);
		dwh.makeAttributeTypeConcept(null, "symmetry", null, null, false, DynamicDataType.NID, null, contentTime);
		dwh.makeAttributeTypeConcept(null, "transitivity", null, null, false, DynamicDataType.NID, null, contentTime);
		dwh.makeAttributeTypeConcept(null, "relationship kind", null, null, false, DynamicDataType.NID, null, contentTime);

		dwh.makeAttributeTypeConcept(null, "released version", null, null, null, 
				new DynamicColumnInfo[] {
						new DynamicColumnInfo(0, 
								makeOrGetAttributeColumn("release date", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(1, 
								makeOrGetAttributeColumn("publisher version", contentTime), DynamicDataType.STRING, null, false, true),
						new DynamicColumnInfo(2,
								makeOrGetAttributeColumn("hl7 maintained indicator", contentTime), DynamicDataType.BOOLEAN, null, false, true),
						new DynamicColumnInfo(3, 
								makeOrGetAttributeColumn("complete codes indicator", contentTime), DynamicDataType.BOOLEAN, null, false, true),
						new DynamicColumnInfo(4, 
								makeOrGetAttributeColumn("hl7 approved indicator", contentTime), DynamicDataType.BOOLEAN, null, false, true),
						}, 
				null, null, contentTime);
		
		dwh.makeAttributeTypeConcept(null, "supported concept relationship", null, null, false, DynamicDataType.NID, null, contentTime);
		
		dwh.makeAttributeTypeConcept(null, "supported concept property", null, null, null, 
				new DynamicColumnInfo[] {
						new DynamicColumnInfo(0, 
								makeOrGetAttributeColumn("property name", contentTime), DynamicDataType.NID, null, false, true),
						new DynamicColumnInfo(1, 
								makeOrGetAttributeColumn("type", contentTime), DynamicDataType.NID, null, false, true),
						new DynamicColumnInfo(2, 
								makeOrGetAttributeColumn("is mandatory indicator", contentTime), DynamicDataType.BOOLEAN, null, false, true),
						new DynamicColumnInfo(3, 
								makeOrGetAttributeColumn("is apply to value sets indicator", contentTime), DynamicDataType.BOOLEAN, null, false, true),
						new DynamicColumnInfo(4, 
								makeOrGetAttributeColumn("default value", contentTime), DynamicDataType.STRING, null, false, true),
						}, 
				null, null, contentTime);
		
		dwh.makeAttributeTypeConcept(null, "supported concept property type", null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, "property default handling kind", null, null, false, DynamicDataType.STRING, null, contentTime);
		dwh.makeAttributeTypeConcept(null, "is selectable", null, null, false, DynamicDataType.BOOLEAN, null, contentTime);
		
		// add property types for ConceptDomains in use
		dwh.makeAttributeTypeConcept(null, ConceptDomainPropertyKind.CONCEPTUAL_SPACE_FOR_CLASS_CODE.value(), null, 
				"Specifies the classCode for which the domain defines the conceptual space", false, 
				DynamicDataType.NID, null, contentTime);
		
		for (Reflexivity r : Reflexivity.values())
		{
			dwh.makeAttributeTypeConcept(null, r.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		}
		
		for (Symmetry s : Symmetry.values())
		{
			dwh.makeAttributeTypeConcept(null, s.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		}
		for (Transitivity t : Transitivity.values())
		{
			dwh.makeAttributeTypeConcept(null, t.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		}
		for (ConceptRelationshipKind crk : ConceptRelationshipKind.values())
		{
			dwh.makeAttributeTypeConcept(null, crk.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		}

		for (ConceptPropertyTypeKind t : ConceptPropertyTypeKind.values())
		{
			dwh.makeAttributeTypeConcept(null, t.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		}

		// TODO this is sometimes used as an attribute on a supportedConceptProperty - need to handle those.
		for (PropertyDefaultHandlingKind t : PropertyDefaultHandlingKind.values())
		{
			dwh.makeAttributeTypeConcept(null, t.name(), null, null, false, DynamicDataType.STRING, null, contentTime);
		}
	}
	
	private UUID makeOrGetAttributeColumn(String columnName, long contentTime)
	{
		UUID root = dwh.getOtherMetadataRootType("Attribute Columns");
		if (root == null)
		{
			root = dwh.makeOtherMetadataRootNode("Attribute Columns", contentTime);
		}
		UUID toReturn = dwh.getOtherType(root, columnName);
		if (toReturn == null)
		{
			toReturn = dwh.makeOtherTypeConcept(root, null, columnName, null, null, null, null, null, contentTime);
		}
		return toReturn;
	}

	/**
	 * Returns true, if this was fully resolved, false, if it could not yet be fully resolved.
	 */
	private boolean addContentDefinitionToRefSet(ContentDefinition cd, UUID refset) throws MojoExecutionException
	{
		if (cd == null)
		{
			return true;
		}
		// TODO handle annotations
//		cd.getAnnotations();
//		cd.getVersionDate();

		boolean fullyResolved = true;

		// these items are part of a switch, in the schema definition - only 0 or 1 will have content.
		boolean foundSwitch = false;

		if (cd.getCombinedContent() != null)
		{
			foundSwitch = true;
			// TODO all these
			CombinedContentDefinition ccd = cd.getCombinedContent();
			// Note - my current hack design for dereferencing calculated refsets won't work properly with exclusions / intersections.
//			ccd.getExcludeContent();
//			ccd.getIntersectionWithContent();

			for (ContentDefinition union : ccd.getUnionWithContent())
			{
				if (!addContentDefinitionToRefSet(union, refset))
				{
					fullyResolved = false;
				}
			}
		}
		if (cd.getCodeBasedContent() != null && cd.getCodeBasedContent().size() > 0)
		{
			if (foundSwitch)
			{
				throw new RuntimeException("Unexpected");
			}
			foundSwitch = true;

			for (CodeBasedContentDefinition cbcd : cd.getCodeBasedContent())
			{
				String codeSystemOid = cd.getCodeSystem();
				for (IncludeRelatedCodes irc : cbcd.getIncludeRelatedCodes())
				{
					for (Integer member : resolveRels(irc.getRelationshipTraversal(), irc.getRelationshipName(), 0,
							findTargetConcept(codeSystemOid, cbcd.getCode()), codeSystemOid))
					{
						Set<Integer> members = inProgressRefsetMembers.get(refset);
						if (members == null)
						{
							members = new HashSet<>();
							inProgressRefsetMembers.put(refset, members);
						}
						members.add(member);
					}
				}
				if (cbcd.isIncludeHeadCode())
				{

					Integer refsetMember = Get.identifierService().getNidForUuids(createConceptCodeUUID(codeSystemOid, cbcd.getCode(), true));
					Set<Integer> members = inProgressRefsetMembers.get(refset);
					if (members == null)
					{
						members = new HashSet<>();
						inProgressRefsetMembers.put(refset, members);
					}
					members.add(refsetMember);
					if (!addContentDefinitionToRefSet(cbcd.getHeadCodes(), refset))
					{
						fullyResolved = false;
					}
				}
			}
		}
		if (cd.getPropertyBasedContent() != null)
		{
			if (foundSwitch)
			{
				throw new RuntimeException("Unexpected");
			}
			foundSwitch = true;
			throw new RuntimeException("propertyBased unhandled");
		}
		if (cd.getCodeFilterContent() != null)
		{
			if (foundSwitch)
			{
				throw new RuntimeException("Unexpected");
			}
			foundSwitch = true;
			throw new RuntimeException("codeFilter unhandled");
		}
		if (cd.getNonComputableContent() != null)
		{
			if (foundSwitch)
			{
				throw new RuntimeException("Unexpected");
			}
			foundSwitch = true;
			throw new RuntimeException("non-computable unhandled");
		}
		if (cd.getValueSetRef() != null)
		{
			if (foundSwitch)
			{
				throw new RuntimeException("Unexpected");
			}
			foundSwitch = true;

			VocabularyValueSetRef vsf = cd.getValueSetRef();
			UUID targetRefset = dwh.getRefsetType(vsf.getName());
//			vsf.getId();
//			vsf.getVersionDate();
//			vsf.getVersionTime();

			if (fullyCalculatedRefsetMembers.contains(targetRefset))
			{
				if (inProgressRefsetMembers.get(targetRefset) == null)
				{
					log.warn("No members for targetRefset: " + vsf.getName() + "?");
				}
				else
				{
					for (Integer targetMember : inProgressRefsetMembers.get(targetRefset))
					{
						Set<Integer> members = inProgressRefsetMembers.get(refset);
						if (members == null)
						{
							members = new HashSet<>();
							inProgressRefsetMembers.put(refset, members);
						}
						members.add(targetMember);
					}
				}
			}
			else
			{
				fullyResolved = false;
				IncompleteValueSetData ivsd = incompleteRefsetData.get(refset);
				if (ivsd == null)
				{
					ivsd = new IncompleteValueSetData(refset);
					incompleteRefsetData.put(refset, ivsd);
				}
				ivsd.add(cd);
			}
		}

		if (!foundSwitch)
		{
			// if no switch was provided, then we just use the entire code system, if it was provided.
			String codeSystemOid = cd.getCodeSystem();
			if (codeUUIDPointers.get(codeSystemOid) == null)
			{
				throw new RuntimeException("Can't find codes for code system '" + codeSystemOid + "'");
			}
			for (UUID conceptUUID : codeUUIDPointers.get(codeSystemOid))
			{
				Set<Integer> members = inProgressRefsetMembers.get(refset);
				if (members == null)
				{
					members = new HashSet<>();
					inProgressRefsetMembers.put(refset, members);
				}
				members.add(Get.identifierService().getNidForUuids(conceptUUID));
			}
		}

		// others
		// TODO handle these
//		cd.isAreBaseQualifiersUnlimited();
//		cd.getAllowedQualifiers();
//		cd.getProhibitedQualifiers();
		return fullyResolved;
	}

	private String flatten(Inline description)
	{
		try
		{
			if (description != null && description.getContent() != null)
			{
				StringWriter writer = new StringWriter();
				inlineMarshaller.marshal(new ObjectFactory().createTxtInlineOnly(description), writer);
				StringBuffer sb = writer.getBuffer();
				// Strip off the leading and trailing XML wrapper <inline xmlns="urn:hl7-org:v3/mif2"> ... </inline>
				sb.delete(0, sb.indexOf(">") + 1);
				sb.delete(sb.lastIndexOf("</"), sb.length());

				return sb.toString().trim();
			}
			return "";
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param description
	 * @return
	 */
	private String flatten(Flow description)
	{
		StringBuilder sb = new StringBuilder();
		for (Serializable x : description.getContent())
		{
			if (x instanceof String)
			{
				sb.append(x.toString());
				sb.append("\r\n");
			}
			else
			{
				throw new RuntimeException("Can't handle " + x.getClass() + ": " + x);
			}
		}
		if (sb.length() > 2)
		{
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}

	private void flatten(List<ComplexMarkupWithLanguage> data, Consumer<String> actionPerString) throws JAXBException
	{
		for (ComplexMarkupWithLanguage stringInfo : data)
		{
			StringWriter writer = new StringWriter();

			// Clever trick, to work around a missing @XmlRootElement annotation...
			complexMarkupMarshaller.marshal(new ObjectFactory().createTxtComplexWithLanguage(stringInfo), writer);
			StringBuffer sb = writer.getBuffer();
			// Strip off the leading and trailing XML wrapper <txtComplexWithLanguage xmlns="urn:hl7-org:v3/mif2"> ... </txtComplexWithLanguage>
			sb.delete(0, sb.indexOf(">") + 1);
			sb.delete(sb.lastIndexOf("</"), sb.length());

			if (sb.toString().trim().length() > 0)
			{
				actionPerString.accept(sb.toString().trim());
			}
		}
	}

	/**
	 * @throws JAXBException
	 * @throws IOException
	 * @throws MojoExecutionException
	 * 
	 */
	private GlobalVocabularyModel readXML(Consumer<String> statusUpdates) throws JAXBException, MojoExecutionException, IOException
	{
		final AtomicReference<Path> zipFile = new AtomicReference<>();
		
		Files.walk(inputFileLocationPath, new FileVisitOption[] {}).forEach(path ->
		{
			if (path.toString().toLowerCase().endsWith(".zip"))
			{
				if (zipFile.get() != null)
				{
					throw new RuntimeException("Only expected to find one zip file in the folder " + inputFileLocationPath.normalize());
				}
				zipFile.set(path);
			}
		});

		if (zipFile.get() == null)
		{
			throw new RuntimeException("Did not find a zip file in " + inputFileLocationPath.normalize());
		}
		
		statusUpdates.accept("Parsing MIF");

		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.get(), StandardOpenOption.READ)))
		{
			ZipEntry ze = zis.getNextEntry();
			
			GlobalVocabularyModel gvm = null;
		
			while (ze != null)
			{
				if (ze.getName().toUpperCase().contains("DEFN=UV=VO") && ze.getName().toLowerCase().endsWith(".coremif"))
				{
					if (gvm != null)
					{
						throw new RuntimeException("Found more than one .coremif file");
					}
					Unmarshaller unmarshaller = JAXBContext.newInstance(VocabularyModel.class).createUnmarshaller();

					// Make sure we don't silently ignore schema parse errors.
					unmarshaller.setEventHandler(new ValidationEventHandler()
					{
						// This should perhaps, be fatal in the future....
						@Override
						public boolean handleEvent(ValidationEvent event)
						{
							log.error(event.toString());
							return true;
						}
					});

					log.info("Parsing " + ze.getName() + " from " + zipFile.get().normalize());

					@SuppressWarnings("unchecked")
					JAXBElement<GlobalVocabularyModel> o = (JAXBElement<GlobalVocabularyModel>) unmarshaller.unmarshal(new CloseIgnoringInputStream(zis));
					gvm = o.getValue();
				}
				ze = zis.getNextEntry();
			}

			if (gvm == null)
			{
				throw new RuntimeException("Failed to find a .coremif file matching the prefix pattern DEFN=UV=VO in the source content");
			}
			return gvm;
		}
	}

	private boolean handleHistoryItem(HistoryItem hi, UUID ref, long time)
	{
		boolean retiredMarker = false;
		String description = flatten(hi.getDescription());
		if (description.contains("retiredAsOfRelease:"))
		{
			retiredMarker = true;
		}

		//TODO should this use the datetime from the history item?
		dwh.makeDynamicSemantic(dwh.getAttributeType("history item"), ref, 
				new DynamicData[] {
					hi.getDateTime() == null ? null : new DynamicStringImpl(hi.getDateTime()),
					hi.getResponsiblePersonName() == null ? null : new DynamicStringImpl(hi.getResponsiblePersonName()),
					hi.getId() == null ? null : new DynamicStringImpl(hi.getId()),
					hi.isIsSubstantiveChange() == null ? null : new DynamicBooleanImpl(hi.isIsSubstantiveChange().booleanValue()),
					hi.isIsBackwardCompatibleChange() == null ? null : new DynamicBooleanImpl(hi.isIsBackwardCompatibleChange().booleanValue()),
					StringUtils.isBlank(description) ? null : new DynamicStringImpl(description) }, 
				time);
		return retiredMarker;
	}

	private UUID createConceptDomainUUID(String name)
	{
		UUID temp =  converterUUID.createNamespaceUUIDFromString("ConceptDomain|" + name, true);
		Get.identifierService().assignNid(temp);  //We load some things out of order, so need to assign early.
		return temp;
	}

	private UUID createConceptCodeUUID(String codeSystemNameOrOID, String name, boolean assignNid) throws MojoExecutionException
	{
		String oid = codeSystemNameToOID.get(codeSystemNameOrOID.toLowerCase());
		if (oid == null)
		{
			if (oidMatcher.matcher(codeSystemNameOrOID).matches())
			{
				oid = codeSystemNameOrOID;
			}
			else
			{
				throw new MojoExecutionException("No oid for " + codeSystemNameOrOID + " and it doesn't appear to be an OID");
			}
		}
		UUID temp = converterUUID.createNamespaceUUIDFromString("Code|" + oid + "|" + name, true);
		if (assignNid) 
		{
			Get.identifierService().assignNid(temp);  //We load some things out of order, so need to assign early.
		}
		return temp;
	}

	private UUID createCodeSystemUUID(String codeSystemName) throws MojoExecutionException
	{
		String oid = codeSystemNameToOID.get(codeSystemName.toLowerCase());
		if (oid == null)
		{
			throw new MojoExecutionException("No oid for " + codeSystemName);
		}
		return converterUUID.createNamespaceUUIDFromString("CodeSystem|" + oid, true);
	}

	private List<Integer> resolveRels(RelationshipTraversalKind traversalKind, String relKind, int depth, Concept concept, String codeSystemOid)
			throws MojoExecutionException
	{
		ArrayList<Integer> result = new ArrayList<>();

		for (ConceptRelationship r : concept.getConceptRelationship())
		{
			if (r.getRelationshipName().equals(relKind))
			{
				int target = Get.identifierService().getNidForUuids(createConceptCodeUUID(r.getTargetConcept().getCodeSystem(), r.getTargetConcept().getCode(), true));

				Concept targetCon = findTargetConcept(r.getTargetConcept().getCodeSystem(), r.getTargetConcept().getCode());

				switch (traversalKind)
				{
					case DIRECT_RELATIONS_ONLY:
						if (depth == 0)
						{
							result.add(target);
						}
						break;
					case TRANSITIVE_CLOSURE:
						result.add(target);
						result.addAll(resolveRels(traversalKind, relKind, depth + 1, targetCon, r.getTargetConcept().getCodeSystem()));
						break;
					case TRANSITIVE_CLOSURE_LEAVES:

						boolean hasChild = false;

						for (ConceptRelationship nestedR : targetCon.getConceptRelationship())
						{
							if (nestedR.getRelationshipName().equals(relKind))
							{
								hasChild = true;
								break;
							}
						}

						if (!hasChild)
						{
							result.add(target);
						}
					default :
						throw new RuntimeException("oops");

				}
			}
		}

		// Need to reverse the match, cause the @#$#@$@$ at HL7 wrote out the XML with the inverse name to the one that would be useful. Sigh.
		String inverseRelName = inverseRelNameMap.get(relKind);
		if (inverseRelName != null)
		{
			// Have to turn the traversal upside down, when matching on inverse name
			// Because the rels only run the reverse way we want, I need to iterate all of the concepts in a code system, looking for ones that
			// have the right kind of rel to the passed in concept. most ineffienct content loader ever in 3... 2... 1...

			for (Entry<String, List<Concept>> allConcepts : codeConceptPointers.entrySet())
			{
				for (Concept c : allConcepts.getValue())
				{
					for (ConceptRelationship r : c.getConceptRelationship())
					{
						if (r.getRelationshipName().equals(inverseRelName)
								&& (r.getTargetConcept().getCodeSystem() == null ? allConcepts.getKey() : r.getTargetConcept().getCodeSystem())
										.equals(codeSystemOid)
								&& r.getTargetConcept().getCode().equals(concept.getCode().get(0).getCode()))
						{
							Integer target = Get.identifierService().getNidForUuids(createConceptCodeUUID(allConcepts.getKey(), c.getCode().get(0).getCode(), true));
							switch (traversalKind)
							{
								case DIRECT_RELATIONS_ONLY:
									if (depth == 0)
									{
										result.add(target);
									}
									break;
								case TRANSITIVE_CLOSURE:
									result.add(target);
									result.addAll(resolveRels(traversalKind, relKind, depth + 1, c, allConcepts.getKey()));
									break;
								case TRANSITIVE_CLOSURE_LEAVES:
									if (resolveRels(RelationshipTraversalKind.DIRECT_RELATIONS_ONLY, relKind, 0, c, allConcepts.getKey()).size() == 0)
									{
										result.add(target);
									}
								default :
									throw new RuntimeException("oops");
							}
						}
					}
				}
			}
		}

		return result;
	}

	private Concept findTargetConcept(String codeSystem, String code)
	{
		List<Concept> concepts = codeConceptPointers.get(codeSystem);
		for (Concept c : concepts)
		{
			for (ConceptCode cc : c.getCode())
			{
				if (cc.getCode().equals(code))
				{
					return c;
				}
			}
		}
		throw new RuntimeException("Couldn't find " + codeSystem + " : " + code);
	}

	private class IncompleteValueSetData
	{
		ArrayList<ContentDefinition> unprocessed = new ArrayList<>();
		UUID refset;

		/**
		 * @param refset
		 */
		protected IncompleteValueSetData(UUID refset)
		{
			this.refset = refset;
		}

		/**
		 * @param cd
		 */
		protected void add(ContentDefinition cd)
		{
			unprocessed.add(cd);
		}

		protected void resolve()
		{

			Iterator<ContentDefinition> it = unprocessed.iterator();
			while (it.hasNext())
			{
				ContentDefinition cd = it.next();
				// The only entries in this list should be of this type:
				if (cd.getValueSetRef() == null)
				{
					throw new RuntimeException("Unexpected!");
				}

				VocabularyValueSetRef vsf = cd.getValueSetRef();
				UUID targetRefset = dwh.getRefsetType(vsf.getName());
				// vsf.getId();
				// vsf.getVersionDate();
				// vsf.getVersionTime();

				if (fullyCalculatedRefsetMembers.contains(targetRefset))
				{
					if (inProgressRefsetMembers.get(targetRefset) != null)
					{
						for (Integer targetMember : inProgressRefsetMembers.get(targetRefset))
						{
							Set<Integer> members = inProgressRefsetMembers.get(refset);
							if (members == null)
							{
								members = new HashSet<>();
								inProgressRefsetMembers.put(refset, members);
							}
							members.add(targetMember);
							valueSetMemberCount++;
						}
					}
					it.remove();
				}
			}
		}
	}
}