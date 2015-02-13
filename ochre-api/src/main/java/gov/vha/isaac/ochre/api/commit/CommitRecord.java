/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.commit;

import java.time.Instant;
import org.apache.mahout.math.map.AbstractIntIntMap;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.apache.mahout.math.set.AbstractIntSet;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author kec
 */
public class CommitRecord {
    protected Instant commitTime;
    protected AbstractIntSet stampsInCommit;
    protected AbstractIntIntMap stampAliases;
    protected String commitComment;
    
    public CommitRecord() {}

    public CommitRecord(Instant commitTime, 
            OpenIntHashSet stampsInCommit, 
            OpenIntIntHashMap stampAliases, 
            String commitComment) {
        this.commitTime = commitTime;
        this.stampsInCommit = stampsInCommit.copy();
        this.stampAliases = stampAliases.copy();
        this.commitComment = commitComment;
    }

    public Instant getCommitTime() {
        return commitTime;
    }

    public AbstractIntSet getStampsInCommit() {
        return stampsInCommit;
    }

    public AbstractIntIntMap getStampAliases() {
        return stampAliases;
    }

    public String getCommitComment() {
        return commitComment;
    }

}
