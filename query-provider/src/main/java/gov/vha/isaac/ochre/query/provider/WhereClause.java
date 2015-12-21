/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.query.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "clause")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
public class WhereClause {
    ClauseSemantic semantic;
    List<String> letKeys = new ArrayList<>();
    List<WhereClause> children = new ArrayList<>();

    @XmlTransient
    public ClauseSemantic getSemantic() {
        return semantic;
    }

    public void setSemantic(ClauseSemantic semantic) {
        this.semantic = semantic;
    }

    public String getSemanticString() {
        return semantic.name();
    }

    public void setSemanticString(String semanticName) {
        this.semantic = ClauseSemantic.valueOf(semanticName);
    }

    public List<String> getLetKeys() {
        return letKeys;
    }

    public void setLetKeys(List<String> letKeys) {
        this.letKeys = letKeys;
    }

    public List<WhereClause> getChildren() {
        return children;
    }

    public void setChildren(List<WhereClause> children) {
        this.children = children;
    }
    
}
