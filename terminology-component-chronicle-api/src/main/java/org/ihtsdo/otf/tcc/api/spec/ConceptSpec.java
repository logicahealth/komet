/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.otf.tcc.api.spec;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Arrays;
import java.util.UUID;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 13/03/24
 * @author         Enter your name here...
 */
public class ConceptSpec implements SpecBI {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /** Field description */
   private static final int dataVersion = 1;

   /** Field description */
   transient private int nid = Integer.MAX_VALUE;

   /** Field description */
   transient private ConceptChronicleBI localChronicle;

   /** Field description */
   transient private ConceptVersionBI localVersion;

   /** Field description */
   private UUID[] uuids;

   /** Field description */
   private String description;

   /** Field description */
   private RelSpec[] relSpecs;

   /**
    * added to allow JavaBeans spec use.
    */
   public ConceptSpec() {
      super();
   }

   /**
    * Constructs ...
    *
    *
    * @param description
    * @param uuid
    */
   public ConceptSpec(String description, String uuid) {
      this(description, uuid, new RelSpec[] {});
   }

   /**
    * Constructs ...
    *
    *
    * @param description
    * @param uuid
    */
   public ConceptSpec(String description, UUID uuid) {
      this(description, new UUID[] { uuid }, new RelSpec[] {});
   }

   /**
    * Constructs ...
    *
    *
    * @param description
    * @param uuid
    * @param relSpecs
    */
   public ConceptSpec(String description, String uuid, RelSpec... relSpecs) {
      this(description, UUID.fromString(uuid), relSpecs);
   }

   /**
    * Constructs ...
    *
    *
    * @param description
    * @param uuid
    * @param relSpecs
    */
   public ConceptSpec(String description, UUID uuid, RelSpec... relSpecs) {
      this(description, new UUID[] { uuid }, relSpecs);
   }

   /**
    * Constructs ...
    *
    *
    * @param description
    * @param uuids
    * @param relSpecs
    */
   public ConceptSpec(String description, UUID[] uuids, RelSpec... relSpecs) {
      this.uuids       = uuids;
      this.description = description;
      this.relSpecs    = relSpecs;
   }

   /**
    * Method description
    *
    *
    * @param in
    *
    * @throws ClassNotFoundException
    * @throws IOException
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == dataVersion) {
         description = in.readUTF();
         uuids       = (UUID[]) in.readObject();
         relSpecs    = (RelSpec[]) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public String toString() {
      return "ConceptSpec{" + description + "; " + Arrays.asList(uuids) + "}";
   }

   /**
    * Method description
    *
    *
    * @param local
    *
    * @throws ContradictionException
    * @throws IOException
    */
   private void validateDescription(ConceptChronicleBI local) throws IOException, ContradictionException {
      boolean found = false;

      for (DescriptionChronicleBI desc : local.getDescriptions()) {
         for (DescriptionVersionBI descv : desc.getVersions()) {
            if (descv.getText().equals(description)) {
               found = true;

               break;
            }
         }
      }

      if (found == false) {
         throw new ValidationException("No description matching: '" + description + "' found for:\n" + local);
      }
   }

   /**
    * Method description
    *
    *
    * @param localVersion
    * @param c
    *
    * @throws ContradictionException
    * @throws IOException
    */
   private void validateDescription(ConceptVersionBI localVersion, ViewCoordinate c)
           throws IOException, ContradictionException {
      boolean found = false;

      for (DescriptionVersionBI desc : localVersion.getDescriptionsActive()) {
         if (desc.getText().equals(description)) {
            found = true;

            break;
         }
      }

      if (found == false) {
         throw new ValidationException("No description matching: '" + description + "' found for:\n"
                                       + localVersion);
      }
   }

   /**
    * Method description
    *
    *
    * @param local
    *
    * @throws IOException
    */
   private void validateRelationships(ConceptChronicleBI local) throws IOException {
      if ((relSpecs == null) || (relSpecs.length == 0)) {
         return;
      }

next:
      for (RelSpec relSpec : relSpecs) {
         ConceptChronicleBI relType     = relSpec.getRelTypeSpec().getLenient();
         ConceptChronicleBI destination = relSpec.getDestinationSpec().getLenient();
         NidSetBI           typeNids    = new NidSet();

         typeNids.add(relType.getNid());

         for (RelationshipChronicleBI rel : local.getRelationshipsOutgoing()) {
            for (RelationshipVersionBI rv : rel.getVersions()) {
               if ((rv.getTypeNid() == relType.getNid())
                   && (rv.getDestinationNid() == destination.getNid())) {
                  continue next;
               }
            }
         }

         throw new ValidationException("No match for RelSpec: " + relSpec);
      }
   }

   /**
    * Method description
    *
    *
    * @param local
    * @param c
    *
    * @throws IOException
    */
   private void validateRelationships(ConceptVersionBI local, ViewCoordinate c) throws IOException {
      if ((relSpecs == null) || (relSpecs.length == 0)) {
         return;
      }

next:
      for (RelSpec relSpec : relSpecs) {
         ConceptVersionBI relType     = relSpec.getRelTypeSpec().getStrict(c);
         ConceptVersionBI destination = relSpec.getDestinationSpec().getStrict(c);
         NidSetBI         typeNids    = new NidSet();

         typeNids.add(relType.getNid());

         for (ConceptVersionBI dest : local.getRelationshipsOutgoingDestinations(typeNids)) {
            if (dest.equals(destination)) {
               continue next;
            }
         }

         throw new ValidationException("No match for RelSpec: " + relSpec);
      }
   }

