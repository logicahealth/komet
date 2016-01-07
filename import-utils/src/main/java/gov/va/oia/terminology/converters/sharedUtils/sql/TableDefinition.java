package gov.va.oia.terminology.converters.sharedUtils.sql;

import java.util.LinkedHashMap;

public class TableDefinition
{
	private String tableName_;
	private LinkedHashMap<String, ColumnDefinition> columns_ = new LinkedHashMap<>();
	
	public TableDefinition(String tableName)
	{
		tableName_ = tableName;
	}
	
	public void addColumn(ColumnDefinition cd)
	{
		columns_.put(cd.getColumnName().toLowerCase(), cd);
	}
	
	public String getTableName()
	{
		return tableName_;
	}
	public ColumnDefinition[] getColumns()
	{
		return columns_.values().toArray(new ColumnDefinition[columns_.size()]);
	}
	
	public DataType getColDataType(String columnName)
	{
		return columns_.get(columnName.toLowerCase()).getDataType();
	}
}
