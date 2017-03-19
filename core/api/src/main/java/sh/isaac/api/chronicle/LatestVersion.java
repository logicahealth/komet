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
 * The Class LatestVersion.
 *
 * @author kec
 * @param <V> the value type
 */
public final class LatestVersion<V> {
   /** The value. */
   V value;

   /** The contradictions. */
   Optional<Set<V>> contradictions;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new latest version.
    *
    * @param versionType the version type
    */
   public LatestVersion(Class<V> versionType) {
      this.contradictions = Optional.empty();
   }

   /**
    * Instantiates a new latest version.
    *
    * @param versions the versions
    */
   public LatestVersion(List<V> versions) {
      this.value = Objects.requireNonNull(versions.get(0), "latest version cannot be null");

      if (versions.size() < 2) {
         this.contradictions = Optional.empty();
      } else {
         this.contradictions = Optional.of(new HashSet<>(versions.subList(1, versions.size())));
      }
   }

   /**
    * Instantiates a new latest version.
    *
    * @param latest the latest
    */
   public LatestVersion(V latest) {
      this.value          = Objects.requireNonNull(latest, "latest version cannot be null");
      this.contradictions = Optional.empty();
   }

   /**
    * Instantiates a new latest version.
    *
    * @param latest the latest
    * @param contradictions the contradictions
    */
   public LatestVersion(V latest, Collection<V> contradictions) {
      this.value = latest;

      if (contradictions == null) {
         this.contradictions = Optional.empty();
      } else {
         this.contradictions = Optional.of(new HashSet<>(contradictions));
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the latest.
    *
    * @param value the value
    */
   public void addLatest(V value) {
      if (this.value == null) {
         this.value = value;
      } else {
         if (!this.contradictions.isPresent()) {
            this.contradictions = Optional.of(new HashSet<>());
         }

         this.contradictions.get()
                            .add(value);
      }
   }

   /**
    * Contradictions.
    *
    * @return the optional
    */
   public Optional<Set<V>> contradictions() {
      return this.contradictions;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "LatestVersion{" + "value=" + this.value + ", contradictions=" + this.contradictions + '}';
   }

   /**
    * Value.
    *
    * @return the v
    */
   public V value() {
      return this.value;
   }

   /**
    * Version stream.
    *
    * @return the stream
    */
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

