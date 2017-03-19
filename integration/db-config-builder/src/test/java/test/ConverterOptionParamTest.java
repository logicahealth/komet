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



package test;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Assert;
import org.junit.Test;

import sh.isaac.pombuilder.GitPublish;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.ConverterOptionParamSuggestedValue;

//~--- classes ----------------------------------------------------------------

/**
 * {@link ConverterOptionParamTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConverterOptionParamTest {
   @Test
   public void testChangesetURLRewrite()
            throws IOException {
      Assert.assertEquals("https://git.isaac.sh/git/r/contentConfigurations.git",
                          GitPublish.constructChangesetRepositoryURL("https://git.isaac.sh/git/"));
      Assert.assertEquals("https://git.isaac.sh/git/r/contentConfigurations.git",
                          GitPublish.constructChangesetRepositoryURL("https://git.isaac.sh/git"));
      Assert.assertEquals("http://git.isaac.sh/git/r/contentConfigurations.git",
                          GitPublish.constructChangesetRepositoryURL("http://git.isaac.sh/git/"));
      Assert.assertEquals("https://git.isaac.sh/git/r/contentConfigurations.git",
                          GitPublish.constructChangesetRepositoryURL(
                              "https://git.isaac.sh/git/r/contentConfigurations.git"));
      Assert.assertEquals("https://git.isaac.sh/git/r/foo.git",
                          GitPublish.constructChangesetRepositoryURL("https://git.isaac.sh/git/r/foo.git"));
   }

   @Test
   public void testJson()
            throws Exception {
      final ConverterOptionParam foo = new ConverterOptionParam("cc",
                                                          "a",
                                                          "b",
                                                          true,
                                                          true,
                                                          new ConverterOptionParamSuggestedValue("e", "e1"),
                                                          new ConverterOptionParamSuggestedValue("f"));

      Assert.assertEquals("e", foo.getSuggestedPickListValues()[0]
                                  .getValue());
      Assert.assertEquals("e1", foo.getSuggestedPickListValues()[0]
                                   .getDescription());
      Assert.assertEquals("f", foo.getSuggestedPickListValues()[1]
                                  .getValue());
      Assert.assertEquals("f", foo.getSuggestedPickListValues()[1]
                                  .getDescription());

      final ConverterOptionParam foo2 = new ConverterOptionParam("33",
                                                           "1",
                                                           "2",
                                                           true,
                                                           false,
                                                           new ConverterOptionParamSuggestedValue("3", "31"),
                                                           new ConverterOptionParamSuggestedValue("4", "41"));

      ConverterOptionParam.serialize(new ConverterOptionParam[] { foo, foo2 }, new File("foo.json"));

      final ConverterOptionParam[] foo3 = ConverterOptionParam.fromFile(new File("foo.json"));

      Assert.assertEquals(foo3[0], foo);
      Assert.assertEquals(foo3[1], foo2);
      new File("foo.json").delete();
   }
}

