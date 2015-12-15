package gov.vha.isaac.ochre.concept.provider;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.model.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.model.concept.ConceptChronologyImpl;
import gov.vha.isaac.ochre.waitfree.WaitFreeMergeSerializer;

/**
 * The concept serializer is currently updating the taxonomy records every time a concept is serialized. 
 * Created by kec on 5/15/15.
 */
public class ConceptSerializer implements WaitFreeMergeSerializer<ConceptChronologyImpl> {

    @Override
    public void serialize(ByteArrayDataBuffer d, ConceptChronologyImpl conceptChronicle) {
        Get.conceptActiveService().updateStatus(conceptChronicle);

        byte[] data = conceptChronicle.getDataToWrite();
        d.put(data, 0, data.length);
    }

    @Override
    public ConceptChronologyImpl merge(ConceptChronologyImpl a, ConceptChronologyImpl b, int writeSequence) {
        byte[] dataBytes = a.mergeData(writeSequence, b.getDataToWrite(writeSequence));
        ByteArrayDataBuffer db = new ByteArrayDataBuffer(dataBytes);
        return ConceptChronologyImpl.make(db);
    }

    @Override
    public ConceptChronologyImpl deserialize(ByteArrayDataBuffer db) {
        return ConceptChronologyImpl.make(db);
    }

}
