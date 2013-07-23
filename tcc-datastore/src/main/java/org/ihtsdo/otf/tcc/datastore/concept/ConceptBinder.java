package org.ihtsdo.otf.tcc.datastore.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;


import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import org.ihtsdo.otf.tcc.chronicle.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.chronicle.cc.attributes.ConceptAttributesRevision;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptAttributesBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponentBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.component.DescriptionBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.component.MediaBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RefexMemberBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RelationshipBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.component.Revision;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.I_ManageConceptData;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.IntSetBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.OFFSETS;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;

public class ConceptBinder extends TupleBinding<ConceptChronicle> {

    public static final byte[] zeroOutputArray;

    static {
        TupleOutput zeroOutput = new TupleOutput();
        zeroOutput.writeInt(0);
        zeroOutputArray = zeroOutput.toByteArray();
    }

    @Override
    public ConceptChronicle entryToObject(TupleInput input) {
        /*
         * We don't retrieve the entire concept. Instead we just retrieve lists
         * of concept components on demand. See getList in ConceptData.
         */
        throw new UnsupportedOperationException();
    }

    @Override
    public void objectToEntry(ConceptChronicle concept, TupleOutput finalOutput) {

        try {
            long dataVersion = concept.getDataVersion();
            I_ManageConceptData conceptData = concept.getData();
            boolean primordial = conceptData.getReadOnlyBytes().length == 0
                    && conceptData.getReadWriteBytes().length == 0;

            byte[] attrOutput = getAttributeBytes(conceptData, primordial,
                    OFFSETS.ATTRIBUTES, conceptData.getConceptAttributesIfChanged(),
                    new ConceptAttributesBinder());
            byte[] descOutput = getComponentBytes(conceptData, primordial,
                    OFFSETS.DESCRIPTIONS, conceptData.getDescriptionsIfChanged(),
                    new DescriptionBinder());
            byte[] relOutput = getComponentBytes(conceptData, primordial,
                    OFFSETS.SOURCE_RELS, conceptData.getSourceRelsIfChanged(),
                    new RelationshipBinder());
            byte[] imageOutput = getComponentBytes(conceptData, primordial,
                    OFFSETS.IMAGES, conceptData.getImagesIfChanged(),
                    new MediaBinder());
            byte[] refsetOutput = getRefsetBytes(conceptData, primordial,
                    OFFSETS.REFSET_MEMBERS, conceptData.getRefsetMembersIfChanged(),
                    new RefexMemberBinder(concept));


            byte[] descNidOutput = getNidSetBytes(conceptData, primordial,
                    conceptData.getDescNidsReadOnly(),
                    conceptData.getDescNids());
            byte[] srcRelNidOutput = getNidSetBytes(conceptData, primordial,
                    conceptData.getSrcRelNidsReadOnly(),
                    conceptData.getSrcRelNids());
            byte[] imageNidOutput = getNidSetBytes(conceptData, primordial,
                    conceptData.getImageNidsReadOnly(),
                    conceptData.getImageNids());

            byte[] memberNidOutput = getNidSetBytes(conceptData, primordial,
                    conceptData.getMemberNidsReadOnly(),
                    conceptData.getMemberNids());

            finalOutput.writeInt(OFFSETS.CURRENT_FORMAT_VERSION); // FORMAT_VERSION
            finalOutput.writeLong(dataVersion); // DATA_VERSION
            if (concept.isAnnotationIndex()) {
                finalOutput.writeByte(2); // ANNOTATION_STYLE_REFSET
            } else if (concept.isAnnotationStyleRefex()) {
                finalOutput.writeByte(1); // ANNOTATION_STYLE_REFSET
            } else {
                finalOutput.writeByte(0); // ANNOTATION_STYLE_REFSET
            }
            int nextDataLocation = OFFSETS.getHeaderSize();
            finalOutput.writeInt(nextDataLocation); // ATTRIBUTES
            nextDataLocation = nextDataLocation + attrOutput.length;

            finalOutput.writeInt(nextDataLocation); // DESCRIPTIONS
            nextDataLocation = nextDataLocation + descOutput.length;

            finalOutput.writeInt(nextDataLocation); // SOURCE_RELS
            nextDataLocation = nextDataLocation + relOutput.length;

            finalOutput.writeInt(nextDataLocation); // REFSET_MEMBERS
            nextDataLocation = nextDataLocation + refsetOutput.length;

            finalOutput.writeInt(nextDataLocation); // DESC_NIDS
            nextDataLocation = nextDataLocation
                    + descNidOutput.length;

            finalOutput.writeInt(nextDataLocation); // SRC_REL_NIDS
            nextDataLocation = nextDataLocation
                    + srcRelNidOutput.length;

            finalOutput.writeInt(nextDataLocation); // IMAGE_NIDS
            nextDataLocation = nextDataLocation
                    + imageNidOutput.length;

            finalOutput.writeInt(nextDataLocation); // MEMBER_NIDS
            nextDataLocation = nextDataLocation
                    + memberNidOutput.length;

            finalOutput.writeInt(nextDataLocation); // IMAGES
            nextDataLocation = nextDataLocation + imageOutput.length;
            
            finalOutput.writeInt(nextDataLocation); // DATA_SIZE

            finalOutput.makeSpace(nextDataLocation);
            finalOutput.writeFast(attrOutput);   // ATTRIBUTES
            finalOutput.writeFast(descOutput);   // DESCRIPTIONS
            finalOutput.writeFast(relOutput);    // SOURCE_RELS
            finalOutput.writeFast(refsetOutput); // REFSET_MEMBERS
            finalOutput.writeFast(descNidOutput);  // DESC_NIDS
            finalOutput.writeFast(srcRelNidOutput);// SRC_REL_NIDS
            finalOutput.writeFast(imageNidOutput); // IMAGE_NIDS
            finalOutput.writeFast(memberNidOutput); // MEMBER_NIDS
            finalOutput.writeFast(imageOutput);    // IMAGES

        } catch (IOException | InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }
    private static IntSetBinder intSetBinder = new IntSetBinder();

    private byte[] getNidSetBytes(I_ManageConceptData conceptData,
            boolean primordial, Set<Integer> nidsReadOnly, Set<Integer> nids) {
        HashSet<Integer> nidsToWrite = new HashSet<>(nids);
        nidsToWrite.removeAll(nidsReadOnly);
        TupleOutput output = new TupleOutput();
        intSetBinder.objectToEntry(nidsToWrite, output);
        return output.toByteArray();
    }

    private static byte[] getAttributeBytes(
            I_ManageConceptData conceptData,
            boolean primordial,
            OFFSETS offset,
            ConceptAttributes attributes,
            ConceptComponentBinder<ConceptAttributesRevision, ConceptAttributes> conceptComponentBinder)
            throws InterruptedException, ExecutionException, IOException {
        assert offset != null && offset.prev != null : "offset is malformed: " + offset;
        byte[] componentBytes;
        if (!primordial && attributes == null) {
            componentBytes = getPreviousData(conceptData, offset, OFFSETS.values()[offset.ordinal() + 1]);
        } else {
            TupleOutput output = new TupleOutput();
            if (attributes != null && attributes.getTime() != Long.MIN_VALUE) {
                List<ConceptAttributes> attrList = new ArrayList<>();
                attrList.add(attributes);
                conceptComponentBinder.objectToEntry(attrList, output);
                componentBytes = output.toByteArray();
            } else {
                componentBytes = zeroOutputArray;
            }
        }
        return componentBytes;
    }

    private <C extends ConceptComponent<V, C>, V extends Revision<V, C>> byte[] getComponentBytes(
            I_ManageConceptData conceptData, boolean primordial, OFFSETS offset,
            Collection<C> componentList,
            ConceptComponentBinder<V, C> binder) throws InterruptedException,
            ExecutionException, IOException {
        byte[] componentBytes;
        if (!primordial && componentList == null) {
            componentBytes = getPreviousData(conceptData, offset, OFFSETS.values()[offset.ordinal() + 1]);
        } else {
            TupleOutput output = new TupleOutput();
            if (componentList != null) {
                binder.objectToEntry(componentList, output);
                componentBytes = output.toByteArray();
            } else {
                componentBytes = zeroOutputArray;
            }
        }
        if (componentBytes.length == 0) {
            componentBytes = zeroOutputArray;
        }
        return componentBytes;
    }

    private byte[] getRefsetBytes(I_ManageConceptData conceptData, boolean primordial,
            OFFSETS offset,
            Collection<RefexMember<?, ?>> members,
            RefexMemberBinder binder) throws InterruptedException,
            ExecutionException, IOException {
        byte[] componentBytes;
        if (!primordial && members == null) {
            componentBytes = getPreviousData(conceptData, offset, OFFSETS.values()[offset.ordinal() + 1]);
        } else {
            TupleOutput output = new TupleOutput();
            if (members != null) {
                binder.objectToEntry(members, output);
                componentBytes = output.toByteArray();
            } else {
                componentBytes = zeroOutputArray;
            }
        }
        if (componentBytes.length == 0) {
            componentBytes = zeroOutputArray;
        }
        return componentBytes;
    }

    private static byte[] getPreviousData(I_ManageConceptData conceptData,
            OFFSETS start,
            OFFSETS end) throws InterruptedException, ExecutionException, IOException {
        assert start != null : "start is null. end: " + end;
        assert end != null : "end is null. start: " + start;
        byte[] output;
        TupleInput readWriteInput = conceptData.getReadWriteTupleInput();
        byte[] bufferBytes = readWriteInput.getBufferBytes();
        if (bufferBytes.length > OFFSETS.getHeaderSize()) {
            int offset = start.getOffset(bufferBytes);
            int endOffset = end.getOffset(bufferBytes);
            int byteCount = endOffset - offset;
            readWriteInput.skipFast(offset);
            assert byteCount >= 0 : " neg byteCount: " + byteCount
                    + " start offset: " + offset + " end offset: " + endOffset
                    + " start: " + start + " end: " + end;
            output = new byte[byteCount];
            System.arraycopy(readWriteInput.getBufferBytes(), offset, output, 0,
                    byteCount);
        } else {
            output = zeroOutputArray;
        }
        return output;
    }
}
