/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.provider.concept.comparison;

import sh.isaac.api.Get;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.logic.definition.LogicalExpressionBuilderImpl;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;

import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;

import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;

import static sh.isaac.api.logic.LogicalExpressionBuilder.SufficientSet;

/**
 *
 * @author kec
 */
public class CorrelationProblem6 {

    static LogicalExpression getReferenceExpression() {
        LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();

        NecessarySet(
                And(
                        ConceptAssertion(Get.concept("9eaf8526-07b2-3853-920d-a573312a6c5c"), leb)
                )
        );

        SufficientSet(
                And(
                        SomeRole(Get.concept("65bf3b7f-c854-36b5-81c3-4915461020a8"),
                                ConceptAssertion(Get.concept("a2be81c8-9131-327f-8dbd-ca55c450c65e"), leb)
                        ),
                        ConceptAssertion(Get.concept("5032532f-6b58-31f9-84c1-4a365dde4449"), leb)
                )
        );
        return leb.build();
    }

    static LogicalExpression getComparisonExpression() {
        LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();

        SufficientSet(
                And(
                        SomeRole(Get.concept("65bf3b7f-c854-36b5-81c3-4915461020a8"),
                                ConceptAssertion(Get.concept("a2be81c8-9131-327f-8dbd-ca55c450c65e"), leb)
                        ),
                         ConceptAssertion(Get.concept("9eaf8526-07b2-3853-920d-a573312a6c5c"), leb)
                )
        );
        return leb.build();
    }
}
