package sh.isaac.solor.direct;

import sh.isaac.MetaData;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.Semaphore;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

public class GenomicRelationshipWriter extends TimedTaskWithProgressTracker<Void> {


    private final Semaphore writeSemaphore;
    private Map<String, Set<String>> genomicRelationships;

    private static final String ENCODING_FOR_UUID_GENERATION = "8859_1";
    private final long commitTime = System.currentTimeMillis();
    private final Status state = Status.ACTIVE;
    private final int authorNid = TermAux.USER.getNid();
    private final int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
    private final int moduleNid = MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid();
    private final LogicalExpressionBuilderService logicalExpressionBuilderService = Get.logicalExpressionBuilderService();
    private final SemanticBuilderService<?> semanticBuilderService = Get.semanticBuilderService();
    private final TaxonomyService taxonomyService = Get.taxonomyService();
    private final GenomicComponentType genomicComponentType;


    public GenomicRelationshipWriter(Map<String, Set<String>> genomicRelationships, Semaphore writeSemaphore,
                                     String message, GenomicComponentType genomicComponentType) {
        this.genomicRelationships = genomicRelationships;
        this.genomicComponentType = genomicComponentType;
        this.writeSemaphore = writeSemaphore;
        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing " + genomicComponentType.toString() + " description batch of size: " + genomicRelationships.size());
        updateMessage(message);
        addToTotalWork(genomicRelationships.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try {
            StampService stampService = Get.stampService();
            AssemblageService assemblageService = Get.assemblageService();
            IdentifierService identifierService = Get.identifierService();
            HashSet<Integer> defferedTaxonomyNids = new HashSet<>();

            genomicRelationships.entrySet().stream()
                    .forEach(entry -> {

                        String sourceConceptString = entry.getKey();
                        UUID sourceConceptUUID =  null;

                        try {
                            if(genomicComponentType == GenomicComponentType.VARIANT_GENE_REL){
                                sourceConceptUUID = UUID.nameUUIDFromBytes(
                                        (GenomicComponentType.VARIANT_CONCEPT.getNamespaceString() + sourceConceptString)
                                                .getBytes(ENCODING_FOR_UUID_GENERATION));
                            }else if(genomicComponentType == GenomicComponentType.GENE_SNOMED_REL){
                                sourceConceptUUID = UUID.nameUUIDFromBytes(
                                        (GenomicComponentType.GENE_CONCEPT.getNamespaceString() + sourceConceptString)
                                                .getBytes(ENCODING_FOR_UUID_GENERATION));
                            }

                        } catch (final UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }

                        if(identifierService.hasUuid(sourceConceptUUID)) {
                            List<UUID> validDestinationConceptUUIDs = new ArrayList<>();

                            try {
                                for(String destinationConceptID : entry.getValue()){
                                    UUID destinationConceptUUID = null;

                                    try{

                                        if(destinationConceptID.contains("UUID:")){
                                            destinationConceptUUID = UUID.fromString(
                                                    destinationConceptID.replace("UUID:",""));
                                        } else {

                                            switch (genomicComponentType) {
                                                case VARIANT_GENE_REL:
                                                    destinationConceptUUID = UUID.nameUUIDFromBytes(
                                                            (GenomicComponentType.GENE_CONCEPT
                                                                    .getNamespaceString() + destinationConceptID)
                                                                    .getBytes(ENCODING_FOR_UUID_GENERATION));
                                                    break;
                                                case GENE_SNOMED_REL:
                                                    destinationConceptUUID = UuidT3Generator.fromSNOMED(destinationConceptID);
                                                    break;
                                            }
                                        }

                                        if(identifierService.hasUuid(destinationConceptUUID))
                                            validDestinationConceptUUIDs.add(destinationConceptUUID);

                                    }catch (UnsupportedEncodingException ueE){
                                        throw new RuntimeException(ueE);
                                    }
                                }

                                if(validDestinationConceptUUIDs.size() > 0) {

                                    int sourceNid = identifierService.getNidForUuids(sourceConceptUUID);
                                    final LogicalExpressionBuilder leb = logicalExpressionBuilderService.getLogicalExpressionBuilder();
                                    final Assertion[] assertions = new Assertion[validDestinationConceptUUIDs.size()];

                                    for (int i = 0; i < validDestinationConceptUUIDs.size(); i++) {
                                        assertions[i] = ConceptAssertion(identifierService.getNidForUuids(validDestinationConceptUUIDs.get(i)), leb);
                                    }

                                    NecessarySet(And(assertions));

                                    LogicalExpression le = leb.build();
                                    SemanticBuilder<?> sb = semanticBuilderService.getLogicalExpressionBuilder(le, sourceNid,
                                            MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid());
                                    sb.setStatus(this.state);
                                    int graphStamp = stampService.getStampSequence(this.state, this.commitTime, authorNid, moduleNid, pathNid);
                                    SemanticChronology sc = sb.build(graphStamp, new ArrayList<>());
                                    defferedTaxonomyNids.add(sc.getNid());
                                    assemblageService.writeSemanticChronology(sc);
                                }
                            } catch (NoSuchElementException nseE) {
                                nseE.printStackTrace();
                            }
                        }

                        completedUnitOfWork();
                    });

            for(int nid : defferedTaxonomyNids){
                taxonomyService.updateTaxonomy(Get.assemblageService().getSemanticChronology(nid));
            }

            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }
}
