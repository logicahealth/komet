/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.ibdf.provider;

import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.task.TimedTaskWithProgressTracker;
import gov.vha.isaac.ochre.model.concept.ConceptChronologyImpl;
import gov.vha.isaac.ochre.model.data.CommitRecordImpl;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author kec
 */
public class BinaryDataReaderProvider 
        extends TimedTaskWithProgressTracker<Integer> 
        implements BinaryDataReaderService, Spliterator<OchreExternalizable> {

    Path dataPath;
    DataInputStream input;
    int streamBytes;
    int objects = 0;
    CountDownLatch complete = new CountDownLatch(1);

    public BinaryDataReaderProvider(Path dataPath) throws FileNotFoundException {
        this.dataPath = dataPath;
        this.input = new DataInputStream(new FileInputStream(dataPath.toFile()));
        try {
            streamBytes = input.available();
            addToTotalWork(streamBytes);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Stream<OchreExternalizable> getStream() {
        running();
        return StreamSupport.stream(this, false);
     }

    /**
     * 
     * @return the number of objects read. 
      */
    @Override
    protected Integer call() {
        try {
            complete.await();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return objects;
    }

    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super OchreExternalizable> action) {
        try {
                int startBytes = input.available();
                OchreExternalizableObjectType type = OchreExternalizableObjectType.fromDataStream(input);
                byte dataFormatVersion = input.readByte();
                int recordSize = input.readInt();
                byte[] objectData = new byte[recordSize];
                input.readFully(objectData);
                ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(objectData);
                buffer.setExternalData(true);
                buffer.setObjectDataFormatVersion(dataFormatVersion);
                switch (type) {
                    case COMMIT_RECORD:
                       action.accept(new CommitRecordImpl(dataFormatVersion, buffer));
                       break;
                    case CONCEPT:
                        action.accept(ConceptChronologyImpl.make(buffer));
                       break;
                    case SEMEME:
                        action.accept(SememeChronologyImpl.make(buffer));
                       break;
                    default:
                        throw new UnsupportedOperationException("Can't handle: " + type);
                }
                objects++;
                completedUnitsOfWork(startBytes - input.available());
                return true;
            } catch (EOFException ex) {
                close();
                return false;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
    }

    @Override
    public Spliterator<OchreExternalizable> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE + NONNULL;
    }

}
