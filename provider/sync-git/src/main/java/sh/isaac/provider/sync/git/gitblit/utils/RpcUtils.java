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
   
   /** The Constant RPC_PATH. */
   public static final String RPC_PATH = "/rpc/";

   //~--- enums ---------------------------------------------------------------

   /**
    * The access permissions available for a repository.
    */
   public static enum AccessPermission {
      
      /** The none. */
      NONE("N"),
      
      /** The exclude. */
      EXCLUDE("X"),
      
      /** The view. */
      VIEW("V"),
      
      /** The clone. */
      CLONE("R"),
      
      /** The push. */
      PUSH("RW"),
      
      /** The create. */
      CREATE("RWC"),
      
      /** The delete. */
      DELETE("RWD"),
      
      /** The rewind. */
      REWIND("RW+"),
      
      /** The owner. */
      OWNER("RW+");

      /** The Constant NEWPERMISSIONS. */
      public static final AccessPermission[] NEWPERMISSIONS = {
         EXCLUDE, VIEW, CLONE, PUSH, CREATE, DELETE, REWIND
      };
      
      /** The Constant SSHPERMISSIONS. */
      public static final AccessPermission[] SSHPERMISSIONS = { VIEW, CLONE, PUSH };
      
      /** The legacy. */
      public static AccessPermission         LEGACY         = REWIND;

      //~--- fields -----------------------------------------------------------

      /** The code. */
      public final String code;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new access permission.
       *
       * @param code the code
       */
      private AccessPermission(String code) {
         this.code = code;
      }

      //~--- methods ----------------------------------------------------------

      /**
       * As role.
       *
       * @param repository the repository
       * @return the string
       */
      public String asRole(String repository) {
         return this.code + ":" + repository;
      }

      /**
       * At least.
       *
       * @param perm the perm
       * @return true, if successful
       */
      public boolean atLeast(AccessPermission perm) {
         return ordinal() >= perm.ordinal();
      }

      /**
       * At most.
       *
       * @param perm the perm
       * @return true, if successful
       */
      public boolean atMost(AccessPermission perm) {
         return ordinal() <= perm.ordinal();
      }

      /**
       * Exceeds.
       *
       * @param perm the perm
       * @return true, if successful
       */
      public boolean exceeds(AccessPermission perm) {
         return ordinal() > perm.ordinal();
      }

      /**
       * From code.
       *
       * @param code the code
       * @return the access permission
       */
      public static AccessPermission fromCode(String code) {
         for (final AccessPermission perm: values()) {
            if (perm.code.equalsIgnoreCase(code)) {
               return perm;
            }
         }

         return AccessPermission.NONE;
      }

      /**
       * Permission from role.
       *
       * @param role the role
       * @return the access permission
       */
      public static AccessPermission permissionFromRole(String role) {
         final String[] fields = role.split(":", 2);

         if (fields.length == 1) {
            // legacy/undefined assume full permissions
            return AccessPermission.LEGACY;
         } else {
            // code:repository
            return AccessPermission.fromCode(fields[0]);
         }
      }

      /**
       * Repository from role.
       *
       * @param role the role
       * @return the string
       */
      public static String repositoryFromRole(String role) {
         final String[] fields = role.split(":", 2);

         if (fields.length == 1) {
            // legacy/undefined assume full permissions
            return role;
         } else {
            // code:repository
            return fields[1];
         }
      }

      /**
       * To string.
       *
       * @return the string
       */
      @Override
      public String toString() {
         return this.code;
      }
   }

   /**
    * Enumeration representing the four access restriction levels.
    */
   public static enum AccessRestrictionType {
      
      /** The none. */
      NONE,
      
      /** The push. */
      PUSH,
      
      /** The clone. */
      CLONE,
      
      /** The view. */
      VIEW;

      /** The Constant AUTH_TYPES. */
      private static final AccessRestrictionType[] AUTH_TYPES = { PUSH, CLONE, VIEW };

      //~--- methods ----------------------------------------------------------

      /**
       * At least.
       *
       * @param type the type
       * @return true, if successful
       */
      public boolean atLeast(AccessRestrictionType type) {
         return this.ordinal() >= type.ordinal();
      }

      /**
       * Choices.
       *
       * @param allowAnonymousPush the allow anonymous push
       * @return the list
       */
      public static List<AccessRestrictionType> choices(boolean allowAnonymousPush) {
         if (allowAnonymousPush) {
            return Arrays.asList(values());
         }

         return Arrays.asList(AUTH_TYPES);
      }

      /**
       * Exceeds.
       *
       * @param type the type
       * @return true, if successful
       */
      public boolean exceeds(AccessRestrictionType type) {
         return this.ordinal() > type.ordinal();
      }

      /**
       * From name.
       *
       * @param name the name
       * @return the access restriction type
       */
      public static AccessRestrictionType fromName(String name) {
         for (final AccessRestrictionType type: values()) {
            if (type.name()
                    .equalsIgnoreCase(name)) {
               return type;
            }
         }

         return NONE;
      }

      /**
       * To string.
       *
       * @return the string
       */
      @Override
      public String toString() {
         return name();
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Checks if valid permission.
       *
       * @param permission the permission
       * @return true, if valid permission
       */
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
      
      /** The authenticated. */
      AUTHENTICATED,
      
      /** The named. */
      NAMED;

      /**
       * From name.
       *
       * @param name the name
       * @return the authorization control
       */
      public static AuthorizationControl fromName(String name) {
         for (final AuthorizationControl type: values()) {
            if (type.name()
                    .equalsIgnoreCase(name)) {
               return type;
            }
         }

         return NAMED;
      }

      /**
       * To string.
       *
       * @return the string
       */
      @Override
      public String toString() {
         return name();
      }
   }

   /**
    * Enumeration representing the federation types.
    */
   public static enum FederationStrategy {
      
      /** The exclude. */
      EXCLUDE,
      
      /** The federate this. */
      FEDERATE_THIS,
      
      /** The federate origin. */
      FEDERATE_ORIGIN;

      /**
       * At least.
       *
       * @param type the type
       * @return true, if successful
       */
      public boolean atLeast(FederationStrategy type) {
         return this.ordinal() >= type.ordinal();
      }

      /**
       * Exceeds.
       *
       * @param type the type
       * @return true, if successful
       */
      public boolean exceeds(FederationStrategy type) {
         return this.ordinal() > type.ordinal();
      }

      /**
       * From name.
       *
       * @param name the name
       * @return the federation strategy
       */
      public static FederationStrategy fromName(String name) {
         for (final FederationStrategy type: values()) {
            if (type.name()
                    .equalsIgnoreCase(name)) {
               return type;
            }
         }

         return FEDERATE_THIS;
      }

      /**
       * To string.
       *
       * @return the string
       */
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
      
      /** The create repository. */
      CREATE_REPOSITORY,
      
      /** The list repositories. */
      LIST_REPOSITORIES;

      /**
       * Exceeds.
       *
       * @param type the type
       * @return true, if successful
       */
      public boolean exceeds(RpcRequest type) {
         return this.ordinal() > type.ordinal();
      }

      /**
       * From name.
       *
       * @param name the name
       * @return the rpc request
       */
      public static RpcRequest fromName(String name) {
         for (final RpcRequest type: values()) {
            if (type.name()
                    .equalsIgnoreCase(name)) {
               return type;
            }
         }

         return null;
      }

      /**
       * To string.
       *
       * @return the string
       */
      @Override
      public String toString() {
         return name();
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * As link.
    *
    * @param remoteURL the url of the remote gitblit instance
    * @param req the rpc request type
    * @param name the name of the actionable object
    * @return the string
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
    * @param repository the repository
    * @param serverUrl the server url
    * @param account the account
    * @param password the password
    * @return true if the action succeeded
    * @throws IOException Signals that an I/O exception has occurred.
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
    * @param request the request
    * @param name the name of the object (may be null)
    * @param object the object
    * @param serverUrl the server url
    * @param account the account
    * @param password the password
    * @return true if the action succeeded
    * @throws IOException Signals that an I/O exception has occurred.
    */
   protected static boolean doAction(RpcRequest request,
                                     String name,
                                     Object object,
                                     String serverUrl,
                                     String account,
                                     char[] password)
            throws IOException {
      final String url        = asLink(serverUrl, request, name);
      final int    resultCode = JsonUtils.sendJsonString(url, JsonUtils.toJsonString(object), account, password);

      return resultCode == 200;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Retrieves a map of the repositories at the remote gitblit instance keyed
    * by the repository clone url.
    *
    * @param serverUrl the server url
    * @param account the account
    * @param password the password
    * @return a map of cloneable repositories
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static JsonObject<String, Map<String, ?>> getRepositories(String serverUrl,
         String account,
         char[] password)
            throws IOException {
      final String url = asLink(serverUrl, RpcRequest.LIST_REPOSITORIES, null);

      return JsonUtils.retrieveJson(url, account, password);
   }
}

