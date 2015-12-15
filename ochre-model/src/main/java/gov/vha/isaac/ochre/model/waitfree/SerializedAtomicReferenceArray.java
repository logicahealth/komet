/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.waitfree;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.model.ByteArrayDataBuffer;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 *
 * @author kec
 */
public class SerializedAtomicReferenceArray extends AtomicReferenceArray<byte[]> {

    WaitFreeMergeSerializer isaacSerializer;

    int segment;

    public SerializedAtomicReferenceArray(int length, WaitFreeMergeSerializer isaacSerializer, int segment) {
        super(length);
        this.isaacSerializer = isaacSerializer;
        this.segment = segment;
    }

    /**
     * Returns the String representation of the current values of array.
     *
     * @return the String representation of the current values of array
     */
    @Override
    public String toString() {
        int iMax = length() - 1;
        if (iMax == -1) {
            return "≤≥";
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0;; i++) {
            b.append('≤');
            int sequence = segment * length() + i;
            b.append(sequence);
            b.append(": ");
            //TODO is this just for concepts?
            b.append(Get.conceptDescriptionText(sequence));
            b.append(" ");
            byte[] byteData = get(i);
            if (byteData != null) {
                ByteArrayDataBuffer db = new ByteArrayDataBuffer(byteData);
                b.append(isaacSerializer.deserialize(db));
            } else {
                b.append("null");
            }
            if (i == iMax) {
                return b.append('≥').toString();
            }
            b.append('≥').append(' ');
        }
    }


    public int getSegment() {
        return segment;
    }

}
