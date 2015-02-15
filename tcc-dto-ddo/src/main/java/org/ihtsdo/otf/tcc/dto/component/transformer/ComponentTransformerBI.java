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



package org.ihtsdo.otf.tcc.dto.component.transformer;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.dto.component.TtkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

/**
 *
 * @author kec
 */
public interface ComponentTransformerBI {
    UUID transform(UUID input, TtkRevision component, ComponentFields field);
    
    UUID transform(UUID input, TtkConceptChronicle concept, ComponentFields field);

    String transform(String input, TtkRevision component, ComponentFields field);

    int transform(int input, TtkRevision component, ComponentFields field);

    long transform(long input, TtkRevision component, ComponentFields field);

    float transform(float input, TtkRevision component, ComponentFields field);

    boolean transform(boolean input, TtkRevision component, ComponentFields field);
    
    boolean transform(boolean input, TtkConceptChronicle concept, ComponentFields field);
    
    byte[][] transform(byte[][] input, TtkRevision component, ComponentFields field);
    
    byte[] transform(byte[] input, TtkRevision component, ComponentFields field);
}
