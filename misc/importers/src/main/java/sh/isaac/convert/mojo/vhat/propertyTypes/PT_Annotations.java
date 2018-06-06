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

package sh.isaac.convert.mojo.vhat.propertyTypes;

import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Annotations;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.misc.constants.VHATConstants;

public class PT_Annotations extends BPT_Annotations
{
	public enum Attribute
	{
		VUID(MetaData.VUID____SOLOR, true), CODE(MetaData.CODE____SOLOR, true), VER_EFF_DATE("Version Effective Date");

		private Property property;
		private final String p;

		private Attribute(String niceName)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below.
			property = new Property(null, niceName);
			p = niceName;
		}

		private Attribute(ConceptSpecification cs, boolean isIdentifier)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below.
			property = new Property(null, cs, isIdentifier);
			p = cs.getRegularName().get();
		}

		public Property getProperty()
		{
			return property;
		}

		public String get()
		{
			return p;
		}
	}

	public PT_Annotations()
	{
		super(VHATConstants.VHAT_ROOT_CONCEPT.getRegularName().get());
		indexByAltNames();
		for (Attribute attr : Attribute.values())
		{
			addProperty(attr.getProperty());
		}
	}
}
