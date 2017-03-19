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

import java.util.HashSet;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.LookupService;
import sh.isaac.api.UserRole;
import sh.isaac.provider.workflow.user.SimpleUserRoleService;

//~--- classes ----------------------------------------------------------------

/**
 * {@link RoleConfigurator}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RoleConfigurator {
   /** The Constant firstUserId (for Unit Testing) */
   private static final UUID firstUserId = UUID.randomUUID();

   /** The Constant secondUserId (for Unit Testing) */
   private static final UUID secondUserId = UUID.randomUUID();

   //~--- methods -------------------------------------------------------------

   public static void configureForTest() {
      final SimpleUserRoleService rolesService = LookupService.get()
                                                        .getService(SimpleUserRoleService.class);

      rolesService.addRole(UserRole.EDITOR);
      rolesService.addRole(UserRole.REVIEWER);
      rolesService.addRole(UserRole.APPROVER);
      rolesService.addRole(UserRole.AUTOMATED);

      // Setup User Role Maps
      HashSet<UserRole> roles = new HashSet<>();

      roles.add(UserRole.EDITOR);
      roles.add(UserRole.APPROVER);
      rolesService.addUser(firstUserId, roles);
      roles = new HashSet<>();
      roles.add(UserRole.REVIEWER);
      rolesService.addUser(secondUserId, roles);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Returns the first test user (for Unit Testing)
    *
    * @return the first test user
    */
   public static UUID getFirstTestUser() {
      return firstUserId;
   }

   /**
    * Returns the first test user seq (Value doesn't matter as long as consistent)
    *
    * @return the first test user seq
    */
   public static int getFirstTestUserSeq() {
      return 1;
   }

   /**
    * Returns the second test user (for Unit Testing)
    *
    * @return the second test user
    */
   public static UUID getSecondTestUser() {
      return secondUserId;
   }

   /**
    * Returns the second test user seq (Value doesn't matter as long as consistent)
    *
    * @return the second test user seq
    */
   public static int getSecondTestUserSeq() {
      return 2;
   }
}

