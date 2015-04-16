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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.tcc.api.constraint.RelConstraintIncoming;
import org.ihtsdo.otf.tcc.api.constraint.RelConstraintOutgoing;

@XmlRootElement(name = "rel-spec")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RelSpec implements SpecBI {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(originSpec);
        out.writeObject(relTypeSpec);
        out.writeObject(destinationSpec);
        out.writeObject(characteristicSpec);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion && objDataVersion > 0) {
            originSpec = (ConceptSpec) in.readObject();
            relTypeSpec = (ConceptSpec) in.readObject();
            destinationSpec = (ConceptSpec) in.readObject();
            if (objDataVersion >= 2)
            {
                characteristicSpec = (ConceptSpec) in.readObject();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    private ConceptSpec originSpec;
    private ConceptSpec relTypeSpec;
    private ConceptSpec destinationSpec;
    private ConceptSpec characteristicSpec;

    /**
     * No arg constructor for jaxb
     */
    public RelSpec() {
    }
    
    public RelSpec(ConceptSpec originSpec, ConceptSpec relTypeSpec, ConceptSpec destinationSpec) {
        this(originSpec, relTypeSpec, destinationSpec, null);
    }

    public RelSpec(ConceptSpec originSpec, ConceptSpec relTypeSpec, ConceptSpec destinationSpec, ConceptSpec characteristicSpec) {
        super();
        this.originSpec = originSpec;
        this.relTypeSpec = relTypeSpec;
        this.destinationSpec = destinationSpec;
        this.characteristicSpec = characteristicSpec;
    }

    public ConceptSpec getOriginSpec() {
        return originSpec;
    }

    public ConceptSpec getRelTypeSpec() {
        return relTypeSpec;
    }

    public ConceptSpec getDestinationSpec() {
        return destinationSpec;
    }
    
    public ConceptSpec getCharacteristicSpec() {
        return characteristicSpec;
    }
    
    public RelConstraintOutgoing getOriginatingRelConstraint() {
        return new RelConstraintOutgoing(originSpec, relTypeSpec, destinationSpec);
    }
    public RelConstraintIncoming getDestinationRelConstraint() {
        return new RelConstraintIncoming(originSpec, relTypeSpec, destinationSpec);
    }

    public void setOriginSpec(ConceptSpec originSpec) {
        this.originSpec = originSpec;
    }

    public void setRelTypeSpec(ConceptSpec relTypeSpec) {
        this.relTypeSpec = relTypeSpec;
    }

    public void setDestinationSpec(ConceptSpec destinationSpec) {
        this.destinationSpec = destinationSpec;
    }
    
    public void setCharacteristicSpec(ConceptSpec characteristicSpec) {
        this.characteristicSpec = characteristicSpec;
    }

}
