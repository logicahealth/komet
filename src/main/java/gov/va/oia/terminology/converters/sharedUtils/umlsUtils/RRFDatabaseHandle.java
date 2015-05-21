package gov.va.oia.terminology.converters.sharedUtils.umlsUtils;

import gov.va.oia.terminology.converters.sharedUtils.ConsoleUtil;
import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.sql.ColumnDefinition;
import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.sql.DataType;
import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.sql.TableDefinition;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class RRFDatabaseHandle
{
	Connection connection_;

	/**
	 * If file provided, created or opened at that path.  If file is null, an in-memory db is created.
	 * Returns false if the database already existed, true if it was newly created.
	 */
	public boolean createOrOpenDatabase(File dbFile) throws ClassNotFoundException, SQLException
	{
		boolean createdNew = true;
		if (dbFile != null)
		{
			File temp = new File(dbFile.getParentFile(), dbFile.getName() + ".h2.db");
			if (temp.exists())
			{
				createdNew = false;
			}
		}
		Class.forName("org.h2.Driver");
		if (dbFile == null)
		{
			connection_ = DriverManager.getConnection("jdbc:h2:mem:;MV_STORE=FALSE");
		}
		else
		{
			connection_ = DriverManager.getConnection("jdbc:h2:" + dbFile.getAbsolutePath() +";LOG=0;CACHE_SIZE=1024000;LOCK_MODE=0;;MV_STORE=FALSE");
		}
		return createdNew;
	}
	
	public void createTable(TableDefinition td) throws SQLException
	{
		Statement s = connection_.createStatement();
		
		StringBuilder sql = new StringBuilder();
		String tableName = td.getTableName();
		if (tableName.indexOf('/') > 0)
		{
			tableName = tableName.substring(tableName.indexOf('/') + 1);
		}
		sql.append("CREATE TABLE " + tableName + " (");
		for (ColumnDefinition cd : td.getColumns())
		{
			sql.append(cd.asH2());
			sql.append(",");
		}
		sql.setLength(sql.length() - 1);
		sql.append(")");
		
		ConsoleUtil.println("Creating Table " + tableName);
		s.executeUpdate(sql.toString());
	}
	
	public Connection getConnection()
	{
		return connection_;
	}
	
	public void shutdown() throws SQLException
	{
		connection_.close();
	}
	
	/**
	 * Create a set of tables that from the UMLS supplied MRCOLS
	 */
	public List<TableDefinition> loadTableDefinitionsFromMRCOLS(InputStream MRFILES, InputStream MRCOLS, HashSet<String> filesToSkip) throws Exception
	{
		//MRFILEs contains fileName/Description/Comma sep col list/col count/row count/byte count
		//MRCOLs contains: col name/description_/doc section number/MIN char/AV char/MAX char/fileName/dataType
		
		filesToSkip.add("MRFILES.RRF");
		filesToSkip.add("MRCOLS.RRF");
		
		ArrayList<String> prefixSkips = new ArrayList<>();
		for (String s : filesToSkip)
		{
			if (s.endsWith("*"))
			{
				prefixSkips.add(s.substring(0, s.length() - 1));
			}
		}
		
		ArrayList<String[]> mrFile = new ArrayList<>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(MRFILES));
		String line = br.readLine();
		while (line != null)
		{
			String[] temp = line.split("\\|");
			if (temp.length > 0)
			{
				mrFile.add(temp);
			}
			line = br.readLine();
		}
		br.close();
		
		//Filename -> col -> datatype
		HashMap<String, HashMap<String, String>> mrCol = new HashMap<>();
		
		br = new BufferedReader(new InputStreamReader(MRCOLS));
		line = br.readLine();
		while (line != null)
		{
			String[] temp = line.split("\\|");
			if (temp.length > 0)
			{
				HashMap<String, String> nested = mrCol.get(temp[6]);
				if (nested == null)
				{
					nested = new HashMap<String, String>();
					mrCol.put(temp[6], nested);
				}
				nested.put(temp[0], temp[7]);
			}
			line = br.readLine();
		}
		br.close();
		
		ArrayList<TableDefinition> tables = new ArrayList<>();
		for (String[] table : mrFile)
		{
			String fileName = table[0];
			boolean skip = false;
			for (String prefix : prefixSkips)
			{
				if (fileName.startsWith(prefix))
				{
					skip = true;
					break;
				}
			}
			
			if (skip || filesToSkip.contains(fileName))
			{
				continue;
			}
			TableDefinition td = new TableDefinition(fileName.substring(0, fileName.indexOf('.')));
			HashMap<String, String> cols = mrCol.get(fileName);
			for (String col : table[2].split(","))
			{
				td.addColumn(new ColumnDefinition(col, new DataType(cols.get(col), null)));
			}
			tables.add(td);
			createTable(td);
		}
		MRFILES.close();
		MRCOLS.close();
		return tables;
	}
	
	/**
	 * Create a set of tables that from an XML file that matches the schema DatabaseDefinition.xsd
	 */
	public List<TableDefinition> loadTableDefinitionsFromXML(InputStream is) throws Exception
	{
		SAXBuilder builder = new SAXBuilder();
		Document d = builder.build(is);
		Element root = d.getRootElement();

		ArrayList<TableDefinition> tables = new ArrayList<>();
		for (Element table : root.getChildren())
		{
			TableDefinition td = new TableDefinition(table.getAttributeValue("name"));
			for (Element column : table.getChildren())
			{
				Integer size = null;
				if (column.getAttributeValue("size") != null)
				{
					size = Integer.parseInt(column.getAttributeValue("size"));
				}
				Boolean allowNull = null;
				if (column.getAttributeValue("allowNull") != null)
				{
					allowNull = Boolean.valueOf(column.getAttributeValue("allowNull"));
				}
				td.addColumn(new ColumnDefinition(column.getAttributeValue("name"), new DataType(column.getAttributeValue("type"), size, allowNull)));
			}
			tables.add(td);
			createTable(td);
		}
		is.close();
		return tables;
	}
	
	public void loadDataIntoTable(TableDefinition td, UMLSFileReader data, Collection<String> SABFilterList) throws SQLException, IOException
	{
		ConsoleUtil.println("Loading table " + td.getTableName());
		StringBuilder insert = new StringBuilder();
		insert.append("INSERT INTO ");
		String tableName = td.getTableName();
		if (tableName.indexOf('/') > 0)
		{
			tableName = tableName.substring(tableName.indexOf('/') + 1);
		}
		insert.append(tableName);
		insert.append("(");
		for (ColumnDefinition cd : td.getColumns())
		{
			insert.append(cd.getColumnName());
			insert.append(",");
		}
		insert.setLength(insert.length() - 1);
		insert.append(") VALUES (");
		for (int i = 0; i < td.getColumns().size(); i++)
		{
			insert.append("?,");
		}
		insert.setLength(insert.length() - 1);
		insert.append(")");

		PreparedStatement ps = connection_.prepareStatement(insert.toString());
		
		int sabFilterColumn = -1;
		HashSet<String> sabHashSet = null;
		if (SABFilterList != null && SABFilterList.size() > 0)
		{
			sabHashSet = new HashSet<>(SABFilterList);
			int pos = 0;
			//Find the SAB column in this table, if it has one.
			for (ColumnDefinition cd : td.getColumns())
			{
				if (cd.getColumnName().equals("SAB"))
				{
					sabFilterColumn = pos;
					break;
				}
				pos++;
			}
		}
		

		int rowCount = 0;
		int sabSkipCount = 0;
		HashSet<String> skippedSabs = new HashSet<>();
		while (data.hasNextRow())
		{
			List<String> cols = data.getNextRow();
			if (cols.size() != td.getColumns().size())
			{
				throw new RuntimeException("Data length mismatch!");
			}
			
			if (sabFilterColumn >= 0)
			{
				if (!sabHashSet.contains(cols.get(sabFilterColumn)))
				{
					skippedSabs.add(cols.get(sabFilterColumn));
					sabSkipCount++;
					continue;
				}
			}

			ps.clearParameters();
			int psIndex = 1;
			
			for (String s : cols)
			{
				DataType colType = td.getColumns().get(psIndex - 1).getDataType();
				if (colType.isBoolean())
				{
					if (s == null || s.length() == 0)
					{
						ps.setNull(psIndex, Types.BOOLEAN);
					}
					else
					{
						ps.setBoolean(psIndex, Boolean.valueOf(s));
					}
				}
				else if (colType.isInteger())
				{
					if (s == null || s.length() == 0)
					{
						ps.setNull(psIndex, Types.INTEGER);
					}
					else
					{
						ps.setInt(psIndex, Integer.parseInt(s));
					}
				}
				else if (colType.isLong())
				{
					if (s == null || s.length() == 0)
					{
						ps.setNull(psIndex, Types.BIGINT);
					}
					else
					{
						ps.setLong(psIndex, Long.parseLong(s));
					}
				}
				else if (colType.isString())
				{
					if (s == null || s.length() == 0)
					{
						ps.setNull(psIndex, Types.VARCHAR);
					}
					else
					{
						ps.setString(psIndex, s);
					}
				}
				else if (colType.isBigDecimal())
				{
					if (s == null || s.length() == 0)
					{
						ps.setNull(psIndex, Types.DECIMAL);
					}
					else
					{
						ps.setBigDecimal(psIndex, new BigDecimal(s));
					}
				}
				else
				{
					throw new RuntimeException("Unsupported data type");
				}
				psIndex++;
			}
			ps.execute();
			rowCount++;
			if (rowCount % 10000 == 0)
			{
				ConsoleUtil.showProgress();
			}
		}
		ps.close();
		data.close();
		ConsoleUtil.println("Loaded " + rowCount + " rows");
		if (sabSkipCount > 0)
		{
			ConsoleUtil.println("Skipped " + sabSkipCount+ " rows for not matching the SAB filter - " + Arrays.toString(skippedSabs.toArray(new String[] {})));
		}
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException
	{
		RRFDatabaseHandle rrf = new RRFDatabaseHandle();
		rrf.createOrOpenDatabase(new File("/mnt/SSD/scratch/h2Test"));
		
		TableDefinition td = new TableDefinition("Test");
		td.addColumn(new ColumnDefinition("testcol", new DataType("STRING", 50, true)));
		
		rrf.createTable(td);
		
		rrf.shutdown();
	}
}
