/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.misc.exporters;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javafx.application.Platform;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.util.RecursiveDelete;

/**
 * Just a main for testing the exporter
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class TableExporterTest
{
	public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException
	{
		try
		{
			RecursiveDelete.delete(new File("target/data"));
			RecursiveDelete.delete(new File("target/export"));
			LookupService.startupPreferenceProvider();
			Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
			
			LookupService.startupIsaac();
			
			File exportFolder = new File("target/export");
			
			TableExporter te = new TableExporter(exportFolder, exportFolder, exportFolder);
			Get.workExecutors().getExecutor().execute(te);
		}
		finally
		{
			LookupService.shutdownSystem();
			Platform.exit();
		}
	}
}
