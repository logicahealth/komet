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
public class VersionStreamWrapper<V> implements VersionStream<V> {
   Stream<LatestVersion<V>> stream;

   public VersionStreamWrapper(Stream<LatestVersion<V>> stream) {
      this.stream = stream;
   }

   @Override
   public Stream<LatestVersion<V>> filter(Predicate<? super LatestVersion<V>> predicate) {
      return stream.filter(predicate);
   }

   @Override
   public <R> Stream<R> map(Function<? super LatestVersion<V>, ? extends R> mapper) {
      return stream.map(mapper);
   }

   @Override
   public IntStream mapToInt(ToIntFunction<? super LatestVersion<V>> mapper) {
      return stream.mapToInt(mapper);
   }

   @Override
   public LongStream mapToLong(ToLongFunction<? super LatestVersion<V>> mapper) {
      return stream.mapToLong(mapper);
   }

   @Override
   public DoubleStream mapToDouble(ToDoubleFunction<? super LatestVersion<V>> mapper) {
      return stream.mapToDouble(mapper);
   }

   @Override
   public <R> Stream<R> flatMap(Function<? super LatestVersion<V>, ? extends Stream<? extends R>> mapper) {
      return stream.flatMap(mapper);
   }

   @Override
   public IntStream flatMapToInt(Function<? super LatestVersion<V>, ? extends IntStream> mapper) {
      return stream.flatMapToInt(mapper);
   }

   @Override
   public LongStream flatMapToLong(Function<? super LatestVersion<V>, ? extends LongStream> mapper) {
      return stream.flatMapToLong(mapper);
   }

   @Override
   public DoubleStream flatMapToDouble(Function<? super LatestVersion<V>, ? extends DoubleStream> mapper) {
      return stream.flatMapToDouble(mapper);
   }

   @Override
   public Stream<LatestVersion<V>> distinct() {
      return stream.distinct();
   }

   @Override
   public Stream<LatestVersion<V>> sorted() {
      return stream.sorted();
   }

   @Override
   public Stream<LatestVersion<V>> sorted(Comparator<? super LatestVersion<V>> comparator) {
      return stream.sorted(comparator);
   }

   @Override
   public Stream<LatestVersion<V>> peek(Consumer<? super LatestVersion<V>> action) {
      return stream.peek(action);
   }

   @Override
   public Stream<LatestVersion<V>> limit(long maxSize) {
      return stream.limit(maxSize);
   }

   @Override
   public Stream<LatestVersion<V>> skip(long n) {
      return stream.skip(n);
   }

   @Override
   public void forEach(Consumer<? super LatestVersion<V>> action) {
      stream.forEach(action);
   }

   @Override
   public void forEachOrdered(Consumer<? super LatestVersion<V>> action) {
      stream.forEachOrdered(action);
   }

   @Override
   public Object[] toArray() {
      return stream.toArray();
   }

   @Override
   public <A> A[] toArray(IntFunction<A[]> generator) {
      return stream.toArray(generator);
   }

   @Override
   public LatestVersion<V> reduce(LatestVersion<V> identity, BinaryOperator<LatestVersion<V>> accumulator) {
      return stream.reduce(identity, accumulator);
   }

   @Override
   public Optional<LatestVersion<V>> reduce(BinaryOperator<LatestVersion<V>> accumulator) {
      return stream.reduce(accumulator);
   }

   @Override
   public <U> U reduce(U identity, BiFunction<U, ? super LatestVersion<V>, U> accumulator, BinaryOperator<U> combiner) {
      return stream.reduce(identity, accumulator, combiner);
   }

   @Override
   public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super LatestVersion<V>> accumulator, BiConsumer<R, R> combiner) {
      return stream.collect(supplier, accumulator, combiner);
   }

   @Override
   public <R, A> R collect(Collector<? super LatestVersion<V>, A, R> collector) {
      return stream.collect(collector);
   }

   @Override
   public Optional<LatestVersion<V>> min(Comparator<? super LatestVersion<V>> comparator) {
      return stream.min(comparator);
   }

   @Override
   public Optional<LatestVersion<V>> max(Comparator<? super LatestVersion<V>> comparator) {
      return stream.max(comparator);
   }

   @Override
   public long count() {
      return stream.count();
   }

   @Override
   public boolean anyMatch(Predicate<? super LatestVersion<V>> predicate) {
      return stream.anyMatch(predicate);
   }

   @Override
   public boolean allMatch(Predicate<? super LatestVersion<V>> predicate) {
      return stream.allMatch(predicate);
   }

   @Override
   public boolean noneMatch(Predicate<? super LatestVersion<V>> predicate) {
      return stream.noneMatch(predicate);
   }

   @Override
   public Optional<LatestVersion<V>> findFirst() {
      return stream.findFirst();
   }

   @Override
   public Optional<LatestVersion<V>> findAny() {
      return stream.findAny();
   }

   @Override
   public Iterator<LatestVersion<V>> iterator() {
      return stream.iterator();
   }

   @Override
   public Spliterator<LatestVersion<V>> spliterator() {
      return stream.spliterator();
   }

   @Override
   public boolean isParallel() {
      return stream.isParallel();
   }

   @Override
   public Stream<LatestVersion<V>> sequential() {
      return stream.sequential();
   }

   @Override
   public Stream<LatestVersion<V>> parallel() {
      return stream.parallel();
   }

   @Override
   public Stream<LatestVersion<V>> unordered() {
      return stream.unordered();
   }

   @Override
   public Stream<LatestVersion<V>> onClose(Runnable closeHandler) {
      return stream.onClose(closeHandler);
   }

   @Override
   public void close() {
      stream.close();
   }

   @Override
   public int hashCode() {
      return stream.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return stream.equals(obj);
   }

   @Override
   public String toString() {
      return stream.toString();
   }
   
   
   
}
