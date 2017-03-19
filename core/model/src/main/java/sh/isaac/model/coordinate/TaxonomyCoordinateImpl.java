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



package sh.isaac.model.coordinate;

//~--- JDK imports ------------------------------------------------------------

import java.beans.Transient;

import java.util.Objects;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.TaxonomyCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@XmlRootElement(name = "taxonomyCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class TaxonomyCoordinateImpl
         implements TaxonomyCoordinate {
   transient int      isaConceptSequence = TermAux.IS_A.getConceptSequence();
   PremiseType        taxonomyType;
   @XmlJavaTypeAdapter(AnyTypeAdapter.class)
   StampCoordinate    stampCoordinate;
   @XmlJavaTypeAdapter(AnyTypeAdapter.class)
   LanguageCoordinate languageCoordinate;
   @XmlJavaTypeAdapter(AnyTypeAdapter.class)
   LogicCoordinate    logicCoordinate;
   UUID               uuid;

   //~--- constructors --------------------------------------------------------

   private TaxonomyCoordinateImpl() {
      // for jaxb
   }

   public TaxonomyCoordinateImpl(PremiseType taxonomyType,
                                 StampCoordinate stampCoordinate,
                                 LanguageCoordinate languageCoordinate,
                                 LogicCoordinate logicCoordinate) {
      this.taxonomyType       = taxonomyType;
      this.stampCoordinate    = stampCoordinate;
      this.languageCoordinate = languageCoordinate;
      this.logicCoordinate    = logicCoordinate;
      this.uuid               = UUID.randomUUID();
   }

   //~--- methods -------------------------------------------------------------

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
   public int hashCode() {
      int hash = 3;

      hash = 53 * hash + Objects.hashCode(this.taxonomyType);
      hash = 53 * hash + Objects.hashCode(this.stampCoordinate);
      hash = 53 * hash + Objects.hashCode(this.languageCoordinate);
      return hash;
   }

   @Override
   public TaxonomyCoordinateImpl makeAnalog(long stampPositionTime) {
      return new TaxonomyCoordinateImpl(this.taxonomyType,
                                        this.stampCoordinate.makeAnalog(stampPositionTime),
                                        this.languageCoordinate,
                                        this.logicCoordinate);
   }

   @Override
   public TaxonomyCoordinateImpl makeAnalog(PremiseType taxonomyType) {
      return new TaxonomyCoordinateImpl(taxonomyType, this.stampCoordinate, this.languageCoordinate, this.logicCoordinate);
   }

   @Override
   public TaxonomyCoordinateImpl makeAnalog(State... state) {
      return new TaxonomyCoordinateImpl(this.taxonomyType,
                                        this.stampCoordinate.makeAnalog(state),
                                        this.languageCoordinate,
                                        this.logicCoordinate);
   }

   @Override
   public String toString() {
      return "TaxonomyCoordinate{" + this.taxonomyType + ",\n" + this.stampCoordinate + ", \n" + this.languageCoordinate + ", \n" +
             this.logicCoordinate + ", uuid=" + this.uuid + '}';
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getIsaConceptSequence() {
      return this.isaConceptSequence;
   }

   @Override
   public LanguageCoordinate getLanguageCoordinate() {
      return this.languageCoordinate;
   }

   @Override
   public LogicCoordinate getLogicCoordinate() {
      return this.logicCoordinate;
   }

   @Override
   public StampCoordinate getStampCoordinate() {
      return this.stampCoordinate;
   }

   @Override
   public PremiseType getTaxonomyType() {
      return this.taxonomyType;
   }

   @Override
   public UUID getUuid() {
      return this.uuid;
   }

   //~--- inner classes -------------------------------------------------------

   private static class AnyTypeAdapter
           extends XmlAdapter<Object, Object> {
      @Override
	public Object marshal(Object v) {
         return v;
      }

      @Override
	public Object unmarshal(Object v) {
         return v;
      }
   }
}

