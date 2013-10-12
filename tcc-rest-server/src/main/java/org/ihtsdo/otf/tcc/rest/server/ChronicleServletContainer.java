/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.otf.tcc.rest.server;

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

import javax.servlet.ServletException;

/**
 * Overriding ServletContainer to enable access to
 * <code>init()</code> and
 * <code>destroy()</code> methods.
 *
 * @author kec
 */
public class ChronicleServletContainer extends ServletContainer {
    private static final Semaphore     storeSemaphore = new Semaphore(1);
    private static BdbTerminologyStore termStore;

    @Override
    public ServletContext getServletContext() {
        return super.getServletContext(); //To change body of generated methods, choose Tools | Templates.
    }

    public ChronicleServletContainer() {}

    public ChronicleServletContainer(ResourceConfig resourceConfig) {
        super(resourceConfig);
    }

    @Override
    public void destroy() {
        System.out.println("Destroy ChronicleServletContainer");

        try {
            storeSemaphore.acquireUninterruptibly();
            System.out.println("Aquired storeSemaphore for destroy. ");

            if (termStore != null) {
                termStore.shutdown();
                termStore = null;
            }
        } finally {
            storeSemaphore.release();
            System.out.println("Released storeSemaphore for destroy. ");
        }

        super.destroy();
    }

    @Override
    public void init() throws ServletException {
        Thread bdbStartupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting BdbTerminologyStore for "
                        + "ChronicleServletContainer in background thread. ");

                //Get the updated resources
                
                SetupServerDependencies setup = new SetupServerDependencies(getServletContext());
                
                setup.run(new String[]{"install"});
                
                try {
                    storeSemaphore.acquireUninterruptibly();
                    System.out.println("Aquired storeSemaphore for init. ");

                    BdbTerminologyStore temp = new BdbTerminologyStore();

                    termStore = temp;
                } finally {
                    storeSemaphore.release();
                    System.out.println("Released storeSemaphore for init. ");
                }
            }
        }, "Bdb ChronicleServletContainer startup thread");

        bdbStartupThread.start();
        super.init();
    }
}
