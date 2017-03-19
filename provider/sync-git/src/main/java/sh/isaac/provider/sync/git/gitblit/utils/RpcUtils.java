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



package sh.isaac.provider.sync.git.gitblit.utils;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//~--- non-JDK imports --------------------------------------------------------

import com.cedarsoftware.util.io.JsonObject;

import sh.isaac.provider.sync.git.gitblit.models.RepositoryModel;

//~--- classes ----------------------------------------------------------------

/**
 * Utility methods for rpc calls.
 *
 */
public class RpcUtils {
   public static final String RPC_PATH = "/rpc/";

   //~--- enums ---------------------------------------------------------------

   /**
    * The access permissions available for a repository.
    */
   public static enum AccessPermission {
      NONE("N"),
      EXCLUDE("X"),
      VIEW("V"),
      CLONE("R"),
      PUSH("RW"),
      CREATE("RWC"),
      DELETE("RWD"),
      REWIND("RW+"),
      OWNER("RW+");

      public static final AccessPermission[] NEWPERMISSIONS = {
         EXCLUDE, VIEW, CLONE, PUSH, CREATE, DELETE, REWIND
      };
      public static final AccessPermission[] SSHPERMISSIONS = { VIEW, CLONE, PUSH };
      public static AccessPermission         LEGACY         = REWIND;

      //~--- fields -----------------------------------------------------------

      public final String code;

      //~--- constructors -----------------------------------------------------

      private AccessPermission(String code) {
         this.code = code;
      }

      //~--- methods ----------------------------------------------------------

      public String asRole(String repository) {
         return code + ":" + repository;
      }

      public boolean atLeast(AccessPermission perm) {
         return ordinal() >= perm.ordinal();
      }

      public boolean atMost(AccessPermission perm) {
         return ordinal() <= perm.ordinal();
      }

      public boolean exceeds(AccessPermission perm) {
         return ordinal() > perm.ordinal();
      }

      public static AccessPermission fromCode(String code) {
         for (AccessPermission perm: values()) {
            if (perm.code.equalsIgnoreCase(code)) {
               return perm;
            }
         }

         return AccessPermission.NONE;
      }

      public static AccessPermission permissionFromRole(String role) {
         String[] fields = role.split(":", 2);

         if (fields.length == 1) {
            // legacy/undefined assume full permissions
            return AccessPermission.LEGACY;
         } else {
            // code:repository
            return AccessPermission.fromCode(fields[0]);
         }
      }

      public static String repositoryFromRole(String role) {
         String[] fields = role.split(":", 2);

         if (fields.length == 1) {
            // legacy/undefined assume full permissions
            return role;
         } else {
            // code:repository
            return fields[1];
         }
      }

      @Override
      public String toString() {
         return code;
      }
   }

   /**
    * Enumeration representing the four access restriction levels.
    */
   public static enum AccessRestrictionType {
      NONE,
      PUSH,
      CLONE,
      VIEW;

      private static final AccessRestrictionType[] AUTH_TYPES = { PUSH, CLONE, VIEW };

      //~--- methods ----------------------------------------------------------

      public boolean atLeast(AccessRestrictionType type) {
         return this.ordinal() >= type.ordinal();
      }

      public static List<AccessRestrictionType> choices(boolean allowAnonymousPush) {
         if (allowAnonymousPush) {
            return Arrays.asList(values());
         }

         return Arrays.asList(AUTH_TYPES);
      }

      public boolean exceeds(AccessRestrictionType type) {
         return this.ordinal() > type.ordinal();
      }

      public static AccessRestrictionType fromName(String name) {
         for (AccessRestrictionType type: values()) {
            if (type.name()
                    .equalsIgnoreCase(name)) {
               return type;
            }
         }

         return NONE;
      }

      @Override
      public String toString() {
         return name();
      }

      //~--- get methods ------------------------------------------------------

      public boolean isValidPermission(AccessPermission permission) {
         switch (this) {
         case VIEW:

            // VIEW restriction
            // all access permissions are valid
            return true;

         case CLONE:

            // CLONE restriction
            // only CLONE or greater access permissions are valid
            return permission.atLeast(AccessPermission.CLONE);

         case PUSH:

            // PUSH restriction
            // only PUSH or greater access permissions are valid
            return permission.atLeast(AccessPermission.PUSH);

         case NONE:

            // NO access restriction
            // all access permissions are invalid
            return false;
         }

         return false;
      }
   }

