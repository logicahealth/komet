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

import java.util.Map;
import java.util.UUID;
import javafx.beans.property.SimpleObjectProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableCoordinate;
import sh.isaac.api.observable.coordinate.ObservableCoordinateImpl;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "manifoldCoordinate")
@XmlAccessorType(value = XmlAccessType.NONE)
public class ManifoldCoordinateForQuery extends ObservableCoordinateImpl implements ManifoldCoordinate, ObservableCoordinate {

    SimpleObjectProperty<PremiseType> premiseTypeProperty = new SimpleObjectProperty<>(this, TermAux.PREMISE_TYPE_FOR_MANIFOLD.toExternalString(), PremiseType.INFERRED);

    SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.STAMP_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());

    SimpleObjectProperty<LetItemKey> languageCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.LANGUAGE_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());

    SimpleObjectProperty<LetItemKey> logicCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.LOGIC_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    
    Query query;

    public ManifoldCoordinateForQuery() {
    }
    
    public ManifoldCoordinateForQuery(Query query) {
        this.query = query;
        for (Map.Entry<LetItemKey, Object> entry: this.query.getLetDeclarations().entrySet()) {
            if (entry.getValue() instanceof StampCoordinate && 
                    !(entry.getValue() instanceof ManifoldCoordinate)) {
                stampCoordinateKeyProperty.set(entry.getKey());
            } else if (entry.getValue() instanceof LanguageCoordinate && 
                    !(entry.getValue() instanceof ManifoldCoordinate)) {
                languageCoordinateKeyProperty.set(entry.getKey());
            } else if (entry.getValue() instanceof LogicCoordinate && 
                    !(entry.getValue() instanceof ManifoldCoordinate)) {
                logicCoordinateKeyProperty.set(entry.getKey());
            } 
        }
    }
    
    
   @Override
   @XmlElement
   public UUID getManifoldCoordinateUuid() {
      return ManifoldCoordinate.super.getManifoldCoordinateUuid();
   }
   
   @SuppressWarnings("unused")
   private void setManifoldCoordinateUuid(UUID uuid) {
        // noop for jaxb
   }

    public void setQuery(Query query) {
        this.query = query;
    }

    @Override
    public PremiseType getTaxonomyPremiseType() {
        return premiseTypeProperty.get();
    }

    @XmlElement
    public PremiseType getPremiseType() {
        return premiseTypeProperty.get();
    }

    public SimpleObjectProperty<PremiseType> premiseTypeProperty() {
        return premiseTypeProperty;
    }

    public void setPremiseType(PremiseType premiseType) {
        this.premiseTypeProperty.set(premiseType);
    }

    @XmlElement
    public LetItemKey getStampCoordinateKey() {
        return stampCoordinateKeyProperty.get();
    }

    public SimpleObjectProperty<LetItemKey> stampCoordinateKey() {
        return stampCoordinateKeyProperty;
    }

    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKeyProperty.set(stampCoordinateKey);
    }

    @XmlElement
    public LetItemKey getLanguageCoordinateKey() {
        return languageCoordinateKeyProperty.get();
    }

    public SimpleObjectProperty<LetItemKey> languageCoordinateKeyProperty() {
        return languageCoordinateKeyProperty;
    }

    public void setLanguageCoordinateKey(LetItemKey languageCoordinateKey) {
        this.languageCoordinateKeyProperty.set(languageCoordinateKey);
    }

    @XmlElement
    public LetItemKey getLogicCoordinateKey() {
        return logicCoordinateKeyProperty.get();
    }

    public SimpleObjectProperty<LetItemKey> logicCoordinateKeyProperty() {
        return logicCoordinateKeyProperty;
    }

    public void setLogicCoordinateKey(LetItemKey logicCoordinateKey) {
        this.logicCoordinateKeyProperty.set(logicCoordinateKey);
    }

    @Override
    public StampCoordinate getStampCoordinate() {
        return (StampCoordinate) this.query.getLetDeclarations().get(stampCoordinateKeyProperty.get());
    }

    @Override
    public LanguageCoordinate getLanguageCoordinate() {
        return (LanguageCoordinate) this.query.getLetDeclarations().get(languageCoordinateKeyProperty.get());
    }

    @Override
    public LogicCoordinate getLogicCoordinate() {
        return (LogicCoordinate) this.query.getLetDeclarations().get(logicCoordinateKeyProperty.get());
    }
    
    @Override
    /**
     * Note, ManifoldCoordinateForQuery doesn't support distinct destination stamp coordinates.  This always returns
     * {@link #getStampCoordinate()}
     */
    public StampCoordinate getDestinationStampCoordinate() {
        // TODO maybe support this some day...
        return getStampCoordinate();
    }

    @Override
    public ManifoldCoordinate makeCoordinateAnalog(PremiseType taxonomyType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ManifoldCoordinate deepClone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StampCoordinate getImmutableAllStateAnalog() {
        return getStampCoordinate().getImmutableAllStateAnalog(); 
    }

}
