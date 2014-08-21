/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.otf.tcc.model.cc.component;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.AnalogBI;
import org.ihtsdo.otf.tcc.api.AnalogGeneratorBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.identifier.IdentifierVersion;

/**
 * @param <R>
 * @param <C>
 */
public abstract class Version<R extends Revision<R, C>, C extends ConceptComponent<R, C>> implements ComponentVersionBI, AnalogGeneratorBI<R> {
    /**
     * Field description
     */
    protected ComponentVersionBI cv;
    protected ConceptComponent<R, C> cc = null;
    
    public Version(){}
    
    public Version(ConceptComponent<R, C> cc) {
        this.cc = cc;
    }

    /**
     * Constructs ...
     *
     *
     * @param cv
     */
    public Version(ComponentVersionBI cv, ConceptComponent<R, C> cc) {
        super();
        this.cc = cc;
        this.cv = cv;
    }

    public boolean isIndexed() {
        return PersistentStore.get().isIndexed(cc.nid);
    }

    public void setIndexed() {
        if (!isUncommitted()) {
            PersistentStore.get().setIndexed(cc.nid, true);
        }
    }

    @Override
    public boolean isActive() {
        return cv.getStatus() == Status.ACTIVE;
    }

    /**
     * Method description
     *
     *
     * @param annotation
     *
     * @return
     *
     * @throws IOException
     */
    @SuppressWarnings(value = "rawtypes")
    @Override
    public boolean addAnnotation(RefexChronicleBI annotation) throws IOException {
        return cc.addAnnotation(annotation);
    }

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (Version.class.isAssignableFrom(obj.getClass())) {
            Version another = (Version) obj;
            if ((this.getNid() == another.getNid()) && (this.getStamp() == another.getStamp())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method description
     *
     *
     * @param another
     *
     * @return
     */
    public abstract boolean fieldsEqual(Version<R,C> another); //TODO-AKF: make sure signature is correct

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{this.getStamp(), cc.nid});
    }

