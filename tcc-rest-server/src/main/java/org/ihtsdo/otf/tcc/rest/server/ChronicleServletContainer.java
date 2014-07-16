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
import java.io.File;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.servlet.ServletContainer;

import org.ihtsdo.otf.tcc.api.time.TimeHelper;


//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Overriding ServletContainer to enable access to <code>init()</code> and
 * <code>destroy()</code> methods.
 *
 * @author kec
 */
public class ChronicleServletContainer extends ServletContainer {

    private static final AtomicInteger threadCount = new AtomicInteger(1);
    //private static BdbTerminologyStore termStore;
    public static SetupStatus status;
    public static Integer maxHeaderSize;
    boolean success = false;
    int tryCount = 0;
    int dbSetupCount = 0;
    private static final int DB_SETUP_TRIES = 10;
    private Thread setupThread;
    boolean deleteBdb = false;

    @Override
    public ServletContext getServletContext() {
        return super.getServletContext();
    }

    @Override
    public void destroy() {
        try {
            getServletContext().log("Destroy ChronicleServletContainer");

            if (setupThread != null) {
                setupThread.interrupt();
                // I know this is bad form, but the maven CLI does not provide a 
                // stop method, and without a stop method, a download could last for
                // a very long time. 
                setupThread.stop();
            }

//            if (termStore != null) {
//                getServletContext().log("termStore is not null, shutting down. ");
//                termStore.shutdown();
//                getServletContext().log("termStore shutdown. ");
//                termStore = null;
//            } else {
//                getServletContext().log("termStore is null. ");
//            }

            status = SetupStatus.CLOSING_DB;
            super.destroy();
        } catch (Throwable e) {
            getServletContext().log("Destroy servlet context exception.", e);
            throw e;
        }
    }

    @Override
    public void init() throws ServletException {
        if (this.getServletConfig().getInitParameter("httpMaxHeaderSize") != null) {
            maxHeaderSize = Integer.parseInt(this.getServletConfig().getInitParameter("httpMaxHeaderSize"));
        } else {
            maxHeaderSize = 900;
        }

        setupThread = new Thread("ChronicleServletContainer Setup thread" + threadCount.getAndIncrement()) {
            @Override
            public void run() {
                SetupDatabase setup = new SetupDatabase();

                try {
                    setup.call();
                } catch (Exception ex) {
                    Logger.getLogger(ChronicleServletContainer.class.getName()).log(Level.SEVERE, null, ex);
                    outputFileSizes();
                }

                super.run();
            }
        };
        setupThread.setDaemon(true);
        setupThread.start();
        super.init();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SetupStatus localStatus = status;

        getServletContext().log("Line added to ensure resources are updated properly - V2.");

        if (localStatus == SetupStatus.DB_OPEN) {
            // If there's an error in db setup, then repeat as necessary
//            while ((termStore == null) && (dbSetupCount < DB_SETUP_TRIES)) {
//                getServletContext().log("Termstore is null");
//                getServletContext().log("Database status: " + status.toString());
//                status = SetupStatus.DB_OPEN_FAILED;
//                init();
//                dbSetupCount++;
//            }
//
//            // If the repetition is unsuccessful, then delete the database and try again
//            if ((termStore == null) && (dbSetupCount >= DB_SETUP_TRIES)) {
//                deleteBdb = true;
//                init();
//            }
//
//            try {
//                super.service(request, response);
//            } catch (NullPointerException e) {
//                getServletContext().log("Database status: " + status.toString());
//                getServletContext().log("Database load error.", e);
//                if (termStore == null) {
//                    getServletContext().log("Termstore is null and query was unsuccessful.");
//                } else {
//                    outputFileSizes();
//                }
//
//            }
        } else {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Try: " + tryCount + " " + localStatus.toString());
        }
    }

    @Override
    public Value<Integer> service(URI baseUri, URI requestUri, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SetupStatus localStatus = status;

        if (status == SetupStatus.DB_OPEN) {
            if (requestUri.toURL().toString().length() > maxHeaderSize) {
                response.sendError(HttpServletResponse.SC_REQUEST_URI_TOO_LONG, "Query is too long.");
            }

            return super.service(baseUri, requestUri, request, response);
        }

        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Try: " + tryCount + " " + localStatus.toString());

        return Values.lazy(new Value<Integer>() {
            @Override
            public Integer get() {
                return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            }
        });
    }

