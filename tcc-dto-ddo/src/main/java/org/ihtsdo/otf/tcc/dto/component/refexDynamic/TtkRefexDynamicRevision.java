package org.ihtsdo.otf.tcc.dto.component.refexDynamic;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlElement;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

public class TtkRefexDynamicRevision extends TtkRevision
{
	public static final long serialVersionUID = 1;
	//TODO (artf231861) [REFEX] the XML tags are not yet tested - may not be correct
	@XmlElement private TtkRefexDynamicData[] data_;

	public TtkRefexDynamicRevision()
	{
		super();
	}

	public TtkRefexDynamicRevision(RefexDynamicVersionBI<?> another) throws IOException
	{
		super(another);
		this.data_ = new TtkRefexDynamicData[another.getData().length];
		for (int i = 0; i < data_.length; i++)
		{
			data_[i] = TtkRefexDynamicData.typeToClass(another.getData()[i].getRefexDataType(), another.getData()[i].getData());
		}
	}

	public TtkRefexDynamicRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException
	{
		super();
		readExternal(in, dataVersion);
	}

	public TtkRefexDynamicRevision(TtkRefexDynamicRevision another, ComponentTransformerBI transformer)
	{
		super(another, transformer);
		this.data_ = another.data_;  //TODO (artf231855) [REFEX] do we need transformer support for the data?  No idea what it is used for.
	}

	//~--- methods -------------------------------------------------------------

	/**
	 * Compares this object to the specified object. The result is {@code true} if and only if the argument is not {@code null}, is a
	 * {@code ERefsetLongVersion} object, and contains the same values, field by field,
	 * as this {@code ERefsetLongVersion}.
	 *
	 * @param obj the object to compare with.
	 * @return {@code true} if the objects are the same; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		if (TtkRefexDynamicRevision.class.isAssignableFrom(obj.getClass()))
		{
			TtkRefexDynamicRevision another = (TtkRefexDynamicRevision) obj;

			// =========================================================
			// Compare properties of 'this' class to the 'another' class
			// =========================================================
			if (!Arrays.deepEquals(this.getData(), another.getData()))
			{
				return false;
			}

			// Compare their parents
			return super.equals(obj);
		}
		return false;
	}

	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder();

		buff.append(this.getClass().getSimpleName()).append(": ");
		buff.append(Arrays.toString(getData()));
		buff.append(" ");
		buff.append(super.toString());

		return buff.toString();
	}

	@Override
	public TtkRefexDynamicRevision makeTransform(ComponentTransformerBI transformer)
	{
		return new TtkRefexDynamicRevision(this, transformer);
	}

	@Override
	public void readExternal(DataInput input, int dataVersion) throws IOException, ClassNotFoundException
	{
		super.readExternal(input, dataVersion);
		//read the following format - 
		//dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
		int colCount = input.readInt();
		data_ = new TtkRefexDynamicData[colCount];
		for (int i = 0; i < colCount; i++)
		{
			RefexDynamicDataType dt = RefexDynamicDataType.getFromToken(input.readInt());
			if (dt == RefexDynamicDataType.UNKNOWN)
			{
				data_[i] = null;
			}
			else
			{
				int dataLength = input.readInt();
				byte[] data = new byte[dataLength];
				input.readFully(data);
				data_[i] = TtkRefexDynamicData.typeToClass(dt, data);
			}
		}
	}

	@Override
	public void writeExternal(DataOutput output) throws IOException
	{
		super.writeExternal(output);
		//dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
		if (getData() != null)
		{
			output.writeInt(getData().length);
			for (TtkRefexDynamicData column : getData())
			{
				if (column == null)
				{
					output.writeInt(RefexDynamicDataType.UNKNOWN.getTypeToken());
				}
				else
				{
					output.writeInt(column.getRefexDataType().getTypeToken());
					output.writeInt(column.getData().length);
					output.write(column.getData());
				}
			}
		}
		else
		{
			output.writeInt(0);
		}
	}

	public TtkRefexDynamicData[] getData()
	{
		return data_;
	}

	public void setData(TtkRefexDynamicData[] data)
	{
		data_ = data;
	}
}
