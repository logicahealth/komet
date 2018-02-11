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
package sh.isaac.solor.rf2.direct;

import java.io.File;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author kec
 */
public class ImportSpecification implements Comparable<ImportSpecification>{
   final File zipFile;
   final ImportStreamType streamType;
   final ZipEntry zipEntry;

   public ImportSpecification(File zipFile, ImportStreamType streamType, ZipEntry zipEntry) {
      this.zipFile = zipFile;
      this.streamType = streamType;
      this.zipEntry = zipEntry;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 37 * hash + Objects.hashCode(this.streamType);
      hash = 37 * hash + Objects.hashCode(this.zipEntry);
      return hash;
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
      final ImportSpecification other = (ImportSpecification) obj;
      if (!Objects.equals(this.zipFile, other.zipFile)) {
         return false;
      }
      if (this.streamType != other.streamType) {
         return false;
      }
      if (!Objects.equals(this.zipEntry, other.zipEntry)) {
         return false;
      }
      return true;
   }

   @Override
   public int compareTo(ImportSpecification o) {
      if (this.streamType != o.streamType) {
         return this.streamType.compareTo(o.streamType);
      }
      return this.zipEntry.toString().compareTo(o.zipEntry.toString());
   }

   @Override
   public String toString() {
      return "ImportSpecification{" + zipFile.getName() + ", " + streamType + ", " + zipEntry + '}';
   }
   
}
