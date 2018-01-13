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

//~--- JDK imports ------------------------------------------------------------

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;

//~--- JDK imports ------------------------------------------------------------

import com.sun.javafx.application.PlatformImpl;

//~--- non-JDK imports --------------------------------------------------------

import gov.va.oia.HK2Utilities.HK2RuntimeInitializer;
import sh.isaac.api.DatastoreServices.DataStoreStartState;
import sh.isaac.api.constants.Constants;
import sh.isaac.api.index.IndexQueryService;

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

   public static final int SL_L5_ISAAC_DEPENDENTS_RUNLEVEL = 5;  //Anything that depends on issac as a whole to be started should be 5 - 
   //this is the fully-started state.

   public static final int SL_L4_ISAAC_STARTED_RUNLEVEL = 4; //at level 3 and 4, secondary isaac services start, such as changeset providers, etc.
   
   public static final int SL_L3 = 3; 
   
   public static final int SL_L2_DATABASE_SERVICES_STARTED_RUNLEVEL = 2;  //In general, ISAAC data-store services start between 0 and 2, in an 
   //isaac specific order.

   public static final int SL_L1 = 1; 
   
   public static final int SL_L0 = 0; 

   //Below 0, we have utility stuff... no ISAAC services.

   public static final int SL_NEG_1_METADATA_STORE_STARTED_RUNLEVEL = -1;

   public static final int SL_NEG_2_WORKERS_STARTED_RUNLEVEL = -2;

   public static final int SL_NEG_3_SYSTEM_STOPPED_RUNLEVEL = -3;

   /** The Constant STARTUP_LOCK. */
   private static final Object STARTUP_LOCK = new Object();

   //~--- methods -------------------------------------------------------------

   /**
    * Stop all core isaac service, blocking until stopped (or failed).
    */
   public static void shutdownIsaac() {

      if (isInitialized()) {
         Get.applicationStates().add(ApplicationStates.STOPPING);
         Get.applicationStates().remove(ApplicationStates.RUNNING);
         syncAll();  //Dan says - really not sure why this should be necessary....  if a datastore doesn't sync itself on shutdown, its broken...
         setRunLevel(SL_NEG_2_WORKERS_STARTED_RUNLEVEL);

         // Fully release any system locks to database
         System.gc();
      }
   }

   /**
    * Stop all system services, blocking until stopped (or failed).
    */
   public static void shutdownSystem() {
      if (isInitialized()) {
         Get.applicationStates().add(ApplicationStates.STOPPING);
         Get.applicationStates().remove(ApplicationStates.RUNNING);
         syncAll();  //Dan says - really not sure why this should be necessary....  if a datastore doesn't sync itself on shutdown, its broken...
         setRunLevel(SL_NEG_3_SYSTEM_STOPPED_RUNLEVEL);
         looker.shutdown();
         ServiceLocatorFactory.getInstance()
                              .destroy(looker);
         looker = null;
         
         // Fully release any system locks to database
         System.gc();
      }
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
                  //LOG.info("Installing headless toolkit");
                  //HeadlessToolkit.installToolkit();
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
   
   public static void startupPreferenceProvider() {
      if (getService(RunLevelController.class).getCurrentRunLevel() < SL_NEG_1_METADATA_STORE_STARTED_RUNLEVEL) {
      	Get.applicationStates().add(ApplicationStates.STARTING);
         setRunLevel(SL_NEG_1_METADATA_STORE_STARTED_RUNLEVEL);
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
         setRunLevel(SL_L2_DATABASE_SERVICES_STARTED_RUNLEVEL);

         validateDatabaseFolderStatus();

         // If database is validated, startup remaining run levels

         LOG.info("Bringing up the rest of isaac...");
         setRunLevel(SL_L4_ISAAC_STARTED_RUNLEVEL);
         LOG.info("Bringing up isaac dependents...");
         setRunLevel(SL_L5_ISAAC_DEPENDENTS_RUNLEVEL);
         
         //Make sure metadata is imported, if the user prefs said to import metadata.
         get().getService(MetadataService.class).importMetadata();
         
         //Now, check and make sure every provider has the same DB ID
         UUID expected = get().getService(MetadataService.class).getDataStoreId().get();
         
         get().getAllServiceHandles(DatastoreServices.class).forEach(handle -> {
            if (handle.isActive()) {
                  if (!expected.equals(handle.getService().getDataStoreId().get())) {
                     throw new RuntimeException("Inconsistent Data Store state!  Provider " + handle.getActiveDescriptor().getImplementation()  
                           + " has an id of " + handle.getService().getDataStoreId() 
                           + ".  Expected " + expected);
                  }
               }
            });
         

         Get.applicationStates().add(ApplicationStates.RUNNING);
         Get.applicationStates().remove(ApplicationStates.STARTING);

      } catch (final Throwable e) {
         e.printStackTrace();
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
      if (getService(RunLevelController.class).getCurrentRunLevel() < SL_NEG_1_METADATA_STORE_STARTED_RUNLEVEL) {
         setRunLevel(SL_NEG_1_METADATA_STORE_STARTED_RUNLEVEL);
      }
   }

   /**
    * Start the WorkExecutor services (without starting ISAAC core services), blocking until started (or failed).
    */
   public static void startupWorkExecutors() {
      if (getService(RunLevelController.class).getCurrentRunLevel() < SL_NEG_2_WORKERS_STARTED_RUNLEVEL) {
         setRunLevel(SL_NEG_2_WORKERS_STARTED_RUNLEVEL);
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
      final UUID discoveredDBId = get().getService(IdentifierService.class).getDataStoreId().get();
      
      LOG.info("System starting with datastore in {} state, with an ID of {}", discoveredValidityValue, discoveredDBId);
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
                        + ".  Expected " + handle.getService().getDataStoreStartState());
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
                                                                                        "org.ihtsdo",
                                                                                        "org.glassfish",
                                                                                        "com.informatics"));
               final boolean readInhabitantFiles = Boolean.valueOf(System.getProperty(Constants.READ_INHABITANT_FILES,
                                                                                      "false"));

               if (System.getProperty(Constants.EXTRA_PACKAGES_TO_SEARCH) != null) {
                  final String[] extraPackagesToSearch = System.getProperty(Constants.EXTRA_PACKAGES_TO_SEARCH)
                                                               .split(";");

                  packagesToSearch.addAll(Arrays.asList(extraPackagesToSearch));
               }

               try {
                  final String[] packages = packagesToSearch.toArray(new String[] {});

                  LOG.info("Looking for HK2 annotations " + (readInhabitantFiles ? "from inhabitant files"
                        : "skipping inhabitant files") + "; and scanning in the packages: " +
                        Arrays.toString(packages));

                  final ServiceLocator temp = HK2RuntimeInitializer.init("ISAAC", readInhabitantFiles, packages);

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
                        }).size() + " services");
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
      return isInitialized() ? getService(RunLevelController.class).getCurrentRunLevel() >= SL_L4_ISAAC_STARTED_RUNLEVEL
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
    * @param runLevel the new run level
    */
   public static void setRunLevel(int runLevel) {
      final RunLevelController rlc = getService(RunLevelController.class);
      final int current = rlc.getCurrentRunLevel();
      if (current > runLevel) {
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
         get().getAllServices(OchreCache.class).forEach((cache) -> {
            cache.reset();
         });
      }

      getService(RunLevelController.class).proceedTo(runLevel);
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
   public static <T> List<T> getServices(Class<T> contractOrImpl) {
      final List<T> services = get().getAllServices(contractOrImpl, new Annotation[0]);

      LOG.debug("LookupService returning {} for {}", services, contractOrImpl.getName());
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
      List<DatastoreServices> syncServiceList =  getServices(DatastoreServices.class);
      for (DatastoreServices syncService:  syncServiceList) {
         try {
            syncService.sync().get();
         } catch (Throwable ex) {
            LOG.error(ex);
         }
      }
   }
}

