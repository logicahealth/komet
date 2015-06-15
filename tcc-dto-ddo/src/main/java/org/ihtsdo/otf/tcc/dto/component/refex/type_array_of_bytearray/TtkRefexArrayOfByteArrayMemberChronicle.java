/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;

/**
 * The Class TkRefexArrayOfBytearrayMember represents an array of byte array
 * type refex member in the eConcept format and contains methods specific for
 * interacting with an array of byte array type refex member. Further discussion
 * of the eConcept format can be found on
 * {@code TkConcept}.
 *
 * @see TkConcept
 */
public class TtkRefexArrayOfByteArrayMemberChronicle
        extends TtkRefexAbstractMemberChronicle<TtkRefexArrayOfByteArrayRevision> {

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
    public byte[][] arrayOfByteArray1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Array of Byte Array Member.
     */
    public TtkRefexArrayOfByteArrayMemberChronicle() {
        super();
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Member based on the
     * {@code refexChronicle}.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Array of Byte Array Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TtkRefexArrayOfByteArrayMemberChronicle(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexArrayOfBytearrayVersionBI) refexChronicle.getPrimordialVersion());
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Member based on the
     * {@code refexArrayOfBytearrayVersion} and using the given
     * {@code revisionHandling}.
     *
     * @param refexArrayOfBytearrayVersion the refex array of byte array version
     * specifying how to construct this TK Refex Array of Byte Array Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TtkRefexArrayOfByteArrayMemberChronicle(RefexArrayOfBytearrayVersionBI refexArrayOfBytearrayVersion) throws IOException {
        super(refexArrayOfBytearrayVersion);
        
            Collection<? extends RefexArrayOfBytearrayVersionBI> refexes = refexArrayOfBytearrayVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexArrayOfBytearrayVersionBI> itr = refexes.iterator();
            RefexArrayOfBytearrayVersionBI rv = itr.next();

            this.arrayOfByteArray1 = rv.getArrayOfByteArray();
            

            if (partCount > 1) {
                revisions = new ArrayList<>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TtkRefexArrayOfByteArrayRevision rev = new TtkRefexArrayOfByteArrayRevision(rv);
                    revisions.add(rev);

                }
            }
        
    }

    /**
     * Instantiates a new TK Refex Array of Byte Array Member based on the
     * specified data input,
     * {@code in}.
     *
     * @param in the data input specifying how to construct this TK Refex
     * Array of Byte Array Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TtkRefexArrayOfByteArrayMemberChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    //~--- methods -------------------------------------------------------------

    @Override
    protected void addUuidReferencesForRefexRevision(Collection<UUID> references) {
        // nothing to do...
    }

    /**
     * Compares this object to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null}, is a
     * {@code ERefsetLongMember} object, and contains the same values, field by
     * field, as this {@code ERefsetLongMember}.
     *
     * @param obj the object to compare with.
     * @return {@code true}, if successful {@code true} if the objects
     * are the same; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TtkRefexArrayOfByteArrayMemberChronicle.class.isAssignableFrom(obj.getClass())) {
            TtkRefexArrayOfByteArrayMemberChronicle another = (TtkRefexArrayOfByteArrayMemberChronicle) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare byteArray
            if (!Arrays.deepEquals(this.arrayOfByteArray1, another.arrayOfByteArray1)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * {@code ERefsetArrayofByteArrayMember}.
     *
     * @return a hash code value for this {@code ERefsetArrayofByteArrayMember}.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
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
        this.arrayOfByteArray1 = new byte[arrayLength][];
        for (int i = 0; i < arrayLength; i++) {
            int byteArrayLength = in.readInt();
            this.arrayOfByteArray1[i] = new byte[byteArrayLength];
            in.readFully(this.arrayOfByteArray1[i]);
        }

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TtkRefexArrayOfByteArrayRevision rev = new TtkRefexArrayOfByteArrayRevision(in, dataVersion);
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
        buff.append(this.arrayOfByteArray1.length);
        for (int i = 0; i < this.arrayOfByteArray1.length; i++) {
            buff.append(" ").append(i);
            buff.append(": ");
            buff.append(this.arrayOfByteArray1[i]);
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
        out.writeShort(arrayOfByteArray1.length);
        for (byte[] bytes : arrayOfByteArray1) {
            out.writeInt(bytes.length);
            out.write(bytes);
        }
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TtkRefexArrayOfByteArrayRevision rmv : revisions) {
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
    public List<? extends TtkRefexArrayOfByteArrayRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return RefexType.ARRAY_BYTEARRAY
     */
    @Override
    public RefexType getType() {
        return RefexType.ARRAY_BYTEARRAY;
    }

    /**
     * Gets the array of byte array associated with this TK Refex Array of Byte Array Member.
     *
     * @return the array of byte array
     */
    public byte[][] getArrayOfByteArray1() {
        return arrayOfByteArray1;
    }

    /**
     * Sets the array of byte array associated with this TK Refex Array of Byte Array Member.
     *
     * @param byteArray1 the array of byte array
     */
    public void setArrayOfByteArray1(byte[][] byteArray1) {
        this.arrayOfByteArray1 = byteArray1;
    }
}
