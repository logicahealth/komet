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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.Hashcode;

/**
 * The Class UncommittedStamp.
 *
 * @author kec
 */
public class UncommittedStamp extends Stamp {
   private static final Logger LOG = LogManager.getLogger();

   public long transactionMsb;

   public long transactionLsb;

   /**
    * Instantiates a new uncommitted stamp.
    *
    * @param input the input
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public UncommittedStamp(DataInput input)
            throws IOException {
      super(input);

      this.transactionMsb = input.readLong();
      this.transactionLsb = input.readLong();
   }

   /**
    * Instantiates a new uncommitted stamp.
    *
    * @param status the status
    * @param authorNid the author nid
    * @param moduleNid the module nid
    * @param pathNid the path nid
    */
   public UncommittedStamp(Transaction transaction, Status status, long time, int authorNid, int moduleNid, int pathNid) {
      super(status, time, authorNid, moduleNid, pathNid);
      this.transactionLsb = transaction == null ? 0 : transaction.getTransactionId().getLeastSignificantBits();
      this.transactionMsb = transaction == null ? 0 : transaction.getTransactionId().getMostSignificantBits();
   }

   @Deprecated
   public UncommittedStamp(Status status, int authorNid, int moduleNid, int pathNid) {
      super(status, Long.MAX_VALUE, authorNid, moduleNid, pathNid);
      this.transactionLsb = 0;
      this.transactionMsb = 0;
   }

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

         if (super.equals(obj) &&
                 (this.transactionLsb == other.transactionLsb) &&
                 (this.transactionMsb == other.transactionMsb)) {
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
         this.hashCode = Hashcode.compute(super.hashCode(), Long.valueOf(this.transactionLsb).hashCode(), Long.valueOf(this.transactionMsb).hashCode());
      }

      return this.hashCode;
   }

   public UUID getTransactionId() {
      return new UUID(transactionMsb, transactionLsb);
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
          sb.append(this.getStatus());
          sb.append(", t:");
          sb.append(" UNCOMMITTED: ");
          sb.append(getTimeAsInstant());
          sb.append(", a:");
          sb.append(Get.conceptDescriptionText(this.getAuthorNid()));
          sb.append(", m:");
          sb.append(Get.conceptDescriptionText(this.getModuleNid()));
          sb.append(", p: ");
          sb.append(Get.conceptDescriptionText(this.getPathNid()));
          sb.append(", transaction: ");
          sb.append(getTransactionId().toString());
          sb.append('}');
          return sb.toString();
      } catch (RuntimeException e) {
         LOG.trace("Failure in toString of uncommitted stamp: ", e.getMessage());
      final StringBuilder sb = new StringBuilder();
         sb.append("UncommittedStamp{s:");
         sb.append(this.getStatus());
         sb.append(", t:");
         sb.append(" UNCOMMITTED: ");
         sb.append(getTimeAsInstant());
         sb.append(", a:");
         sb.append(this.getAuthorNid());
         sb.append(", m:");
         sb.append(this.getModuleNid());
         sb.append(", p: ");
         sb.append(this.getPathNid());
         sb.append(", transaction: ");
         sb.append(getTransactionId().toString());
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
   @Override
   public void write(DataOutput output) throws IOException {
      super.write(output);
      output.writeLong(this.transactionMsb);
      output.writeLong(this.transactionLsb);
   }
}
