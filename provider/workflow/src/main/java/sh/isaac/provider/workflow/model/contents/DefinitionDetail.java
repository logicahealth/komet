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

import java.time.LocalDate;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.UserRole;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.provider.workflow.BPMNInfo;

//~--- classes ----------------------------------------------------------------

/**
 * The metadata defining a given workflow definition.
 *
 * {@link AbstractStorableWorkflowContents}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class DefinitionDetail
        extends AbstractStorableWorkflowContents {
   /** The bpmn2 id that contains the definition if it exists. */
   private String bpmn2Id;

   /** The definition name. */
   private String name;

   /** The definition namespace. */
   private String namespace;

   /** The version of the definition. */
   private String version;

   /** The workflow roles available defined via the definition . */
   private Set<UserRole> roles;

   /** A description of the purpose of the Definition pulled by BPMN2. */
   private String description;

   /** Automated date when BPMN2 imported into bundle. */
   private long importDate;

   //~--- constructors --------------------------------------------------------

   /**
    * Constructor for a new definition based on serialized content.
    *
    * @param data
    *            The data to deserialize into its components
    */
   public DefinitionDetail(byte[] data) {
      readData(new ByteArrayDataBuffer(data));
   }

   /**
    * Constructor for a new definition based on specified entry fields.
    *
    * @param bpmn2Id the bpmn 2 id
    * @param name the name
    * @param namespace the namespace
    * @param version the version
    * @param roles the roles
    * @param description the description
    */
   public DefinitionDetail(String bpmn2Id,
                           String name,
                           String namespace,
                           String version,
                           Set<UserRole> roles,
                           String description) {
      this.bpmn2Id     = bpmn2Id;
      this.name        = name;
      this.namespace   = namespace;
      this.version     = version;
      this.roles       = roles;
      this.description = description;
      this.importDate  = new Date().getTime();
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
      final DefinitionDetail other = (DefinitionDetail) obj;

      return this.bpmn2Id.equals(other.bpmn2Id) &&
             this.name.equals(other.name) &&
             this.namespace.equals(other.namespace) &&
             this.version.equals(other.version) &&
             this.roles.equals(other.roles) &&
             this.description.equals(other.description) &&
             (this.importDate == other.importDate);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.bpmn2Id.hashCode() + this.name.hashCode() + this.namespace.hashCode() + this.version.hashCode() + this.roles.hashCode() +
             this.description.hashCode() + new Long(this.importDate).hashCode();
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuffer buf = new StringBuffer();

      for (final UserRole r: this.roles) {
         buf.append(r + ", ");
      }

      final LocalDate date             = LocalDate.ofEpochDay(this.importDate);
      final String    importDateString = BPMNInfo.workflowDateFormatter.format(date);

      return "\n\t\tId: " + this.id + "\n\t\tBPMN2 Id: " + this.bpmn2Id + "\n\t\tName: " + this.name + "\n\t\tNamespace: " +
             this.namespace + "\n\t\tVersion: " + this.version + "\n\t\tRoles: " + buf.toString() + "\n\t\tDescription: " +
             this.description + "\n\t\tImport Date: " + importDateString;
   }

   /**
    * Put additional workflow fields.
    *
    * @param out the out
    */
   @Override
   protected void putAdditionalWorkflowFields(ByteArrayDataBuffer out) {
      out.putByteArrayField(this.bpmn2Id.getBytes());
      out.putByteArrayField(this.name.getBytes());
      out.putByteArrayField(this.namespace.getBytes());
      out.putByteArrayField(this.version.getBytes());
      out.putInt(this.roles.size());

      for (final UserRole s: this.roles) {
         out.putInt(s.ordinal());
      }

      out.putByteArrayField(this.description.getBytes());
      out.putLong(this.importDate);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the additional workflow fields.
    *
    * @param in the in
    * @return the additional workflow fields
    */
   @Override
   protected void getAdditionalWorkflowFields(ByteArrayDataBuffer in) {
      this.bpmn2Id   = new String(in.getByteArrayField());
      this.name      = new String(in.getByteArrayField());
      this.namespace = new String(in.getByteArrayField());
      this.version   = new String(in.getByteArrayField());

      final int colCount = in.getInt();

      this.roles = new HashSet<>();

      for (int i = 0; i < colCount; i++) {
         this.roles.add(UserRole.safeValueOf(in.getInt())
                           .get());
      }

      this.description = new String(in.getByteArrayField());
      this.importDate  = in.getLong();
   }

   /**
    * Gets the BPMN2 file's Id.
    *
    * @return bpmn2 id
    */
   public String getBpmn2Id() {
      return this.bpmn2Id;
   }

   /**
    * Gets the name of the workflow definition.
    *
    * @return definition name
    */
   public String getDescription() {
      return this.description;
   }

   /**
    * Gets the date which the BPM2 file containing the definition was imported
    * into the system.
    *
    * @return version
    */
   public long getImportDate() {
      return this.importDate;
   }

   /**
    * Gets the name of the workflow definition.
    *
    * @return definition name
    */
   public String getName() {
      return this.name;
   }

   /**
    * Gets the namespace for which the definition is relevant.
    *
    * @return namespace
    */
   public String getNamespace() {
      return this.namespace;
   }

   /**
    * Gets the workflow roles that are used within the definition.
    *
    * @return the workflow roles available
    */
   public Set<UserRole> getRoles() {
      return this.roles;
   }

   /**
    * Gets the definition's version.
    *
    * @return version
    */
   public String getVersion() {
      return this.version;
   }
}

