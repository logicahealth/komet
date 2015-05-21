package gov.va.oia.terminology.converters.sharedUtils.umlsUtils.sql;

public class DataType
{
	private static short STRING = 0;
	private static short INTEGER = 1;
	private static short LONG = 2;
	private static short BOOLEAN = 3;
	private static short BIGDECIMAL= 4;

	private short type_;
	private int dataSize_ = -1;
	private int scale_ = -1;
	private boolean allowsNull_;
	
	public DataType(String sql92Type, Boolean allowsNull)
	{
		if (sql92Type.startsWith("varchar"))
		{
			type_ = STRING;
		}
		else if (sql92Type.startsWith("numeric"))
		{
			type_ = BIGDECIMAL;
		}
		else if (sql92Type.startsWith("integer"))
		{
			type_ = INTEGER;
		}
		else if (sql92Type.startsWith("char"))
		{
			type_ = STRING;
		}
		else
		{
			throw new RuntimeException("Not yet mapped - " + sql92Type);
		}
		
		int index = sql92Type.indexOf('(');
		if (index > 0 && type_ == STRING)
		{
			dataSize_ = Integer.parseInt(sql92Type.substring((index + 1), sql92Type.indexOf(')', index)));
		}
		if (index > 0 && type_ == BIGDECIMAL)
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
	
	/**
	 * Takes constants: STRING,INTEGER, LONG, BOOLEAN, BIGDECIMAL
	 */
	public DataType(String type, Integer size, Boolean allowsNull)
	{
		if (type.equals("STRING"))
		{
			type_ = STRING;
		}
		else if (type.equals("INTEGER"))
		{
			type_ = INTEGER;
		}
		else if (type.equals("LONG"))
		{
			type_ = LONG;
		}
		else if (type.equals("BOOLEAN"))
		{
			type_ = BOOLEAN;
		}
		else if (type.equals("BIGDECIMAL"))
		{
			type_ = BIGDECIMAL;
		}
		else
		{
			throw new RuntimeException("oops");
		}
		
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
		return type_ == STRING;
	}
	
	public boolean isBoolean()
	{
		return type_ == BOOLEAN;
	}
	
	public boolean isInteger()
	{
		return type_ == INTEGER;
	}
	
	public boolean isLong()
	{
		return type_ == LONG;
	}
	
	public boolean isBigDecimal()
	{
		return type_ == BIGDECIMAL;
	}
	
	public String asH2()
	{
		StringBuilder sb = new StringBuilder();
		if (type_ == STRING)
		{
			sb.append("VARCHAR ");
			sb.append("(" + dataSize_ + ") ");
		}
		else if (type_ == INTEGER)
		{
			sb.append("INT ");
		}
		else if (type_ == LONG)
		{
			sb.append("BIGINT ");
		}
		else if (type_ == BOOLEAN)
		{
			sb.append("BOOLEAN ");
		}
		else if (type_ == BIGDECIMAL)
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
