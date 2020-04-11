/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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
package sh.isaac.provider.drools;

import org.drools.core.base.BaseEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.EvaluatorDefinition;
import org.drools.core.base.evaluators.Operator;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.coordinate.ManifoldCoordinate;

/**
 *
 * {@link IsKindOfEvaluatorDefinition}
 *
 * Only includes stated view.
 *
 * @author kec
 * @author afurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsKindOfEvaluatorDefinition extends IsaacBaseEvaluatorDefinition implements EvaluatorDefinition {

    public static final Operator IS_KIND_OF = Operator.addOperatorToRegistry("isKindOf", false);
    public static final Operator NOT_IS_KIND_OF = Operator.addOperatorToRegistry(IS_KIND_OF.getOperatorString(), true);

    public static class IsKindOfEvaluator extends IsaacBaseEvaluator {

        public IsKindOfEvaluator() {
            super();
            // No arg constructor for serialization.  
        }

        public IsKindOfEvaluator(final ValueType type, final boolean isNegated) {
            super(type, isNegated ? IsKindOfEvaluatorDefinition.NOT_IS_KIND_OF : IsKindOfEvaluatorDefinition.IS_KIND_OF);
        }

        @Override
        protected boolean test(final Object value1, final Object value2) {

           ConceptSnapshot possibleKind = null;
           ConceptSnapshot parentKind = null;
           if (ConceptSnapshot.class.isAssignableFrom(value1.getClass())) {
              possibleKind = (ConceptSnapshot) value1;
           } else {
              throw new UnsupportedOperationException("Can't convert: " + value1.getClass() + ": " + value1);
           }
           if (ConceptSnapshot.class.isAssignableFrom(value2.getClass())) {
              parentKind = (ConceptSnapshot) value2;
           } else {
              throw new UnsupportedOperationException("Can't convert: " + value2);
           }
           if (!possibleKind.getManifoldCoordinateUuid().equals(parentKind.getManifoldCoordinateUuid())) {
              throw new UnsupportedOperationException("Snapshots have different manifold coordinates: \n"
                      + " possibleKind: " + possibleKind
                      + " parentKind: " + parentKind
              );
           }
           ManifoldCoordinate manifoldCoordinate = parentKind;
           return this.getOperator().isNegated()
                   ^ (Get.taxonomyService().getSnapshot(manifoldCoordinate).isKindOf(possibleKind.getNid(),
                           parentKind.getNid()));
        }

        @Override
        public String toString() {
            return "IsKindOf isKindOf";
        }
    }

    /**
     * @return @see
     * gov.va.isaac.drools.evaluators.IsaacBaseEvaluatorDefinition#getListId()
     */
    @Override
    protected String getId() {
        return IS_KIND_OF.getOperatorString();
    }

    /**
     * @param type
     * @param isNegated
     * @param parameterText
     * @return
     * @see
     * gov.va.isaac.drools.evaluators.IsaacBaseEvaluatorDefinition#buildEvaluator(org.drools.core.base.ValueType,
     * boolean, String)
     */
    @Override
    protected BaseEvaluator buildEvaluator(ValueType type, boolean isNegated, String parameterText) {
        return new IsKindOfEvaluator(type, isNegated);
    }
}
