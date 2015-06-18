/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.model.cc.refex.logic;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionByteArrayConverter;
import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.logicgraph.LogicGraphAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphRevision;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

/**
 *
 * @author kec
 */
public class LogicGraphMemberVersion extends RefexMemberVersion<LogicGraphRevision, LogicGraphMember> 
    implements LogicGraphAnalogBI<LogicGraphRevision> {

    LogicGraphMemberVersion(LogicGraphAnalogBI<LogicGraphRevision> cv, final LogicGraphMember rm, int stamp) {
        super(cv,rm, stamp);
    }

    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------
    @Override
    public byte[][] getLogicGraphBytes() {
        return getCv().getLogicGraphBytes();
    }

    LogicGraphAnalogBI<LogicGraphRevision> getCv() {
        return (LogicGraphAnalogBI<LogicGraphRevision>) cv;
    }

    @Override
    public TtkLogicGraphMemberChronicle getERefsetMember() throws IOException {
        return new TtkLogicGraphMemberChronicle(this);
    }

    @Override
    public TtkLogicGraphRevision getERefsetRevision() throws IOException {
        return new TtkLogicGraphRevision(this);
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setLogicGraphBytes(byte[][] logicGraphBytes) throws PropertyVetoException {
        getCv().setLogicGraphBytes(logicGraphBytes);
    }
   
        @Override
    public byte[][] getExternalLogicGraphBytes() {
        LogicalExpressionByteArrayConverter converter = Hk2Looker.get().getService(LogicalExpressionByteArrayConverter.class);
        return converter.convertLogicGraphForm(getCv().getLogicGraphBytes(), DataTarget.EXTERNAL);
    }

}
