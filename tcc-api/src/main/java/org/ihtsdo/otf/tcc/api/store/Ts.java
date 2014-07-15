package org.ihtsdo.otf.tcc.api.store;

import java.lang.reflect.Method;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

import javax.inject.Inject;

/**
 * Ts is short for Terminology store...
 *
 * @author kec
 *
 */

public class Ts {

    private static Ts singleton;

    @Inject private TerminologyStoreDI store;

    private Ts() {};

   //~--- methods -------------------------------------------------------------

   public static void close() throws Exception {
      get().shutdown();
   }

   //~--- get methods ---------------------------------------------------------

   public static TerminologyStoreDI get() {
      if (singleton == null) {
          singleton = new Ts();
      }
      return singleton.store;
   }

   public static TerminologySnapshotDI getGlobalSnapshot() {
       return get().getGlobalSnapshot();
   }
}
