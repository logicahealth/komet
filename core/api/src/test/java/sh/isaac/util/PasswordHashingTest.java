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

import java.util.Arrays;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Assert;
import org.junit.Test;
import sh.isaac.api.util.PasswordHasher;

//~--- classes ----------------------------------------------------------------

/**
 * {@link PasswordHashingTest}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class PasswordHashingTest {
   /**
    * Encrypt test four.
    *
    * @throws Exception the exception
    */
   @Test
   public void encryptTestFour()
            throws Exception {
      final char[] password  = "".toCharArray();
      final String data      = "some data";
      final String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertTrue(Arrays.equals(PasswordHasher.decrypt(password, encrypted), data.getBytes()));

      try {
         final String decrypted = new String(PasswordHasher.decrypt("wrongPassword".toCharArray(), encrypted));

         Assert.assertFalse(decrypted.equals(data));
         Assert.fail("Expected an exception, but instead got decrypted data: '" + decrypted + "'");
      } catch (final Exception e) {
         // expected
      }
   }

   /**
    * Encrypt test one.
    *
    * @throws Exception the exception
    */
   @Test
   public void encryptTestOne()
            throws Exception {
      final char[] password  = "$sentences_make_better_passwords....".toCharArray();
      final String data      = "There was a man with a plan";
      final String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertTrue(Arrays.equals(PasswordHasher.decrypt(password, encrypted), data.getBytes()));

      try {
          String decrypted = new String(PasswordHasher.decrypt("wrongPassword".toCharArray(), encrypted));

         Assert.assertFalse(decrypted.equals(data));
         Assert.fail("Expected an exception, but instead got decrypted data: '" + decrypted + "'");
      } catch (final Exception e) {
         // expected
      }
   }

   /**
    * Encrypt test three.
    *
    * @throws Exception the exception
    */
   @Test
   public void encryptTestThree()
            throws Exception {
      final char[] password =
         "µJû5¥¨J«eÜäÅT5¼, BìRß¸jAf½çx.îüöìÍj(Çõïkêpùnðö7¾&Äÿ÷)ÆJgn,GÂá÷+¦òxÂÍ«`¯JXÁ%Ò*ÖtÝ]Ú%U~ÂÅ¿=Ü*º'X·íY(Ù0".toCharArray();
      final String data      = "";
      final String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertTrue(Arrays.equals(PasswordHasher.decrypt(password, encrypted), data.getBytes()));

      try {
          String decrypted = new String(PasswordHasher.decrypt("wrongPassword".toCharArray(), encrypted));

         Assert.assertFalse(decrypted.equals(data));
         Assert.fail("Expected an exception, but instead got decrypted data: '" + decrypted + "'");
      } catch (final Exception e) {
         // expected
      }
   }

   /**
    * Encrypt test two.
    *
    * @throws Exception the exception
    */
   @Test
   public void encryptTestTwo()
            throws Exception {
      final char[] password  = "simple".toCharArray();
      final String data      = "There was a man with a plan that wasn't very good";
      final String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertTrue(Arrays.equals(PasswordHasher.decrypt(password, encrypted), data.getBytes()));

      try {
          String decrypted =  new String(PasswordHasher.decrypt("".toCharArray(), encrypted));

         Assert.assertFalse(decrypted.equals(data));
         Assert.fail("Expected an exception, but instead got decrypted data: '" + decrypted + "'");
      } catch (final Exception e) {
         // expected
      }
   }
   
   @Test
   public void encryptTestFive()
            throws Exception {
      final char[] password  = "simple".toCharArray();
      final String data      = "There was a man with a plan that wasn't very good";
      final String encrypted = PasswordHasher.encrypt(password, data);

      Assert.assertEquals(PasswordHasher.decryptToString(password, encrypted), data);

   }
   
   @Test
   public void encryptTestSix()
            throws Exception {
      final char[] password  = "simple".toCharArray();
      final char[] data      = "There was a man with a plan that wasn't very good".toCharArray();
      final String encrypted = PasswordHasher.encrypt(password, data);

      char[] decrypted = PasswordHasher.decryptToChars(password, encrypted);
      
      Assert.assertEquals(decrypted.length, data.length);
      for (int i = 0; i < data.length; i++)
      {
         Assert.assertEquals(decrypted[i], data[i]);
      }
   }

   /**
    * Hash test four.
    *
    * @throws Exception the exception
    */
   @Test
   public void hashTestFour()
            throws Exception {
      final char[] password     = "$sentences_make_better_---passwords....".toCharArray();
      final String passwordHash = PasswordHasher.getSaltedHash(password);

      Assert.assertTrue(PasswordHasher.check(password, passwordHash));
      Assert.assertFalse(PasswordHasher.check("-".toCharArray(), passwordHash));
   }

   /**
    * Hash test one.
    *
    * @throws Exception the exception
    */
   @Test
   public void hashTestOne()
            throws Exception {
      final char[] password     = "My password is really good!".toCharArray();
      final String passwordHash = PasswordHasher.getSaltedHash(password);

      Assert.assertTrue(PasswordHasher.check(password, passwordHash));
      Assert.assertFalse(PasswordHasher.check("not my password".toCharArray(), passwordHash));
   }

   /**
    * Hash test three.
    *
    * @throws Exception the exception
    */
   @Test
   public void hashTestThree()
            throws Exception {
      final char[] password =
         "µJû5¥¨J«eÜäÅT5¼, BìRß¸jAf½çx.îüöìÍj(Çõïkêpùnðö7¾&Äÿ÷)ÆJgn,GÂá÷+¦òxÂÍ«`¯JXÁ%Ò*ÖtÝ]Ú%U~ÂÅ¿=Ü*º'X·íY(Ù0".toCharArray();
      final String passwordHash = PasswordHasher.getSaltedHash(password);

      Assert.assertTrue(PasswordHasher.check(password, passwordHash));
      Assert.assertFalse(PasswordHasher.check("".toCharArray(), passwordHash));
   }

   /**
    * Hash test two.
    *
    * @throws Exception the exception
    */
   @Test
   public void hashTestTwo()
            throws Exception {
      final char[] password     = "password".toCharArray();
      final String passwordHash = PasswordHasher.getSaltedHash(password);

      Assert.assertTrue(PasswordHasher.check(password, passwordHash));
      Assert.assertFalse(PasswordHasher.check("fred".toCharArray(), passwordHash));
   }
}

