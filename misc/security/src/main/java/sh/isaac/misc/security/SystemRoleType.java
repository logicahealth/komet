/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sh.isaac.misc.security;

public enum SystemRoleType {
   /*
    * 
    * General role type.  Catch all.  Use this in most cases.
    */
   GENERAL,
   /*
    * Modeling role types are dependent on the ISAAC DB deployed.  Prisme's granting of this role occurs
    * when the user has been granted modeling authority for that particular DB build.
    */
   MODELING,
   /*
    *The requirements document specifies this as a role type too, but it currently carries the same 
    * connotation as GENERAL. 
    */
   DEPLOYMENT,
   
   /**
    * A type for roles that should never be assigned to any real user
    */
   NON_USER;
}
