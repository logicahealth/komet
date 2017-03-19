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



package sh.isaac.provider.workflow.user;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.hk2.api.Rank;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.UserRole;
import sh.isaac.api.UserRoleService;

//~--- classes ----------------------------------------------------------------

/**
 * A simple implementation of a role service that can be manually configured, for test (or any other) purpose.
 * It has no storage - must be manually configured with non-interface methods
 *
 * {@link SimpleUserRoleService}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Service(name = "simpleUserRoleService")
@Rank(value = -50)
@Singleton
public class SimpleUserRoleService
         implements UserRoleService {
   /** The user role map  (for Unit Testing). */
   private final Map<UUID, Set<UserRole>> userRoleMap = new HashMap<>();

   /** The definition roles. */
   private final Set<UserRole> definitionRoles = new HashSet<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Defines the user roles for the Mock case.
    */
   private SimpleUserRoleService() {
      // For HK2 to construct
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the role.
    *
    * @param roleName the role name
    */
   public void addRole(UserRole roleName) {
      this.definitionRoles.add(roleName);
   }

   /**
    * Adds the user.
    *
    * @param user the user
    * @param roles the roles
    */
   public void addUser(UUID user, Set<UserRole> roles) {
      this.userRoleMap.put(user, roles);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the all user roles.
    *
    * @return the all user roles
    * @see sh.isaac.api.UserRoleService#getAllUserRoles()
    */
   @Override
   public Set<UserRole> getAllUserRoles() {
      return this.definitionRoles;
   }

   /**
    * Gets the user roles.
    *
    * @param userId the user id
    * @return the user roles
    * @see sh.isaac.api.UserRoleService#getUserRoles(java.util.UUID)
    */
   @Override
   public Set<UserRole> getUserRoles(UUID userId) {
      return this.userRoleMap.get(userId);
   }
}

