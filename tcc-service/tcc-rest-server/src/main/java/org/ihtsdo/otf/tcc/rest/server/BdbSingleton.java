
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.ws.rs.ext.Provider;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.termstore.PersistentStoreI;

/**
 *
 * @author kec
 */
@Provider
public class BdbSingleton {

    static {
        try {
            String directory = "berkeley-db";


            if (System.getProperty("BdbSingleton.BDB_LOCATION") != null) {
                directory = System.getProperty("BdbSingleton.BDB_LOCATION");
            }
            System.out.println("Initializing BdbSingleton from directory: " + directory);
            if (new File(directory).exists()) {
                Ts.setup(Ts.EMBEDDED_BERKELEY_DB_IMPL_CLASS, directory);
            } else {
                Ts.setup(Ts.EMBEDDED_BERKELEY_DB_IMPL_CLASS, directory);
                System.out.println("Loading new files tp: " + directory);

                File[] econFiles = new File[]{new File("/Users/kec/NetBeansProjects/econ/eConcept.econ"),
                    new File("/Users/kec/NetBeansProjects/econ/DescriptionLogicMetadata.econ")};

                Ts.get().loadEconFiles(econFiles);
                System.out.println("Finished load of: " + Arrays.asList(econFiles));
            }
            Ts.get().setGlobalSnapshot(Ts.get().getSnapshot(StandardViewCoordinates.getSnomedInferredLatest()));
            Ts.get().putViewCoordinate(StandardViewCoordinates.getSnomedInferredThenStatedLatest());
            Ts.get().putViewCoordinate(StandardViewCoordinates.getSnomedStatedLatest());
        } catch (Throwable ex) {
            Logger.getLogger(BdbSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static PersistentStoreI get() {
        return P.s;
    }

    public static void close() {
        singleton.closeBdb();
    }
    private static final BdbSingleton singleton = new BdbSingleton();
    //~--- methods -------------------------------------------------------------

    @PreDestroy
    public void closeBdb() {
        try {
            Ts.close(Ts.EMBEDDED_BERKELEY_DB_IMPL_CLASS);
        } catch (Exception ex) {
            Logger.getLogger(BdbSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
