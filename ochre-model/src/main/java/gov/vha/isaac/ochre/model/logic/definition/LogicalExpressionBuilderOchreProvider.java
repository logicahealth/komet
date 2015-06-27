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
package gov.vha.isaac.ochre.model.logic.definition;

import gov.vha.isaac.ochre.api.DataSource;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service
public class LogicalExpressionBuilderOchreProvider implements LogicalExpressionBuilderService {

    public LogicalExpressionBuilderOchreProvider() {
    }

    @Override
    public LogicalExpressionBuilder getLogicalExpressionBuilder() {
        return new LogicalExpressionBuilderOchreImpl();
    }

    @Override
    public LogicalExpression fromSememe(LogicGraphSememe sememe) {
        return new LogicalExpressionOchreImpl(sememe.getGraphData(), DataSource.INTERNAL, sememe.getReferencedComponentNid());
    }
}
