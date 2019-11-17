package sh.isaac.provider.stamp;

import sh.isaac.api.commit.Stamp;
import sh.isaac.api.commit.UncommittedStamp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class StampFileStoreMap extends FileStoreMapData<Integer, Stamp> {
    public StampFileStoreMap(File dataFile) throws IOException {
        super(dataFile);
    }

    @Override
    protected Map.Entry<Integer, Stamp> readEntry(DataInputStream dis) throws IOException {
        Integer key = dis.readInt();
        Stamp stamp = new Stamp(dis);
        return Map.entry(key, stamp);
    }

    @Override
    protected void writeEntry(Map.Entry<Integer, Stamp> entry, DataOutputStream dos) throws IOException {
        dos.writeInt(entry.getKey());
        entry.getValue().write(dos);
    }
}
