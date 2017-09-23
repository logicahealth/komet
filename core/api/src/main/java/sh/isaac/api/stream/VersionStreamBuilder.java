/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.api.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import sh.isaac.api.chronicle.LatestVersion;

/**
 *
 * @author kec
 */
public class VersionStreamBuilder<V> implements Stream.Builder<LatestVersion<V>> {

   Stream.Builder<LatestVersion<V>> builder = Stream.builder();
   
   @Override
   public void accept(LatestVersion<V> latestVersion) {
      builder.accept(latestVersion);
   }

   @Override
   public VersionStream<V> build() {
      return new VersionStreamImpl();
   }

   private class VersionStreamImpl implements VersionStream<V> {
      Stream<LatestVersion<V>> versionStream = builder.build();

      @Override
      public Optional<LatestVersion<V>> reduce(BinaryOperator<LatestVersion<V>> accumulator) {
         return versionStream.reduce(accumulator);
      }

      @Override
      public Optional<LatestVersion<V>> min(Comparator<? super LatestVersion<V>> comparator) {
         throw new UnsupportedOperationException();
      }

      @Override
      public Optional<LatestVersion<V>> max(Comparator<? super LatestVersion<V>> comparator) {
         throw new UnsupportedOperationException();
      }

      @Override
      public VersionStream<V> filter(Predicate<? super LatestVersion<V>> predicate) {
         return (VersionStream<V>) versionStream.filter(predicate);
      }

      @Override
      public <R> Stream<R> map(Function<? super LatestVersion<V>, ? extends R> mapper) {
         return versionStream.map(mapper);
      }

      @Override
      public IntStream mapToInt(ToIntFunction<? super LatestVersion<V>> mapper) {
         return versionStream.mapToInt(mapper);
      }

      @Override
      public LongStream mapToLong(ToLongFunction<? super LatestVersion<V>> mapper) {
         return versionStream.mapToLong(mapper);
      }

      @Override
      public DoubleStream mapToDouble(ToDoubleFunction<? super LatestVersion<V>> mapper) {
         return versionStream.mapToDouble(mapper);
      }

      @Override
      public <R> Stream<R> flatMap(Function<? super LatestVersion<V>, ? extends Stream<? extends R>> mapper) {
         return versionStream.flatMap(mapper);
      }

      @Override
      public IntStream flatMapToInt(Function<? super LatestVersion<V>, ? extends IntStream> mapper) {
         return versionStream.flatMapToInt(mapper);
      }

      @Override
      public LongStream flatMapToLong(Function<? super LatestVersion<V>, ? extends LongStream> mapper) {
         return versionStream.flatMapToLong(mapper);
      }

      @Override
      public DoubleStream flatMapToDouble(Function<? super LatestVersion<V>, ? extends DoubleStream> mapper) {
         return versionStream.flatMapToDouble(mapper);
      }

      @Override
      public VersionStream<V> distinct() {
         return (VersionStream<V>) versionStream.distinct();
      }

      @Override
      public VersionStream<V> sorted() {
         return (VersionStream<V>) versionStream.sorted();
      }

      @Override
      public VersionStream<V> sorted(Comparator<? super LatestVersion<V>> comparator) {
         return (VersionStream<V>) versionStream.sorted(comparator);
      }

      @Override
      public VersionStream<V> peek(Consumer<? super LatestVersion<V>> action) {
         return (VersionStream<V>) versionStream.peek(action);
      }

      @Override
      public VersionStream<V> limit(long maxSize) {
         return (VersionStream<V>) versionStream.limit(maxSize);
      }

      @Override
      public VersionStream<V> skip(long n) {
         return (VersionStream<V>) versionStream.skip(n);
      }

      @Override
      public void forEach(Consumer<? super LatestVersion<V>> action) {
         versionStream.forEach(action);
      }

      @Override
      public void forEachOrdered(Consumer<? super LatestVersion<V>> action) {
         versionStream.forEachOrdered(action);
      }

      @Override
      public Object[] toArray() {
         return versionStream.toArray();
      }

      @Override
      public <A> A[] toArray(IntFunction<A[]> generator) {
         return versionStream.toArray(generator);
      }

      @Override
      public LatestVersion<V> reduce(LatestVersion<V> identity, BinaryOperator<LatestVersion<V>> accumulator) {
         return versionStream.reduce(identity, accumulator);
      }

      @Override
      public <U> U reduce(U identity, BiFunction<U, ? super LatestVersion<V>, U> accumulator, BinaryOperator<U> combiner) {
         return versionStream.reduce(identity, accumulator, combiner);
      }

      @Override
      public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super LatestVersion<V>> accumulator, BiConsumer<R, R> combiner) {
         return versionStream.collect(supplier, accumulator, combiner);
      }

      @Override
      public <R, A> R collect(Collector<? super LatestVersion<V>, A, R> collector) {
         return versionStream.collect(collector);
      }
      @Override
      public long count() {
         return versionStream.count();
      }

      @Override
      public boolean anyMatch(Predicate<? super LatestVersion<V>> predicate) {
         return versionStream.anyMatch(predicate);
      }

      @Override
      public boolean allMatch(Predicate<? super LatestVersion<V>> predicate) {
         return versionStream.allMatch(predicate);
      }

      @Override
      public boolean noneMatch(Predicate<? super LatestVersion<V>> predicate) {
         return versionStream.noneMatch(predicate);
      }

      @Override
      public Optional<LatestVersion<V>> findFirst() {
         return versionStream.findFirst();
      }

      @Override
      public Optional<LatestVersion<V>> findAny() {
         return versionStream.findAny();
      }

      @Override
      public Iterator<LatestVersion<V>> iterator() {
         return versionStream.iterator();
      }

      @Override
      public Spliterator<LatestVersion<V>> spliterator() {
         return versionStream.spliterator();
      }

      @Override
      public boolean isParallel() {
         return versionStream.isParallel();
      }

      @Override
      public VersionStream<V> sequential() {
         return (VersionStream<V>) versionStream.sequential();
      }

      @Override
      public VersionStream<V> parallel() {
         return (VersionStream<V>) versionStream.parallel();
      }

      @Override
      public VersionStream<V> unordered() {
         return (VersionStream<V>) versionStream.unordered();
      }

      @Override
      public VersionStream<V> onClose(Runnable closeHandler) {
         return (VersionStream<V>) versionStream.onClose(closeHandler);
      }

      @Override
      public void close() {
         versionStream.close();
      }

      @Override
      public int hashCode() {
         return versionStream.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         return versionStream.equals(obj);
      }

      @Override
      public String toString() {
         return versionStream.toString();
      }
      
   }
}
