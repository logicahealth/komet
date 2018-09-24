/*
 * Copyright 2017 ISAAC's KOMET Collaborators.
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
package sh.komet.fx.stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import javafx.application.Platform;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.komet.preferences.IsaacPreferencesImpl;
import sh.isaac.provider.metacontent.MVStoreMetaContentProvider;

/**
 *
 * @author kec
 */
public class ChangeDatabaseMemoryConfigurationAndQuit extends TimedTaskWithProgressTracker<Void> {
   final MemoryConfiguration memoryConfiguration;

   public ChangeDatabaseMemoryConfigurationAndQuit(MemoryConfiguration memoryConfiguration) {
      this.memoryConfiguration = memoryConfiguration;
   }
   
   
   @Override
   protected Void call() throws Exception {
      Get.configurationService().getGlobalDatastoreConfiguration().putOption(MemoryConfiguration.class.getName(), memoryConfiguration.name());
      LookupService.shutdownSystem();
      
      Path location = Get.configurationService().getDataStoreFolderPath();
      Files.walk(location)
             .map(Path::toFile)
             .sorted(Comparator.reverseOrder())
             .forEach(file -> 
             {
                if (file.getParentFile().getName().equals(IsaacPreferencesImpl.DB_PREFERENCES_FOLDER) || file.getParentFile().getName().equals(MVStoreMetaContentProvider.STORE_FOLDER)
                   || file.getName().equals(IsaacPreferencesImpl.DB_PREFERENCES_FOLDER) || file.getName().equals(MVStoreMetaContentProvider.STORE_FOLDER)) {
                   //don't delete these files where our prefs are stored....
                }
                else {
                   file.delete();
                }
             });
      Platform.exit();
      System.exit(0);
      return null;
   }
   
}
