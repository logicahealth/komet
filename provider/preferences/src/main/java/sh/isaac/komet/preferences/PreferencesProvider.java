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



package sh.isaac.komet.preferences;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.LookupService;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;


//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service(name = "Preferences Provider")
@RunLevel(value = LookupService.SL_L0_METADATA_STORE_STARTED_RUNLEVEL)
public class PreferencesProvider
         implements PreferencesService {

   private static final Logger LOG = LogManager.getLogger();

   //~--- methods -------------------------------------------------------------

   
   /**
    * Start me.
    */
   @PostConstruct
   protected void startMe() {
      //Just doing this to make sure it starts without errors
      getConfigurationPreferences();
   }
   /**
    * Stop me.
    */
   @PreDestroy
   protected void stopMe() {
      try {
         LOG.info("Stopping Preferences Provider.");
         getConfigurationPreferences().sync();
      } catch (Throwable ex) {
         LOG.error("Unexpected error stopping prefs provider", ex);
         throw new RuntimeException(ex);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public void reloadConfigurationPreferences() {
       IsaacPreferencesImpl.reloadConfigurationPreferences();
   }
   /**
    * {@inheritDoc}
    */
   @Override
   public IsaacPreferences getConfigurationPreferences() {
      return IsaacPreferencesImpl.getConfigurationRootPreferences();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IsaacPreferences getSystemPreferences() {
      return new PreferencesWrapper(Preferences.systemRoot());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IsaacPreferences getUserPreferences() {
      return new PreferencesWrapper(Preferences.userRoot());
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void clearConfigurationPreferences() {
      try {
         getConfigurationPreferences().removeNode();
         getConfigurationPreferences().flush();
      }
      catch (BackingStoreException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void clearSystemPreferences() {
      try {
         Preferences.systemRoot().removeNode();
         Preferences.systemRoot().flush();
      }
      catch (BackingStoreException e) {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void clearUserPreferences() {
      try {
         Preferences.userRoot().removeNode();
         Preferences.userRoot().flush();
      }
      catch (BackingStoreException e) {
         throw new RuntimeException(e);
      }
   }
}