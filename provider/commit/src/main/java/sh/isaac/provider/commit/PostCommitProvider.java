/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.provider.commit;

//~--- JDK imports ------------------------------------------------------------

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.commit.ChangeSetListener;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.PostCommitService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.identity.StampedVersion;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Nuno Marques
 */
@Service(name = "Post Commit Provider")
@RunLevel(value = 1)
public class PostCommitProvider
         implements PostCommitService, ChronologyChangeListener {
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private final UUID                                            listenerId         = UUID.randomUUID();
   ConcurrentSkipListSet<WeakReference<ChangeSetListener>> changeSetListeners = new ConcurrentSkipListSet<>();

   //~--- constructors --------------------------------------------------------

   private PostCommitProvider() {
      // for HK2
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void addChangeSetListener(ChangeSetListener changeSetListener) {
      LOG.debug("add listener");
      this.changeSetListeners.add(new ChangeSetListenerReference(changeSetListener));
   }

   @Override
   public void handleChange(ConceptChronology<? extends StampedVersion> cc) {
      // not interested
   }

   @Override
   public void handleChange(SememeChronology<? extends SememeVersion<?>> sc) {
      // not interested
   }

   @Override
   public void handleCommit(CommitRecord commitRecord) {
      LOG.debug("change set listeners size: {}", this.changeSetListeners.size());
      this.changeSetListeners.forEach((listenerReference) -> {
                                    final ChangeSetListener listener = listenerReference.get();

                                    if (listener == null) {
                                       this.changeSetListeners.remove(listenerReference);
                                    } else {
                                       listener.handlePostCommit(commitRecord);
                                    }
                                 });
   }

   @Override
   public void removeChangeSetListener(ChangeSetListener changeSetListener) {
      LOG.debug("remove listener");
      this.changeSetListeners.remove(new ChangeSetListenerReference(changeSetListener));
   }

   @PostConstruct
   private void startMe() {
      LOG.info("Starting PostCommitProvider post-construct");
      Get.commitService()
         .addChangeListener(this);
   }

   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping PostCommitProvider pre-destroy. ");
   }

   //~--- get methods ---------------------------------------------------------

   // ChronologyChangeListener interfaces
   @Override
   public UUID getListenerUuid() {
      return this.listenerId;
   }

   //~--- inner classes -------------------------------------------------------

   private static class ChangeSetListenerReference
           extends WeakReference<ChangeSetListener>
            implements Comparable<ChangeSetListenerReference> {
      UUID listenerUuid;

      //~--- constructors -----------------------------------------------------

      public ChangeSetListenerReference(ChangeSetListener referent) {
         super(referent);
         this.listenerUuid = referent.getListenerUuid();
      }

      public ChangeSetListenerReference(ChangeSetListener referent, ReferenceQueue<? super ChangeSetListener> q) {
         super(referent, q);
         this.listenerUuid = referent.getListenerUuid();
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(ChangeSetListenerReference o) {
         return this.listenerUuid.compareTo(o.listenerUuid);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }

         if (getClass() != obj.getClass()) {
            return false;
         }

         final ChangeSetListenerReference other = (ChangeSetListenerReference) obj;

         return Objects.equals(this.listenerUuid, other.listenerUuid);
      }

      @Override
      public int hashCode() {
         int hash = 3;

         hash = 67 * hash + Objects.hashCode(this.listenerUuid);
         return hash;
      }
   }
}

