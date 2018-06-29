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
import sh.isaac.api.Status;
import sh.isaac.api.util.Hashcode;

//~--- classes ----------------------------------------------------------------

/**
 * The Class UncommittedStamp.
 *
 * @author kec
 */
public class UncommittedStamp {
   /** The hash code. */
   public int hashCode = Integer.MAX_VALUE;

   /** The status. */
   public Status status;

   /** The author nid. */
   public int authorNid;

   /** The module nid. */
   public int moduleNid;

   /** The path nid. */
   public int pathNid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new uncommitted stamp.
    *
    * @param input the input
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public UncommittedStamp(DataInput input)
            throws IOException {
      super();

      if (input.readBoolean()) {
         this.status = Status.ACTIVE;
      } else {
         this.status = Status.INACTIVE;
      }

      this.authorNid = input.readInt();
      this.moduleNid = input.readInt();
      this.pathNid   = input.readInt();
   }

   /**
    * Instantiates a new uncommitted stamp.
    *
    * @param status the status
    * @param authorNid the author nid
    * @param moduleNid the module nid
    * @param pathNid the path nid
    */
   public UncommittedStamp(Status status, int authorNid, int moduleNid, int pathNid) {
      super();
      this.status         = status;
      this.authorNid = authorNid;
      this.moduleNid = moduleNid;
      this.pathNid   = pathNid;
      assert status != null:
             "s: " + status + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert pathNid < 0:
             "s: " + status + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert moduleNid < 0:
             "s: " + status + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert authorNid < 0:
             "s: " + status + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UncommittedStamp) {
         final UncommittedStamp other = (UncommittedStamp) obj;

         if ((this.status == other.status) &&
               (this.authorNid == other.authorNid) &&
               (this.pathNid == other.pathNid) &&
               (this.moduleNid == other.moduleNid)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      if (this.hashCode == Integer.MAX_VALUE) {
         this.hashCode = Hashcode.compute(new int[] { this.status.ordinal(), this.authorNid, this.pathNid,
               this.moduleNid });
      }

      return this.hashCode;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      
      try {
      final StringBuilder sb = new StringBuilder();
         sb.append("UncommittedStamp{s:");
         sb.append(this.status);
         sb.append(", a:");
         sb.append(Get.conceptDescriptionText(this.authorNid));
         sb.append(", m:");
         sb.append(Get.conceptDescriptionText(this.moduleNid));
         sb.append(", p: ");
         sb.append(Get.conceptDescriptionText(this.pathNid));
         sb.append('}');
         return sb.toString();
      } catch (RuntimeException e) {
         e.printStackTrace();
      final StringBuilder sb = new StringBuilder();
          sb.append("UncommittedStamp{s:");
         sb.append(this.status);
         sb.append(", a:");
         sb.append(this.authorNid);
         sb.append(", m:");
         sb.append(this.moduleNid);
         sb.append(", p: ");
         sb.append(this.pathNid);
         sb.append('}');
         return sb.toString();
     }
   }

   /**
    * Write.
    *
    * @param output the output
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(DataOutput output)
            throws IOException {
      output.writeBoolean(this.status.isActive());
      output.writeInt(this.authorNid);
      output.writeInt(this.moduleNid);
      output.writeInt(this.pathNid);
   }
}

