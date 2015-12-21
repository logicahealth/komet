/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package gov.vha.isaac.ochre.query.provider.lucene;

import org.apache.lucene.search.similarities.DefaultSimilarity;

/**
 *
 * @author kec
 */
class ShortTextSimilarity extends DefaultSimilarity {

    public ShortTextSimilarity() {
    }

    @Override
    public float coord(int overlap, int maxOverlap) {
        return 1.0f;
    }

    @Override
    public float tf(float freq) {
        return 1.0f;
    }
    
}
