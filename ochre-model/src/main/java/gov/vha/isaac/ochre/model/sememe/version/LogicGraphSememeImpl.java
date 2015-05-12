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

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.logic.LogicByteArrayConverter;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableLogicGraphSememe;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronicleImpl;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import org.glassfish.hk2.api.MultiException;

/**
 *
 * @author kec
 */
public class LogicGraphSememeImpl extends SememeVersionImpl
        implements MutableLogicGraphSememe {

    private static LogicByteArrayConverter converter;

    private static LogicByteArrayConverter getExternalDataConverter() throws MultiException {
        if (converter == null) {
            converter = LookupService.get().getService(LogicByteArrayConverter.class);
        }
        return converter;
    }

    byte[][] graphData = null;

    public LogicGraphSememeImpl(SememeChronicleImpl<LogicGraphSememeImpl> container, int stampSequence,
            DataBuffer data) {
        super(container, stampSequence, data);
        int graphNodes = data.getInt();
        this.graphData = new byte[graphNodes][];
        for (int i = 0; i < graphNodes; i++) {
            this.graphData[i] = data.getByteArrayField();
        }
    }

    public LogicGraphSememeImpl(SememeChronicleImpl<LogicGraphSememeImpl> container, int stampSequence) {
        super(container, stampSequence);
    }

    @Override
    protected void writeVersionData(DataBuffer data) {
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

}
