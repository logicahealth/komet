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
import gov.vha.isaac.metadata.source.IsaacMetadataAuxiliaryBinding;
import gov.vha.isaac.mojo.GenerateMetadataEConcepts;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.model.constants.IsaacMetadataConstants;
import java.beans.PropertyVetoException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.TtkUtils;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicString;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicUUID;
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
	public final static UUID isARelUuid_ = IsaacMetadataAuxiliaryBinding.IS_A.getPrimodialUuid();
	public final static UUID authorUuid_ = IsaacMetadataAuxiliaryBinding.USER.getPrimodialUuid();
	public final static UUID synonymUuid_ = IsaacMetadataAuxiliaryBinding.SYNONYM.getPrimodialUuid();
	public final static UUID definitionUuid_ = IsaacMetadataAuxiliaryBinding.DEFINITION_DESCRIPTION_TYPE.getPrimodialUuid();
	public final static UUID fullySpecifiedNameUuid_ = IsaacMetadataAuxiliaryBinding.FULLY_SPECIFIED_NAME.getPrimodialUuid();
	public final static UUID descriptionAcceptableUuid_ = IsaacMetadataAuxiliaryBinding.ACCEPTABLE.getPrimodialUuid();
	public final static UUID descriptionPreferredUuid_ = IsaacMetadataAuxiliaryBinding.PREFERRED.getPrimodialUuid();
	public final static UUID usEnRefsetUuid_ = IsaacMetadataAuxiliaryBinding.US_ENGLISH_DIALECT.getPrimodialUuid();
	public final static UUID definingCharacteristicUuid_ = IsaacMetadataAuxiliaryBinding.STATED.getPrimodialUuid();
	public final static UUID notRefinableUuid = SnomedMetadataRf2.NOT_REFINABLE_RF2.getPrimodialUuid();  //TODO missing from ochre metadata
	public final static UUID terminologyPathUUID_ = IsaacMetadataAuxiliaryBinding.DEVELOPMENT.getPrimodialUuid();
	public final static UUID refsetMemberTypeNormalMemberUuid_ = IsaacMetadataAuxiliaryBinding.NORMAL_MEMBER.getPrimodialUuid();
	public final static String PROJECT_REFSETS_NAME = "SOLOR Refsets";
	public final static UUID PROJECT_REFSETS_UUID = UUID.fromString("7a9b495e-69c1-53e5-a2d5-41be2429c146");  //This is UuidT5Generator.PATH_ID_FROM_FS_DESC, "SOLOR Refsets")
	
	public final long defaultTime_;
	private final String lang_ = "en";
	public UUID moduleUuid_ = null;
	private HashMap<UUID, DynamicSememeColumnInfo[]> refexAllowedColumnTypes_ = new HashMap<>();;

	private LoadStats ls_ = new LoadStats();

	/**
	 * Creates and stores the path concept - sets up the various namespace details.
	 * @param namespaceSeed The string to use for seeding the UUID generator for this namespace
	 * @param moduleName The name to use for the concept that will be created as the 'module' concept
	 * @param defaultTime - the timestamp to place on created elements, when no other timestamp is specified on the element itself.
	 * @param dos - location to write the output
	 * @throws Exception
	 */
	public EConceptUtility(UUID module, DataOutputStream dos, long defaultTime) throws Exception
	{
		ConverterUUID.addMapping("isA", isARelUuid_);
		ConverterUUID.addMapping("Synonym", synonymUuid_);
		ConverterUUID.addMapping("Fully Specified Name", fullySpecifiedNameUuid_);
		ConverterUUID.addMapping("US English Refset", usEnRefsetUuid_);
		
		defaultTime_ = defaultTime;
		
		//Just use the module as the namespace
		ConverterUUID.configureNamespace(module);
		
		moduleUuid_ = module;
		ConsoleUtil.println("Loading with module '" + moduleUuid_+ " on DEVELOPMENT path");
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
		addLegacyUuidAnnotation(description, null, (preferred ? descriptionPreferredUuid_ : descriptionAcceptableUuid_), usEnRefsetUuid_, Status.ACTIVE, null);
		
		if (sourceDescriptionRefsetUUID != null)
		{
			try
			{
				addAnnotation(description, null, (sourceDescriptionTypeUUID == null ? null : new TtkRefexDynamicUUID(sourceDescriptionTypeUUID)),
						sourceDescriptionRefsetUUID, null, null);
			}
			catch (PropertyVetoException e)
			{
				throw new RuntimeException("Unexpected");
			}
		}
		
		ls_.addDescription(wbDescriptionType.name() + (sourceDescriptionTypeUUID == null ? (sourceDescriptionRefsetUUID == null ? "" : ":-member-:") :
				":" + getOriginStringForUuid(sourceDescriptionTypeUUID) + ":")
					+ (sourceDescriptionRefsetUUID == null ? "" : getOriginStringForUuid(sourceDescriptionRefsetUUID)));
		return description;
	}
	
	/**
	 * Generated the UUID, uses the concept time
	 */
	public TtkRefexDynamicMemberChronicle addStringAnnotation(TtkConceptChronicle concept, String annotationValue, UUID refsetUuid, Status status)
	{
		if (annotationValue == null)
		{
			throw new RuntimeException("value is now required.");
		}
		try
		{
			return addAnnotation(concept.getConceptAttributes(), null, new TtkRefexDynamicData[] {new TtkRefexDynamicString(annotationValue)}, refsetUuid, status, null);
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException("Unexpected");
		}
	}

	/**
	 * uses the concept time, UUID is created from the component UUID, the annotation value and type.
	 */
	public TtkRefexDynamicMemberChronicle addStringAnnotation(TtkComponentChronicle<?, ?> component, String annotationValue, UUID refsetUuid, Status status)
	{
		if (annotationValue == null)
		{
			throw new RuntimeException("value is now required.");
		}
		try
		{
			return addAnnotation(component, null, new TtkRefexDynamicData[] {new TtkRefexDynamicString(annotationValue)}, refsetUuid, status, null);
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException("Unexpected");
		}
	}
	
	public TtkRefexDynamicMemberChronicle addAnnotationStyleRefsetMembership(TtkComponentChronicle<?, ?> component, UUID refexDynamicTypeUuid, Status status, Long time)
	{
		return addAnnotation(component, null, (TtkRefexDynamicData[])null, refexDynamicTypeUuid, status, time);
	}
	
	public TtkRefexDynamicMemberChronicle addAnnotation(TtkComponentChronicle<?, ?> component, UUID uuidForCreatedAnnotation, TtkRefexDynamicData value, 
			UUID refexDynamicTypeUuid, Status status, Long time)
	{
		return addAnnotation(component, uuidForCreatedAnnotation, new TtkRefexDynamicData[] {value}, refexDynamicTypeUuid, status, time);
	}
	
	/**
	 * @param component The component to attach this annotation to
	 * @param UuidForCreatedAnnotation  - the UUID to use for the created annotation.  If null, generated from uuidForCreatedAnnotation, value, refexDynamicTypeUuid
	 * @param values - the values to attach (may be null if the annotation only serves to mark 'membership') - columns must align with values specified in the definition
	 * of the sememe represented by refexDynamicTypeUuid
	 * @param refexDynamicTypeUuid - the uuid of the dynamic refex type - 
	 * @param status
	 * @param time - if null, uses the component time
	 * @return
	 */
	public TtkRefexDynamicMemberChronicle addAnnotation(TtkComponentChronicle<?, ?> component, UUID uuidForCreatedAnnotation, TtkRefexDynamicData[] values, 
			UUID refexDynamicTypeUuid, Status status, Long time)
	{
		List<TtkRefexDynamicMemberChronicle> annotations = component.getAnnotationsDynamic();
		if (annotations == null)
		{
			annotations = new ArrayList<TtkRefexDynamicMemberChronicle>();
			component.setAnnotationsDynamic(annotations);
		}
		
		TtkRefexDynamicMemberChronicle annotation = new TtkRefexDynamicMemberChronicle();
		
		annotation.setComponentUuid(component.getPrimordialComponentUuid());
		annotation.setRefexAssemblageUuid(refexDynamicTypeUuid);
		validateDataTypes(refexDynamicTypeUuid, values);
		annotation.setData(values);
		if (uuidForCreatedAnnotation == null)
		{
			try
			{
				String hashValue = GenerateMetadataEConcepts.setUUIDForRefex(annotation, values, ConverterUUID.getNamespace());
			
			ConverterUUID.addMapping(hashValue, annotation.getPrimordialComponentUuid());
			}
			catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
			{
				throw new RuntimeException("Unexpected", e);
			}
		}
		else
		{
			annotation.setPrimordialComponentUuid(uuidForCreatedAnnotation);
		}
		
		setRevisionAttributes(annotation, status, (time == null ? component.getTime() : time));
		annotations.add(annotation);
		annotationLoadStats(component, refexDynamicTypeUuid);
		return annotation;
	}

	/**
	 * @param refexDynamicTypeUuid
	 * @param values
	 */
	private void validateDataTypes(UUID refexDynamicTypeUuid, TtkRefexDynamicData[] values)
	{
		//TODO this should be a much better validator - checking all of the various things in RefexDynamicCAB.validateData - or in 
		//generateMetadataEConcepts
		if (values != null && values.length > 0)
		{
			DynamicSememeColumnInfo[] colInfo = refexAllowedColumnTypes_.get(refexDynamicTypeUuid);
			if (colInfo == null || colInfo.length == 0)
			{
				throw new RuntimeException("Attempted to store data on a concept not configured as a dynamic refex");
			}
			for (int i = 0; i < values.length; i++)
			{
				DynamicSememeColumnInfo column = null;
				for (DynamicSememeColumnInfo x : colInfo)
				{
					if(x.getColumnOrder() == i)
					{
						column = x;
						break;
					}
				}
				if (column == null)
				{
					throw new RuntimeException("Column count mismatch");
				}
				else
				{
					if (column.getColumnDataType() != values[i].getRefexDataType())
					{
						throw new RuntimeException("Datatype mismatch - " + column.getColumnDataType() + " - " + values[i].getRefexDataType());
					}
				}
			}
		}
	}

	/**
	 * uses the component time, creates the UUID from the component UUID, the value UUID, and the type UUID.
	 */
	public TtkRefexDynamicMemberChronicle addUuidAnnotation(TtkComponentChronicle<?, ?> component, UUID value, UUID refsetUuid)
	{
		if (value == null)
		{
			throw new RuntimeException("value is now required.");
		}
		try
		{
			return addAnnotation(component, null, new TtkRefexDynamicData[] {new TtkRefexDynamicUUID(value)}, refsetUuid, null, null);
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException("Unexpected");
		}
	}
	
	/**
	 * Generates the UUID, uses the component time
	 */
	public TtkRefexDynamicMemberChronicle addUuidAnnotation(TtkConceptChronicle concept, UUID value, UUID refsetUuid)
	{
		if (value == null)
		{
			throw new RuntimeException("value is now required.");
		}
		try
		{
			return addAnnotation(concept.getConceptAttributes(), null, new TtkRefexDynamicData[] {new TtkRefexDynamicUUID(value)}, refsetUuid, null, null);
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException("Unexpected");
		}
	}

	/**
	 * annotationPrimordialUuid - if null, generated from component UUID, value, type
	 * @param time - If time is null, uses the component time.
	 * @param valueConcept - if value is null, it uses RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid()
	 */
	private TtkRefexUuidMemberChronicle addLegacyUuidAnnotation(TtkComponentChronicle<?, ?> component, UUID annotationPrimordialUuid, UUID valueConcept, UUID refsetUuid, 
			Status status, Long time)
	{
		List<TtkRefexAbstractMemberChronicle<?>> annotations = component.getAnnotations();

		if (annotations == null)
		{
			annotations = new ArrayList<TtkRefexAbstractMemberChronicle<?>>();
			component.setAnnotations(annotations);
		}

		TtkRefexUuidMemberChronicle conceptRefexMember = new TtkRefexUuidMemberChronicle();

		conceptRefexMember.setReferencedComponentUuid(component.getPrimordialComponentUuid());
		if (annotationPrimordialUuid == null)
		{
			annotationPrimordialUuid = ConverterUUID.createNamespaceUUIDFromStrings(component.getPrimordialComponentUuid().toString(), 
					(valueConcept == null ? refsetMemberTypeNormalMemberUuid_ : valueConcept).toString(), refsetUuid.toString());
		}
		conceptRefexMember.setPrimordialComponentUuid(annotationPrimordialUuid);
		conceptRefexMember.setUuid1(valueConcept == null ? refsetMemberTypeNormalMemberUuid_ : valueConcept);
		conceptRefexMember.setAssemblageUuid(refsetUuid);
		setRevisionAttributes(conceptRefexMember, status, (time == null ? component.getTime() : time));

		annotations.add(conceptRefexMember);

		annotationLoadStats(component, refsetUuid);
		return conceptRefexMember;
	}

	private void annotationLoadStats(TtkComponentChronicle<?, ?> component, UUID refsetUuid)
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
			ls_.addAnnotation(getOriginStringForUuid(((TtkRefexStringMemberChronicle) component).getAssemblageUuid()), getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TtkRefexUuidMemberChronicle)
		{
			ls_.addAnnotation(getOriginStringForUuid(((TtkRefexUuidMemberChronicle) component).getAssemblageUuid()), getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TtkRefexDynamicMemberChronicle)
		{
			ls_.addAnnotation(getOriginStringForUuid(((TtkRefexDynamicMemberChronicle) component).getRefexAssemblageUuid()), getOriginStringForUuid(refsetUuid));
		}
		else
		{
			ls_.addAnnotation(getOriginStringForUuid(component.getPrimordialComponentUuid()), getOriginStringForUuid(refsetUuid));
		}
	}
	
	public TtkRefexDynamicMemberChronicle addDynamicRefsetMember(TtkConceptChronicle refsetConcept, UUID targetUuid, UUID uuidForCreatedAnnotation, Status status, Long time)
	{
		List<TtkRefexDynamicMemberChronicle> members = refsetConcept.getRefsetMembersDynamic();
		if (members == null)
		{
			members = new ArrayList<TtkRefexDynamicMemberChronicle>();
			refsetConcept.setRefsetDynamicMembers(members);
		}
		
		TtkRefexDynamicMemberChronicle member = new TtkRefexDynamicMemberChronicle();
		
		member.setComponentUuid(targetUuid);
		member.setRefexAssemblageUuid(refsetConcept.getPrimordialUuid());
		//validateDataTypes(refexDynamicTypeUuid, values);
		member.setData(null);
		if (uuidForCreatedAnnotation == null)
		{
			try
			{
				String hashValue = GenerateMetadataEConcepts.setUUIDForRefex(member, null, ConverterUUID.getNamespace());
			
			ConverterUUID.addMapping(hashValue, member.getPrimordialComponentUuid());
			}
			catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
			{
				throw new RuntimeException("Unexpected", e);
			}
		}
		else
		{
			member.setPrimordialComponentUuid(uuidForCreatedAnnotation);
		}
		
		setRevisionAttributes(member, status, (time == null ? refsetConcept.getConceptAttributes().getTime() : time));
		members.add(member);
		ls_.addRefsetMember(getOriginStringForUuid(refsetConcept.getPrimordialUuid()));
		return member;
	}
	
	//TODO write addAssociation methods

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
	public void setRevisionAttributes(TtkRevision object, Status status, Long time)
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
	public TtkConceptChronicle createAndStoreMetaDataConcept(String name, UUID relParentPrimordial, DataOutputStream dos) throws Exception
	{
		return createMetaDataConcept(ConverterUUID.createNamespaceUUIDFromString(name), name, null, null, null, relParentPrimordial, null, null, dos);
	}

	/**
	 * Utility method to build and store a metadata concept.
	 */
	public TtkConceptChronicle createAndStoreMetaDataConcept(UUID primordial, String name, UUID relParentPrimordial, 
			BiConsumer<TtkConceptChronicle, EConceptUtility> callback,
			DataOutputStream dos) throws Exception
	{
		return createMetaDataConcept(primordial, name, null, null, null, relParentPrimordial, null, callback, dos);
	}

	/**
	 * Utility method to build and store a metadata concept.
	 * @param callback - optional - used to fire a callback if present.  No impact on created concept.  
	 * @param dos - optional - does not store when not provided
	 * @param secondParent - optional
	 */
	public TtkConceptChronicle createMetaDataConcept(UUID primordial, String fsnName, String preferredName, String altName, String definition, 
			UUID relParentPrimordial, UUID secondParent, BiConsumer<TtkConceptChronicle, EConceptUtility> callback, DataOutputStream dos)
			throws Exception
	{
		TtkConceptChronicle concept = createConcept(primordial, fsnName);
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

		//Fire the callback
		if (callback != null)
		{
			callback.accept(concept, this);
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
		List<TtkRefexDynamicMemberChronicle> attrs = new ArrayList<>();
		for (PropertyType pt : propertyTypes)
		{
			if (pt instanceof BPT_Skip)
			{
				continue;
			}
			createAndStoreMetaDataConcept(pt.getPropertyTypeUUID(), pt.getPropertyTypeDescription(), parentPrimordial, null, dos);
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
				secondParent = setupWbPropertyMetadata(IsaacMetadataAuxiliaryBinding.DESCRIPTION_SOURCE_TYPE_REFERENCE_SETS.getPrimodialUuid(),
						IsaacMetadataAuxiliaryBinding.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY.getPrimodialUuid(), pt, dos);
			}
			
			else if (pt instanceof BPT_Relations)
			{
				secondParent = setupWbPropertyMetadata(IsaacMetadataAuxiliaryBinding.RELATIONSHIP_SOURCE_TYPE_REFERENCE_SETS.getPrimodialUuid(),
						IsaacMetadataAuxiliaryBinding.RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY.getPrimodialUuid(), pt, dos);
			}
			
			for (Property p : pt.getProperties())
			{
				//In the case of refsets, don't store these  yet.  User must manually store refsets after they have been populated.
				createMetaDataConcept(p.getUUID(), p.getSourcePropertyNameFSN(), p.getSourcePropertyPreferredName(), p.getSourcePropertyAltName(), 
						p.getSourcePropertyDefinition(), pt.getPropertyTypeUUID(), secondParent, p.getCallback(), 
						(pt instanceof BPT_MemberRefsets ? null : dos));
				
				//cache the indexing info
				if (pt.createAsDynamicRefex())
				{
					if (pt instanceof BPT_MemberRefsets && (p.getDataColumnsForDynamicRefex() == null || p.getDataColumnsForDynamicRefex().length == 0))
					{
						//no index required - this is a member style refex
					}
					else
					{
						refexAllowedColumnTypes_.put(p.getUUID(), p.getDataColumnsForDynamicRefex());
						if (p.getDataColumnsForDynamicRefex() != null)
						{
							Integer[] temp = new Integer[p.getDataColumnsForDynamicRefex().length];
							for (int i = 0; i < temp.length; i++)
							{
								temp[i] = i;
							}
							
							
							
							attrs.add(TtkUtils.configureDynamicRefexIndexes(p.getUUID(), temp, (rev -> setRevisionAttributes(rev, Status.ACTIVE, defaultTime_))));
						}
					}
				}
			}
		}
		
		//For lack of a better place to put them, drop them an the index concept:
		
		TtkConceptChronicle indexConcept = createConcept(IsaacMetadataConstants.DYNAMIC_SEMEME_INDEX_CONFIGURATION.getUUID());
		indexConcept.getConceptAttributes().setAnnotationsDynamic(attrs);
		indexConcept.writeExternal(dos);
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
	
	private UUID setupWbPropertyMetadata(UUID refsetSynonymParent, UUID refsetValueParent, PropertyType pt, DataOutputStream dos) throws Exception
	{
		if (pt.getPropertyTypeReferenceSetName() == null || pt.getPropertyTypeReferenceSetUUID() == null)
		{
			throw new RuntimeException("Unhandled case!");
		}
		
		//Create the terminology specific refset type
		BiConsumer<TtkConceptChronicle, EConceptUtility> callback = new BiConsumer<TtkConceptChronicle, EConceptUtility>()
		{
			@Override
			public void accept(TtkConceptChronicle concept, EConceptUtility util)
			{
				try
				{
					DynamicSememeColumnInfo[] colInfo = new DynamicSememeColumnInfo[] {
							new DynamicSememeColumnInfo(0, IsaacMetadataConstants.DYNAMIC_SEMEME_COLUMN_VALUE.getUUID(), DynamicSememeDataType.UUID, null, true)};
					TtkUtils.configureConceptAsRefex(concept, "Carries the source description type information",
							colInfo,
							null, 
							null,
							(rev -> setRevisionAttributes(rev, Status.ACTIVE, concept.getConceptAttributes().getTime())));
					refexAllowedColumnTypes_.put(concept.getPrimordialUuid(), colInfo);
					List<TtkRefexDynamicMemberChronicle> attrs = concept.getConceptAttributes().getAnnotationsDynamic();
					if (attrs == null)
					{
						attrs = new ArrayList<>();
					}
					attrs.add(TtkUtils.configureDynamicRefexIndexes(concept.getPrimordialUuid(), new Integer[] {0}, 
							(rev -> setRevisionAttributes(rev, Status.ACTIVE, concept.getConceptAttributes().getTime()))));
				}
				catch (NoSuchAlgorithmException | PropertyVetoException | IOException e)
				{
					throw new RuntimeException("Unexpected", e);
				}
			}
		};
		
		createAndStoreMetaDataConcept(pt.getPropertyTypeReferenceSetUUID(), pt.getPropertyTypeReferenceSetName(), refsetSynonymParent, callback, dos);
		ConverterUUID.addMapping(pt.getPropertyTypeReferenceSetName(), pt.getPropertyTypeReferenceSetUUID());
		
		//Now create the terminology specific refset type as a child - very similar to above, but since this isn't the refset concept, just an organization
		//concept, I add an 's' to make it plural, and use a different UUID (calculated from the new plural)
		//I have a case in UMLS and RxNorm loaders where this makes a duplicate, but its ok, it should merge.
		return createAndStoreMetaDataConcept(ConverterUUID.createNamespaceUUIDFromString(pt.getPropertyTypeReferenceSetName() + "s", true), 
				pt.getPropertyTypeReferenceSetName() + "s", refsetValueParent, null, dos).getPrimordialUuid();
	}
}
