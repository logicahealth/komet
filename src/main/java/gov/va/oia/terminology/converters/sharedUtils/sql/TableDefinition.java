package gov.va.oia.terminology.converters.sharedUtils.sql;

import java.util.ArrayList;
import java.util.List;

public class TableDefinition
{
	private String tableName_;
	private ArrayList<ColumnDefinition> columns_ = new ArrayList<>();
	
	public TableDefinition(String tableName)
	{
		tableName_ = tableName;
	}
	
	public void addColumn(ColumnDefinition cd)
	{
		columns_.add(cd);
	}
	
	public String getTableName()
	{
		return tableName_;
	}
	public List<ColumnDefinition> getColumns()
	{
		return columns_;
	}
}
