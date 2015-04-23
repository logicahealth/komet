/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api;

import gov.va.oia.HK2Utilities.HK2RuntimeInitializer;
import gov.vha.isaac.ochre.api.constants.Constants;
import gov.vha.isaac.ochre.util.HeadlessToolkit;
import java.awt.GraphicsEnvironment;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import com.sun.javafx.application.PlatformImpl;

/**
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("restriction")
public class LookupService {
    private static final Logger log = LogManager.getLogger();
    private static volatile ServiceLocator looker = null;
    public static final int ISAAC_STARTED_RUNLEVEL = 4;
    public static final int ISAAC_STOPPED_RUNLEVEL = -1;
    private static final Object lock = new Object();

    /**
     * @return the {@link ServiceLocator} that is managing this ISAAC instance
     */
    public static ServiceLocator get() {
        if (looker == null) {
            synchronized (lock) {
                if (looker == null) {
                    if (GraphicsEnvironment.isHeadless()) {
                        log.info("Installing headless toolkit");
                        HeadlessToolkit.installToolkit();
                    }

                    PlatformImpl.startup(() -> {
                        // No need to do anything here
                        });
                    
                    ArrayList<String> packagesToSearch = new ArrayList<String>(Arrays.asList("gov.va", "gov.vha", "org.ihtsdo", "org.glassfish"));

                    boolean readInhabitantFiles = Boolean.getBoolean(System.getProperty(Constants.READ_INHABITANT_FILES, "false"));
                    if (System.getProperty(Constants.EXTRA_PACKAGES_TO_SEARCH) != null) {
                        String[] extraPackagesToSearch = System.getProperty(Constants.EXTRA_PACKAGES_TO_SEARCH).split(";");
                        for (String packageToSearch: extraPackagesToSearch) {
                            packagesToSearch.add(packageToSearch);
                        }
                    }
                    try {
                        String[] packages = packagesToSearch.toArray(new String[]{});
                        log.info("Looking for HK2 annotations " + (readInhabitantFiles ? "from inhabitant files" : "skipping inhabitant files") 
                                + "; and scanning in the packages: " + Arrays.toString(packages));
                        looker = HK2RuntimeInitializer.init("ISAAC", readInhabitantFiles, packages);
                        log.info("HK2 initialized.  Identifed " + looker.getAllServiceHandles((criteria) -> {return true;}).size() + " services");
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return looker;
    }

    /**
     * Return the highest ranked service that implements the requested contract or implementation class.
     * @see ServiceLocator#getService(Class, Annotation...) 
     */
    public static <T> T getService(Class<T> contractOrImpl) {
        return get().getService(contractOrImpl, new Annotation[0]);
    }
    
    /**
     * Find the best ranked service with the specified name.  If no service with the specified name is available, 
     * this returns null (even if there is a service with another name [or no name] which would meet the contract)
     * 
     * @see ServiceLocator#getService(Class, String, Annotation...)
     * 
     * @param contractOrService May not be null, and is the contract or concrete implementation to get the best instance of
     * @param name May not be null or empty
     */
    public static <T> T getService(Class<T> contractOrService, String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("You must specify a service name to use this method");
        }
        return get().getService(contractOrService, name, new Annotation[0]);
    }
    
    /**
     * Find a service by name, and automatically fall back to any service which implements the contract if the named service was not available.
     * 
     * @param contractOrService May not be null, and is the contract or concrete implementation to get the best instance of
     * @param name May be null (to indicate any name is ok), and is the name of the implementation to be returned
     */
    public static <T> T getNamedServiceIfPossible(Class<T> contractOrService, String name) {
        if (StringUtils.isEmpty(name)) {
            return get().getService(contractOrService);
        }
        else {
            T service = get().getService(contractOrService, name);
            if (service == null) {
                service = get().getService(contractOrService);
            }
            return service;
        }
    }

    public static RunLevelController getRunLevelController() {
        return getService(RunLevelController.class);
    }
    
    /**
     * Start all core isaac services, blocking until started (or failed)
     */
    public static void startupIsaac() {
        getRunLevelController().proceedTo(ISAAC_STARTED_RUNLEVEL);
    }
    
    /**
     * Stop all core isaac service, blocking until stopped (or failed)
     */
    public static void shutdownIsaac() {
        getRunLevelController().proceedTo(ISAAC_STOPPED_RUNLEVEL);
    }
    
    /**
     * start all core isaac services in a background thread, returning immediately.  
     * @param callWhenStartComplete (optional) - if provided,  a call back will be provided
     * notifying of successfully start of ISAAC, or providing the Exception, if the startup sequence failed.
     */
    public static void startupIsaac(BiConsumer<Boolean, Exception> callWhenStartComplete) {
        log.info("Background starting ISAAC services");
        Thread backgroundLoad = new Thread(() ->
        {
            try {
                startupIsaac();
                log.info("Background start complete - runlevel now " + getRunLevelController().getCurrentRunLevel());
                if (callWhenStartComplete != null) {
                    callWhenStartComplete.accept(isIssacStarted(), null);
                }
            }
            catch (Exception e) {
                log.warn("Background start failed - runlevel now " + getRunLevelController().getCurrentRunLevel(), e);
                if (callWhenStartComplete != null) {
                    callWhenStartComplete.accept(false, e);
                }
            }
        }, "Datastore init thread");
        backgroundLoad.start();
    }
    
    public static boolean isIssacStarted() {
        return getRunLevelController().getCurrentRunLevel() == ISAAC_STARTED_RUNLEVEL;
    }
}
