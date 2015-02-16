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

import gov.vha.isaac.ochre.api.ConceptProxy;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
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
import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 13/03/24
 * @author         Enter your name here...
 */

@XmlRootElement(name = "concept-spec")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ConceptSpec extends ConceptProxy implements SpecBI {

   /** dataversion for serialization versioning */
   protected static final int dataVersion = 1;
   /**
    *
    */
   private static final long serialVersionUID = 1L;
   /** Native identifier for the concept proxied by this object  */
   protected transient int nid = Integer.MAX_VALUE;

   @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConceptSpec other = (ConceptSpec) obj;
        if (!Arrays.deepEquals(this.getUuids(), other.getUuids())) {
            return false;
        }
        if (!Objects.equals(this.getDescription(), other.getDescription())) {
            return false;
        }
        if (this.relSpecs != other.relSpecs) {
            if (!Arrays.deepEquals(this.relSpecs, other.relSpecs)) {
                return false;
            }
            
        }
        return true;
    }

   /** Field description */
   transient private ConceptChronicleBI localChronicle;

   /** Field description */
   transient private ConceptVersionBI localVersion;

   /** Field description */
   private RelSpec[] relSpecs = new RelSpec[0];

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
   public ConceptSpec(ConceptSpec conceptSpec) {
      this(conceptSpec.getDescription(), conceptSpec.getUuids(), new RelSpec[] {});
   }
   /**
    * Constructs ...
    *
    *
    */
   public ConceptSpec(SimpleConceptSpecification simpleSpec) {
      this(simpleSpec.getDescription(), simpleSpec.getUuid(), new RelSpec[] {});
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
    * @param parentConcept - used as the destination in a relspec, with a type of {@link Snomed#IS_A} and a source of this spec being created.
    */
   public ConceptSpec(String description, UUID uuid, ConceptSpec parentConcept) {
      this(description, new UUID[] { uuid }, new RelSpec[1]);
      this.relSpecs[0] = new RelSpec(this, Snomed.IS_A, parentConcept);
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

   public ConceptSpec(int nid) throws IOException{
      this(Ts.get().getConcept(nid));
   }

   public ConceptSpec(ConceptChronicleBI chronicle) throws IOException{
      this(chronicle.getDescriptions().iterator().next().getPrimordialVersion().getText(), 
              chronicle.getUUIDs().toArray(new UUID[0]));
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
      super(description, uuids);
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
         setDescription(in.readUTF());
         setUuids((UUID[]) in.readObject());
         relSpecs    = (RelSpec[]) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
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
            if (descv.getText().equals(getDescription())) {
               found = true;

               break;
            }
         }
      }

      if (found == false) {
         throw new ValidationException("No description matching: '" + getDescription() + "' found for:\n" + local);
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
         if (desc.getText().equals(getDescription())) {
            found = true;

            break;
         }
      }

      if (found == false) {
         throw new ValidationException("No description matching: '" + getDescription() + "' found for:\n"
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
      out.writeUTF(getDescription());
      out.writeObject(getUuids());
      out.writeObject(relSpecs);
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
       assert Ts.get() != null: "Ts not properly set up. Ts.get() is null();";
      try {
         if (localChronicle != null) {
            return localChronicle;
         }

         boolean found = false;

         for (UUID uuid : getUuids()) {
            if (Ts.get().hasUuid(uuid)) {
               found = true;

               break;
            }
         }

         if (!found) {
            throw new ValidationException("No matching ids in db: " + this.toString());
         }

         localChronicle = Ts.get().getConcept(getUuids());

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

         for (UUID uuid : getUuids()) {
            if (Ts.get().hasUuid(uuid)) {
               found = true;

               break;
            }
         }

         if (!found) {
            throw new ValidationException("No matching ids in db: " + this.toString());
         }

         localVersion = Ts.get().getConceptVersion(vc, getUuids());

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
    * @param relSpecs
    */
   public void setRelSpecs(RelSpec[] relSpecs) {
      this.relSpecs = relSpecs;
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
}
