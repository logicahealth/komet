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



package sh.isaac.provider.workflow.model.contents;

//~--- JDK imports ------------------------------------------------------------

import java.text.SimpleDateFormat;

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.WaitFreeComparable;

//~--- classes ----------------------------------------------------------------

/**
 * An abstract class extended by all Workflow Content Store Entry classes.
 * Contains fields and methods shared by all such Entries.
 *
 * {@link AvailableAction} {@link ProcessHistory}
 * {@link ProcessDetail} {@link DefinitionDetail}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public abstract class AbstractStorableWorkflowContents
         implements WaitFreeComparable {
   /** The Logger made available to each Workflow Content Store Entry class */
   protected final Logger logger = LogManager.getLogger();

   /**
    * As every content store entry is key-value based and as all keys are of
    * type UUID, add in abstract
    */
   protected UUID id;
   private long   primordialUuidMsb;
   private long   primordialUuidLsb;

   /**
    * The write sequence is incremented each time data is written, and provides
    * a check to see if this chronicle has had any changes written since the
    * data for this chronicle was read. If the write sequence does not match
    * the write sequences in the persistence storage, the data needs to be
    * merged prior to writing, according to the principles of a
    * {@code WaitFreeComparable} object.
    */
   private int writeSequence;

   //~--- methods -------------------------------------------------------------

   /**
    * Write a complete binary representation of this chronicle, including all versions, to the
    * ByteArrayDataBuffer using externally valid identifiers (all nids, sequences, replaced with UUIDs).
    *
    * @param out the buffer to write to.
    */
   public final void putExternal(ByteArrayDataBuffer out) {
      assert out.isExternalData() == true;
      writeWorkflowData(out);
      out.putInt(0);  // last data is a zero length version record
   }

   protected abstract void putAdditionalWorkflowFields(ByteArrayDataBuffer out);

   protected void readData(ByteArrayDataBuffer data) {
      if (data.getObjectDataFormatVersion() != 0) {
         throw new UnsupportedOperationException("Can't handle data format version: " +
               data.getObjectDataFormatVersion());
      }

      if (data.isExternalData()) {
         this.writeSequence = Integer.MIN_VALUE;
      } else {
         this.writeSequence = data.getInt();
      }

      this.id = new UUID(data.getLong(), data.getLong());

      if (data.isExternalData()) {
         getAdditionalWorkflowFields(data);
      } else {
         getAdditionalWorkflowFields(data);
      }
   }

   /**
    * Write only the chronicle data (not the versions) to the ByteArrayDataBuffer
    * using identifiers determined by the ByteArrayDataBuffer.isExternalData() to
    * determine if the identifiers should be nids and sequences, or if they should
    * be UUIDs.
    *
    * @param data the buffer to write to.
    */
   protected void writeWorkflowData(ByteArrayDataBuffer data) {
      if (!data.isExternalData()) {
         data.putInt(writeSequence);
      }

      primordialUuidMsb = id.getMostSignificantBits();
      primordialUuidLsb = id.getLeastSignificantBits();
      data.putLong(primordialUuidMsb);
      data.putLong(primordialUuidLsb);
      putAdditionalWorkflowFields(data);
   }

   //~--- get methods ---------------------------------------------------------

   protected abstract void getAdditionalWorkflowFields(ByteArrayDataBuffer in);

   /**
    * Get data to write to datastore, use the writeSequence as it was
    * originally read from the database.
    *
    * @return the data to write
    */
   public byte[] getDataToWrite() {
      return getDataToWrite(this.writeSequence);
   }

   /**
    * Get data to write to datastore. Set the write sequence to the specified
    * value
    *
    * @param writeSequence the write sequence to prepend to the data
    * @return the data to write
    */
   public byte[] getDataToWrite(int writeSequence) {
      setWriteSequence(writeSequence);

      // creating a brand new object
      ByteArrayDataBuffer db = new ByteArrayDataBuffer(10);

      writeWorkflowData(db);
      db.putInt(0);  // zero length version record.
      db.trimToSize();
      return db.getData();
   }

   /**
    * Return an entry's key
    *
    * @return content-store entry key
    */
   public UUID getId() {
      return id;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set an entry's key
    *
    * @param key
    * The key to each content-store entry
    */
   public void setId(UUID key) {
      id = key;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getWriteSequence() {
      return writeSequence;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setWriteSequence(int writeSequence) {
      this.writeSequence = writeSequence;
   }
}

