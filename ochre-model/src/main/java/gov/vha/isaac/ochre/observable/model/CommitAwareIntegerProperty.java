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
package gov.vha.isaac.ochre.observable.model;

import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.commit.CommittableComponent;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author kec
 */
public class CommitAwareIntegerProperty extends SimpleIntegerProperty {

    public CommitAwareIntegerProperty(Object bean, String name, int initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    public void set(int newValue) {
        checkChangesAllowed(getBean());
        super.set(newValue); 
    }

    public static void checkChangesAllowed(Object bean) throws RuntimeException {
        if (bean instanceof CommittableComponent) {
            CommittableComponent committableComponent = (CommittableComponent) bean;
            if (committableComponent.getCommitState() == CommitStates.COMMITTED) {
                throw new RuntimeException("Cannot change value, component is already committed.");
            }
        }
    }

    @Override
    public void setValue(Number v) {
        checkChangesAllowed(getBean());
        super.setValue(v); 
    }

}
