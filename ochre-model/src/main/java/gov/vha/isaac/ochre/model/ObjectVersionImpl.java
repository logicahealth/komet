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

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.MutableStampedVersion;
import gov.vha.isaac.ochre.api.commit.CommitManager;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author kec
 * @param <C>
 * @param <V>
 */
public class ObjectVersionImpl<C extends ObjectChronicleImpl<V>, V extends ObjectVersionImpl> implements MutableStampedVersion {
    private static CommitManager commitManager;
    
    private static CommitManager getCommitManager() {
        if (commitManager == null) {
            commitManager = LookupService.getService(CommitManager.class);
        }
        return commitManager;
    }
    
    protected final C chronicle;   
    private int stampSequence;

    public ObjectVersionImpl(C chronicle, int stampSequence) {
        this.chronicle = chronicle;
        this.stampSequence = stampSequence;
    }
    
    public ObjectVersionImpl(C chronicle, 
            State status, 
            long time,
            int authorSequence,
            int moduleSequence,
            int pathSequence) {
        this.chronicle = chronicle;
        this.stampSequence = getCommitManager().getStamp(status, time, 
                authorSequence, moduleSequence, pathSequence);
    }
    
    protected void writeVersionData(DataBuffer data) {
         data.putInt(stampSequence);
    }
        
    @Override
    public int getStampSequence() {
        return stampSequence;
    }

    @Override
    public State getState() {
        return getCommitManager().getStatusForStamp(stampSequence);
    }

    @Override
    public long getTime() {
        return getCommitManager().getTimeForStamp(stampSequence);
    }

    @Override
    public int getAuthorSequence() {
       return getCommitManager().getAuthorSequenceForStamp(stampSequence);
    }

    @Override
    public int getModuleSequence() {
        return getCommitManager().getModuleSequenceForStamp(stampSequence);
    }

    @Override
    public int getPathSequence() {
        return getCommitManager().getPathSequenceForStamp(stampSequence);
    }

    @Override
    public int getNid() {
        return chronicle.getNid();
    }

    @Override
    public int getContainerSequence() {
        return chronicle.getContainerSequence();
    }

    @Override
    public void setState(State state) {
        checkUncommitted();
        this.stampSequence = getCommitManager().getStamp(state, 
                getTime(), 
                getAuthorSequence(), 
                getModuleSequence(), 
                getPathSequence());
    }

    @Override
    public void setTime(long time) {
        checkUncommitted();
        this.stampSequence = getCommitManager().getStamp(getState(), 
                time, 
                getAuthorSequence(), 
                getModuleSequence(), 
                getPathSequence());
    }

    @Override
    public void setAuthorSequence(int authorSequence) {
        checkUncommitted();
        this.stampSequence = getCommitManager().getStamp(getState(), 
                getTime(), 
                authorSequence, 
                getModuleSequence(), 
                getPathSequence());
    }

    @Override
    public void setModuleSequence(int moduleSequence) {
        checkUncommitted();
        this.stampSequence = getCommitManager().getStamp(getState(), 
                getTime(), 
                getAuthorSequence(), 
                moduleSequence, 
                getPathSequence());
    }

    @Override
    public void setPathSequence(int pathSequence) {
        checkUncommitted();
        this.stampSequence = getCommitManager().getStamp(getState(), 
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
    public UUID getPrimordialUuid() {
        return chronicle.getPrimordialUuid();
    }

    @Override
    public List<UUID> getUUIDs() {
        return chronicle.getUUIDs();
    }
    
}
