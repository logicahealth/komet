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
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptSnapshotService;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.CoordinateFactory;
import gov.vha.isaac.ochre.api.logic.LogicService;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.api.progress.ActiveTasks;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataServiceFactory;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataWriterService;
import gov.vha.isaac.ochre.api.util.WorkExecutors;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

/**
 * Provides simple static access to common services, in a lookup service aware
 * way. Intended to be used in place of static fields placed in classes that
 * frequently use a common service. This class was added specifically to address
 * problems when a service is used in a mojo that spans more than one project,
 * by ensuring that static initialization of services does not provide a way to
 * retain stale services.
 *
 * @author kec
 */
@Service
@Singleton
public class Get implements OchreCache {

    private static final Logger LOG = LogManager.getLogger();

    private static ActiveTasks activeTaskSet;
    private static ConfigurationService configurationService;
    private static CommitService commitService;
    private static ConceptActiveService conceptActiveService;
    private static ConceptService conceptService;
    private static ConceptSnapshotService conceptSnapshot;
    private static IdentifiedObjectService identifiedObjectService;
    private static IdentifierService identifierService;
    private static LanguageCoordinateService languageCoordinateService;
    private static LogicalExpressionBuilderService logicalExpressionBuilderService;
    private static LogicService logicService;
    private static PathService pathService;
    private static SememeBuilderService<?> sememeBuilderService;
    private static SememeService sememeService;
    private static CoordinateFactory coordinateFactory;
    private static TaxonomyService taxonomyService;
    private static WorkExecutors workExecutors;
    private static ConceptBuilderService conceptBuilderService;

    public Get() {
    }

    private static <T> T getService(Class<T> clazz) {
        T service = LookupService.getService(clazz);
        if (service == null) {
            throw new RuntimeException("No service for contract '" + clazz.getName()
                    + "'... Is the service provider on the classpath?");
        }
        return service;
    }

    public static CoordinateFactory coordinateFactory() {
        if (coordinateFactory == null) {
            coordinateFactory = getService(CoordinateFactory.class);
        }
        return coordinateFactory;
    }

    public static ActiveTasks activeTasks() {
        if (activeTaskSet == null) {
            activeTaskSet = getService(ActiveTasks.class);
        }
        return activeTaskSet;
    }

    public static ConfigurationService configurationService() {
        if (configurationService == null) {
            configurationService = getService(ConfigurationService.class);
        }
        return configurationService;
    }

    public static ConceptService conceptService() {
        if (conceptService == null) {
            conceptService = getService(ConceptService.class);
        }
        return conceptService;
    }

    public static ConceptActiveService conceptActiveService() {
        if (conceptActiveService == null) {
            conceptActiveService = getService(ConceptActiveService.class);
        }
        return conceptActiveService;
    }

    /**
     *
     * @return a {@code ConceptSnapshotService} configured using the default
     * {@code StampCoordinate} and {@code LanguageCoordinate} provided by the
     * configuration service.
     */
    public static ConceptSnapshotService conceptSnapshot() {
        if (conceptSnapshot == null) {
            conceptSnapshot = getService(ConceptService.class)
                    .getSnapshot(Get.configurationService().getDefaultStampCoordinate(),
                            Get.configurationService().getDefaultLanguageCoordinate());
        }
        return conceptSnapshot;
    }

    /**
     * Simple method for getting text of the description of a concept. This
     * method will try first to return the fully specified description, or the
     * preferred description, as specified in the default
     * {@code StampCoordinate} and the default {@code LanguageCoordinate}.
     *
     * @param conceptId nid or sequence of the concept to get the description
     * for
     * @return a description for this concept. If no description can be found,
     * {@code "No desc for: " + conceptId;} will be returned.
     */
    public static String conceptDescriptionText(int conceptId) {
        Optional<LatestVersion<DescriptionSememe<?>>> descriptionOptional
                = conceptSnapshot().getDescriptionOptional(conceptId);
        if (descriptionOptional.isPresent()) {
            return descriptionOptional.get().value().getText();
        }
        return "No desc for: " + conceptId;
    }

    public static String conceptDescriptionTextList(int[] conceptIds) {
        if (conceptIds != null && conceptIds.length > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            Arrays.stream(conceptIds).forEach((conceptId) -> {
                builder.append(conceptDescriptionText(conceptId));
                builder.append(", ");
            });
            builder.delete(builder.length() - 2, builder.length() - 1);
            builder.append("]");
            return builder.toString();
        }
        return "[]";
    }

    public static String conceptDescriptionTextList(ConceptSequenceSet conceptIds) {
        return conceptDescriptionTextList(conceptIds.asArray());
    }

