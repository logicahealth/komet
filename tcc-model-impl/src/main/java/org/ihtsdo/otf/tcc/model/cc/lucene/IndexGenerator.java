package org.ihtsdo.otf.tcc.chronicle.cc.lucene;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;

public abstract class IndexGenerator implements ProcessUnfetchedConceptDataBI {

    protected IndexWriter writer;
    protected NativeIdSetBI nidSet;
    protected int lineCounter = 0;

    @Override
    public NativeIdSetBI getNidSet() {
        return nidSet;
    }

    public IndexGenerator(IndexWriter writer) throws IOException {
        super();
        this.writer = writer;
        this.nidSet = P.s.getAllConceptNids();
    }

    @Override
    public boolean continueWork() {
        return true;
    }
}
