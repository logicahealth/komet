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
package sh.isaac.convert.mojo.rf2Direct;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.converters.sharedUtils.config.ConfigOptionsDescriptor;
import sh.isaac.pombuilder.converter.ContentConverterCreator;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.ConverterOptionParamSuggestedValue;

/**
 * 
 * {@link Rf2DirectConfigOptions}
 *
 * Descriptions of the conversion options that this converter expects, in a form that can be automatically 
 * converted to a json file to be published with the mojo, for easy consumption and eventual inclusion into the 
 * GUI.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
public class Rf2DirectConfigOptions implements ConfigOptionsDescriptor
{
   public ConverterOptionParam[] getConfigOptions() {
      return new ConverterOptionParam[] { 
            new ConverterOptionParam("Classifiers to process", ContentConverterCreator.CLASSIFIERS_OPTION,
                  "The classifiers to process.  Defaults to 'Snapshot, Full' in pom mode.  Defaults to 'Snapshot' in direct mode", 
                  true, 
                  true,
                  false,
                  new String[] {"Snapshot"},
                  new ConverterOptionParamSuggestedValue("Snapshot", "Process the Snapshot portion of the RF2 content"),
                  new ConverterOptionParamSuggestedValue("Full", "Process the Full portion of the RF2 content"),
                  new ConverterOptionParamSuggestedValue("Snapshot-Active-Only", "Process the Snapshot portion of the RF2 content, and only maintain the active components")
            )};
   }
   
   @Override
   public String getName()
   {
      return "convert-RF2-direct-to-ibdf";
   }
}
