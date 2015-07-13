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
package gov.vha.isaac.ochre.model.coordinate;

import java.lang.ref.WeakReference;
import javafx.beans.WeakListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ArrayChangeListener;
import javafx.collections.ObservableIntegerArray;

/**
 *
 * @author kec
 */
public class WeakArrayChangeListener implements WeakListener, ArrayChangeListener<ObservableIntegerArray>{

    private final WeakReference<ArrayChangeListener<ObservableIntegerArray>> ref;

    public WeakArrayChangeListener(ArrayChangeListener<ObservableIntegerArray> listener) {
        this.ref = new WeakReference<>(listener);
    }

    @Override
    public boolean wasGarbageCollected() {
        return (ref.get() == null);
    }

    @Override
    public void onChanged(ObservableIntegerArray observableArray, boolean sizeChanged, int from, int to) {
        ArrayChangeListener<ObservableIntegerArray> listener = ref.get();
        if (listener != null) {
            listener.onChanged(observableArray, sizeChanged, from, to);
        } else {
            // The weakly reference listener has been garbage collected,
            // so this WeakListener will now unhook itself from the
            // source bean
            observableArray.removeListener(this);
        }
    }
    
}
