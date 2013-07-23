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



package org.ihtsdo.otf.tcc.datastore;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.datastore.temp.AceLog;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMemberFactory;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;

/**
 *
 * @author maestro
 */
public class AnnotationAdder implements ProcessUnfetchedConceptDataBI {

   /** Field description */
   NativeIdSetBI conceptNids = new ConcurrentBitSet();

   /** Field description */
   ConcurrentHashMap<Integer, ConcurrentSkipListSet<TtkRefexAbstractMemberChronicle<?>>> membersForConcept =
      new ConcurrentHashMap<>();

   /**
    * Constructs ...
    *
    *
    * @param members
    */
   AnnotationAdder(List<TtkRefexAbstractMemberChronicle<?>> members) {
      TkRmComparator comparator = new TkRmComparator();
      int            errors     = 0;
      Set<UUID>      errorSet   = new TreeSet<>();

      for (TtkRefexAbstractMemberChronicle<?> member : members) {
         UUID componentUuid = member.getComponentUuid();
         int  nid           = Bdb.uuidToNid(componentUuid);
         int  cNid          = Bdb.getConceptNid(nid);

         if (cNid + Integer.MIN_VALUE >= 0) {
            conceptNids.setMember(cNid);

            ConcurrentSkipListSet<TtkRefexAbstractMemberChronicle<?>> set = new ConcurrentSkipListSet<>(comparator);

            membersForConcept.putIfAbsent(cNid, set);
            membersForConcept.get(cNid).add(member);
         } else {
            errors++;
            errorSet.add(componentUuid);

            int nid2  = Bdb.uuidToNid(member.getComponentUuid());
            int cNid2 = Bdb.getConceptNid(nid);

            AceLog.getAppLog().warning("No concept for: " + member);
         }
      }

      if (errors > 0) {
         AceLog.getAppLog().warning(errors + " processing errors.\n\nError set: " + errorSet.size() + "\n"
                                    + errorSet);
      }
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public boolean allowCancel() {
      return false;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public boolean continueWork() {
      return true;
   }

   /**
    * Method description
    *
    *
    * @param cNid
    * @param fcfc
    *
    * @throws Exception
    */
   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
      if (conceptNids.isMember(cNid)) {
         ConceptChronicle                                         c   = (ConceptChronicle) fcfc.fetch();
         ConcurrentSkipListSet<TtkRefexAbstractMemberChronicle<?>> set = membersForConcept.get(cNid);

         for (TtkRefexAbstractMemberChronicle<?> member : set) {
            ComponentChronicleBI<?> component = c.getComponent(Bdb.uuidToNid(member.getComponentUuid()));

            if (component != null) {
               component.addAnnotation(RefexMemberFactory.create(member, cNid));
            } else {
               AceLog.getAppLog().warning("Cannot import annotation. Component is null for: " + member);
            }
         }

         membersForConcept.remove(cNid);
         BdbCommitManager.addUncommittedNoChecks(c);
      }
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public NativeIdSetBI getNidSet() throws IOException {
      return conceptNids;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public String getTitle() {
      return "Adding annotations";
   }

   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/25
    * @author         Enter your name here...    
    */
   static class TkRmComparator implements Comparator<TtkRefexAbstractMemberChronicle<?>> {

      /**
       * Method description
       *
       *
       * @param t
       * @param t1
       *
       * @return
       */
      @Override
      public int compare(TtkRefexAbstractMemberChronicle<?> t, TtkRefexAbstractMemberChronicle<?> t1) {
         return t.primordialUuid.compareTo(t1.primordialUuid);
      }
   }
}
