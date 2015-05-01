/*
 * Copyright 2015 kec.
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
package org.ihtsdo.otf.tcc.dto.component.sememe;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.logic.LogicByteArrayConverter;
import gov.vha.isaac.ochre.api.sememe.SememeChronicle;
import gov.vha.isaac.ochre.api.sememe.SememeType;
import gov.vha.isaac.ochre.model.coordinate.EditCoordinateImpl;
import gov.vha.isaac.ochre.model.sememe.SememeChronicleImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphRevision;

/**
 *
 * @author kec
 */
public class SememeFromDtoFactory {
    private static LogicByteArrayConverter byteConverter;
    private static LogicByteArrayConverter getLogicByteArrayConverter() {
        return byteConverter;
    }

    private static IdentifierService identifierProvider;

    private static IdentifierService getIdentifierService() {
        if (identifierProvider == null) {
            identifierProvider = LookupService.getService(IdentifierService.class);
        }
        return identifierProvider;
    }

    public static SememeChronicle<?> create(TtkRefexAbstractMemberChronicle<?> eRefsetMember) {
        IdentifierService ids = getIdentifierService();
        int nid = getIdentifierService().getNidForUuids(eRefsetMember.primordialUuid);
        int assemblageSequence = ids.getConceptSequence(ids.getNidForUuids(eRefsetMember.assemblageUuid));
        int referencedComponentNid = ids.getNidForUuids(eRefsetMember.referencedComponentUuid);
        int containerSequence = ids.getSememeSequence(nid);
        EditCoordinateImpl ec = new EditCoordinateImpl(ids.getConceptSequence(ids.getNidForUuids(eRefsetMember.authorUuid)), 
                ids.getConceptSequence(ids.getNidForUuids(eRefsetMember.moduleUuid)), 
                ids.getConceptSequence(ids.getNidForUuids(eRefsetMember.pathUuid)));
        switch (eRefsetMember.getType()) {

            case LOGIC:
                SememeChronicleImpl<LogicGraphSememeImpl> chronicle = 
                        new SememeChronicleImpl(SememeType.LOGIC_GRAPH, 
                            eRefsetMember.primordialUuid, nid, assemblageSequence,
                            referencedComponentNid, containerSequence);    
                
                // primordial version
                LogicGraphSememeImpl graphVersion = chronicle.createMutableUncommittedVersion(LogicGraphSememeImpl.class, 
                        eRefsetMember.status.getState(), 
                        ec);
                TtkLogicGraphMemberChronicle logicGraphMember = (TtkLogicGraphMemberChronicle) eRefsetMember;
                        
                graphVersion.setGraphData(getLogicByteArrayConverter().
                        convertLogicGraphForm(logicGraphMember.logicGraphBytes, DataTarget.INTERNAL));
                graphVersion.setTime(eRefsetMember.time);
               //revisions
                for (TtkRevision r: eRefsetMember.getRevisions()) {
                    ec.setAuthorSequence(ids.getNidForUuids(r.authorUuid));
                    ec.setModuleSequence(ids.getNidForUuids(r.moduleUuid));
                    ec.setPathSequence(ids.getNidForUuids(r.pathUuid));
                    graphVersion = chronicle.createMutableUncommittedVersion(LogicGraphSememeImpl.class, 
                        eRefsetMember.status.getState(), 
                        ec);
                    graphVersion.setGraphData(getLogicByteArrayConverter().
                        convertLogicGraphForm(((TtkLogicGraphRevision)r).logicGraphBytes, DataTarget.INTERNAL));
                    graphVersion.setTime(r.time);
                }
                
                return chronicle;
            case BOOLEAN:
            case CID:
            case CID_CID:
            case CID_CID_CID:
            case CID_CID_STR:
            case CID_INT:
            case CID_STR:
            case INT:
            case CID_FLOAT:
            case MEMBER:
            case STR:
            case CID_LONG:
            case LONG:
            case ARRAY_BYTEARRAY:
            case CID_CID_CID_FLOAT:
            case CID_CID_CID_INT:
            case CID_CID_CID_LONG:
            case CID_CID_CID_STRING:
            case CID_BOOLEAN:
            default:
                throw new UnsupportedOperationException("Can't handle member type: " + eRefsetMember.getType());
        }
    }
}
