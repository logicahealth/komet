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

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import sh.isaac.api.chronicle.LatestVersion;

/**
 *
 * @author kec
 * @param <V> the type of version
 */
public interface VersionStream<V> extends Stream<LatestVersion<V>> {

   default LatestVersion<V> findAnyVersion() {
      Optional<LatestVersion<V>> optionalLatest = findAny();
      if (optionalLatest.isPresent()) {
         return optionalLatest.get();
      }
      return new LatestVersion<>();
   }

   default LatestVersion<V> findFirstVersion() {
      Optional<LatestVersion<V>> optionalLatest = findFirst();
      if (optionalLatest.isPresent()) {
         return optionalLatest.get();
      }
      return new LatestVersion<>();
   }

   default LatestVersion<V> reduceVersion(BinaryOperator<LatestVersion<V>> accumulator) {
      Optional<LatestVersion<V>> optionalLatest = reduce(accumulator);
      if (optionalLatest.isPresent()) {
         return optionalLatest.get();
      }
      return new LatestVersion<>();
   }

   default VersionStream<V> filterVersion(Predicate<? super LatestVersion<V>> predicate) {
      return new VersionStreamWrapper<>(filter(predicate));
   }   
}
