/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.otf.tcc.api.relationship;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 *
 * @author kec
 */
public enum RelationshipType {
    STATED_HIERARCHY, STATED_ROLE,
    INFERRED_HIERARCY, INFERRED_ROLE,
    HISTORIC, QUALIFIER;

	public static RelationshipType getRelationshipType(int refNid, int charNid) throws ValidationException, IOException {
		if (charNid == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getNid()) {
			if (refNid == SnomedMetadataRf2.NOT_REFINABLE_RF2.getNid()) {
				return STATED_HIERARCHY;
			} else if (refNid == SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getNid()) {
				return STATED_ROLE;
			}
		} else if (charNid == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getNid()) {
				if (refNid == SnomedMetadataRf2.NOT_REFINABLE_RF2.getNid()) {
					return INFERRED_HIERARCY;
				} else if (refNid == SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getNid()) {
					return INFERRED_ROLE;
				}
		} else if (charNid == SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getNid()) {
			if (refNid == SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getNid()) {
				return QUALIFIER;
			}
		} else if (charNid == SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getNid()) {
			if (refNid == SnomedMetadataRf2.NOT_REFINABLE_RF2.getNid()) {
				return HISTORIC;
			}
		}

		return null;
	}
}
