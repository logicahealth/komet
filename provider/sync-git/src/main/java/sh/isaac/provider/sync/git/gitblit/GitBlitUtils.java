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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.isaac.provider.sync.git.gitblit.models.RepositoryModel;
import sh.isaac.provider.sync.git.gitblit.utils.RpcUtils;
import sh.isaac.provider.sync.git.gitblit.utils.RpcUtils.AccessRestrictionType;

//~--- classes ----------------------------------------------------------------

/**
 * {@link GitBlitUtils}
 *
 * This entire package exists because the GitBlit client API is a bit painful to use, and the client libraries they produce
 * aren't available in maven central, and they have a dependency chain we may not want to drag in.
 *
 * The code in this package, and below, are extracted from http://gitblit.github.io/gitblit-maven
 * within the com.gitblit:gbapi:1.8.0 module.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class GitBlitUtils {
   
   /** The log. */
   private static Logger log = LoggerFactory.getLogger(GitBlitUtils.class);

   //~--- methods -------------------------------------------------------------

   /**
    * This hackery is being done because of a code-sync issue between PRISME and ISAAC-Rest, where PRISME is putting a bare URL into the props file.
    * It will be fixed on the PRISME side, eventually, making this method a noop - but for now, handle either the old or new style.
    * 
    * Essentially, if we see a bare URL like https://vaauscttdbs80.aac.va.gov:8080 we add /git to the end of it.
    * If we see a URL that includes a location - like https://vaauscttdbs80.aac.va.gov:8080/gitServer - we do nothing more than add a trailing forward slash
    *
    * @param url the url
    * @return the string
    */
   public static String adjustBareUrlForGitBlit(String url) {
      String temp = url;

      if (!temp.endsWith("/")) {
         temp += "/";
      }

      if (temp.matches("(?i)https?:\\/\\/[a-zA-Z0-9\\.\\-_]+:?\\d*\\/$")) {
         temp += "git/";
      }

      return temp;
   }

   /**
    * Create a repository on a remote gitblit server.
    *
    * @param baseRemoteAddress - should be a url like https://git.isaac.sh/git/ (though {@link #adjustBareUrlForGitBlit(String)} is utilized
    * @param repoName a name such a foo or foo.git
    * @param repoDesc the description
    * @param username the username
    * @param password the password
    * @param allowRead true to allow unauthenticated users to read / clone the repository.  False to lock down the repository
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void createRepository(String baseRemoteAddress,
         String repoName,
         String repoDesc,
         String username,
         char[] password,
         boolean allowRead)
            throws IOException {
      try {
         final RepositoryModel rm = new RepositoryModel(repoName, repoDesc, username, new Date());

         if (allowRead) {
            rm.accessRestriction = AccessRestrictionType.PUSH.toString();
         }

         final boolean status = RpcUtils.createRepository(rm, adjustBareUrlForGitBlit(baseRemoteAddress), username, password);

         log.info("Repository: " + repoName + ", create successfully: " + status);

         if (!status) {
            throw new IOException("Create of repo '" + repoName + "' failed");
         }
      } catch (final Exception e) {
         log.error("Failed to create repository: " + repoName + ", Unexpected Error: ", e);
         throw new IOException("Failed to create repository: " + repoName + ", Internal error", e);
      }
   }

   /**
    * Take in a URL like https://git.isaac.sh/git/r/db_test.git
    * and turn it into https://git.isaac.sh/git
    *
    * @param url the url
    * @return the string
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static String parseBaseRemoteAddress(String url)
            throws IOException {
      final Pattern p =
         Pattern.compile("(?i)(https?:\\/\\/[a-zA-Z0-9\\.\\-_]+:?\\d*\\/[a-zA-Z0-9\\-_]+\\/)r\\/[a-zA-Z0-9\\-_]+.git$");
      final Matcher m = p.matcher(url);

      if (m.find()) {
         return m.group(1);
      }

      throw new IOException("Not a known giblit url pattern!");
   }

   /**
    * Read repositories.
    *
    * @param baseRemoteAddress the base remote address
    * @param username the username
    * @param password the password
    * @return the set
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static Set<String> readRepositories(String baseRemoteAddress,
         String username,
         char[] password)
            throws IOException {
      final HashSet<String> results = new HashSet<>();

      RpcUtils.getRepositories(adjustBareUrlForGitBlit(baseRemoteAddress), username, password)
              .forEach((name, value) -> results.add((String) value.get("name")));
      return results;
   }
}

