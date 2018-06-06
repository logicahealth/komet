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

package sh.isaac.provider.sync.git;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import sh.isaac.provider.sync.git.gitblit.GitBlitUtils;

//~--- classes ----------------------------------------------------------------

/**
 * {@link GitBlitUtilsTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class GitBlitUtilsTest {
   /**
    * Test base URL parse.
    */
   @Test
   public void TestBaseURLParse() {
      Assert.assertEquals("https://servername.not.real.com:8080/git/", GitBlitUtils.adjustBareUrlForGitBlit("https://servername.not.real.com:8080/"));
      Assert.assertEquals("https://Servername.not.real.com:8080/git/", GitBlitUtils.adjustBareUrlForGitBlit("https://Servername.not.real.com:8080"));
      Assert.assertEquals("http://servername.not.real.com:8080/git/", GitBlitUtils.adjustBareUrlForGitBlit("http://servername.not.real.com:8080/"));
      Assert.assertEquals("http://servername.not.real.com:8080/git/", GitBlitUtils.adjustBareUrlForGitBlit("http://servername.not.real.com:8080"));
      Assert.assertEquals("https://servername.not.real.com/git/", GitBlitUtils.adjustBareUrlForGitBlit("https://servername.not.real.com/"));
      Assert.assertEquals("https://servername.not.real.com/git/", GitBlitUtils.adjustBareUrlForGitBlit("https://servername.not.real.com"));
      Assert.assertEquals("https://servername.not.real.com/fred/", GitBlitUtils.adjustBareUrlForGitBlit("https://servername.not.real.com/fred"));
      Assert.assertEquals("https://servername.not.real.com:8080/git/", GitBlitUtils.adjustBareUrlForGitBlit("https://servername.not.real.com:8080/git"));
      Assert.assertEquals("https://servername.not.real.com:8080/git/", GitBlitUtils.adjustBareUrlForGitBlit("https://servername.not.real.com:8080/git/"));
      Assert.assertEquals("https://servername.not.real.com:8080/fred/", GitBlitUtils.adjustBareUrlForGitBlit("https://servername.not.real.com:8080/fred"));
      Assert.assertEquals("https://servername.not.real.com:8080/fred/", GitBlitUtils.adjustBareUrlForGitBlit("https://servername.not.real.com:8080/fred/"));
      Assert.assertEquals("HTtps://ser-ver_0.not.real.com:8080/git/", GitBlitUtils.adjustBareUrlForGitBlit("HTtps://ser-ver_0.not.real.com:8080/"));
   }

   /**
    * Test URL adjust.
    *
    * @throws Exception
    *            the exception
    */
   @Test
   public void TestURLAdjust() throws Exception {
      Assert.assertEquals("https://another.server.com:4848/git/", GitBlitUtils.parseBaseRemoteAddress("https://another.server.com:4848/git/r/db_test.git"));
      Assert.assertEquals("https://another.server.com:4848/git/", GitBlitUtils.parseBaseRemoteAddress("https://another.server.com:4848/git/r/db_test.GIT"));
      Assert.assertEquals("Https://another.se-ve_r.com:4848/git/", GitBlitUtils.parseBaseRemoteAddress("Https://another.se-ve_r.com:4848/git/r/db_-test.git"));

   }
   
   /**
    * Test changeset URL rewrite.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Test
   public void testChangesetURLRewrite()
            throws IOException {
      Assert.assertEquals("https://git.isaac.sh:4848/git/r/contentConfigurations.git",
            GitBlitUtils.constructChangesetRepositoryURL("https://git.isaac.sh:4848/git/"));
      Assert.assertEquals("https://git.isaac.sh:4848/git/r/contentConfigurations.git",
            GitBlitUtils.constructChangesetRepositoryURL("https://git.isaac.sh:4848/git"));
      Assert.assertEquals("http://git.isaac.sh:4848/git/r/contentConfigurations.git",
            GitBlitUtils.constructChangesetRepositoryURL("http://git.isaac.sh:4848/git/"));
      Assert.assertEquals("https://git.isaac.sh:4848/git/r/contentConfigurations.git",
            GitBlitUtils.constructChangesetRepositoryURL("https://git.isaac.sh:4848/git/r/contentConfigurations.git"));
      Assert.assertEquals("https://git.isaac.sh:4848/git/r/foo.git", GitBlitUtils.constructChangesetRepositoryURL("https://git.isaac.sh:4848/git/r/foo.git"));
   }
}
