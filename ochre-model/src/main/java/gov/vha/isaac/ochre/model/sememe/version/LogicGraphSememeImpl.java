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

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.sememe.version.MutableLogicGraphSememe;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronicleImpl;
import gov.vha.isaac.ochre.model.sememe.SememeType;

/**
 *
 * @author kec
 */
public class LogicGraphSememeImpl extends SememeVersionImpl
    implements MutableLogicGraphSememe {

    byte[][] graphData;

    public LogicGraphSememeImpl(SememeChronicleImpl<LogicGraphSememeImpl> container, int stampSequence, 
            DataBuffer data) {
        super(container, stampSequence, data);
        int graphNodes = data.getInt();
        this.graphData = new byte[graphNodes][];
        for (int i = 0; i < graphNodes; i++) {
            this.graphData[i] = data.getBytes();
        }
    }

    public LogicGraphSememeImpl(SememeChronicleImpl<LogicGraphSememeImpl> container, State status, long time, int authorSequence, int moduleSequence, int pathSequence) {
        super(container, 
                status, time, authorSequence, moduleSequence, pathSequence);
    }

    @Override
    protected void writeVersionData(DataBuffer data) {
        super.writeVersionData(data);
        data.putInt(graphData.length);
        for (byte[] graphData1 : graphData) {
            data.put(graphData1);
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
    public void setGraphData(byte[][] graphData) {
        checkUncommitted();
        this.graphData = graphData;
    }

}
