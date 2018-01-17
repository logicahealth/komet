/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.bootstrap;

import java.util.UUID;
import sh.isaac.api.ConceptProxy;

/**
 *
 * @author kec
 */
public class TestConcept {
    public static final ConceptProxy HOMOCYSTINE_MV_URINE = 
            new ConceptProxy("Homocystine [Mass/volume] in Urine", 
                    UUID.fromString("ada7be10-0c94-5ed9-9fdd-cf537b829db4"));
}
