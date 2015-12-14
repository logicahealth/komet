/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api;

/**
 *
 * @author kec
 */
@Deprecated
public enum ConceptModel {
    
    /**
     * The OCHRE concept model, where there are no relationships, as
     * definitions are represented as logic graphs, and concept attributes
     * are integrated with the concept itself, as the primitive/defined field
     * is not relevant as the logic graphs can represent multiple necessary and
     * sufficient sets, and full nesting of logical axioms is supported. 
     */
    OCHRE_CONCEPT_MODEL, 
}
