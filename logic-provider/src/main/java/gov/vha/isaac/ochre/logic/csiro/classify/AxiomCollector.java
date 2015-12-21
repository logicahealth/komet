package gov.vha.isaac.ochre.logic.csiro.classify;

import au.csiro.ontology.Factory;
import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.Role;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Created by kec on 12/16/14.
 */

// TODO move to CSIRO specific module

public class AxiomCollector implements Collector<LogicalExpressionOchreImpl, Set<Axiom>, Set<Axiom>> {

    BitSet conceptSequences;
    Concept[] concepts;
    OpenIntObjectHashMap<Role> roles;
    OpenIntHashSet neverGroupRoleSequences;
    int roleGroupConceptSequence;

    public AxiomCollector(BitSet conceptSequences, OpenIntHashSet roleSequences,
                          OpenIntHashSet neverGroupRoleSequences, int roleGroupConceptSequence) {
        this.conceptSequences = conceptSequences;
        this.concepts = new Concept[conceptSequences.length()];
        Arrays.parallelSetAll(this.concepts, conceptSequence -> Factory.createNamedConcept(Integer.toString(conceptSequence)));
        roles = new OpenIntObjectHashMap<>(roleSequences.size());
        roleSequences.forEachKey(roleSequence -> {
            roles.put(roleSequence, Factory.createNamedRole(Integer.toString(roleSequence)));
            return true;
        });
        this.neverGroupRoleSequences = neverGroupRoleSequences;
        this.roleGroupConceptSequence = roleGroupConceptSequence;
    }


    @Override
    public Supplier<Set<Axiom>> supplier() {
        return HashSet::new;
    }

    @Override
    public BiConsumer<Set<Axiom>, LogicalExpressionOchreImpl> accumulator() {
        return new AxiomAccumulator(concepts, conceptSequences, roles, neverGroupRoleSequences, roleGroupConceptSequence);
    }

    @Override
    public BinaryOperator<Set<Axiom>> combiner() {
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }

    @Override
    public Function<Set<Axiom>, Set<Axiom>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
                Characteristics.IDENTITY_FINISH));
    }
}
