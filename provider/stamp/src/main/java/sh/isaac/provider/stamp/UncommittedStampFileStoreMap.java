package sh.isaac.provider.stamp;

import sh.isaac.api.commit.UncommittedStamp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UncommittedStampFileStoreMap extends FileStoreMapData<Integer, UncommittedStamp> {
    public UncommittedStampFileStoreMap(File dataFile) throws IOException {
        super(dataFile);
    }

    @Override
    protected Map.Entry<Integer, UncommittedStamp> readEntry(DataInputStream dis) throws IOException {
        Integer key = dis.readInt();
        UncommittedStamp uncommittedStamp = new UncommittedStamp(dis);
        return Map.entry(key, uncommittedStamp);
    }

    @Override
    protected void writeEntry(Map.Entry<Integer, UncommittedStamp> entry, DataOutputStream dos) throws IOException {
        dos.writeInt(entry.getKey());
        entry.getValue().write(dos);
    }
}
