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
package gov.vha.isaac.ochre.api.chronicle;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author kec
 * @param <V>
 */
public class LatestVersion<V extends Object>  {
    
    V value;
    
    Optional<Set<V>> contradictions;
    
    public LatestVersion() {
        value = null;
        contradictions = Optional.empty();
    };

    public LatestVersion(V latest) {
        this.value = latest;
        contradictions = Optional.empty();
    }

    public LatestVersion(V latest, Collection<V> contradictions) {
        this.value = latest;
        if (contradictions == null) {
            this.contradictions = Optional.empty();
        } else {
            this.contradictions = Optional.of(new HashSet<V>(contradictions));
        }
    }
    
    public void addLatest(V value) {
        if (this.value == null) {
            this.value = value;
        } else {
            if (!contradictions.isPresent()) {
                contradictions = Optional.of(new HashSet<V>());
            }
            contradictions.get().add(value);
        }
    }
    
    
    public V value() {
        return value;
    }
    
    public Optional<Set<V>> contradictions() {
        return contradictions;
    }
}
