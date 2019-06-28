/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.model.builder;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------
import javafx.concurrent.Task;

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.LookupService;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidFactory;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.DynamicImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.model.semantic.version.LongVersionImpl;
import sh.isaac.model.semantic.version.SemanticVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticBuildListenerI;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.model.ModelGet;

//~--- classes ----------------------------------------------------------------
/**
 * The Class SemanticBuilderImpl.
 *
 * @author kec
 * @param <C> the generic type
 */
public class SemanticBuilderImpl<C extends SemanticChronology>
        extends ComponentBuilder<C>
        implements SemanticBuilder<C> {

    /**
     * The referenced component nid.
     */
    int referencedComponentNid = Integer.MAX_VALUE;

    /**
     * The referenced component builder.
     */
    IdentifiedComponentBuilder<?> referencedComponentBuilder;

    /**
     * The semantic type.
     */
    VersionType semanticType;

    /**
     * The parameters.
     */
    Object[] parameters;
    
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Instantiates a new semantic builder impl.
     *
     * @param referencedComponentBuilder the referenced component builder
     * @param assemblageConceptId the assemblage concept nid
     * @param semanticType the semantic type
     * @param paramaters the paramaters
     */
    public SemanticBuilderImpl(IdentifiedComponentBuilder<?> referencedComponentBuilder,
            int assemblageConceptId,
            VersionType semanticType,
            Object... paramaters) {
        super(assemblageConceptId);
        this.referencedComponentBuilder = referencedComponentBuilder;
        this.semanticType = semanticType;
        this.parameters = paramaters;
    }

    /**
     * Instantiates a new semantic builder impl.
     *
     * @param referencedComponentNid the referenced component nid
     * @param assemblageConceptId the assemblage concept nid
     * @param semanticType the semantic type
     * @param paramaters the paramaters
     */
    public SemanticBuilderImpl(int referencedComponentNid,
            int assemblageConceptId,
            VersionType semanticType,
            Object... paramaters) {
        super(assemblageConceptId);
        this.referencedComponentNid = referencedComponentNid;
        this.semanticType = semanticType;
        this.parameters = paramaters;
    }

    @Override
    public SemanticBuilderImpl<C> setPrimordialUuid(UUID uuid) {
        return (SemanticBuilderImpl<C>) super.setPrimordialUuid(uuid); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SemanticBuilderImpl<C> setPrimordialUuid(String uuidString) {
        return (SemanticBuilderImpl<C>) super.setPrimordialUuid(uuidString); //To change body of generated methods, choose Tools | Templates.
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Builds the.
     *
     * @param stampSequence the stamp sequence
     * @param builtObjects the built objects
     * @return the c
     * @throws IllegalStateException the illegal state exception
     */
    @Override
    public C build(Transaction transaction, int stampSequence,
                   List<Chronology> builtObjects)
            throws IllegalStateException {
        if (this.referencedComponentNid == Integer.MAX_VALUE) {
            this.referencedComponentNid = Get.identifierService()
                    .getNidForUuids(this.referencedComponentBuilder.getUuids());
        }
        
        if (getModule().isPresent()) {
            Stamp requested = Get.stampService().getStamp(stampSequence);
            stampSequence = Get.stampService().getStampSequence(requested.getStatus(), requested.getTime(), requested.getAuthorNid(), getModule().get().getNid(), requested.getPathNid());
        }
         
        final int finalStamp = stampSequence;
        
        List<SemanticBuildListenerI> semanticBuildListeners = LookupService.get().getAllServices(SemanticBuildListenerI.class);
        for (SemanticBuildListenerI listener : semanticBuildListeners) {
            if (listener != null) {
                if (listener.isEnabled()) {
                    // LOG.info("Calling " + listener.getListenerName() + ".applyBefore(...)");
                    try {
                        listener.applyBefore(finalStamp, builtObjects);
                    } catch (RuntimeException e) {
                        LOG.error("FAILED running " + listener.getListenerName() + ".applyBefore(...): ", e);
                    }
                } else {
                    LOG.info("NOT calling " + listener.getListenerName() + ".applyBefore(...) because listener has been disabled");
                }
            }
        }
        SemanticVersion version = null;
        SemanticChronologyImpl semanticChronicle;
        
        final int semanticNid = getNid();
        if (Get.assemblageService().hasSemantic(semanticNid)) {
            semanticChronicle = (SemanticChronologyImpl) Get.assemblageService()
                    .getSemanticChronology(semanticNid);
            
            if ((semanticChronicle.getVersionType() != this.semanticType)
                    || !semanticChronicle.isIdentifiedBy(getPrimordialUuid())
                    || (semanticChronicle.getAssemblageNid() != this.assemblageId)
                    || (semanticChronicle.getReferencedComponentNid() != this.referencedComponentNid)) {
                throw new RuntimeException("Builder is being used to attempt a mis-matched edit of an existing semantic!");
            }
        } else {
            semanticChronicle = new SemanticChronologyImpl(this.semanticType,
                    getPrimordialUuid(),
                    this.assemblageId,
                    this.referencedComponentNid);
        }
        
        semanticChronicle.setAdditionalUuids(this.additionalUuids);
        
        switch (this.semanticType) {
            case COMPONENT_NID:
                final ComponentNidVersionImpl cnsi
                        = (ComponentNidVersionImpl) semanticChronicle.createMutableVersion(finalStamp);
                
                cnsi.setComponentNid((Integer) this.parameters[0]);
                version = cnsi;
                break;
            
            case Nid1_Int2:
                final Nid1_Int2_Version nid1int2
                        = (Nid1_Int2_Version) semanticChronicle.createMutableVersion(finalStamp);
                
                nid1int2.setNid1((Integer) this.parameters[0]);
                nid1int2.setInt2((Integer) this.parameters[1]);
                version = nid1int2;
                
                break;
            
            case LONG:
                final LongVersionImpl lsi = (LongVersionImpl) semanticChronicle.createMutableVersion(finalStamp);
                version = lsi;
                lsi.setLongValue((Long) this.parameters[0]);
                break;
            
            case LOGIC_GRAPH:
                final LogicGraphVersionImpl lgsi
                        = (LogicGraphVersionImpl) semanticChronicle.createMutableVersion(finalStamp);
                version = lgsi;
                lgsi.setGraphData(((LogicalExpression) this.parameters[0]).getData(DataTarget.INTERNAL));
                break;
            
            case MEMBER:
                SemanticVersionImpl svi = semanticChronicle.createMutableVersion(finalStamp);
                version = svi;
                break;
            
            case STRING:
                final StringVersionImpl ssi = (StringVersionImpl) semanticChronicle.createMutableVersion(finalStamp);
                version = ssi;
                ssi.setString((String) this.parameters[0]);
                break;
            
            case DESCRIPTION: {
                final DescriptionVersionImpl dsi
                        = (DescriptionVersionImpl) semanticChronicle.createMutableVersion(finalStamp);
                version = dsi;
                dsi.setCaseSignificanceConceptNid((Integer) this.parameters[0]);
                dsi.setDescriptionTypeConceptNid((Integer) this.parameters[1]);
                dsi.setLanguageConceptNid((Integer) this.parameters[2]);
                dsi.setText((String) this.parameters[3]);
                break;
            }
            
            case DYNAMIC: {
                final DynamicImpl dsi = (DynamicImpl) semanticChronicle.createMutableVersion(finalStamp);
                if (referencedComponentBuilder != null) {
                    dsi.setReferencedComponentVersionType(referencedComponentBuilder.getVersionType());
                }
                
                version = dsi;
                if ((this.parameters != null) && (this.parameters.length > 0)) {
                    // See notes in SemanticBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
                    dsi.setData(((AtomicReference<DynamicData[]>) this.parameters[0]).get());
                }

                // TODO [Dan 2] this needs to fire the validator!
                break;
            }
            
            default:
                throw new UnsupportedOperationException("p Can't handle: " + this.semanticType);
        }
        transaction.addVersionToTransaction(version);
        getSemanticBuilders().forEach((builder) -> builder.build(transaction, finalStamp, builtObjects));
        builtObjects.add(semanticChronicle);
        ModelGet.identifierService().setupNid(semanticChronicle.getNid(), semanticChronicle.getAssemblageNid(), semanticChronicle.getIsaacObjectType(), semanticChronicle.getVersionType());

        for (SemanticBuildListenerI listener : semanticBuildListeners) {
            if (listener != null) {
                if (listener.isEnabled()) {
                    // LOG.info("Calling " + listener.getListenerName() + ".applyAfter(...)");
                    try {
                        listener.applyAfter(finalStamp, version, builtObjects);
                    } catch (RuntimeException e) {
                        LOG.error("FAILED running " + listener.getListenerName() + ".applyAfter(...): ", e);
                    }
                } else {
                    LOG.info("NOT calling " + listener.getListenerName() + ".applyAfter(...) because listener has been disabled");
                }
            }
        }
        return (C) semanticChronicle;
    }

    /**
     * Builds the.
     *
     * @param editCoordinate the edit coordinate
     * @param builtObjects the built objects
     * @return the optional wait task
     * @throws IllegalStateException the illegal state exception
     */
    @Override
    public OptionalWaitTask<C> build(Transaction transaction, EditCoordinate editCoordinate,
            List<Chronology> builtObjects)
            throws IllegalStateException {
        
        List<SemanticBuildListenerI> semanticBuildListeners = LookupService.get().getAllServices(SemanticBuildListenerI.class);
        for (SemanticBuildListenerI listener : semanticBuildListeners) {
            if (listener != null) {
                if (listener.isEnabled()) {
                    // LOG.info("Calling " + listener.getListenerName() + ".applyBefore(...)");
                    try {
                        listener.applyBefore(transaction, editCoordinate, builtObjects);
                    } catch (RuntimeException e) {
                        LOG.error("FAILED running " + listener.getListenerName() + ".applyBefore(...): ", e);
                    }
                } else {
                    LOG.info("NOT calling " + listener.getListenerName() + ".applyBefore(...) because listener has been disabled");
                }
            }
        }
        
        if (this.referencedComponentNid == Integer.MAX_VALUE) {
            this.referencedComponentNid = Get.identifierService()
                    .getNidForUuids(this.referencedComponentBuilder.getUuids());
        }
        
        SemanticVersion version;
        SemanticChronologyImpl semanticChronology;
        
        final int semanticNid = getNid();
        if (Get.assemblageService().hasSemantic(semanticNid)) {
            semanticChronology = (SemanticChronologyImpl) Get.assemblageService().getSemanticChronology(semanticNid);
            
            if ((semanticChronology.getVersionType() != this.semanticType)
                    || !semanticChronology.isIdentifiedBy(getPrimordialUuid())
                    || (semanticChronology.getAssemblageNid() != this.assemblageId)
                    || (semanticChronology.getReferencedComponentNid() != this.referencedComponentNid)) {
                throw new RuntimeException("Builder is being used to attempt a mis-matched edit of an existing semantic!");
            }
        } else {
            semanticChronology = new SemanticChronologyImpl(this.semanticType,
                    getPrimordialUuid(),
                    this.assemblageId,
                    this.referencedComponentNid);
        }
        
        semanticChronology.setAdditionalUuids(this.additionalUuids);
        
        switch (this.semanticType) {
            case COMPONENT_NID:
                final ComponentNidVersionImpl cnsi
                        = (ComponentNidVersionImpl) semanticChronology.createMutableVersion(transaction, this.state,
                                editCoordinate, getModule());
                version = cnsi;
                cnsi.setComponentNid((Integer) this.parameters[0]);
                break;
            
            case LONG:
                final LongVersionImpl lsi = (LongVersionImpl) semanticChronology.createMutableVersion(transaction, this.state,
                        editCoordinate, getModule());
                version = lsi;
                lsi.setLongValue((Long) this.parameters[0]);
                break;
            
            case LOGIC_GRAPH:
                final LogicGraphVersionImpl lgsi
                        = (LogicGraphVersionImpl) semanticChronology.createMutableVersion(transaction, this.state,
                                editCoordinate, getModule());
                version = lgsi;
                lgsi.setGraphData(((LogicalExpression) this.parameters[0]).getData(DataTarget.INTERNAL));
                break;
            
            case MEMBER:
                final SemanticVersionImpl svi = semanticChronology.createMutableVersion(transaction, this.state, editCoordinate, getModule());
                version = svi;
                break;
            
            case STRING:
                final StringVersionImpl ssi = (StringVersionImpl) semanticChronology.createMutableVersion(transaction, this.state,
                        editCoordinate, getModule());
                version = ssi;
                ssi.setString((String) this.parameters[0]);
                break;
            
            case DESCRIPTION: {
                final DescriptionVersionImpl dsi
                        = (DescriptionVersionImpl) semanticChronology.createMutableVersion(transaction, this.state,
                                editCoordinate, getModule());
                version = dsi;
                dsi.setCaseSignificanceConceptNid((Integer) this.parameters[0]);
                dsi.setDescriptionTypeConceptNid((Integer) this.parameters[1]);
                dsi.setLanguageConceptNid((Integer) this.parameters[2]);
                dsi.setText((String) this.parameters[3]);
                break;
            }
            
            case DYNAMIC: {
                final DynamicImpl dsi = (DynamicImpl) semanticChronology.createMutableVersion(transaction, this.state,
                        editCoordinate, getModule());
                
                if ((this.parameters != null) && (this.parameters.length > 0)) {
                    // See notes in SemanticBuilderProvider - this casting / wrapping nonesense it to work around Java being stupid.
                    dsi.setData(((AtomicReference<DynamicData[]>) this.parameters[0]).get());
                }
                version = dsi;
                break;
            }
            
            default:
                throw new UnsupportedOperationException("q Can't handle: " + this.semanticType);
        }
        transaction.addVersionToTransaction(version);
        Task<Void> primaryNested = Get.commitService()
                .addUncommitted(transaction, semanticChronology);

        final ArrayList<OptionalWaitTask<?>> nested = new ArrayList<>();
        
        getSemanticBuilders().forEach((builder) -> {
            getModule().ifPresent((moduleSpec) -> {
                builder.setModule(moduleSpec);
            });
            nested.add(builder.build(transaction, editCoordinate,
                    builtObjects));
        });
        builtObjects.add(semanticChronology);
        for (SemanticBuildListenerI listener : semanticBuildListeners) {
            if (listener != null) {
                if (listener.isEnabled()) {
                    // LOG.info("Calling " + listener.getListenerName() + ".applyAfter(...)");
                    listener.applyAfter(transaction, editCoordinate, version, builtObjects);
                } else {
                    LOG.info("NOT calling " + listener.getListenerName() + ".applyAfter(...) because listener has been disabled");
                }
            }
        }
        return new OptionalWaitTask<>(primaryNested, (C) semanticChronology, nested);
    }
    
    @Override
    public IdentifiedComponentBuilder<C> setT5Uuid(UUID namespace, BiConsumer<String, UUID> consumer) {
        if (isPrimordialUuidSet() && getPrimordialUuid().version() == 4) {
            throw new RuntimeException("Attempting to set Type 5 UUID where the UUID was previously set to random");
        }
        
        if (!isPrimordialUuidSet()) {
            UUID assemblageUuid = Get.identifierService().getUuidPrimordialForNid(this.assemblageId);
            
            UUID refCompUuid = null;
            if (referencedComponentBuilder != null) {
                refCompUuid = referencedComponentBuilder.getPrimordialUuid();
            } else {
                refCompUuid = Get.identifierService().getUuidPrimordialForNid(referencedComponentNid);
            }
            
            if (namespace == null) {
               namespace = UuidT5Generator.PATH_ID_FROM_FS_DESC;
            }
            
            if (semanticType == VersionType.LOGIC_GRAPH) {
                setPrimordialUuid(UuidFactory.getUuidForLogicGraphSemantic(namespace, assemblageUuid, refCompUuid, (LogicalExpression) parameters[0], consumer));
            } else if (semanticType == VersionType.MEMBER) {
                setPrimordialUuid(UuidFactory.getUuidForMemberSemantic(namespace, assemblageUuid, refCompUuid, consumer));
            } else if (semanticType == VersionType.DYNAMIC) {
                setPrimordialUuid(UuidFactory.getUuidForDynamic(namespace, assemblageUuid, refCompUuid,
                        (parameters != null && parameters.length > 0 ? ((AtomicReference<DynamicData[]>) parameters[0]).get() : null), consumer));
            } else if (semanticType == VersionType.COMPONENT_NID) {
                UUID componentUuid = Get.identifierService().getUuidPrimordialForNid((Integer) parameters[0]);
                setPrimordialUuid(UuidFactory.getUuidForComponentNidSemantic(namespace, assemblageUuid, refCompUuid, componentUuid, consumer));
            } else if (semanticType == VersionType.DESCRIPTION) {
                setPrimordialUuid(UuidFactory.getUuidForDescriptionSemantic(namespace, refCompUuid,
                        Get.identifierService().getUuidPrimordialForNid((Integer) parameters[0]),
                        Get.identifierService().getUuidPrimordialForNid((Integer) parameters[1]),
                        Get.identifierService().getUuidPrimordialForNid((Integer) parameters[2]),
                        (String) parameters[3],
                        consumer));
            } else if (semanticType == VersionType.STRING) {
                setPrimordialUuid(UuidFactory.getUuidForStringSemantic(namespace, assemblageUuid, refCompUuid, (String) parameters[0], consumer));
            }
        }
        return this;
    }
    
    @Override
    public IdentifiedComponentBuilder<C> setT5UuidNested(UUID namespace) {
        setT5Uuid(namespace, null);
        for (SemanticBuilder<?> sb : getSemanticBuilders()) {
            sb.setT5UuidNested(namespace);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VersionType getVersionType() {
        return semanticType;
    }
}
