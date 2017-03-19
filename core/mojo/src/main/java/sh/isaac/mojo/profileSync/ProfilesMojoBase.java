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



package sh.isaac.mojo.profileSync;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import sh.isaac.api.LookupService;
import sh.isaac.api.sync.SyncFiles;
import sh.isaac.mojo.external.QuasiMojo;

//~--- classes ----------------------------------------------------------------

/**
 * {@link ProfilesMojoBase}
 *
 * This allows authentication to be passed in via system property, parameter, or, will
 * prompt for the username/password (if allowed by the system property 'profileSyncNoPrompt')
 * IN THAT ORDER.  System properties have the highest priority.
 *
 * To prevent prompting during automated runs - set the system property 'profileSyncNoPrompt=true'
 * To set the username via system property - set 'profileSyncUsername=username'
 * To set the password via system property - set 'profileSyncPassword=password'
 *
 * To enable authentication without prompts, using public keys - set both of the following
 *   'profileSyncUsername=username'
 *   'profileSyncNoPrompt=true'
 *
 * This will cause a public key authentication to be attempted using the ssh credentials found
 * in the current users .ssh folder (in their home directory)
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class ProfilesMojoBase
        extends QuasiMojo {
   // For disabling Profile Sync entirely
   public static final String PROFILE_SYNC_DISABLE = "profileSyncDisable";

   // For preventing command line prompts for credentials during automated runs - set this system property to true.
   public static final String PROFILE_SYNC_NO_PROMPTS = "profileSyncNoPrompt";

   // Allow setting the username via a system property
   public static final String PROFILE_SYNC_USERNAME_PROPERTY = "profileSyncUsername";

   // Allow setting the password via a system property
   public static final String PROFILE_SYNC_PWD_PROPERTY = "profileSyncPassword";
   private static String      username                  = null;
   private static char[]      pwd                       = null;

   //~--- fields --------------------------------------------------------------

   private boolean disableHintGiven = false;

   /**
    * The location of the (already existing) profiles folder which should be shared via SCM.
    */
   @Parameter(required = true)
   File userProfileFolderLocation = null;

   /**
    * The location URL to use when connecting to the sync service
    */
   @Parameter(required = true)
   String changeSetURL = null;

   /**
    * The Type of the specified changeSetURL - should be GIT or SVN
    */
   @Parameter(required = true)
   String changeSetURLType = null;

   /**
    * The username to use for remote operations
    */
   @Parameter(required = false)
   private String profileSyncUsername = null;

   /**
    * The password to use for remote operations
    */
   @Parameter(required = false)
   private String profileSyncPassword = null;

   //~--- constructors --------------------------------------------------------

   public ProfilesMojoBase()
            throws MojoExecutionException {
      super();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      if (StringUtils.isNotBlank(changeSetURL) &&
            !changeSetURLType.equalsIgnoreCase("GIT") &&
            !changeSetURLType.equalsIgnoreCase("SVN")) {
         throw new MojoExecutionException("Change set URL type must be GIT or SVN");
      }
   }

   protected boolean skipRun() {
      if (Boolean.valueOf(System.getProperty(PROFILE_SYNC_DISABLE))) {
         return true;
      }

      if (StringUtils.isBlank(changeSetURL)) {
         getLog().info("No SCM configuration will be done - no 'changeSetUrl' parameter was provided");
         return true;
      } else {
         return false;
      }
   }

   //~--- get methods ---------------------------------------------------------

   protected char[] getPassword()
            throws MojoExecutionException  // protected String getPassword() throws MojoExecutionException
   {
      if (pwd == null) {
         pwd = System.getProperty(PROFILE_SYNC_PWD_PROPERTY)
                     .toCharArray();

         // still blank, try the passed in param
         if (pwd.length == 0)  // if (StringUtils.isBlank(pwd))
         {
            pwd = profileSyncPassword.toCharArray();
         }

         // still no password, prompt if allowed
         if ((pwd.length == 0) &&!Boolean.valueOf(System.getProperty(PROFILE_SYNC_NO_PROMPTS))) {
            Callable<Void> callable = new Callable<Void>() {
               @Override
               public Void call()
                        throws Exception {
                  try {
                     if (!disableHintGiven) {
                        System.out.println("To disable remote sync during build, add '-D" + PROFILE_SYNC_DISABLE +
                                           "=true' to your maven command");
                        disableHintGiven = true;
                     }

                     System.out.println("Enter the " + changeSetURLType +
                                        " password for the Profiles/Changset remote store: (" + changeSetURL + "):");

                     // Use console if available, for password masking
                     Console console = System.console();

                     if (console != null) {
                        pwd = console.readPassword();
                     } else {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                        pwd = br.readLine()
                                .toCharArray();
                     }
                  } catch (IOException e) {
                     throw new MojoExecutionException("Error reading password from console");
                  }

                  return null;
               }
            };

            try {
               Executors.newSingleThreadExecutor(new ThreadFactory() {
                                                    @Override
                                                    public Thread newThread(Runnable r) {
                                                       Thread t = new Thread(r, "User Password Prompt Thread");

                                                       t.setDaemon(true);
                                                       return t;
                                                    }
                                                 }).submit(callable).get(2, TimeUnit.MINUTES);
            } catch (TimeoutException | InterruptedException e) {
               throw new MojoExecutionException("Password not provided within timeout");
            } catch (ExecutionException ee) {
               throw((ee.getCause() instanceof MojoExecutionException) ? (MojoExecutionException) ee.getCause()
                     : new MojoExecutionException("Unexpected", ee.getCause()));
            }
         }
      }

      return pwd;
   }

   protected SyncFiles getProfileSyncImpl()
            throws MojoExecutionException {
      if (changeSetURLType.equalsIgnoreCase("GIT")) {
         SyncFiles svc = LookupService.getService(SyncFiles.class, "GIT");

         if (svc == null) {
            throw new MojoExecutionException(
                "Unable to load the GIT implementation of the ProfileSyncI interface." +
                "  Is sh.isaac.gui.modules.sync-git listed as a dependency for the mojo execution?");
         }

         svc.setRootLocation(userProfileFolderLocation);
         return svc;
      } else if (changeSetURLType.equalsIgnoreCase("SVN")) {
         SyncFiles svc = LookupService.getService(SyncFiles.class, "SVN");

         if (svc == null) {
            throw new MojoExecutionException(
                "Unable to load the SVN implementation of the ProfileSyncI interface." +
                "  Is sh.isaac.gui.modules.sync-svn listed as a dependency for the mojo execution?");
         }

         svc.setRootLocation(userProfileFolderLocation);
         return svc;
      } else {
         throw new MojoExecutionException("Unsupported change set URL Type");
      }
   }

   /**
    * Does the necessary substitution to put the contents of getUserName() into the URL, if a known pattern needing substitution is found.
    *  ssh://someuser@csfe.aceworkspace.net:29418/... for example needs to become:
    *  ssh://<getUsername()>@csfe.aceworkspace.net:29418/...
    * @throws MojoExecutionException
    */
   protected String getURL()
            throws MojoExecutionException {
      return getProfileSyncImpl().substituteURL(changeSetURL, getUsername());
   }

   protected String getUsername()
            throws MojoExecutionException {
      if (username == null) {
         username = System.getProperty(PROFILE_SYNC_USERNAME_PROPERTY);

         // still blank, try property
         if (StringUtils.isBlank(username)) {
            username = profileSyncUsername;
         }

         // still no username, prompt if allowed
         if (StringUtils.isBlank(username) &&!Boolean.valueOf(System.getProperty(PROFILE_SYNC_NO_PROMPTS))) {
            Callable<Void> callable = new Callable<Void>() {
               @Override
               public Void call()
                        throws Exception {
                  if (!disableHintGiven) {
                     System.out.println("To disable remote sync during build, add '-D" + PROFILE_SYNC_DISABLE +
                                        "=true' to your maven command");
                     disableHintGiven = true;
                  }

                  try {
                     System.out.println("Enter the " + changeSetURLType +
                                        " username for the Profiles/Changset remote store (" + changeSetURL + "):");

                     BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                     username = br.readLine();
                  } catch (IOException e) {
                     throw new MojoExecutionException("Error reading username from console");
                  }

                  return null;
               }
            };

            try {
               Executors.newSingleThreadExecutor(new ThreadFactory() {
                                                    @Override
                                                    public Thread newThread(Runnable r) {
                                                       Thread t = new Thread(r, "User Prompt Thread");

                                                       t.setDaemon(true);
                                                       return t;
                                                    }
                                                 }).submit(callable).get(2, TimeUnit.MINUTES);
            } catch (TimeoutException | InterruptedException e) {
               throw new MojoExecutionException("Username not provided within timeout");
            } catch (ExecutionException ee) {
               throw((ee.getCause() instanceof MojoExecutionException) ? (MojoExecutionException) ee.getCause()
                     : new MojoExecutionException("Unexpected", ee.getCause()));
            }
         }
      }

      return username;
   }
}

