package org.ihtsdo.otf.tcc.api.store;

import java.lang.reflect.Method;

/**
 * Ts is short for Terminology store...
 *
 * @author kec
 *
 */
public class Ts {
   public static final String        BERKELEY_DB_FOLDER              = "berkeley-db";
   public static final String        EMBEDDED_BERKELEY_DB_IMPL_CLASS = "org.ihtsdo.otf.tcc.datastore.Bdb";
   public static final String        DEFAULT_LOCAL_HOST_SERVER = "http://localhost:8080/terminology/rest/";
   public static final String        DEFAULT_CLIENT_IMPL_CLASS = "org.ihtsdo.oft.tcc.rest.client.TccRestClient";
   private static Class<?>           implClass;
   private static TerminologyStoreDI store;

   //~--- methods -------------------------------------------------------------

   public static void close() throws Exception {
      Method method = implClass.getMethod("close");

      method.invoke(null);
   }

   public static void close(String storeClassName) throws Exception {
      Class<?> class1 = Class.forName(storeClassName);
      Method   method = class1.getMethod("close");

      method.invoke(null);
   }

   public static void setupEmbedded() throws Exception {
      setup(EMBEDDED_BERKELEY_DB_IMPL_CLASS, BERKELEY_DB_FOLDER);
   }
   public static void setupClient() throws Exception {
      setup(DEFAULT_CLIENT_IMPL_CLASS, DEFAULT_LOCAL_HOST_SERVER);
   }

   public static void setup(String storeClassName, String dbRoot) throws Exception {
      implClass = Class.forName(storeClassName);

      Method method = implClass.getMethod("setup", String.class);

      method.invoke(null, dbRoot);
   }

   //~--- get methods ---------------------------------------------------------

   public static TerminologyStoreDI get() {
      return store;
   }

   public static TerminologySnapshotDI getGlobalSnapshot() {
      return store.getGlobalSnapshot();
   }

   //~--- set methods ---------------------------------------------------------

   public static void set(TerminologyStoreDI store) {
      Ts.store = store;
   }
}
