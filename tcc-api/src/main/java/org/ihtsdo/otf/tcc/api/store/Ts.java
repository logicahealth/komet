package org.ihtsdo.otf.tcc.api.store;

import java.lang.reflect.Method;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * Ts is short for Terminology store...
 *
 * @author kec
 *
 * @deprecated This class will be removed in a future sprint, 
 * replaced by HK2 dependency injection
 */
@Deprecated
public class Ts {
   public static final String        BERKELEY_DB_FOLDER              = "berkeley-db";
   public static final String        EMBEDDED_BERKELEY_DB_IMPL_CLASS = "org.ihtsdo.otf.tcc.datastore.Bdb";
   public static final String        DEFAULT_LOCAL_HOST_SERVER = "http://localhost:8080/terminology/rest/";
   public static final String        DEFAULT_CLIENT_IMPL_CLASS = "org.ihtsdo.oft.tcc.rest.client.TccRestClient";
   private static TerminologyStoreDI store;

   //~--- methods -------------------------------------------------------------

   public static void close() throws Exception {
      store.shutdown();
   }

   @Deprecated
   public static void setupEmbedded() throws Exception {
      setup(EMBEDDED_BERKELEY_DB_IMPL_CLASS, BERKELEY_DB_FOLDER);
   }
   @Deprecated
   public static void setupClient() throws Exception {
      setup(DEFAULT_CLIENT_IMPL_CLASS, DEFAULT_LOCAL_HOST_SERVER);
   }

   @Deprecated
   public static void setup(String storeClassName, String dbRoot) throws Exception {
      
      System.out.println("Ts.setup(String storeClassName, String dbRoot) is deprecated. Use Hk2 lookup.");
      System.setProperty("org.ihtsdo.otf.tcc.datastore.bdb-location", dbRoot);
      store = Hk2Looker.get().getService(TerminologyStoreDI.class);
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