    public static ConceptBuilderService conceptBuilderService() {
        if (conceptBuilderService == null) {
            conceptBuilderService = getService(ConceptBuilderService.class);
        }
        return conceptBuilderService;
    }
    
    
    public static String conceptDescriptionTextList(List<Integer> conceptIds) {
        return conceptDescriptionTextList(
                conceptIds
                .stream().mapToInt((boxedInt) -> (int) boxedInt).toArray()
        );
    }

    public static IdentifierService identifierService() {
        if (identifierService == null) {
            identifierService = LookupService.getService(IdentifierService.class);
        }
        return identifierService;
    }

    public static LanguageCoordinateService languageCoordinateService() {
        if (languageCoordinateService == null) {
            languageCoordinateService = getService(LanguageCoordinateService.class);
        }
        return languageCoordinateService;
    }

    public static LogicalExpressionBuilderService logicalExpressionBuilderService() {
        if (logicalExpressionBuilderService == null) {
            logicalExpressionBuilderService = getService(LogicalExpressionBuilderService.class);
        }
        return logicalExpressionBuilderService;
    }

    public static LogicService logicService() {
        if (logicService == null) {
            logicService = getService(LogicService.class);
        }
        return logicService;
    }

    public static PathService pathService() {
        if (pathService == null) {
            pathService = getService(PathService.class);
        }
        return pathService;
    }

    /**
     *
     * @param conceptId either a concept nid or sequence.
     * @return the stated definition chronology for the specified concept
     * according to the default logic coordinate.
     */
    public static Optional<SememeChronology<? extends SememeVersion<?>>> statedDefinitionChronology(int conceptId) {
        conceptId = identifierService().getConceptNid(conceptId);
        return sememeService().getSememesForComponentFromAssemblage(conceptId, configurationService().getDefaultLogicCoordinate().getStatedAssemblageSequence()).findAny();
    }

    /**
     *
     * @param conceptId either a concept nid or sequence.
     * @return the inferred definition chronology for the specified concept
     * according to the default logic coordinate.
     */
    public static Optional<SememeChronology<? extends SememeVersion<?>>> inferredDefinitionChronology(int conceptId) {
        conceptId = identifierService().getConceptNid(conceptId);
        return sememeService().getSememesForComponentFromAssemblage(conceptId, configurationService().getDefaultLogicCoordinate().getInferredAssemblageSequence()).findAny();
    }

    public static TaxonomyService taxonomyService() {
        if (taxonomyService == null) {
            taxonomyService = getService(TaxonomyService.class);
        }
        return taxonomyService;
    }

    public static CommitService commitService() {
        if (commitService == null) {
            commitService = getService(CommitService.class);
        }
        return commitService;
    }

    public static SememeService sememeService() {
        if (sememeService == null) {
            sememeService = getService(SememeService.class);
        }
        return sememeService;
    }

    public static SememeBuilderService<? extends SememeChronology<? extends SememeVersion<?>>> sememeBuilderService() {
        if (sememeBuilderService == null) {
            sememeBuilderService = getService(SememeBuilderService.class);
        }
        return sememeBuilderService;
    }

    public static IdentifiedObjectService identifiedObjectService() {
        if (identifiedObjectService == null) {
            identifiedObjectService = getService(IdentifiedObjectService.class);
        }
        return identifiedObjectService;
    }

    public static WorkExecutors workExecutors() {
        if (workExecutors == null) {
            workExecutors = getService(WorkExecutors.class);
        }
        return workExecutors;
    }
    
    /**
     * Note, this method may fail during bootstrap, if concept being requested is not already loaded
     * into the concept service. 
     * @param id either a nid or a concept sequence. 
     * @return A concept specification for the corresponding identifier
     */
    public static ConceptSpecification conceptSpecification(int id) {
        id = identifierService().getConceptNid(id);
        return new ConceptProxy(conceptDescriptionText(id), identifierService().getUuidArrayForNid(id));
    }
    
    public static BinaryDataReaderService binaryDataReader(Path dataPath) throws FileNotFoundException {
        return getService(BinaryDataServiceFactory.class).getReader(dataPath);
    }
    
    public static BinaryDataWriterService binaryDataWriter(Path dataPath) throws FileNotFoundException {
        return getService(BinaryDataServiceFactory.class).getWriter(dataPath);
    }

    @Override
    public void reset() {
        LOG.info("Resetting service cache.");
        activeTaskSet = null;
        configurationService = null;
        commitService = null;
        conceptBuilderService = null;
        conceptActiveService = null;
        conceptService = null;
        conceptSnapshot = null;
        coordinateFactory = null;
        identifiedObjectService = null;
        identifierService = null;
        languageCoordinateService = null;
        logicalExpressionBuilderService = null;
        logicService = null;
        pathService = null;
        sememeBuilderService = null;
        sememeService = null;
        taxonomyService = null;
        workExecutors = null;
    }

}
