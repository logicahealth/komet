package org.ihtsdo.otf.tcc.chronicle.cc.concept;

import java.io.IOException;

import com.sleepycat.bind.tuple.TupleInput;

public interface ConceptDataFetcherI {

    byte[] getReadOnlyBytes() throws IOException;

    byte[] getReadWriteBytes() throws IOException;

    TupleInput getReadOnlyTupleInput() throws IOException;

    TupleInput getMutableTupleInput() throws IOException;

    boolean isPrimordial() throws IOException;

    void reset();
}