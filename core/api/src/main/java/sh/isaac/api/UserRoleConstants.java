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

/**
 * {@link UserRoleConstants}.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
public class UserRoleConstants {
   /** The Constant AUTOMATED. */
   public final static String AUTOMATED = "automated";

   /** The Constant SUPER_USER. */
   public final static String SUPER_USER = "super_user";

   /** The Constant ADMINISTRATOR. */
   public final static String ADMINISTRATOR = "administrator";

   /** The Constant READ_ONLY. */
   public final static String READ_ONLY = "read_only";

   /** The Constant EDITOR. */
   public final static String EDITOR = "editor";

   /** The Constant REVIEWER. */
   public final static String REVIEWER = "reviewer";

   /** The Constant APPROVER. */
   public final static String APPROVER = "approver";

   /** The Constant MANAGER. */
   public final static String MANAGER = "manager";

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new user role constants.
    */
   private UserRoleConstants() {}
}

