/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.preferences;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface PreferencesService {
   
   /**
    * @return a database specific preferences store which is stored within the data store folder.
    */
   IsaacPreferences getConfigurationPreferences();
   
   /**
    * @return a preference store that is stored globally within an OS store, for any user that runs the application
    * on this specific computer.
    */
   IsaacPreferences getSystemPreferences();
   
   /**
    * @return a preference store that is stored within a user profile folder of the current OS user, so it will apply
    * to any database a user opens on this computer.
    */
   IsaacPreferences getUserPreferences();
   
   /**
    * Remove any and all application preferences stored data store folder
    * The behavior of any handles to the system preferences that are still held after this operation is undefined.
    */
   void clearConfigurationPreferences();
   
   /**
    * Remove any and all preferences stored in the system store
    * The behavior of any handles to the system preferences that are still held after this operation is undefined.
    */
   void clearSystemPreferences();
   
   /**
    * Remove any and all preferences stored in the system user store
    * The behavior of any handles to the user preferences that are still held after this operation is undefined.
    */
   void clearUserPreferences();
}
