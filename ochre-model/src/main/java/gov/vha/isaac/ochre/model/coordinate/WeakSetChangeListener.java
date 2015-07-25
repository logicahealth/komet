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
import javafx.collections.SetChangeListener;

/**
 *
 * @author kec
 */
public class WeakSetChangeListener<T> implements WeakListener, SetChangeListener<T>{

   private final WeakReference<SetChangeListener<T>> ref;

    public WeakSetChangeListener(SetChangeListener<T> listener) {
        this.ref = new WeakReference<>(listener);
    }
    
    @Override
    public boolean wasGarbageCollected() {
        return (ref.get() == null);
    }

    @Override
    public void onChanged(Change<? extends T> change) {
        SetChangeListener<T> listener = ref.get();
        if (listener != null) {
            listener.onChanged(change);
        } else {
            // The weakly reference listener has been garbage collected,
            // so this WeakListener will now unhook itself from the
            // source bean
            change.getSet().removeListener(this);
        }
    }
    
}
