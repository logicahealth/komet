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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.concurrent.Task;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.Util;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Long2_Version;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;
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
        return (SemanticBuilderImpl<C>) super.setPrimordialUuid(uuid); 
    }
    
    @Override
    public SemanticBuilderImpl<C> setPrimordialUuid(String uuidString) {
        return (SemanticBuilderImpl<C>) super.setPrimordialUuid(uuidString);
    }
    
    /*
     * Does not build subs, when buildSubs is false, or fire the after listener
     */
    private C buildInternal(WriteCoordinate writeCoordinate, List<Chronology> builtObjects, boolean buildSubs) throws IllegalStateException {
        if (this.referencedComponentNid == Integer.MAX_VALUE) {
            this.referencedComponentNid = Get.identifierService()
                    .getNidForUuids(this.referencedComponentBuilder.getUuids());
        }

        WriteCoordinate forMain = status == null ? writeCoordinate : new WriteCoordinateImpl(writeCoordinate, status);

        SemanticVersion version = null;
        SemanticChronologyImpl semanticChronicle;
        
        final int semanticNid = getNid();
        if (Get.assemblageService().hasSemantic(semanticNid)) {
            semanticChronicle = (SemanticChronologyImpl) Get.assemblageService()
                    .getSemanticChronology(semanticNid);

            if ((semanticChronicle.getVersionType() != this.semanticType)) {
                throw new RuntimeException("1. Builder is being used to attempt a mis-matched edit of an existing semantic! \n" +
                        "Version types do not match: " + semanticChronicle.getVersionType() + " " + this.semanticType + "\n" +
                        semanticChronicle + "\n" + this.toString());
            }
            if (!semanticChronicle.isIdentifiedBy(getPrimordialUuid())) {
                throw new RuntimeException("2. Builder is being used to attempt a mis-matched edit of an existing semantic! \n" +
                        "UUID mismatch: " + semanticChronicle.getPrimordialUuid() + " vs " + this.getPrimordialUuid() + "\n" +
                        semanticChronicle + "\n" + this.toString());
            }
            if (semanticChronicle.getAssemblageNid() != this.assemblageId) {
                throw new RuntimeException("3. Builder is being used to attempt a mis-matched edit of an existing semantic! \n" +
                        " Assemblage nids do not match: " +
                        Get.conceptDescriptionText(semanticChronicle.getAssemblageNid()) + ": " +
                        Get.identifierService().getUuidPrimordialStringForNid(semanticChronicle.getAssemblageNid()) +
                        " vs " + Get.conceptDescriptionText(this.assemblageId) + ": " +
                        Get.identifierService().getUuidPrimordialStringForNid(this.assemblageId) + "\n" +
                        semanticChronicle + "\n" + this.toString());
            }
            if (semanticChronicle.getReferencedComponentNid() != this.referencedComponentNid) {
                LOG.error("UUID info before throw: " + this.getUuidList());
                throw new RuntimeException("4. Builder is being used to attempt a mis-matched edit of an existing semantic! \n" +
                        " Referenced component nids do not match: " +
                        Get.identifierService().getUuidPrimordialStringForNid(semanticChronicle.getReferencedComponentNid()) +
                        " vs " +
                        Get.identifierService().getUuidPrimordialStringForNid(this.referencedComponentNid) + "\n" +
                        Get.conceptDescriptionText(semanticChronicle.getReferencedComponentNid()) + "\n" +
                        Get.conceptDescriptionText(this.referencedComponentNid) + "\n" +
                        semanticChronicle + "\n" + this.toString());
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
                        = (ComponentNidVersionImpl) semanticChronicle.createMutableVersion(forMain);
                
                cnsi.setComponentNid((Integer) this.parameters[0]);
                version = cnsi;
                break;

            case Nid1_Int2:
                final Nid1_Int2_Version nid1int2
                        = (Nid1_Int2_Version) semanticChronicle.createMutableVersion(forMain);

                nid1int2.setNid1((Integer) this.parameters[0]);
                nid1int2.setInt2((Integer) this.parameters[1]);
                version = nid1int2;

                break;

            case Nid1_Long2:
                final Nid1_Long2_Version nid1long2
                        = (Nid1_Long2_Version) semanticChronicle.createMutableVersion(forMain);

                nid1long2.setNid1((Integer) this.parameters[0]);
                nid1long2.setLong2((Long) this.parameters[1]);
                version = nid1long2;

                break;

            case LONG:
                final LongVersionImpl lsi = (LongVersionImpl) semanticChronicle.createMutableVersion(forMain);
                version = lsi;
                lsi.setLongValue((Long) this.parameters[0]);
                break;
            
            case LOGIC_GRAPH:
                final LogicGraphVersionImpl lgsi
                        = (LogicGraphVersionImpl) semanticChronicle.createMutableVersion(forMain);
                version = lgsi;
                lgsi.setGraphData(((LogicalExpression) this.parameters[0]).getData(DataTarget.INTERNAL));
                break;
            
            case MEMBER:
                SemanticVersionImpl svi = semanticChronicle.createMutableVersion(forMain);
                version = svi;
                break;
            
            case STRING:
                final StringVersionImpl ssi = (StringVersionImpl) semanticChronicle.createMutableVersion(forMain);
                version = ssi;
                ssi.setString((String) this.parameters[0]);
                break;
            
            case DESCRIPTION: {
                final DescriptionVersionImpl dsi
                        = (DescriptionVersionImpl) semanticChronicle.createMutableVersion(forMain);
                version = dsi;
                dsi.setCaseSignificanceConceptNid((Integer) this.parameters[0]);
                dsi.setDescriptionTypeConceptNid((Integer) this.parameters[1]);
                dsi.setLanguageConceptNid((Integer) this.parameters[2]);
                dsi.setText((String) this.parameters[3]);
                break;
            }
            
            case DYNAMIC: {
                final DynamicImpl dsi = (DynamicImpl) semanticChronicle.createMutableVersion(forMain);
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
        builtObjects.add(semanticChronicle);
        
        if (buildSubs) {
            getSemanticBuilders().forEach((builder) -> builder.build(writeCoordinate, builtObjects));
        }
        return (C) semanticChronicle;
    }

    @Override
    public C build(Transaction transaction, int stampSequence, List<Chronology> builtObjects) throws IllegalStateException
    {
        return buildInternal(adjustForModule(new WriteCoordinateImpl(transaction, stampSequence)), builtObjects, true);
    }

    @Override
    public OptionalWaitTask<C> buildAndWrite(WriteCoordinate writeCoordinate, List<Chronology> builtObjects) throws IllegalStateException
    {
        WriteCoordinate forWrite = adjustForModule(writeCoordinate);
        SemanticChronology sc = buildInternal(forWrite, builtObjects, false);
        Task<Void> primaryNested = Get.commitService().addUncommitted(forWrite.getTransaction().get(), sc);
        final ArrayList<OptionalWaitTask<?>> nested = new ArrayList<>();
        getSemanticBuilders().forEach((builder) -> nested.add(builder.buildAndWrite(forWrite, builtObjects)));
        
        return new OptionalWaitTask<>(primaryNested, (C) sc, nested);
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
            
            switch (semanticType) {
                case COMPONENT_NID:
                {
                    UUID componentUuid = Get.identifierService().getUuidPrimordialForNid((Integer) parameters[0]);
                    setPrimordialUuid(UuidFactory.getUuidForComponentNidSemantic(namespace, assemblageUuid, refCompUuid, componentUuid, consumer));
                    break;
                }
                case DESCRIPTION:
                {
                    setPrimordialUuid(UuidFactory.getUuidForDescriptionSemantic(namespace, refCompUuid,
                            Get.identifierService().getUuidPrimordialForNid((Integer) parameters[0]),
                            Get.identifierService().getUuidPrimordialForNid((Integer) parameters[1]),
                            Get.identifierService().getUuidPrimordialForNid((Integer) parameters[2]),
                            (String) parameters[3],
                            consumer));
                    break;
                }
                case DYNAMIC:
                {
                    setPrimordialUuid(UuidFactory.getUuidForDynamic(namespace, assemblageUuid, refCompUuid,
                            (parameters != null && parameters.length > 0 ? ((AtomicReference<DynamicData[]>) parameters[0]).get() : null), consumer));
                    break;
                }
                case LOGIC_GRAPH:
                {
                    setPrimordialUuid(UuidFactory.getUuidForLogicGraphSemantic(namespace, assemblageUuid, refCompUuid, (LogicalExpression) parameters[0], consumer));
                    break;
                }
                case LONG:
                {
                    setPrimordialUuid(UuidFactory.getUuidForLongSemantic(namespace, assemblageUuid, refCompUuid, (Long) parameters[0], consumer));
                    break;
                }
                case MEMBER:
                {
                    setPrimordialUuid(UuidFactory.getUuidForMemberSemantic(namespace, assemblageUuid, refCompUuid, consumer));
                    break;
                }
                case Nid1_Int2:
                {
                    UUID componentUuid = Get.identifierService().getUuidPrimordialForNid((Integer) parameters[0]);
                    setPrimordialUuid(UuidFactory.getUuidForNidIntSemantic(namespace, assemblageUuid, refCompUuid, componentUuid, (Integer) parameters[1], consumer));
                    break;
                }
                case Nid1_Long2:
                {
                    UUID componentUuid = Get.identifierService().getUuidPrimordialForNid((Integer) parameters[0]);
                    setPrimordialUuid(UuidFactory.getUuidForNidLongSemantic(namespace, assemblageUuid, refCompUuid, componentUuid, (Long) parameters[1], consumer));
                    break;
                }
                case STRING:
                {
                    setPrimordialUuid(UuidFactory.getUuidForStringSemantic(namespace, assemblageUuid, refCompUuid, (String) parameters[0], consumer));
                    break;
                }
                case Str1_Nid2_Nid3_Nid4:
                case Str1_Str2:
                case Str1_Str2_Nid3_Nid4:
                case Str1_Str2_Nid3_Nid4_Nid5:
                case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                case MEASURE_CONSTRAINTS:
                case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                case Nid1_Nid2:
                case Nid1_Nid2_Int3:
                case Nid1_Nid2_Str3:
                case Nid1_Str2:
                case RF2_RELATIONSHIP:
                case CONCEPT:
                case UNKNOWN:
                default :
                    throw new UnsupportedOperationException("Cannot generate proper UUID for type " + semanticType);
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
