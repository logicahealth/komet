package org.ihtsdo.otf.tcc.model.cc.refexDynamic;

import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.model.cc.component.AbstractSerializer;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/13/14.
 */
public class RefexDynamicSerializer extends AbstractSerializer<RefexDynamicMember, RefexDynamicRevision> {
    private static RefexDynamicSerializer singleton;
    public static RefexDynamicSerializer get() {
        if (singleton == null) {
            singleton = new RefexDynamicSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordial(DataOutput output, RefexDynamicMember cc) throws IOException {
        output.writeInt(cc.assemblageNid);
        output.writeInt(cc.referencedComponentNid);
        //Write with the following format -
        //dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
        output.writeInt(cc.getData().length);
        for (RefexDynamicDataBI column : cc.getData())
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

    @Override
    protected void serializeRevision(DataOutput output, RefexDynamicRevision r) throws IOException {
        //Write with the following format -
        //dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
        output.writeInt(r.getData().length);
        for (RefexDynamicDataBI column : r.getData())
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

    @Override
    public void deserializePrimordial(DataInput input, RefexDynamicMember cc) throws IOException {
        cc.assemblageNid = input.readInt();
        cc.referencedComponentNid = input.readInt();
        assert cc.assemblageNid != Integer.MAX_VALUE;
        assert cc.referencedComponentNid != Integer.MAX_VALUE;

        //read the following format -
        //dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
        int colCount = input.readInt();
        cc.data_ = new RefexDynamicDataBI[colCount];
        for (int i = 0; i < colCount; i++)
        {
            RefexDynamicDataType dt = RefexDynamicDataType.getFromToken(input.readInt());
            if (dt == RefexDynamicDataType.UNKNOWN)
            {
                cc.data_[i] = null;
            }
            else
            {
                int dataLength = input.readInt();
                byte[] data = new byte[dataLength];
                input.readFully(data);

                cc.data_[i] = RefexDynamicData.typeToClass(dt, data, cc.assemblageNid, i);
            }
        }
    }

    @Override
    public RefexDynamicRevision newRevision() {
        return new RefexDynamicRevision();
    }

    @Override
    public RefexDynamicMember newComponent() {
        return new RefexDynamicMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, RefexDynamicRevision r) throws IOException {
        int colCount = input.readInt();
        r.data_ = new RefexDynamicDataBI[colCount];
        for (int i = 0; i < colCount; i++) {
            RefexDynamicDataType dt = RefexDynamicDataType.getFromToken(input.readInt());
            if (dt == RefexDynamicDataType.UNKNOWN)
            {
                r.data_[i] = null;
            }
            else
            {
                int dataLength = input.readInt();
                byte[] data = new byte[dataLength];
                input.readFully(data);
                r.data_[i] = RefexDynamicData.typeToClass(dt, data, r.getAssemblageNid(), i);
            }
        }
    }
}
