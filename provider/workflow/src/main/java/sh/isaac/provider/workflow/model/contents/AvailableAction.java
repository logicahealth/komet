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

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.UserRole;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;

//~--- classes ----------------------------------------------------------------

/**
 * The available workflow actions as defined via the workflow definition. Each
 * entry contains a single initial state-action-outcome state triplet that is an
 * available action for a given role.
 *
 * The workflow must be in the initial state and a user must have the workflow
 * role to be able to perform the action.
 *
 * {@link AbstractStorableWorkflowContents}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class AvailableAction
        extends AbstractStorableWorkflowContents {
   /**
    * The workflow definition key for which the Available Action is relevant.
    */
   private UUID definitionId;

   /** The state which the action described may be executed upon. */
   private String initialState;

   /** The action that may be taken. */
   private String action;

   /** The resulting state based on the action taken on the initial state. */
   private String outcomeState;

   /** The workflow role which may perform the action on the initial state. */
   private UserRole role;

   /**
    * Definition uuid most significant bits for this component
    */
   private long definitionIdMsb;

   /**
    * Definition uuid least significant bits for this component
    */
   private long definitionIdLsb;

   //~--- constructors --------------------------------------------------------

   /**
    * Constructor for a new available action based on serialized content.
    *
    * @param data
    *            The data to deserialize into its components
    */
   public AvailableAction(byte[] data) {
      readData(new ByteArrayDataBuffer(data));
   }

   /**
    * Constructor for a new available action on specified entry fields.
    *
    * @param definitionId
    * @param initialState
    * @param action
    * @param outcomeState
    * @param role
    */
   public AvailableAction(UUID definitionId, String initialState, String action, String outcomeState, UserRole role) {
      this.definitionId    = definitionId;
      this.definitionIdMsb = definitionId.getMostSignificantBits();
      this.definitionIdLsb = definitionId.getLeastSignificantBits();
      this.initialState    = initialState;
      this.action          = action;
      this.outcomeState    = outcomeState;
      this.role            = role;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      final AvailableAction other = (AvailableAction) obj;

      return this.definitionId.equals(other.definitionId) &&
             this.initialState.equals(other.initialState) &&
             this.action.equals(other.action) &&
             this.outcomeState.equals(other.outcomeState) &&
             this.role.equals(other.role);
   }

   @Override
   public int hashCode() {
      return this.definitionId.hashCode() + this.initialState.hashCode() + this.action.hashCode() + this.outcomeState.hashCode() +
             this.role.hashCode();
   }

   @Override
   public String toString() {
      return "\n\t\tId: " + this.id + "\n\t\tDefinition Id: " + this.definitionId.toString() + "\n\t\tInitial State: " +
             this.initialState + "\n\t\tAction: " + this.action + "\n\t\tOutcome State: " + this.outcomeState + "\n\t\tRole: " + this.role;
   }

   @Override
   protected void putAdditionalWorkflowFields(ByteArrayDataBuffer out) {
      out.putLong(this.definitionIdMsb);
      out.putLong(this.definitionIdLsb);
      out.putByteArrayField(this.initialState.getBytes());
      out.putByteArrayField(this.action.getBytes());
      out.putByteArrayField(this.outcomeState.getBytes());
      out.putInt(this.role.ordinal());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the action which can be executed on the initial state.
    *
    * @return the action
    */
   public String getAction() {
      return this.action;
   }

   @Override
   protected void getAdditionalWorkflowFields(ByteArrayDataBuffer in) {
      this.definitionIdMsb = in.getLong();
      this.definitionIdLsb = in.getLong();
      this.initialState    = new String(in.getByteArrayField());
      this.action          = new String(in.getByteArrayField());
      this.outcomeState    = new String(in.getByteArrayField());
      this.role            = UserRole.safeValueOf(in.getInt())
                                .get();
      this.definitionId    = new UUID(this.definitionIdMsb, this.definitionIdLsb);
   }

   /**
    * Gets the definition Id associated with the process.
    *
    * @return the key of the definition from which the process is created
    */
   public UUID getDefinitionId() {
      return this.definitionId;
   }

   /**
    * Gets the initial state associated with the available action.
    *
    * @return the initial state
    */
   public String getInitialState() {
      return this.initialState;
   }

   /**
    * Gets the outcome state if the action is performed.
    *
    * @return the outcomeState
    */
   public String getOutcomeState() {
      return this.outcomeState;
   }

   /**
    * Gets the workflow role that a user must have to perform the action.
    *
    * @return the role
    */
   public UserRole getRole() {
      return this.role;
   }
}

