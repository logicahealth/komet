/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.dto.component.refex.logicgraph;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.refex.logicgraph.LogicGraphVersionBI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

/**
 *
 * @author kec
 */
public class TtkLogicGraphRevision extends TtkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The array of byte array associated with this TK Refex Array of Bytearray
     * Revision.
     */
    public byte[][] logicGraphBytes;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Array of Byte Array Revision.
     */
    public TtkLogicGraphRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Revision based on the
     * <code>logicGraphVersion</code>.
     *
     * @param logicGraphVersion the refex array of byte array version
     * specifying how to construct this TK Refex Array of Byte Array Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TtkLogicGraphRevision(LogicGraphVersionBI logicGraphVersion) throws IOException {
        super(logicGraphVersion);
        this.logicGraphBytes = logicGraphVersion.getExternalLogicGraphBytes();
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Revision based on the
     * specified data input,
     * <code>in</code>.
     *
     * @param in in the data input specifying how to construct this TK Refex
     * Array of Byte Array Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TtkLogicGraphRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Revision based on
     * <code>another</code> TK Refex Array of Byte Array Revision and allows for
     * uuid conversion.
     *
     * @param another the TK Refex Array of Byte Array Revision specifying how to
     * construct this TK Refex Array of Byte Array Revision
     * @param transformer
      */
    public TtkLogicGraphRevision(TtkLogicGraphRevision another, ComponentTransformerBI transformer) {
        super(another, transformer);
        this.logicGraphBytes = transformer.transform(another.logicGraphBytes, 
                another, ComponentFields.REFEX_LOGIC_GRAPH_BYTES);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addUuidReferencesForRevisionComponent(Collection<UUID> references) {
        // nothing to add
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetLongVersion</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetLongVersion</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TtkLogicGraphRevision.class.isAssignableFrom(obj.getClass())) {
            TtkLogicGraphRevision another = (TtkLogicGraphRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare byteArray
            if (!Arrays.deepEquals(this.logicGraphBytes, another.logicGraphBytes)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * 
     * @return a hash code value for this <tt>ERefsetArrayofByteArrayRevision</tt>.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     *
     * @param transformer
      * @return the converted TK Refex Array of Byte Array Revision
     */
    @Override
    public TtkLogicGraphRevision makeTransform(ComponentTransformerBI transformer) {
        return new TtkLogicGraphRevision(this, transformer);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Refex Array
     * of Byte Array Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        int arrayLength = in.readShort();
        this.logicGraphBytes = new byte[arrayLength][];
        for (int i = 0; i < arrayLength; i++) {
            int byteArrayLength = in.readInt();
            this.logicGraphBytes[i] = new byte[byteArrayLength];
            in.readFully(this.logicGraphBytes[i]);
        }
    }

    /**
     * Returns a string representation of this TK Refex Array of Byte Array Revision object.
     *
     * @return a string representation of this TK Refex Array of Byte Array Revision object
     * including the size and the array of byte array.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" size: ");
        buff.append(this.logicGraphBytes.length);
        for (int i = 0; i < this.logicGraphBytes.length; i++) {
            buff.append(" ").append(i);
            buff.append(": ");
            buff.append(this.logicGraphBytes[i]);
        }
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeShort(logicGraphBytes.length);
        for (byte[] bytes : logicGraphBytes) {
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }

    /**
     * Gets the array of byte array associated with this TK Refex Array of Byte Array Member.
     *
     * @return the array of byte array
     */
    public byte[][] getLogicGraphBytes() {
        return logicGraphBytes;
    }

    /**
     * Sets the array of byte array associated with this TK Refex Array of Byte Array Member.
     *
     * @param logicGraphBytes the bytes of the logic graph
     */
    public void setLogicGraphBytes(byte[][] logicGraphBytes) {
        this.logicGraphBytes = logicGraphBytes;
    }
}