package org.ihtsdo.otf.tcc.dto.component.refexDynamic;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;

public class TtkRefexDynamicMemberChronicle extends 
        TtkComponentChronicle<TtkRefexDynamicRevision, StampedVersion>
{
	public static final long serialVersionUID = 1;

	@XmlAttribute public UUID componentUuid;
	@XmlAttribute public UUID refexAssemblageUuid;
	//TODO (artf231861) [REFEX] the XML tags are not yet tested - may not be correct
	@XmlElement private TtkRefexDynamicData[] data_;

	public TtkRefexDynamicMemberChronicle()
	{
		super();
	}

	public TtkRefexDynamicMemberChronicle(SememeChronologyImpl<DynamicSememeImpl> another)
	{
		super(another);
		
		this.componentUuid = Get.identifierService().getUuidPrimordialForNid(another.getReferencedComponentNid()).get();
		this.refexAssemblageUuid = Get.identifierService().getUuidPrimordialForNid(another.getAssemblageSequence()).get();

		Collection<? extends DynamicSememeImpl> refexes = another.getVersions();
		int partCount = refexes.size();
		Iterator<? extends DynamicSememeImpl> itr = refexes.iterator();
		DynamicSememeImpl rv = itr.next();

		this.data_ = new TtkRefexDynamicData[rv.getData().length];
		for (int i = 0; i < data_.length; i++)
		{
			if (rv.getData()[i] == null)
			{
				data_[i] = null;
			}
			else
			{
				data_[i] = TtkRefexDynamicData.typeToClass(rv.getData()[i].getRefexDataType(), rv.getData()[i].getData());
			}
		}

		if (partCount > 1)
		{
			revisions = new ArrayList<>(partCount - 1);

			while (itr.hasNext())
			{
				rv = itr.next();
				revisions.add(new TtkRefexDynamicRevision(rv));
			}
		}
	}

	public TtkRefexDynamicMemberChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException
	{
		super();
		readExternal(in, dataVersion);
	}

	/**
	 * Returns a hash code for this {@code ERefset}.
	 *
	 * @return a hash code value for this {@code ERefset}.
	 */
	@Override
	public int hashCode()
	{
		return this.primordialUuid.hashCode();
	}

	@Override
	public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException
	{
		super.readExternal(in, dataVersion);
		refexAssemblageUuid = new UUID(in.readLong(), in.readLong());
		componentUuid = new UUID(in.readLong(), in.readLong());

		//read the following format - 
		//dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
		int colCount = in.readInt();
		data_ = new TtkRefexDynamicData[colCount];
		for (int i = 0; i < colCount; i++)
		{
			DynamicSememeDataType dt = DynamicSememeDataType.getFromToken(in.readInt());
			if (dt == DynamicSememeDataType.UNKNOWN)
			{
				data_[i] = null;
			}
			else
			{
				int dataLength = in.readInt();
				byte[] data = new byte[dataLength];
				in.readFully(data);
				data_[i] = TtkRefexDynamicData.typeToClass(dt, data);
			}
		}

		int versionSize = in.readInt();

		if (versionSize > 0)
		{
			revisions = new ArrayList<>(versionSize);

			for (int i = 0; i < versionSize; i++)
			{
				revisions.add(new TtkRefexDynamicRevision(in, dataVersion));
			}
		}
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeLong(refexAssemblageUuid.getMostSignificantBits());
		out.writeLong(refexAssemblageUuid.getLeastSignificantBits());
		out.writeLong(componentUuid.getMostSignificantBits());
		out.writeLong(componentUuid.getLeastSignificantBits());

		//dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
		if (getData() != null)
		{
			out.writeInt(getData().length);
			for (TtkRefexDynamicData column : getData())
			{
				if (column == null)
				{
					out.writeInt(DynamicSememeDataType.UNKNOWN.getTypeToken());
				}
				else
				{
					out.writeInt(column.getRefexDataType().getTypeToken());
					out.writeInt(column.getData().length);
					out.write(column.getData());
				}
			}
		}
		else
		{
			out.writeInt(0);
		}

		if (revisions == null)
		{
			out.writeInt(0);
		}
		else
		{
			out.writeInt(revisions.size());

			for (TtkRefexDynamicRevision rmv : revisions)
			{
				rmv.writeExternal(out);
			}
		}
	}

	public UUID getComponentUuid()
	{
		return componentUuid;
	}

	public UUID getRefexAssemblageUuid()
	{
		return refexAssemblageUuid;
	}

	public void setComponentUuid(UUID componentUuid)
	{
		this.componentUuid = componentUuid;
	}

	public void setRefexAssemblageUuid(UUID refexAssemblageUuid)
	{
		this.refexAssemblageUuid = refexAssemblageUuid;
	}

	public TtkRefexDynamicData[] getData()
	{
		return data_;
	}

	public void setData(TtkRefexDynamicData[] data)
	{
		data_ = data;
	}

	/**
	 * Compares this object to the specified object. The result is {@code true} if and only if the argument is not {@code null}, is a
	 * {@code ERefsetLongMember} object, and contains the same values, field by field,
	 * as this {@code ERefsetLongMember}.
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

		if (TtkRefexDynamicMemberChronicle.class.isAssignableFrom(obj.getClass()))
		{
			TtkRefexDynamicMemberChronicle another = (TtkRefexDynamicMemberChronicle) obj;

			// =========================================================
			// Compare properties of 'this' class to the 'another' class
			// =========================================================
			// Compare refsetUuid
			if (!this.refexAssemblageUuid.equals(another.refexAssemblageUuid))
			{
				return false;
			}

			// Compare componentUuid
			if (!this.componentUuid.equals(another.componentUuid))
			{
				return false;
			}
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

		buff.append(" refex:");
		buff.append(informAboutUuid(this.refexAssemblageUuid));
		buff.append(" component:");
		buff.append(informAboutUuid(this.componentUuid));
		buff.append(" ");

		buff.append(Arrays.toString(getData()));
		buff.append(" ");

		buff.append(super.toString());
		return buff.toString();
	}

	@Override
	public List<TtkRefexDynamicRevision> getRevisionList()
	{
		return revisions;
	}

	//TODO Dan hack - whats this?
	@Override
	protected void addUuidReferencesForRevisionComponent(Collection<UUID> references)
	{
		throw new UnsupportedOperationException();
	}
}
