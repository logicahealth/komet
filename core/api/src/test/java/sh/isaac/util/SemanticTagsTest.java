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



package sh.isaac.util;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Assert;
import org.junit.Test;

import sh.isaac.api.util.SemanticTags;

//~--- classes ----------------------------------------------------------------

/**
 * {@link SemanticTagsTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SemanticTagsTest {

   @Test
   public void containsSemanticTag() throws Exception {

      Assert.assertTrue(SemanticTags.containsSemanticTag("a fred (bear)"));
      Assert.assertTrue(SemanticTags.containsSemanticTag("a fred (beA12ar)"));
      Assert.assertTrue(SemanticTags.containsSemanticTag("a  fred (bear)"));
      Assert.assertTrue(SemanticTags.containsSemanticTag("a fred   (bear)"));
      Assert.assertTrue(SemanticTags.containsSemanticTag(" (Bear)"));
      Assert.assertTrue(SemanticTags.containsSemanticTag("something ( something (ab)"));
      Assert.assertTrue(SemanticTags.containsSemanticTag("something (sss) something (ab)"));
      Assert.assertTrue(SemanticTags.containsSemanticTag("a fred ( whatever ( jane ) yes ( a )"));
      
      Assert.assertFalse(SemanticTags.containsSemanticTag("a fred(bear)"));
      Assert.assertFalse(SemanticTags.containsSemanticTag("a fred (bear) "));
      Assert.assertFalse(SemanticTags.containsSemanticTag("a fred (bear) something something"));
      Assert.assertFalse(SemanticTags.containsSemanticTag("a fred"));
      Assert.assertFalse(SemanticTags.containsSemanticTag("a fred ("));
      
   }
   
   @Test
   public void addSemanticTag() throws Exception {

      Assert.assertEquals(SemanticTags.addSemanticTagIfAbsent("a fred", "bear"), "a fred (bear)");
      Assert.assertEquals(SemanticTags.addSemanticTagIfAbsent("a fred", "(bear"), "a fred (bear)");
      Assert.assertEquals(SemanticTags.addSemanticTagIfAbsent("a fred", "(bear)"), "a fred (bear)");
      Assert.assertEquals(SemanticTags.addSemanticTagIfAbsent("a fred", " (bear)"), "a fred (bear)");
      
      Assert.assertEquals(SemanticTags.addSemanticTagIfAbsent("a fred (bear)", "(bear)"), "a fred (bear)");
      Assert.assertEquals(SemanticTags.addSemanticTagIfAbsent("", "(bear)"), "(bear)");
   }

   @Test
   public void stripSemanticTag() throws Exception {

      Assert.assertEquals("a fred", SemanticTags.stripSemanticTagIfPresent("a fred (bear)"));
      Assert.assertEquals("a fred", SemanticTags.stripSemanticTagIfPresent("a fred (beA12ar)"));
      Assert.assertEquals("a fred  ", SemanticTags.stripSemanticTagIfPresent("a fred   (bear)"));
      Assert.assertEquals("", SemanticTags.stripSemanticTagIfPresent(" (Bear)"));
      Assert.assertEquals("something ( something", SemanticTags.stripSemanticTagIfPresent("something ( something (ab)"));
      Assert.assertEquals("something (sss) something", SemanticTags.stripSemanticTagIfPresent("something (sss) something (ab)"));
      Assert.assertEquals("a fred ( whatever ( jane ) yes", SemanticTags.stripSemanticTagIfPresent("a fred ( whatever ( jane ) yes ( a )"));
      
      Assert.assertEquals(SemanticTags.stripSemanticTagIfPresent("a fred(bear)"), "a fred(bear)");
      Assert.assertEquals(SemanticTags.stripSemanticTagIfPresent("a fred (bear) "), "a fred (bear) ");
      Assert.assertEquals(SemanticTags.stripSemanticTagIfPresent("a fred (bear) something something"), "a fred (bear) something something");
      Assert.assertEquals(SemanticTags.stripSemanticTagIfPresent("a fred"), "a fred");
      Assert.assertEquals(SemanticTags.stripSemanticTagIfPresent("a fred ("), "a fred (");

   }
   
   @Test
   public void findSemanticTag() throws Exception {
      Assert.assertEquals("bear", SemanticTags.findSemanticTagIfPresent("a fred (bear)").get());
      Assert.assertEquals("beA12ar", SemanticTags.findSemanticTagIfPresent("a fred (beA12ar)").get());
      Assert.assertEquals("bear", SemanticTags.findSemanticTagIfPresent("a  fred (bear)").get());
      Assert.assertEquals("bear", SemanticTags.findSemanticTagIfPresent("a fred   (bear)").get());
      Assert.assertEquals("Bear", SemanticTags.findSemanticTagIfPresent(" (Bear)").get());
      Assert.assertEquals("ab", SemanticTags.findSemanticTagIfPresent("something ( something (ab)").get());
      Assert.assertEquals("ab", SemanticTags.findSemanticTagIfPresent("something (sss) something (ab)").get());
   }
}

