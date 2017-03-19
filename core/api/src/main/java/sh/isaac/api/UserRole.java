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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;

//~--- enums ------------------------------------------------------------------

/**
 * {@link UserRole}.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
public enum UserRole {
   // Do not rearrange. Add new values to end.

   /** The automated. */
   /*
    * AUTOMATED is used to capture that the system automated the workflow
    * advancement rather than a specific user
    */
   AUTOMATED(UserRoleConstants.AUTOMATED),
   
   /** The super user. */
   SUPER_USER(UserRoleConstants.SUPER_USER),
   
   /** The administrator. */
   ADMINISTRATOR(UserRoleConstants.ADMINISTRATOR),
   
   /** The read only. */
   READ_ONLY(UserRoleConstants.READ_ONLY),
   
   /** The editor. */
   EDITOR(UserRoleConstants.EDITOR),
   
   /** The reviewer. */
   REVIEWER(UserRoleConstants.REVIEWER),
   
   /** The approver. */
   APPROVER(UserRoleConstants.APPROVER),
   
   /** The manager. */
   MANAGER(UserRoleConstants.MANAGER);

   /** The text. */
   private final String text;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new user role.
    *
    * @param text the text
    */
   private UserRole(String text) {
      this.text = text;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Safe value of.
    *
    * @param ord the ord
    * @return the optional
    */
   public static Optional<UserRole> safeValueOf(int ord) {
      for (final UserRole role: UserRole.values()) {
         if (role.ordinal() == ord) {
            return Optional.of(role);
         }
      }

      return Optional.empty();
   }

   /**
    * Safe value of.
    *
    * @param str the str
    * @return the optional
    */
   public static Optional<UserRole> safeValueOf(String str) {
      for (final UserRole role: UserRole.values()) {
         if (role.getText()
                 .equalsIgnoreCase(str)) {
            return Optional.of(role);
         }
      }

      return Optional.empty();
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
public String toString() {
      return this.text;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the text.
    *
    * @return the text
    */
   public String getText() {
      return this.text;
   }
}

