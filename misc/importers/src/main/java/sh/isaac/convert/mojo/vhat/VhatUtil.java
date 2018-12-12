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

package sh.isaac.convert.mojo.vhat;

import java.util.UUID;
import java.util.function.Function;
import sh.isaac.api.constants.MetadataConceptConstant;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.misc.constants.VHATConstants;

/**
 * Sanity checks for all of the constants in VHATConstants - to make sure we know if some framework change upset the UUID generation patterns
 * {@link VhatUtil}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class VhatUtil
{
	/**
	 * @param dwh
	 * @param rootConcept
	 * @param missingConcept
	 */
	public static void check(DirectWriteHelper dwh, UUID rootConcept, UUID missingConcept)
	{
		check((name) -> dwh.getAssociationType(name), VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE);
		check((name) -> dwh.getDescriptionType(name), VHATConstants.VHAT_ABBREVIATION);
		check((name) -> dwh.getDescriptionType(name), VHATConstants.VHAT_FULLY_SPECIFIED_NAME);
		check((name) -> dwh.getDescriptionType(name), VHATConstants.VHAT_PREFERRED_NAME);
		check((name) -> dwh.getDescriptionType(name), VHATConstants.VHAT_SYNONYM);
		check((name) -> dwh.getDescriptionType(name), VHATConstants.VHAT_VISTA_NAME);
		check((name) -> dwh.getRefsetType(name), VHATConstants.VHAT_ALL_CONCEPTS);
		if (!dwh.getAssociationTypesNode().get().equals(VHATConstants.VHAT_ASSOCIATION_TYPES.getPrimordialUuid()))
		{
			throw new RuntimeException("Constants file doesn't match for " + VHATConstants.VHAT_ASSOCIATION_TYPES.toExternalString());
		}
		if (!dwh.getAttributeTypesNode().get().equals(VHATConstants.VHAT_ATTRIBUTE_TYPES.getPrimordialUuid()))
		{
			throw new RuntimeException("Constants file doesn't match for " + VHATConstants.VHAT_ATTRIBUTE_TYPES.toExternalString());
		}
		if (!dwh.getRefsetTypesNode().get().equals(VHATConstants.VHAT_REFSETS.getPrimordialUuid()))
		{
			throw new RuntimeException("Constants file doesn't match for " + VHATConstants.VHAT_REFSETS.toExternalString());
		}
		if (!dwh.getDescriptionTypesNode().get().equals(VHATConstants.VHAT_DESCRIPTION_TYPES.getPrimordialUuid()))
		{
			throw new RuntimeException("Constants file doesn't match for " + VHATConstants.VHAT_DESCRIPTION_TYPES.toExternalString());
		}

		if (!VHATConstants.VHAT_ROOT_CONCEPT.getPrimordialUuid().equals(rootConcept))
		{
			throw new RuntimeException("Root concept got an unexpected UUID! " + rootConcept);
		}

		if (!VHATConstants.VHAT_MISSING_SDO_CODE_SYSTEM_CONCEPTS.getPrimordialUuid().equals(missingConcept))
		{
			throw new RuntimeException("Missing concept got an unexpected UUID! " + missingConcept);
		}
	}
	
	private static void check(Function<String, UUID> read, MetadataConceptConstant c)
	{
		UUID uuid = read.apply(c.getRegularName().get());
		if (uuid == null || !c.getPrimordialUuid().equals(uuid))
		{
			throw new RuntimeException("Constants file doesn't match for " + c.toExternalString() + " : " + uuid);
		}
	}
}