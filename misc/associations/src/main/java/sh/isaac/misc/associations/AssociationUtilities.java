/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.model.semantic.DynamicUtilityImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.utility.Frills;


/**
 * {@link AssociationUtilities}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AssociationUtilities
{
	private static int associationNid = Integer.MIN_VALUE;
	private static Logger log = LogManager.getLogger();
	
	private static int getAssociationNid()
	{
		if (associationNid == Integer.MIN_VALUE)
		{
			associationNid = DynamicConstants.get().DYNAMIC_ASSOCIATION.getNid();
		}
		return associationNid;
	}
	
	/**
	 * Get a particular associations 
	 * @param associationNid
	 * @param stampFilter - optional - if not provided, uses the default from the config service
	 * @return the found associationInstance, if present on the provided stamp path
	 */
	public static Optional<AssociationInstance> getAssociation(int associationNid, StampFilter stampFilter)
	{
		StampFilter localStamp = stampFilter == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getPathCoordinate().getStampFilter() : stampFilter;
		SemanticChronology sc = Get.assemblageService().getSemanticChronology(associationNid);
		LatestVersion<Version> latest = sc.getLatestVersion(localStamp);
		if (latest.isPresent())
		{
			return Optional.of(AssociationInstance.read((DynamicVersion)latest.get(), localStamp));
		}
		return Optional.empty();
	}

	/**
	 * Get all associations that originate on the specified componentNid
	 * @param componentNid
	 * @param stampFilter - optional - if not provided, uses the default from the config service
	 * @return the associations
	 */
	public static List<AssociationInstance> getSourceAssociations(int componentNid, StampFilter stampFilter)
	{
		ArrayList<AssociationInstance> results = new ArrayList<>();
		StampFilter localStamp = stampFilter == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getPathCoordinate().getStampFilter() : stampFilter;
		
		Set<Integer> associationTypes = getAssociationConceptNids();
		if (associationTypes.size() == 0) 
		{
			 return results;
		}
		Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblages(componentNid, associationTypes)
			.forEach(associationC -> 
				{
					LatestVersion<Version> latest = associationC.getLatestVersion(localStamp);
					if (latest.isPresent())
					{
						if (latest.get().getSemanticType() == VersionType.DYNAMIC) 
						{
							results.add(AssociationInstance.read((DynamicVersion)latest.get(), stampFilter));
						}
						else
						{
							log.warn("Got back {} when by design, we should only be getting DynamicVersions!", latest.get());
						}
					}
					
				});
		return results;
	}

	/**
	 * Get all association instances that have a target of the specified componentNid
	 * @param componentNid
	 * @param stampFilter - optional - if not provided, uses the default from the config service
	 * @return  the association instances
	 */
	//TODO [DAN 3] should probably have a method here that takes in a target UUID, since that seems to be how I stored them?
	public static List<AssociationInstance> getTargetAssociations(int componentNid, StampFilter stampFilter)
	{
		ArrayList<AssociationInstance> result = new ArrayList<>();

		IndexSemanticQueryService indexer = LookupService.getService(IndexSemanticQueryService.class);
		if (indexer == null)
		{
			throw new RuntimeException("Required index is not available");
		}
		
		ArrayList<Integer> associationTypes = new ArrayList<>();
//		ArrayList<Integer> colIndex = new ArrayList<>();
		
		try
		{
			UUID uuid = Get.identifierService().getUuidPrimordialForNid(componentNid);

			for (Integer associationTypeSequenece : getAssociationConceptNids())
			{
				associationTypes.add(associationTypeSequenece);
//				colIndex.add(findTargetColumnIndex(associationTypeSequenece));
			}
			
			//TODO [DAN 3] when issue with colIndex restrictions is fixed, put it back.
			List<SearchResult> refexes = indexer.queryData(new DynamicStringImpl(uuid.toString()),
					false, associationTypes.stream().mapToInt(i->i).toArray(), null, null, null, null, null, null);
			for (SearchResult sr : refexes)
			{
				LatestVersion<DynamicVersion> latest = Get.assemblageService().getSnapshot(DynamicVersion.class, 
						stampFilter == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getPathCoordinate().getStampFilter() : stampFilter)
						.getLatestSemanticVersion(sr.getNid());
				
				if (latest.isPresent())
				{
					result.add(AssociationInstance.read(latest.get(), stampFilter));
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * 
	 * @param associationTypeConceptNid
	 * @param stampFilter - optional - if not provided, uses the default from the config service
	 * @return the associations of the specified type
	 */
	public static List<AssociationInstance> getAssociationsOfType(int associationTypeConceptNid, StampFilter stampFilter)
	{
		ArrayList<AssociationInstance> results = new ArrayList<>();
		StampFilter localFilter = stampFilter == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getPathCoordinate().getStampFilter() : stampFilter;
		Get.assemblageService().getSemanticChronologyStream(associationTypeConceptNid)
			.forEach(associationC -> 
				{
					LatestVersion<Version> latest = associationC.getLatestVersion(localFilter);
					if (latest.isPresent())
					{
						results.add(AssociationInstance.read((DynamicVersion)latest.get(), stampFilter));
					}
					
				});
		return results;
	}

	/**
	 * @return a list of all of the concepts that identify a type of association - returning their concept nid identifier.
	 */
	public static Set<Integer> getAssociationConceptNids()
	{
		HashSet<Integer> result = new HashSet<>();

		Get.assemblageService().getSemanticChronologyStream(getAssociationNid()).forEach(associationC ->
		{
			result.add(associationC.getReferencedComponentNid());
		});
		return result;
	}

	/**
	 * @param assemblageNidOrSequence
	 */
	protected static int findTargetColumnIndex(int assemblageNidOrSequence)
	{
		DynamicUsageDescription rdud = LookupService.get().getService(DynamicUtilityImpl.class).readDynamicUsageDescription(assemblageNidOrSequence);

		for (DynamicColumnInfo rdci : rdud.getColumnInfo())
		{
			if (rdci.getColumnDescriptionConcept().equals(DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid()))
			{
				return rdci.getColumnOrder();
			}
		}
		return Integer.MIN_VALUE;
	}
	
	public static boolean isAssociation(SemanticChronology sc)
	{
		return Frills.definesAssociation(sc);
	}
}
