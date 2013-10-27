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

import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.servlet.ServletContainer;

import org.ihtsdo.otf.tcc.api.thread.NamedThreadFactory;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URI;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Overriding ServletContainer to enable access to
 * <code>init()</code> and
 * <code>destroy()</code> methods.
 *
 * @author kec
 */
public class ChronicleServletContainer extends ServletContainer {
    private static final Semaphore       storeSemaphore            = new Semaphore(1);
    private static final ThreadGroup     chroncileServletContainer =
        new ThreadGroup("ChronicleServletContainer threads");
    private static final ExecutorService setupExecutor             =
        Executors.newCachedThreadPool(new NamedThreadFactory(chroncileServletContainer, "parallel iterator service"));
    private static BdbTerminologyStore termStore;
    public static SetupStatus          status;
    public static Future<Void>         setupFuture;

    @Override
    public ServletContext getServletContext() {
        return super.getServletContext();
    }

    @Override
    public void destroy() {
        getServletContext().log("Destroy ChronicleServletContainer");

        if (setupFuture != null) {
            setupFuture.cancel(true);
        }

        try {
            storeSemaphore.acquireUninterruptibly();
            getServletContext().log("Aquired storeSemaphore for destroy. ");

            if (termStore != null) {
                termStore.shutdown();
                termStore = null;
            }
        } finally {
            storeSemaphore.release();
            getServletContext().log("Released storeSemaphore for destroy. ");
        }

        status = SetupStatus.CLOSING_DB;
        getServletContext().setAttribute("status", status);
        super.destroy();
    }

    @Override
    public void init() throws ServletException {
        SetupDatabase setup = new SetupDatabase();

        setupFuture = setupExecutor.submit(setup);
        super.init();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SetupStatus localStatus = status;

        if (localStatus == SetupStatus.DB_OPEN) {
            super.service(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, localStatus.toString());  
        }

    }

    @Override
    public Value<Integer> service(URI baseUri, URI requestUri, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SetupStatus localStatus = status;

        if (status == SetupStatus.DB_OPEN) {
            return super.service(baseUri, requestUri, request, response);
        }

        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, localStatus.toString());

        return Values.lazy(new Value<Integer>() {
            @Override
            public Integer get() {
                return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            }
        });
    }

    private class SetupDatabase implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            long startTime = System.currentTimeMillis();

            getServletContext().log("Starting database setup at: " + TimeHelper.formatDate(startTime));

            try {
                storeSemaphore.acquireUninterruptibly();
                getServletContext().log("Aquired storeSemaphore for init. ");

                try {
                    getServletContext().log("Starting BdbTerminologyStore for "
                                            + "ChronicleServletContainer in background thread. ");

                    // Get the updated resources
                    status = SetupStatus.BUILDING;
                    getServletContext().setAttribute("status", status);

                    SetupServerDependencies setup = new SetupServerDependencies(getServletContext());

                    setup.run(null);

                    long elapsedTime = System.currentTimeMillis() - startTime;

                    getServletContext().log("Finised database dependency setup: "
                                            + TimeHelper.getElapsedTimeString(elapsedTime));
                    status = SetupStatus.OPENING_DB;
                    getServletContext().setAttribute("status", status);

                    BdbTerminologyStore temp = new BdbTerminologyStore();

                    termStore = temp;
                    status = SetupStatus.DB_OPEN;
                } finally {
                    storeSemaphore.release();
                    getServletContext().log("Released storeSemaphore for init. ");
                    getServletContext().setAttribute("status", status);
                }

                long elapsedTime = System.currentTimeMillis() - startTime;

                getServletContext().log("Finised database startup: " + TimeHelper.getElapsedTimeString(elapsedTime));

                return null;
            } catch (IOException ex) {
                status = SetupStatus.DB_OPEN_FAILED;

                throw ex;
            }
        }
    }
}
