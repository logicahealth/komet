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
package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 *
 * @author AKF
 */
public class CaseSensitive {
    public static ConceptSpec CS_WORDS_REFSET =
            new ConceptSpec("Case Sensitive Words Refset",
            UUID.fromString("715967cc-104a-30f6-990c-2cf926b28895"));
    public static ConceptSpec IC_SIGNIFICANT =
            new ConceptSpec("unchangable",
            UUID.fromString("71889d90-a033-3859-a8aa-230aa8901667"));
    public static ConceptSpec MAYBE_IC_SIGNIFICANT =
            new ConceptSpec("maybe ics",
            UUID.fromString("2ebcc864-cb10-3ac9-9bba-a21358e0a266"));
}
