/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.datastore.id;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 *
 * @author kec
 */
public class RelationshipIndexRecordFactory {
   public static int[] make(ConceptChronicleBI concept) throws IOException {
      Map<RecordKey, IntArrayList> records = new TreeMap<>();

      for (RelationshipChronicleBI relationship : concept.getRelationshipsOutgoing()) {
         for (RelationshipVersionBI rv : relationship.getVersions()) {
            if (rv.isInferred() || rv.isStated()) {
               RecordKey    key  = new RecordKey(rv.getTypeNid(), rv.getDestinationNid());
               IntArrayList data = records.get(key);

               if (data == null) {
                  data = new IntArrayList(16);
                  records.put(key, data);
                  data.add(Integer.MIN_VALUE);                          // Length byte
                  data.add(rv.getTypeNid());
                  data.add(rv.getDestinationNid());
               }

               int stamp = rv.getStamp();

               if (rv.isInferred()) {
                  stamp |= RelationshipIndexRecord.INFERRED_BITMASK;    // Sets the flag using bitwise OR
               }

               if (rv.getGroup() > 0) {
                  stamp |= RelationshipIndexRecord.GROUP_BITMASK;       // Sets the flag using bitwise OR
                  data.add(stamp);
                  data.add(rv.getGroup());
               } else {
                  data.add(stamp);
               }

               data.set(0, data.size());
            }
         }
      }

      IntArrayList returnValue = new IntArrayList();

      for (IntArrayList record : records.values()) {
          returnValue.addAllOf(record);
      }
      returnValue.trimToSize();
      return returnValue.elements();
   }

   //~--- inner classes -------------------------------------------------------

   private static class RecordKey implements Comparable<RecordKey> {
      int destinationNid;
      int typeNid;

      //~--- constructors -----------------------------------------------------

      public RecordKey(int typeNid, int destinationNid) {
         this.typeNid        = typeNid;
         this.destinationNid = destinationNid;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RecordKey o) {
         if (destinationNid != o.destinationNid) {
            return destinationNid - o.destinationNid;
         }

         return typeNid - o.typeNid;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }

         if (getClass() != obj.getClass()) {
            return false;
         }

         final RecordKey other = (RecordKey) obj;

         if (this.destinationNid != other.destinationNid) {
            return false;
         }

         if (this.typeNid != other.typeNid) {
            return false;
         }

         return true;
      }

      @Override
      public int hashCode() {
         int hash = 3;

         hash = 23 * hash + this.destinationNid;
         hash = 23 * hash + this.typeNid;

         return hash;
      }
   }
}
