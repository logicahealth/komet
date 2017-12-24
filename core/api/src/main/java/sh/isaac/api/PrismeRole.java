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

package sh.isaac.api;

import java.util.Optional;

/**
 * This is the master role file.  Both Prisme and IsaacRest should refer to this file.
 * Note:  The string for myName should be unique relative to all Roles.
 * Note:  There are Prisme unit tests that will break the build if new roles are added here and
 * not properly referenced in Prisme, so let a prisme dev know if you need to add in a  role.
 * @author cshupp
 * 
 */
public enum PrismeRole {
   SUPER_USER(PrismeRoleType.GENERAL, PrismeRoleConstants.SUPER_USER),
   ADMINISTRATOR(PrismeRoleType.GENERAL, PrismeRoleConstants.ADMINISTRATOR),
   READ_ONLY(PrismeRoleType.GENERAL, PrismeRoleConstants.READ_ONLY),
   EDITOR(PrismeRoleType.MODELING, PrismeRoleConstants.EDITOR),
   REVIEWER(PrismeRoleType.MODELING, PrismeRoleConstants.REVIEWER),
   APPROVER(PrismeRoleType.MODELING, PrismeRoleConstants.APPROVER),
   VUID_REQUESTOR(PrismeRoleType.GENERAL, PrismeRoleConstants.VUID_REQUESTOR),
   NTRT_USER(PrismeRoleType.GENERAL, PrismeRoleConstants.NTRT_USER),
   NTRT_STAFF(PrismeRoleType.GENERAL, PrismeRoleConstants.NTRT_STAFF),
   NTRT_ADMIN(PrismeRoleType.GENERAL, PrismeRoleConstants.NTRT_ADMIN),
   DEPLOYMENT_MANAGER(PrismeRoleType.DEPLOYMENT, PrismeRoleConstants.DEPLOYMENT_MANAGER),
   AUTOMATED(PrismeRoleType.NON_USER, PrismeRoleConstants.AUTOMATED);

   private String myName;
   private PrismeRoleType myType;

   private PrismeRole(PrismeRoleType type, String name) {
      myType = type;
      myName = name;
   }

   public String toString() {
      return myName;
   }
   
   public PrismeRoleType getType() {
      return myType;
   }
   
    public static PrismeRole getEnum(String value) {
        for(PrismeRole v : values())
            if(v.toString().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException("No enum existst for " + value);
    }

   public static Optional<PrismeRole> safeValueOf(String str) {
      for (PrismeRole role : PrismeRole.values()) {
         if (role.myName.equalsIgnoreCase(str)) {
            return Optional.of(role);
         }
      }
      
      return Optional.empty();
   }
   public static Optional<PrismeRole> safeValueOf(int ord) {
      for (PrismeRole role : PrismeRole.values()) {
         if (role.ordinal() == ord) {
            return Optional.of(role);
         }
      }
      
      return Optional.empty();
   }
}
