/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.model.cc.component;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberFactory;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMemberFactory;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicRevision;


/**
 *
 * @author kec
 */
public class AnnotationWriter {
   public static AtomicInteger encountered = new AtomicInteger();
   public static AtomicInteger written     = new AtomicInteger();

   //~--- constructors --------------------------------------------------------

   public AnnotationWriter() {}

   //~--- methods -------------------------------------------------------------

   @SuppressWarnings("unchecked")
   public ConcurrentSkipListSet<RefexMember<?, ?>> entryToObject(DataInputStream input, int enclosingConceptNid) throws IOException {
      int listSize = input.readShort();

      if (listSize == 0) {
         return null;
      }

      ConcurrentSkipListSet<RefexMember<?, ?>> newRefsetMemberList =
         new ConcurrentSkipListSet<>(new Comparator<RefexChronicleBI<?>>() {
         @Override
         public int compare(RefexChronicleBI<?> t, RefexChronicleBI<?> t1) {
            return t.getNid() - t1.getNid();
         }
      });

      for (int index = 0; index < listSize; index++) {
         int typeNid = input.readInt();

         input.mark(8);

         int nid = input.readInt();

         input.reset();

         RefexMember<?, ?> refsetMember = (RefexMember<?, ?>) ConceptChronicle.componentsCRHM.get(nid);

         if (refsetMember == null) {
            try {
               refsetMember = RefexMemberFactory.create(nid, typeNid, enclosingConceptNid, input);

               if (refsetMember.getTime() != Long.MIN_VALUE) {
                  RefexMember<?, ?> oldMember = (RefexMember<?,
                                                   ?>) ConceptChronicle.componentsCRHM.putIfAbsent(nid, refsetMember);

                  if (oldMember != null) {
                     refsetMember = oldMember;
                  }
               }
            } catch (IOException ex) {
               throw new RuntimeException(ex);
            }
         } else {
            try {
               refsetMember.merge(RefexMemberFactory.create(nid, typeNid, enclosingConceptNid, input));
            } catch (IOException ex) {
               throw new RuntimeException(ex);
            }
         }

         if (refsetMember.getTime() != Long.MIN_VALUE) {
            newRefsetMemberList.add(refsetMember);
         }
      }

      return newRefsetMemberList;
   }
   
   public ConcurrentSkipListSet<RefexDynamicMember> entryDynamicToObject(DataInputStream input, int enclosingConceptNid) throws IOException {
      int listSize = input.readShort();

      if (listSize == 0) {
         return null;
      }

      ConcurrentSkipListSet<RefexDynamicMember> newRefsetMemberList =
         new ConcurrentSkipListSet<>(new Comparator<RefexDynamicChronicleBI<?>>() {
         @Override
         public int compare(RefexDynamicChronicleBI<?> t, RefexDynamicChronicleBI<?> t1) {
            return t.getNid() - t1.getNid();
         }
      });

      for (int index = 0; index < listSize; index++) {
         input.mark(8);

         int nid = input.readInt();

         input.reset();

         RefexDynamicMember refsetMember = (RefexDynamicMember) ConceptChronicle.componentsCRHM.get(nid);

         if (refsetMember == null) {
            try {
               refsetMember = RefexDynamicMemberFactory.create(nid, enclosingConceptNid, input);

               if (refsetMember.getTime() != Long.MIN_VALUE) {
                  RefexDynamicMember oldMember = (RefexDynamicMember) ConceptChronicle.componentsCRHM.putIfAbsent(nid, refsetMember);

                  if (oldMember != null) {
                     refsetMember = oldMember;
                  }
               }
            } catch (IOException ex) {
               throw new RuntimeException(ex);
            }
         } else {
            try {
               refsetMember.merge(RefexDynamicMemberFactory.create(nid, enclosingConceptNid, input));
            } catch (IOException ex) {
               throw new RuntimeException(ex);
            }
         }

         if (refsetMember.getTime() != Long.MIN_VALUE) {
            newRefsetMemberList.add(refsetMember);
         }
      }

      return newRefsetMemberList;
   }


   public void objectToEntry(Collection<RefexMember<?, ?>> list, DataOutput output,
                             int maxReadOnlyStatusAtPositionId) throws IOException {
      if (list == null) {
         output.writeShort(0);    // List size

         return;
      }

      List<RefexMember<?, ?>> refsetMembersToWrite = new ArrayList<>(list.size());

      for (RefexChronicleBI<?> refsetChronicle : list) {
         RefexMember<?, ?> refsetMember = (RefexMember<?, ?>) refsetChronicle;

         encountered.incrementAndGet();
         assert refsetMember.getStamp() != Integer.MAX_VALUE;

         if ((refsetMember.primordialStamp > maxReadOnlyStatusAtPositionId)
                 && (refsetMember.getTime() != Long.MIN_VALUE)) {
            refsetMembersToWrite.add(refsetMember);
         } else {
            if (refsetMember.revisions != null) {
               for (RefexRevision<?, ?> r : refsetMember.revisions) {
                  if ((r.getStamp() > maxReadOnlyStatusAtPositionId) && (r.getTime() != Long.MIN_VALUE)) {
                     refsetMembersToWrite.add(refsetMember);

                     break;
                  }
               }
            }
         }
      }

      output.writeShort(refsetMembersToWrite.size());    // List size

      for (RefexMember<?, ?> refsetMember : refsetMembersToWrite) {
         written.incrementAndGet();
         output.writeInt(refsetMember.getTypeNid());
         refsetMember.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
      }
   }
   
   public void objectDynamicToEntry(Collection<RefexDynamicMember> list, DataOutput output, int maxReadOnlyStatusAtPositionId) throws IOException {
      if (list == null) {
         output.writeShort(0);    // List size

         return;
      }

      List<RefexDynamicMember> refsetMembersToWrite = new ArrayList<>(list.size());

      for (RefexDynamicChronicleBI<?> refsetChronicle : list) {
         RefexDynamicMember refsetMember = (RefexDynamicMember) refsetChronicle;

         encountered.incrementAndGet();
         assert refsetMember.getStamp() != Integer.MAX_VALUE;

         if ((refsetMember.primordialStamp > maxReadOnlyStatusAtPositionId)
                 && (refsetMember.getTime() != Long.MIN_VALUE)) {
            refsetMembersToWrite.add(refsetMember);
         } else {
            if (refsetMember.revisions != null) {
               for (RefexDynamicRevision r : refsetMember.revisions) {
                  if ((r.getStamp() > maxReadOnlyStatusAtPositionId) && (r.getTime() != Long.MIN_VALUE)) {
                     refsetMembersToWrite.add(refsetMember);

                     break;
                  }
               }
            }
         }
      }

      output.writeShort(refsetMembersToWrite.size());    // List size

      for (RefexDynamicMember refsetMember : refsetMembersToWrite) {
         written.incrementAndGet();
         refsetMember.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
      }
   }
}
