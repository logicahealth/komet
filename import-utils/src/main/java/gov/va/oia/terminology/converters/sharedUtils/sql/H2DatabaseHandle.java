package gov.va.oia.terminology.converters.sharedUtils.sql;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import gov.va.oia.terminology.converters.sharedUtils.ConsoleUtil;

public class H2DatabaseHandle
{

	protected Connection connection_;

	public H2DatabaseHandle()
	{
		super();
	}

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
	 * @returns rowCount loaded
	 */
	public int loadDataIntoTable(TableDefinition td, TerminologyFileReader data) throws SQLException, IOException
	{
		return loadDataIntoTable(td, data, null, null);
	}
	
	/**
	 * @param td
	 * @param data
	 * @param includeValuesColumnName - (optional) the name of the column to check for an include values filter 
	 * @param includeValues - (optional) - the values to include.  If this parameter, and the above parameter are specified, only rows which have 
	 * a column name that matches 'includeValuesColumnName' with a value from the set of 'includeValues" will be loaded.
	 * @throws SQLException
	 * @throws IOException
	 * @return row count loaded
	 */
	public int loadDataIntoTable(TableDefinition td, TerminologyFileReader data, String includeValuesColumnName, Collection<String> includeValues) throws SQLException, IOException
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
		
		int filterColumn = -1;
		HashSet<String> sabHashSet = null;
		if (includeValues != null && includeValues.size() > 0 && includeValuesColumnName != null)
		{
			sabHashSet = new HashSet<>(includeValues);
			int pos = 0;
			//Find the skip column in this table, if it has one.
			for (ColumnDefinition cd : td.getColumns())
			{
				if (cd.getColumnName().equalsIgnoreCase(includeValuesColumnName))
				{
					filterColumn = pos;
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
			
			if (filterColumn >= 0)
			{
				if (!sabHashSet.contains(cols.get(filterColumn)))
				{
					skippedSabs.add(cols.get(filterColumn));
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
						ps.setBoolean(psIndex, (s.equalsIgnoreCase("true") || s.equals("1")));
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
			ConsoleUtil.println("Skipped " + sabSkipCount+ " rows for not matching the include filter - " + Arrays.toString(skippedSabs.toArray(new String[] {})));
		}
		return rowCount;
	}

}