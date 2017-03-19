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



package sh.isaac.api.chronicle;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <V>
 */
public final class LatestVersion<V> {
   V                value;
   Optional<Set<V>> contradictions;

   //~--- constructors --------------------------------------------------------

   public LatestVersion(Class<V> versionType) {
      this.contradictions = Optional.empty();
   }

   public LatestVersion(List<V> versions) {
      this.value = Objects.requireNonNull(versions.get(0), "latest version cannot be null");

      if (versions.size() < 2) {
         this.contradictions = Optional.empty();
      } else {
         this.contradictions = Optional.of(new HashSet<>(versions.subList(1, versions.size())));
      }
   }

   public LatestVersion(V latest) {
      this.value     = Objects.requireNonNull(latest, "latest version cannot be null");
      this.contradictions = Optional.empty();
   }

   public LatestVersion(V latest, Collection<V> contradictions) {
      this.value = latest;

      if (contradictions == null) {
         this.contradictions = Optional.empty();
      } else {
         this.contradictions = Optional.of(new HashSet<V>(contradictions));
      }
   }

   //~--- methods -------------------------------------------------------------

   public void addLatest(V value) {
      if (this.value == null) {
         this.value = value;
      } else {
         if (!this.contradictions.isPresent()) {
            this.contradictions = Optional.of(new HashSet<V>());
         }

         this.contradictions.get()
                       .add(value);
      }
   }

   public Optional<Set<V>> contradictions() {
      return this.contradictions;
   }

   @Override
   public String toString() {
      return "LatestVersion{" + "value=" + this.value + ", contradictions=" + this.contradictions + '}';
   }

   public V value() {
      return this.value;
   }

   public Stream<V> versionStream() {
      final Stream.Builder<V> builder = Stream.builder();

      if (this.value == null) {
         return Stream.<V>builder()
                      .build();
      }

      builder.accept(this.value);

      if (this.contradictions.isPresent()) {
         this.contradictions.get().forEach((contradiction) -> {
                                   builder.add(contradiction);
                                });
      }

      return builder.build();
   }
}

