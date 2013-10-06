/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.model.index.service;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * A
 * <code>Callable&lt;Long&gt;</code> object that will block until the indexer
 * has added the document to the index. The
 * <code>call()</code> method on the object will return the index generation
 * that contains the document, which can be used in search calls to make sure
 * that generation is available to the searcher.
 *
 * @author kec
 */
public class IndexedGenerationCallable implements Callable<Long> {

    private CountDownLatch latch = new CountDownLatch(1);
    private long indexGeneration;

    public void setIndexGeneration(long indexGeneration) {
        this.indexGeneration = indexGeneration;
        latch.countDown();
    }

    @Override
    public Long call() throws Exception {
        latch.await();

        return indexGeneration;
    }
}
