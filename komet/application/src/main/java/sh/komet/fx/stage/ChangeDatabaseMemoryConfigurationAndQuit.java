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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import javafx.application.Platform;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import static sh.isaac.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import sh.isaac.api.constants.MemoryConfiguration;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

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
      IsaacPreferences appPreferences = Get.applicationPreferences();
                  appPreferences.putEnum(memoryConfiguration);
      
      appPreferences.sync();
      LookupService.shutdownSystem();
      
      Optional<String> location = appPreferences.get(DATA_STORE_ROOT_LOCATION_PROPERTY);
      if (location.isPresent()) {
         Files.walk(Paths.get(location.get()))
                .map(Path::toFile)
                .sorted(Comparator.reverseOrder())
                .forEach(File::delete);
      }
      Platform.exit();
      System.exit(0);      
      return null;
   }
   
}
