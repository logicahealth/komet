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
package sh.isaac.convert.mojo.loinc;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.converters.sharedUtils.config.ConfigOptionsDescriptor;
import sh.isaac.pombuilder.converter.ContentConverterCreator;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.api.importers.ConverterOptionParamSuggestedValue;

/**
 * 
 * {@link LoincConfigOptions}
 *
 * Descriptions of the conversion options that this converter expects, in a form that can be automatically
 * converted to a json file to be published with the mojo, for easy consumption and eventual inclusion into the
 * GUI.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
public class LoincConfigOptions implements ConfigOptionsDescriptor
{
	@Override
	public ConverterOptionParam[] getConfigOptions()
	{
		return new ConverterOptionParam[] { 
				new ConverterOptionParam("Conversion Style", ContentConverterCreator.CLASSIFIERS_OPTION,
				"The converter to use.  Defaults to 'native', if not specified.", 
				true, false, false, 
				new String[] { "native" },
				new ConverterOptionParamSuggestedValue("native", "Load Loinc using the native, full standalone conversion process"),
				new ConverterOptionParamSuggestedValue("solor", "Load Loinc using the solor load / conversion process")) };
	}

	@Override
	public String getName()
	{
		return "convert-loinc-to-ibdf";
	}
}