   /**
    * Enumeration representing the types of authorization control for an
    * access restricted resource.
    */
   public static enum AuthorizationControl {
      AUTHENTICATED,
      NAMED;

      public static AuthorizationControl fromName(String name) {
         for (AuthorizationControl type: values()) {
            if (type.name()
                    .equalsIgnoreCase(name)) {
               return type;
            }
         }

         return NAMED;
      }

      @Override
      public String toString() {
         return name();
      }
   }

   /**
    * Enumeration representing the federation types.
    */
   public static enum FederationStrategy {
      EXCLUDE,
      FEDERATE_THIS,
      FEDERATE_ORIGIN;

      public boolean atLeast(FederationStrategy type) {
         return this.ordinal() >= type.ordinal();
      }

      public boolean exceeds(FederationStrategy type) {
         return this.ordinal() > type.ordinal();
      }

      public static FederationStrategy fromName(String name) {
         for (FederationStrategy type: values()) {
            if (type.name()
                    .equalsIgnoreCase(name)) {
               return type;
            }
         }

         return FEDERATE_THIS;
      }

      @Override
      public String toString() {
         return name();
      }
   }

   /**
    * Enumeration representing the possible remote procedure call requests from
    * a client.
    */
   public static enum RpcRequest {
      CREATE_REPOSITORY,
      LIST_REPOSITORIES;

      public boolean exceeds(RpcRequest type) {
         return this.ordinal() > type.ordinal();
      }

      public static RpcRequest fromName(String name) {
         for (RpcRequest type: values()) {
            if (type.name()
                    .equalsIgnoreCase(name)) {
               return type;
            }
         }

         return null;
      }

      @Override
      public String toString() {
         return name();
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    *
    * @param remoteURL
    * the url of the remote gitblit instance
    * @param req
    * the rpc request type
    * @param name
    * the name of the actionable object
    * @return
    */
   public static String asLink(String remoteURL, RpcRequest req, String name) {
      if ((remoteURL.length() > 0) && (remoteURL.charAt(remoteURL.length() - 1) == '/')) {
         remoteURL = remoteURL.substring(0, remoteURL.length() - 1);
      }

      if (req == null) {
         req = RpcRequest.LIST_REPOSITORIES;
      }

      return remoteURL + RPC_PATH + "?req=" + req.name().toLowerCase() + ((name == null) ? ""
            : ("&name=" + StringUtils.encodeURL(name)));
   }

   /**
    * Create a repository on the Gitblit server.
    *
    * @param repository
    * @param serverUrl
    * @param account
    * @param password
    * @return true if the action succeeded
    * @throws IOException
    */
   public static boolean createRepository(RepositoryModel repository,
         String serverUrl,
         String account,
         char[] password)
            throws IOException {
      // ensure repository name ends with .git
      if (!repository.name.endsWith(".git")) {
         repository.name += ".git";
      }

      return doAction(RpcRequest.CREATE_REPOSITORY, null, repository, serverUrl, account, password);
   }

   /**
    * Do the specified administrative action on the Gitblit server.
    *
    * @param request
    * @param name
    * the name of the object (may be null)
    * @param object
    * @param serverUrl
    * @param account
    * @param password
    * @return true if the action succeeded
    * @throws IOException
    */
   protected static boolean doAction(RpcRequest request,
                                     String name,
                                     Object object,
                                     String serverUrl,
                                     String account,
                                     char[] password)
            throws IOException {
      String url        = asLink(serverUrl, request, name);
      int    resultCode = JsonUtils.sendJsonString(url, JsonUtils.toJsonString(object), account, password);

      return resultCode == 200;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Retrieves a map of the repositories at the remote gitblit instance keyed
    * by the repository clone url.
    *
    * @param serverUrl
    * @param account
    * @param password
    * @return a map of cloneable repositories
    * @throws IOException
    */
   public static JsonObject<String, Map<String, ?>> getRepositories(String serverUrl,
         String account,
         char[] password)
            throws IOException {
      String url = asLink(serverUrl, RpcRequest.LIST_REPOSITORIES, null);

      return JsonUtils.retrieveJson(url, account, password);
   }
}

