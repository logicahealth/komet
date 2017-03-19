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

import sh.isaac.api.util.PasswordHasher;

//~--- classes ----------------------------------------------------------------

/**
 * {@link PasswordHashingTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class PasswordHashingTest {
   @Test
   public void encryptTestFour()
            throws Exception {
      String password  = "";
      String data      = "some data";
      String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertTrue(PasswordHasher.decryptToString(password, encrypted)
                                      .equals(data));

      try {
         String decrypted = PasswordHasher.decryptToString(password, "wrong encrypted string");

         Assert.assertFalse(decrypted.equals(data));
         Assert.fail("Expected an exception, but instead got decrypted data: '" + decrypted + "'");
      } catch (Exception e) {
         // expected
      }
   }

   @Test
   public void encryptTestOne()
            throws Exception {
      String password  = "$sentences_make_better_passwords....";
      String data      = "There was a man with a plan";
      String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertTrue(PasswordHasher.decryptToString(password, encrypted)
                                      .equals(data));

      try {
         String decrypted = PasswordHasher.decryptToString("wrongPassword", encrypted);

         Assert.assertFalse(decrypted.equals(data));
         Assert.fail("Expected an exception, but instead got decrypted data: '" + decrypted + "'");
      } catch (Exception e) {
         // expected
      }
   }

   @Test
   public void encryptTestThree()
            throws Exception {
      String password =
         "µJû5¥¨J«eÜäÅT5¼, BìRß¸jAf½çx.îüöìÍj(Çõïkêpùnðö7¾&Äÿ÷)ÆJgn,GÂá÷+¦òxÂÍ«`¯JXÁ%Ò*ÖtÝ]Ú%U~ÂÅ¿=Ü*º'X·íY(Ù0";
      String data      = "";
      String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertTrue(PasswordHasher.decryptToString(password, encrypted)
                                      .equals(data));

      try {
         String decrypted = PasswordHasher.decryptToString("wrongPassword", encrypted);

         Assert.assertFalse(decrypted.equals(data));
         Assert.fail("Expected an exception, but instead got decrypted data: '" + decrypted + "'");
      } catch (Exception e) {
         // expected
      }
   }

   @Test
   public void encryptTestTwo()
            throws Exception {
      String password  = "simple";
      String data      = "There was a man with a plan that wasn't very good";
      String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertTrue(PasswordHasher.decryptToString(password, encrypted)
                                      .equals(data));

      try {
         String decrypted = PasswordHasher.decryptToString("", encrypted);

         Assert.assertFalse(decrypted.equals(data));
         Assert.fail("Expected an exception, but instead got decrypted data: '" + decrypted + "'");
      } catch (Exception e) {
         // expected
      }
   }

   @Test
   public void hashTestFour()
            throws Exception {
      String password     = "$sentences_make_better_$$$passwords....";
      String passwordHash = PasswordHasher.getSaltedHash(password);

      Assert.assertTrue(PasswordHasher.check(password, passwordHash));
      Assert.assertFalse(PasswordHasher.check("$", passwordHash));
   }

   @Test
   public void hashTestOne()
            throws Exception {
      String password     = "My password is really good!";
      String passwordHash = PasswordHasher.getSaltedHash(password);

      Assert.assertTrue(PasswordHasher.check(password, passwordHash));
      Assert.assertFalse(PasswordHasher.check("not my password", passwordHash));
   }

   @Test
   public void hashTestThree()
            throws Exception {
      String password =
         "µJû5¥¨J«eÜäÅT5¼, BìRß¸jAf½çx.îüöìÍj(Çõïkêpùnðö7¾&Äÿ÷)ÆJgn,GÂá÷+¦òxÂÍ«`¯JXÁ%Ò*ÖtÝ]Ú%U~ÂÅ¿=Ü*º'X·íY(Ù0";
      String passwordHash = PasswordHasher.getSaltedHash(password);

      Assert.assertTrue(PasswordHasher.check(password, passwordHash));
      Assert.assertFalse(PasswordHasher.check("", passwordHash));
   }

   @Test
   public void hashTestTwo()
            throws Exception {
      String password     = "password";
      String passwordHash = PasswordHasher.getSaltedHash(password);

      Assert.assertTrue(PasswordHasher.check(password, passwordHash));
      Assert.assertFalse(PasswordHasher.check("fred", passwordHash));
   }
}

