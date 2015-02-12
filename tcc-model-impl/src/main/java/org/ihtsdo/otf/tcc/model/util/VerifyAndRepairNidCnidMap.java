package org.ihtsdo.otf.tcc.model.util;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ProcessComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.model.cc.termstore.PersistentStoreI;

import java.io.IOException;

/**
 * Created by kec on 12/1/14.
 */
public class VerifyAndRepairNidCnidMap implements ProcessUnfetchedConceptDataBI, ProcessComponentChronicleBI {

    PersistentStoreI store;
    private int cNid;
    public int errorCount = 0;

    public VerifyAndRepairNidCnidMap(PersistentStoreI store) {
        this.store = store;
    }

    @Override
    public boolean allowCancel() {
        return false;
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        ConceptChronicleBI conceptChronicle = fetcher.fetch();
        this.cNid = cNid;
        conceptChronicle.processComponentChronicles(this);
    }

    @Override
    public void process(ComponentChronicleBI cc) throws Exception {
        if (store.getConceptNidForNid(cc.getNid()) != cNid) {
            errorCount++;
            store.resetConceptNidForNid(cNid, cc.getNid());
        }
    }

    @Override
    public NativeIdSetBI getNidSet() throws IOException {
        return null;
    }

    @Override
    public String getTitle() {
        return "Verify and Repair Nid->Cnid Map";
    }

    @Override
    public boolean continueWork() {
        return true;
    }

}
