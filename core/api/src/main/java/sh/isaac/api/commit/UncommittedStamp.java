/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.commit;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.util.Hashcode;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class UncommittedStamp {
   public int   hashCode = Integer.MAX_VALUE;
   public State status;
   public int   authorSequence;
   public int   moduleSequence;
   public int   pathSequence;

   //~--- constructors --------------------------------------------------------

   public UncommittedStamp(DataInput input)
            throws IOException {
      super();

      if (input.readBoolean()) {
         this.status = State.ACTIVE;
      } else {
         this.status = State.INACTIVE;
      }

      this.authorSequence = input.readInt();
      this.moduleSequence = input.readInt();
      this.pathSequence   = input.readInt();
   }

   public UncommittedStamp(State status, int authorSequence, int moduleSequence, int pathSequence) {
      super();
      this.status         = status;
      this.authorSequence = authorSequence;
      this.moduleSequence = moduleSequence;
      this.pathSequence   = pathSequence;
      assert status != null:
             "s: " + status + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert pathSequence > 0:
             "s: " + status + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert moduleSequence > 0:
             "s: " + status + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert authorSequence > 0:
             "s: " + status + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UncommittedStamp) {
         final UncommittedStamp other = (UncommittedStamp) obj;

         if ((this.status == other.status) &&
               (this.authorSequence == other.authorSequence) &&
               (this.pathSequence == other.pathSequence) &&
               (this.moduleSequence == other.moduleSequence)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      if (this.hashCode == Integer.MAX_VALUE) {
         this.hashCode = Hashcode.compute(new int[] { this.status.ordinal(), this.authorSequence, this.pathSequence, this.moduleSequence });
      }

      return this.hashCode;
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("UncommittedStamp{s:");
      sb.append(this.status);
      sb.append(", a:");
      sb.append(Get.conceptDescriptionText(this.authorSequence));
      sb.append(", m:");
      sb.append(Get.conceptDescriptionText(this.moduleSequence));
      sb.append(", p: ");
      sb.append(Get.conceptDescriptionText(this.pathSequence));
      sb.append('}');
      return sb.toString();
   }

   public void write(DataOutput output)
            throws IOException {
      output.writeBoolean(this.status.isActive());
      output.writeInt(this.authorSequence);
      output.writeInt(this.moduleSequence);
      output.writeInt(this.pathSequence);
   }
}

