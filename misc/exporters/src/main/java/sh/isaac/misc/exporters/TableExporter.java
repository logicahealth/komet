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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sh.isaac.api.DataSource;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicLong;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.logic.LogicalExpressionImpl;

/**
 * A class to export content into a TSV and SQL form for data validation.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@SuppressWarnings("deprecation")
public class TableExporter extends TimedTaskWithProgressTracker<Void>
{
	private Workbook workbook;
	private File excelExportFolder;
	private File tsvExportFolder;
	private Connection h2Connection;
	
	private DataTypeWriter extraUUIDs;
	private static final Logger LOG = LogManager.getLogger();
	
	private Cache<Integer, UUID> nidToUUIDCache = Caffeine.newBuilder().initialCapacity(300).maximumSize(300).build();

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
		updateTitle("Exporting...");
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
		if (h2Connection != null)
		{
			h2Connection.close();
		}
	}

	private void exportConcepts() throws IOException, SQLException
	{
		DataTypeWriter dtw = new DataTypeWriter("concept", tsvExportFolder, h2Connection, workbook, 
				new String[] {"UUID", "IsaacObjectType", "VersionType", "Assemblage", "Status", "Time", "Author", "Module", "Path", "Description"}, 
				new Class[] {UUID.class, String.class, String.class, UUID.class, String.class, Time.class, UUID.class, UUID.class, UUID.class, String.class});
		
		Get.conceptService().getConceptChronologyStream().sequential().forEach(concept -> {
			
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
						getUuidPrimordialForNid(concept.getAssemblageNid()),
						conceptVersion.getStatus().toString(), new Date(conceptVersion.getTime()),
						getUuidPrimordialForNid(conceptVersion.getAuthorNid()),
						getUuidPrimordialForNid(conceptVersion.getModuleNid()),
						getUuidPrimordialForNid(conceptVersion.getPathNid()),
						Get.conceptDescriptionText(concept.getNid())});
			}
			completedUnitOfWork();
		});
		
		dtw.close();
	}

	private void exportSemantics() throws IOException, SQLException
	{		
		HashMap<Integer, DataTypeWriter> semanticWriters = new HashMap<>();

		Get.assemblageService().getSemanticChronologyStream().sequential().forEach(semantic -> {
			
			UUID[] uuids = semantic.getUuids();
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
			DataTypeWriter dtw = semanticWriters.get(semantic.getAssemblageNid());
			if (dtw == null)
			{
				String semanticDescription = Get.conceptService().getConceptChronology(semantic.getAssemblageNid()).getRegularName()
						.orElse(Get.conceptDescriptionText(semantic.getAssemblageNid()));
				
				semanticDescription = "assemblage" + formatName(semanticDescription, true)+ "-" + semantic.getAssemblageNid();
				
				ArrayList<String> columnHeaders = new ArrayList<>(Arrays.asList(
						new String[] {"UUID", "IsaacObjectType", "VersionType", "ReferencedComponent", "Status", "Time", "Author", "Module", "Path"}));
				@SuppressWarnings("rawtypes")
				ArrayList<Class> columnDataTypes = new ArrayList<>(Arrays.asList(
						new Class[] {UUID.class, String.class, String.class, UUID.class, String.class, Time.class, UUID.class, UUID.class, UUID.class}));
				switch (semantic.getVersionType())
				{
					case MEMBER:
						//noop
						break;
					case COMPONENT_NID:
						columnHeaders.add("component");
						columnDataTypes.add(UUID.class);
						break;
					case DESCRIPTION:
						columnHeaders.add("text");
						columnDataTypes.add(String.class);
						columnHeaders.add("descriptionType");
						columnDataTypes.add(String.class);
						columnHeaders.add("language");
						columnDataTypes.add(UUID.class);
						columnHeaders.add("caseSignificance");
						columnDataTypes.add(UUID.class);
						break;
					case STRING:
						columnHeaders.add("string");
						columnDataTypes.add(String.class);
						break;
					case LONG:
						columnHeaders.add("long");
						columnDataTypes.add(Long.class);
						break;
					case DYNAMIC:
						DynamicUsageDescription dud = Get.service(DynamicUtility.class).readDynamicUsageDescription(semantic.getAssemblageNid());
						for (DynamicColumnInfo dci : dud.getColumnInfo())
						{
							columnHeaders.add(formatName(dci.getColumnName(), false));
							if (dci.getColumnDataType() == DynamicDataType.LONG)
							{
								columnDataTypes.add(Long.class);
							}
							else if (dci.getColumnDataType() == DynamicDataType.UUID || dci.getColumnDataType() == DynamicDataType.NID)
							{
								columnDataTypes.add(UUID.class);
							}
							else 
							{
								columnDataTypes.add(String.class);
							}
							//Could support some other types natively in sql / excel, but no real need at the moment...
						}
						break;
					case LOGIC_GRAPH:
						columnHeaders.add("graph");
						columnDataTypes.add(String.class);
						break;
						
					//These could be supported dynamically, with the mocking info available in the 'brittle' types.  but don't care right now.
					case LOINC_RECORD:
					case MEASURE_CONSTRAINTS:
					case Nid1_Int2:
					case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
					case Nid1_Int2_Str3_Str4_Nid5_Nid6:
					case Nid1_Nid2:
					case Nid1_Nid2_Int3:
					case Nid1_Nid2_Str3:
					case Nid1_Str2:
					case RF2_RELATIONSHIP:
					case Str1_Nid2_Nid3_Nid4:
					case Str1_Str2:
					case Str1_Str2_Nid3_Nid4:
					case Str1_Str2_Nid3_Nid4_Nid5:
					case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
					case CONCEPT:
					case UNKNOWN:
					default :
						LOG.warn("Not writing all data for unsupported semantic type " + semantic.getVersionType().toString());
						break;
				}
				
				dtw = new DataTypeWriter(semanticDescription, tsvExportFolder, h2Connection, workbook, columnHeaders.toArray(new String[columnHeaders.size()]),
						columnDataTypes.toArray(new Class[columnDataTypes.size()]));
				
				semanticWriters.put(semantic.getAssemblageNid(), dtw);
			}

					
			
			for (Version semanticVersion : semantic.getVersionList())
			{
				ArrayList<Object> data = new ArrayList<>();
				data.add(uuids[0]);
				data.add(semantic.getIsaacObjectType().toString());
				data.add(semantic.getVersionType().toString());
				data.add(getUuidPrimordialForNid(semantic.getReferencedComponentNid()));
				data.add(semanticVersion.getStatus().toString());
				data.add(new Date(semanticVersion.getTime()));
				data.add(getUuidPrimordialForNid(semanticVersion.getAuthorNid()));
				data.add(getUuidPrimordialForNid(semanticVersion.getModuleNid()));
				data.add(getUuidPrimordialForNid(semanticVersion.getPathNid()));
				
				switch (semantic.getVersionType())
				{
					case MEMBER:
						//noop
						break;
					case COMPONENT_NID:
						data.add(getUuidPrimordialForNid(((ComponentNidVersion)semanticVersion).getComponentNid()));
						break;
					case DESCRIPTION:
						data.add(((DescriptionVersion)semanticVersion).getText());
						data.add(((DescriptionVersion)semanticVersion).getDescriptionType());
						data.add(getUuidPrimordialForNid(((DescriptionVersion)semanticVersion).getLanguageConceptNid()));
						data.add(getUuidPrimordialForNid(((DescriptionVersion)semanticVersion).getCaseSignificanceConceptNid()));
						break;
					case STRING:
						data.add(((StringVersion)semanticVersion).getString());
						break;
					case LONG:
						data.add(((LongVersion)semanticVersion).getLongValue());
						break;
					case DYNAMIC:
						for (DynamicData dd : ((DynamicVersion<?>)semanticVersion).getData())
						{
							if (dd == null)
							{
								data.add(null);
							}
							else if (dd.getDynamicDataType() == DynamicDataType.NID)
							{
								data.add(getUuidPrimordialForNid(((DynamicNid)dd).getDataNid()));
							}
							else if (dd.getDynamicDataType() == DynamicDataType.UUID)
							{
								data.add(((DynamicUUID)dd).getDataUUID());
							}
							else if (dd.getDynamicDataType() == DynamicDataType.LONG)
							{
								data.add(((DynamicLong)dd).getDataLong());
							}
							else
							{
								data.add(dd.dataToString());
							}
						}
						break;
					case LOGIC_GRAPH:
						data.add(new LogicalExpressionImpl(((LogicGraphVersion)semanticVersion).getExternalGraphData(), DataSource.EXTERNAL).toString());
						break;
						
					//These could be supported dynamically, with the mocking info available in the 'brittle' types.  but don't care right now.
					case LOINC_RECORD:
					case MEASURE_CONSTRAINTS:
					case Nid1_Int2:
					case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
					case Nid1_Int2_Str3_Str4_Nid5_Nid6:
					case Nid1_Nid2:
					case Nid1_Nid2_Int3:
					case Nid1_Nid2_Str3:
					case Nid1_Str2:
					case RF2_RELATIONSHIP:
					case Str1_Nid2_Nid3_Nid4:
					case Str1_Str2:
					case Str1_Str2_Nid3_Nid4:
					case Str1_Str2_Nid3_Nid4_Nid5:
					case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
					case CONCEPT:
					case UNKNOWN:
					default :
						//noop for now
						break;
				}
				dtw.addRow(data.toArray(new Object[data.size()]));
			}
			completedUnitOfWork();
		});
		
		for (DataTypeWriter dtw : semanticWriters.values())
		{
			dtw.close();
		}
	}
	
	private String formatName(String input, boolean uppercaseFirst)
	{
		String temp = WordUtils.capitalizeFully(input, ' ').replaceAll(" ", "").replaceAll("[\\[\\]\\+]", "");
		
		return uppercaseFirst ? temp.substring(0,  1).toUpperCase() + temp.substring(1, temp.length()) : temp;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected Void call() throws Exception
	{
		addToTotalWork(Get.conceptService().getConceptCount());
		addToTotalWork(Get.assemblageService().getSemanticCount());
		exportConcepts();
		exportSemantics();
		close();
		return null;
	}
	
	private UUID getUuidPrimordialForNid(int nid)
	{
		return nidToUUIDCache.get(nid, theNid -> 
		{
			return Get.identifierService().getUuidPrimordialForNid(theNid);
		});
	}
}
