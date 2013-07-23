/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.chronicle.cc.component;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.OFFSETS;

/**
 *
 * @author maestro
 */
public class AnnotationStyleBinder extends TupleBinding<Boolean> {

    private static AnnotationStyleBinder binder = new AnnotationStyleBinder();

    public static AnnotationStyleBinder getBinder() {
        return binder;
    }

    @Override
    public Boolean entryToObject(TupleInput ti) {
        ti.skipFast(OFFSETS.ANNOTATION_STYLE_REFSET.getOffset());
        return ti.readByte() >= 1;
    }

    @Override
    public void objectToEntry(Boolean arg0, TupleOutput to) {
        throw new UnsupportedOperationException();
    }
}
