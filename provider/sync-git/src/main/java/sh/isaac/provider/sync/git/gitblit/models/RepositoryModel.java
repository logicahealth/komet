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



package sh.isaac.provider.sync.git.gitblit.models;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.provider.sync.git.gitblit.utils.RpcUtils.AccessRestrictionType;
import sh.isaac.provider.sync.git.gitblit.utils.RpcUtils.AuthorizationControl;
import sh.isaac.provider.sync.git.gitblit.utils.RpcUtils.FederationStrategy;
import sh.isaac.provider.sync.git.gitblit.utils.StringUtils;

//~--- classes ----------------------------------------------------------------

/**
 * RepositoryModel is a serializable model class that represents a Gitblit
 * repository including its configuration settings and access restriction.
 *
 *
 */
public class RepositoryModel
         implements Serializable, Comparable<RepositoryModel> {
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   public String       name;
   public String       description;
   public List<String> owners;
   public Date         lastChange;
   public String       accessRestriction;
   public String       authorizationControl;
   public String       federationStrategy;
   public List<String> federationSets;
   public boolean      isBare;
   public String       projectPath;
   private String      displayName;
   public boolean      acceptNewPatchsets;
   public boolean      acceptNewTickets;

   //~--- constructors --------------------------------------------------------

   public RepositoryModel(String name, String description, String owner, Date lastchange) {
      this.name                 = name;
      this.description          = description;
      this.lastChange           = lastchange;
      this.accessRestriction    = AccessRestrictionType.VIEW.toString();
      this.authorizationControl = AuthorizationControl.NAMED.toString();
      this.federationSets       = new ArrayList<String>();
      this.federationStrategy   = FederationStrategy.FEDERATE_THIS.toString();
      this.projectPath          = StringUtils.getFirstPathElement(name);
      this.owners               = new ArrayList<String>();
      this.isBare               = true;
      this.acceptNewTickets     = true;
      this.acceptNewPatchsets   = true;
      addOwner(owner);
   }

   //~--- methods -------------------------------------------------------------

   public void addOwner(String username) {
      if (!StringUtils.isEmpty(username)) {
         final String name = username.toLowerCase();

         // a set would be more efficient, but this complicates JSON
         // deserialization so we enforce uniqueness with an arraylist
         if (!this.owners.contains(name)) {
            this.owners.add(name);
         }
      }
   }

   @Override
   public int compareTo(RepositoryModel o) {
      return StringUtils.compareRepositoryNames(this.name, o.name);
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof RepositoryModel) {
         return this.name.equals(((RepositoryModel) o).name);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   @Override
   public String toString() {
      if (this.displayName == null) {
         this.displayName = StringUtils.stripDotGit(this.name);
      }

      return this.displayName;
   }
}

