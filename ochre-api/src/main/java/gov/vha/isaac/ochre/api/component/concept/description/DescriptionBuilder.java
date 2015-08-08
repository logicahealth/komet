/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.component.concept.description;

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.IdentifiedComponentBuilder;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;

/**
 *
 * @author kec
 * @param <T>
 * @param <V>
 */
public interface DescriptionBuilder<T extends ObjectChronology<?>, V extends StampedVersion> 
    extends IdentifiedComponentBuilder<T> {
    DescriptionBuilder<?,?> setPreferredInDialectAssemblage(ConceptProxy dialectAssemblage);
    DescriptionBuilder<?,?> setAcceptableInDialectAssemblage(ConceptProxy dialectAssemblage);
}
