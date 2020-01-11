/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.coordinate;

import java.util.*;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampCoordinateReadOnly;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;

/**
 *
 * @author kec
 */
public class StampCoordinateImmutableWrapper implements StampCoordinateReadOnly {
    private final StampCoordinateImpl stampCoordinate;

    public StampCoordinateImmutableWrapper(StampCoordinateImpl stampCoordinate) {
        this.stampCoordinate = stampCoordinate;
    }

    @Override
    public boolean equals(Object obj) {
        return stampCoordinate.equals(obj);
    }

    @Override
    public int hashCode() {
        return stampCoordinate.hashCode();
    }

    @Override
    public StampCoordinateImpl makeCoordinateAnalog(long stampPositionTime) {
        return stampCoordinate.makeCoordinateAnalog(stampPositionTime);
    }

    @Override
    public StampCoordinateImpl makeCoordinateAnalog(Status... states) {
        return (StampCoordinateImpl) stampCoordinate.makeCoordinateAnalog(states);
    }

    @Override
    public StampCoordinate makeCoordinateAnalog(Set<Status> states) {
        return stampCoordinate.makeCoordinateAnalog(states);
    }

    @Override
    public StampCoordinate makeModuleAnalog(Collection<ConceptSpecification> modules, boolean add) {
        return stampCoordinate.makeModuleAnalog(modules, add);
    }

    @Override
    public StampCoordinate makePathAnalog(ConceptSpecification pathForPosition) {
        return stampCoordinate.makePathAnalog(pathForPosition);
    }

    @Override
    public String toString() {
        return stampCoordinate.toString();
    }

    @Override
    public EnumSet<Status> getAllowedStates() {
        return stampCoordinate.getAllowedStates();
    }

    @Override
    public NidSet getModuleNids() {
        return stampCoordinate.getModuleNids();
    }

    @Override
    public StampPosition getStampPosition() {
        return stampCoordinate.getStampPosition();
    }

    @Override
    public StampPrecedence getStampPrecedence() {
        return stampCoordinate.getStampPrecedence();
    }

    public ChangeListener<StampPrecedence> setStampPrecedenceProperty(ObjectProperty<StampPrecedence> stampPrecedenceProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StampCoordinateImpl deepClone() {
        return stampCoordinate.deepClone();
    }

    @Override
    public List<ConceptSpecification> getModulePreferenceOrderForVersions() {
        return stampCoordinate.getModulePreferenceOrderForVersions();
    }

    @Override
    public Set<ConceptSpecification> getModuleSpecifications() {
        return stampCoordinate.getModuleSpecifications();
    }

    @Override
    public Set<ConceptSpecification> getAuthorSpecifications() {
        return stampCoordinate.getAuthorSpecifications();
    }

    @Override
    public UUID getStampCoordinateUuid() {
        return stampCoordinate.getStampCoordinateUuid();
    }

    @Override
    public NidSet getAuthorNids() {
        return stampCoordinate.getAuthorNids();
    }

    @Override
    public String toUserString() {
        return this.stampCoordinate.toUserString();
    }

    @Override
    public void putExternal(ByteArrayDataBuffer out) {
        stampCoordinate.putExternal(out);
    }

    @Override
    public StampCoordinateReadOnly getStampCoordinateReadOnly() {
        return this;
    }
}
