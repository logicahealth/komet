/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.otf.tcc.api.spec;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSnapshot;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

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
 * @version Enter version here..., 13/03/24
 * @author Enter your name here...
 */
@XmlRootElement(name = "concept-spec")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ConceptSpec extends ConceptProxy implements SpecBI {

    
    /**
     * dataversion for serialization versioning
     */
    protected static final int dataVersion = 1;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Native identifier for the concept proxied by this object
     */


    /**
     * Field description
     */
    transient private ConceptChronology localChronicle;

    /**
     * Field description
     */
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
        this(description, uuid, new RelSpec[]{});
    }

    public ConceptSpec(ConceptSpec conceptSpec) {
        this(conceptSpec.getConceptDescriptionText(), conceptSpec.getUuids(), new RelSpec[]{});
    }

    /**
     * Constructs ...
     *
     *
     * @param simpleSpec
     */
    public ConceptSpec(SimpleConceptSpecification simpleSpec) {
        this(simpleSpec.getDescription(), simpleSpec.getUuid(), new RelSpec[]{});
    }

    /**
     * Constructs ...
     *
     *
     * @param description
     * @param uuid
     */
    public ConceptSpec(String description, UUID uuid) {
        this(description, new UUID[]{uuid}, new RelSpec[]{});
    }

    /**
     * Constructs ...
     *
     *
     * @param description
     * @param uuid
     * @param parentConcept - used as the destination in a relspec, with a type
     * of {@link Snomed#IS_A} and a source of this spec being created.
     */
    public ConceptSpec(String description, UUID uuid, ConceptSpec parentConcept) {
        this(description, new UUID[]{uuid}, new RelSpec[1]);
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
        this(description, new UUID[]{uuid}, relSpecs);
    }

    public ConceptSpec(int nid) throws IOException {
        this(Ts.get().getConcept(nid));
    }

    public ConceptSpec(ConceptChronicleBI chronicle) throws IOException {
        this(chronicle.getDescriptions().iterator().next().getPrimordialVersion().getText(),
                chronicle.getUuidList().toArray(new UUID[0]));
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
        this.relSpecs = relSpecs;
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
            relSpecs = (RelSpec[]) in.readObject();
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
    private void validateDescription(ConceptChronology local) throws IOException, ContradictionException {
        if (!local.containsDescription(getConceptDescriptionText())) {
            throw new ValidationException("No description matching: '" + getConceptDescriptionText() + "' found for:\n" + local);
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
    private void validateDescription(ConceptChronology localVersion, ViewCoordinate c)
            throws IOException, ContradictionException {

        if (!localVersion.containsDescription(description, c)) {
            throw new ValidationException("No description matching: '" + getConceptDescriptionText() + "' found for:\n"
                    + localVersion);
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
        out.writeUTF(getConceptDescriptionText());
        out.writeObject(getUuids());
        out.writeObject(relSpecs);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws ValidationException
     */
    public ConceptChronology getLenient() throws ValidationException {

        if (localChronicle != null) {
            return localChronicle;
        }

        boolean found = false;

        for (UUID uuid : getUuids()) {
            if (Get.identifierService().hasUuid(uuid)) {
                found = true;

                break;
            }
        }

        if (!found) {
            throw new ValidationException("No matching ids in db: " + this.toString());
        }
        try {
            localChronicle = Get.conceptService().getConcept(getUuids());
            validateDescription(localChronicle);
        } catch (IOException ex) {
            localChronicle = null;
            throw new RuntimeException(ex);
        } catch (ContradictionException ex) {
            throw new ValidationException(ex);
        }
        return localChronicle;

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
        ConceptSnapshot conceptVersion = getStrict(vc);
        return conceptVersion.getNid();
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
    public ConceptSnapshot getStrict(ViewCoordinate vc) throws ValidationException, IOException {
        ConceptChronology conceptChronology = getLenient();
        
        ConceptSnapshot conceptSnapshot = Get.conceptService().getSnapshot(vc).getConceptSnapshot(conceptChronology.getConceptSequence());
        conceptSnapshot.containsActiveDescription(description);
        return conceptSnapshot;
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
        if (!Objects.equals(this.getConceptDescriptionText(), other.getConceptDescriptionText())) {
            return false;
        }
        if (this.relSpecs != other.relSpecs) {
            if (!Arrays.deepEquals(this.relSpecs, other.relSpecs)) {
                return false;
            }

        }
        return true;
    }
}
