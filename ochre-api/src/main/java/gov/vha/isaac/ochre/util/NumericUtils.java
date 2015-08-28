package gov.vha.isaac.ochre.util;

import java.math.BigDecimal;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDoubleBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloatBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeIntegerBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLongBI;

public class NumericUtils 
{
	public static Number parseUnknown(String value) throws NumberFormatException
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch (Exception e)
		{
			//noop
		}
		try
		{
			return Long.parseLong(value);
		}
		catch (Exception e)
		{
			//noop
		}
		try
		{
			return Float.parseFloat(value);
		}
		catch (Exception e)
		{
			//noop
		}
		return Double.parseDouble(value);
	}

	public static Number readNumber(DynamicSememeDataBI value) throws NumberFormatException
	{
		if (value instanceof DynamicSememeDoubleBI)
		{
			return Double.valueOf(((DynamicSememeDoubleBI) value).getDataDouble());
		}
		else if (value instanceof DynamicSememeFloatBI)
		{
			return Float.valueOf(((DynamicSememeFloatBI) value).getDataFloat());
		}
		else if (value instanceof DynamicSememeIntegerBI)
		{
			return Integer.valueOf(((DynamicSememeIntegerBI) value).getDataInteger());
		}
		else if (value instanceof DynamicSememeLongBI)
		{
			return Long.valueOf(((DynamicSememeLongBI) value).getDataLong());
		}
		else
		{
			throw new NumberFormatException("The value passed in to the validator is not a number");
		}
	}

	public static int compare(final Number x, final Number y)
	{
		if (isSpecial(x) || isSpecial(y))
		{
			return Double.compare(x.doubleValue(), y.doubleValue());
		}
		else
		{
			return toBigDecimal(x).compareTo(toBigDecimal(y));
		}
	}

	private static boolean isSpecial(final Number x)
	{
		boolean specialDouble = x instanceof Double && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
		boolean specialFloat = x instanceof Float && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));
		return specialDouble || specialFloat;
	}

	public static BigDecimal toBigDecimal(final Number number) throws NumberFormatException
	{
		if (number instanceof Integer || number instanceof Long)
		{
			return new BigDecimal(number.longValue());
		}
		else if (number instanceof Float || number instanceof Double)
		{
			return new BigDecimal(number.doubleValue());
		}
		else
		{
			throw new NumberFormatException("Unexpected data type passed in to toBigDecimal (" + number.getClass() + ")");
		}
	}
}
