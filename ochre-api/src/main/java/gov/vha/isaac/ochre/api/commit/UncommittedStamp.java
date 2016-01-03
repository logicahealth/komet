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
package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.util.Hashcode;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author kec
 */
public class UncommittedStamp {

    public int hashCode = Integer.MAX_VALUE;
    public State status;
    public int authorSequence;
    public int moduleSequence;
    public int pathSequence;

    //~--- constructors --------------------------------------------------------
    public UncommittedStamp(State status, int authorSequence, int moduleSequence, int pathSequence) {
        super();
        this.status = status;
        this.authorSequence = authorSequence;
        this.moduleSequence = moduleSequence;
        this.pathSequence = pathSequence;
      assert status != null: "s: " + status +  " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert pathSequence  > 0: "s: " + status +  " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert moduleSequence > 0: "s: " + status +  " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert authorSequence > 0: "s: " + status +  " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
   }

    public UncommittedStamp(DataInput input) throws IOException {
        super();
        if (input.readBoolean()) {
            this.status = State.ACTIVE;
        } else {
            this.status = State.INACTIVE;
        }
        this.authorSequence = input.readInt();
        this.moduleSequence = input.readInt();
        this.pathSequence = input.readInt();
    }

    public void write(DataOutput output) throws IOException {
        output.writeBoolean(status.isActive());
        output.writeInt(authorSequence);
        output.writeInt(moduleSequence);
        output.writeInt(pathSequence);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UncommittedStamp) {
            UncommittedStamp other = (UncommittedStamp) obj;

            if ((status == other.status) && (authorSequence == other.authorSequence)
                    && (pathSequence == other.pathSequence) && (moduleSequence == other.moduleSequence)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hashCode == Integer.MAX_VALUE) {
            hashCode = Hashcode.compute(new int[]{status.ordinal(), authorSequence, pathSequence, moduleSequence});
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UncommittedStamp{s:");
        sb.append(status);
        sb.append(", a:");
        sb.append(Get.conceptDescriptionText(authorSequence));
        sb.append(", m:");
        sb.append(Get.conceptDescriptionText(moduleSequence));
        sb.append(", p: ");
        sb.append(Get.conceptDescriptionText(pathSequence));
        sb.append('}');
        return sb.toString();
    }
}
