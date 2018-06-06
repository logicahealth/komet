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

import java.util.Optional;

/**
 * This is the master role file.  Both Prisme and IsaacRest should refer to this file.
 * Note:  The string for myName should be unique relative to all Roles.
 * Note:  There are Prisme unit tests that will break the build if new roles are added here and
 * not properly referenced in Prisme, so let a prisme dev know if you need to add in a  role.
 * @author cshupp
 * 
 */
public enum SystemRole {
   SUPER_USER(SystemRoleType.GENERAL, SystemRoleConstants.SUPER_USER),
   ADMINISTRATOR(SystemRoleType.GENERAL, SystemRoleConstants.ADMINISTRATOR),
   READ_ONLY(SystemRoleType.GENERAL, SystemRoleConstants.READ_ONLY),
   EDITOR(SystemRoleType.MODELING, SystemRoleConstants.EDITOR),
   REVIEWER(SystemRoleType.MODELING, SystemRoleConstants.REVIEWER),
   APPROVER(SystemRoleType.MODELING, SystemRoleConstants.APPROVER),
   VUID_REQUESTOR(SystemRoleType.GENERAL, SystemRoleConstants.VUID_REQUESTOR),
   NTRT_USER(SystemRoleType.GENERAL, SystemRoleConstants.NTRT_USER),
   NTRT_STAFF(SystemRoleType.GENERAL, SystemRoleConstants.NTRT_STAFF),
   NTRT_ADMIN(SystemRoleType.GENERAL, SystemRoleConstants.NTRT_ADMIN),
   DEPLOYMENT_MANAGER(SystemRoleType.DEPLOYMENT, SystemRoleConstants.DEPLOYMENT_MANAGER),
   AUTOMATED(SystemRoleType.NON_USER, SystemRoleConstants.AUTOMATED);

   private String myName;
   private SystemRoleType myType;

   private SystemRole(SystemRoleType type, String name) {
      myType = type;
      myName = name;
   }

   public String toString() {
      return myName;
   }
   
   public SystemRoleType getType() {
      return myType;
   }
   
    public static SystemRole getEnum(String value) {
        for(SystemRole v : values())
            if(v.toString().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException("No enum existst for " + value);
    }

   public static Optional<SystemRole> safeValueOf(String str) {
      for (SystemRole role : SystemRole.values()) {
         if (role.myName.equalsIgnoreCase(str)) {
            return Optional.of(role);
         }
      }
      
      return Optional.empty();
   }
   public static Optional<SystemRole> safeValueOf(int ord) {
      for (SystemRole role : SystemRole.values()) {
         if (role.ordinal() == ord) {
            return Optional.of(role);
         }
      }
      
      return Optional.empty();
   }
}