    /**
     * Method description
     *
     *
     * @param ec
     * @param vc
     *
     * @return
     *
     * @throws IOException
     */
    public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
        return cc.makeAdjudicationAnalogs(ec, vc);
    }

    /**
     * Method description
     *
     *
     * @param min
     * @param max
     *
     * @return
     */
    @Override
    public boolean stampIsInRange(int min, int max) {
        return cv.stampIsInRange(min, max);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toString() {
        return "Version: " + cv.toString();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toUserString() {
        return cv.toUserString();
    }

    /**
     * Method description
     *
     *
     * @param snapshot
     *
     * @return
     *
     * @throws ContradictionException
     * @throws IOException
     */
    @Override
    public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
        return cv.toUserString(snapshot);
    }

    /**
     * Method description
     *
     *
     * @param vc1
     * @param vc2
     * @param compareAuthoring
     *
     * @return
     */
    @Override
    public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
        return cc.versionsEqual(vc1, vc2, compareAuthoring);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<IdentifierVersion> getAdditionalIdentifierParts() {
        if (cc.additionalIdVersions == null) {
            return Collections.unmodifiableList(new ArrayList<IdentifierVersion>());
        }
        return Collections.unmodifiableList(cc.additionalIdVersions);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Collection<? extends IdBI> getAdditionalIds() {
        return cc.getAdditionalIds();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Collection<? extends IdBI> getAllIds() {
        return cc.getIdVersions();
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Set<Integer> getAllNidsForVersion() throws IOException {
        return cv.getAllNidsForVersion();
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    public Set<Integer> getAllStamps() throws IOException {
        return cc.getAllStamps();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
        return cc.getAnnotations();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getAuthorNid() {
        return cv.getAuthorNid();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public ComponentChronicleBI getChronicle() {
        return cc.getChronicle();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getConceptNid() {
        return cc.enclosingConceptNid;
    }

    /**
     * Method description
     *
     *
     * @param xyz
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz) throws IOException {
        return cc.getAnnotationsActive(xyz);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param cls
     * @param <T>
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz, Class<T> cls) throws IOException {
        return cc.getAnnotationsActive(xyz, cls);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refexNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz, int refexNid) throws IOException {
        return cc.getAnnotationsActive(xyz, refexNid);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refexNid
     * @param cls
     * @param <T>
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz, int refexNid, Class<T> cls) throws IOException {
        return cc.getAnnotationsActive(xyz, refexNid, cls);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refsetNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid) throws IOException {
        return cc.getRefexMembersActive(xyz, refsetNid);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException {
        return cc.getRefexMembersActive(xyz);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException {
        return cc.getRefexMembersInactive(xyz);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getModuleNid() {
        return cv.getModuleNid();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getNid() {
        return cc.nid;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getPathNid() {
        return cv.getPathNid();
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Position getPosition() throws IOException {
        return cv.getPosition();
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    public Set<Position> getPositions() throws IOException {
        return cc.getPositions();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public UUID getPrimordialUuid() {
        return new UUID(cc.primordialMsb, cc.primordialLsb);
    }

    /**
     * Method description
     *
     *
     * @param refsetNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
        return cc.getRefexMembers(refsetNid);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
        return cc.getRefexes();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public R getRevision() {
        if (cv == cc) {
            return makeAnalog(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid());
        }
        return (R) cv; //TODO-AKF: not sure casting and return type is correct
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getStamp() {
        return cv.getStamp();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Status getStatus() {
        return cv.getStatus();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public long getTime() {
        return cv.getTime();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public List<UUID> getUUIDs() {
        return cc.getUUIDs();
    }

    //    @Override
    //    public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet) {
    //        return ConceptComponent.this.getVisibleIds(viewpointSet);
    //    }
    //
    //    @Override
    //    public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet, int... authorityNids) {
    //        return ConceptComponent.this.getVisibleIds(viewpointSet, authorityNids);
    //    }
    /**
     * Method description
     *
     *
     * @param xyz
     * @param refsetNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
        return cc.hasCurrentAnnotationMember(xyz, refsetNid);
    }

    /**
     * Method description
     *
     *
     * @param xyz
     * @param refsetNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
        return cc.hasCurrentRefexMember(xyz, refsetNid);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public boolean isBaselineGeneration() {
        return cv == cc; //TODO-AKF: is this okay?
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public boolean isUncommitted() {
        return getTime() == Long.MAX_VALUE;
    }

    /**
     * Method description
     *
     *
     * @param authorNid
     *
     * @throws PropertyVetoException
     */
    public void setAuthorNid(int authorNid) throws PropertyVetoException {
        ((AnalogBI) cv).setAuthorNid(authorNid);
    }

    /**
     * Method description
     *
     *
     * @param moduleNid
     *
     * @throws PropertyVetoException
     */
    public void setModuleNid(int moduleNid) throws PropertyVetoException {
        ((AnalogBI) cv).setModuleNid(moduleNid);
    }

    /**
     * Method description
     *
     *
     * @param nid
     *
     * @throws PropertyVetoException
     */
    public void setNid(int nid) throws PropertyVetoException { //TODO-AKF: removing final
        ((AnalogBI) cv).setNid(nid);
    }

    /**
     * Method description
     *
     *
     * @param pathId
     *
     * @throws PropertyVetoException
     */
    public void setPathNid(int pathId) throws PropertyVetoException {
        ((AnalogBI) cv).setPathNid(pathId);
    }

    /**
     * Method description
     *
     *
     * @param statusNid
     *
     * @throws PropertyVetoException
     */
    public void setStatus(Status status) throws PropertyVetoException {
        ((AnalogBI) cv).setStatus(status);
    }

    /**
     * Method description
     *
     *
     * @param time
     *
     * @throws PropertyVetoException
     */
    public void setTime(long time) throws PropertyVetoException {
        ((AnalogBI) cv).setTime(time);
    }

    @Override
    public Collection<? extends RefexDynamicVersionBI<?>> getRefexesDynamicActive(ViewCoordinate viewCoordinate) throws IOException
    {
        return cc.getRefexesDynamicActive(viewCoordinate);
    }
    /**
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamic()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexesDynamic() throws IOException
    {
        return cc.getRefexesDynamic();
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicAnnotations()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicAnnotations() throws IOException
    {
        return cc.getRefexDynamicAnnotations();
    }
    /**
     * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicMembers()
     */
    @Override
    public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicMembers() throws IOException
    {
        return cc.getRefexDynamicMembers();
    }

    @Override
    public boolean addDynamicAnnotation(RefexDynamicChronicleBI<?> annotation) throws IOException {
        return cc.addDynamicAnnotation(annotation);
    }
}

