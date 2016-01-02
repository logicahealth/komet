package gov.va.oia.terminology.converters.sharedUtils.sql;

public class ColumnDefinition
{
	private String columnName_;
	private DataType dataType_;
	
	public ColumnDefinition(String columnName, DataType dataType)
	{
		columnName_ = columnName;
		dataType_ = dataType;
	}
	
	public DataType getDataType()
	{
		return dataType_;
	}
	
	public String getColumnName()
	{
		return columnName_;
	}
	
	public String asH2()
	{
		return columnName_ + " " + dataType_.asH2();
	}
}
