/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */

package sh.isaac.api;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import net.sagebits.HK2Utilities.HK2RuntimeInitializer;
import sh.isaac.api.DatastoreServices.DataStoreStartState;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.progress.Stoppable;
import sh.isaac.api.util.HeadlessToolkit;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LookupService.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("restriction")
public class LookupService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The looker. */
   private static volatile ServiceLocator looker = null;

   /** The fx platform up. */
   private static volatile boolean fxPlatformUp = false;

   public static final int SL_L6_ISAAC_DEPENDENTS_RUNLEVEL = 6;  //Anything that depends on issac as a whole to be started should be 6 - 
   //this is the fully-started state.

   public static final int SL_L5_ISAAC_STARTED_RUNLEVEL = 5; //at level 4 and 5, secondary isaac services start, such as changeset providers, etc.
   
   public static final int SL_L4 = 4; 
   
   public static final int SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL = 3;  //In general, ISAAC data-store services start between 1 and 3, in an 
   //isaac specific order.

   public static final int SL_L2 = 2; 
   
   public static final int SL_L1 = 1; 

   //Below 0, we have utility stuff... no ISAAC services.

   public static final int SL_L0_METADATA_STORE_STARTED_RUNLEVEL = 0;

   public static final int SL_NEG_1_WORKERS_STARTED_RUNLEVEL = -1;

   //Do not use runlevels of -2 or below.  -2 is where the HK2 RunLevel controller starts, trying to do anything with services at this level
   //results in bad behavior
   private static final int SL_NEG_2_SYSTEM_STOPPED_RUNLEVEL = -2;

   /** The Constant STARTUP_LOCK. */
   private static final Object STARTUP_LOCK = new Object();
   
   private static Map<Stoppable, Integer> jobsToStop = Collections.synchronizedMap(new WeakHashMap<>());

   //~--- methods -------------------------------------------------------------

   /**
    * Stop all core isaac service, blocking until stopped (or failed).
    * Goes down to {@link #SL_NEG_1_WORKERS_STARTED_RUNLEVEL}
    */
   public static void shutdownIsaac() {
      LOG.info("Shutdown Isaac called");
      if (isInitialized()) {
         Get.applicationStates().add(ApplicationStates.STOPPING);
         Get.applicationStates().remove(ApplicationStates.RUNNING);
         setRunLevel(SL_NEG_1_WORKERS_STARTED_RUNLEVEL);

         // Fully release any system locks to database
         System.gc();
      }
      LOG.info("Shutdown Isaac completed");
   }

   /**
    * Stop all system services, blocking until stopped (or failed).  Note, it is very likely
    * you will need to call {@link Platform#exit()} after this, if you are truly exiting the JVM.
    * But, be careful, if you are in a webserver environment....
    */
   public static void shutdownSystem() {
      LOG.info("Shutdown system called");
      if (isInitialized()) {
         Get.applicationStates().add(ApplicationStates.STOPPING);
         Get.applicationStates().remove(ApplicationStates.RUNNING);
         setRunLevel(SL_NEG_2_SYSTEM_STOPPED_RUNLEVEL);
         looker.shutdown();
         ServiceLocatorFactory.getInstance()
                              .destroy(looker);
         looker = null;
         
         // Fully release any system locks to database
         System.gc();
      }
      LOG.info("Shutdown system completed");
   }

   /**
    * This is automatically done when a typical ISAAC pattern is utilized.  This method is only exposed as public for obscure use cases where
    * we want to utilize the javaFX task API, but are not looking to start up HK2 and other various ISAAC services.
    */
   public static void startupFxPlatform() {
      if (!fxPlatformUp) {
         LOG.debug("FxPlatform is not yet up - obtaining lock");
      Get.applicationStates().add(ApplicationStates.STARTING);

         synchronized (STARTUP_LOCK) {
            LOG.debug("Lock obtained, starting fxPlatform");

            if (!fxPlatformUp) {
               System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

               if (GraphicsEnvironment.isHeadless()) {
                  LOG.info("Installing headless toolkit");
                  HeadlessToolkit.installToolkit();
               }

               LOG.debug("Starting JavaFX Platform");
               PlatformImpl.startup(() -> {
                  // No need to do anything here
               });
               fxPlatformUp = true;
            }
         }
      Get.applicationStates().add(ApplicationStates.RUNNING);
      Get.applicationStates().remove(ApplicationStates.STARTING);
      }
   }
   
   /**
    * Bring the system up to runlevel {@link #SL_L0_METADATA_STORE_STARTED_RUNLEVEL}
    */
   public static void startupPreferenceProvider() {
      if (getService(RunLevelController.class).getCurrentRunLevel() < SL_L0_METADATA_STORE_STARTED_RUNLEVEL) {
         Get.applicationStates().add(ApplicationStates.STARTING);
         setRunLevel(SL_L0_METADATA_STORE_STARTED_RUNLEVEL);
      }
   }

   /**
    * Start all core isaac services, blocking until started (or failed).
    */
   public static void startupIsaac() {
      try {
         // So Fortify does not complain about Locale dependent comparison
         // when the application uses .equals or
         Locale.setDefault(Locale.US);

         Get.applicationStates().add(ApplicationStates.STARTING);
         // Set run level to startup database and associated services running on top of database

         LOG.info("Bringing up Isaac data stores...");
         // Set run level to startup database and associated services running on top of database
         if (getService(RunLevelController.class).getCurrentRunLevel() < SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL) {
            setRunLevel(SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL);
         }
         else {
            LOG.warn("Asked to startup isaac when it was already running?");
         }

         validateDatabaseFolderStatus();

         // If database is validated, startup remaining run levels

         LOG.info("Bringing up the rest of isaac...");
         if (getService(RunLevelController.class).getCurrentRunLevel() < SL_L5_ISAAC_STARTED_RUNLEVEL) {
            setRunLevel(SL_L5_ISAAC_STARTED_RUNLEVEL);
         }
         else {
            LOG.warn("Asked to startup isaac when it was already running?");
         }
         
         LOG.info("Bringing up isaac dependents...");
         if (getService(RunLevelController.class).getCurrentRunLevel() < SL_L6_ISAAC_DEPENDENTS_RUNLEVEL) {
            setRunLevel(SL_L6_ISAAC_DEPENDENTS_RUNLEVEL);
         }
         else {
            LOG.warn("Asked to startup isaac when it was already running?");
         }
         
         //Make sure metadata is imported, if the user prefs said to import metadata.
         get().getService(MetadataService.class).importMetadata();
         
         //Now, check and make sure every provider has the same DB ID
         UUID expected = get().getService(MetadataService.class).getDataStoreId().get();
         
         get().getAllServiceHandles(DatastoreServices.class).forEach(handle -> {
            if (handle.isActive()) {
                  if (!expected.equals(handle.getService().getDataStoreId().orElse(null))) {
                     throw new RuntimeException("Inconsistent Data Store state!  Provider " + handle.getActiveDescriptor().getImplementation()  
                           + " has an id of " + handle.getService().getDataStoreId() 
                           + ".  Expected " + expected);
                  }
               }
            });
         

         Get.applicationStates().add(ApplicationStates.RUNNING);
         Get.applicationStates().remove(ApplicationStates.STARTING);

      } catch (final Throwable e) {
         LOG.error("Error starting isaac", e);
         // Will inform calling routines that database is corrupt
         throw new RuntimeException (e);
      } 
   }

   /**
    * start all core isaac services in a background thread, returning immediately.
    * @param callWhenStartComplete (optional) - if provided,  a call back will be provided
    * notifying of successfully start of ISAAC, or providing the Exception, if the startup sequence failed.
    */
   public static void startupIsaac(BiConsumer<Boolean, Exception> callWhenStartComplete) {
      LOG.info("Background starting ISAAC services");
      Get.applicationStates().add(ApplicationStates.STARTING);

      final Thread backgroundLoad = new Thread(() -> {
               try {
                  startupIsaac();
                  LOG.info("Background start complete - runlevel now " +
                           getService(RunLevelController.class).getCurrentRunLevel());

                  if (callWhenStartComplete != null) {
                     callWhenStartComplete.accept(isIsaacStarted(), null);
                  }
               } catch (final Exception e) {
                  LOG.warn("Background start failed - runlevel now " +
                           getService(RunLevelController.class).getCurrentRunLevel(),
                           e);

                  if (callWhenStartComplete != null) {
                     callWhenStartComplete.accept(false, e);
                  }
               }
            },
                                               "Datastore init thread");

      backgroundLoad.start();
   }

   /**
    * Start the Metadata services (without starting ISAAC core services), blocking until started (or failed).
    */
   public static void startupMetadataStore() {
      if (getService(RunLevelController.class).getCurrentRunLevel() < SL_L0_METADATA_STORE_STARTED_RUNLEVEL) {
         setRunLevel(SL_L0_METADATA_STORE_STARTED_RUNLEVEL);
      }
   }

   /**
    * Start the WorkExecutor services (without starting ISAAC core services), blocking until started (or failed).
    */
   public static void startupWorkExecutors() {
      if (getService(RunLevelController.class).getCurrentRunLevel() < SL_NEG_1_WORKERS_STARTED_RUNLEVEL) {
         setRunLevel(SL_NEG_1_WORKERS_STARTED_RUNLEVEL);
      }
   }


   /**
    * Check datastore provider start states.  In general, all providers should have had a consistent state - Inconsistent state 
    * suggests database corruption.  
    * 
    * The only exception to this, is the indexers, which are allowed to start in a {@link DataStoreStartState#NO_DATASTORE} even while 
    * the other providers are in {@link DataStoreStartState#EXISTING_DATASTORE} because they will build themselves as necessary.
    * 
    *  Also validate that all DBIDs (if present) are identical across the data stores.
    */
   private static void validateDatabaseFolderStatus() {
      //Read initial values from the Identifier service, which should be happy...  Reading the IdentifierService, since it happened
      //to be implemented by the BDB datastore, which is a level 0 (earliest started) service.
      final DataStoreStartState discoveredValidityValue = get().getService(IdentifierService.class).getDataStoreStartState();
      
      LOG.info("System starting with datastore in {} state, with an ID of {}", discoveredValidityValue, get().getService(IdentifierService.class).getDataStoreId());
      if (discoveredValidityValue == DataStoreStartState.NOT_YET_CHECKED)
      {
         throw new RuntimeException("validateDatabaseFolderStatus should not be called prior to starting all DataStore services");
      }

      get().getAllServiceHandles(DatastoreServices.class).forEach(handle -> {
         if (handle.isActive()) {
               if (discoveredValidityValue != handle.getService().getDataStoreStartState() && 
                     !(handle.getService() instanceof IndexQueryService)) {
                  throw new RuntimeException("Inconsistent Data Store state!  Provider " + handle.getActiveDescriptor().getImplementation()  
                        + " has start state of " + handle.getService().getDataStoreStartState() 
                        + ".  Expected " + discoveredValidityValue);
               }
            }
         });
      }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the current run level.
    *
    * @return the current run level
    */
   public static int getCurrentRunLevel() {
      return getService(RunLevelController.class).getCurrentRunLevel();
   }
   
   /**
    * @return the run level we are working toward, or, if we are not working toward any run level, returns the current run level.
    */
   public static int getProceedingToRunLevel() {
      RunLevelFuture rlf = getService(RunLevelController.class).getCurrentProceeding();
      if (rlf != null) {
         return rlf.getProposedLevel();
      }
      return getCurrentRunLevel();
   }

   /**
    * Gets the.
    *
    * @return the {@link ServiceLocator} that is managing this ISAAC instance
    */
   public static ServiceLocator get() {
      if (looker == null) {
         synchronized (STARTUP_LOCK) {
            if (looker == null) {
               startupFxPlatform();

               final ArrayList<String> packagesToSearch = new ArrayList<>(Arrays.asList("sh",
                                                                                        "one",
                                                                                        "org.glassfish",
                                                                                        "com.informatics"));

               if (System.getProperty(SystemPropertyConstants.EXTRA_PACKAGES_TO_SEARCH) != null) {
                  final String[] extraPackagesToSearch = System.getProperty(SystemPropertyConstants.EXTRA_PACKAGES_TO_SEARCH)
                                                               .split(";");

                  packagesToSearch.addAll(Arrays.asList(extraPackagesToSearch));
               }

               try {
                  final String[] packages = packagesToSearch.toArray(new String[] {});

                  LOG.info("Looking for HK2 annotations skipping inhabitant files and scanning in the packages: " +
                        Arrays.toString(packages));

                  final ServiceLocator temp = HK2RuntimeInitializer.init("ISAAC", false, packages);

                  if (looker != null) {
                     final RuntimeException e =
                        new RuntimeException(
                            "RECURSIVE Lookup Service Reference!  Ensure that there are no static variables " +
                            "objects utilizing the LookupService during their init!");

                     e.printStackTrace();
                     throw e;
                  }

                  looker = temp;
                  LOG.info("HK2 initialized.  Identifed " + looker.getAllServiceHandles((criteria) -> {
                           return true;
                        }).size() + " services.  Looker ID: " + looker.getLocatorId());
               } catch (IOException | ClassNotFoundException | MultiException e) {
                  throw new RuntimeException(e);
               }

               try {
                  LookupService.startupWorkExecutors();
               } catch (final Exception e) {
                  final RuntimeException ex =
                     new RuntimeException(
                         "Unexpected error trying to come up to the work executors level, possible classpath problems!",
                         e);

                  ex.printStackTrace();  // We are in a world of hurt if this happens, make sure this exception makes it out somewhere, and doesn't get eaten.
                  throw ex;
               }
            }
         }
      }

      return looker;
   }

   /**
    * Checks if initialized.
    *
    * @return true, if initialized
    */
   public static boolean isInitialized() {
      return looker != null;
   }

   /**
    * Checks if isaac started.
    *
    * @return true, if isaac started
    */
   public static boolean isIsaacStarted() {
      return isInitialized() ? getService(RunLevelController.class).getCurrentRunLevel() >= SL_L5_ISAAC_STARTED_RUNLEVEL
                             : false;
   }

   /**
    * Find a service by name, and automatically fall back to any service which implements the contract if the named service was not available.
    *
    * @param <T> the generic type
    * @param contractOrService May not be null, and is the contract or concrete implementation to get the best instance of
    * @param name May be null (to indicate any name is ok), and is the name of the implementation to be returned
    * @return the named service if possible
    */
   public static <T> T getNamedServiceIfPossible(Class<T> contractOrService, String name) {
      T service = null;

      if (StringUtils.isEmpty(name)) {
         service = get().getService(contractOrService);
      } else {
         service = get().getService(contractOrService, name);

         if (service == null) {
            service = get().getService(contractOrService);
         }
      }

      LOG.debug("LookupService returning {} for {} with name={}", ((service != null) ? service.getClass()
            .getName()
            : null), contractOrService.getName(), name);
      return service;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the run level.
    *
    * @param targetRunLevel the new run level
    */
   public static void setRunLevel(int targetRunLevel) {
      final RunLevelController rlc = getService(RunLevelController.class);
      int currentRunLevel = rlc.getCurrentRunLevel();
      
      // Make sure we aren't still proceeding somewhere, if so, we need to wait...
      RunLevelFuture rlf = rlc.getCurrentProceeding();
      if (rlf != null) {
         LOG.info("Attempting to cancel previous runlevel request");
         rlf.cancel(true);
         try {
            rlf.get();
         } catch (InterruptedException | ExecutionException e) {
            // noop
         }
      }
      
      if (currentRunLevel > targetRunLevel) {
         Iterator<Entry<Stoppable, Integer>> it = jobsToStop.entrySet().iterator();
          while (it.hasNext()) { 
              //Stop any jobs that need to be stopped prior to shutting down services
             Entry<Stoppable, Integer> job = it.next();
              if (job.getValue() > targetRunLevel) {
                 job.getKey().stopJob();
                 it.remove();
              }
           }

         while (currentRunLevel > targetRunLevel) {
            LOG.info("Setting run level to: " + --currentRunLevel);
            try {
               getService(RunLevelController.class).proceedTo(currentRunLevel);
            }
            catch (MultiException e) {
               if (e.getErrors().size() == 1 && e.getErrors().get(0) instanceof InterruptedException) {
                  LOG.info("Interrupted while wating for runlevel change?  Continuing to try to achive desired run level...");
                  ++currentRunLevel;  //the attempt failed, make sure we try again.
               }
               else {
                  throw e;
               }
            }
         }
         HashSet<String> clearedCaches = new HashSet<>();
         getActiveServices(IsaacCache.class).forEach((cache) -> {
            LOG.info("Clear cache for: {}", cache.getClass().getName());
            cache.reset();
            clearedCaches.add(cache.getClass().getName());
         });
         //There are some cache services that have static methods, that may not have been "active" here.  clear those too.
         getServices(StaticIsaacCache.class).forEach((cache) -> {
             if (!clearedCaches.contains(cache.getClass().getName()))
             {
                LOG.info("Clear cache for: {}", cache.getClass().getName());
                cache.reset();
             }
          });
      } else {
         while (currentRunLevel < targetRunLevel) {
            LOG.info("Setting run level to: " + ++currentRunLevel);
            getService(RunLevelController.class).proceedTo(currentRunLevel);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Return the highest ranked service that implements the requested contract or implementation class.
    *
    * @param <T> the generic type
    * @param contractOrImpl the contract or impl
    * @return the service
    * @see ServiceLocator#getService(Class, Annotation...)
    */
   public static <T> T getService(Class<T> contractOrImpl) {
      final T service = get().getService(contractOrImpl, new Annotation[0]);

      LOG.debug("LookupService returning {} for {}", ((service != null) ? service.getClass()
            .getName()
            : null), contractOrImpl.getName());
      return service;
   }
   
   /**
    * Return all services that implement the specified contract.
    * *Caution* this will launch and return services that should not be active, according to your run level - 
    * this method should not be used during shutdown operations (for example, don't use this to do a syncAll())
    * as it will attempt to sync services that were never even started.
    * See {@link #getActiveServices(Class)}
    * @param contractOrImpl
    * @return all services that implement the specified contract
    */
   public static <T> List<T> getServices(Class<T> contractOrImpl) {
      final List<T> services = get().getAllServices(contractOrImpl, new Annotation[0]);

      LOG.debug("LookupService returning {} for {}", services, contractOrImpl.getName());
      return services;
   }
   
   /**
    * Return implementations of the specified contract that are active at our current runlevel.
    * @param contractOrImpl
    * @return implementations of the specified contract that are active at our current runlevel
    */
   public static <T> List<T> getActiveServices(Class<T> contractOrImpl) {
      
      final List<T> services = new ArrayList<>();
      get().getAllServiceHandles(contractOrImpl).forEach(serviceHandle ->
      {
         if (serviceHandle.isActive()) {
            services.add(serviceHandle.getService());
         }
      });

      LOG.debug("LookupService returning active services {} for {}", services, contractOrImpl.getName());
      return services;
   }

   /**
    * Find the best ranked service with the specified name.  If no service with the specified name is available,
    * this returns null (even if there is a service with another name [or no name] which would meet the contract)
    *
    * @param <T> the generic type
    * @param contractOrService May not be null, and is the contract or concrete implementation to get the best instance of
    * @param name May not be null or empty
    * @return the service
    * @see ServiceLocator#getService(Class, String, Annotation...)
    */
   public static <T> T getService(Class<T> contractOrService, String name) {
      if (StringUtils.isEmpty(name)) {
         throw new IllegalArgumentException("You must specify a service name to use this method");
      }

      final T service = get().getService(contractOrService, name, new Annotation[0]);

      LOG.debug("LookupService returning {} for {} with name={}", ((service != null) ? service.getClass()
            .getName()
            : null), contractOrService.getName(), name);
      return service;
   }

   /**
    * Return true if and only if any service implements the requested contract or implementation class.
    *
    * @param contractOrImpl the contract or impl
    * @return true, if successful
    * @see ServiceLocator#getService(Class, Annotation...)
    */
   public static boolean hasService(Class<?> contractOrImpl) {
      return get().getServiceHandle(contractOrImpl, new Annotation[0]) != null;
   }

   /**
    * Return true if and only if there is a service with the specified name.  If no service with the specified name is available,
    * this returns false (even if there is a service with another name [or no name] which would meet the contract)
    *
    * @param contractOrService May not be null, and is the contract or concrete implementation to get the best instance of
    * @param name May not be null or empty
    * @return true, if successful
    * @see ServiceLocator#getService(Class, String, Annotation...)
    */
   public static boolean hasService(Class<?> contractOrService, String name) {
      if (StringUtils.isEmpty(name)) {
         throw new IllegalArgumentException("You must specify a service name to use this method");
      }

      return get().getServiceHandle(contractOrService, name, new Annotation[0]) != null;
   }

   public static void syncAll() {
      List<DatastoreServices> syncServiceList =  getActiveServices(DatastoreServices.class);
      for (DatastoreServices syncService:  syncServiceList) {
         try {
            syncService.sync().get();
         } catch (Throwable ex) {
            LOG.error(ex);
         }
      }
   }
   
   /**
    * Certain background jobs may need to be told to stop, if a system shutdown is in process - for example, the indexer 
    * uses services at run levels 4 and 5 - while it itself starts at runlevel 3.  If we are shutting down, and a reindex
    * is in process - bad things happen when 4 and 5 runlevel services stop, prior to the index service stopping at runlevel 3.
    * @param stoppable - the job to stop
    * @param stopIfGoingBelowRunlevel - issue the stop command if the run level is going to drop below this level.
    */
   public static void registerStoppable(Stoppable stoppable, int stopIfGoingBelowRunlevel) {
      LOG.debug("Registering {} to stop if below runlevel {}", stoppable, stopIfGoingBelowRunlevel);
      jobsToStop.put(stoppable, stopIfGoingBelowRunlevel);
   }
}

