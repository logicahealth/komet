package org.ihtsdo.otf.tcc.model.cc.concept;

import java.io.*;

public class NidDataInMemory implements ConceptDataFetcherI {

    private byte[] readWriteBytes;

    public NidDataInMemory(byte[] readWriteBytes) {
        super();
        this.readWriteBytes = readWriteBytes.clone();
    }

    public NidDataInMemory(InputStream is) throws IOException {
        super();
        DataInputStream dis = new DataInputStream(is);
        readWriteBytes = new byte[dis.readInt()];
        dis.readFully(readWriteBytes);
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
        return readWriteBytes == null;
    }

    @Override
    public void reset() {
        // nothing to do...
    }
}
