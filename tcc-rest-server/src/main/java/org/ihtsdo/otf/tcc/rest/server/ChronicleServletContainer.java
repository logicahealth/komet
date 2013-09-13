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

import java.util.concurrent.Semaphore;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import javax.servlet.ServletException;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

/**
 * Overriding ServletContainer to enable access to
 * <code>init()</code> and
 * <code>destroy()</code> methods.
 *
 * @author kec
 */
public class ChronicleServletContainer extends ServletContainer {

    private static BdbTerminologyStore termStore;
    private static final Semaphore shutdownSemaphore = new Semaphore(1);
    private static final Semaphore startupSemphore = new Semaphore(1);

    static {
        shutdownSemaphore.acquireUninterruptibly();
    }

    public ChronicleServletContainer() {
    }

    public ChronicleServletContainer(ResourceConfig resourceConfig) {
        super(resourceConfig);
    }

    @Override
    public void destroy() {
        System.out.println("Destroy ChronicleServletContainer");
        try {
            if (termStore != null) {
                shutdownSemaphore.acquireUninterruptibly();
                System.out.println("Aquired shutdown permit. ");
                if (termStore != null) {
                    termStore.shutdown();
                    termStore = null;
                }
            }
        } finally {
            startupSemphore.release();
            System.out.println("Released startup permit. ");
        }

        super.destroy();

    }

    @Override
    public void init() throws ServletException {

        Thread bdbStartupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting BdbTerminologyStore for ChronicleServletContainer in background thread. ");
                try {
                    startupSemphore.acquireUninterruptibly();
                    System.out.println("Aquired startup permit. ");
                    BdbTerminologyStore temp = new BdbTerminologyStore();
                    termStore = temp;
                } finally {
                    shutdownSemaphore.release();
                    System.out.println("Released shutdown permit. ");
                }

            }
        }, "Bdb ChronicleServletContainer startup thread");
        bdbStartupThread.start();
        super.init();
    }
}
