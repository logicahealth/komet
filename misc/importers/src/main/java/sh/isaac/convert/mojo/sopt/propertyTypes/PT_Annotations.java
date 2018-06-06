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

package sh.isaac.convert.mojo.sopt.propertyTypes;

import sh.isaac.MetaData;
import sh.isaac.convert.mojo.sopt.data.SOPTDataColumnsV1;
import sh.isaac.convert.mojo.sopt.data.SOPTValueSetColumnsV1;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Annotations;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;

public class PT_Annotations extends BPT_Annotations
{
	public PT_Annotations()
	{
		super("SOPT");
		indexByAltNames();
		addProperty(SOPTDataColumnsV1.CodeSystemOID.name());
		addProperty(SOPTDataColumnsV1.CodeSystemVersion.name());
		Property p = new Property(MetaData.CODE____SOLOR, true);  //three different columns mapped here... not sure if I need to track that mapping anywhere. 
		p.setSourcePropertyAltName(SOPTDataColumnsV1.CodeSystemCode.name());
		addProperty(p);
		addPropertyAltName(p, SOPTDataColumnsV1.ConceptCode.name());
		addProperty(SOPTDataColumnsV1.HL7Table0396Code.name(), true);
		addProperty(SOPTDataColumnsV1.PreferredAlternateCode.name(), true);
		
		
		addPropertyAltName(p, SOPTValueSetColumnsV1.ValueSetCode.name());
		addProperty(SOPTValueSetColumnsV1.ValueSetOID.name());
		addProperty(SOPTValueSetColumnsV1.ValueSetReleaseComments.name());
		addProperty(SOPTValueSetColumnsV1.ValueSetStatus.name());
		addProperty(SOPTValueSetColumnsV1.ValueSetUpdatedDate.name());
		addProperty(SOPTValueSetColumnsV1.ValueSetVersion.name());
	}
}
