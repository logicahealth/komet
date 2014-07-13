package org.ihtsdo.otf.tcc.model.cc.concept;

import java.io.*;

public class NidDataInMemory implements ConceptDataFetcherI {

    private byte[] readOnlyBytes;
    private byte[] readWriteBytes;

    public NidDataInMemory(byte[] readOnlyBytes, byte[] readWriteBytes) {
        super();
        this.readOnlyBytes = readOnlyBytes.clone();
        this.readWriteBytes = readWriteBytes.clone();
    }

    public NidDataInMemory(InputStream is) throws IOException {
        super();
        DataInputStream dis = new DataInputStream(is);
        readOnlyBytes = new byte[dis.readInt()];
        dis.readFully(readOnlyBytes);
        readWriteBytes = new byte[dis.readInt()];
        dis.readFully(readWriteBytes);
    }

    @Override
    public byte[] getReadOnlyBytes() {
        return readOnlyBytes;
    }

    @Override
    public DataInputStream getReadOnlyDataStream() {
        return new DataInputStream(new ByteArrayInputStream(getReadOnlyBytes()));
    }

    @Override
    public byte[] getMutableBytes() {
        return readWriteBytes;
    }

    @Override
    public DataInputStream getMutableInputStream() {
        return new DataInputStream(new ByteArrayInputStream(getMutableBytes()));
    }

    @Override
    public boolean isPrimordial() throws IOException {
        return readOnlyBytes != null || readWriteBytes != null;
    }

    @Override
    public void reset() {
        // nothing to do...
    }
}
