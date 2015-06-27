/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.model.cc.refex.logic;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionByteArrayConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.logicgraph.LogicGraphAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.logicgraph.LogicGraphVersionBI;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphRevision;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.version.VersionComputer;

/**
 *
 * @author kec
 */
public class LogicGraphMember extends RefexMember<LogicGraphRevision, LogicGraphMember> 
    implements LogicGraphAnalogBI<LogicGraphRevision> {

    private static VersionComputer<RefexMemberVersion<LogicGraphRevision, LogicGraphMember>> computer =
            new VersionComputer<>();
   protected byte[][] logicGraphBytes;

    //~--- constructors --------------------------------------------------------
    public LogicGraphMember() {
        super();
    }

    public LogicGraphMember(TtkLogicGraphMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        LogicalExpressionByteArrayConverter converter = Hk2Looker.get().getService(LogicalExpressionByteArrayConverter.class);
        logicGraphBytes =  converter.convertLogicGraphForm(logicGraphBytes, DataTarget.INTERNAL);

        if (refsetMember.getRevisionList() != null) {
            revisions = new RevisionSet(primordialStamp);

            for (TtkLogicGraphRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new LogicGraphRevision(eVersion, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addRefsetTypeNids(Set<Integer> allNids) {
        // ;
    }
 
    @Override
    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(ComponentProperty.ARRAY_OF_BYTEARRAY, logicGraphBytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (LogicGraphVersionBI.class.equals(obj.getClass())) {
            LogicGraphVersionBI another = (LogicGraphVersionBI) obj;

            return this.nid == another.getNid();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{nid});
    }

    @Override
    public LogicGraphRevision makeAnalog() {
        LogicGraphRevision newR = new LogicGraphRevision(getStatus(), getTime(),
                getAuthorNid(), getModuleNid(), getPathNid(), this);

        return newR;
    }
    
    @Override
    public LogicGraphRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        LogicGraphRevision newR = new LogicGraphRevision(status, time,
                authorNid, moduleNid, pathNid, this);

        addRevision(newR);

        return newR;
    }

    @Override
    protected boolean refexFieldsEqual(ConceptComponent<LogicGraphRevision, LogicGraphMember> obj) {
        if (LogicGraphMember.class.isAssignableFrom(obj.getClass())) {
            LogicGraphMember another = (LogicGraphMember) obj;

            return Arrays.deepEquals(this.logicGraphBytes, another.logicGraphBytes);
        }

        return false;
    }

    @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(LogicGraphVersionBI.class.isAssignableFrom(another.getClass())){
            LogicGraphVersionBI bv = (LogicGraphVersionBI) another;
            return Arrays.deepEquals(this.logicGraphBytes, bv.getLogicGraphBytes());
        }
        return false;
    }

    @Override
    public boolean readyToWriteRefsetMember() {
        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
     buff.append("LG size: ");
      buff.append(this.logicGraphBytes.length);
      for (int i = 0; i < this.logicGraphBytes.length; i++) {
        buff.append(" ").append(i);
        buff.append(": ");
        if(this.logicGraphBytes[i].length == 16){
            buff.append(UuidT5Generator.getUuidFromRawBytes(this.logicGraphBytes[i]));
        }else{
            buff.append(this.logicGraphBytes[i]);
        }
        
      }
      buff.append(" ");
      buff.append(super.toString());

        return buff.toString();
    }


    //~--- get methods ---------------------------------------------------------

    @Override
    protected RefexType getTkRefsetType() {
        return RefexType.LOGIC;
    }

    @Override
    public int getTypeNid() {
        return RefexType.LOGIC.getTypeToken();
    }

    @Override
    protected VersionComputer<RefexMemberVersion<LogicGraphRevision, LogicGraphMember>> getVersionComputer() {
        return computer;
    }
    @Override
    public List<LogicGraphMemberVersion> getVersionList() {
        return getVersions();
    }
    @Override
    public List<LogicGraphMemberVersion> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<LogicGraphMemberVersion> list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new LogicGraphMemberVersion(this, this, primordialStamp));
                    for (int stampAlias : getCommitManager().getAliases(primordialStamp)) {
                list.add(new LogicGraphMemberVersion(this, this, stampAlias));
            }
    }

            if (revisions != null) {
                revisions.stream().filter((br) -> (br.getTime() != Long.MIN_VALUE)).forEach((br) -> {
                    list.add(new LogicGraphMemberVersion(br, this, br.stamp));
                    for (int stampAlias : getCommitManager().getAliases(br.stamp)) {
                        list.add(new LogicGraphMemberVersion(br, this, stampAlias));
                    }
                 });
            }

            versions = list;
        }

        return (List<LogicGraphMemberVersion>) versions;
    }
    @Override
    public byte[][] getExternalLogicGraphBytes() {
        LogicalExpressionByteArrayConverter converter = LookupService.get().getService(LogicalExpressionByteArrayConverter.class);
        return converter.convertLogicGraphForm(logicGraphBytes, DataTarget.EXTERNAL);
    }

    @Override
    public byte[][] getLogicGraphBytes() {
        return logicGraphBytes;
    }

    @Override
    public void setLogicGraphBytes(byte[][] byteArray) {
        this.logicGraphBytes = byteArray;
        modified();
    }


}
