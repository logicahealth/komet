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

package sh.isaac.convert.mojo.rxnorm;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.converters.sharedUtils.config.ConfigOptionsDescriptor;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.ConverterOptionParamSuggestedValue;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link RxNormConfigOptions}
 *
 * Descriptions of the conversion options that this converter expects, in a form that can be automatically converted to a json file to be
 * published with the mojo, for easy consumption and eventual inclusion into the GUI.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
public class RxNormConfigOptions implements ConfigOptionsDescriptor {
   /**
    * Gets the config options.
    *
    * @return the config options
    */
   @Override
   public ConverterOptionParam[] getConfigOptions() {
      return new ConverterOptionParam[] {
            new ConverterOptionParam("TTY Restriction", "ttyRestriction",
                  "An optional list of TTY types which should be included.  If no selection is made, concepts are created from all CUI's that"
                        + " match the SAB selection list.  If provided, we only create concepts where the RxCUI has an entry with a TTY that matches one"
                        + " or more of the TTY's provided here.  Providing a small set of TTYs here will drastically reduce the amount of content imported"
                        + " from RxNorm.  The default here is to not make any selection.",
                  true, true, true, 
                  new String[] {}, 
                  new ConverterOptionParamSuggestedValue("IN", "Name for an ingredient"), new ConverterOptionParamSuggestedValue("SCD", "Semantic Clinical Drug"),
                  new ConverterOptionParamSuggestedValue("SCDF", "Semantic clinical drug and form"), new ConverterOptionParamSuggestedValue("SCDG", "Semantic clinical drug group"),
                  new ConverterOptionParamSuggestedValue("DF", "Dose Form"), new ConverterOptionParamSuggestedValue("SBD", "Semantic branded drug"),
                  new ConverterOptionParamSuggestedValue("BN", "Fully-specified drug brand name that can not be prescribed")), // this isn't the complete list of TTYs
            new ConverterOptionParam("SABs to Include", "sabsToInclude", 
                  "An optional list of SABs which should be included.  The SAB RXNORM is always included.  Use this parameter to specify others to include."
                  + " The SABs required by the FileMan effort are VANDF, NDDF, MMSL and ATC.  NDFRT should be included for Pharamacy VUID searches.",
                  true,
                  true,
                  true,
                  new String[] {"RXNORM", "VANDF", "NDDF", "MMSL", "ATC", "NDFRT"},
                  new ConverterOptionParamSuggestedValue("ATC", "Anatomical Therapeutic Chemical Classification System"),
                  new ConverterOptionParamSuggestedValue("DRUGBANK", "Drug Bank"),
                  new ConverterOptionParamSuggestedValue("GS", "Gold Standard Drug Database"), new ConverterOptionParamSuggestedValue("MDDB", "Master Drug Data Base"),
                  new ConverterOptionParamSuggestedValue("MMSL", "Multum MediSource Lexicon"), new ConverterOptionParamSuggestedValue("MMX", "Micromedex RED BOOK"),
                  new ConverterOptionParamSuggestedValue("MSH", "Medical Subject Headings"),
                  new ConverterOptionParamSuggestedValue("MTHCMSFRF", "Metathesaurus CMS Formulary Reference File"),
                  new ConverterOptionParamSuggestedValue("MTHSPL", "Metathesaurus FDA Structured Product Labels"),
                  new ConverterOptionParamSuggestedValue("NDDF", "FDB MedKnowledge (formerly NDDF Plus)"), new ConverterOptionParamSuggestedValue("NDFRT", "National Drug File"),
                  new ConverterOptionParamSuggestedValue("NDFRT_FDASPL", "National Drug File - FDASPL"),
                  new ConverterOptionParamSuggestedValue("NDFRT_FMTSME", "National Drug File - FMTSME"),
                  new ConverterOptionParamSuggestedValue("SNOMEDCT_US", "US Edition of SNOMED CT"),
                  new ConverterOptionParamSuggestedValue("VANDF", "Veterans Health Administration National Drug File")) };
   }

   /**
    * Gets the name.
    *
    * @return the name
    */
   @Override
   public String getName() {
      return "convert-rxnorm-to-ibdf";
   }
}
