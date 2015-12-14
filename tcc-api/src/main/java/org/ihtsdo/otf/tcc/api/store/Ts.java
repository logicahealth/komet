package org.ihtsdo.otf.tcc.api.store;

import gov.vha.isaac.ochre.api.LookupService;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * Ts is short for Terminology termstore...
 *
 * @author kec
 *
 */

public class Ts {


    private static TerminologyStoreDI store;
   //~--- get methods ---------------------------------------------------------

   public static TerminologyStoreDI get() {
      if (store == null) {
          store = LookupService.get().getService(TerminologyStoreDI.class);
      }
      return store;
   }
   
   /**
    * Determine if a TermStore has been constructed - does not construct one if it isn't yet active.
    * There are currently issues with the TermStore implementation that causes it to not operate in a headless
    * environment - this method is needed to not hit those errors in certain cases (like building the metadata on the
    * continuous integration server)
    */
   public static boolean hasActiveTermStore()
   {
       ServiceHandle<TerminologyStoreDI> sh = LookupService.get().getServiceHandle(TerminologyStoreDI.class);
       if (sh == null)
       {
           return false;
       }
       else
       {
           return sh.isActive();
       }
   }
}
