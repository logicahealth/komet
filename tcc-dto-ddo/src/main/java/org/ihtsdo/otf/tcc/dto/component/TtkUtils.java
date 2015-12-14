package org.ihtsdo.otf.tcc.dto.component;

import java.beans.PropertyVetoException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import gov.vha.isaac.ochre.api.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicArray;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicBoolean;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicInteger;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicString;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicUUID;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.model.constants.IsaacMetadataConstants;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 * In general, code that helps in writing TTK econcepts - mostly ported from the EConceptUtility in the common converter code
 * {@link TtkUtils}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TtkUtils	
{
	private static final UUID descriptionAcceptableUuid_ = SnomedMetadataRf2.ACCEPTABLE_RF2.getPrimordialUuid();
	private static final UUID descriptionPreferredUuid_ = SnomedMetadataRf2.PREFERRED_RF2.getPrimordialUuid();
	private static final UUID usEnRefsetUuid_ = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getPrimordialUuid();
	
	public static void configureConceptAsDynamicRefex(TtkConceptChronicle concept, String refexDescription,
			DynamicSememeColumnInfo[] columns, ObjectChronologyType referencedComponentTypeRestriction, SememeType referencedComponentTypeSubRestriction,
			Consumer<TtkRevision> revSetter) throws NoSuchAlgorithmException, UnsupportedEncodingException, PropertyVetoException
	{
		// See {@link DynamicSememeUsageDescriptionBI} class for more details on this format.
		//Add the special synonym to establish this as an assemblage concept
		TtkDescriptionChronicle description = addDescription(concept, refexDescription, Snomed.DEFINITION_DESCRIPTION_TYPE.getPrimordialUuid(), true, 
				LanguageCode.EN, revSetter);
		
		//Annotate the description as the 'special' type that means this concept is suitable for use as an assemblage concept
		List<TtkRefexDynamicMemberChronicle> nested = description.getAnnotationsDynamic();
		if (nested == null)
		{
			nested = new ArrayList<>();
		}
		nested.add(createDynamicAnnotation(description.getPrimordialUuid(), IsaacMetadataConstants.DYNAMIC_SEMEME_DEFINITION_DESCRIPTION.getUUID(), 
				new TtkRefexDynamicData[0], revSetter));

		if (columns != null)
		{
			for (DynamicSememeColumnInfo col : columns)
			{
				TtkRefexDynamicData[] data = new TtkRefexDynamicData[7];
				data[0] = new TtkRefexDynamicInteger(col.getColumnOrder());
				data[1] = new TtkRefexDynamicUUID(col.getColumnDescriptionConcept());
				data[2] = new TtkRefexDynamicString(col.getColumnDataType().name());
				data[3] = TtkRefexDynamicData.convertPolymorphicDataColumn(col.getDefaultColumnValue(), col.getColumnDataType());
				data[4] = new TtkRefexDynamicBoolean(col.isColumnRequired());

				if (col.getValidator() != null)
				{
					ArrayList<TtkRefexDynamicString> validators = new ArrayList<>();
					for (int i = 0; i < col.getValidator().length; i++)
					{
						validators.add(new TtkRefexDynamicString(col.getValidator()[i].name()));
					}
					data[5] = new TtkRefexDynamicArray<TtkRefexDynamicString>(validators.toArray(new TtkRefexDynamicString[validators.size()]));
				}
				else
				{
					data[5] = null;
				}

				if (col.getValidatorData() != null)
				{
					ArrayList<TtkRefexDynamicData> validators = new ArrayList<>();
					for (int i = 0; i < col.getValidatorData().length; i++)
					{
						validators.add(TtkRefexDynamicData.convertPolymorphicDataColumn(col.getValidatorData()[i], col.getValidatorData()[i].getDynamicSememeDataType()));
					}
					data[6] = new TtkRefexDynamicArray<TtkRefexDynamicData>(validators.toArray(new TtkRefexDynamicData[validators.size()]));
				}
				else
				{
					data[6] = null;
				}
				nested = concept.getConceptAttributes().getAnnotationsDynamic();
				if (nested == null)
				{
					nested = new ArrayList<>();
				}
				nested.add(createDynamicAnnotation(concept.getConceptAttributes().getPrimordialUuid(), 
						IsaacMetadataConstants.DYNAMIC_SEMEME_EXTENSION_DEFINITION.getUUID(), data, revSetter));
			}
		}
		
		if (referencedComponentTypeRestriction != null && ObjectChronologyType.UNKNOWN_NID != referencedComponentTypeRestriction)
		{
			int size = 1;
			if (referencedComponentTypeSubRestriction!= null && SememeType.UNKNOWN != referencedComponentTypeSubRestriction)
			{
				size = 2;
			}

			TtkRefexDynamicData[] data = new TtkRefexDynamicData[size];
			data[0] = new TtkRefexDynamicString(referencedComponentTypeRestriction.name());
			if (size == 2)
			{
				data[1] = new TtkRefexDynamicString(referencedComponentTypeSubRestriction.name());
			}

			nested.add(createDynamicAnnotation(concept.getConceptAttributes().getPrimordialUuid(), 
					IsaacMetadataConstants.DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getUUID(), data, revSetter));
		}
	}
	
	public static DynamicSememeColumnInfo[] configureConceptAsAssociation(TtkConceptChronicle concept, String refexDescription, String associationInverseName,
			ObjectChronologyType referencedComponentTypeRestriction, SememeType referencedComponentTypeSubRestriction,
			Consumer<TtkRevision> revSetter) throws NoSuchAlgorithmException, UnsupportedEncodingException, PropertyVetoException
	{
		DynamicSememeColumnInfo[] columns = new DynamicSememeColumnInfo[] {
				new DynamicSememeColumnInfo(0, IsaacMetadataConstants.DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(), 
						DynamicSememeDataType.UUID, null, false)};
		
		configureConceptAsDynamicRefex(concept, refexDescription, columns, referencedComponentTypeRestriction, referencedComponentTypeSubRestriction, revSetter);
		
		List<TtkRefexDynamicMemberChronicle> members = concept.getConceptAttributes().getAnnotationsDynamic();
		if (members == null)
		{
			members = new ArrayList<>();
		}
		members.add(configureDynamicRefexIndexes(concept.getPrimordialUuid(), new Integer[] {0}, revSetter));
		
		//Then add the inverse name, if present.
		if (!StringUtils.isBlank(associationInverseName))
		{
			addDescription(concept, associationInverseName, Snomed.SYNONYM_DESCRIPTION_TYPE.getPrimordialUuid(), false, LanguageCode.EN, revSetter);
		}
		
		members.add(createDynamicAnnotation(concept.getPrimordialUuid(), IsaacMetadataConstants.DYNAMIC_SEMEME_ASSOCIATION_SEMEME.getUUID(), 
				new TtkRefexDynamicData[] {}, revSetter));
		return columns;
	}
	
	/**
	 * Add a description to the concept.
	 * 
	 * @param time - if null, set to the time on the concept.
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public static TtkDescriptionChronicle addDescription(TtkConceptChronicle eConcept, String descriptionValue, UUID descriptionType, boolean preferred, 
			LanguageCode lang, Consumer<TtkRevision> revSetter)
	{
		List<TtkDescriptionChronicle> descriptions = eConcept.getDescriptions();
		if (descriptions == null)
		{
			descriptions = new ArrayList<TtkDescriptionChronicle>();
			eConcept.setDescriptions(descriptions);
		}
		TtkDescriptionChronicle description = new TtkDescriptionChronicle();
		description.setConceptUuid(eConcept.getPrimordialUuid());
		description.setLang(lang.getFormatedLanguageNoDialectCode());
		description.setPrimordialComponentUuid(UuidT5Generator.get(DescriptionCAB.descSpecNamespace,
				eConcept.getPrimordialUuid().toString() + descriptionType + lang.getFormatedLanguageNoDialectCode() + descriptionValue));
		description.setTypeUuid(descriptionType);
		description.setText(descriptionValue);
		revSetter.accept(description);
		descriptions.add(description);
		addUuidAnnotation(description, (preferred ? descriptionPreferredUuid_ : descriptionAcceptableUuid_), usEnRefsetUuid_, revSetter);
		return description;
	}
	
	/**
	 * @param time - If time is null, uses the component time.
	 * @param valueConcept - if value is null, it uses RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid()
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public static TtkRefexUuidMemberChronicle addUuidAnnotation(TtkComponentChronicle<?,?> component, UUID valueConcept, UUID refsetUuid, Consumer<TtkRevision> revSetter)
	{
		List<TtkRefexAbstractMemberChronicle<?>> annotations = component.getAnnotations();
		if (annotations == null)
		{
			annotations = new ArrayList<TtkRefexAbstractMemberChronicle<?>>();
			component.setAnnotations(annotations);
		}
		TtkRefexUuidMemberChronicle conceptRefexMember = new TtkRefexUuidMemberChronicle();
		conceptRefexMember.setReferencedComponentUuid(component.getPrimordialComponentUuid());
		conceptRefexMember.setPrimordialComponentUuid(UuidT5Generator.get(RefexCAB.refexSpecNamespace, RefexType.MEMBER.name() + refsetUuid.toString()
				+ component.getPrimordialComponentUuid().toString()));
		conceptRefexMember.setUuid1(valueConcept);
		conceptRefexMember.setAssemblageUuid(refsetUuid);
		revSetter.accept(conceptRefexMember);
		annotations.add(conceptRefexMember);
		return conceptRefexMember;
	}

	public static TtkRefexDynamicMemberChronicle createDynamicAnnotation(UUID component, UUID assemblageID, TtkRefexDynamicData[] data,
			Consumer<TtkRevision> revAttrSetter) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		//TODO this should have a validator for data columns aligning with the refex description
		TtkRefexDynamicMemberChronicle dynamicSememe = new TtkRefexDynamicMemberChronicle();
		dynamicSememe.setComponentUuid(component);
		dynamicSememe.setRefexAssemblageUuid(assemblageID);
		dynamicSememe.setData(data);
		setUUIDForDynamicSememe(dynamicSememe, data, null);
		revAttrSetter.accept(dynamicSememe);

		return dynamicSememe;
	}

	public static TtkRefexDynamicMemberChronicle configureDynamicRefexIndexes(UUID sememeToIndex, Integer[] columnConfiguration, Consumer<TtkRevision> revAttrSetter)
			throws NoSuchAlgorithmException, UnsupportedEncodingException, PropertyVetoException
	{
		TtkRefexDynamicData[] data = null;
		if (columnConfiguration != null && columnConfiguration.length > 0)
		{
			data = new TtkRefexDynamicData[1];
			TtkRefexDynamicInteger[] cols = new TtkRefexDynamicInteger[columnConfiguration.length];
			for (int i = 0; i < columnConfiguration.length; i++)
			{
				cols[i] = new TtkRefexDynamicInteger(columnConfiguration[i]);
			}

			data[0] = new TtkRefexDynamicArray<TtkRefexDynamicData>(cols);
		}

		return createDynamicAnnotation(sememeToIndex, IsaacMetadataConstants.DYNAMIC_SEMEME_INDEX_CONFIGURATION.getUUID(), data, revAttrSetter);
	}

	/**
	 * @param namespace - optional - uses {@link DynamicSememe#DYNAMIC_SEMEME_NAMESPACE} if not specified
	 * @return - the generated string used for refex creation
	 */
	public static String setUUIDForDynamicSememe(TtkRefexDynamicMemberChronicle dynamicSememe, TtkRefexDynamicData[] data, UUID namespace)
			throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		//TODO dan - need to look and see how I am generating UUIDs for dynamic sememes in the Builder...
		StringBuilder sb = new StringBuilder();
		sb.append(dynamicSememe.getRefexAssemblageUuid().toString());
		sb.append(dynamicSememe.getComponentUuid().toString());
		if (data != null)
		{
			for (TtkRefexDynamicData d : data)
			{
				if (d == null)
				{
					sb.append("null");
				}
				else
				{
					sb.append(d.getRefexDataType().getDisplayName());
					sb.append(new String(d.getData()));
				}
			}
		}
		dynamicSememe.setPrimordialComponentUuid(UuidT5Generator.get((namespace == null ? RefexCAB.refexSpecNamespace : namespace), sb.toString()));
		return sb.toString();
	}
	
	/**
	 * @param namespace - optional - uses {@link RefexCAB#refexSpecNamespace} if not specified
	 * @return - the generated string used for refex creation 
	 */
	public static String setUUIDForRefex(TtkRefexDynamicMemberChronicle dynamicSememe, TtkRefexDynamicData[] data, UUID namespace) throws NoSuchAlgorithmException, 
		UnsupportedEncodingException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(dynamicSememe.getRefexAssemblageUuid().toString()); 
		sb.append(dynamicSememe.getComponentUuid().toString());
		if (data != null)
		{
			for (TtkRefexDynamicData d : data)
			{
				if (d == null)
				{
					sb.append("null");
				}
				else
				{
					sb.append(d.getRefexDataType().getDisplayName());
					sb.append(new String(d.getData()));
				}
			}
		}
		dynamicSememe.setPrimordialComponentUuid(UuidT5Generator.get((namespace == null ? RefexCAB.refexSpecNamespace : namespace), sb.toString()));
		return sb.toString();
	}
}
