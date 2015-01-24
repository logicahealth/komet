/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
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
package gov.va.oia.terminology.converters.sharedUtils;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Descriptions;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_MemberRefsets;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Relations;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Skip;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import gov.va.oia.terminology.converters.sharedUtils.stats.ConverterUUID;
import gov.va.oia.terminology.converters.sharedUtils.stats.LoadStats;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierLong;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierString;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;

/**
 * 
 * {@link EConceptUtility}
 * 
 * Various constants and methods for building up workbench TtkConceptChronicles.
 * 
 * A much easier interfaces to use than trek - takes care of boilerplate stuff for you.
 * Also, forces consistency in how things are converted.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class EConceptUtility
{
	public static enum DescriptionType{FSN, SYNONYM, DEFINITION};
	public static final UUID isARelUuid_ = Snomed.IS_A.getUuids()[0];
	public final UUID authorUuid_ = TermAux.USER.getUuids()[0];
	public final static UUID synonymUuid_ = Snomed.SYNONYM_DESCRIPTION_TYPE.getUuids()[0];
	public final static UUID definitionUuid_ = Snomed.DEFINITION_DESCRIPTION_TYPE.getUuids()[0];
	public final static UUID fullySpecifiedNameUuid_ = Snomed.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUuids()[0];
	public final static UUID descriptionAcceptableUuid_ = SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()[0];
	public final static UUID descriptionPreferredUuid_ = SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0];
	public final static UUID usEnRefsetUuid_ = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getUuids()[0];
	public final UUID definingCharacteristicUuid_ = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()[0];
	public final UUID notRefinableUuid = SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids()[0];
	public final UUID moduleUuid_ = TtkRevision.unspecifiedModuleUuid;
	public final static UUID refsetMemberTypeNormalMemberUuid_ = UUID.fromString("cc624429-b17d-4ac5-a69e-0b32448aaf3c"); //normal member
	public final String PROJECT_REFSETS_NAME = "Project Refsets";
	public final UUID PROJECT_REFSETS_UUID = UUID.fromString("7fe3e31f-a969-53ff-8702-f7837e4a03d9");  //This is UuidT5Generator.PATH_ID_FROM_FS_DESC, "Project Refsets")
	public final static UUID pathOriginRefSetUUID_ = TermAux.PATH_ORIGIN_REFSET.getUuids()[0];
	public final static UUID pathRefSetUUID_ = TermAux.PATH_REFSET.getUuids()[0];
	public final UUID pathUUID_ = TermAux.PATH.getUuids()[0];
	public final UUID pathReleaseUUID_ =  UUID.fromString("88f89cc0-1d94-34a4-85ed-aa1949079314");
	public final UUID workbenchAuxilary = TermAux.WB_AUX_PATH.getUuids()[0];
	public final long defaultTime_;
	
	private UUID wbPropertyMetadataDescUUID = null;
	private UUID wbPropertyMetadataRelUUID = null;
	
	private final String lang_ = "en";
	private UUID terminologyPathUUID_ = workbenchAuxilary;  //start with this.

	private LoadStats ls_ = new LoadStats();

	/**
	 * Creates and stores the path concept - sets up the various namespace details.
	 * @param namespaceSeed The string to use for seeing the UUID generator for this namespace
	 * @param pathName The name to use for the concept that will be created as the 'path' concept
	 * @param defaultTime - the timestamp to place on created elements, when no other timestamp is specified on the element itself.
	 * @param dos - location to write the output
	 * @throws Exception
	 */
	public EConceptUtility(String namespaceSeed, String pathName, DataOutputStream dos, long defaultTime) throws Exception
	{
		ConverterUUID.addMapping("isA", isARelUuid_);
		ConverterUUID.addMapping("Synonym", synonymUuid_);
		ConverterUUID.addMapping("Fully Specified Name", fullySpecifiedNameUuid_);
		ConverterUUID.addMapping("US English Refset", usEnRefsetUuid_);
		ConverterUUID.addMapping("Path reference set", pathRefSetUUID_);
		ConverterUUID.addMapping("Path origin reference set", pathOriginRefSetUUID_);
		
		defaultTime_ = defaultTime;
		
		UUID namespace = ConverterUUID.createNamespaceUUIDFromString(null, namespaceSeed);
		ConverterUUID.configureNamespace(namespace);
		
		//Start our creating our path concept, by hanging it under path/release
		//Note, this concept gets created on WorkbenchAuxiliary path.
		//need to gen the UUID on the special namespace, so it can be targeted from assembly pom
		TtkConceptChronicle c = createConcept(ConverterUUID.createNamespaceUUIDFromString(UuidT5Generator.PATH_ID_FROM_FS_DESC, pathName), pathName, pathReleaseUUID_);  
		addDescription(c, pathName, DescriptionType.SYNONYM, true, null, null, Status.ACTIVE);  //Need a synonym as well, to be able to target from assembly pom
		c.writeExternal(dos);
		
		//Add it to the pathOriginRefSet, done on workbenchAux path.
		TtkConceptChronicle pathOriginRefsetConcept = createConcept(pathOriginRefSetUUID_);
		//Max value will be displayed as 'latest'.  Why on earth we are using an int for a time value, I have no idea.
		addRefsetMember(pathOriginRefsetConcept, c.getPrimordialUuid(), workbenchAuxilary, Integer.MAX_VALUE, Status.ACTIVE, null);
		pathOriginRefsetConcept.writeExternal(dos);
		
		//Also, add it to the pathRefset.  Also done on WorkbenchAux path.
		TtkConceptChronicle pathRefsetConcept = createConcept(pathRefSetUUID_);
		addRefsetMember(pathRefsetConcept, pathUUID_, c.getPrimordialUuid(), Status.ACTIVE, null);
		pathRefsetConcept.writeExternal(dos);
		
		terminologyPathUUID_ = c.getPrimordialUuid();  //Now change the path to our new path concept
		ConsoleUtil.println("The path to be specified in the workbench baseline pom (or assembly pom) is '" + pathName + "' - " + terminologyPathUUID_);
	}

	/**
	 * Create a concept, automatically setting as many fields as possible (adds a description, calculates
	 * the UUID, status current, etc)
	 */
	public TtkConceptChronicle createConcept(String preferredDescription)
	{
		return createConcept(ConverterUUID.createNamespaceUUIDFromString(preferredDescription), preferredDescription);
	}

	/**
	 * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
	 */
	public TtkConceptChronicle createConcept(String name, UUID parentConceptPrimordial)
	{
		TtkConceptChronicle concept = createConcept(name);
		addRelationship(concept, parentConceptPrimordial);
		return concept;
	}

	/**
	 * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
	 */
	public TtkConceptChronicle createConcept(UUID conceptPrimordialUuid, String name, UUID relParentPrimordial)
	{
		TtkConceptChronicle concept = createConcept(conceptPrimordialUuid, name);
		addRelationship(concept, relParentPrimordial);
		return concept;
	}
	
	public TtkConceptChronicle createConcept(UUID conceptPrimordialUuid)
	{
		return createConcept(conceptPrimordialUuid, (Long)null, Status.ACTIVE);
	}

	/**
	 * Create a concept, automatically setting as many fields as possible (adds a description (en US)
	 * status current, etc
	 */
	public TtkConceptChronicle createConcept(UUID conceptPrimordialUuid, String preferredDescription)
	{
		return createConcept(conceptPrimordialUuid, preferredDescription, null, Status.ACTIVE);
	}

	/**
	 * Create a concept, automatically setting as many fields as possible (adds a description (en US))
	 * 
	 * @param time - set to now if null
	 */
	public TtkConceptChronicle createConcept(UUID conceptPrimordialUuid, String preferredDescription, Long time, Status status)
	{
		TtkConceptChronicle TtkConceptChronicle = createConcept(conceptPrimordialUuid, time, status);
		addFullySpecifiedName(TtkConceptChronicle, preferredDescription);
		return TtkConceptChronicle;
	}

	/**
	 * Just create a concept and the nested conceptAttributes.
	 * 
	 * @param conceptPrimordialUuid
	 * @param time - if null, set to default
	 * @param status - if null, set to current
	 * @return
	 */
	public TtkConceptChronicle createConcept(UUID conceptPrimordialUuid, Long time, Status status)
	{
		TtkConceptChronicle TtkConceptChronicle = new TtkConceptChronicleWrapper(this);
		TtkConceptChronicle.setPrimordialUuid(conceptPrimordialUuid);
		TtkConceptAttributesChronicle conceptAttributes = new TtkConceptAttributesChronicle();
		conceptAttributes.setDefined(false);
		conceptAttributes.setPrimordialComponentUuid(conceptPrimordialUuid);
		setRevisionAttributes(conceptAttributes, status, time);
		TtkConceptChronicle.setConceptAttributes(conceptAttributes);
		ls_.addConcept();
		return TtkConceptChronicle;
	}
	
	/**
	 * Clones the minimum required items for creating and merging a concept - except the path - path is set per normal 
	 */
	public TtkConceptChronicle createSkeletonClone(TtkConceptChronicle cloneSource)
	{
		if (cloneSource == null)
		{
			return null;
		}
		TtkConceptChronicle TtkConceptChronicle = new TtkConceptChronicleWrapper(this);
		TtkConceptChronicle.setPrimordialUuid(cloneSource.getPrimordialUuid());
		TtkConceptAttributesChronicle conceptAttributes = new TtkConceptAttributesChronicle();
		if (cloneSource.getConceptAttributes() != null)
		{
			conceptAttributes.setDefined(cloneSource.getConceptAttributes().isDefined());
			conceptAttributes.setPrimordialComponentUuid(cloneSource.getConceptAttributes().getPrimordialComponentUuid());
			conceptAttributes.setAuthorUuid(cloneSource.getConceptAttributes().getAuthorUuid());
			conceptAttributes.setModuleUuid(cloneSource.getConceptAttributes().getModuleUuid());
			conceptAttributes.setStatus(cloneSource.getConceptAttributes().getStatus());
			conceptAttributes.setTime(cloneSource.getConceptAttributes().getTime());
		}
		conceptAttributes.setPathUuid(terminologyPathUUID_);
		TtkConceptChronicle.setConceptAttributes(conceptAttributes);
		ls_.addConceptClone();
		return TtkConceptChronicle;
	}

	/**
	 * Create a concept with a UUID set from "Project Refsets" (PROJECT_REFSETS_UUID) and a name of "Project Refsets" (PROJECT_REFSETS_NAME)
	 * nested under ConceptConstants.REFSET
	 */
	private TtkConceptChronicle createVARefsetRootConcept()
	{
		return createConcept(PROJECT_REFSETS_UUID, PROJECT_REFSETS_NAME, TermAux.REFSET_IDENTITY.getUuids()[0]);
	}

	/**
	 * Add a workbench official "Fully Specified Name".  Convenience method for adding a description of type FSN
	 */
	public TtkDescriptionChronicle addFullySpecifiedName(TtkConceptChronicle TtkConceptChronicle, String fullySpecifiedName)
	{
		return addDescription(TtkConceptChronicle, fullySpecifiedName, DescriptionType.FSN, true, null, null, Status.ACTIVE);
	}
	
	
	/**
	 * Add a batch of WB descriptions, following WB rules in always generating a FSN (picking the value based on the propertySubType order). 
	 * And then adding other types as specified by the propertySubType value, setting preferred / acceptable according to their ranking. 
	 */
	public List<TtkDescriptionChronicle> addDescriptions(TtkConceptChronicle TtkConceptChronicle, List<? extends ValuePropertyPair> descriptions)
	{
		ArrayList<TtkDescriptionChronicle> result = new ArrayList<>(descriptions.size());
		Collections.sort(descriptions);
		
		boolean haveFSN = false;
		boolean havePreferredSynonym = false;
		boolean havePreferredDefinition = false;
		for (ValuePropertyPair vpp : descriptions)
		{
			DescriptionType descriptionType = null;
			boolean preferred;
			
			if (!haveFSN)
			{
				descriptionType = DescriptionType.FSN;
				preferred = true;
				haveFSN = true;
			}
			else
			{
				if (vpp.getProperty().getPropertySubType() < BPT_Descriptions.SYNONYM)
				{
					descriptionType = DescriptionType.FSN;
					preferred = false;  //true case is handled above
				}
				else if (vpp.getProperty().getPropertySubType() >= BPT_Descriptions.SYNONYM && 
						(vpp.getProperty().getPropertySubType() < BPT_Descriptions.DEFINITION || vpp.getProperty().getPropertySubType() == Integer.MAX_VALUE))
				{
					descriptionType = DescriptionType.SYNONYM;
					if (!havePreferredSynonym)
					{
						preferred = true;
						havePreferredSynonym = true;
					}
					else
					{
						preferred = false;
					}
				}
				else if (vpp.getProperty().getPropertySubType() >= BPT_Descriptions.DEFINITION)
				{
					descriptionType = DescriptionType.DEFINITION;
					if (!havePreferredDefinition)
					{
						preferred = true;
						havePreferredDefinition = true;
					}
					else
					{
						preferred = false;
					}
				}
				else
				{
					throw new RuntimeException("Unexpected error");
				}
			}
			
			if (!(vpp.getProperty().getPropertyType() instanceof BPT_Descriptions))
			{
				throw new RuntimeException("This method requires properties that have a parent that are an instance of BPT_Descriptions");
			}
			BPT_Descriptions descPropertyType = (BPT_Descriptions) vpp.getProperty().getPropertyType();
			
			result.add(addDescription(TtkConceptChronicle, vpp.getUUID(), vpp.getValue(), descriptionType, preferred, vpp.getProperty().getUUID(), 
					descPropertyType.getPropertyTypeReferenceSetUUID(), (vpp.isDisabled() ? Status.INACTIVE : Status.ACTIVE)));
		}
		
		return result;
	}
	
	/**
	 * Add a description to the concept.  UUID for the description is calculated from the target concept, description value, type, and preferred flag.
	 */
	public TtkDescriptionChronicle addDescription(TtkConceptChronicle TtkConceptChronicle, String descriptionValue, DescriptionType wbDescriptionType, 
			boolean preferred, UUID sourceDescriptionTypeUUID, UUID sourceDescriptionRefsetUUID, Status status)
	{
		return addDescription(TtkConceptChronicle, null, descriptionValue, wbDescriptionType, preferred, sourceDescriptionTypeUUID, sourceDescriptionRefsetUUID, status);
	}

	/**
	 * Add a description to the concept.
	 * 
	 * @param descriptionPrimordialUUID - if not supplied, created from the concept UUID, the description value, the description type, and preferred flag
	 * and the sourceDescriptionTypeUUID (if present)
	 * @param sourceDescriptionTypeUUID - if null, set to "member"
	 * @param sourceDescriptionRefsetUUID - if null, this and sourceDescriptionTypeUUID are ignored.
	 */
	public TtkDescriptionChronicle addDescription(TtkConceptChronicle ttkConceptChronicle, UUID descriptionPrimordialUUID, String descriptionValue, 
			DescriptionType wbDescriptionType, boolean preferred, UUID sourceDescriptionTypeUUID, UUID sourceDescriptionRefsetUUID, Status status)
	{
		List<TtkDescriptionChronicle> descriptions = ttkConceptChronicle.getDescriptions();
		if (descriptions == null)
		{
			descriptions = new ArrayList<TtkDescriptionChronicle>();
			ttkConceptChronicle.setDescriptions(descriptions);
		}
		TtkDescriptionChronicle description = new TtkDescriptionChronicle();
		description.setConceptUuid(ttkConceptChronicle.getPrimordialUuid());
		description.setLang(lang_);
		if (descriptionPrimordialUUID == null)
		{
			descriptionPrimordialUUID = ConverterUUID.createNamespaceUUIDFromStrings(ttkConceptChronicle.getPrimordialUuid().toString(), descriptionValue, 
					wbDescriptionType.name(), preferred + "", (sourceDescriptionTypeUUID == null ? null : sourceDescriptionTypeUUID.toString()));
		}
		description.setPrimordialComponentUuid(descriptionPrimordialUUID);
		UUID descriptionTypeUuid = null;
		if (DescriptionType.FSN == wbDescriptionType)
		{
			descriptionTypeUuid = fullySpecifiedNameUuid_;
		}
		else if (DescriptionType.SYNONYM == wbDescriptionType)
		{
			descriptionTypeUuid = synonymUuid_;
		}
		else if (DescriptionType.DEFINITION == wbDescriptionType)
		{
			descriptionTypeUuid = definitionUuid_;
		}
		else
		{
			throw new RuntimeException("Unsupported descriptiontype '" + wbDescriptionType + "'");
		}
		description.setTypeUuid(descriptionTypeUuid);
		description.setText(descriptionValue);
		setRevisionAttributes(description, status, ttkConceptChronicle.getConceptAttributes().getTime());

		descriptions.add(description);
		//Add the en-us info
		addUuidAnnotation(description, (preferred ? descriptionPreferredUuid_ : descriptionAcceptableUuid_), usEnRefsetUuid_);
		
		if (sourceDescriptionRefsetUUID != null)
		{
			addUuidAnnotation(description, sourceDescriptionTypeUUID, sourceDescriptionRefsetUUID);
		}
		
		ls_.addDescription(wbDescriptionType.name() + (sourceDescriptionTypeUUID == null ? (sourceDescriptionRefsetUUID == null ? "" : ":-member-:") :
				":" + getOriginStringForUuid(sourceDescriptionTypeUUID) + ":")
					+ (sourceDescriptionRefsetUUID == null ? "" : getOriginStringForUuid(sourceDescriptionRefsetUUID)));
		return description;
	}

	public TtkIdentifier addAdditionalIds(TtkConceptChronicle TtkConceptChronicle, Object id, UUID idTypeUuid, Status status)
	{
		if (id != null)
		{
			List<TtkIdentifier> additionalIds = TtkConceptChronicle.getConceptAttributes().getAdditionalIdComponents();
			if (additionalIds == null)
			{
				additionalIds = new ArrayList<TtkIdentifier>();
				TtkConceptChronicle.getConceptAttributes().setAdditionalIdComponents(additionalIds);
			}

			// create the identifier and add it to the additional ids list
			TtkIdentifier cid;
			if (id instanceof String)
			{
				cid = new TtkIdentifierString();
			}
			else if (id instanceof Long)
			{
				cid = new TtkIdentifierLong();
			}
			else if (id instanceof UUID)
			{
				cid = new TtkIdentifierLong();
			}
			else
			{
				throw new RuntimeException("Unsupported identifier type - must be String, Long or UUID");
			}
			additionalIds.add(cid);

			// populate the type
			cid.setAuthorityUuid(idTypeUuid);

			// populate the actual value of the identifier
			cid.setDenotation(id);

			setRevisionAttributes(cid, status, TtkConceptChronicle.getConceptAttributes().getTime());

			ls_.addConceptId(getOriginStringForUuid(idTypeUuid));
			return cid;
		}
		return null;
	}

	public TtkIdentifier addAdditionalIds(TtkComponentChronicle<?> component, Object id, UUID idTypeUuid)
	{
		if (id != null)
		{
			List<TtkIdentifier> additionalIds = component.getAdditionalIdComponents();
			if (additionalIds == null)
			{
				additionalIds = new ArrayList<TtkIdentifier>();
				component.setAdditionalIdComponents(additionalIds);
			}

			// create the identifier and add it to the additional ids list
			TtkIdentifier cid;
			if (id instanceof String)
			{
				cid = new TtkIdentifierString();
			}
			else if (id instanceof Long)
			{
				cid = new TtkIdentifierLong();
			}
			else if (id instanceof UUID)
			{
				cid = new TtkIdentifierLong();
			}
			else
			{
				throw new RuntimeException("Unsupported identifier type - must be String, Long or UUID");
			}
			additionalIds.add(cid);

			// populate the type
			cid.setAuthorityUuid(idTypeUuid);

			// populate the actual value of the identifier
			cid.setDenotation(id);

			setRevisionAttributes(cid, Status.ACTIVE, component.getTime());

			String label;
			if (component instanceof TtkDescriptionChronicle)
			{
				label = "Description";
			}
			else
			{
				label = component.getClass().getSimpleName();
			}

			ls_.addComponentId(label, getOriginStringForUuid(idTypeUuid));
			return cid;
		}
		return null;
	}

	/**
	 * Generated the UUID, uses the concept time
	 */
	public TtkRefexStringMemberChronicle addStringAnnotation(TtkConceptChronicle TtkConceptChronicle, String annotationValue, UUID refsetUuid, Status status)
	{
		return addStringAnnotation(TtkConceptChronicle.getConceptAttributes(), annotationValue, refsetUuid, status);
	}

	/**
	 * uses the concept time, UUID is created from the component UUID, the annotation value and type.
	 */
	public TtkRefexStringMemberChronicle addStringAnnotation(TtkComponentChronicle<?> component, String annotationValue, UUID refsetUuid, Status status)
	{
		return addStringAnnotation(component, null, annotationValue, refsetUuid, status, null);
	}

	/**
	 * @param annotationPrimordialUuid - if null, generated from component UUID, value, type UUID
	 * @param time - if null, uses the component time.
	 */
	public TtkRefexStringMemberChronicle addStringAnnotation(TtkComponentChronicle<?> component, UUID annotationPrimordialUuid, String value, UUID refsetUuid, 
			Status status, Long time)
	{
		List<TtkRefexAbstractMemberChronicle<?>> annotations = component.getAnnotations();

		if (annotations == null)
		{
			annotations = new ArrayList<TtkRefexAbstractMemberChronicle<?>>();
			component.setAnnotations(annotations);
		}

		if (value != null)
		{
			TtkRefexStringMemberChronicle strRefexMember = new TtkRefexStringMemberChronicle();

			strRefexMember.setComponentUuid(component.getPrimordialComponentUuid());
			strRefexMember.setString1(value);
			if (annotationPrimordialUuid == null)
			{
				annotationPrimordialUuid = ConverterUUID.createNamespaceUUIDFromStrings(component.getPrimordialComponentUuid().toString(), 
						value, refsetUuid.toString());
			}
			strRefexMember.setPrimordialComponentUuid(annotationPrimordialUuid);
			strRefexMember.setRefexExtensionUuid(refsetUuid);
			setRevisionAttributes(strRefexMember, status, (time == null ? component.getTime() : time));
			annotations.add(strRefexMember);

			annotationLoadStats(component, refsetUuid);
			return strRefexMember;
		}
		return null;
	}

	/**
	 * uses the component time, creates the UUID from the component UUID, the value UUID, and the type UUID.
	 * 
	 * @param valueConcept - if value is null, it uses RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid()
	 */
	public TtkRefexUuidMemberChronicle addUuidAnnotation(TtkComponentChronicle<?> component, UUID valueConcept, UUID refsetUuid)
	{
		return addUuidAnnotation(component, null, valueConcept, refsetUuid, Status.ACTIVE, null);
	}
	
	/**
	 * Generates the UUID, uses the component time
	 * 
	 * @param valueConcept - if value is null, it uses RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid()
	 */
	public TtkRefexUuidMemberChronicle addUuidAnnotation(TtkConceptChronicle concept, UUID valueConcept, UUID refsetUuid)
	{
		return addUuidAnnotation(concept.getConceptAttributes(), valueConcept, refsetUuid);
	}

	/**
	 * annotationPrimordialUuid - if null, generated from component UUID, value, type
	 * @param time - If time is null, uses the component time.
	 * @param valueConcept - if value is null, it uses RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid()
	 */
	public TtkRefexUuidMemberChronicle addUuidAnnotation(TtkComponentChronicle<?> component, UUID annotationPrimordialUuid, UUID valueConcept, UUID refsetUuid, 
			Status status, Long time)
	{
		List<TtkRefexAbstractMemberChronicle<?>> annotations = component.getAnnotations();

		if (annotations == null)
		{
			annotations = new ArrayList<TtkRefexAbstractMemberChronicle<?>>();
			component.setAnnotations(annotations);
		}

		TtkRefexUuidMemberChronicle conceptRefexMember = new TtkRefexUuidMemberChronicle();

		conceptRefexMember.setComponentUuid(component.getPrimordialComponentUuid());
		if (annotationPrimordialUuid == null)
		{
			annotationPrimordialUuid = ConverterUUID.createNamespaceUUIDFromStrings(component.getPrimordialComponentUuid().toString(), 
					(valueConcept == null ? refsetMemberTypeNormalMemberUuid_ : valueConcept).toString(), refsetUuid.toString());
		}
		conceptRefexMember.setPrimordialComponentUuid(annotationPrimordialUuid);
		conceptRefexMember.setUuid1(valueConcept == null ? refsetMemberTypeNormalMemberUuid_ : valueConcept);
		conceptRefexMember.setRefexExtensionUuid(refsetUuid);
		setRevisionAttributes(conceptRefexMember, status, (time == null ? component.getTime() : time));

		annotations.add(conceptRefexMember);

		annotationLoadStats(component, refsetUuid);
		return conceptRefexMember;
	}

	private void annotationLoadStats(TtkComponentChronicle<?> component, UUID refsetUuid)
	{
		if (component instanceof TtkConceptAttributesChronicle)
		{
			ls_.addAnnotation("Concept", getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TtkDescriptionChronicle)
		{
			ls_.addAnnotation("Description", getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TtkRelationshipChronicle)
		{
			ls_.addAnnotation(getOriginStringForUuid(((TtkRelationshipChronicle) component).getTypeUuid()), getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TtkRefexStringMemberChronicle)
		{
			ls_.addAnnotation(getOriginStringForUuid(((TtkRefexStringMemberChronicle) component).getRefexExtensionUuid()), getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TtkRefexUuidMemberChronicle)
		{
			ls_.addAnnotation(getOriginStringForUuid(((TtkRefexUuidMemberChronicle) component).getRefexExtensionUuid()), getOriginStringForUuid(refsetUuid));
		}
		else
		{
			ls_.addAnnotation(getOriginStringForUuid(component.getPrimordialComponentUuid()), getOriginStringForUuid(refsetUuid));
		}
	}

	/**
	 * @param time = if null, set to refsetConcept time
	 * @param refsetMemberType - if null, is set to "normal member"
	 */
	public TtkRefexUuidMemberChronicle addRefsetMember(TtkConceptChronicle refsetConcept, UUID targetUuid, UUID refsetMemberType, Status status, Long time)
	{
		
		return addRefsetMember(refsetConcept, targetUuid, refsetMemberType, null, status, time);
	}

	/**
	 * @param time = if null, set to refsetConcept time
	 * @param refsetMemberType - if null, is set to "normal member"
	 * @param refsetMemberPrimordial - if null, computed from refset type, target, member type
	 */
	public TtkRefexUuidMemberChronicle addRefsetMember(TtkConceptChronicle refsetConcept, UUID targetUuid, UUID refsetMemberType, UUID refsetMemberPrimordial, Status status, Long time)
	{
		List<TtkRefexAbstractMemberChronicle<?>> refsetMembers = refsetConcept.getRefsetMembers();
		if (refsetMembers == null)
		{
			refsetMembers = new ArrayList<TtkRefexAbstractMemberChronicle<?>>();
			refsetConcept.setRefsetMembers(refsetMembers);
			/*
			 * These settings could be used to convert member lists - but it requires painful conversion at load time
			 * where concepts must be manually listed in the pom file.  So, don't bother.
			 */
			refsetConcept.setAnnotationStyleRefex(false);  //put the annotations on the target (when true)
		}
		
		TtkRefexUuidMemberChronicle refsetMember = new TtkRefexUuidMemberChronicle();
		if (refsetMemberPrimordial == null)
		{
			refsetMemberPrimordial = ConverterUUID.createNamespaceUUIDFromStrings(refsetConcept.getPrimordialUuid().toString(), 
					targetUuid.toString(), (refsetMemberType == null ? refsetMemberTypeNormalMemberUuid_.toString() : refsetMemberType.toString()));
		}
		refsetMember.setPrimordialComponentUuid(refsetMemberPrimordial);
		refsetMember.setComponentUuid(targetUuid);  // ComponentUuid and refsetUuid seem like they are reversed at first glance, but this is right.
		refsetMember.setRefexExtensionUuid(refsetConcept.getPrimordialUuid());
		refsetMember.setUuid1(refsetMemberType == null ? refsetMemberTypeNormalMemberUuid_ : refsetMemberType);
		setRevisionAttributes(refsetMember, status, (time == null ? refsetConcept.getConceptAttributes().getTime() : time));
		refsetMembers.add(refsetMember);

		ls_.addRefsetMember(getOriginStringForUuid(refsetConcept.getPrimordialUuid()));
		
		return refsetMember;
	}
	
	/**
	 * @param time = if null, set to refsetConcept time
	 * @param refsetMemberType - if null, is set to "normal member"
	 */
	private TtkRefexUuidIntMemberChronicle addRefsetMember(TtkConceptChronicle refsetConcept, UUID targetUuid, UUID refsetMemberType, int refsetMemberIntValue, Status status, Long time)
	{
		List<TtkRefexAbstractMemberChronicle<?>> refsetMembers = refsetConcept.getRefsetMembers();
		if (refsetMembers == null)
		{
			refsetMembers = new ArrayList<TtkRefexAbstractMemberChronicle<?>>();
			refsetConcept.setRefsetMembers(refsetMembers);
			/*
			 * These settings could be used to convert member lists - but it requires painful conversion at load time
			 * where concepts must be manually listed in the pom file.  So, don't bother.
			 */
			refsetConcept.setAnnotationStyleRefex(false);  //put the annotations on the target (when true)
		}
		
		TtkRefexUuidIntMemberChronicle refsetMember = new TtkRefexUuidIntMemberChronicle();
		
		refsetMember.setPrimordialComponentUuid(ConverterUUID.createNamespaceUUIDFromStrings(
				refsetConcept.getPrimordialUuid().toString(), targetUuid.toString(), refsetMemberIntValue + ""));
		refsetMember.setComponentUuid(targetUuid);  // ComponentUuid and refsetUuid seem like they are reversed at first glance, but this is right.
		refsetMember.setRefexExtensionUuid(refsetConcept.getPrimordialUuid());
		refsetMember.setUuid1(refsetMemberType == null ? refsetMemberTypeNormalMemberUuid_ : refsetMemberType);
		refsetMember.setInt1(refsetMemberIntValue);
		setRevisionAttributes(refsetMember, status, (time == null ? refsetConcept.getConceptAttributes().getTime() : time));
		refsetMembers.add(refsetMember);

		ls_.addRefsetMember(getOriginStringForUuid(refsetConcept.getPrimordialUuid()));
		
		return refsetMember;
	}

	/**
	 * Add an IS_A_REL relationship, with the time set to now.
	 */
	public TtkRelationshipChronicle addRelationship(TtkConceptChronicle TtkConceptChronicle, UUID targetUuid)
	{
		return addRelationship(TtkConceptChronicle, null, targetUuid, null, null, null, null);
	}

	/**
	 * Add a relationship. The source of the relationship is assumed to be the specified concept. The UUID of the
	 * relationship is generated.
	 * 
	 * @param relTypeUuid - is optional - if not provided, the default value of IS_A_REL is used.
	 * @param time - if null, default is used
	 */
	public TtkRelationshipChronicle addRelationship(TtkConceptChronicle TtkConceptChronicle, UUID targetUuid, UUID relTypeUuid, Long time)
	{
		return addRelationship(TtkConceptChronicle, null, targetUuid, relTypeUuid, null, null, time);
	}
	
	/**
	 * This rel add method handles the advanced cases where a rel type 'foo' is actually being loaded as "is_a" (or some other arbitrary type)
	 * it makes the swap, and adds the second value as a UUID annotation on the created relationship. 
	 */
	public TtkRelationshipChronicle addRelationship(TtkConceptChronicle TtkConceptChronicle, UUID targetUuid, Property p, Long time)
	{
		if (p.getWBTypeUUID() == null)
		{
			return addRelationship(TtkConceptChronicle, null, targetUuid, p.getUUID(), null, null, time);
		}
		else
		{
			return addRelationship(TtkConceptChronicle, null, targetUuid, p.getWBTypeUUID(), p.getUUID(), p.getPropertyType().getPropertyTypeReferenceSetUUID(), time);
		}
	}
	
	/**
	 * Add a relationship. The source of the relationship is assumed to be the specified concept.
	 * 
	 * @param relPrimordialUuid - optional - if not provided, created from the source, target and type.
	 * @param relTypeUuid - is optional - if not provided, the default value of IS_A_REL is used.
	 * @param time - if null, default is used
	 */
	public TtkRelationshipChronicle addRelationship(TtkConceptChronicle TtkConceptChronicle, UUID relPrimordialUuid, UUID targetUuid, UUID relTypeUuid, 
			UUID sourceRelTypeUUID, UUID sourceRelRefsetUUID, Long time)
	{
		List<TtkRelationshipChronicle> relationships = TtkConceptChronicle.getRelationships();
		if (relationships == null)
		{
			relationships = new ArrayList<TtkRelationshipChronicle>();
			TtkConceptChronicle.setRelationships(relationships);
		}

		TtkRelationshipChronicle rel = new TtkRelationshipChronicle();
		rel.setPrimordialComponentUuid(relPrimordialUuid != null ? relPrimordialUuid : 
			ConverterUUID.createNamespaceUUIDFromStrings(TtkConceptChronicle.getPrimordialUuid().toString(), targetUuid.toString(), 
					(relTypeUuid == null ? isARelUuid_.toString() : relTypeUuid.toString())));
		rel.setC1Uuid(TtkConceptChronicle.getPrimordialUuid());
		rel.setTypeUuid(relTypeUuid == null ? isARelUuid_ : relTypeUuid);
		rel.setC2Uuid(targetUuid);
		rel.setCharacteristicUuid(definingCharacteristicUuid_);
		rel.setRefinabilityUuid(notRefinableUuid);
		rel.setRelGroup(0);
		setRevisionAttributes(rel, null, time);

		relationships.add(rel);
		
		if (sourceRelTypeUUID != null && sourceRelRefsetUUID != null)
		{
			addUuidAnnotation(rel, sourceRelTypeUUID, sourceRelRefsetUUID);
			ls_.addRelationship(getOriginStringForUuid(relTypeUuid) + ":" + getOriginStringForUuid(sourceRelTypeUUID));
		}
		else
		{
			ls_.addRelationship(getOriginStringForUuid(relTypeUuid == null ? isARelUuid_ : relTypeUuid));
		}
		return rel;
	}

	/**
	 * Set up all the boilerplate stuff.
	 * 
	 * @param object - The object to do the setting to
	 * @param statusUuid - Uuid or null (for current)
	 * @param time - time or null (for default)
	 */
	private void setRevisionAttributes(TtkRevision object, Status status, Long time)
	{
		object.setAuthorUuid(authorUuid_);
		object.setModuleUuid(moduleUuid_);
		object.setPathUuid(terminologyPathUUID_);
		object.setStatus(status == null ? Status.ACTIVE : status);
		object.setTime(time == null ? defaultTime_ : time.longValue());
	}

	private String getOriginStringForUuid(UUID uuid)
	{
		String temp = ConverterUUID.getUUIDCreationString(uuid);
		if (temp != null)
		{
			String[] parts = temp.split(":");
			if (parts != null && parts.length > 1)
			{
				return parts[parts.length - 1];
			}
			return temp;
		}
		return "Unknown";
	}

	public LoadStats getLoadStats()
	{
		return ls_;
	}

	public void clearLoadStats()
	{
		ls_ = new LoadStats();
	}

	/**
	 * Utility method to build and store a metadata concept.
	 */
	public TtkConceptChronicle createAndStoreMetaDataConcept(String name, boolean indexRefsetMembers, UUID relParentPrimordial, DataOutputStream dos) throws Exception
	{
		return createMetaDataConcept(ConverterUUID.createNamespaceUUIDFromString(name), name, null, null, null, indexRefsetMembers, relParentPrimordial, null, null, dos);
	}

	/**
	 * Utility method to build and store a metadata concept.
	 */
	public TtkConceptChronicle createAndStoreMetaDataConcept(UUID primordial, String name, boolean indexRefsetMembers, UUID relParentPrimordial, DataOutputStream dos) throws Exception
	{
		return createMetaDataConcept(primordial, name, null, null, null, indexRefsetMembers, relParentPrimordial, null, null, dos);
	}

	/**
	 * Utility method to build and store a metadata concept.
	 * @param sourceProperty - optional - used to fire a callback if present.  No impact on created concept.
	 * @param dos - optional - does not store when not provided
	 * @param secondParent - optional
	 */
	public TtkConceptChronicle createMetaDataConcept(UUID primordial, String fsnName, String preferredName, String altName, String definition, 
			boolean indexRefsetMembers, UUID relParentPrimordial, UUID secondParent, Property sourceProperty, DataOutputStream dos)
			throws Exception
	{
		TtkConceptChronicle concept = createConcept(primordial, fsnName);
		concept.setAnnotationStyleRefex(indexRefsetMembers);
		addRelationship(concept, relParentPrimordial);
		if (secondParent != null)
		{
			addRelationship(concept, secondParent);
		}
		if (StringUtils.isNotEmpty(preferredName))
		{
			addDescription(concept, preferredName, DescriptionType.SYNONYM, true, null, null, Status.ACTIVE);
		}
		if (StringUtils.isNotEmpty(altName))
		{
			addDescription(concept, altName, DescriptionType.SYNONYM, false, null, null, Status.ACTIVE);
		}
		if (StringUtils.isNotEmpty(definition))
		{
			addDescription(concept, definition, DescriptionType.DEFINITION, true, null, null, Status.ACTIVE);
		}
		
		//fire the calllback
		if (sourceProperty != null)
		{
			sourceProperty.conceptCreated(concept);
		}
		
		if (dos != null)
		{
			concept.writeExternal(dos);
		}
		return concept;
	}


	/**
	 * Create metadata TtkConceptChronicles from the PropertyType structure
	 * NOTE - Refset types are not stored!
	 */
	public void loadMetaDataItems(PropertyType propertyType, UUID parentPrimordial, DataOutputStream dos) throws Exception
	{
		ArrayList<PropertyType> propertyTypes = new ArrayList<PropertyType>();
		propertyTypes.add(propertyType);
		loadMetaDataItems(propertyTypes, parentPrimordial, dos);
	}

	/**
	 * Create metadata TtkConceptChronicles from the PropertyType structure
	 * NOTE - Refset types are not stored!
	 */
	public void loadMetaDataItems(Collection<PropertyType> propertyTypes, UUID parentPrimordial, DataOutputStream dos) throws Exception
	{
		for (PropertyType pt : propertyTypes)
		{
			if (pt instanceof BPT_Skip)
			{
				continue;
			}
			createAndStoreMetaDataConcept(pt.getPropertyTypeUUID(), pt.getPropertyTypeDescription(), pt.getIndexRefsetMembers(), parentPrimordial, dos);
			UUID secondParent = null;
			if (pt instanceof BPT_MemberRefsets)
			{
				//Need to create the VA_Refsets concept, and create a terminology refset grouper, then use that as a second parent
				TtkConceptChronicle refsetRoot = createVARefsetRootConcept();
				refsetRoot.writeExternal(dos);
				
				TtkConceptChronicle refsetTermGroup = createConcept(pt.getPropertyTypeReferenceSetName(), refsetRoot.getPrimordialUuid());
				((BPT_MemberRefsets) pt).setRefsetIdentityParent(refsetTermGroup);
				secondParent = refsetTermGroup.getPrimordialUuid(); 
			}
			else if (pt instanceof BPT_Descriptions)
			{
				//only do this once, in case we see a BPT_Descriptions more than once
				if (wbPropertyMetadataDescUUID == null)
				{
					wbPropertyMetadataDescUUID = setupWbPropertyMetadata("Description source type reference set", "Description name in source terminology", pt, dos);
					secondParent = wbPropertyMetadataDescUUID;
				}
			}
			
			else if (pt instanceof BPT_Relations)
			{
				//only do this once, in case we see a BPT_Relations more than once
				if (wbPropertyMetadataRelUUID == null)
				{
					wbPropertyMetadataRelUUID = setupWbPropertyMetadata("Relation source type reference set", "Relation name in source terminology", pt, dos);
					secondParent = wbPropertyMetadataRelUUID;
				}
			}
			
			for (Property p : pt.getProperties())
			{
				//In the case of refsets, don't store these  yet.  User must manually store refsets after they have been populated.
				createMetaDataConcept(p.getUUID(), p.getSourcePropertyNameFSN(), p.getSourcePropertyPreferredName(), p.getSourcePropertyAltName(), 
						p.getSourcePropertyDefinition(), pt.getIndexRefsetMembers(), pt.getPropertyTypeUUID(), secondParent, p, 
						(pt instanceof BPT_MemberRefsets ? null : dos));
			}
		}
	}
	
	public void storeRefsetConcepts(BPT_MemberRefsets refsets, DataOutputStream dos) throws IOException
	{
		refsets.getRefsetIdentityParent().writeExternal(dos);
		for (Property p : refsets.getProperties())
		{
			refsets.getConcept(p).writeExternal(dos);
		}
		refsets.clearConcepts();
	}
	
	/**
	 * This is just used by the setupWbPropertyMetadata method, which requires the UUIDs to be specified in a certain way.
	 */
	private TtkConceptChronicle createMetaDataSpecialConcept(UUID primordial, String fsnName, String preferredName, UUID relParentPrimordial, DataOutputStream dos)
			throws Exception
	{
		TtkConceptChronicle concept = createConcept(primordial);
		//UUID needs to always be the same per description, so things merge properly when this gets created by multiple loaders
		addDescription(concept, UuidT5Generator.get("FSN:" + fsnName), fsnName, DescriptionType.FSN, true, null, null, Status.ACTIVE);
		concept.setAnnotationStyleRefex(false);
		addRelationship(concept, relParentPrimordial);
		if (preferredName != null)
		{
			//UUID needs to always be the same per description, so things merge properly when this gets created by multiple loaders
			addDescription(concept, UuidT5Generator.get("preferredName:" +preferredName), preferredName, DescriptionType.SYNONYM, true, null, null, Status.ACTIVE);
		}
		concept.writeExternal(dos);
		return concept;
	}

	
	private UUID setupWbPropertyMetadata(String refsetSynonymName, String refsetValueParentSynonynmName, PropertyType pt, DataOutputStream dos) throws Exception
	{
		if (pt.getPropertyTypeReferenceSetName() == null || pt.getPropertyTypeReferenceSetUUID() == null)
		{
			throw new RuntimeException("Unhandled case!");
		}
		//Create a concept under "Reference set (foundation metadata concept)"  7e38cd2d-6f1a-3a81-be0b-21e6090573c2
		//Now create the description type refset bucket.  UUID should always be the same - not terminology specific.  This should come from the WB, eventually.
		UUID uuid = UuidT5Generator.get(refsetSynonymName + " (foundation metadata concept)");
		createMetaDataSpecialConcept(uuid, refsetSynonymName + " (foundation metadata concept)", refsetSynonymName,
				UUID.fromString("7e38cd2d-6f1a-3a81-be0b-21e6090573c2"), dos);
		
		//Now create the terminology specific refset type as a child
		createAndStoreMetaDataConcept(pt.getPropertyTypeReferenceSetUUID(), pt.getPropertyTypeReferenceSetName(), false, uuid, dos);
		ConverterUUID.addMapping(pt.getPropertyTypeReferenceSetName(), pt.getPropertyTypeReferenceSetUUID());
		
		//TODO we shouldn't have to create this concept in the future - two new concepts have been added to the US extension for this purpose.
		//Should eventually be changed to "Semantic Description Type" and "Semantic Relationship Type" - so don't create this intermediate concept, 
		//just hang our concept under the appropriate one which will already exist.
		//Until then - create our own.....
		//Finally, create the Reference set attribute children that we will put the actual properties under
		//Create the concept under "Reference set attribute (foundation metadata concept)"  7e52203e-8a35-3121-b2e7-b783b34d97f2
		uuid = UuidT5Generator.get(refsetValueParentSynonynmName + " (foundation metadata concept)");
		createMetaDataSpecialConcept(uuid, refsetValueParentSynonynmName + " (foundation metadata concept)", refsetValueParentSynonynmName,
				UUID.fromString("7e52203e-8a35-3121-b2e7-b783b34d97f2"), dos).getPrimordialUuid();
		
		//Now create the terminology specific refset type as a child - very similar to above, but since this isn't the refset concept, just an organization
		//concept, I add an 's' to make it plural, and use a different UUID (calculated from the new plural)
		//I have a case in UMLS and RxNorm loaders where this makes a duplicate, but its ok, it should merge.
		return createAndStoreMetaDataConcept(ConverterUUID.createNamespaceUUIDFromString(pt.getPropertyTypeReferenceSetName() + "s", true), pt.getPropertyTypeReferenceSetName() + "s", false, uuid, dos).getPrimordialUuid();
	}
}
