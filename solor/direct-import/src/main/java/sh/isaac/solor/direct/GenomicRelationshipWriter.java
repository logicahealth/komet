package sh.isaac.solor.direct;

import sh.isaac.MetaData;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.logic.assertions.SufficientSet;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT3Generator;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.Semaphore;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

public class GenomicRelationshipWriter extends TimedTaskWithProgressTracker<Void> {


    private final Semaphore writeSemaphore;
    private final Map<String, Set<String>> genomicRelationshipMap;

    private static final String ENCODING_FOR_UUID_GENERATION = "8859_1";
    private final long commitTime = System.currentTimeMillis();
    private final Status state = Status.ACTIVE;
    private final int authorNid = TermAux.USER.getNid();
    private final int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
    private final int moduleNid = MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid();
    private final LogicalExpressionBuilderService logicalExpressionBuilderService = Get.logicalExpressionBuilderService();
    private final SemanticBuilderService<?> semanticBuilderService = Get.semanticBuilderService();
    private final TaxonomyService taxonomyService = Get.taxonomyService();
    private final GenomicConceptType genomicConceptType;


    public GenomicRelationshipWriter(Map<String, Set<String>> genomicRelationshipMap, Semaphore writeSemaphore,
                                     String message, GenomicConceptType genomicConceptType) {
        this.genomicRelationshipMap = genomicRelationshipMap;
        this.genomicConceptType = genomicConceptType;
        this.writeSemaphore = writeSemaphore;
        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing " + genomicConceptType.toString() + " description batch of size: " + genomicRelationshipMap.size());
        updateMessage(message);
        addToTotalWork(genomicRelationshipMap.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try {
            StampService stampService = Get.stampService();
            AssemblageService assemblageService = Get.assemblageService();
            IdentifierService identifierService = Get.identifierService();
            HashSet<Integer> defferedTaxonomyNids = new HashSet<>();
            Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.INACTIVE);

            for(Map.Entry<String, Set<String>> entry : this.genomicRelationshipMap.entrySet()){

                String sourceConceptString = entry.getKey();


                    UUID sourceConceptUUID;

                    try {
                        final String sourceID = "gov.nih.nlm.ncbi." + sourceConceptString;
                        sourceConceptUUID = UUID.nameUUIDFromBytes(sourceID.getBytes(ENCODING_FOR_UUID_GENERATION));
                    } catch (final UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        int sourceNid = identifierService.getNidForUuids(sourceConceptUUID);

                        final LogicalExpressionBuilder leb = logicalExpressionBuilderService.getLogicalExpressionBuilder();


                        final Assertion[] assertions = new Assertion[entry.getValue().size()];
                        int i = 0;
                        for(String destinationConceptString : entry.getValue()){
                            UUID destinationConceptUUID;
                            try {

                                if(this.genomicConceptType == GenomicConceptType.GENE_SNOMED) {
                                    destinationConceptUUID = UuidT3Generator.fromSNOMED(destinationConceptString);
                                }else {
                                    final String destinationConceptID = "gov.nih.nlm.ncbi." + destinationConceptString;
                                    destinationConceptUUID = UUID.nameUUIDFromBytes(destinationConceptID.getBytes(ENCODING_FOR_UUID_GENERATION));
                                }

                            } catch (final UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }

                            assertions[i++] = ConceptAssertion(identifierService.getNidForUuids(destinationConceptUUID), leb);
                        }

                        NecessarySet(And(assertions));

                        LogicalExpression le = leb.build();

                        SemanticBuilder<?> sb = semanticBuilderService.getLogicalExpressionBuilder(le, sourceNid ,
                                MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid());
                        sb.setStatus(this.state);
                        int graphStamp = stampService.getStampSequence(this.state, this.commitTime, authorNid, moduleNid, pathNid);
                        SemanticChronology sc = sb.build(transaction, graphStamp, new ArrayList<>());
                        defferedTaxonomyNids.add(sc.getNid());
                        assemblageService.writeSemanticChronology(sc);

                    }catch (NoSuchElementException nseE){
                        nseE.printStackTrace();
                    }

                completedUnitOfWork();
            }

            transaction.commit("Genomic relationship import");

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