    public void outputFileSizes() {

        StringBuilder bi = new StringBuilder("");
        
        String s = this.getServletContext().getRealPath("mvn-repo");

        bi.append("The dir to maven repo: ").append(s).append("\n");
//        if (termStore != null) {
//
//            File bdb = new File(termStore.getBdbLocation());
//
//            getServletContext().log("The bdb path is: " + bdb.getAbsolutePath());
//
//            if (bdb.exists()) {
//                long i = getFolderSize(bdb);
//                bi.append("The bdb size is : ").append(i).append(" bytes.\n");
//            } else {
//                bi.append("File does not exist!\n");
//            }
//
//            File m2 = new File(bdb.getAbsolutePath() + "/../../mvn-repo");
//
//            bi.append("Maven repo path: ").append(m2.getAbsolutePath()).append("\n");
//
//            if (m2.exists()) {
//                long j = getFolderSize(m2);
//                bi.append("Maven repo size: ").append(j).append(" bytes.\n");
//                for (File f : listFiles(m2.getAbsolutePath())) {
//                    bi.append("File: ").append(f.getAbsolutePath()).append(" Size: ").append(f.length()).append("\n");
//                }
//            } else {
//                bi.append("Maven repo doesn't exist.");
//            }
//        } else {
//            bi.append("Termstore is null.");
//        }
        
        getServletContext().log(bi.toString());
    }

    public static long getFolderSize(final File... selectedDirectories) {
        long foldersize = 0;
        for (final File item : selectedDirectories) {
            for (final File subItem : item.listFiles()) {
                if (subItem.isDirectory()) {
                    foldersize += getFolderSize(subItem);
                } else {
                    foldersize += subItem.length();
                }
            }
        }
        return foldersize;
    }

    public static List<File> listFiles(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<File>();

        // get all the files from a directory
        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
                System.out.println(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                resultList.addAll(listFiles(file.getAbsolutePath()));
            }
        }
        return resultList;
    }

    private class SetupDatabase implements Callable<Void> {

        private static final int MAX_TRIES_BEFORE_FAILURE = 10;

        @Override
        public Void call() throws Exception {
            long startTime = System.currentTimeMillis();

            getServletContext().log("Starting database setup at: " + TimeHelper.formatDate(startTime));

            try {
                getServletContext().log("Starting BdbTerminologyStore for "
                        + "ChronicleServletContainer in background thread. ");

                status = SetupStatus.BUILDING;

                SetupServerDependencies setup = new SetupServerDependencies(getServletContext());

                // If needed, update resources
                if (deleteBdb) {
                    setup.deleteAppDir();
                    deleteBdb = false;
                }

                while ((success == false) && (tryCount < MAX_TRIES_BEFORE_FAILURE)) {
                    tryCount++;
                    getServletContext().log("Setup try: " + tryCount);
                    success = setup.execute();
                    if (!success) {
                        getServletContext().log("Setup try  " + tryCount + " failed.");
                        //setup.deleteAppDir();
                    }
                }

                if (success) {
                    long elapsedTime = System.currentTimeMillis() - startTime;

                    getServletContext().log("Finised database dependency setup: "
                            + TimeHelper.getElapsedTimeString(elapsedTime));
                    status = SetupStatus.OPENING_DB;

//                    BdbTerminologyStore temp = new BdbTerminologyStore();
//
//                    termStore = temp;
                    status = SetupStatus.DB_OPEN;
                } else {
                    status = SetupStatus.DB_OPEN_FAILED;
                }

                long elapsedTime = System.currentTimeMillis() - startTime;

                if (success) {
                    getServletContext().log("Finised database startup: "
                            + TimeHelper.getElapsedTimeString(elapsedTime));
                } else {
                    getServletContext().log("FAILED database startup: " + TimeHelper.getElapsedTimeString(elapsedTime));
                }

                return null;
            } catch (IOException ex) {
                status = SetupStatus.DB_OPEN_FAILED;

                throw ex;
            }
        }
    }
}
