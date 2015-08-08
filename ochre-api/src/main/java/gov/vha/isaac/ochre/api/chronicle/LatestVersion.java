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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author kec
 * @param <V>
 */
public final class LatestVersion<V>  {
    
    V value;
    
    Optional<Set<V>> contradictions;
    
    public LatestVersion(Class<V> versionType) {
        contradictions = Optional.empty();
    }

    public LatestVersion(V latest) {
        this.value = Objects.requireNonNull(latest, "latest version cannot be null");
        contradictions = Optional.empty();
    }

    public LatestVersion(List<V> versions) {
        this.value = Objects.requireNonNull(versions.get(0), "latest version cannot be null");
        if (versions.size() < 2) {
            contradictions = Optional.empty();
        } else {
            contradictions = Optional.of(new HashSet<>(versions.subList(1, versions.size())));
        }
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
    
    public Stream<V> versionStream() {
        Stream.Builder<V> builder = Stream.builder();
        if (value == null) {
            return Stream.<V>builder().build();
        }
        builder.accept(value);
        if (contradictions.isPresent()) {
            contradictions.get().forEach((contradiction) -> {
                builder.add(contradiction); 
            });
        }
        return builder.build();
    }
    
    @Override
    public String toString() {
        return "LatestVersion{" + "value=" + value + ", contradictions=" + contradictions + '}';
    }
}
