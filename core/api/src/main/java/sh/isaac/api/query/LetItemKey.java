/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.query;

import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "LetKey")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"itemName", "itemId"})
public class LetItemKey {
    UUID itemId;
    String itemName;
    /**
     * No arg constructor for Jaxb. 
     */
    public LetItemKey() {
    }

    public LetItemKey(String itemName) {
        this.itemId = UUID.randomUUID();
        this.itemName = itemName;
    }

    public LetItemKey(String itemName, UUID itemId) {
        this.itemId = itemId;
        this.itemName = itemName;
    }

    @XmlAttribute(name = "uuid")
    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    @XmlAttribute(name = "name")
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.itemId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LetItemKey other = (LetItemKey) obj;
        return Objects.equals(this.itemId, other.itemId);
    }

    @Override
    public String toString() {
        return itemName;
    }
    
    
}
