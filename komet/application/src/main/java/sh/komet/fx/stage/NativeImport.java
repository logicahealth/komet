/*
 * Copyright 2018 ISAAC's KOMET Collaborators.
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
package sh.komet.fx.stage;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;

/**
 *
 * @author kec
 */
public class NativeImport extends TimedTaskWithProgressTracker<Integer> {

    final File importFile;

    public NativeImport(File importFile) {
        this.importFile = importFile;
        updateTitle("Native import from " + importFile.getName());
        Get.activeTasks().add(this);
    }

    @Override
    protected Integer call() throws Exception {
        try (ZipFile zipFile = new ZipFile(importFile, Charset.forName("UTF-8"))) {
            ZipEntry identifierEntry = zipFile.getEntry("identifiers");
            DataInputStream dis = new DataInputStream(zipFile.getInputStream(identifierEntry));
            int identifierCount = dis.readInt();
            LOG.info("Identifier count: " + identifierCount);
            addToTotalWork(2 * identifierCount);
            updateMessage("Importing identifiers...");
            for (int i = 0; i < identifierCount; i++) {
                int nid = dis.readInt();
                int uuidLength = dis.readInt();
                UUID[] uuids = new UUID[uuidLength];
                for (int uuidIndex = 0; uuidIndex < uuidLength; uuidIndex++) {
                    uuids[uuidIndex] = new UUID(dis.readLong(), dis.readLong());
                }
                IsaacObjectType objectType = IsaacObjectType.fromToken(dis.readByte());
                VersionType versionType = VersionType.getFromToken(dis.readByte());
                // DO something with the identifier data before going to the next one.
                completedUnitOfWork();

            }
            dis.close();
            
            updateMessage("Importing Types...");
            ZipEntry typeEntry = zipFile.getEntry("types");
            DataInputStream typeIs = new DataInputStream(zipFile.getInputStream(typeEntry));
            int typeCount = typeIs.readInt();
            for (int i = 0; i < typeCount; i++) {
                int nid = typeIs.readInt();
                IsaacObjectType objectType = IsaacObjectType.fromToken((byte) typeIs.readInt());
                VersionType versionType = VersionType.getFromToken((byte) typeIs.readInt());
                // do something with the version info. 
            }
            typeIs.close();
            
            updateMessage("Importing STAMPs...");
            ZipEntry stampEntry = zipFile.getEntry("stamp");
            DataInputStream stampIs = new DataInputStream(zipFile.getInputStream(stampEntry));
            int stampCount = stampIs.readInt();
            for (int i = 0; i < stampCount; i++) {
                int stampSequence = stampIs.readInt();
                Status status = Status.valueOf(stampIs.readUTF());
                long time = stampIs.readLong();
                int authorNid = stampIs.readInt();
                int moduleNid = stampIs.readInt();
                int pathNid = stampIs.readInt();
                // do something with the stamp...
            }
            stampIs.close();
            
            updateMessage("Importing Taxonomy...");
            ZipEntry taxonomyEntry = zipFile.getEntry("taxonomy");
            DataInputStream taxonomyIs = new DataInputStream(zipFile.getInputStream(taxonomyEntry));
            int taxonomyCount = taxonomyIs.readInt();
            addToTotalWork(taxonomyCount);
 
            for (int i = 0; i < taxonomyCount; i++) {
                int nid = taxonomyIs.readInt();
                int taxonomyArraySize = taxonomyIs.readInt();
                int[] taxonomyData = new int[taxonomyArraySize];
                for (int j = 0; j < taxonomyArraySize; j++) {
                    taxonomyData[j] = taxonomyIs.readInt();
                }
                // do something with the taxonomy data...
                completedUnitOfWork();
            }
            taxonomyIs.close();
            
            updateMessage("Importing objects...");
            ZipEntry ibdfEntry = zipFile.getEntry("ibdf");
            DataInputStream ibdfIs = new DataInputStream(zipFile.getInputStream(ibdfEntry));
            try {
                while (true) {
                    readObject(ibdfIs);
                }
            } catch (EOFException ex) {
                // assuming we find an end of file exception...
            }
        }
        return 0;
    }

    protected void readObject(DataInputStream ibdfIs) throws IOException, IllegalStateException, UnsupportedOperationException {
        IsaacObjectType objectType = IsaacObjectType.fromToken(ibdfIs.readByte());
        int listSize = ibdfIs.readInt();
        byte[][] dataToRead = new byte[listSize][];
        for (int i = 0; i < listSize; i++) {
            int byteArraySize = ibdfIs.readInt();
            byte[] dataRead = new byte[byteArraySize];
            ibdfIs.readFully(dataRead);
            dataToRead[i] = dataRead;
        }

        int size = 0;
        for (byte[] dataEntry : dataToRead) {
            size = size + dataEntry.length;
        }
        ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(
                size + 4);  // room for 0 int value at end to indicate last version
        for (int i = 0; i < dataToRead.length; i++) {
            if (i == 0) {
                // discard the 0 integer at the beginning of the record.
                // 0 put in to enable the chronicle to sort before the versions.
                if (dataToRead[0][0] != 0 && dataToRead[0][1] != 0 && dataToRead[0][2] != 0 && dataToRead[0][3] != 0) {
                    throw new IllegalStateException("Record does not start with zero...");
                }
                byteBuffer.put(dataToRead[0], 4, dataToRead[0].length - 4);
            } else {
                byteBuffer.put(dataToRead[i]);
            }

        }

        byteBuffer.putInt(0);
        byteBuffer.rewind();

        switch (objectType) {
            case CONCEPT:
                IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
                ConceptChronologyImpl temp = ConceptChronologyImpl.make(byteBuffer);
                // do something with concept...
                break;

            case SEMANTIC:
                IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
                SemanticChronologyImpl.make(byteBuffer);
                // do something with semantic...
                break;
            default:
            // unexpected...
        }
        completedUnitOfWork();
    }

}
