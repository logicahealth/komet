/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
 * @author kec
 */
public class RefexAux {
        public static ConceptSpec VIEWER_IMAGE =
            new ConceptSpec("viewer image",
            UUID.fromString("5f5be40f-24c1-374f-bd04-4a5003e366ea"));

        public static ConceptSpec BOOLEAN_CIRCLE_ICONS_FALSE =
            new ConceptSpec("false with forbidden icon",
            UUID.fromString("89138782-bfe6-3e0a-8796-2e882260b327"));

        public static ConceptSpec BOOLEAN_CIRCLE_ICONS_TRUE =
            new ConceptSpec("true with circle check icon",
            UUID.fromString("00ff1b42-1b98-31f7-ba67-af3181ce1175"));

}
