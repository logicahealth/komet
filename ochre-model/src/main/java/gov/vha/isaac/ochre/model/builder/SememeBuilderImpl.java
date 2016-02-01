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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY_STATE_SET KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model.builder;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.IdentifiedComponentBuilder;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.version.ComponentNidSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LongSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;

/**
 *
 * @author kec
 * @param <C>
 */
public class SememeBuilderImpl<C extends SememeChronology<? extends SememeVersion<?>>> extends ComponentBuilder<C> implements SememeBuilder<C> {

    IdentifiedComponentBuilder referencedComponentBuilder;
    int referencedComponentNid = Integer.MAX_VALUE;
    
    int assemblageConceptSequence;
    SememeType sememeType;
    Object[] parameters;

    public SememeBuilderImpl(IdentifiedComponentBuilder referencedComponentBuilder, 
            int assemblageConceptSequence, 
            SememeType sememeType, Object... paramaters) {
        this.referencedComponentBuilder = referencedComponentBuilder;
        this.assemblageConceptSequence = assemblageConceptSequence;
        this.sememeType = sememeType;
        this.parameters = paramaters;
    }
    public SememeBuilderImpl(int referencedComponentNid, 
            int assemblageConceptSequence, 
            SememeType sememeType, Object... paramaters) {
        this.referencedComponentNid = referencedComponentNid;
        this.assemblageConceptSequence = assemblageConceptSequence;
        this.sememeType = sememeType;
        this.parameters = paramaters;
    }
    
    @Override
    public C build(EditCoordinate editCoordinate, 
            ChangeCheckerMode changeCheckerMode,
            List builtObjects) throws IllegalStateException {
        if (referencedComponentNid == Integer.MAX_VALUE) {
            referencedComponentNid = Get.identifierService().getNidForUuids(referencedComponentBuilder.getUuids());
        }
        SememeChronologyImpl sememeChronicle = new SememeChronologyImpl(sememeType, 
                getPrimordialUuid(), 
                Get.identifierService().getNidForUuids(this.getUuids()), 
            assemblageConceptSequence, 
            referencedComponentNid, 
            Get.identifierService().getSememeSequenceForUuids(this.getUuids()));
        sememeChronicle.setAdditionalUuids(additionalUuids);
        switch (sememeType) {
            case COMPONENT_NID:
                ComponentNidSememeImpl cnsi = (ComponentNidSememeImpl) 
                        sememeChronicle.createMutableVersion(ComponentNidSememeImpl.class, State.ACTIVE, editCoordinate);
                cnsi.setComponentNid((Integer) parameters[0]);
                break;
            case LONG:
                LongSememeImpl lsi = (LongSememeImpl) 
                        sememeChronicle.createMutableVersion(LongSememeImpl.class, State.ACTIVE, editCoordinate);
                lsi.setLongValue((Long) parameters[0]);
                break;
            case LOGIC_GRAPH:
                LogicGraphSememeImpl lgsi = (LogicGraphSememeImpl) 
                        sememeChronicle.createMutableVersion(LogicGraphSememeImpl.class, State.ACTIVE, editCoordinate);
                lgsi.setGraphData(((LogicalExpression) parameters[0]).getData(DataTarget.INTERNAL));
                break;
            case MEMBER:
                SememeVersionImpl svi = (SememeVersionImpl)
                        sememeChronicle.createMutableVersion(SememeVersionImpl.class, State.ACTIVE, editCoordinate);
                break;
            case STRING:
                StringSememeImpl ssi = (StringSememeImpl)
                    sememeChronicle.createMutableVersion(StringSememeImpl.class, State.ACTIVE, editCoordinate);
                ssi.setString((String) parameters[0]);
                break;
            case DESCRIPTION: {
                DescriptionSememeImpl dsi = (DescriptionSememeImpl)
                    sememeChronicle.createMutableVersion(DescriptionSememeImpl.class, State.ACTIVE, editCoordinate);
                dsi.setCaseSignificanceConceptSequence((Integer) parameters[0]);
                dsi.setDescriptionTypeConceptSequence((Integer) parameters[1]);
                dsi.setLanguageConceptSequence((Integer) parameters[2]);
                dsi.setText((String) parameters[3]);
                break;
            }
            case DYNAMIC: {
                DynamicSememeImpl dsi = (DynamicSememeImpl)sememeChronicle.createMutableVersion(DynamicSememeImpl.class, State.ACTIVE, editCoordinate);
                if (parameters != null && parameters.length > 0) {
                    //See notes in SememeBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
                    dsi.setData(((AtomicReference<DynamicSememeData[]>)parameters[0]).get());
                }
                //TODO DAN this needs to fire the validator!
                break;
            }	
            default:
                throw new UnsupportedOperationException("Can't handle: " + sememeType);
        }
        
        if (changeCheckerMode == ChangeCheckerMode.ACTIVE) {
            Get.commitService().addUncommitted(sememeChronicle);
        } else {
            Get.commitService().addUncommittedNoChecks(sememeChronicle);
        }
        sememeBuilders.forEach((builder) -> builder.build(editCoordinate, changeCheckerMode, builtObjects));
        builtObjects.add(sememeChronicle);
        return (C) sememeChronicle;
    }

