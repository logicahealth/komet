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

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import sh.isaac.api.util.StringUtils;

/**
 * 
 * {@link User}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class User implements Principal {
   private static final long ROLE_CHECK_INTERVAL = 5 * 60 * 1000;  //5 minutes
   private final String name;
   private final String ssoToken;
   private final UUID id;
   private Set<PrismeRole> roles = new HashSet<>();
   private long rolesUpdatedAt;
   
   public User(String name, UUID id, String ssoToken, Collection<PrismeRole> roles) {
      this(name, id, ssoToken, roles != null ? roles.toArray(new PrismeRole[roles.size()]) : (PrismeRole[])null);
   }
   public User(String name, UUID id, String ssoToken, PrismeRole...roles) {
      this.name = name;
      this.id = id;
      this.ssoToken = StringUtils.isBlank(ssoToken) ? null : ssoToken;
      if (roles != null) {
         for (PrismeRole role : roles) {
            this.roles.add(role);
         }
      }
      rolesUpdatedAt = System.currentTimeMillis();
   }

   /* (non-Javadoc)
    * @see java.security.Principal#getName()
    */
   @Override
   public String getName() {
      return name;
   }
   /**
    * @return
    */
   public UUID getId() {
      return id;
   }
   
   /**
    * @return The SSOToken tied to this user.  May be not be present, when in non-prod mode and sso isn't on
    */
   public Optional<String> getSSOToken()
   {
      return Optional.ofNullable(ssoToken);
   }
   
   /**
    * Return true, if the roles are still good, or false, if a recheck against prisme is necessary.
    * @return
    */
   public boolean rolesStillValid()
   {
      return (System.currentTimeMillis() - ROLE_CHECK_INTERVAL) < rolesUpdatedAt;
   }
   
   public long rolesCheckedAt()
   {
      return rolesUpdatedAt;
   }
   
   /**
    * Replace the existing roles with the provided roles
    * @param roles
    */
   public void updateRoles(PrismeRole...roles)
   {
      HashSet<PrismeRole> newRoles = new HashSet<>();
      if (roles != null) {
         for (PrismeRole role : roles) {
            newRoles.add(role);
         }
      }
      this.roles = newRoles;
      this.rolesUpdatedAt = System.currentTimeMillis();
   }
   
   /**
    * @return
    */
   public Set<PrismeRole> getRoles() {
      return Collections.unmodifiableSet(roles);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      User other = (User) obj;
      if (id == null) {
         if (other.id != null)
            return false;
      } else if (!id.equals(other.id))
         return false;
      return true;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "User [name=" + name + ", id=" + id + ", roles=" + roles + ", checkedAt=" + rolesUpdatedAt+"]";
   }
}