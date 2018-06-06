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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.opencsv.CSVWriter;

/**
 * Code that writes for a single table / sheet / file.
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DataTypeWriter
{
	
	private CSVWriter tsvWriter;
	private Connection h2Connection;
	private PreparedStatement insertStatement;
	private int insertStatementParamCount = 0;
	private Sheet sheet;
	private SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public DataTypeWriter(String dataTypeLabel, File tsvExportFolder, Connection h2Connection, Workbook workbook, String[] columnHeaders, Class<?>[] dataTypes)
	{
		try
		{
			if (tsvExportFolder != null)
			{
				tsvWriter = new CSVWriter(new BufferedWriter(new FileWriter(new File(tsvExportFolder, "IsaacExport-" + dataTypeLabel + ".tsv"))), '\t', 
						CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
				tsvWriter.writeNext(columnHeaders);
			}
			this.h2Connection = h2Connection;
			
			if (h2Connection != null)
			{
				StringBuilder tableCreate = new StringBuilder("CREATE TABLE \"" + dataTypeLabel + "\" (");
				StringBuilder insertStatementBuilder = new StringBuilder("INSERT INTO \"" + dataTypeLabel + "\" (");
				StringBuilder placeholders = new StringBuilder();
				for (int i = 0; i < columnHeaders.length; i++)
				{
					tableCreate.append("\"" + columnHeaders[i] + "\"");
					if (dataTypes[i].equals(UUID.class))
					{
						tableCreate.append(" UUID, ");
					}
					else if (dataTypes[i].equals(Time.class))
					{
						tableCreate.append(" TIMESTAMP, ");
					}
					else if (dataTypes[i].equals(Long.class))
					{
						tableCreate.append(" BIGINT, ");
					}
					else
					{
						tableCreate.append(" VARCHAR, ");
					}
					
					insertStatementBuilder.append("\"" + columnHeaders[i] + "\"");
					insertStatementBuilder.append(", ");
					
					placeholders.append("?, ");
					insertStatementParamCount++;
				}
				tableCreate.setLength(tableCreate.length() - 2);
				tableCreate.append(")");
				
				insertStatementBuilder.setLength(insertStatementBuilder.length() - 2);
				insertStatementBuilder.append(") VALUES (");
				
				placeholders.setLength(placeholders.length() - 2);
				insertStatementBuilder.append(placeholders.toString());
				insertStatementBuilder.append(")");
				
				
				h2Connection.createStatement().execute(tableCreate.toString());
				insertStatement = h2Connection.prepareStatement(insertStatementBuilder.toString());
			}
			
			if (workbook != null)
			{
				sheet = workbook.createSheet(dataTypeLabel);
				Row row = sheet.createRow(0);
				for (int i = 0; i < columnHeaders.length; i++)
				{
					Cell cell = row.createCell(i);
					cell.setCellValue(columnHeaders[i]);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("oops", e);
		}
	}
	
	public void addRow(Object[] data)
	{
		try
		{
			if (tsvWriter != null)
			{
				String[] temp = new String[data.length];
				for (int i = 0; i < data.length; i++)
				{
					if (data[i] instanceof Date)
					{
						temp[i] = timeFormatter.format(((Date)data[i]));
					}
					else
					{
						temp[i] = data[i] == null ? "" : data[i].toString();
					}
				}
				tsvWriter.writeNext(temp);
			}
			
			if (sheet != null)
			{
				Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
				for (int i = 0; i < data.length; i++)
				{
					Cell cell = row.createCell(i);
					if (data[i] instanceof Date)
					{
						cell.setCellValue((Date)data[i]);
					}
					else if (data[i] instanceof Long)
					{
						cell.setCellValue((Long)data[i]);
					}
					else if (data[i] != null)
					{
						cell.setCellValue(data[i].toString());
					}
				}
			}
			
			if (h2Connection != null)
			{
				for (int i = 0; i < data.length; i++)
				{
					insertStatement.setObject((i + 1), data[i]);
				}
				//pad trailing / missing data
				for (int i = data.length; i < insertStatementParamCount; i++)
				{
					insertStatement.setObject((i + 1), null);
				}
				insertStatement.execute();
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException("row write error", e);
		}
	}
	
	public void close() throws IOException, SQLException
	{
		if (tsvWriter != null)
		{
			tsvWriter.close();
		}
		if (insertStatement != null)
		{
			insertStatement.close();
		}
	}
}
