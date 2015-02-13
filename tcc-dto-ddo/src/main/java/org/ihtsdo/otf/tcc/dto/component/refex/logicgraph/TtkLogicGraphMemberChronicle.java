/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.dto.component.refex.logicgraph;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.logicgraph.LogicGraphVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

/**
 *
 * @author kec
 */
public class TtkLogicGraphMemberChronicle 
        extends TtkRefexAbstractMemberChronicle<TtkLogicGraphRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    

    //~--- fields --------------------------------------------------------------
    /**
     * The array of byte array associated with this TK Refex Array of Bytearray
     * Member.
     */
    public byte[][] logicGraphBytes;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Array of Byte Array Member.
     */
    public TtkLogicGraphMemberChronicle() {
        super();
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Array of Byte Array Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TtkLogicGraphMemberChronicle(RefexChronicleBI refexChronicle) throws IOException {
        this((LogicGraphVersionBI) refexChronicle.getPrimordialVersion());
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Member based on the
     * <code>logicGraphVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param logicGraphVersion the refex array of byte array version
     * specifying how to construct this TK Refex Array of Byte Array Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TtkLogicGraphMemberChronicle(LogicGraphVersionBI logicGraphVersion) throws IOException {
        super(logicGraphVersion);
        
            Collection<? extends LogicGraphVersionBI> refexes = logicGraphVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends LogicGraphVersionBI> itr = refexes.iterator();
            LogicGraphVersionBI rv = itr.next();

            this.logicGraphBytes = rv.getExternalLogicGraphBytes();

            if (partCount > 1) {
                revisions = new ArrayList<>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TtkLogicGraphRevision rev = new TtkLogicGraphRevision(rv);
                    revisions.add(rev);

                }
            }
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Member based on the
     * specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex
     * Array of Byte Array Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TtkLogicGraphMemberChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Member based on
     * <code>another</code> TK Refex Array of Byte Array Member and allows for
     * uuid conversion.
     *
     * @param another the TK Refex Array of Byte Array Member specifying how to
     * construct this TK Refex Array of Byte Array Member
     * @param transformer
     */
    public TtkLogicGraphMemberChronicle(TtkLogicGraphMemberChronicle another, ComponentTransformerBI transformer) {
        super(another, transformer);
        this.logicGraphBytes = transformer.transform(another.logicGraphBytes, another, ComponentFields.REFEX_ARRAY_OF_BYTEARRAY);
    }

    //~--- methods -------------------------------------------------------------

    @Override
    protected void addUuidReferencesForRefexRevision(Collection<UUID> references) {
        // nothing to do...
    }

    /**
     * Compares this object to the specified object. The result is <code>true</code>
     * if and only if the argument is not <code>null</code>, is a
     * <code>ERefsetLongMember</code> object, and contains the same values, field by
     * field, as this <code>ERefsetLongMember</code>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code>, if successful <code>true</code> if the objects
     * are the same; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TtkLogicGraphMemberChronicle.class.isAssignableFrom(obj.getClass())) {
            TtkLogicGraphMemberChronicle another = (TtkLogicGraphMemberChronicle) obj;

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
     * Returns a hash code for this
     * <code>ERefsetArrayofByteArrayMember</code>.
     *
     * @return a hash code value for this <code>ERefsetArrayofByteArrayMember</code>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     *
     * @param transformer
     * @return the converted TK Refex Array of Byte Array Member
     */
    @Override
    public TtkLogicGraphMemberChronicle makeTransform(ComponentTransformerBI transformer) {
        return new TtkLogicGraphMemberChronicle(this, transformer);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Refex Array
     * of Byte Array Member
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

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TtkLogicGraphRevision rev = new TtkLogicGraphRevision(in, dataVersion);
                revisions.add(rev);
            }
        }
    }
    /**
     * Returns a string representation of this TK Refex Array of Byte Array Member object.
     *
     * @return a string representation of this TK Refex Array of Byte Array Member object
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
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TtkLogicGraphRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     *
     * @return a list of revisions on this TK Refex Array of Byte Array Member
     */
    @Override
    public List<? extends TtkLogicGraphRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return RefexType.ARRAY_BYTEARRAY
     */
    @Override
    public RefexType getType() {
        return RefexType.LOGIC;
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
     * @param logicGraphBytes the logic graph bytes
     */
    public void setLogicGraphBytes(byte[][] logicGraphBytes) {
        this.logicGraphBytes = logicGraphBytes;
    }
}
