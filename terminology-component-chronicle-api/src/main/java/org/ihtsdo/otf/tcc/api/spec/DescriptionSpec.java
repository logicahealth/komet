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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

public class DescriptionSpec implements SpecBI {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(descText);
        out.writeUTF(langText);
        out.writeObject(descUuids);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	descText = in.readUTF();
        	langText = in.readUTF();
        	descUuids = (UUID[]) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    private UUID[] descUuids;

	private String descText;
	private String langText = "en";

	private ConceptSpec conceptSpec;

	public void setConceptSpec(ConceptSpec conceptSpec) {
		this.conceptSpec = conceptSpec;
	}

	public void setDescTypeSpec(ConceptSpec descTypeSpec) {
		this.descTypeSpec = descTypeSpec;
	}

	private ConceptSpec descTypeSpec;


	public DescriptionSpec(UUID[] descUuids, ConceptSpec concept, 
			ConceptSpec descType, String description) {
		this.descUuids = descUuids;
		this.descText = description;
		this.conceptSpec = concept;
		this.descTypeSpec = descType;
	}
	

	public DescriptionSpec(List<UUID> descUuids, ConceptSpec concept, 
			ConceptSpec descType, String description) {
		UUID[] uuid = descUuids.toArray(new UUID[0]);
		this.descUuids = uuid;
		this.descText = description;
		this.conceptSpec = concept;
		this.descTypeSpec = descType;
	}



	public DescriptionVersionBI get(ViewCoordinate c) throws IOException {
		ConceptVersionBI concept = conceptSpec.getStrict(c);
		DescriptionVersionBI desc = (DescriptionVersionBI) Ts.get().getComponent(descUuids);
		if (concept.getNid() != desc.getConceptNid()) {
			throw new RuntimeException("Concept NIDs do not match. 1: "
					+ desc.getConceptNid() + " " + descText + " 2: " + concept);
		}
		if (descText.equals(desc.getText())) {
			return desc;
		} else {
			throw new RuntimeException("Descriptions do not match. 1: "
					+ descText + " 2: " + desc.getText());
		}
	}
	
	public UUID[] getUuids() {
		return descUuids;
	}

	public String getDescText() {
		return descText;
	}

	public ConceptSpec getConceptSpec() {
		return conceptSpec;
	}

	public ConceptSpec getDescTypeSpec() {
		return descTypeSpec;
	}

	public DescriptionSpec(String description, String uuid, ConceptSpec concept, ConceptSpec descType) {
		this(description, UUID.fromString(uuid), concept, descType);
	}

	public DescriptionSpec(String description, UUID uuid, ConceptSpec concept, ConceptSpec descType) {
		this(new UUID[] { uuid }, concept, descType, description);
	}

	public void setDescText(String extractText) {
		descText = extractText;
	}
	
	public String getLangText() {
		return langText;
	}

	public void setLangText(String langText) {
		this.langText = langText;
	}



}
