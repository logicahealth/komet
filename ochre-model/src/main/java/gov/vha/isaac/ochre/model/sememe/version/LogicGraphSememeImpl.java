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
package gov.vha.isaac.ochre.model.sememe.version;

import gov.vha.isaac.ochre.api.DataSource;
import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionByteArrayConverter;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableLogicGraphSememe;
import gov.vha.isaac.ochre.model.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import org.glassfish.hk2.api.MultiException;

/**
 *
 * @author kec
 */
public class LogicGraphSememeImpl extends SememeVersionImpl<LogicGraphSememeImpl>
        implements MutableLogicGraphSememe<LogicGraphSememeImpl> {

    private static LogicalExpressionByteArrayConverter converter;

    private static LogicalExpressionByteArrayConverter getExternalDataConverter() throws MultiException {
        if (converter == null) {
            converter = LookupService.get().getService(LogicalExpressionByteArrayConverter.class);
        }
        return converter;
    }

    byte[][] graphData = null;

    public LogicGraphSememeImpl(SememeChronologyImpl<LogicGraphSememeImpl> container, 
            int stampSequence, short versionSequence,
            ByteArrayDataBuffer data) {
        super(container, stampSequence, versionSequence);
        int graphNodes = data.getInt();
        this.graphData = new byte[graphNodes][];
        for (int i = 0; i < graphNodes; i++) {
            try {
                this.graphData[i] = data.getByteArrayField();
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(e);
            }
        }
    }

    public LogicGraphSememeImpl(SememeChronologyImpl<LogicGraphSememeImpl> container, 
            int stampSequence, short versionSequence) {
        super(container, stampSequence, versionSequence);
    }

    @Override
    protected void writeVersionData(ByteArrayDataBuffer data) {
        super.writeVersionData(data);
        data.putInt(graphData.length);
        for (byte[] graphDataElement : graphData) {
            data.putByteArrayField(graphDataElement);
        }
    }

    @Override
    public SememeType getSememeType() {
        return SememeType.LOGIC_GRAPH;
    }

    @Override
    public byte[][] getGraphData() {
        return graphData;
    }

    @Override
    public LogicalExpression getLogicalExpression() {
        return new LogicalExpressionOchreImpl(graphData, DataSource.INTERNAL, getReferencedComponentNid());
    }

    @Override
    public byte[][] getExternalGraphData() {
        return getExternalDataConverter().convertLogicGraphForm(graphData, DataTarget.EXTERNAL);
    }

    @Override
    public void setGraphData(byte[][] graphData) {
        if (this.graphData != null) {
            checkUncommitted();
        }
        this.graphData = graphData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSememeType().toString());
        LogicalExpressionOchreImpl lg = new LogicalExpressionOchreImpl(graphData, DataSource.INTERNAL, Get.identifierService().getConceptSequence(getReferencedComponentNid()));
        sb.append("\n ");
        sb.append(lg.toString());
        toString(sb);
        return sb.toString();
    }

}
