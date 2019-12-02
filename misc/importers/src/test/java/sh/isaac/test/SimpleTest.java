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
package sh.isaac.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import javafx.application.Platform;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.convert.delta.vhat.VHATDeltaImport;
import sh.isaac.mojo.IndexTermstore;
import sh.isaac.mojo.LoadTermstore;
import sh.isaac.utility.Frills;

/**
 * {@link SimpleTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SimpleTest
{
   public static void main(String[] args) throws MojoExecutionException, IOException
   {
      try
      {
         File db = new File("target/db");
         FileUtils.deleteDirectory(db);
         db.mkdirs();
         System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, db.getCanonicalPath());
         LookupService.startupIsaac();
         LoadTermstore lt = new LoadTermstore();
         lt.setLog(new SystemStreamLog());
         lt.setibdfFilesFolder(new File("../../metadata/target/generated-resource/"));
         lt.execute();
         new IndexTermstore().execute();
         Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.INACTIVE);
         VHATDeltaImport i = new VHATDeltaImport(transaction,
            //new String(Files.readAllBytes(Paths.get("src/test/resources/VHAT XML Update files/Test File 2.xml"))),
            new String(Files.readAllBytes(Paths.get("src/test/resources/VHAT XML Update files/Test File 1.xml"))),
            TermAux.USER.getPrimordialUuid(), Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
            TermAux.DEVELOPMENT_PATH.getPrimordialUuid(),
            null, new File("target"));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         LookupService.shutdownSystem();
         System.exit(0);
         Platform.exit();
      }
      
   }
}
