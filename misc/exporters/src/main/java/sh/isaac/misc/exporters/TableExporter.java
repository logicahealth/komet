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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;

/**
 * A class to export content into a TSV and SQL form for data validation.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class TableExporter
{
	private Workbook workbook;
	private File excelExportFolder;
	private File tsvExportFolder;
	private Connection h2Connection;
	
	private DataTypeWriter extraUUIDs;

	public TableExporter(File tsvExportFolder, File h2ExportFolder, File excelExportFolder) throws IOException, ClassNotFoundException, SQLException
	{
		if (tsvExportFolder != null)
		{
			tsvExportFolder.mkdirs();
			this.tsvExportFolder = tsvExportFolder;
		}

		if (excelExportFolder != null)
		{
			excelExportFolder.mkdirs();
			this.excelExportFolder = excelExportFolder;
			workbook = new XSSFWorkbook();
		}

		if (h2ExportFolder != null)
		{
			h2ExportFolder.mkdirs();
			Class.forName("org.h2.Driver");
			h2Connection = DriverManager.getConnection("jdbc:h2:" + new File(h2ExportFolder, "IsaacExport").getAbsolutePath() 
					+ ";LOG=0;LOCK_MODE=0;MV_STORE=FALSE");
		}
		
		extraUUIDs = new DataTypeWriter("extraUuids", tsvExportFolder, h2Connection, workbook, new String[] {"UUID", "UUID1", "UUID2", "UUID3", "UUID4", "UUID5"}, 
				new Class[] {UUID.class, UUID.class, UUID.class, UUID.class, UUID.class, UUID.class});
		
		exportConcepts();
		close();

	}

	private void close() throws FileNotFoundException, IOException, SQLException
	{
		extraUUIDs.close();
		if (workbook != null)
		{
			try (OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(new File(excelExportFolder, "IsaacExport.xlsx"))))
			{
				workbook.write(fileOut);
			}
			workbook.close();
		}
	}

	private void exportConcepts() throws IOException, SQLException
	{
		DataTypeWriter dtw = new DataTypeWriter("concept", tsvExportFolder, h2Connection, workbook, 
				new String[] {"UUID", "IsaacObjectType", "VersionType", "Description", "Status", "Time", "Author", "Module", "Path"}, 
				new Class[] {UUID.class, String.class, String.class, String.class, String.class, Time.class, UUID.class, UUID.class, UUID.class});
		
		Get.conceptService().getConceptChronologyStream().forEach(concept -> {
			
			UUID[] uuids = concept.getUuids();
			if (uuids.length > 1)
			{
				UUID[] temp = new UUID[6];
				for (int i = 0; i < uuids.length; i++)
				{
					temp[i] = uuids[i];
				}
				//Pad to column length
				for (int i = (uuids.length); i < 6; i++)
				{
					temp[i] = null;
				}
				extraUUIDs.addRow(temp);
			}
			
			for (Version conceptVersion : concept.getVersionList())
			{
				dtw.addRow(new Object[] {uuids[0], concept.getIsaacObjectType().toString(), concept.getVersionType().toString(), 
						Get.conceptDescriptionText(concept.getNid()), 
						conceptVersion.getStatus().toString(), new Date(conceptVersion.getTime()),
						Get.identifierService().getUuidPrimordialForNid(conceptVersion.getAuthorNid()),
						Get.identifierService().getUuidPrimordialForNid(conceptVersion.getModuleNid()),
						Get.identifierService().getUuidPrimordialForNid(conceptVersion.getPathNid())});
			}
		});
		
		dtw.close();
	}
}
