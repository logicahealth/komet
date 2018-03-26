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
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
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
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.convert.mojo.hl7v3.propertyTypes.PT_Annotations;
import sh.isaac.convert.mojo.hl7v3.propertyTypes.PT_Associations;
import sh.isaac.convert.mojo.hl7v3.propertyTypes.PT_Descriptions;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyAssociation;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicBooleanImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;

/**
 * @author a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         Goal which converts HL7v3 content into ISAAC
 */

@Mojo(name = "convert-hl7v3-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class HL7v3ImportMojo extends ConverterBaseMojo
{
	private UUID rootConceptUUID;

	private PropertyType attributes_, descriptions_, associations_;
	private BPT_Refsets refsets_;

//	private UUID allHL7v3ConceptsRefset;

	private static Logger log = LogManager.getLogger();

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

	@Override
	public void execute() throws MojoExecutionException
	{

		try
		{
			super.execute();

			complexMarkupMarshaller = JAXBContext.newInstance(ComplexMarkupWithLanguage.class).createMarshaller();
			complexMarkupMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			inlineMarshaller = JAXBContext.newInstance(Inline.class).createMarshaller();
			inlineMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			oidMatcher = Pattern.compile("(\\d+\\.)+\\d+");

			GlobalVocabularyModel gvm = readXML();

			if (PackageKind.VERSION != gvm.getPackageKind())
			{
				throw new MojoExecutionException("Expected 'packageKind' of " + PackageKind.VERSION.name());
			}

			String schemaVersion = gvm.getSchemaVersion();
			if (!"2.1.7".equals(schemaVersion) && !"2.1.6".equals(schemaVersion))  // I'm currently compiled against 2.1.6, have tested against 2.1.7.
			{
				throw new MojoExecutionException("Untested schema version: " + schemaVersion);
			}

			importUtil = new IBDFCreationUtility(Optional.of(HL7v3Constants.TERMINOLOGY_NAME + " " + converterSourceArtifactVersion),
					Optional.of(MetaData.HL7_V3_MODULES____SOLOR), outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion,
					converterOutputArtifactClassifier, false,
					gvm.getHeader().getRenderingInformation().getRenderingTime().toGregorianCalendar().getTimeInMillis());

			// TODO would be nice to automate this, could I use reflection to read the dynamic semantics from the class?
			importUtil.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_NID_EXTENSION.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_NID_EXTENSION.getDynamicColumns());
			importUtil.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMANTIC_TYPE.getDynamicColumns());
			importUtil.registerDynamicColumnInfo(IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getPrimordialUuid(),
					IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_STRING_EXTENSION.getDynamicColumns());

			attributes_ = new PT_Annotations(importUtil);
			descriptions_ = new PT_Descriptions();
			associations_ = new PT_Associations();
//			relationships_ = new BPT_Relations(HL7v3Constants.TERMINOLOGY_NAME);
			refsets_ = new BPT_Refsets(HL7v3Constants.TERMINOLOGY_NAME);
//			refsets_.addProperty("All HL7v3 Concepts");
//			allHL7v3ConceptsRefset = refsets_.getProperty("All HL7v3 Concepts").getUUID();

			// add property types for ConceptDomains in use
			attributes_.addProperty(new Property(null, ConceptDomainPropertyKind.CONCEPTUAL_SPACE_FOR_CLASS_CODE.value(), null,
					"Specifies the classCode for which the domain defines the conceptual space", false, Integer.MAX_VALUE,
					new DynamicColumnInfo[] { new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.NID,
							null, true, true) }));

			ComponentReference hl7v3Metadata = ComponentReference.fromConcept(createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(),
					HL7v3Constants.TERMINOLOGY_NAME + " Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			importUtil.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			HashMap<String, UUID> enumConstants = new HashMap<>();
			// Build concepts for enumerated types
			for (Reflexivity r : Reflexivity.values())
			{
				enumConstants.put(r.name(), createType(PT_Annotations.Attribute.REFLEXIVITY.getUUID(), r.name()).getPrimordialUuid());
			}
			for (Symmetry s : Symmetry.values())
			{
				enumConstants.put(s.name(), createType(PT_Annotations.Attribute.SYMMETRY.getUUID(), s.name()).getPrimordialUuid());
			}
			for (Transitivity t : Transitivity.values())
			{
				enumConstants.put(t.name(), createType(PT_Annotations.Attribute.TRANSITIVITY.getUUID(), t.name()).getPrimordialUuid());
			}
			for (ConceptRelationshipKind crk : ConceptRelationshipKind.values())
			{
				enumConstants.put(crk.name(), createType(PT_Annotations.Attribute.RELATIONSHIP_KIND.getUUID(), crk.name()).getPrimordialUuid());
			}

			for (ConceptPropertyTypeKind t : ConceptPropertyTypeKind.values())
			{
				enumConstants.put(t.name(), createType(PT_Annotations.Attribute.SUPPORTED_CONCEPT_PROPERTY_TYPE.getUUID(), t.name()).getPrimordialUuid());
			}

			// TODO this is sometimes used as an attribute on a supportedConceptProperty - need to handle those.
			for (PropertyDefaultHandlingKind t : PropertyDefaultHandlingKind.values())
			{
				enumConstants.put(t.name(), createType(PT_Annotations.Attribute.PROPERTY_DEFAULT_HANDLING_KIND.getUUID(), t.name()).getPrimordialUuid());
			}

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
//							known.getAllowedForSources();
//							known.getAllowedForTargets();
//							known.getDefiningConcept();
//							known.getId();
//							known.getReflexivity();
//							known.getRelationshipKind();
//							known.getRequiredForSources();
//							known.getRequiredForTargets();
//							known.getSupportedProperty();
//							known.getSymmetry();
//							known.getTransitivity();
//							known.isFunctionalism();
//							known.isIsNavigable();

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
								throw new MojoExecutionException("concept relationship defined differently: " + known.getName());
							}
						}
						else
						{
							associations_.addProperty(
									new PropertyAssociation(null, scr.getName(), null, scr.getInverseName(), flatten(scr.getDescription()), false));
							supportedConceptRelationshipsToSCR.put(scr.getName(), scr);

							// Need to annotate with the rest of the attributes, but have to wait until the metadata is loaded.
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
								throw new MojoExecutionException("concept property defined differently: " + known.getPropertyName());
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
								attributes_.addProperty(new Property(null, scp.getPropertyName(), null, flatten(scp.getDescription()), false, Integer.MAX_VALUE,
										new DynamicColumnInfo[] { new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(),
												DynamicDataType.STRING,
												StringUtils.isNotBlank(scp.getDefaultValue()) ? new DynamicStringImpl(scp.getDefaultValue()) : null, true,
												true) }));
								supportedConceptPropertiesToSCP.put(scp.getPropertyName(), scp);
								// TODO handle these
