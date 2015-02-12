/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.ddo.concept.component.refex.logicgraph;

import java.io.IOException;
import javafx.beans.property.SimpleObjectProperty;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.logicgraph.LogicGraphVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexVersionDdo;

/**
 *
 * @author kec
 */
public class LogicGraphVersionDdo<T extends RefexChronicleDdo,
    V extends LogicGraphVersionDdo> extends RefexVersionDdo<T, V> {

   /** Field description */
   public static final long serialVersionUID = 1;

   /** Field description */
   private SimpleObjectProperty<byte[][]> arrayOfByteArrayProperty;

   /**
    * Constructs ...
    *
    */
   public LogicGraphVersionDdo() {
      super();
   }

   /**
    * Constructs ...
    *
    *
    * @param chronicle
    * @param ss
    * @param another
    *
    * @throws ContradictionException
    * @throws IOException
    */
   public LogicGraphVersionDdo(T chronicle, TerminologySnapshotDI ss,
       LogicGraphVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.arrayOfByteArrayProperty = new SimpleObjectProperty(another.getLogicGraphBytes());
   }

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidVersion</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (LogicGraphVersionDdo.class.isAssignableFrom(obj.getClass())) {
         LogicGraphVersionDdo another = (LogicGraphVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.arrayOfByteArrayProperty.equals(another.arrayOfByteArrayProperty)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a string representation of the object.
    *
    * @return
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" c1:");
      buff.append(this.arrayOfByteArrayProperty);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public byte[][] getArrayOfByteArray() {
      return arrayOfByteArrayProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public SimpleObjectProperty<byte[][]> getArrayOfByteArrayProperty() {
      return arrayOfByteArrayProperty;
   }

   /**
    * Method description
    *
    *
    * @param comp1Ref
    */
   public void setArrayOfByteArray(byte[][] comp1Ref) {
      this.arrayOfByteArrayProperty.setValue(comp1Ref);
   }
}
