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
package gov.vha.isaac.ochre.api.externalizable;

import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

/**
 * 
 * {@link BinaryDataReaderQueueService}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface BinaryDataReaderQueueService {

    /**
     * Return a queue view of the data reader service - the queue being populated by a multi-threaded operation.
     * Order is not maintained.
     * @return
     */
    BlockingQueue<OchreExternalizable> getQueue();
    
    /**
     * Call to determine if no futher elements will populate the queue
     */
    public boolean isFinished();
    
    /**
     * Cancel any inprogress processing
     */
    public void shutdown();
}
