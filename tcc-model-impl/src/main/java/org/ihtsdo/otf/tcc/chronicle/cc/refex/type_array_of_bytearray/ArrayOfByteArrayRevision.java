/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_array_of_bytearray;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray.TtkRefexArrayOfByteArrayRevision;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class ArrayOfByteArrayRevision extends RefexRevision<ArrayOfByteArrayRevision, ArrayOfByteArrayMember>
        implements RefexArrayOfBytearrayAnalogBI<ArrayOfByteArrayRevision>  {

   private byte[][] arrayOfByteArray;

    @Override
    public byte[][] getArrayOfByteArray() {
        return arrayOfByteArray;
    }

    @Override
    public void setArrayOfByteArray(byte[][] arrayOfByteArray) {
        this.arrayOfByteArray = arrayOfByteArray;
        modified();
    }

    
   //~--- constructors --------------------------------------------------------

   public ArrayOfByteArrayRevision() {
      super();
   }

   protected ArrayOfByteArrayRevision(int statusAtPositionNid, ArrayOfByteArrayMember another) {
      super(statusAtPositionNid, another);
      this.arrayOfByteArray = another.getArrayOfByteArray();
   }

   public ArrayOfByteArrayRevision(TtkRefexArrayOfByteArrayRevision eVersion, ArrayOfByteArrayMember another) throws IOException {
      super(eVersion, another);
      this.arrayOfByteArray = eVersion.getArrayOfByteArray1();
   }

   public ArrayOfByteArrayRevision(TupleInput in, ArrayOfByteArrayMember primoridalMember) {
      super(in, primoridalMember);
      int arrayLength = in.readShort();
      this.arrayOfByteArray = new byte[arrayLength][];
      for (int i = 0; i < arrayLength; i++) {
          int byteArrayLength = in.readInt();
          this.arrayOfByteArray[i] = new byte[byteArrayLength];
          in.read(this.arrayOfByteArray[i], 0, byteArrayLength);
      }
   }

   protected ArrayOfByteArrayRevision(Status status, long time, int authorNid,
           int moduleNid, int pathNid, ArrayOfByteArrayMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.arrayOfByteArray = primoridalMember.getArrayOfByteArray();
   }

   protected ArrayOfByteArrayRevision(Status status, long time, int authorNid,
           int moduleNid, int pathNid, ArrayOfByteArrayRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      this.arrayOfByteArray = another.getArrayOfByteArray();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      // ;
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.ARRAY_OF_BYTEARRAY, getArrayOfByteArray());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (ArrayOfByteArrayRevision.class.isAssignableFrom(obj.getClass())) {
         ArrayOfByteArrayRevision another = (ArrayOfByteArrayRevision) obj;

         return (Arrays.deepEquals(arrayOfByteArray, another.getArrayOfByteArray())) && super.equals(obj);
      }

      return false;
   }

   @Override
   public ArrayOfByteArrayRevision makeAnalog() {
      return new ArrayOfByteArrayRevision(getStatus(), getTime(), getAuthorNid(),
              getModuleNid(), getPathNid(),  this);
   }
   
   @Override
   public ArrayOfByteArrayRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      ArrayOfByteArrayRevision newR = new ArrayOfByteArrayRevision(status, time,
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
      buff.append(this.arrayOfByteArray.length);
      for (int i = 0; i < this.arrayOfByteArray.length; i++) {
        buff.append(" ").append(i);
        buff.append(": ");
        if (this.arrayOfByteArray[i].length == 16){
            buff.append(UuidT5Generator.getUuidFromRawBytes(this.arrayOfByteArray[i]));
        }else{
            buff.append(this.arrayOfByteArray[i]);
        }
      }
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput out) {
      out.writeShort(arrayOfByteArray.length);
      for (byte[] bytes: arrayOfByteArray) {
        out.writeInt(bytes.length);  
        out.write(bytes);
      }
   }

   //~--- get methods ---------------------------------------------------------


    @Override
   protected RefexType getTkRefsetType() {
      return RefexType.ARRAY_BYTEARRAY;
   }

   @Override
   public ArrayOfByteArrayMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (ArrayOfByteArrayMember.Version) ((ArrayOfByteArrayMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<ArrayOfByteArrayMember.Version> getVersions() {
      return ((ArrayOfByteArrayMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<ArrayOfByteArrayRevision>> getVersions(ViewCoordinate c) {
      return ((ArrayOfByteArrayMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

    @Override
    public IntArrayList getVariableVersionNids() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