//								scp.isApplyToValueSetsIndicator();
//								scp.isIsMandatoryIndicator();
//								scp.getDefaultHandlingCode();
//								scp.getEnumerationValue();
//								scp.getType();
							}
						}
					}
				}
			}

			if (uniqueReleasedVersions.size() > 1)
			{
				ConsoleUtil.printErrorln("Don't currently handle multiple releasedVersions!");
				for (String s : uniqueReleasedVersions)
				{
					ConsoleUtil.printErrorln(s);
				}
				throw new MojoExecutionException("Failing due to previous errors");
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
					ConsoleUtil.printErrorln("Don't currently handle multiple value set releasedVersions!");
					for (String s : uniqueReleasedVersions)
					{
						ConsoleUtil.printErrorln(s);
					}
					throw new MojoExecutionException("Failing due to previous errors");
				}

				String in = valueSetNameToOID.put(vs.getName().toLowerCase(), vs.getId());
				if (in != null)
				{
					throw new MojoExecutionException("Non-unique value set name!");
				}

				refsets_.addProperty(new Property(null, vs.getName()));
			}

			importUtil.loadMetaDataItems(Arrays.asList(descriptions_, attributes_, associations_, refsets_), hl7v3Metadata.getPrimordialUuid());

			// add other attributes onto relationships
			for (SupportedConceptRelationship scr : supportedConceptRelationshipsToSCR.values())
			{
				ComponentReference relConcept = ComponentReference.fromConcept(associations_.getProperty(scr.getName()).getUUID());

				if (scr.getReflexivity() != null)
				{
					importUtil.addAnnotation(relConcept, null,
							new DynamicNidImpl(Get.identifierService().getNidForUuids(enumConstants.get(scr.getReflexivity().name()))),
							PT_Annotations.Attribute.REFLEXIVITY.getUUID(), Status.ACTIVE, null);
				}

				if (scr.getSymmetry() != null)
				{
					importUtil.addAnnotation(relConcept, null,
							new DynamicNidImpl(Get.identifierService().getNidForUuids(enumConstants.get(scr.getSymmetry().name()))),
							PT_Annotations.Attribute.SYMMETRY.getUUID(), Status.ACTIVE, null);
				}

				if (scr.getTransitivity() != null)
				{
					importUtil.addAnnotation(relConcept, null,
							new DynamicNidImpl(Get.identifierService().getNidForUuids(enumConstants.get(scr.getTransitivity().name()))),
							PT_Annotations.Attribute.TRANSITIVITY.getUUID(), Status.ACTIVE, null);
				}

				if (scr.getRelationshipKind() != null)
				{
					importUtil.addAnnotation(relConcept, null,
							new DynamicNidImpl(Get.identifierService().getNidForUuids(enumConstants.get(scr.getRelationshipKind().name()))),
							PT_Annotations.Attribute.RELATIONSHIP_KIND.getUUID(), Status.ACTIVE, null);
				}
				importUtil.addAnnotation(relConcept, null, new DynamicBooleanImpl(scr.isIsNavigable()), PT_Annotations.Attribute.IS_NAVIGABLE.getUUID(),
						Status.ACTIVE, null);
			}

			ConsoleUtil.println("Metadata load stats");
			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}

			importUtil.clearLoadStats();

			importUtil.addAnnotation(importUtil.getModule(), null,
					new DynamicData[] { new DynamicStringImpl(gvm.getName()), new DynamicStringImpl(gvm.getTitle()),
							new DynamicStringImpl(gvm.getPackageKind().value()), new DynamicStringImpl(gvm.getDefinitionKind().value()),
							new DynamicStringImpl(gvm.getSchemaVersion()) },
					PT_Annotations.Attribute.VOCABULARY_MODEL.getUUID(), null, null, null);

			DynamicStringImpl[] realms = new DynamicStringImpl[gvm.getPackageLocation().getRealmNamespace() == null ? 0
					: gvm.getPackageLocation().getRealmNamespace().size()];
			for (int i = 0; i < realms.length; i++)
			{
				realms[i] = new DynamicStringImpl(gvm.getPackageLocation().getRealmNamespace().get(i));
			}

			importUtil.addAnnotation(importUtil.getModule(), null, new DynamicData[] { new DynamicStringImpl(gvm.getPackageLocation().getCombinedId()),
					new DynamicStringImpl(gvm.getPackageLocation().getRoot().value()), new DynamicStringImpl(gvm.getPackageLocation().getArtifact().value()),
					new DynamicArrayImpl<DynamicString>(realms), new DynamicStringImpl(gvm.getPackageLocation().getVersion()) },
					PT_Annotations.Attribute.PACKAGE_LOCATION.getUUID(), null, null, null);

			importUtil.addAnnotation(importUtil.getModule(), null,
					new DynamicData[] { new DynamicStringImpl(gvm.getHeader().getRenderingInformation().getRenderingTime().toString()),
							new DynamicStringImpl(gvm.getHeader().getRenderingInformation().getApplication()) },
					PT_Annotations.Attribute.RENDERING_INFORMATION.getUUID(), null, null, null);

			DynamicStringImpl[] copyrightYears = new DynamicStringImpl[gvm.getHeader().getLegalese().getCopyrightYears() == null ? 0
					: gvm.getHeader().getLegalese().getCopyrightYears().size()];

			for (int i = 0; i < realms.length; i++)
			{
				copyrightYears[i] = new DynamicStringImpl(gvm.getHeader().getLegalese().getCopyrightYears().get(i) + "");
			}

			importUtil.addAnnotation(importUtil.getModule(), null, new DynamicData[] {
					new DynamicStringImpl(gvm.getHeader().getLegalese().getCopyrightOwner()), new DynamicArrayImpl<DynamicString>(copyrightYears) },
					PT_Annotations.Attribute.LEGALESE.getUUID(), null, null, null);

			rootConceptUUID = createType(MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid(), HL7v3Constants.TERMINOLOGY_NAME).getPrimordialUuid();

			UUID conceptDomains = createType(rootConceptUUID, "Concept Domains").getPrimordialUuid();

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
			ConsoleUtil.println("Processing Concept Domains");
			int conceptDomainCount = 0;
			int deprecatedConceptDomains = 0;

			for (ConceptDomain cd : gvm.getConceptDomain())
			{
				UUID domainUUID = createConceptDomainUUID(cd.getName());
				Status state = Status.ACTIVE;
				for (HistoryItem hi : cd.getHistoryItem())
				{
					boolean retiredFlag = handleHistoryItem(hi, ComponentReference.fromConcept(domainUUID));
					if (retiredFlag)
					{
						state = Status.INACTIVE;
					}
				}

				if (cd.getAnnotations() != null && cd.getAnnotations().getAppInfo() != null && cd.getAnnotations().getAppInfo().getDeprecationInfo() != null
						&& StringUtils.isNotBlank(cd.getAnnotations().getAppInfo().getDeprecationInfo().getDeprecationEffectiveVersion()))
				{
					deprecatedConceptDomains++;
					continue;
				}

				ComponentReference cr = ComponentReference.fromConcept(createType(conceptDomains, domainUUID, cd.getName(), state));
				conceptDomainCount++;
				if (conceptDomainCount % 100 == 0)
				{
					ConsoleUtil.showProgress();
				}

				if (cd.getAnnotations() != null && cd.getAnnotations().getDocumentation() != null
						&& cd.getAnnotations().getDocumentation().getDefinition() != null)
				{
					// TODO any other annotations
					flatten(cd.getAnnotations().getDocumentation().getDefinition().getText(), s -> {
						importUtil.addDescription(cr, s, DescriptionType.DEFINITION, false, descriptions_.getProperty("documentation").getUUID(), null);
					});
				}
				for (ConceptDomainRef sd : cd.getSpecializesDomain())
				{
					importUtil.addAssociation(cr, null, createConceptDomainUUID(sd.getName()), PT_Associations.Attribute.SPECIALIZES_DOMAIN.getUUID(), null,
							null, null);
				}

				for (ConceptDomainRef sbd : cd.getSpecializedByDomain())
				{
					importUtil.addAssociation(cr, null, createConceptDomainUUID(sbd.getName()), PT_Associations.Attribute.SPECIALIZED_BY_DOMAIN.getUUID(),
							null, null, null);
				}

				for (ConceptDomainProperty p : cd.getProperty())
				{
					Property property = attributes_.getProperty(p.getName().value());
					if (property == null)
					{
						throw new MojoExecutionException("Oops - no handler for " + p.getName().value());
					}

					// Split data like this to code system / code "ActClass.ADJUD"

					int split = p.getValue().indexOf('.');
					if (split < 0)
					{
						throw new MojoExecutionException("Oops: " + p.getValue());
					}

					importUtil.addAnnotation(cr, null,
							new DynamicNidImpl(Get.identifierService().getNidForUuids(
									createConceptCodeUUID(p.getValue().substring(0, split), p.getValue().substring((split + 1), p.getValue().length()), true))),
							property.getUUID(), null, null);
				}

				// TODO handle these
//				cd.isIsBindable();
//				cd.getExampleConcept();
//				cd.getBusinessName();
//				cd.getSortKey();
			}

			ConsoleUtil.println("Processed " + conceptDomainCount + " concept domains");
			ConsoleUtil.println("Skipped " + deprecatedConceptDomains + " deprecated concept domains");

			UUID codeSystems = createType(rootConceptUUID, "Code Systems").getPrimordialUuid();

			// Process the Code systems
			ConsoleUtil.println("Processing Code Systems");
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
					ConsoleUtil.showProgress();
				}

				ComponentReference codeSystem = ComponentReference
						.fromConcept(importUtil.createConcept(createCodeSystemUUID(cs.getName()), null, Status.ACTIVE, null));
				importUtil.addParent(codeSystem, codeSystems);
				importUtil.addDescription(codeSystem, cs.getName(), DescriptionType.FULLY_QUALIFIED_NAME, true, descriptions_.getProperty("name").getUUID(),
						null);
				importUtil.addDescription(codeSystem, cs.getTitle(), DescriptionType.REGULAR_NAME, true, descriptions_.getProperty("title").getUUID(), null);

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
						importUtil.addDescription(codeSystem, s, DescriptionType.DEFINITION, false, descriptions_.getProperty("description").getUUID(), null);
					});
				}

				importUtil.addStaticStringAnnotation(codeSystem, cs.getCodeSystemId(), PT_Annotations.Attribute.OID.getUUID(), Status.ACTIVE);

				for (CodeSystemVersion csv : cs.getReleasedVersion())
				{
					importUtil.addAnnotation(codeSystem, null,
							new DynamicData[] { new DynamicStringImpl(csv.getReleaseDate().toString()),
									csv.getPublisherVersionId() == null ? null : new DynamicStringImpl(csv.getPublisherVersionId()),
									new DynamicBooleanImpl(csv.isHl7MaintainedIndicator()), new DynamicBooleanImpl(csv.isCompleteCodesIndicator()),
									new DynamicBooleanImpl(csv.isHl7ApprovedIndicator()) },
							PT_Annotations.Attribute.RELEASED_VERSION.getUUID(), Status.ACTIVE, null, null);

					for (SupportedConceptRelationship scr : csv.getSupportedConceptRelationship())
					{
						importUtil.addAnnotation(codeSystem, null,
								new DynamicNidImpl(Get.identifierService().getNidForUuids(associations_.getProperty(scr.getName()).getUUID())),
								PT_Annotations.Attribute.SUPPORTED_CONCEPT_RELATIONSHIP.getUUID(), Status.ACTIVE, null);
					}
					for (SupportedConceptProperty scp : csv.getSupportedConceptProperty())
					{
						importUtil.addAnnotation(codeSystem, null,
								new DynamicData[] {
										new DynamicNidImpl(Get.identifierService().getNidForUuids(attributes_.getProperty(scp.getPropertyName()).getUUID())),
										new DynamicNidImpl(Get.identifierService().getNidForUuids(enumConstants.get(scp.getType().name()))),
										new DynamicBooleanImpl(scp.isIsMandatoryIndicator()), new DynamicBooleanImpl(scp.isApplyToValueSetsIndicator()),
										StringUtils.isBlank(scp.getDefaultValue()) ? null : new DynamicStringImpl(scp.getDefaultValue()) },
								PT_Annotations.Attribute.SUPPORTED_CONCEPT_PROPERTY.getUUID(), Status.ACTIVE, null, null);
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
						
						ComponentReference concept = ComponentReference.fromConcept(importUtil
								.createConcept(conceptUUID, additionalUUIDs.toArray(new UUID[additionalUUIDs.size()])));

						codeSystemCodePointers.add(conceptUUID);
						codeSystemConceptPointers.add(c);

						try
						{
							importUtil.addParent(concept, codeSystem.getPrimordialUuid());
						}
						catch (RuntimeException e)
						{
							ConsoleUtil.printErrorln("Duplicate code? " + cs.getName() + " " + c.getCode().get(0).getCode());
							throw e;
						}

						importUtil.addAnnotation(concept, null, new DynamicBooleanImpl(c.isIsSelectable()), PT_Annotations.Attribute.IS_SELECTABLE.getUUID(),
								Status.ACTIVE, null);

						// TODO the rest of the annotations nested annotation possibilities
						if (c.getAnnotations() != null && c.getAnnotations().getDocumentation() != null
								&& c.getAnnotations().getDocumentation().getDefinition() != null)
						{
							flatten(c.getAnnotations().getDocumentation().getDefinition().getText(), s -> {
								importUtil.addDescription(concept, null, s, DescriptionType.DEFINITION, false,
										descriptions_.getProperty("documentation").getUUID(), null);
							});

						}
						for (PrintName d : c.getPrintName())
						{
							// TODO icon
							if (!d.getLanguage().equals("en"))
							{
								throw new MojoExecutionException("unhandled language!");
							}
							importUtil.addDescription(concept, null, d.getText(), DescriptionType.REGULAR_NAME, d.isPreferredForLanguage(), null, null, null,
									null, descriptions_.getProperty("print name").getUUID(), null, null);
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
							importUtil.addDescription(concept, null, cc.getCode(), DescriptionType.FULLY_QUALIFIED_NAME, false,
									descriptions_.getProperty("code").getUUID(), null);
							importUtil.addStaticStringAnnotation(concept, cc.getCode(), PT_Annotations.Attribute.Code.getUUID(), Status.ACTIVE);
						}

						for (ConceptProperty cp : c.getConceptProperty())
						{
							Property p = attributes_.getProperty(cp.getName());
							if (p == null)
							{
								// Data bug...
								if (cp.getName().equals("Name:role:scoper:Entity"))
								{
									p = attributes_.getProperty("Name:Role:scoper:Entity");
								}
								else
								{
									throw new MojoExecutionException("Can't find attribute definition for '" + cp.getName() + "'");
								}
							}
							// TODO status concept property needs special handling
							if (p.getUUID().equals(PT_Annotations.Attribute.OID.getUUID()))
							{
								importUtil.addStaticStringAnnotation(concept, cp.getValue(), p.getUUID(), null);
							}
							else
							{
								importUtil.addAnnotation(concept, null, new DynamicStringImpl(cp.getValue()), p.getUUID(), null, null);
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
							importUtil.addAssociation(ComponentReference.fromConcept(createConceptCodeUUID(cs.getName(), c.getCode().get(0).getCode(), false)), null,
									createConceptCodeUUID(
											StringUtils.isBlank(r.getTargetConcept().getCodeSystem()) ? cs.getName() : r.getTargetConcept().getCodeSystem(),
											r.getTargetConcept().getCode(), true),
									associations_.getProperty(r.getRelationshipName()).getUUID(), null, null, null);
							// TODO handle these
							// r.getProperty()
							// r.isIsDerived();
						}
					}
					ConsoleUtil.println("Added " + conceptCount + " concepts to " + cs.getName());
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
				ComponentReference refset = ComponentReference.fromConcept(refsets_.getProperty(vs.getName()).getUUID());

				if (vs.getAnnotations() != null && vs.getAnnotations().getAppInfo() != null && vs.getAnnotations().getAppInfo().getDeprecationInfo() != null
						&& StringUtils.isNotBlank(vs.getAnnotations().getAppInfo().getDeprecationInfo().getDeprecationEffectiveVersion()))
				{
					skippedDeprecatedValueSets++;
					continue;
				}

				importUtil.addStaticStringAnnotation(refset, vs.getId(), PT_Annotations.Attribute.OID.getUUID(), Status.ACTIVE);

				if (vs.getAnnotations() != null && vs.getAnnotations().getDocumentation() != null
						&& vs.getAnnotations().getDocumentation().getDescription() != null)
				{
					// TODO any other annotations
					flatten(vs.getAnnotations().getDocumentation().getDescription().getText(), s -> {
						importUtil.addDescription(refset, s, DescriptionType.DEFINITION, false, descriptions_.getProperty("description").getUUID(), null);
					});
				}

				for (HistoryItem hi : vs.getHistoryItem())
				{
					handleHistoryItem(hi, refset);
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
					fullyCalculatedRefsetMembers.add(refset.getPrimordialUuid());
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
			// duplicates) 
			// populate our refsets.
			for (Entry<UUID, Set<Integer>> refsetData : inProgressRefsetMembers.entrySet())
			{
				for (Integer refsetMember : refsetData.getValue())
				{
					importUtil.addAssemblageMembership(ComponentReference.fromChronology(refsetMember), refsetData.getKey(), Status.ACTIVE, null);
					valueSetMemberCount++;
				}
			}

			ConsoleUtil.println("Created " + codeSystemCount + " code systems");
			ConsoleUtil.println("Skipped " + skippedEmptyCodeSystems + " code systems for being empty");
			ConsoleUtil.println("Skipped " + skippedDeprecatedCodeSystems + " code systems for being deprecated");
			ConsoleUtil.println("Skipped " + skippedDeprecatedValueSets + " value sets for being deprecated");
			ConsoleUtil.println("Created " + totalConceptCount + " concepts");
			ConsoleUtil.println("Created " + valueSetCount + " value sets");
			ConsoleUtil.println("Created " + valueSetMemberCount + " value set memebers");

			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(outputDirectory, "hl7v3Uuid");

			ConsoleUtil.println("Load stats");
			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}

			importUtil.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception ex)
		{
			System.out.println("Dieing .... dumping UUID debug file: ");
			try
			{
				ConverterUUID.dump(outputDirectory, "vhatUuid");
			}
			catch (IOException e)
			{
				System.out.println("Failed dumping debug file...");
			}
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * Returns true, if this was fully resolved, false, if it could not yet be fully resolved.
	 */
	private boolean addContentDefinitionToRefSet(ContentDefinition cd, ComponentReference refset) throws MojoExecutionException
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
						Set<Integer> members = inProgressRefsetMembers.get(Get.identifierService().getNidForUuids(refset.getPrimordialUuid()));
						if (members == null)
						{
							members = new HashSet<>();
							inProgressRefsetMembers.put(refset.getPrimordialUuid(), members);
						}
						members.add(member);
					}
				}
				if (cbcd.isIncludeHeadCode())
				{

					Integer refsetMember = Get.identifierService().getNidForUuids(createConceptCodeUUID(codeSystemOid, cbcd.getCode(), true));
					Set<Integer> members = inProgressRefsetMembers.get(refset.getPrimordialUuid());
					if (members == null)
					{
						members = new HashSet<>();
						inProgressRefsetMembers.put(refset.getPrimordialUuid(), members);
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
			ComponentReference targetRefset = ComponentReference.fromConcept(refsets_.getProperty(vsf.getName()).getUUID());
//			vsf.getId();
//			vsf.getVersionDate();
//			vsf.getVersionTime();

			if (fullyCalculatedRefsetMembers.contains(targetRefset.getPrimordialUuid()))
			{
				if (inProgressRefsetMembers.get(targetRefset.getPrimordialUuid()) == null)
				{
					System.out.println("No members for targetRefset: " + vsf.getName() + "?");
				}
				else
				{
					for (Integer targetMember : inProgressRefsetMembers.get(targetRefset.getPrimordialUuid()))
					{
						Set<Integer> members = inProgressRefsetMembers.get(refset.getPrimordialUuid());
						if (members == null)
						{
							members = new HashSet<>();
							inProgressRefsetMembers.put(refset.getPrimordialUuid(), members);
						}
						members.add(targetMember);
					}
				}
			}
			else
			{
				fullyResolved = false;
				IncompleteValueSetData ivsd = incompleteRefsetData.get(refset.getPrimordialUuid());
				if (ivsd == null)
				{
					ivsd = new IncompleteValueSetData(refset);
					incompleteRefsetData.put(refset.getPrimordialUuid(), ivsd);
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
				Set<Integer> members = inProgressRefsetMembers.get(refset.getPrimordialUuid());
				if (members == null)
				{
					members = new HashSet<>();
					inProgressRefsetMembers.put(refset.getPrimordialUuid(), members);
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

	private String flatten(Inline description) throws JAXBException
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
	private GlobalVocabularyModel readXML() throws JAXBException, MojoExecutionException, IOException
	{
		File zipFile = null;
		for (File f : inputFileLocation.listFiles())
		{
			if (f.getName().toLowerCase().endsWith(".zip"))
			{
				if (zipFile != null)
				{
					throw new MojoExecutionException("Only expected to find one zip file in the folder " + inputFileLocation.getCanonicalPath());
				}
				zipFile = f;
			}
		}

		if (zipFile == null)
		{
			throw new MojoExecutionException("Did not find a zip file in " + inputFileLocation.getCanonicalPath());
		}

		try (ZipFile zf = new ZipFile(zipFile);)
		{
			Enumeration<? extends ZipEntry> zipEntries = zf.entries();
			ZipEntry ze = null;
			while (zipEntries.hasMoreElements())
			{
				ZipEntry zet = zipEntries.nextElement();
				if (zet.getName().toUpperCase().contains("DEFN=UV=VO") && zet.getName().toLowerCase().endsWith(".coremif"))
				{
					// The data file we want to load.
					if (ze != null)
					{
						throw new MojoExecutionException("Found more than one .coremif file matching the prefix pattern DEFN=UV=VO in the source content");
					}
					ze = zet;
				}
			}

			if (ze == null)
			{
				throw new MojoExecutionException("Failed to find a .coremif file matching the prefix pattern DEFN=UV=VO in the source content");
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

			log.info("Parsing " + ze.getName() + " from " + new File(zf.getName()).getCanonicalPath());

			try (InputStream in = zf.getInputStream(ze);)
			{
				@SuppressWarnings("unchecked")
				JAXBElement<GlobalVocabularyModel> o = (JAXBElement<GlobalVocabularyModel>) unmarshaller.unmarshal(in);
				return o.getValue();
			}
		}
	}

	private boolean handleHistoryItem(HistoryItem hi, ComponentReference ref)
	{
		boolean retiredMarker = false;
		String description = flatten(hi.getDescription());
		if (description.contains("retiredAsOfRelease:"))
		{
			retiredMarker = true;
		}

		importUtil.addAnnotation(ref, null,
				new DynamicData[] { hi.getDateTime() == null ? null : new DynamicStringImpl(hi.getDateTime()),
						hi.getResponsiblePersonName() == null ? null : new DynamicStringImpl(hi.getResponsiblePersonName()),
						hi.getId() == null ? null : new DynamicStringImpl(hi.getId()),
						hi.isIsSubstantiveChange() == null ? null : new DynamicBooleanImpl(hi.isIsSubstantiveChange().booleanValue()),
						hi.isIsBackwardCompatibleChange() == null ? null : new DynamicBooleanImpl(hi.isIsBackwardCompatibleChange().booleanValue()),
						StringUtils.isBlank(description) ? null : new DynamicStringImpl(description) },
				PT_Annotations.Attribute.HISTORY_ITEM.getUUID(), Status.ACTIVE, null, null);
		return retiredMarker;
	}

	private ConceptVersion createType(UUID parentUuid, UUID primordial, String typeName, Status state) throws Exception
	{
		ConceptVersion concept = importUtil.createConcept(primordial, typeName, true, null, state);
		// loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	private ConceptVersion createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptVersion concept = importUtil.createConcept(typeName, true);
		// loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	private UUID createConceptDomainUUID(String name)
	{
		UUID temp =  ConverterUUID.createNamespaceUUIDFromString("ConceptDomain|" + name, true);
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
		UUID temp = ConverterUUID.createNamespaceUUIDFromString("Code|" + oid + "|" + name, true);
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
		return ConverterUUID.createNamespaceUUIDFromString("CodeSystem|" + oid, true);
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
		ComponentReference refset;

		/**
		 * @param refset
		 */
		protected IncompleteValueSetData(ComponentReference refset)
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
				ComponentReference targetRefset = ComponentReference.fromConcept(refsets_.getProperty(vsf.getName()).getUUID());
				// vsf.getId();
				// vsf.getVersionDate();
				// vsf.getVersionTime();

				if (fullyCalculatedRefsetMembers.contains(targetRefset.getPrimordialUuid()))
				{
					if (inProgressRefsetMembers.get(targetRefset.getPrimordialUuid()) != null)
					{
						for (Integer targetMember : inProgressRefsetMembers.get(targetRefset.getPrimordialUuid()))
						{
							Set<Integer> members = inProgressRefsetMembers.get(refset.getPrimordialUuid());
							if (members == null)
							{
								members = new HashSet<>();
								inProgressRefsetMembers.put(refset.getPrimordialUuid(), members);
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

	public static void main(String[] args) throws MojoExecutionException
	{
		HL7v3ImportMojo i = new HL7v3ImportMojo();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocation= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
		i.converterOutputArtifactVersion = "2.47.1-1.0-SNAPSHOT";
		i.converterVersion = "1.0-SNAPSHOT";
		i.converterSourceArtifactVersion = "2.47.1";
		i.execute();
		javafx.application.Platform.exit();
	}
}