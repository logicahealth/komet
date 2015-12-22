package gov.va.oia.terminology.converters.sharedUtils.sql;

public class DataType
{
	public enum SUPPORTED_DATA_TYPE {STRING, INTEGER, LONG, BOOLEAN, BIGDECIMAL;

	public static SUPPORTED_DATA_TYPE parse(String value)
	{
		for (SUPPORTED_DATA_TYPE s : SUPPORTED_DATA_TYPE.values())
		{
			if (value.toUpperCase().equals(s.name())) {
				return s;
			}
		}
		throw new RuntimeException("Unknown type " + value);
	}};

	private SUPPORTED_DATA_TYPE type_;
	private int dataSize_ = -1;
	private int scale_ = -1;
	private boolean allowsNull_;
	
	public DataType(String sql92Type, Boolean allowsNull)
	{
		if (sql92Type.startsWith("varchar"))
		{
			type_ = SUPPORTED_DATA_TYPE.STRING;
		}
		else if (sql92Type.startsWith("numeric"))
		{
			type_ = SUPPORTED_DATA_TYPE.BIGDECIMAL;
		}
		else if (sql92Type.startsWith("integer"))
		{
			type_ = SUPPORTED_DATA_TYPE.INTEGER;
		}
		else if (sql92Type.startsWith("char"))
		{
			type_ = SUPPORTED_DATA_TYPE.STRING;
		}
		else
		{
			throw new RuntimeException("Not yet mapped - " + sql92Type);
		}
		
		int index = sql92Type.indexOf('(');
		if (index > 0 && type_ == SUPPORTED_DATA_TYPE.STRING)
		{
			dataSize_ = Integer.parseInt(sql92Type.substring((index + 1), sql92Type.indexOf(')', index)));
		}
		if (index > 0 && type_ == SUPPORTED_DATA_TYPE.BIGDECIMAL)
		{
			int commaPos = sql92Type.indexOf(',', index);
			if (commaPos > 0)
			{	
				dataSize_ = Integer.parseInt(sql92Type.substring(index + 1, commaPos));
				scale_ = Integer.parseInt(sql92Type.substring((commaPos + 1), sql92Type.indexOf(')', commaPos)));
			}
			else
			{
				dataSize_ = Integer.parseInt(sql92Type.substring((index + 1), sql92Type.indexOf(')', index)));
			}
		}
		
		if (allowsNull == null) 
		{
			allowsNull_ = true;
		}
		else
		{
			allowsNull_ = allowsNull.booleanValue();
		}
	}
	

	public DataType(SUPPORTED_DATA_TYPE type, Integer size, Boolean allowsNull)
	{
		type_ = type;
		
		if (size != null)
		{
			dataSize_ = size;
		}
		
		if (allowsNull == null) 
		{
			allowsNull_ = true;
		}
		else
		{
			allowsNull_ = allowsNull.booleanValue();
		}
	}
	
	public boolean isString()
	{
		return type_ == SUPPORTED_DATA_TYPE.STRING;
	}
	
	public boolean isBoolean()
	{
		return type_ == SUPPORTED_DATA_TYPE.BOOLEAN;
	}
	
	public boolean isInteger()
	{
		return type_ == SUPPORTED_DATA_TYPE.INTEGER;
	}
	
	public boolean isLong()
	{
		return type_ == SUPPORTED_DATA_TYPE.LONG;
	}
	
	public boolean isBigDecimal()
	{
		return type_ == SUPPORTED_DATA_TYPE.BIGDECIMAL;
	}
	
	public String asH2()
	{
		StringBuilder sb = new StringBuilder();
		if (type_ == SUPPORTED_DATA_TYPE.STRING)
		{
			sb.append("VARCHAR ");
			if (dataSize_ > 0)
			{
				sb.append("(" + dataSize_ + ") ");
			}
		}
		else if (type_ == SUPPORTED_DATA_TYPE.INTEGER)
		{
			sb.append("INT ");
		}
		else if (type_ == SUPPORTED_DATA_TYPE.LONG)
		{
			sb.append("BIGINT ");
		}
		else if (type_ == SUPPORTED_DATA_TYPE.BOOLEAN)
		{
			sb.append("BOOLEAN ");
		}
		else if (type_ == SUPPORTED_DATA_TYPE.BIGDECIMAL)
		{
			if (scale_ > 0)
			{
				sb.append("NUMERIC (" + dataSize_ + ", " + scale_ + ") ");
			}
			else
			{
				sb.append("DECIMAL (" + dataSize_ + ") ");
			}
		}
		else
		{
			throw new RuntimeException("not implemented");
		}
		
		if (!allowsNull_)
		{
			sb.append("NOT NULL");
		}
		return sb.toString();
	}
}
