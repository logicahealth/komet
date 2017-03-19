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
    * @param bpmn2Id
    * @param name
    * @param namespace
    * @param version
    * @param roles
    * @param description
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

   @Override
   public boolean equals(Object obj) {
      DefinitionDetail other = (DefinitionDetail) obj;

      return this.bpmn2Id.equals(other.bpmn2Id) &&
             this.name.equals(other.name) &&
             this.namespace.equals(other.namespace) &&
             this.version.equals(other.version) &&
             this.roles.equals(other.roles) &&
             this.description.equals(other.description) &&
             (this.importDate == other.importDate);
   }

   @Override
   public int hashCode() {
      return bpmn2Id.hashCode() + name.hashCode() + namespace.hashCode() + version.hashCode() + roles.hashCode() +
             description.hashCode() + new Long(importDate).hashCode();
   }

   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      for (UserRole r: roles) {
         buf.append(r + ", ");
      }

      LocalDate date             = LocalDate.ofEpochDay(importDate);
      String    importDateString = BPMNInfo.workflowDateFormatter.format(date);

      return "\n\t\tId: " + id + "\n\t\tBPMN2 Id: " + bpmn2Id + "\n\t\tName: " + name + "\n\t\tNamespace: " +
             namespace + "\n\t\tVersion: " + version + "\n\t\tRoles: " + buf.toString() + "\n\t\tDescription: " +
             description + "\n\t\tImport Date: " + importDateString;
   }

   @Override
   protected void putAdditionalWorkflowFields(ByteArrayDataBuffer out) {
      out.putByteArrayField(bpmn2Id.getBytes());
      out.putByteArrayField(name.getBytes());
      out.putByteArrayField(namespace.getBytes());
      out.putByteArrayField(version.getBytes());
      out.putInt(roles.size());

      for (UserRole s: roles) {
         out.putInt(s.ordinal());
      }

      out.putByteArrayField(description.getBytes());
      out.putLong(importDate);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   protected void getAdditionalWorkflowFields(ByteArrayDataBuffer in) {
      bpmn2Id   = new String(in.getByteArrayField());
      name      = new String(in.getByteArrayField());
      namespace = new String(in.getByteArrayField());
      version   = new String(in.getByteArrayField());

      int colCount = in.getInt();

      roles = new HashSet<>();

      for (int i = 0; i < colCount; i++) {
         roles.add(UserRole.safeValueOf(in.getInt())
                           .get());
      }

      description = new String(in.getByteArrayField());
      importDate  = in.getLong();
   }

   /**
    * Gets the BPMN2 file's Id
    *
    * @return bpmn2 id
    */
   public String getBpmn2Id() {
      return bpmn2Id;
   }

   /**
    * Gets the name of the workflow definition
    *
    * @return definition name
    */
   public String getDescription() {
      return description;
   }

   /**
    * Gets the date which the BPM2 file containing the definition was imported
    * into the system
    *
    * @return version
    */
   public long getImportDate() {
      return importDate;
   }

   /**
    * Gets the name of the workflow definition
    *
    * @return definition name
    */
   public String getName() {
      return name;
   }

   /**
    * Gets the namespace for which the definition is relevant
    *
    * @return namespace
    */
   public String getNamespace() {
      return namespace;
   }

   /**
    * Gets the workflow roles that are used within the definition
    *
    * @return the workflow roles available
    */
   public Set<UserRole> getRoles() {
      return roles;
   }

   /**
    * Gets the definition's version
    *
    * @return version
    */
   public String getVersion() {
      return version;
   }
}

