package org.ihtsdo.otf.tcc.chronicle.cc.concept;

import java.io.IOException;

import com.sleepycat.bind.tuple.TupleInput;
import java.io.DataInputStream;
import java.io.InputStream;

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
    public TupleInput getReadOnlyTupleInput() {
        return new TupleInput(getReadOnlyBytes());
    }

    @Override
    public byte[] getReadWriteBytes() {
        return readWriteBytes;
    }

    @Override
    public TupleInput getMutableTupleInput() {
        return new TupleInput(getReadWriteBytes());
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
