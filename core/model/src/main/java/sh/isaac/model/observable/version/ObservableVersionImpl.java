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



package sh.isaac.model.observable.version;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import sh.isaac.api.State;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.model.ObjectVersionImpl;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.CommitAwareLongProperty;
import sh.isaac.model.observable.CommitAwareObjectProperty;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <OV>
 * @param <V>
 */
public class ObservableVersionImpl<OV extends ObservableVersionImpl<OV, V>, V extends ObjectVersionImpl<?, ?>>
         implements ObservableVersion, CommittableComponent {
   ObjectProperty<State>              stateProperty;
   LongProperty                       timeProperty;
   IntegerProperty                    authorSequenceProperty;
   IntegerProperty                    moduleSequenceProperty;
   IntegerProperty                    pathSequenceProperty;
   ObjectProperty<CommitStates>       commitStateProperty;
   ObjectBinding<CommitStates>        commitStateBinding;
   IntegerProperty                    stampSequenceProperty;
   protected V                        stampedVersion;
   protected ObservableChronology<OV> chronology;

   //~--- constructors --------------------------------------------------------

   public ObservableVersionImpl(V stampedVersion, ObservableChronology<OV> chronology) {
      this.stampedVersion = stampedVersion;
      this.chronology     = chronology;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public final IntegerProperty authorSequenceProperty() {
      if (authorSequenceProperty == null) {
         authorSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.AUTHOR_SEQUENCE_FOR_VERSION.toExternalString(),
               getAuthorSequence());
      }

      return authorSequenceProperty;
   }

   @Override
   public final ObjectProperty<CommitStates> commitStateProperty() {
      if (commitStateProperty == null) {
         commitStateBinding = new ObjectBinding<CommitStates>() {
            @Override
            protected CommitStates computeValue() {
               if (timeProperty.get() == Long.MAX_VALUE) {
                  return CommitStates.UNCOMMITTED;
               }

               return CommitStates.COMMITTED;
            }
         };
         commitStateProperty = new SimpleObjectProperty(this,
               ObservableFields.COMMITTED_STATE_FOR_VERSION.toExternalString(),
               commitStateBinding.get());
         commitStateProperty.bind(commitStateBinding);
      }

      return commitStateProperty;
   }

   @Override
   public final IntegerProperty moduleSequenceProperty() {
      if (moduleSequenceProperty == null) {
         moduleSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.MODULE_SEQUENCE_FOR_VERSION.toExternalString(),
               getModuleSequence());
      }

      return moduleSequenceProperty;
   }

   @Override
   public final IntegerProperty pathSequenceProperty() {
      if (pathSequenceProperty == null) {
         pathSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.PATH_SEQUENCE_FOR_VERSION.toExternalString(),
               getPathSequence());
      }

      return pathSequenceProperty;
   }

   @Override
   public final IntegerProperty stampSequenceProperty() {
      if (stampSequenceProperty == null) {
         stampSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.STAMP_SEQUENCE_FOR_VERSION.toExternalString(),
               getStampSequence());
      }

      return stampSequenceProperty;
   }

   @Override
   public final ObjectProperty<State> stateProperty() {
      if (stateProperty == null) {
         stateProperty = new CommitAwareObjectProperty<>(this,
               ObservableFields.STATUS_FOR_VERSION.toExternalString(),
               getState());
      }

      return stateProperty;
   }

   @Override
   public final LongProperty timeProperty() {
      if (timeProperty == null) {
         timeProperty = new CommitAwareLongProperty(this,
               ObservableFields.TIME_FOR_VERSION.toExternalString(),
               getTime());
      }

      return timeProperty;
   }

   @Override
   public String toUserString() {
      return toString();
   }

   public void updateVersion(V stampedVersion) {
      this.stampedVersion = stampedVersion;

      if (stampSequenceProperty != null) {
         stampSequenceProperty.set(stampedVersion.getStampSequence());
      }

      if (commitStateBinding != null) {
         commitStateBinding.invalidate();
      }

      if (stateProperty != null) {
         stateProperty.set(stampedVersion.getState());
      }

      if (authorSequenceProperty != null) {
         authorSequenceProperty.set(stampedVersion.getAuthorSequence());
      }

      if (moduleSequenceProperty != null) {
         moduleSequenceProperty.set(stampedVersion.getModuleSequence());
      }

      if (pathSequenceProperty != null) {
         pathSequenceProperty.set(stampedVersion.getPathSequence());
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public final int getAuthorSequence() {
      if (authorSequenceProperty != null) {
         return authorSequenceProperty.get();
      }

      return stampedVersion.getAuthorSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAuthorSequence(int authorSequence) {
      if (authorSequenceProperty != null) {
         authorSequenceProperty.set(authorSequence);
      } else {
         stampedVersion.setAuthorSequence(authorSequence);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ObservableChronology<OV> getChronology() {
      return chronology;
   }

   @Override
   public final CommitStates getCommitState() {
      if (commitStateProperty != null) {
         return commitStateProperty.get();
      }

      if (getTime() == Long.MAX_VALUE) {
         return CommitStates.UNCOMMITTED;
      }

      return CommitStates.COMMITTED;
   }

   @Override
   public final int getModuleSequence() {
      if (moduleSequenceProperty != null) {
         return moduleSequenceProperty.get();
      }

      return stampedVersion.getModuleSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setModuleSequence(int moduleSequence) {
      if (moduleSequenceProperty != null) {
         moduleSequenceProperty.set(moduleSequence);
      } else {
         stampedVersion.setModuleSequence(moduleSequence);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid() {
      return stampedVersion.getNid();
   }

   @Override
   public final int getPathSequence() {
      if (pathSequenceProperty != null) {
         return pathSequenceProperty.get();
      }

      return stampedVersion.getPathSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setPathSequence(int pathSequence) {
      if (pathSequenceProperty != null) {
         pathSequenceProperty.set(pathSequence);
      } else {
         stampedVersion.setPathSequence(pathSequence);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public UUID getPrimordialUuid() {
      return stampedVersion.getPrimordialUuid();
   }

   @Override
   public final int getStampSequence() {
      if (stampSequenceProperty != null) {
         return stampSequenceProperty.get();
      }

      return stampedVersion.getStampSequence();
   }

   @Override
   public final State getState() {
      if (stateProperty != null) {
         return stateProperty.get();
      }

      return stampedVersion.getState();
   }

   @Override
   public final long getTime() {
      if (timeProperty != null) {
         return timeProperty.get();
      }

      return stampedVersion.getTime();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setTime(long time) {
      if (timeProperty != null) {
         timeProperty.set(time);
      } else {
         stampedVersion.setTime(time);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public boolean isUncommitted() {
      return getCommitState() == CommitStates.UNCOMMITTED;
   }

   @Override
   public List<UUID> getUuidList() {
      return stampedVersion.getUuidList();
   }

   public short getVersionSequence() {
      return stampedVersion.getVersionSequence();
   }
}

