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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

//~--- non-JDK imports --------------------------------------------------------

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import sh.isaac.provider.sync.git.gitblit.GitBlitException.ForbiddenException;
import sh.isaac.provider.sync.git.gitblit.GitBlitException.NotAllowedException;
import sh.isaac.provider.sync.git.gitblit.GitBlitException.UnauthorizedException;
import sh.isaac.provider.sync.git.gitblit.GitBlitException.UnknownRequestException;

//~--- classes ----------------------------------------------------------------

/**
 * Utility methods for json calls to a Gitblit server.
 *
 * @author James Moger
 *
 */
public class JsonUtils {
   /**
    * Reads a gson object from the specified url.
    *
    * @param url
    * @param type
    * @param username
    * @param password
    * @return the deserialized object
    * @throws {@link IOException}
    */
   @SuppressWarnings("unchecked")
   public static JsonObject<String, Map<String, ?>> retrieveJson(String url,
         String username,
         char[] password)
            throws IOException {
      String json = retrieveJsonString(url, username, password);

      if (StringUtils.isEmpty(json)) {
         return null;
      }

      DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

      dateformat.setTimeZone(TimeZone.getTimeZone("UTC"));

      HashMap<String, Object> config = new HashMap<>();

      config.put(JsonWriter.DATE_FORMAT, dateformat);
      return (JsonObject<String, Map<String, ?>>) JsonReader.jsonToJava(json, config);
   }

   /**
    * Retrieves a JSON message.
    *
    * @param url
    * @return the JSON message as a string
    * @throws {@link IOException}
    */
   public static String retrieveJsonString(String url, String username, char[] password)
            throws IOException {
      try {
         URLConnection conn = ConnectionUtils.openReadConnection(url, username, password);
         StringBuilder json = new StringBuilder();

         try (InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, ConnectionUtils.CHARSET));) {
            char[] buffer = new char[4096];
            int    len    = 0;

            while ((len = reader.read(buffer)) > -1) {
               json.append(buffer, 0, len);
            }
         }

         return json.toString();
      } catch (IOException e) {
         if (e.getMessage()
              .indexOf("401") > -1) {
            // unauthorized
            throw new UnauthorizedException(url);
         } else if (e.getMessage()
                     .indexOf("403") > -1) {
            // requested url is forbidden by the requesting user
            throw new ForbiddenException(url);
         } else if (e.getMessage()
                     .indexOf("405") > -1) {
            // requested url is not allowed by the server
            throw new NotAllowedException(url);
         } else if (e.getMessage()
                     .indexOf("501") > -1) {
            // requested url is not recognized by the server
            throw new UnknownRequestException(url);
         }

         throw e;
      }
   }

   /**
    * Sends a JSON message.
    *
    * @param url
    * the url to write to
    * @param json
    * the json message to send
    * @param username
    * @param password
    * @return the http request result code
    * @throws {@link IOException}
    */
   public static int sendJsonString(String url, String json, String username, char[] password)
            throws IOException {
      try {
         byte[]        jsonBytes = json.getBytes(ConnectionUtils.CHARSET);
         URLConnection conn      = ConnectionUtils.openConnection(url, username, password);

         conn.setRequestProperty("Content-Type", "text/plain;charset=" + ConnectionUtils.CHARSET);
         conn.setRequestProperty("Content-Length", "" + jsonBytes.length);

         // write json body
         try (OutputStream os = conn.getOutputStream();) {
            os.write(jsonBytes);
         }

         int status = ((HttpURLConnection) conn).getResponseCode();

         return status;
      } catch (IOException e) {
         if (e.getMessage()
              .indexOf("401") > -1) {
            // unauthorized
            throw new UnauthorizedException(url);
         } else if (e.getMessage()
                     .indexOf("403") > -1) {
            // requested url is forbidden by the requesting user
            throw new ForbiddenException(url);
         } else if (e.getMessage()
                     .indexOf("405") > -1) {
            // requested url is not allowed by the server
            throw new NotAllowedException(url);
         } else if (e.getMessage()
                     .indexOf("501") > -1) {
            // requested url is not recognized by the server
            throw new UnknownRequestException(url);
         }

         throw e;
      }
   }

   /**
    * Creates JSON from the specified object.
    *
    * @param o
    * @return json
    */
   public static String toJsonString(Object o) {
      DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

      dateformat.setTimeZone(TimeZone.getTimeZone("UTC"));

      HashMap<String, Object> config = new HashMap<>();

      config.put(JsonWriter.DATE_FORMAT, dateformat);
      config.put(JsonWriter.SKIP_NULL_FIELDS, true);
      config.put(JsonWriter.TYPE, false);
      config.put(JsonWriter.ENUM_PUBLIC_ONLY, true);
      return JsonWriter.objectToJson(o, config);
   }
}

