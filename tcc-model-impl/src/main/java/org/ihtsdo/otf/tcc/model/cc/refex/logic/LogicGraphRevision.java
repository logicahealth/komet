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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.logicgraph.LogicGraphAnalogBI;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

/**
 *
 * @author kec
 */
public class LogicGraphRevision 
        extends RefexRevision<LogicGraphRevision, LogicGraphMember>
        implements LogicGraphAnalogBI<LogicGraphRevision>  {

   protected byte[][] logicGraphBytes;

    
   //~--- constructors --------------------------------------------------------

   public LogicGraphRevision() {
      super();
   }

   protected LogicGraphRevision(int statusAtPositionNid, LogicGraphMember another) {
      super(statusAtPositionNid, another);
      this.logicGraphBytes = another.getLogicGraphBytes();
   }

   public LogicGraphRevision(TtkLogicGraphRevision eVersion, LogicGraphMember another) throws IOException {
      super(eVersion, another);
        LogicalExpressionByteArrayConverter converter = LookupService.get().getService(LogicalExpressionByteArrayConverter.class);
        this.logicGraphBytes = converter.convertLogicGraphForm(eVersion.getLogicGraphBytes(), DataTarget.INTERNAL);
   }


   protected LogicGraphRevision(Status status, long time, int authorNid,
           int moduleNid, int pathNid, LogicGraphMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.logicGraphBytes = primoridalMember.getLogicGraphBytes();
   }

   protected LogicGraphRevision(Status status, long time, int authorNid,
           int moduleNid, int pathNid, LogicGraphRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      this.logicGraphBytes = another.getLogicGraphBytes();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      // ;
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.ARRAY_OF_BYTEARRAY, getLogicGraphBytes());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (LogicGraphRevision.class.isAssignableFrom(obj.getClass())) {
         LogicGraphRevision another = (LogicGraphRevision) obj;

         return (Arrays.deepEquals(logicGraphBytes, another.getLogicGraphBytes())) && super.equals(obj);
      }

      return false;
   }

   @Override
   public LogicGraphRevision makeAnalog() {
      return new LogicGraphRevision(getStatus(), getTime(), getAuthorNid(),
              getModuleNid(), getPathNid(),  this);
   }
   
   @Override
   public LogicGraphRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, 
           long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      LogicGraphRevision newR = new LogicGraphRevision(status, time,
              authorNid, moduleNid, pathNid,this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();
      buff.append(" size: ");
      buff.append(this.logicGraphBytes.length);
      for (int i = 0; i < this.logicGraphBytes.length; i++) {
        buff.append(" ").append(i);
        buff.append(": ");
        if (this.logicGraphBytes[i].length == 16){
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
   public Optional<LogicGraphMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      Optional<RefexMemberVersion<LogicGraphRevision, LogicGraphMember>> temp =  ((LogicGraphMember) primordialComponent).getVersion(c);
      return Optional.ofNullable(temp.isPresent() ? (LogicGraphMemberVersion)temp.get() : null);
   }

   @Override
   public List<LogicGraphMemberVersion> getVersions() {
      return (List<LogicGraphMemberVersion>) ((LogicGraphMember) primordialComponent).getVersions();
   }

   @Override
   public List<LogicGraphMemberVersion> getVersionList() {
      return (List<LogicGraphMemberVersion>) ((LogicGraphMember) primordialComponent).getVersions();
   }

   @Override
   public List<RefexMemberVersion<LogicGraphRevision, LogicGraphMember>> getVersions(ViewCoordinate c) {
      return ((LogicGraphMember) primordialComponent).getVersions(c);
   }


   
    @Override
    public byte[][] getLogicGraphBytes() {
        return logicGraphBytes;
    }

    @Override
    public void setLogicGraphBytes(byte[][] logicGraphBytes) {
        this.logicGraphBytes = logicGraphBytes;
        modified();
    }

    @Override
    public byte[][] getExternalLogicGraphBytes() {
        LogicalExpressionByteArrayConverter converter = LookupService.get().getService(LogicalExpressionByteArrayConverter.class);
        return converter.convertLogicGraphForm(logicGraphBytes, DataTarget.EXTERNAL);
    }
}
