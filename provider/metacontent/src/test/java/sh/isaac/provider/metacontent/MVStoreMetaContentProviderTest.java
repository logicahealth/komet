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



package sh.isaac.provider.metacontent;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.concurrent.ConcurrentMap;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Assert;
import org.junit.Test;

import sh.isaac.provider.metacontent.userPrefs.UserPreference;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MVStoreMetaContentProviderTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MVStoreMetaContentProviderTest {
   /**
    * Test my data.
    *
    * @throws Exception the exception
    */
   @Test
   public void testMyData()
            throws Exception {
      MVStoreMetaContentProvider  store = new MVStoreMetaContentProvider(new File("target"), "test", true);
      ConcurrentMap<String, Long> map   = store.<String, Long>openStore("fred");

      map.put("a", 500l);
      map.put("a", 600l);
      map.put("b", Long.MAX_VALUE);
      Assert.assertEquals(600l, map.get("a")
                                   .longValue());
      Assert.assertEquals(Long.MAX_VALUE, map.get("b")
            .longValue());
      store.close();
      store = new MVStoreMetaContentProvider(new File("target"), "test", false);
      map   = store.<String, Long>openStore("fred");
      Assert.assertEquals(600l, map.get("a")
                                   .longValue());
      Assert.assertEquals(Long.MAX_VALUE, map.get("b")
            .longValue());
      Assert.assertNotNull(map.get("a"));
      map.remove("a");
      Assert.assertNull(map.get("a"));
      store.removeStore("fred");
      map = store.<String, Long>openStore("fred");
      Assert.assertNull(map.get("b"));
   }

   /**
    * Test user prefs.
    *
    * @throws Exception the exception
    */
   @Test
   public void testUserPrefs()
            throws Exception {
      MVStoreMetaContentProvider store = new MVStoreMetaContentProvider(new File("target"), "test", true);

      store.putUserPrefs(5, new UserPreference("hi there"));

      UserPreference b = new UserPreference(store.getUserPrefs(5));

      Assert.assertTrue(b.toString()
                         .equals("hi there"));
      store.close();
      store = new MVStoreMetaContentProvider(new File("target"), "test", false);
      b     = new UserPreference(store.getUserPrefs(5));
      Assert.assertTrue(b.toString()
                         .equals("hi there"));
      store.close();
      store = new MVStoreMetaContentProvider(new File("target"), "test", true);
      Assert.assertNull(store.getUserPrefs(5));
      store.close();
   }
}

