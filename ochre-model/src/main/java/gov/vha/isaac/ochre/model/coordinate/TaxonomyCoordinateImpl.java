/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "taxonomyCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class TaxonomyCoordinateImpl implements TaxonomyCoordinate {

    PremiseType taxonomyType;
    @XmlJavaTypeAdapter(AnyTypeAdapter.class)
    StampCoordinate stampCoordinate;
    @XmlJavaTypeAdapter(AnyTypeAdapter.class)
    LanguageCoordinate languageCoordinate;
    @XmlJavaTypeAdapter(AnyTypeAdapter.class)
    LogicCoordinate logicCoordinate;
    UUID uuid;
    
    private TaxonomyCoordinateImpl (){
        //for jaxb
    }

    public TaxonomyCoordinateImpl(PremiseType taxonomyType, StampCoordinate stampCoordinate,
                                  LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        this.taxonomyType = taxonomyType;
        this.stampCoordinate = stampCoordinate;
        this.languageCoordinate = languageCoordinate;
        this.logicCoordinate = logicCoordinate;
        uuid = UUID.randomUUID();
    }
    
    @Override
    public PremiseType getTaxonomyType() {
        return taxonomyType;
    }

    @Override
    public StampCoordinate getStampCoordinate() {
       return stampCoordinate;
    }

    @Override
    public LanguageCoordinate getLanguageCoordinate() {
        return languageCoordinate;
    }

    @Override
    public LogicCoordinate getLogicCoordinate() {
        return logicCoordinate;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.taxonomyType);
        hash = 53 * hash + Objects.hashCode(this.stampCoordinate);
        hash = 53 * hash + Objects.hashCode(this.languageCoordinate);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaxonomyCoordinateImpl other = (TaxonomyCoordinateImpl) obj;
        if (this.taxonomyType != other.taxonomyType) {
            return false;
        }
        if (!Objects.equals(this.stampCoordinate, other.stampCoordinate)) {
            return false;
        }
        if (!Objects.equals(this.logicCoordinate, other.logicCoordinate)) {
            return false;
        }
        return Objects.equals(this.languageCoordinate, other.languageCoordinate);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public TaxonomyCoordinateImpl makeAnalog(long stampPositionTime) {
        return new TaxonomyCoordinateImpl(taxonomyType, stampCoordinate.makeAnalog(stampPositionTime),
                                  languageCoordinate, logicCoordinate);
    }

    @Override
    public TaxonomyCoordinateImpl makeAnalog(State... state) {
        return new TaxonomyCoordinateImpl(taxonomyType, stampCoordinate.makeAnalog(state),
                                  languageCoordinate, logicCoordinate);
    }

    @Override
    public TaxonomyCoordinateImpl makeAnalog(PremiseType taxonomyType) {
        return new TaxonomyCoordinateImpl(taxonomyType, stampCoordinate,
                                  languageCoordinate, logicCoordinate);
    }

    @Override
    public String toString() {
        return "TaxonomyCoordinate{" + taxonomyType + ",\n" + stampCoordinate + ", \n" + languageCoordinate + ", \n" + logicCoordinate + ", uuid=" + uuid + '}';
    }
    
    private static class AnyTypeAdapter extends XmlAdapter<Object,Object> {
        public Object unmarshal(Object v) { return v; }
        public Object marshal(Object v) { return v; }
    }
    
}
