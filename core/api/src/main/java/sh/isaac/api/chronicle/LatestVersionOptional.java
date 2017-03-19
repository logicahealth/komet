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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

//~--- classes ----------------------------------------------------------------

/**
 * TODO implement class that combines latest and optional to reduce API complexity...
 * 
 * Maybe a bad idea as collections and streams return the other Optional...
 * And if we create a new class, can't take advantage of those features,
 * and Optional is declared final, so we can't subclass.
 *
 * @author kec
 * @param <V> the value type
 */
public class LatestVersionOptional<V> {
   /**
    * Common instance for {@code empty()}.
    */
   private static final LatestVersionOptional<?> EMPTY = new LatestVersionOptional<>();

   //~--- fields --------------------------------------------------------------

   /** The value. */
   V                value;
   
   /** The contradictions. */
   Optional<Set<V>> contradictions;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new latest version optional.
    */
   public LatestVersionOptional() {
      this.contradictions = Optional.empty();
   }

   /**
    * Instantiates a new latest version optional.
    *
    * @param latest the latest
    */
   public LatestVersionOptional(V latest) {
      this.value     = Objects.requireNonNull(latest, "latest version cannot be null");
      this.contradictions = Optional.empty();
   }

   /**
    * Instantiates a new latest version optional.
    *
    * @param latest the latest
    * @param contradictions the contradictions
    */
   public LatestVersionOptional(V latest, Collection<V> contradictions) {
      this.value = latest;

      if (contradictions == null) {
         this.contradictions = Optional.empty();
      } else {
         this.contradictions = Optional.of(new HashSet<V>(contradictions));
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
            this.contradictions = Optional.of(new HashSet<V>());
         }

         this.contradictions.get()
                       .add(value);
      }
   }

   /**
    * Returns an empty {@code Optional} instance.  No value is present for this
    * Optional.
    *
    * @param <V> Type of the non-existent value
    * @return an empty {@code Optional}
    * @apiNote Though it may be tempting to do so, avoid testing if an object
    * is empty by comparing with {@code ==} against instances returned by
    * {@code Option.empty()}. There is no guarantee that it is a singleton.
    * Instead, use {@link #isPresent()}.
    */
   public static <V> LatestVersionOptional<V> empty() {
      @SuppressWarnings("unchecked")
	final
      LatestVersionOptional<V> t = (LatestVersionOptional<V>) EMPTY;

      return t;
   }

   /**
    * Indicates whether some other object is "equal to" this Optional. The
    * other object is considered equal if:
    * <ul>
    * <li>it is also an {@code Optional} and;
    * <li>both instances have no value present or;
    * <li>the present values are "equal to" each other via {@code equals()}.
    * </ul>
    *
    * @param obj an object to be tested for equality
    * @return {code true} if the other object is "equal to" this object
    * otherwise {@code false}
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof LatestVersionOptional)) {
         return false;
      }

      final LatestVersionOptional<?> other = (LatestVersionOptional<?>) obj;

      return Objects.equals(this.value, other.value);
   }

   /**
    * If a value is present, and the value matches the given predicate,
    * return an {@code Optional} describing the value, otherwise return an
    * empty {@code Optional}.
    *
    * @param predicate a predicate to apply to the value, if present
    * @return an {@code Optional} describing the value of this {@code Optional}
    * if a value is present and the value matches the given predicate,
    * otherwise an empty {@code Optional}
    * @throws NullPointerException if the predicate is null
    */
   public LatestVersionOptional<V> filter(Predicate<? super V> predicate) {
      Objects.requireNonNull(predicate);

      if (!isPresent()) {
         return this;
      } else {
         return predicate.test(this.value) ? this
                                      : empty();
      }
   }

   /**
    * If a value is present, apply the provided {@code Optional}-bearing
    * mapping function to it, return that result, otherwise return an empty
    * {@code Optional}.  This method is similar to {@link #map(Function)},
    * but the provided mapper is one whose result is already an {@code Optional},
    * and if invoked, {@code flatMap} does not wrap it with an additional
    * {@code Optional}.
    *
    * @param <U> The type parameter to the {@code Optional} returned by
    * @param mapper a mapping function to apply to the value, if present
    *           the mapping function
    * @return the result of applying an {@code Optional}-bearing mapping
    * function to the value of this {@code Optional}, if a value is present,
    * otherwise an empty {@code Optional}
    * @throws NullPointerException if the mapping function is null or returns
    * a null result
    */
   public <U> LatestVersionOptional<U> flatMap(Function<? super V, LatestVersionOptional<U>> mapper) {
      Objects.requireNonNull(mapper);

      if (!isPresent()) {
         return empty();
      } else {
         return Objects.requireNonNull(mapper.apply(this.value));
      }
   }

   /**
    * Returns the hash code value of the present value, if any, or 0 (zero) if
    * no value is present.
    *
    * @return hash code value of the present value or 0 if no value is present
    */
   @Override
   public int hashCode() {
      return Objects.hashCode(this.value);
   }

   /**
    * If a value is present, invoke the specified consumer with the value,
    * otherwise do nothing.
    *
    * @param consumer block to be executed if a value is present
    * @throws NullPointerException if value is present and {@code consumer} is
    * null
    */
   public void ifPresent(Consumer<? super V> consumer) {
      if (this.value != null) {
         consumer.accept(this.value);
      }
   }

   /**
    * If a value is present, apply the provided mapping function to it,
    * and if the result is non-null, return an {@code Optional} describing the
    * result.  Otherwise return an empty {@code Optional}.
    *
    * @param <U> The type of the result of the mapping function
    * @param mapper a mapping function to apply to the value, if present
    * @return an {@code Optional} describing the result of applying a mapping
    * function to the value of this {@code Optional}, if a value is present,
    * otherwise an empty {@code Optional}
    * @throws NullPointerException if the mapping function is null
    * @apiNote This method supports post-processing on optional values, without
    * the need to explicitly check for a return status.  For example, the
    * following code traverses a stream of file names, selects one that has
    * not yet been processed, and then opens that file, returning an
    * {@code Optional<FileInputStream>}:
    * 
    * <pre>{@code
    *     Optional<FileInputStream> fis =
    *         names.stream().filter(name -> !isProcessedYet(name))
    *                       .findFirst()
    *                       .map(name -> new FileInputStream(name));
    * }</pre>
    * 
    * Here, {@code findFirst} returns an {@code Optional<String>}, and then
    * {@code map} returns an {@code Optional<FileInputStream>} for the desired
    * file if one exists.
    */
   public <U> LatestVersionOptional<U> map(Function<? super V, ? extends U> mapper) {
      Objects.requireNonNull(mapper);

      if (!isPresent()) {
         return empty();
      } else {
         return LatestVersionOptional.ofNullable(mapper.apply(this.value));
      }
   }

   /**
    * Returns an {@code Optional} with the specified present non-null value.
    *
    * @param <V> the class of the value
    * @param value the value to be present, which must be non-null
    * @return an {@code Optional} with the value present
    * @throws NullPointerException if value is null
    */
   public static <V> LatestVersionOptional<V> of(V value) {
      return new LatestVersionOptional<>(value);
   }

   /**
    * Returns an {@code Optional} describing the specified value, if non-null,
    * otherwise returns an empty {@code Optional}.
    *
    * @param <V> the class of the value
    * @param value the possibly-null value to describe
    * @return an {@code Optional} with a present value if the specified value
    * is non-null, otherwise an empty {@code Optional}
    */
   public static <V> LatestVersionOptional<V> ofNullable(V value) {
      return (value == null) ? empty()
                             : of(value);
   }

   /**
    * Return the value if present, otherwise return {@code other}.
    *
    * @param other the value to be returned if there is no value present, may
    * be null
    * @return the value, if present, otherwise {@code other}
    */
   public V orElse(V other) {
      return (this.value != null) ? this.value
                             : other;
   }

   /**
    * Return the value if present, otherwise invoke {@code other} and return
    * the result of that invocation.
    *
    * @param other a {@code Supplier} whose result is returned if no value
    * is present
    * @return the value if present otherwise the result of {@code other.get()}
    * @throws NullPointerException if value is not present and {@code other} is
    * null
    */
   public V orElseGet(Supplier<? extends V> other) {
      return (this.value != null) ? this.value
                             : other.get();
   }

   /**
    * Return the contained value, if present, otherwise throw an exception
    * to be created by the provided supplier.
    *
    * @param <X> Type of the exception to be thrown
    * @param exceptionSupplier The supplier which will return the exception to
    * be thrown
    * @return the present value
    * @throws X if there is no value present
    * @throws NullPointerException if no value is present and
    * {@code exceptionSupplier} is null
    * @apiNote A method reference to the exception constructor with an empty
    * argument list can be used as the supplier. For example,
    * {@code IllegalStateException::new}
    */
   public <X extends Throwable> V orElseThrow(Supplier<? extends X> exceptionSupplier)
            throws X {
      if (this.value != null) {
         return this.value;
      } else {
         throw exceptionSupplier.get();
      }
   }

   /**
    * Returns a non-empty string representation of this Optional suitable for
    * debugging. The exact presentation format is unspecified and may vary
    * between implementations and versions.
    *
    * @return the string representation of this instance
    * @implSpec If a value is present the result must include its string
    * representation in the result. Empty and present LatestVersionOptionals must be
    * unambiguously differentiable.
    */
   @Override
   public String toString() {
      return (this.value != null) ? String.format("LatestVersionOptional[%s]", this.value)
                             : "LatestVersionOptional.empty";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * If a value is present in this {@code LatestVersionOptional}, returns the value,
    * otherwise throws {@code NoSuchElementException}.
    *
    * @return the non-null value held by this {@code LatestVersionOptional}
    * @throws NoSuchElementException if there is no value present
    *
    * @see LatestVersionOptional#isPresent()
    */
   public V get() {
      if (this.value == null) {
         throw new NoSuchElementException("No value present");
      }

      return this.value;
   }

   /**
    * Return {@code true} if there is a value present, otherwise {@code false}.
    *
    * @return {@code true} if there is a value present, otherwise {@code false}
    */
   public boolean isPresent() {
      return this.value != null;
   }
}

