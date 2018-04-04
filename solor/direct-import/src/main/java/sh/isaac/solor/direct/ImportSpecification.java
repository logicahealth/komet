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
package sh.isaac.solor.direct;

import java.util.ArrayList;
import java.util.Objects;
import sh.isaac.api.component.semantic.version.brittle.BrittleVersion.BrittleDataTypes;
import sh.isaac.solor.ContentProvider;

/**
 *
 * @author kec
 */
public class ImportSpecification implements Comparable<ImportSpecification>{
   final ImportStreamType streamType;
   final ContentProvider contentProvider;
   final BrittleDataTypes[] refsetBrittleTypes;
   
   public ImportSpecification(ContentProvider contentProvider, ImportStreamType streamType, String refsetFileName) {
       this.streamType = streamType;
       this.contentProvider = contentProvider;
       ArrayList<BrittleDataTypes> bdt = new ArrayList<>();
       if (streamType != ImportStreamType.DYNAMIC) {
          throw new RuntimeException("This constructor should only be used with DYNAMIC refset types");
       }
       if (refsetFileName.toLowerCase().contains("refset"))
       {
          //split things like "_iisssccrefset"
          //Though this stuff should really be read from the refset metadata, not the file name
          //as we could then capture the rest of the metadata we need about the column, like name, purpose, etc. 
          int end = refsetFileName.toLowerCase().lastIndexOf("refset");
          int start = refsetFileName.substring(0, end).lastIndexOf('_');
          String spec = refsetFileName.substring(start + 1, end).toLowerCase();
          for (char c : spec.toCharArray()) {
              switch (c) {
                  case 'i':
                      bdt.add(BrittleDataTypes.INTEGER);
                      break;
                  case 'c':
                      bdt.add(BrittleDataTypes.NID);
                      break;
                  case 's':
                      bdt.add(BrittleDataTypes.STRING);
                      break;
                  case 'b':
                      bdt.add(BrittleDataTypes.BOOLEAN);
                      break;
                  case 'f':
                      bdt.add(BrittleDataTypes.FLOAT);
                      break;
                  default:
                      throw new RuntimeException("Unhandled refset type " + c + " or maybe misparsed the spec: " + spec);
              }
          }
          refsetBrittleTypes = bdt.toArray(new BrittleDataTypes[bdt.size()]);
       }
       else {
          throw new RuntimeException("Don't call this constructor without a valid refset file name");
       }
   }
   
   public ImportSpecification(ContentProvider contentProvider, ImportStreamType streamType) {
         this.streamType = streamType;
         this.contentProvider = contentProvider;
         this.refsetBrittleTypes = null;
   }
   
   @Override
   public int hashCode() {
      int hash = 7;
      hash = 37 * hash + Objects.hashCode(this.streamType);
      hash = 37 * hash + Objects.hashCode(this.contentProvider.getStreamSourceName());
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
      if (this.streamType != other.streamType) {
          return false;
      }
      return Objects.equals(this.contentProvider.getStreamSourceName(), other.contentProvider.getStreamSourceName());
   }

   @Override
   public int compareTo(ImportSpecification o) {
      if (this.streamType != o.streamType) {
         return this.streamType.compareTo(o.streamType);
      }
      return this.contentProvider.getStreamSourceName().compareTo(o.contentProvider.getStreamSourceName());
   }

   @Override
   public String toString() {
      return "ImportSpecification{" + this.contentProvider.getStreamSourceName() + ", " + streamType + '}';
   }
}
