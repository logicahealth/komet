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
package sh.isaac.model;

/**
 *
 * @author kec
 */
public class VersionWithScoreWrapper implements Comparable<VersionWithScoreWrapper> {
   final VersionImpl version;

   public VersionImpl getVersion() {
      return version;
   }

   public int getScore() {
      return score;
   }
   final int score; 

   public VersionWithScoreWrapper(VersionImpl version, int score) {
      this.version = version;
      this.score = score;
   }

   @Override
   public int compareTo(VersionWithScoreWrapper o) {
      return this.score - o.score;
   }

   @Override
   public int hashCode() {
      return version.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final VersionWithScoreWrapper other = (VersionWithScoreWrapper) obj;
      return this.version.equals(other.version);
   }
   
   
}
