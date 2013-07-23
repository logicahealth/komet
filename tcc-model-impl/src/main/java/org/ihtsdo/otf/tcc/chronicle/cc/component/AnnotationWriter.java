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



package org.ihtsdo.otf.tcc.chronicle.cc.component;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMemberFactory;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;

/**
 *
 * @author kec
 */
public class AnnotationWriter {
   public static AtomicInteger encountered = new AtomicInteger();
   public static AtomicInteger written     = new AtomicInteger();

   //~--- fields --------------------------------------------------------------

   RefexMemberFactory factory = new RefexMemberFactory();

   //~--- constructors --------------------------------------------------------

   public AnnotationWriter() {}

   //~--- methods -------------------------------------------------------------

   @SuppressWarnings("unchecked")
   public ConcurrentSkipListSet<RefexMember<?, ?>> entryToObject(TupleInput input, int enclosingConceptNid) {
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
               refsetMember = factory.create(nid, typeNid, enclosingConceptNid, input);

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
               refsetMember.merge(factory.create(nid, typeNid, enclosingConceptNid, input),
                                  new HashSet<ConceptChronicleBI>());
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

   public void objectToEntry(Collection<RefexMember<?, ?>> list, TupleOutput output,
                             int maxReadOnlyStatusAtPositionId) {
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
}
