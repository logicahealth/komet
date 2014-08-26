package org.ihtsdo.otf.tcc.api.store;

import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

/**
 * Ts is short for Terminology store...
 *
 * @author kec
 *
 */

public class Ts {


    private static TerminologyStoreDI store;
   //~--- get methods ---------------------------------------------------------

   public static TerminologyStoreDI get() {
      if (store == null) {
          store = Hk2Looker.get().getService(TerminologyStoreDI.class);
      }
      return store;
   }
}
