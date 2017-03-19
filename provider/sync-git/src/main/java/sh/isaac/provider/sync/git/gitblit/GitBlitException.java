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



package sh.isaac.provider.sync.git.gitblit;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

//~--- classes ----------------------------------------------------------------

/**
 * The Class GitBlitException.
 */
public class GitBlitException
        extends IOException {
   
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new git blit exception.
    *
    * @param message the message
    */
   public GitBlitException(String message) {
      super(message);
   }

   /**
    * Instantiates a new git blit exception.
    *
    * @param cause the cause
    */
   public GitBlitException(Throwable cause) {
      super(cause);
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * Exception to indicate that the requested action can not be executed by
    * the specified user.
    */
   public static class ForbiddenException
           extends GitBlitException {
      
      /** The Constant serialVersionUID. */
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new forbidden exception.
       *
       * @param message the message
       */
      public ForbiddenException(String message) {
         super(message);
      }
   }


   /**
    * Exception to indicate that the requested action has been disabled on the
    * Gitblit server.
    */
   public static class NotAllowedException
           extends GitBlitException {
      
      /** The Constant serialVersionUID. */
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new not allowed exception.
       *
       * @param message the message
       */
      public NotAllowedException(String message) {
         super(message);
      }
   }


   /**
    * Exception to indicate that the client should prompt for credentials
    * because the requested action requires authentication.
    */
   public static class UnauthorizedException
           extends GitBlitException {
      
      /** The Constant serialVersionUID. */
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new unauthorized exception.
       *
       * @param message the message
       */
      public UnauthorizedException(String message) {
         super(message);
      }
   }


   /**
    * Exception to indicate that the requested action can not be executed by
    * the server because it does not recognize the request type.
    */
   public static class UnknownRequestException
           extends GitBlitException {
      
      /** The Constant serialVersionUID. */
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new unknown request exception.
       *
       * @param message the message
       */
      public UnknownRequestException(String message) {
         super(message);
      }
   }
}

