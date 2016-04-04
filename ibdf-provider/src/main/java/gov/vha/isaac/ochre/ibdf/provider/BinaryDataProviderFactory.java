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

import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderQueueService;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataServiceFactory;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataWriterService;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class BinaryDataProviderFactory implements BinaryDataServiceFactory {

    @Override
    public BinaryDataReaderService getReader(Path dataPath) throws FileNotFoundException {
        return new BinaryDataReaderProvider(dataPath);
    }
    @Override
    public BinaryDataReaderQueueService getQueueReader(Path dataPath) throws FileNotFoundException {
        return new BinaryDataReaderQueueProvider(dataPath);
    }

    @Override
    public BinaryDataWriterService getWriter(Path dataPath) throws FileNotFoundException {
       return new BinaryDataWriterProvider(dataPath);
    }
}
