package org.ihtsdo.otf.tcc.datastore.concept;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributesSerializer;
import org.ihtsdo.otf.tcc.model.cc.component.*;
import org.ihtsdo.otf.tcc.model.cc.concept.*;
import org.ihtsdo.otf.tcc.model.cc.description.Description;
import org.ihtsdo.otf.tcc.model.cc.description.DescriptionSerializer;
import org.ihtsdo.otf.tcc.model.cc.media.MediaSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexGenericSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicSerializer;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.cc.relationship.RelationshipSerializer;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
            I_ManageSimpleConceptData conceptData = (I_ManageSimpleConceptData) concept.getData();
            long dataVersion = conceptData.getLastChange();
            boolean primordial = conceptData.getReadWriteBytes().length == 0;

            byte[] attrOutput = getAttributeBytes(conceptData, primordial,
                    OFFSETS.ATTRIBUTES, conceptData.getConceptAttributesIfChanged());
            byte[] descOutput = getDescriptionBytes(conceptData, primordial, conceptData.getDescriptionsIfChanged());
            byte[] relOutput = getRelBytes(conceptData, primordial, conceptData.getSourceRelsIfChanged());
            byte[] imageOutput = getImageBytes(conceptData, primordial, conceptData.getImagesIfChanged());
            byte[] refsetOutput = getRefsetBytes(conceptData, primordial, conceptData.getRefsetMembersIfChanged());
            byte[] refsetDynamicOutput = getRefsetDynamicBytes(conceptData, primordial,conceptData.getRefsetDynamicMembersIfChanged());
            byte[] descNidOutput = getNidSetBytes(conceptData, primordial,
                    conceptData.getDescNids());
            byte[] srcRelNidOutput = getNidSetBytes(conceptData, primordial,
                    conceptData.getSrcRelNids());
            byte[] imageNidOutput = getNidSetBytes(conceptData, primordial,
                    conceptData.getImageNids());

            byte[] memberNidOutput = getNidSetBytes(conceptData, primordial,
                    conceptData.getMemberNids());

            finalOutput.writeInt(OFFSETS.CURRENT_FORMAT_VERSION); // FORMAT_VERSION
            finalOutput.writeLong(dataVersion); // DATA_VERSION
            if (concept.isAnnotationStyleRefex()) {
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
            
            finalOutput.writeInt(nextDataLocation); // REFSET_DYNAMIC_MEMBERS
            nextDataLocation = nextDataLocation + refsetDynamicOutput.length;

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
            finalOutput.writeFast(refsetDynamicOutput); // REFSET_DYNAMIC_MEMBERS
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
            boolean primordial, Set<Integer> nids) throws IOException {
        HashSet<Integer> nidsToWrite = new HashSet<>(nids);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        intSetBinder.objectToEntry(nidsToWrite, new DataOutputStream(output));
        return output.toByteArray();
    }

    private static byte[] getAttributeBytes(
            I_ManageSimpleConceptData conceptData,
            boolean primordial,
            OFFSETS offset,
            ConceptAttributes attributes)
            throws InterruptedException, ExecutionException, IOException {
        assert offset != null && offset.prev != null : "offset is malformed: " + offset;
        byte[] componentBytes;
        if (!primordial && attributes == null) {
            componentBytes = getPreviousData(conceptData, offset, OFFSETS.values()[offset.ordinal() + 1]);
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (attributes != null && attributes.getTime() != Long.MIN_VALUE) {
                ConceptAttributesSerializer.get().serialize(new DataOutputStream(output), attributes);
                componentBytes = output.toByteArray();
            } else {
                componentBytes = zeroOutputArray;
            }
        }
        return componentBytes;
    }

    private byte[] getDescriptionBytes(
            I_ManageSimpleConceptData conceptData, boolean primordial,
            Collection<Description> componentList) throws InterruptedException,
            ExecutionException, IOException {
        byte[] componentBytes;
        if (!primordial && componentList == null) {
            componentBytes = getPreviousData(conceptData,
                    OFFSETS.DESCRIPTIONS,
                    OFFSETS.values()[OFFSETS.DESCRIPTIONS.ordinal() + 1]);
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (componentList != null) {
                DescriptionSerializer.get().serialize(new DataOutputStream(output), componentList);
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

    private byte[] getRelBytes(
            I_ManageSimpleConceptData conceptData, boolean primordial,
            Collection<Relationship> componentList) throws InterruptedException,
            ExecutionException, IOException {
        byte[] componentBytes;
        if (!primordial && componentList == null) {
            componentBytes = getPreviousData(conceptData,
                    OFFSETS.SOURCE_RELS,
                    OFFSETS.values()[OFFSETS.SOURCE_RELS.ordinal() + 1]);
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (componentList != null) {
                RelationshipSerializer.get().serialize(new DataOutputStream(output), componentList);
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


    private byte[] getImageBytes(
            I_ManageSimpleConceptData conceptData, boolean primordial,
            Collection componentList) throws InterruptedException,
            ExecutionException, IOException {
        byte[] componentBytes;
        if (!primordial && componentList == null) {
            componentBytes = getPreviousData(conceptData,
                    OFFSETS.IMAGES,
                    OFFSETS.values()[OFFSETS.IMAGES.ordinal() + 1]);
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (componentList != null) {
                MediaSerializer.get().serialize(new DataOutputStream(output), componentList);
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


    private byte[] getRefsetBytes(I_ManageSimpleConceptData conceptData, boolean primordial,
            Collection<RefexMember<?, ?>> members) throws InterruptedException,
            ExecutionException, IOException {
        byte[] componentBytes;
        if (!primordial && members == null) {
            componentBytes = getPreviousData(conceptData,
                    OFFSETS.REFSET_MEMBERS,
                    OFFSETS.values()[OFFSETS.REFSET_MEMBERS.ordinal() + 1]);
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (members != null) {
                RefexGenericSerializer.get().serialize(new DataOutputStream(output), members);
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
    
    private byte[] getRefsetDynamicBytes(I_ManageSimpleConceptData conceptData, boolean primordial,
            Collection<RefexDynamicMember> members) throws InterruptedException,
            ExecutionException, IOException {
        byte[] componentBytes;
        if (!primordial && members == null) {
            componentBytes = getPreviousData(conceptData,
                    OFFSETS.REFSET_DYNAMIC_MEMBERS,
                    OFFSETS.values()[OFFSETS.REFSET_DYNAMIC_MEMBERS.ordinal() + 1]);
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (members != null) {
                RefexDynamicSerializer.get().serialize(new DataOutputStream(output), members);
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

    private static byte[] getPreviousData(I_ManageSimpleConceptData conceptData,
            OFFSETS start,
            OFFSETS end) throws InterruptedException, ExecutionException, IOException {
        assert start != null : "start is null. end: " + end;
        assert end != null : "end is null. start: " + start;
        byte[] output;


        ConceptDataSimpleReference cdsr = (ConceptDataSimpleReference) conceptData;
        byte[] bufferBytes = cdsr.getMutableBytes();
        DataInputStream readWriteInput = new DataInputStream(new ByteArrayInputStream(bufferBytes));

        if (bufferBytes.length > OFFSETS.getHeaderSize()) {
            int offset = start.getOffset(bufferBytes);
            int endOffset = end.getOffset(bufferBytes);
            int byteCount = endOffset - offset;
            readWriteInput.skip(offset);
            assert byteCount >= 0 : " neg byteCount: " + byteCount
                    + " start offset: " + offset + " end offset: " + endOffset
                    + " start: " + start + " end: " + end;
            output = new byte[byteCount];
            System.arraycopy(cdsr.getMutableBytes(), offset, output, 0,
                    byteCount);
        } else {
            output = zeroOutputArray;
        }
        return output;
    }
}