   /**
    * Method description
    *
    *
    * @param out
    *
    * @throws IOException
    */
   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeUTF(description);
      out.writeObject(uuids);
      out.writeObject(relSpecs);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public String getDescription() {
      return description;
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws IOException
    * @throws ValidationException
    */
   public ConceptChronicleBI getLenient() throws ValidationException, IOException {
      try {
         if (localChronicle != null) {
            return localChronicle;
         }

         boolean found = false;

         for (UUID uuid : uuids) {
            if ((Ts.get() != null) && Ts.get().hasUuid(uuid)) {
               found = true;

               break;
            }
         }

         if (!found) {
            throw new ValidationException("No matching ids in db: " + this.toString());
         }

         localChronicle = Ts.get().getConcept(uuids);

         try {
            validateDescription(localChronicle);
            validateRelationships(localChronicle);
         } catch (IOException | ContradictionException ex) {
            localChronicle = null;

            throw ex;
         }
         nid = localChronicle.getNid();
         return localChronicle;
      } catch (ContradictionException e) {
         throw new ValidationException(e);
      }
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws IOException
    * @throws ValidationException
    */
   public int getNid() throws ValidationException, IOException {
      if (nid == Integer.MAX_VALUE) {
         ConceptChronicleBI conceptChronicle = getLenient();

         nid = conceptChronicle.getNid();
      }

      return nid;
   }

   /**
    * Method description
    *
    *
    * @param vc
    *
    * @return
    *
    * @throws IOException
    * @throws ValidationException
    */
   public int getNid(ViewCoordinate vc) throws ValidationException, IOException {
      if (nid == Integer.MAX_VALUE) {
         ConceptVersionBI conceptVersion = getStrict(vc);

         nid = conceptVersion.getNid();
      }

      return nid;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public RelSpec[] getRelSpecs() {
      return relSpecs;
   }

   /**
    * Method description
    *
    *
    * @param vc
    *
    * @return
    *
    * @throws IOException
    * @throws ValidationException
    */
   public ConceptVersionBI getStrict(ViewCoordinate vc) throws ValidationException, IOException {
      try {
         if (localVersion != null) {
            return localVersion;
         }

         boolean found = false;

         for (UUID uuid : uuids) {
            if (Ts.get().hasUuid(uuid)) {
               found = true;

               break;
            }
         }

         if (!found) {
            throw new ValidationException("No matching ids in db: " + this.toString());
         }

         localVersion = Ts.get().getConceptVersion(vc, uuids);

         try {
            validateDescription(localVersion, vc);
            validateRelationships(localVersion, vc);
         } catch (IOException | ContradictionException ex) {
            localVersion = null;

            throw ex;
         }
         nid = localVersion.getNid();
         return localVersion;
      } catch (ContradictionException e) {
         throw new ValidationException(e);
      }
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public String[] getUuidStrs() {
      String[] results = new String[uuids.length];

      for (int i = 0; i < uuids.length; i++) {
         results[i] = uuids[i].toString();
      }

      return results;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public UUID[] getUuids() {
      return uuids;
   }

   /**
    * added as an alternative way to get the uuids as strings rather than UUID
    * objects
    * this was done to help with Maven making use of this class
    *
    * @return
    */
   public String[] getUuidsAsString() {
      String[] returnVal = new String[uuids.length];
      int      i         = 0;

      for (UUID uuid : uuids) {
         returnVal[i++] = uuid.toString();
      }

      return returnVal;
   }

   /**
    * Method description
    *
    *
    * @param description
    */
   public void setDescription(String description) {
      this.description = description;
   }

   /**
    * Method description
    *
    *
    * @param relSpecs
    */
   public void setRelSpecs(RelSpec[] relSpecs) {
      this.relSpecs = relSpecs;
   }

   /**
    * Method description
    *
    *
    * @param uuidStrs
    */
   public void setUuidStrs(String[] uuidStrs) {
      this.uuids = new UUID[uuidStrs.length];

      for (int i = 0; i < uuidStrs.length; i++) {
         this.uuids[i] = UUID.fromString(uuidStrs[i]);
      }
   }

   /**
    * Method description
    *
    *
    * @param uuids
    */
   public void setUuids(UUID[] uuids) {
      this.uuids = uuids;
   }

   /**
    * Added primarily for Maven so that using a String type configuration in
    * a POM file the UUIDs array could be set.
    * This allows the ConceptSpec class to be embedded into a object to be configured
    * by Maven POM configuration. Note that the ConceptDescriptor class also exists
    * for a similar purpose, however it exists in a dependent project and
    * cannot
    * be used in this project.
    *
    * @param uuids
    */
   public void setUuidsAsString(String[] uuids) {
      this.uuids = new UUID[uuids.length];

      int i = 0;

      for (String uuid : uuids) {
         this.uuids[i++] = UUID.fromString(uuid);
      }
   }
}
