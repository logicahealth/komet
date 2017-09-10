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



package sh.isaac.api.chronicle;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.State;
import sh.isaac.api.commit.CommitStates;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class CategorizedVersion
         implements Version {
   private final Version             delegate;
   private final CategorizedVersions categorizedVersions;

   //~--- constructors --------------------------------------------------------

   public CategorizedVersion(Version delegate, CategorizedVersions categorizedVersions) {
      this.delegate            = delegate;
      this.categorizedVersions = categorizedVersions;
   }

   //~--- methods -------------------------------------------------------------
   
   public <V extends Version> V unwrap() {
      return (V) delegate;
   }
   
   public VersionCategory getVersionCategory() {
      return categorizedVersions.getVersionCategory(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (this.getClass() != obj.getClass()) {
         return false;
      }
      return delegate.equals(obj);
   }

   @Override
   public int hashCode() {
      return delegate.hashCode();
   }

   @Override
   public String toString() {
      return delegate.toString();
   }

   @Override
   public String toUserString() {
      return delegate.toUserString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAuthorSequence() {
      return delegate.getAuthorSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAuthorSequence(int authorSequence) {
      delegate.setAuthorSequence(authorSequence);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Chronology getChronology() {
      return delegate.getChronology();
   }

   @Override
   public CommitStates getCommitState() {
      return delegate.getCommitState();
   }

   @Override
   public int getModuleSequence() {
      return delegate.getModuleSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setModuleSequence(int moduleSequence) {
      delegate.setModuleSequence(moduleSequence);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid() {
      return delegate.getNid();
   }

   @Override
   public int getPathSequence() {
      return delegate.getPathSequence();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setPathSequence(int pathSequence) {
      delegate.setPathSequence(pathSequence);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public UUID getPrimordialUuid() {
      return delegate.getPrimordialUuid();
   }

   @Override
   public int getStampSequence() {
      return delegate.getStampSequence();
   }

   @Override
   public State getState() {
      return delegate.getState();
   }

   @Override
   public long getTime() {
      return delegate.getTime();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setTime(long time) {
      delegate.setTime(time);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public boolean isUncommitted() {
      return delegate.isUncommitted();
   }

   @Override
   public List<UUID> getUuidList() {
      return delegate.getUuidList();
   }

   @Override
   public UUID[] getUuids() {
      return delegate.getUuids();
   }

   @Override
   public VersionType getVersionType() {
      return delegate.getVersionType();
   }
   
}

