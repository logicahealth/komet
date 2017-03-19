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



package sh.isaac.model.builder;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
public class DescriptionBuilderProvider
         implements DescriptionBuilderService {
   @Override
   public DescriptionBuilder<? extends SememeChronology<?>,
                             DescriptionSememeImpl> getDescriptionBuilder(String descriptionText,
                                   ConceptBuilder conceptBuilder,
                                   ConceptSpecification descriptionType,
                                   ConceptSpecification languageForDescription) {
      return new DescriptionBuilderOchreImpl(descriptionText, conceptBuilder, descriptionType, languageForDescription);
   }

   @Override
   public DescriptionBuilder<? extends SememeChronology<?>,
                             DescriptionSememeImpl> getDescriptionBuilder(String descriptionText,
                                   int conceptSequence,
                                   ConceptSpecification descriptionType,
                                   ConceptSpecification languageForDescription) {
      return new DescriptionBuilderOchreImpl(descriptionText, conceptSequence, descriptionType, languageForDescription);
   }
}

