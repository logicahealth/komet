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

import java.net.URL;
import java.net.URLConnection;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import java.util.Arrays;
import java.util.Base64;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.ArrayUtils;

//~--- classes ----------------------------------------------------------------

/**
 * Utility class for establishing HTTP/HTTPS connections.
 *
 *
 */
public class ConnectionUtils {
   /** The Constant CHARSET. */
   static final String CHARSET;

   //~--- static initializers -------------------------------------------------

   static {
      CHARSET = "UTF-8";

      // Disable Java 7 SNI checks
      // http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0
      System.setProperty("jsse.enableSNIExtension", "false");
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Open connection.
    *
    * @param url the url
    * @param username the username
    * @param password the password
    * @return the URL connection
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static URLConnection openConnection(String url, String username, char[] password)
            throws IOException {
      final URL           urlObject = new URL(url);
      final URLConnection conn      = urlObject.openConnection();

      setAuthorization(conn, username, password);
      conn.setUseCaches(false);
      conn.setDoOutput(true);
      return conn;
   }

   /**
    * Open read connection.
    *
    * @param url the url
    * @param username the username
    * @param password the password
    * @return the URL connection
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static URLConnection openReadConnection(String url, String username, char[] password)
            throws IOException {
      final URLConnection conn = openConnection(url, username, password);

      conn.setRequestProperty("Accept-Charset", ConnectionUtils.CHARSET);
      return conn;
   }

   /**
    * To bytes.
    *
    * @param chars the chars
    * @return the byte[]
    */
   private static byte[] toBytes(char[] chars) {
      final CharBuffer charBuffer = CharBuffer.wrap(chars);
      final ByteBuffer byteBuffer = Charset.forName("UTF-8")
                                           .encode(charBuffer);
      final byte[]     bytes      = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());

      Arrays.fill(charBuffer.array(), '\u0000');  // clear sensitive data
      Arrays.fill(byteBuffer.array(), (byte) 0);  // clear sensitive data
      return bytes;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set authorization.
    *
    * @param conn the conn
    * @param username the username
    * @param password the password
    */
   public static void setAuthorization(URLConnection conn, String username, char[] password) {
      if (!StringUtils.isEmpty(username) && ((password != null) && (password.length > 0))) {
         conn.setRequestProperty("Authorization",
                                 "Basic " +
                                 Base64.getEncoder().encodeToString(toBytes(ArrayUtils.addAll(new String(username +
                                    ":").toCharArray(),
                                       password))));
      }
   }
}

