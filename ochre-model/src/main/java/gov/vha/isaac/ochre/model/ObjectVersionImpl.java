/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model;

import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.IdentifiedStampedVersion;
import gov.vha.isaac.ochre.api.chronicle.MutableStampedVersion;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author kec
 * @param <C>
 * @param <V>
 */
public class ObjectVersionImpl<C extends ObjectChronologyImpl<V>, V extends ObjectVersionImpl> 
    implements MutableStampedVersion, IdentifiedStampedVersion {
    private static CommitService commitManager;
    
    protected static CommitService getCommitService() {
        if (commitManager == null) {
            commitManager = LookupService.getService(CommitService.class);
        }
        return commitManager;
    }
    
    private static IdentifierService identifierService;
    protected static IdentifierService getIdentifierService() {
        if (identifierService == null) {
            identifierService = LookupService.getService(IdentifierService.class);
        }
        return identifierService;
    }
    
    protected final C chronicle;   
    private int stampSequence;
    private final short versionSequence;

    public ObjectVersionImpl(C chronicle, int stampSequence, short versionSequence) {
        this.chronicle = chronicle;
        this.stampSequence = stampSequence;
        this.versionSequence = versionSequence;
    }

    protected void writeVersionData(DataBuffer data) {
         data.putInt(stampSequence);
         data.putShort(versionSequence);
    }

    public short getVersionSequence() {
        return versionSequence;
    }
        
    @Override
    public int getStampSequence() {
        return stampSequence;
    }

    @Override
    public State getState() {
        return getCommitService().getStatusForStamp(stampSequence);
    }

    @Override
    public long getTime() {
        return getCommitService().getTimeForStamp(stampSequence);
    }

    @Override
    public int getAuthorSequence() {
       return getCommitService().getAuthorSequenceForStamp(stampSequence);
    }

    @Override
    public int getModuleSequence() {
        return getCommitService().getModuleSequenceForStamp(stampSequence);
    }

    @Override
    public int getPathSequence() {
        return getCommitService().getPathSequenceForStamp(stampSequence);
    }

    @Override
    public void setTime(long time) {
        checkUncommitted();
        this.stampSequence = getCommitService().getStampSequence(getState(), 
                time, 
                getAuthorSequence(), 
                getModuleSequence(), 
                getPathSequence());
    }

    @Override
    public void setAuthorSequence(int authorSequence) {
        checkUncommitted();
        this.stampSequence = getCommitService().getStampSequence(getState(), 
                getTime(), 
                authorSequence, 
                getModuleSequence(), 
                getPathSequence());
    }

    @Override
    public void setModuleSequence(int moduleSequence) {
        checkUncommitted();
        this.stampSequence = getCommitService().getStampSequence(getState(), 
                getTime(), 
                getAuthorSequence(), 
                moduleSequence, 
                getPathSequence());
    }

    @Override
    public void setPathSequence(int pathSequence) {
        checkUncommitted();
        this.stampSequence = getCommitService().getStampSequence(getState(), 
                getTime(), 
                getAuthorSequence(), 
                getModuleSequence(), 
                pathSequence);
    }

    protected void checkUncommitted() throws RuntimeException {
        if (!this.isUncommitted()) {
            throw new RuntimeException("Component is already committed");
        }
    }

    @Override
    public boolean isUncommitted() {
        return this.getTime() == Long.MAX_VALUE;
    }

    @Override
    public CommitStates getCommitState() {
        if (isUncommitted()) {
            return CommitStates.UNCOMMITTED;
        }
        return CommitStates.COMMITTED;
    }
    
    
    public StringBuilder toString(StringBuilder builder) {
        builder.append(", stampSequence=")
                .append(stampSequence)
                .append(" ")
                .append(getCommitService().describeStampSequence(stampSequence));
        return builder;
    }

    

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    @Override
    public String toUserString() {
        return toString();
    }

    @Override
    public int getNid() {
        return chronicle.getNid();
    }

    @Override
    public UUID getPrimordialUuid() {
        return chronicle.getPrimordialUuid();
    }

    @Override
    public List<UUID> getUuidList() {
        return chronicle.getUuidList();
    }

}