    @Override
    public C build(int stampSequence,
            List builtObjects) throws IllegalStateException {
        if (referencedComponentNid == Integer.MAX_VALUE) {
            referencedComponentNid = Get.identifierService().getNidForUuids(referencedComponentBuilder.getUuids());
        }
        SememeChronologyImpl sememeChronicle = new SememeChronologyImpl(sememeType, 
                getPrimordialUuid(), 
                Get.identifierService().getNidForUuids(this.getUuids()), 
            assemblageConceptSequence, 
            referencedComponentNid, 
            Get.identifierService().getSememeSequenceForUuids(this.getUuids()));
        sememeChronicle.setAdditionalUuids(additionalUuids);
        switch (sememeType) {
            case COMPONENT_NID:
                ComponentNidSememeImpl cnsi = (ComponentNidSememeImpl) 
                        sememeChronicle.createMutableVersion(ComponentNidSememeImpl.class, stampSequence);
                cnsi.setComponentNid((Integer) parameters[0]);
                break;
            case LONG:
                LongSememeImpl lsi = (LongSememeImpl) 
                        sememeChronicle.createMutableVersion(LongSememeImpl.class, stampSequence);
                lsi.setLongValue((Long) parameters[0]);
                break;
            case LOGIC_GRAPH:
                LogicGraphSememeImpl lgsi = (LogicGraphSememeImpl) 
                        sememeChronicle.createMutableVersion(LogicGraphSememeImpl.class, stampSequence);
                lgsi.setGraphData(((LogicalExpression) parameters[0]).getData(DataTarget.INTERNAL));
                break;
            case MEMBER:
                SememeVersionImpl svi = (SememeVersionImpl)
                        sememeChronicle.createMutableVersion(SememeVersionImpl.class, stampSequence);
                break;
            case STRING:
                StringSememeImpl ssi = (StringSememeImpl)
                    sememeChronicle.createMutableVersion(StringSememeImpl.class, stampSequence);
                ssi.setString((String) parameters[0]);
                break;
            case DESCRIPTION: {
                DescriptionSememeImpl dsi = (DescriptionSememeImpl)
                    sememeChronicle.createMutableVersion(DescriptionSememeImpl.class, stampSequence);
                dsi.setCaseSignificanceConceptSequence((Integer) parameters[0]);
                dsi.setDescriptionTypeConceptSequence((Integer) parameters[1]);
                dsi.setLanguageConceptSequence((Integer) parameters[2]);
                dsi.setText((String) parameters[3]);
                break;
            }
            case DYNAMIC: {
                DynamicSememeImpl dsi = (DynamicSememeImpl)sememeChronicle.createMutableVersion(DynamicSememeImpl.class, stampSequence);
                if (parameters != null && parameters.length > 0) {
                    //See notes in SememeBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
                    dsi.setData(((AtomicReference<DynamicSememeData[]>)parameters[0]).get());
                }
                //TODO Dan this needs to fire the validator!
                break;
            }	
            default:
                throw new UnsupportedOperationException("Can't handle: " + sememeType);
        }
        sememeBuilders.forEach((builder) -> builder.build(stampSequence, builtObjects));
        builtObjects.add(sememeChronicle);
        return (C) sememeChronicle;    
    }


}
