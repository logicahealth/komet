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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import sh.isaac.api.Status;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.identity.StampedVersion;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LatestVersion.
 *
 * @author kec
 * @param <V> the value type
 * TODO [KEC] search for all get() methods to make sure test for isPresent() is completed. 
 */
public final class LatestVersion<V> {
   
    private static final LatestVersion<?> EMPTY = new LatestVersion<>();
   /** The value. */
   V value;

   /** The contradictions. */
   Set<V> contradictions;

   //~--- constructors --------------------------------------------------------
   public LatestVersion() {}

   /**
    * Instantiates a new latest version.
    *
    * @param versionType the version type
    */
   public LatestVersion(Class<V> versionType) {}

   /**
    * Instantiates a new latest version.
    *
    * @param versions the versions
    */
   public LatestVersion(List<V> versions) {
      this.value = Objects.requireNonNull(versions.get(0), "latest version cannot be null");

      if (versions.size() < 2) {
         this.contradictions = null;
      } else {
         this.contradictions = new HashSet<>(versions.subList(1, versions.size()));
      }
   }

   /**
    * Instantiates a new latest version.
    *
    * @param latest the latest
    */
   public LatestVersion(V latest) {
      this.value          = Objects.requireNonNull(latest, "latest version cannot be null");
      this.contradictions = null;
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
         this.contradictions = null;
      } else {
         this.contradictions = new HashSet<>(contradictions);
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
         if (this.contradictions == null) {
            this.contradictions = new HashSet<>();
         }

         this.contradictions.add(value);
      }
   }
   
   /**
    * 
    * @param consumer the consumer to process the value if it is present.
    * @return the latest version unmodified for use in a fluent API manner. 
    */
   public LatestVersion<V> ifPresent(Consumer<? super V> consumer) {
      if (value != null) {
         consumer.accept(this.value);
      }
      return this;
   }
   
   /**
    * Return true if there is a value present, otherwise false.
    * @return true if there is a value present, otherwise false.
    */
   public boolean isPresent() {
      return value != null;
   }
   
   /**
    * Return false if there is no value present, otherwise, passes the value into the supplied customCheck, and returns that response.
    * @param customCheck The test to run, if the value is present.
    * @return true if present and customCheck returns true, otherwise, false.
    */
   public boolean isPresentAnd(Predicate<V> customCheck) {
      if (value == null) {
         return false;
      }
      else {
         return customCheck.test(value);
      }
   }
   
   /**
    * Return true if there is a value absent, otherwise false.
    * @return true if the value absent, otherwise false.
    */
   public boolean isAbsent() {
      return value == null;
   }
   
   /**
    * Return the value if present, otherwise return other.
    * @param other
    * @return the value if present, otherwise return other.
    */
   public V orElse(V other) {
      if (this.value != null) {
         return this.value;
      }
      return other;
   }
   
   /**
    * Return the value if present, otherwise invoke other and return the result of that invocation.
    * @param other
    * @return the value if present, otherwise invoke other and return the result of that invocation.
    */
   public V orElseGet(Supplier<? extends V> other) {
      if (this.value != null) {
         return this.value;
      }
      return other.get();
   }
   /**
    * Execute the runnable to execute if the value is present.
    * @param runnable the runnable to execute if the value is present
    * @return the latest version unmodified for use in a fluent API manner. 
    */
   public LatestVersion<V> ifAbsent(Runnable runnable) {
      if (value == null) {
         runnable.run();
      }
      return this;
   }
           
   /**
    * Return the contained value, if present, otherwise throw an exception to be created by the provided supplier.
    * @param <X> Type of the exception to be thrown
    * @param exceptionSupplier The supplier which will return the exception to be thrown
    * @return the present value
    * @throws X if there is no value present
    */
   public <X extends Throwable> V orElseThrow(Supplier<? extends X> exceptionSupplier) 
      throws X {
      if (this.value != null) {
         return this.value;
      }
      throw exceptionSupplier.get();
   }
   /**
    * Read-only set of contradictions.
    *
    * @return the optional
    */
   public Set<V> contradictions() {
      if (this.contradictions == null) {
         return Collections.emptySet();
      }
      return this.contradictions;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "LatestVersion{" + this.value + ", contradictions=" + contradictions() + '}';
   }

   /**
    * The latest version value
    * @return the latest version
    * @throws NoSuchElementException - if there is no value present
    * @see isPresent()
    */
   public V get() {
      if (this.value == null) {
         throw new NoSuchElementException();
      }
      return this.value;
   }

   /**
    * If a value is present, and the value matches the given predicate, return an Optional describing the value, otherwise return an empty Optional.
    * @param predicate a predicate to apply to the value, if present
    * @return an Optional describing the value of this Optional if a value is present and the value matches the given predicate, otherwise an empty Optional
    */
   public LatestVersion<V> filter(Predicate<LatestVersion<V>> predicate) {
      if (predicate.test(this)) {
         return this;
      }
      return new LatestVersion<>();
   }
           
   /**
    * If a value is present, apply the provided mapping function to it, and if the result is non-null, 
    * return an Optional describing the result. Otherwise return an empty Optional.
    * @param <U> The type of the result of the mapping function
    * @param mapper a mapping function to apply to the value, if present
    * @return an Optional describing the result of applying a mapping function to the value of this Optional, if a value is present, otherwise an empty Optional
    */
   public <U> LatestVersion<U> map(Function<? super LatestVersion<V>,? extends LatestVersion<U>> mapper) {
      return mapper.apply(this);
   }
   
   /**
    * Stream of the latest values (if more that one latest value is computed, then
    * all are included in this stream), including all contradictions.
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

      if (this.contradictions != null) {
         this.contradictions.forEach((contradiction) -> {
                                        builder.add(contradiction);
                                     });
      }

      return builder.build();
   }
   
   public List<V> versionList() {
      if (this.value == null) {
         return Collections.emptyList();
      }
      if (this.contradictions == null) {
         return Arrays.asList(this.value);
      }
      ArrayList<V> versions = new ArrayList<>(this.contradictions.size() + 1);
      versions.add(value);
      versions.addAll(this.contradictions);
      return versions;
   }
   
   /**
    * Note, this method assumes that the type extends {@link StampedVersion} - it returns an empty set, if they dont.
    * @return
    */
   public StampSequenceSet getStamps() {
      StampSequenceSet stampSequences = new StampSequenceSet();
      versionStream().forEach((v) -> {
         if (v instanceof StampedVersion) {
            stampSequences.add(((StampedVersion) v).getStampSequence());
         }
      });
      return stampSequences;
   }
   
   /**
    * Note, this method assumes that the type extends {@link StampedVersion} - Its a noop if it doesn't.
    */
   public void sortByState() {
      if (value != null && value instanceof StampedVersion && ((StampedVersion)value).getStatus() != Status.ACTIVE && contradictions != null) {
         //See if we have an active one to swap it with.
         for (V c : contradictions) {
            if (((StampedVersion)c).getStatus() == Status.ACTIVE) {
               contradictions.remove(c);
               contradictions.add(value);
               value = c;
               break;
            }
         }
      }
   }

   public boolean isContradicted() {
      if (this.contradictions == null) {
         return false;
      }
      return !this.contradictions.isEmpty();
   }
   public static <T> LatestVersion<T> of(T value) {
        return new LatestVersion<>(value);
   }
   
   public static <T> LatestVersion<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
   }

   public static<T> LatestVersion<T> empty() {
        @SuppressWarnings("unchecked")
        LatestVersion<T> t = (LatestVersion<T>) EMPTY;
        return t;
   }
}

