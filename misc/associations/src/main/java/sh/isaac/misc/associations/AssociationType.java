/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
package sh.isaac.misc.associations;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.utility.Frills;



public class AssociationType
{
	private int associationNid;
	private String associationName_;
	private Optional<String> associationInverseName_;
	private String description_;
	
	private static final Logger log = LogManager.getLogger();
	
	private AssociationType(int nid)
	{
		this.associationNid = nid;
	}

	/**
	 * Read all details that define an Association.  
	 * @param conceptNid The concept that represents the association assemblage
    * @param stampFilter optional - uses system default if not provided.
    * @param language optional - uses system default if not provided
	 * @return the AssociationType information
	 */
	public static AssociationType read(int conceptNid, StampFilter stampFilter, LanguageCoordinate language)
	{
		AssociationType at = new AssociationType(conceptNid);

		StampFilter localStamp = (stampFilter == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getPathCoordinate().getStampFilter() : stampFilter);
		LanguageCoordinate localLanguage = (language == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getLanguageCoordinate() : language);

		at.associationName_ = localLanguage.getDescription(conceptNid, localStamp).toStringOr(dv -> dv.getText(), "No description for " + conceptNid);
		
		//Find the inverse name
		for (DescriptionVersion desc : Frills.getDescriptionsOfType(conceptNid, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR, 
				localStamp.makeCoordinateAnalog(Status.ACTIVE)))
		{
			
			if (Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(desc.getNid(), 
					DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getNid(), false).anyMatch(nestedSemantic ->
			{
				if (nestedSemantic.getVersionType() == VersionType.DYNAMIC)
				{
					return nestedSemantic.getLatestVersion(localStamp).isPresent();
				}
				return false;
			}))
			{
				at.associationInverseName_ = Optional.of(desc.getText());
			}
		}
		
		//find the description
		for (DescriptionVersion desc : Frills.getDescriptionsOfType(at.getAssociationType(),
				MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR, localStamp.makeCoordinateAnalog(Status.ACTIVE)))
		{
			if (Frills.isDescriptionPreferred(desc.getNid(), localStamp) &&
					Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(desc.getNid(), 
							DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getNid(), false).anyMatch(nestedSemantic ->
			{
				if (nestedSemantic.getVersionType() == VersionType.DYNAMIC)
				{
					return nestedSemantic.getLatestVersion(localStamp).isPresent();
				}
				return false;
			}))
			{
				at.description_ = desc.getText();
			}
		}
		
		if (at.associationInverseName_ == null)
		{
			at.associationInverseName_ = Optional.empty();
		}
		if (at.description_ == null)
		{
			at.description_ = "-No description on path!-";
		}
		
		return at;
	}
	
	/**
	 * @return the association type concept
	 */
	public ConceptChronology getAssociationTypeConcept() 
	{
		return Get.conceptService().getConceptChronology(associationNid);
	}
	
	/**
	 * @return the concept nid of the association type concept (assemblage concept)
	 */
	public int getAssociationType() 
	{
		return associationNid;
	}
	

	public String getAssociationName()
	{
		return associationName_;
	}
	
	/**
	 * @return the inverse name of the association (if present) (Read from the association type concept)
	 */
	public Optional<String> getAssociationInverseName()
	{
		return associationInverseName_;
	}
	
	public String getDescription()
	{
		return description_;
	}
	
	/**
	 * Create and store a new mapping set in the DB.
	 * @param associationName - The name of the association (used for the FSN and preferred term of the underlying concept)
	 * @param associationInverseName - (optional) inverse name of the association (if it makes sense for the association)
	 * @param description - (optional) description that describes the purpose of the association
	 * @param referencedComponentRestriction - (optional) - may be null - if provided - this restricts the type of object referenced by the nid or
	 * UUID that is set for the referenced component in an instance of this semantic.  If {@link IsaacObjectType#UNKNOWN} is passed, it is ignored, as
	 * if it were null.
	 * @param referencedComponentSubRestriction - (optional) - may be null - subtype restriction for {@link IsaacObjectType#SEMANTIC} restrictions
	 * @param stampFilter - optional - used during the readback to create the return object.  See {@link #read(int, StampFilter, LanguageCoordinate)}
	 * @param writeCoord - optional - the edit coordinate to use when creating the association.  Uses the system default if not provided.  If provided, 
	 * and it contains a transaction, this method will NOT commit the transaction.  If provided and it does NOT contain a transaction (or not provided), 
	 * this method will create a transaction, and commit it.
	 * @return the concept nid of the created concept that carries the association definition
	 */
	public static AssociationType createAssociation(String associationName, String associationInverseName, String description, 
			IsaacObjectType referencedComponentRestriction, VersionType referencedComponentSubRestriction, StampFilter stampFilter, WriteCoordinate writeCoord) 
	{
		try 
		{
			WriteCoordinate localWriteCoord = (writeCoord == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getWriteCoordinate().get() : writeCoord);

			boolean commitTransaction = false;
			if (localWriteCoord.getTransaction().isEmpty()) {
				localWriteCoord = new WriteCoordinateImpl(Get.commitService().newTransaction(Optional.of("create association steps"), ChangeCheckerMode.ACTIVE),
						localWriteCoord);
				commitTransaction = true;
			}
			
			//We need to create a new concept - which itself is defining a dynamic semantic - so set that up here.
			DynamicUsageDescription rdud = Frills.buildNewDynamicSemanticUsageDescription(localWriteCoord,
					associationName, associationName, StringUtils.isBlank(description) ? "Defines the association type " + associationInverseName : description,
					new DynamicColumnInfo[]{
								 new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(),
											DynamicDataType.UUID, null, false)},
					DynamicConstants.get().DYNAMIC_ASSOCIATION.getNid(), referencedComponentRestriction, referencedComponentSubRestriction);

			//Then add the inverse name, if present.
			if (!StringUtils.isBlank(associationInverseName)) {
				Chronology builtDesc = LookupService.get().getService(DescriptionBuilderService.class)
						  .getDescriptionBuilder(associationInverseName, rdud.getDynamicUsageDescriptorNid(),
									 MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR, MetaData.ENGLISH_LANGUAGE____SOLOR).buildAndWrite(localWriteCoord).getNoThrow();

				Get.semanticBuilderService().getDynamicBuilder(builtDesc.getNid(), DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getAssemblageNid())
						.buildAndWrite(localWriteCoord).getNoThrow();
			}

			//Add the association marker semantic
			Get.semanticBuilderService().getDynamicBuilder(rdud.getDynamicUsageDescriptorNid(),
					DynamicConstants.get().DYNAMIC_ASSOCIATION.getNid())
					.buildAndWrite(localWriteCoord).getNoThrow();

			if (commitTransaction) {
				localWriteCoord.getTransaction().get().commit().get();
			}

		return read(rdud.getDynamicUsageDescriptorNid(), stampFilter, Get.languageCoordinateService().getUsEnglishLanguageRegularTermCoordinate());
	}
	catch (Exception e)
	{
		log.error("Unexpected error creating association", e);
		throw new RuntimeException(e);
		}
	}
}
