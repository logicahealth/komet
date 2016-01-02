/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.api.collections.StampSequenceSet;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
import java.time.Instant;
import org.apache.mahout.math.map.AbstractIntIntMap;
import org.apache.mahout.math.map.OpenIntIntHashMap;

/**
 * Used to notify listeners of a commit event. 
 * @author kec
 */
public class CommitRecord  {

    protected Instant commitTime;
    protected StampSequenceSet stampsInCommit;
    protected AbstractIntIntMap stampAliases;
    protected String commitComment;
    protected ConceptSequenceSet conceptsInCommit;
    protected SememeSequenceSet sememesInCommit;

    public CommitRecord() {
    }

    public CommitRecord(Instant commitTime,
            StampSequenceSet stampsInCommit,
            OpenIntIntHashMap stampAliases,
            ConceptSequenceSet conceptsInCommit,
            SememeSequenceSet sememesInCommit,
            String commitComment) {
        this.commitTime = commitTime;
        this.stampsInCommit = StampSequenceSet.of(stampsInCommit);
        this.stampAliases = stampAliases.copy();
        this.conceptsInCommit = ConceptSequenceSet.of(conceptsInCommit);
        this.sememesInCommit = SememeSequenceSet.of(sememesInCommit);
        this.commitComment = commitComment;
    }

    public Instant getCommitTime() {
        return commitTime;
    }

    public StampSequenceSet getStampsInCommit() {
        return stampsInCommit;
    }

    public AbstractIntIntMap getStampAliases() {
        return stampAliases;
    }

    public String getCommitComment() {
        return commitComment;
    }

    public ConceptSequenceSet getConceptsInCommit() {
        return conceptsInCommit;
    }

    public SememeSequenceSet getSememesInCommit() {
        return sememesInCommit;
    }

    @Override
    public String toString() {
        return "CommitRecord{" + "commitTime=" + commitTime + ", stampsInCommit=" + stampsInCommit + ", stampAliases=" + stampAliases + ", commitComment=" + commitComment + ", conceptsInCommit=" + conceptsInCommit + ", sememesInCommit=" + sememesInCommit + '}';
    }

}
